package com.caihua.mybluetooth;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDBOpenHelper extends SQLiteOpenHelper {
	public static final String DB_NAME = "Info.db";// 数据库名称
	public static final int DB_VERSION = 1;// 数据库版本号

	/**
	 * 
	 * @param context
	 *            应用程序上下文
	 * @param name
	 *            数据库的名字
	 * @param factory
	 *            查询数据库的游标工厂 一般情况下 用sdk默认的
	 * @param version
	 *            数据库的版本 版本号必须不小1
	 * 
	 */
	public MyDBOpenHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	// 在mydbOpenHelper 在数据库第一次被创建的时候 会执行onCreate();
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		// 第一次调用getWritableDatabase();或getReadableDatabase();时会执行
		// 这个方法通常用来创建表,和初始数据的
		String sql = "create table temperature(pid integer primary key autoincrement,timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,temperature float(64))";
		db.execSQL(sql);
		System.out.println("-->数据库创建成功");
		
	}
	
	@Override
	public void onOpen(SQLiteDatabase db){
		// TODO Auto-generated method stub
		super.onOpen(db);
		System.out.println("-->加载数据库");
		
	}
	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
}
