package com.caihua.mybluetooth;

import java.util.UUID;



import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.Context;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MyBlueToothManager {
	private BluetoothManager bluetoothManager;
	private BluetoothAdapter adapter;
	
	@SuppressLint("NewApi")
	public MyBlueToothManager(Context context){
		bluetoothManager=(BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
		adapter=bluetoothManager.getAdapter();
		if(adapter==null){
			Toast.makeText(context, "adapter is null",Toast.LENGTH_SHORT).show();
		}
		
	}
	/**
	 * 判断blueTooth设备是否打开
	 * 
	 * @return
	 */
	public boolean isBlueToothEnabled(){
		return adapter.isEnabled();
	}
	
//	/**
//	 * 向用户请求开启blueTooth 用户可以拒绝
//	 * 暂时无用
//	 * 
//	 * @param context
//	 */
//	private void blueToothEnabledRequest(Context context){
//		
//		Intent intent=new Intent();
//		intent.setAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//		
//	}
	
	/**
	 * 
	 *搜索所有blueTooth le设备
	 * 
	 * @param onClickListener 实现leScanCallBack接口的对象
	 * @return
	 */
	public boolean startLeScan(LeScanCallback callback){
		return adapter.startLeScan(callback);
	}
	/**
	 *  
	 * 扫描指定uuids的blueTooth Le设备
	 * 
	 * @param callback 实现leScanCallBack接口的对象
	 * @param serviceUuids 指定的uuids
	 * @return
	 */
	@SuppressLint("NewApi")
	public boolean startLeScan(LeScanCallback callback,UUID[] serviceUuids){
		return adapter.startLeScan(serviceUuids, callback);
	}
	
	
	/**
	 * 停止扫描blueTooth le设备
	 * 
	 * @param callback
	 */
	public void stopLeScan(LeScanCallback callback){
		adapter.stopLeScan(callback);
	}
	
	
}
