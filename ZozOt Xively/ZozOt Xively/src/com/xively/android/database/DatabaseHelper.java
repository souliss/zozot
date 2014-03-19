package com.xively.android.database;

import java.text.MessageFormat;
import java.util.ArrayList;

import com.xively.android.consumer.Device;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "ZozOtXively.db";
	private static final int SCHEMA_VERSION = 1;
		
	public DatabaseHelper(Context context)
	{
		super(context, DATABASE_NAME, null, SCHEMA_VERSION);
	}
	
		
	public void onCreate(SQLiteDatabase db)
	{
		String sql = "CREATE TABLE {0} ({1} INTEGER PRIMARY KEY AUTOINCREMENT," + 
			" {2} TEXT NOT NULL,{3} TEXT NOT NULL, {4} TEXT NOT NULL, {5} TEXT NOT NULL ,{6} INTEGER, {7} BOOLEAN);";
		db.execSQL(MessageFormat.format(sql, nodeTableInterface.TABLE_NAME, nodeTableInterface._ID,
				nodeTableInterface.NODE_NAME,nodeTableInterface.NODE_NUMBER,nodeTableInterface.DEVICE_NUMBER,nodeTableInterface.TYPICAL_NUMBER,nodeTableInterface.STREAM_NAME,nodeTableInterface.ENABLED ));
		
		sql = "CREATE TABLE {0} ({1} INTEGER PRIMARY KEY AUTOINCREMENT, {2} TEXT NOT NULL);";
			db.execSQL(MessageFormat.format(sql, streamTableInterface.TABLE_NAME, streamTableInterface._ID,	streamTableInterface.STREAM_NAME));
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public void insertSoulissDevice(SQLiteDatabase db, String sNomeNodo, String sNumeroNodo, String sNumeroDevice, String sTypical, String sStreamName, boolean bEnabled)
	{
		ContentValues v = new ContentValues();
		v.put(nodeTableInterface.NODE_NAME, sNomeNodo);
		v.put(nodeTableInterface.NODE_NUMBER, sNumeroNodo);
		v.put(nodeTableInterface.DEVICE_NUMBER, sNumeroDevice);
		v.put(nodeTableInterface.TYPICAL_NUMBER, sTypical);
		v.put(nodeTableInterface.STREAM_NAME, sStreamName);	
		v.put(nodeTableInterface.ENABLED, bEnabled);				
		db.insert(nodeTableInterface.TABLE_NAME, null, v);
	}

	public void insertStream(SQLiteDatabase db, String sNomeStream)
	{
		ContentValues v = new ContentValues();
		v.put(streamTableInterface.STREAM_NAME, sNomeStream);
		
		db.insert(streamTableInterface.TABLE_NAME, null, v);
	}
	
	public void deleteStreams(SQLiteDatabase db){
		db.delete(streamTableInterface.TABLE_NAME, null, null);	
	}
	public void deleteSoulissDevices(SQLiteDatabase db){
		db.delete(nodeTableInterface.TABLE_NAME, null, null);	
	}


	public ArrayList<String> readDBStreams(SQLiteDatabase readableDatabase, ArrayList<String> aXivelyFeeds) {
		if (aXivelyFeeds!= null && aXivelyFeeds.size()>0 ) aXivelyFeeds.clear();
		Cursor myCursor= getReadableDatabase().query(streamTableInterface.TABLE_NAME, streamTableInterface.COLUMNS,null,null,null, null,null);
		try
		{
			int iColonna=myCursor.getColumnIndex(streamTableInterface.STREAM_NAME);
			while (myCursor.moveToNext())
			{
				aXivelyFeeds.add(myCursor.getString(iColonna));
			}
		}
		finally
		{
			myCursor.close();
		}
		
			return aXivelyFeeds;
	}


	public ArrayList<Device> readDBSoulissDevices(SQLiteDatabase readableDatabase, ArrayList<Device> aSoulissDevices) {
		if (aSoulissDevices!= null && aSoulissDevices.size()>0 ) aSoulissDevices.clear();
		Cursor myCursor= getReadableDatabase().query(nodeTableInterface.TABLE_NAME, nodeTableInterface.COLUMNS,null,null,null, null,null);
		try
		{
			int iColonna_NODE_NAME=myCursor.getColumnIndex(nodeTableInterface.NODE_NAME);
			int iColonna_NODE_NUMBER=myCursor.getColumnIndex(nodeTableInterface.NODE_NUMBER);
			int iColonna_DEVICE_NUMBER=myCursor.getColumnIndex(nodeTableInterface.DEVICE_NUMBER);
			int iColonna_TYPICAL_NUMBER=myCursor.getColumnIndex(nodeTableInterface.TYPICAL_NUMBER);
			int iColonna_STREAM_NAME=myCursor.getColumnIndex(nodeTableInterface.STREAM_NAME);
			int iColonna_ENABLED=myCursor.getColumnIndex(nodeTableInterface.ENABLED);
			while (myCursor.moveToNext())
			{
				Device d =new Device(Integer.parseInt(myCursor.getString(iColonna_NODE_NUMBER)), Integer.parseInt(myCursor.getString(iColonna_DEVICE_NUMBER)), Integer.parseInt(myCursor.getString(iColonna_TYPICAL_NUMBER)), myCursor.getString(iColonna_NODE_NAME));
				d.setsStream(myCursor.getString(iColonna_STREAM_NAME));
				if (myCursor.getInt(iColonna_ENABLED)==0){
					d.setbEnabled(false);	
				}else{
					d.setbEnabled(true);
				}
					
				
				aSoulissDevices.add(d);
			}
		}
		finally
		{
			myCursor.close();
		}
		
			return aSoulissDevices;
	
	}
	
}
