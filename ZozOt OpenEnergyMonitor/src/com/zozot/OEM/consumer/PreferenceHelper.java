package com.zozot.OEM.consumer;

import java.io.Serializable;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PreferenceHelper implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Context contx;
	private String myApiKey;
	private String myFeedId;
	private String myUrl;
	SharedPreferences.Editor editor;
	private String myInterval="0";
	private String myIntervalForPowerTypical="0";
	
	
	private boolean bPushServiceState=false;
	private boolean startOnBoot = false;
	
	public boolean isConfigured=false;

	private int[] powerTypicalsArray;

	//array che contiene la lista dei datapoints ancora da inserire
	//SortedSet<DataPoint> sortedSetDataPoints= new TreeSet<DataPoint>(); 
	//SortedSet<DataPoint> sortedSetDataPoints;
	
	private static final String TAG =  Constants.TAG_LivelloAvvisiApplicazione;


	
	

	public PreferenceHelper(Context contx) {
		super();
		this.contx = contx;
	
		//contx.getSharedPreferences("preferenze", Activity.MODE_PRIVATE);
		initializePrefs();
		// Log.d(TAG, "Constructing prefs");
	}
	
	
	public void initializePrefs() {
		// Get the xml/preferences.xml preferences
	
		
		//PreferenceManager.setDefaultValues(contx, R.xml.preference_headers, false);
		SharedPreferences prefs  = PreferenceManager.getDefaultSharedPreferences(contx);
		
		editor = prefs.edit();
		
		
		setMyApiKey(prefs.getString("APIKey",""));
		try {
			setMyFeedId(prefs.getString("FeedId", ""));	
		} catch (Exception e) {
					}
		try {
		setMyInterval(prefs.getString("scheduleInterval", "0"));
		setMyIntervalForPowerTypical(prefs.getString("scheduleIntervalPowerRetrieving", "0"));
		} catch (Exception e) {
			
		}
		//setMyDatastreamName(prefs.getString("DatastreamName", ""));
		setMyUrl(prefs.getString("UrlZozzariello", "http://127.0.0.1:8080/structure?all"));
		
		setMyServiceState(prefs.getBoolean("pushState", false));
		setStartOnBoot(prefs.getBoolean("startOnBoot", false));
		
		
		
	}

	public String getMyApiKey() {
		return myApiKey;
	}


	public void setMyApiKey(String myApiKey) {
		this.myApiKey = myApiKey;
		editor.putString("APIKey", myApiKey);
		editor.commit();
		isConfiguredCheck();
		
	}


	public int getMyFeedId() {
		try {
			return Integer.parseInt(myFeedId);
			} catch (NumberFormatException e) {
				return 0;
			}
	}


	public void setMyFeedId(String myFeedId) {
		this.myFeedId = myFeedId;
		editor.putString("FeedId", myFeedId);
		editor.commit();
		isConfiguredCheck();
		
	}


private void isConfiguredCheck() {
	if(this.myApiKey!="")
		isConfigured=true;
	 else
		 isConfigured=false;
		
	}
	
	public void reload() {
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contx);
		initializePrefs();
		
	}

	public String getMyUrl() {
		return myUrl;
	}

	public void setMyUrl(String myUrl) {
		this.myUrl = myUrl;
		editor.putString("UrlZozzariello", this.myUrl);
		editor.commit();
	}
	
	public long getMyInterval(){
		try {
			return Long.parseLong(myInterval);	
			} catch (NumberFormatException e) {
				return 0;
			}
}

	public void setMyInterval(String sInterval){
			this.myInterval=sInterval;
			editor.putString("scheduleInterval", this.myInterval);
		editor.commit();	
	}


	public long getMyIntervalForPowerTypical() {
		try {
			return Long.parseLong(myIntervalForPowerTypical);	
			} catch (NumberFormatException e) {
				return 0;
			}
}

	public void setMyIntervalForPowerTypical(String myIntervalForPowerTypical) {
		this.myIntervalForPowerTypical=myIntervalForPowerTypical;
		editor.putString("scheduleIntervalPowerRetrieving", this.myIntervalForPowerTypical);
		editor.commit();
	}

	public boolean getMyServiceState() {
		// TODO Auto-generated method stub
		return bPushServiceState;
	}
	
	public void setMyServiceState(boolean bPushServiceState) {
		this.bPushServiceState=bPushServiceState;
		editor.putBoolean("pushState", this.bPushServiceState);
		editor.commit();
	}

	public void setPowerTypicalsArray(int[] powerTypicalsArray2) {
		this.powerTypicalsArray= powerTypicalsArray2;
		
	}


	public int[] getPowerTypicalsArray() {
		return powerTypicalsArray;
	}
	
	public String getStringXML(int iRVal){
		try{
			
			return contx.getString(iRVal);		
		}catch(Exception e){
			Log.v(TAG, "ERROR to get string from preferences: " + e.getMessage());
			return "NULL";
		}
}
	
	private void setStartOnBoot(boolean startOnBoot) {
		this.startOnBoot=startOnBoot;
		editor.putBoolean("startOnBoot", this.startOnBoot);
		editor.commit();
		
	}

	public boolean setStartOnBoot() {
		// TODO Auto-generated method stub
		return startOnBoot;
	}
	
}