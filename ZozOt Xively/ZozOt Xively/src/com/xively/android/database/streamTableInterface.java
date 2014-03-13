package com.xively.android.database;

import android.provider.BaseColumns;

public interface streamTableInterface extends BaseColumns
{
	String TABLE_NAME = "Streams";

	String STREAM_NAME = "streamName"; 
	 
	String[] COLUMNS = new String[]
	{ _ID, STREAM_NAME};
}