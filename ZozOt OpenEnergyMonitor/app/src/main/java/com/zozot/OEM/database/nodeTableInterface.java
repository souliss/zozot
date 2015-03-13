package com.zozot.OEM.database;

import android.provider.BaseColumns;

public interface nodeTableInterface extends BaseColumns
{
	String TABLE_NAME = "SoulissDevices";

	String NODE_NAME = "nodeName"; 
	String NODE_NUMBER = "node";
	String DEVICE_NUMBER = "device";
	String STREAM_NAME= "streamName";
	String ENABLED="enabled";
	String TYPICAL_NUMBER = "typical";
 
	String[] COLUMNS = new String[]
	{ _ID,NODE_NAME, NODE_NUMBER, DEVICE_NUMBER, TYPICAL_NUMBER ,STREAM_NAME,ENABLED };

	
}