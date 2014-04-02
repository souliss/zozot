package com.zozot.OEM.consumer;



import com.zozot.OEM.consumer.PreferenceHelper;
import com.zozot.OEM.consumer.R;

import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;



public class PreferencesActivity extends PreferenceActivity {
	PreferenceHelper opzioni;
	static String TAG=Constants.TAG_LivelloAvvisiApplicazione;    
	
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
