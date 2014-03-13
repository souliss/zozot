package com.zozot.OEM.consumer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Intent;

public class Constants {
	public static final String TAG = "App - OpenEnergyMonitor";

	public static final float[] roundedCorners = new float[] { 5,5,5,5,5,5,5,5 };
	public static final int versionNumber = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
	public static final SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
	public static final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy-MM-dd");

	public static final int GUI_UPDATE_INTERVAL = 5000;

	//public static final int   CHECK_STATUS_PAUSE_MSEC = 250;
	
	//public static final long TEXT_SIZE_TITLE_OFFSET = 10;

	public static final int CONNECTION_NONE = -1;
	
	public static final int PUSH_TO_CLOUD = 0;
	public static final int SOULISS_DEVICES = 1;
	public static final int ASSOCIATIONS_ACTIVITY = 11;
	public static final int PREFERENCES_ACTIVITY = 22;
	public static final int CLOUD_PUSH_SERVICE = 33;
	
	public static final String  NULL_STREAM_NAME = "------------";

	public static final int CONNECTION_RETRY_NUMBERS = 6;

	public static final int PUSH_RETRY_NUMBERS = 5;

	public static final String  PUSH_RESULT_UPDATE_INTENT_ACTION_NAME = "PushOperationResultOEM";

	public static final String COUNTDOWN_KEY = "CountdownKey";
	public static final String PUSH_RESULT_UPDATE_KEY = "PushValue";

	public static int iSetupHealtLevelToSetNullValue=10;

	

	
}
