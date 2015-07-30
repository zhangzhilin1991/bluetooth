package com.caihua.mybluetooth;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends Activity  {
	public static final String TAG = "BluetoothLE";

	private final Messenger mMessenger;
	private Intent mServiceIntent;
	private Messenger mService = null;
	
	private TextView temperature;
	private BleService.State mState = BleService.State.UNKNOWN;

	private MenuItem mRefreshItem = null;

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = new Messenger(service);
			try {
				Message msg = Message.obtain(null, BleService.MSG_REGISTER);
				if (msg != null) {
					msg.replyTo = mMessenger;
					mService.send(msg);
				} else {
					mService = null;
				}
			} catch (Exception e) {
				Log.w(TAG, "Error connecting to BleService", e);
				mService = null;
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
	};

	public MainActivity() {
		super();
		mMessenger = new Messenger(new IncomingHandler(this));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ble);
		temperature=(TextView) findViewById(R.id.temperature);
		
		mServiceIntent = new Intent(this, BleService.class);
		startService(mServiceIntent);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		if (mService != null) {
			try {
				Message msg = Message.obtain(null, BleService.MSG_UNREGISTER);
				if (msg != null) {
					msg.replyTo = mMessenger;
					mService.send(msg);
				}
			} catch (Exception e) {
				Log.w(TAG, "Error unregistering with BleService", e);
				mService = null;
			} finally {
				unbindService(mConnection);
			}
		}
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//解绑后stopService
		if(mServiceIntent!=null){
		stopService(mServiceIntent);
		Log.d(TAG," stop service");
		}
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		//mDeviceListFragment = DeviceListFragment.newInstance(null);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
	
		return super.onOptionsItemSelected(item);
	}

	private static class IncomingHandler extends Handler {
		private final WeakReference<MainActivity> mActivity;

		public IncomingHandler(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MainActivity activity = mActivity.get();
			if (activity != null) {
				switch (msg.what) {
					case BleService.MSG_STATE_CHANGED:
						//activity.stateChanged(BleService.State.values()[msg.arg1]);
						break;
					
					case BleService.MSG_DEVICE_DATA:
						float temperature = msg.arg1 / 10f;
                        float humidity =  0 ;
						//activity.mDisplay.setData(temperature, humidity);
                        activity.temperature.setText(temperature+"°C");
						break;
				}
			}
			super.handleMessage(msg);
		}
	}
	
	public void onClick(View v){
		Intent intent=new Intent(this,Chart.class);
		startActivity(intent);
	}

//	private void stateChanged(BleService.State newState) {
//		boolean disconnected = mState == BleService.State.CONNECTED;
//		mState = newState;
//		switch (mState) {
//			case SCANNING:
//				mRefreshItem.setEnabled(true);
//		
//				break;
//			case BLUETOOTH_OFF:
//				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//				startActivityForResult(enableBtIntent, ENABLE_BT);
//				break;
//			case IDLE:
//			
//			
//				break;
//			case CONNECTED:
//				FragmentTransaction tx = getFragmentManager().beginTransaction();
//				tx.replace(R.id.main_content, mDisplay);
//				tx.commit();
//				break;
//		}
//	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			new AlertDialog.Builder(MainActivity.this).setTitle("退出程序？").setPositiveButton("是",new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					MainActivity.this.finish();
				}
			}).setNegativeButton("否",null).create().show();
		}
		return super.onKeyDown(keyCode, event);
	}
}