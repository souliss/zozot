package com.zozot.OEM.consumer;

import static com.zozot.OEM.consumer.Constants.TAG;

import com.zozot.OEM.consumer.R;
import com.zozot.OEM.consumer.PreferenceHelper;

import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class PreferencesActivity extends PreferenceActivity {
	PreferenceHelper opzioni;
	
	Intent myIntent;
	@TargetApi(11)
	public void onBuildHeaders(List<Header> target) {
		Log.i(TAG, "PreferenceActivityonBuildHeaders()");
		//loadHeadersFromResource(R.xml.preference_headers, target);
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		Log.i(TAG, "PreferenceActivity - getOpzioni() ");
		opzioni = ZozOtActivity.getOpzioni();
		
		addPreferencesFromResource(R.xml.preference_headers);
		
	}
		
	

	@SuppressLint("NewApi")
	@Override
	protected void onStart() {
		super.onStart();
	
			Log.d(TAG, "Going thru preference onStart()");
			if (opzioni!=null) {
				optionReload();
			}
			
	}
		
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		//unregisterReceiver(macacoRawDataReceiver);
		super.onPause();
		optionReload();
		
		Intent risposta = new Intent();
	//risposta.putStringArrayListExtra(getPackageName(), aStreamsXSoulissDevice);
		//Restituisco il codice identificativo dell'activity corrente e la risposta
		setResult(RESULT_OK, risposta);
		  finish();
	}
	
	
			private void optionReload(){
				opzioni.reload();
			//	PushService(opzioni.getMyServiceState()); 
			}
			
		 
}
