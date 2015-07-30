package com.caihua.mybluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManger {
	private static final String TAG = "TemperatureDao";
	// private Context context;
	MyDBOpenHelper dbOpenHelper;
	private SQLiteDatabase db;

	public DBManger(Context context) {
		dbOpenHelper = new MyDBOpenHelper(context);
	}

	public void getConnect() {
		// db.openOrCreateDatabase("info.db", null);
	}

	/**
	 * 往数据库添加一条数据
	 */
	public boolean insertBySQL(String sql, Object[] bindArgs) {
		boolean flag = false;
		try {
			SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
			db.execSQL(sql, bindArgs);
			Log.d(TAG,"-->插入数据");
			flag = true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return flag;
	}
	
	
	public List<Data> query(String sql,String[] selectArgs){
		List<Data> list=new ArrayList<Data>();
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery(sql, selectArgs);
		System.out.println("-->开始查询");
		while (cursor.moveToNext()) {
			String date=cursor.getString(cursor.getColumnIndex("datetime(timestamp,'localtime')"));
			System.out.println("date:"+date);
			String temperatrue=cursor.getString(cursor.getColumnIndex("temperature"));
			list.add(new Data(date,temperatrue));
		}
		System.out.println("-->查询成功");
		db.close();
		return list;		
	}

	
	/**
	 * 查找数据库的操作
	 */
	//查询时间
	public List<String> queryD(String sql, String[] selsectArgs) {
		List<String> list=new ArrayList<String>();
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery(sql, selsectArgs);
		while (cursor.moveToNext()) {
			System.out.println("-->"
					+ cursor.getString(cursor.getColumnIndex("date")));
			String date=cursor.getString(cursor.getColumnIndex("date"));
			list.add(date);
		}
		System.out.println("-->查询成功");
		db.close();
		return list;
	}
	// 查询温度
	public List<String> queryT(String sql, String[] selsectArgs) {
		List<String> list=new ArrayList<String>();
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery(sql, selsectArgs);
		while (cursor.moveToNext()) {
			System.out.println("-->"
					+ cursor.getString(cursor.getColumnIndex("temperature")));
			String temperature=cursor.getString(cursor.getColumnIndex("temperature"));
			list.add(temperature);
		}
		System.out.println("-->查询成功");
		db.close();
		return list;
	}
	
//	public Cursor find2(boolean distinct, String table, String[] columns,
//			String selection, String[] selectionArgs, String groupBy,
//			String having, String orderBy, String limit) {
//		Cursor cursor = null;
//		
//		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
//		cursor = db.query(distinct, table, columns, selection, selectionArgs,
//				groupBy, having, orderBy, limit);
//
//		return cursor;
//	}
	
	/**
	 * 删除一条记录
	 * 
	 * @param date
	 */
	public void delete(String date) {
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		if (db.isOpen()) {
			db.execSQL("delete from temperature where date =?",
					new String[] { date });
			db.close();
		}
	}

	public void releaseconn() {
		if (db != null) {
			db.close();
		}

	}

	// 获得数据库的连接
	public void getDataBAseConn() {
		db = dbOpenHelper.getWritableDatabase();

	}

}
