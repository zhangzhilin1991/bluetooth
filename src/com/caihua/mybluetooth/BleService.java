package com.caihua.mybluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.android.soundcommunicate.EncoderCore;
import com.android.soundcommunicate.PowerSupply;

public class BleService extends Service implements
		BluetoothAdapter.LeScanCallback {
	public static final String TAG = "BleService";
	static final int MSG_REGISTER = 0x1001;
	static final int MSG_UNREGISTER = 0x1002;
	static final int MSG_START = 0x1003;
	static final int MSG_STATE_CHANGED = 0x1004;
	static final int MSG_DEVICE_FOUND = 0x1005;
	static final int MSG_DEVICE_CONNECT = 0x1006;
	static final int MSG_DEVICE_DISCONNECT = 0x1007;
	static final int MSG_DEVICE_DATA = 0x1008;
	static final int MSG_NO_DEVICE_FIND = 0x1009;

	private static final long SCAN_PERIOD = 14000;

	public static final String KEY_MAC_ADDRESSES = "KEY_MAC_ADDRESSES";

	private static final UUID UUID_HUMIDITY_SERVICE = UUID
			.fromString("00001809-0000-1000-8000-00805f9b34fb");
	private static final UUID UUID_HUMIDITY_DATA = UUID
			.fromString("00002a1c-0000-1000-8000-00805f9b34fb");
	private static final UUID UUID_HUMIDITY_CONF = UUID
			.fromString("00002a1e-0000-1000-8000-00805f9b34fb");
	private static final UUID UUID_CCC = UUID
			.fromString("00002902-0000-1000-8000-00805f9b34fb");
	private static final byte[] ENABLE_SENSOR = { 0x01, 0x00 }; // open
	private static final byte[] DISABLE_SENSOR = { 0x00, 0x00 };// close

	private static final Queue<Object> sWriteQueue = new ConcurrentLinkedQueue<Object>();
	private static boolean sIsWriting = false;
	private boolean isInsert=false;
	private  IncomingHandler mHandler;
	private  Messenger mMessenger;

	private final List<Messenger> mClients = new LinkedList<Messenger>();
	private final Map<String, BluetoothDevice> mDevice = new HashMap<String, BluetoothDevice>();
	private BluetoothGatt mGatt = null;
	private PowerSupply powerSupply;
	private EncoderCore encoderCore;
	private final int frequency = 3400;
	private DBManger manger;
	private float temperature=0;

	private BluetoothGattService bluetoothGattService;
	private BluetoothGattCharacteristic dataCharacteristic;
	private BluetoothGattCharacteristic configCharacteristic;
	private BluetoothGattDescriptor config;

	public enum State {
		UNKNOWN, IDLE, SCANNING, BLUETOOTH_OFF, CONNECTING, CONNECTED, DISCONNECTING
	}

	private BluetoothAdapter mBluetooth = null;
	private State mState = State.UNKNOWN;

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			super.onConnectionStateChange(gatt, status, newState);
			Log.v(TAG,
					"Connection State Changed: "
							+ (newState == BluetoothProfile.STATE_CONNECTED ? "Connected"
									: "Disconnected"));
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				setState(State.CONNECTED);
				mGatt=gatt;
				gatt.discoverServices();
			} else {
				//mGatt=device.connectGatt(this,true, mGattCallback);
				//mGatt.connect();
				setState(State.IDLE);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.v(TAG, "onServicesDiscovered: " + status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				subscribe(gatt);
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			Log.v(TAG, "onCharacteristicWrite: " +(status==0?"SUCCESS":"FAILED"));
			sIsWriting = false;
			nextWrite();
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			Log.v(TAG, "onDescriptorWrite: " + (status==0?"SUCCESS":"FAILED"));
			sIsWriting = false;
			nextWrite();
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			Log.v(TAG, "onCharacteristicChanged: " + characteristic.getUuid());
			byte[] value=characteristic.getValue();
			Log.d(TAG,"length:"+value.length);
			Log.d(TAG,"hex:"+ByteUtil.toString(value, value.length));
			
			
			int t = shortUnsignedAtOffset(characteristic, 0);

			temperature = (float) (Math.round((t * 0.0625) * 10)) / 10;
			Log.d(TAG,"math"+Math.round((t * 0.0625) * 10)/10);
			Log.d(TAG, ":" + temperature);

			if(!isInsert){
				
				SqlInsert();
				
			}
			Message msg = Message.obtain(null, MSG_DEVICE_DATA);
			msg.arg1 = (int) (temperature * 10);
			sendMessage(msg);
			Log.d(TAG," send data");
		}
	};
	
	
	@Override
	public void onCreate() {
		mHandler = new IncomingHandler(this);
		mMessenger = new Messenger(mHandler);
		powerSupply = new PowerSupply(this,(AudioManager) this.getSystemService(Context.AUDIO_SERVICE));
		encoderCore = new EncoderCore();
		manger = new DBManger(getBaseContext());
	};
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return Service.START_NOT_STICKY;
	}

//	public BleService() {
//		mHandler = new IncomingHandler(this);
//		mMessenger = new Messenger(mHandler);
//		powerSupply = new PowerSupply(this,(AudioManager) this.getSystemService(Context.AUDIO_SERVICE));
//		encoderCore = new EncoderCore();
//	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(powerSupply!=null){
		powerSupply.pwStop();
		}
		if (mBluetooth != null) {
			mBluetooth.stopLeScan(BleService.this);
		}
		//鍏抽棴鏁版嵁涓婁紶
		if(config!=null){
			
		config.setValue(DISABLE_SENSOR);
		//mGatt.setCharacteristicNotification(configCharacteristic, false);
		}
		//鏂紑涓庢湇鍔″櫒杩炴帴
		if(mGatt!=null){
			mGatt.disconnect();
			mGatt.close();
		}
		if(manger!=null){
			manger.releaseconn();
		}
		Log.d(TAG, "service destory");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	private static class IncomingHandler extends Handler {
		private final WeakReference<BleService> mService;

		public IncomingHandler(BleService service) {
			mService = new WeakReference<BleService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			BleService service = mService.get();
			if (service != null) {
				switch (msg.what) {
				case MSG_REGISTER:
					service.mClients.add(msg.replyTo);
					Log.d(TAG, "Registered");
					break;
				case MSG_UNREGISTER:
					service.mClients.remove(msg.replyTo);
//					if (service.mState == State.CONNECTED
//							&& service.mGatt != null) {
//						service.mGatt.disconnect();
//					}
					Log.d(TAG, "Unegistered");
					break;
				case MSG_START:

					service.start();
					Log.d(TAG, "Start");
					break;
				case MSG_DEVICE_CONNECT:
//					service.connect((String) msg.obj);
					break;
				case MSG_DEVICE_DISCONNECT:
					if (service.mState == State.CONNECTED
							&& service.mGatt != null) {
						//service.mGatt.disconnect();
//						service.mGatt.close();
					}
					break;
				default:
					super.handleMessage(msg);
				}
			}
		}
	}

	private void start() {
		mDevice.clear();
		
		setState(State.SCANNING);
		if (mBluetooth == null) {
			BluetoothManager bluetoothMgr = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
			mBluetooth = bluetoothMgr.getAdapter();
		}
		if (mBluetooth == null || !mBluetooth.isEnabled()) {
			setState(State.BLUETOOTH_OFF);
		} else {
			//mBluetooth.stopLeScan(BleService.this);
			powerSupply.pwStart(encoderCore.carrierSignalGen(frequency));
			Log.d(TAG, "enabled bluetooth device");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mState == State.SCANNING) {
						mBluetooth.stopLeScan(BleService.this);
						powerSupply.pwStop();
						setState(State.IDLE);
					}
				}
			}, SCAN_PERIOD);
			//mBluetooth.startLeScan(this);
			mBluetooth.startLeScan(new UUID[] { UUID_HUMIDITY_SERVICE }, this);
		}
	}
	
	/**
	 * 开启后每隔1分钟插入一次数据
	 */
	private void SqlInsert(){
		isInsert=true;
		
		//每隔一分钟插入一次
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String sql = "insert into temperature (temperature)values (?)";
				Object[] bindArgs = {temperature };
				manger.insertBySQL(sql, bindArgs);
				isInsert=false;
			}
		},60*1000);
	}
	@Override
	public void onLeScan(final BluetoothDevice device, int rssi,
			byte[] scanRecord) {
		mBluetooth.stopLeScan(BleService.this);
		powerSupply.pwStop();
		if (device != null && !mDevice.containsValue(device)
				&& device.getName() != null /*
											 * &&
											 * device.getName().equals(DEVICE_NAME
											 * )
											 */) {
			mDevice.put(device.getAddress(), device);
//			Message msg = Message.obtain(null, MSG_DEVICE_FOUND);
//			if (msg != null) {
//				Bundle bundle = new Bundle();
//				String[] addresses = mDevice.keySet().toArray(
//						new String[mDevice.size()]);
//				bundle.putStringArray(KEY_MAC_ADDRESSES, addresses);
//				msg.setData(bundle);
//				sendMessage(msg);
//			}
			BluetoothDevice mDevice = mBluetooth.getRemoteDevice(device.getAddress());
	        if (mDevice == null) {
	            Log.w(TAG, "Device not found.  Unable to connect.");
	            return;
	        }
			if(mGatt==null){
				  mGatt=device.connectGatt(BleService.this, true, mGattCallback);
//				new AsyncTask<Void, Void, Void>() {
//				    @Override
//				    protected Void doInBackground(Void... params) {
//				        device.connectGatt(BleService.this, true, mGattCallback);
//				        return null;
//				    }
//				}.execute();
			}
		}
	}

//	public void connect(String macAddress) {
//		BluetoothDevice device = mDevice.get(macAddress);
//		if (device != null) {
//			mGatt = device.connectGatt(this, true, mGattCallback);
//		}
//	}
	
//	  /**
//     * Connects to the GATT server hosted on the Bluetooth LE device.
//     *
//     * @param address The device address of the destination device.
//     *
//     * @return Return true if the connection is initiated successfully. The connection result
//     *         is reported asynchronously through the
//     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
//     *         callback.
//     */
//    public boolean connect(final String address) {
//        if (mBluetooth == null || address == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
//            return false;
//        }
//
//        // Previously connected device.  Try to reconnect.
//        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
//                && mBluetoothGatt != null) {
//            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//                mConnectionState = STATE_CONNECTING;
//                return true;
//            } else {
//                return false;
//            }
//        }
//
//        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//        if (device == null) {
//            Log.w(TAG, "Device not found.  Unable to connect.");
//            return false;
//        }
//        // We want to directly connect to the device, so we are setting the autoConnect
//        // parameter to false.
//        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
//        Log.d(TAG, "Trying to create a new connection.");
//        mBluetoothDeviceAddress = address;
//        mConnectionState = STATE_CONNECTING;
//        return true;
//    }

	private void subscribe(BluetoothGatt gatt) {
		bluetoothGattService = gatt.getService(UUID_HUMIDITY_SERVICE);
		dataCharacteristic = bluetoothGattService
				.getCharacteristic(UUID_HUMIDITY_DATA);
		Log.d(TAG, "data character:" + dataCharacteristic.getUuid());
		configCharacteristic = bluetoothGattService
				.getCharacteristic(UUID_HUMIDITY_CONF);
		Log.d(TAG, "config character:" + dataCharacteristic.getUuid());

		if (dataCharacteristic != null && configCharacteristic != null) {
			config = configCharacteristic
					.getDescriptor(UUID_CCC);
			Log.d(TAG, "descriptor:" + config.getUuid());
			
			gatt.setCharacteristicNotification(configCharacteristic, true);
			
			byte[] value = config.getValue();
			if (value != null) {
				Log.d(TAG, " config dafault value:" + value[0] + ":" + value[1]);
			}
			config.setValue(ENABLE_SENSOR);
			boolean state = gatt.writeDescriptor(config);
			Log.d(TAG, "write config:" + state);
		}
	}

	private synchronized void write(Object o) {
		if (sWriteQueue.isEmpty() && !sIsWriting) {
			doWrite(o);
		} else {
			sWriteQueue.add(o);
		}
	}

	private synchronized void nextWrite() {
		if (!sWriteQueue.isEmpty() && !sIsWriting) {
			doWrite(sWriteQueue.poll());
		}
	}

	private synchronized void doWrite(Object o) {
		if (o instanceof BluetoothGattCharacteristic) {
			sIsWriting = true;
			mGatt.writeCharacteristic((BluetoothGattCharacteristic) o);
		} else if (o instanceof BluetoothGattDescriptor) {
			sIsWriting = true;
			mGatt.writeDescriptor((BluetoothGattDescriptor) o);
		} else {
			nextWrite();
		}
	}

	private void setState(State newState) {
		if (mState != newState) {
			mState = newState;
			Message msg = getStateMessage();
			if (msg != null) {
				sendMessage(msg);
			}
		}
	}

	private Message getStateMessage() {
		Message msg = Message.obtain(null, MSG_STATE_CHANGED);
		if (msg != null) {
			msg.arg1 = mState.ordinal();
		}
		return msg;
	}

	private void sendMessage(Message msg) {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			Messenger messenger = mClients.get(i);
			if (!sendMessage(messenger, msg)) {
				mClients.remove(messenger);
			}
		}
	}

	private boolean sendMessage(Messenger messenger, Message msg) {
		boolean success = true;
		try {
			messenger.send(msg);
		} catch (RemoteException e) {
			Log.w(TAG, "Lost connection to client", e);
			success = false;
		}
		return success;
	}

	private static Integer shortUnsignedAtOffset(
			BluetoothGattCharacteristic characteristic, int offset) {
		Integer lowerByte = characteristic.getIntValue(
				BluetoothGattCharacteristic.FORMAT_UINT8, offset);
		Integer upperByte = characteristic.getIntValue(
				BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1);
		Log.d(TAG,"data:"+upperByte+":"+lowerByte);
		return (upperByte << 8) + lowerByte;
	}

}