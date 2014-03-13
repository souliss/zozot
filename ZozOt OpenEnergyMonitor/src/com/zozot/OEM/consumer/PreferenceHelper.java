package com.zozot.OEM.consumer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper implements Serializable {
	
	// Identificatore delle preferenze dell'applicazione
    private final static String MY_PREFERENCES = "MyPref";
	
	private static final long serialVersionUID = 1L;
	private Context contx;
	private String myApiKey;
	private String myFeedId;
	private String myDatastreamName;
	private String myUrl;
	SharedPreferences.Editor editor;
	private String myInterval="0";
	private boolean bPushServiceState=false;

	public boolean isConfigured=false;
	
	

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
		} catch (Exception e) {
			
		}
		//setMyDatastreamName(prefs.getString("DatastreamName", ""));
		setMyUrl(prefs.getString("UrlZozzariello", "http://127.0.0.1:8080/structure?all"));
		
		setMyServiceState(prefs.getBoolean("pushState", false));
		//setMyUrl("http://127.0.0.1:8080/structure?all");
		
		 
		
				/*lightTheme = prefs.getBoolean("checkboxHoloLight", true);
		IPPreference = prefs.getString("edittext_IP", "");
		IPPreferencePublic = prefs.getString("edittext_IP_pubb", "");
		DimensTesto = prefs.getString("listPref", "0");
		PrefFont = prefs.getString("fontPref", "Futura.ttf");
		remoteTimeoutPref = Integer.parseInt(prefs.getString("remoteTimeout", "10000"));
		dataServiceInterval = prefs.getInt("updateRate", 10) * 1000;
		homeThold = prefs.getInt("distanceThold", 150);
		dataServiceEnabled = prefs.getBoolean("checkboxService", false);
		webserverEnabled = prefs.getBoolean("webserverEnabled", false);
		userIndex = prefs.getInt("userIndex", -1);
		nodeIndex = prefs.getInt("nodeIndex", -1);
		animations = prefs.getBoolean("checkboxAnimazione", true);
		antitheftPresent = prefs.getBoolean("antitheft", false);
		antitheftNotify = prefs.getBoolean("antitheftNotify", false);
		eqLow= prefs.getFloat("eqLow", 1f);
		eqMed= prefs.getFloat("eqMed", 1f);
		eqHigh= prefs.getFloat("eqHigh", 1f);
		Calendar fake = Calendar.getInstance();
		fake.add(Calendar.MONTH, -2);//Default value in the past
		serviceLastrun= prefs.getLong("serviceLastrun", Calendar.getInstance().getTimeInMillis());
		nextServiceRun= prefs.getLong("nextServiceRun", fake.getTimeInMillis());
		try {
			ListDimensTesto = Float.valueOf(DimensTesto);
		} catch (Exception e) {
			ListDimensTesto = 14;
		}
*/
		/*
		 * try { cachedInet = InetAddress.getByName(IPPreference); } catch
		 * (UnknownHostException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
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


//	public String getMyDatastreamName() {
//		return myDatastreamName;
//	}
//
//
//	public void setMyDatastreamName(String myDatastreamName) {
//		this.myDatastreamName = myDatastreamName;
//		editor.putString("myDatastreamName", myDatastreamName);
//		editor.commit();
//	}
	
	
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
	
	public void setMyInterval(String sInterval){
		
		//	this.myInterval=Long.parseLong(sInterval);
			this.myInterval=sInterval;
			editor.putString("scheduleInterval", this.myInterval);
		editor.commit();	
		
	}
	public long getMyInterval(){
		try {
			return Long.parseLong(myInterval);	
			} catch (NumberFormatException e) {
				return 0;
			}
		

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
	
}
