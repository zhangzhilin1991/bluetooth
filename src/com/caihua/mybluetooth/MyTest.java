package com.caihua.mybluetooth;

import java.util.List;

import android.test.AndroidTestCase;

public class MyTest extends AndroidTestCase {

	public MyTest() {
		// ToDo Auto-generated constructor stub
	}

	public void initTable() {
		MyDBOpenHelper dbManager = new MyDBOpenHelper(getContext());
		dbManager.getWritableDatabase();

	}

	public void insert() {
		String sql = "insert into temperature (temperature) values (?)";
		Object[] bindArgs = {"39.5"};
		System.out.println("-->插入数据");
		DBManger manger = new DBManger(getContext());
		manger.insertBySQL(sql, bindArgs);
	}
	
    //查询数据
	public void query() {
		String sql = "select * from temperature ";
		//String[] selsectArgs = {};
		DBManger manger = new DBManger(getContext());
//		manger.getDataBAseConn();
		List<Data> datas=manger.query(sql, null);
		System.out.println("---------");
		for(int i=0;i<datas.size();i++){
			System.out.println("temp:"+datas.get(i).getTemperature()+" +date:"+datas.get(i).getDate());
		}
	}

}