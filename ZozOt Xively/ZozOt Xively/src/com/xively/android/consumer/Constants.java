package com.xively.android.consumer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Intent;

public class Constants {
	public static final String TAG_LivelloAvvisiApplicazione="ZozOtXively";
	public static final String TAG_LivelloAvvisiServizio="ZozOtService";
	
		
	
	public static final float[] roundedCorners = new float[] { 5,5,5,5,5,5,5,5 };
	public static final int versionNumber = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
	public static final SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
	public static final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy-MM-dd");

	public static final int GUI_UPDATE_INTERVAL = 5000;

	//public static final int   CHECK_STATUS_PAUSE_MSEC = 250;
	
	//public static final long TEXT_SIZE_TITLE_OFFSET = 10;

	public static final int CONNECTION_NONE = -1;
	
	public static final int RETRIEVING = 0;
	public static final int PUSH = 1;
	public static final int ASSOCIATIONS_ACTIVITY = 111;
	public static final int PREFERENCES_ACTIVITY = 222;
	public static final int XIVELY_PUSH_SERVICE = 333;
	
	public static final String  NULL_XIVELY_STREAM_NAME = "------------";

	public static final int CONNECTION_RETRY_NUMBERS = 6;

	public static final int PUSH_RETRY_NUMBERS = 5;

	public static final String  PUSH_RESULT_UPDATE_INTENT_ACTION_NAME = "PushOperationResult";
	public static final String  PUSH_POWER_TYPICALS_RESULT_UPDATE_INTENT_ACTION_NAME = "PushOperationResultForPowerTypicals";

	public static final String COUNTDOWN_KEY = "CountdownKey";
	public static final String PUSH_RESULT_UPDATE_KEY = "PushValue";
	public static final Integer PUSH_MIN_PACKET = 10;
	public static final Integer PUSH_MAX_PACKET = 1000; //massimo 1000 elementi per volta caricabili su xively

	public static int iSetupHealtLevelToSetNullValue=10;

	

	
}
