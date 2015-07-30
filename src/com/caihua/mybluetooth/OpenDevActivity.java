package com.caihua.mybluetooth;

import java.lang.ref.WeakReference;

import javax.net.ssl.HandshakeCompletedListener;

import com.caihua.mybluetooth.BleService.State;

import android.R.bool;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import static com.caihua.mybluetooth.BleService.*;
import static android.bluetooth.BluetoothAdapter.*;

public class OpenDevActivity extends Activity {

	private static final String TAG = "openDev";

	private static final int ENABLE_BT = 0x1000;

	private Button mStart;
	private TextView mDatail;
	private Handler handler;
	private Messenger mMessenger;
	private Messenger mService;
	private Intent mInetent;
	private boolean mBind = false;
	private OpenDevActivity instance;
	private MyBlueToothManager blueToothManager;
	

	private boolean flag = false;
	private boolean isConnect = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_opendev);
		instance = OpenDevActivity.this;

		mStart = (Button) findViewById(R.id.btn_open);
		mDatail = (TextView) findViewById(R.id.tv_dev_datail);

		blueToothManager = new MyBlueToothManager(OpenDevActivity.this);

		mInetent = new Intent(OpenDevActivity.this, BleService.class);
		handler = new IncomingHandler(OpenDevActivity.this);
		mMessenger = new Messenger(handler);

		Log.d(TAG, "on start");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		bindService(mInetent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if (mService != null) {
			try {
				Message msg = Message.obtain(null, BleService.MSG_UNREGISTER);
				if (msg != null) {
					msg.replyTo=mMessenger;
					mService.send(msg);
				}
			} catch (Exception e) {
				Log.w(TAG, "Error unregistering with BleService", e);
				mService = null;
			} finally {
				unbindService(mConnection);
				mBind = false;
			}
		}
		super.onStop();
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = new Messenger(service);
			mBind = true;
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
				mBind = false;
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			mService = null;
			mBind = false;
		}
	};

	public void onClick(View v) {
		// 初始化设备在此做，判断模块类型跳转不同activity
		Log.d(TAG, "on click");
		if (!blueToothManager.isBlueToothEnabled()) {
			mDatail.setText("请开启蓝牙");
			startActivityForResult(new Intent(ACTION_REQUEST_ENABLE), ENABLE_BT);
		}
		start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				start();
			}  
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * 发送服务开启并连接设备，蓝牙需先开起
	 */
	private void start() {
		if (mBind = false) {
			mDatail.setText("绑定服务失败，请重试");
			return;
		}

		Message msg = Message.obtain(null, BleService.MSG_START);
		if (msg != null) {
			try {
				mService.send(msg);
			} catch (RemoteException e) {
				Log.w(TAG, "Lost connection to service", e);
				unbindService(mConnection);
			}
		}
	}


	
	private static class IncomingHandler extends Handler {
		private final WeakReference<OpenDevActivity> mActivity;

		public IncomingHandler(OpenDevActivity activity) {
			mActivity = new WeakReference<OpenDevActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			OpenDevActivity activity = mActivity.get();
			if (activity != null) {
				switch (msg.what) {
				
				case MSG_DEVICE_DATA:
					//不重复跳转
					if(activity.isConnect){
						return ;
					}
					//收到数据就跳转
					Log.d(TAG,"receive data start mainActivity");
					// activity.stateChanged(BleService.State.values()[msg.arg1]);
					activity.startActivity(new Intent(activity, MainActivity.class));
					activity.isConnect=true;
					activity.finish();
					break;
				case MSG_NO_DEVICE_FIND:
					//未扫描到设备时，提示
					
					break;
				case MSG_DEVICE_FOUND:
					activity.mDatail.setText("正在连接设备...");
					break;
				
				default:
					super.handleMessage(msg);
				}
				}
			}
		}
	
	
}
