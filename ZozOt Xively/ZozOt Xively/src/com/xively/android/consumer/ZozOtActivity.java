package com.xively.android.consumer;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xively.android.cloudservice.Response;
import com.xively.android.consumer.R;
import com.xively.android.database.DatabaseHelper;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;


import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.xively.android.cloudservice.IHttpService;
import com.xively.android.service.ZozOtService;

/**
 * A primitive activity to demo simple remote client connection to the Xively's
 * AIDL Service service.
 * 
 * @author s0pau
 * 
 */
/**
 * @author User
 *
 */
public class ZozOtActivity extends Activity {
	private static final String TAG =  Constants.TAG_LivelloAvvisiApplicazione;
	IHttpService xivelyService;
	private static volatile Context context;
	private static PreferenceHelper opzioni;
	final DatabaseHelper dbHelper=new DatabaseHelper(this);
	final String contentType = "text/html; charset=UTF-8";
	int iNrFails=Constants.CONNECTION_RETRY_NUMBERS;
		TextView resultField;
	ArrayList<Device> aSoulissDevices= new ArrayList<Device>();
	ArrayList<String> aXivelyFeeds=new ArrayList<String>();
	// ------------------------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.zozotactivity);
		if (context == null)
			context = getApplicationContext();
		setOpzioni(new PreferenceHelper(context));
		 opzioni=getOpzioni();

		//impostazioni della texview per il log		 
		 resultField = (TextView) findViewById(R.id.result);
		//	resultField.setMaxLines(80); 
		 resultField.setMovementMethod(new ScrollingMovementMethod());
		 resultField.setText(MyApplication.getInstance().getTextBoxLog());
		 
		 //inizializzazione servizio per la connessione alla rete ed a xively
		 initService();
	//LETTURA DATABASE
	aXivelyFeeds=dbHelper.readDBStreams(dbHelper.getReadableDatabase(),aXivelyFeeds);
	aSoulissDevices=dbHelper.readDBSoulissDevices(dbHelper.getReadableDatabase(), aSoulissDevices);
	
	//registo il receiver che si occupa di recuperare i messaggi broadcast del service (aggiornamenti da visualizzare sull'activity principale)
	registerReceiver(uiUpdated, new IntentFilter(Constants.PUSH_RESULT_UPDATE_INTENT_ACTION_NAME));
	registerReceiver(uiUpdatedPowerTypicals, new IntentFilter(Constants.PUSH_POWER_TYPICALS_RESULT_UPDATE_INTENT_ACTION_NAME));
	
	
		// Setup the UI
		ToggleButton button1 = (ToggleButton) findViewById(R.id.doTimer);
		
		//ripristina lo stato del pulsante ON
		if (isMyServiceRunning()){
			button1.setChecked(true);
			opzioni.setMyServiceState(true);
		}else{
			button1.setChecked(false);
			opzioni.setMyServiceState(false);
		}
		
		
		button1.setOnClickListener(new OnClickListener() {
		
			public void onClick(View v) {
				// Is the toggle on?
			    boolean on = ((ToggleButton) v).isChecked();
			    if (on) {
			    	if(opzioni.isConfigured) {
			    	if(aSoulissDevices.size()>0){
			    		myIntents2= TimerStart();	
			    	} else {
			    		 Toast.makeText(ZozOtActivity.this, "Acquisire nodi e canali...", Toast.LENGTH_SHORT).show();
			    		 ((ToggleButton) v).setChecked(false);
			    	}} else{
			    		 Toast.makeText(ZozOtActivity.this, "Configurare...", Toast.LENGTH_SHORT).show();
			    		 ((ToggleButton) v).setChecked(false);
			    	}
			    } else {
			    	//TimerStop();
			    	doUnbindService();
			    }
				}	
		});
						
	}

	// GESTIONE CLICK MENU
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
	
			if (id == R.id.options) {
				Intent myIntents = new Intent(this, PreferencesActivity.class);
				startActivityForResult(myIntents,Constants.PREFERENCES_ACTIVITY);
				return true; 
			} else if (id == R.id.streams ) {
				if(opzioni.isConfigured){
				Intent myIntents2 = new Intent(this, AssociationsActivity.class);
				String pkg=getPackageName();
				myIntents2.putParcelableArrayListExtra(pkg+"SoulissDevices", aSoulissDevices);
				myIntents2.putStringArrayListExtra(pkg+"XivelyFeeds", aXivelyFeeds);
				startActivityForResult(myIntents2, Constants.ASSOCIATIONS_ACTIVITY);
				}else{
					 Toast.makeText(ZozOtActivity.this, "Configurare...", Toast.LENGTH_SHORT).show();
				}
					
				return true;
				
			} else if (id == R.id.getNodesAndStream) {
				
				//avvia un thread per riempire gli array con i feeds di Xively ed i devides di Souliss solo quando il servizio di connessione di xively � partito
	if(opzioni.isConfigured){
				final Handler handler = new Handler();
				new Thread(new Runnable() {
	
					@SuppressWarnings("unused")
					@Override
					public void run() {
						Looper.prepare();
						//thread sempre in esecuzione. Controlla che i due array aXivelyFeeds e aSoulissDevices siano sempre pieni.
						boolean isOK=false;	
						while (!isOK && iNrFails>0){
							if (xivelyService==null){
								//pongo la variabile a zero per uscire dal ciclo while
								iNrFails=0;
							}else{
							aXivelyFeeds= getFeeds(dbHelper);	
						
							//prima di tutto cancello in contenuto della tabella soulisDevices sul DB
							dbHelper.deleteSoulissDevices(dbHelper.getWritableDatabase());
	
					 String sURL= opzioni.getMyUrl();
					 //INVIA LA RICHIESTA A SOULISS E RACCOGLIE LA RISPOSTA
					 String s = null;
					 if (sURL!=null && !sURL.isEmpty()){
						 s = SoulissDevicesHelper.getUrlResponse(sURL);
					 }
					// parse JSON data
					try {
						JSONObject jsonObject=new JSONObject(s);
						JSONArray jArray = jsonObject.getJSONArray("id");
						aSoulissDevices.clear();
		//scorre l'array JSON per leggerne il contenuto
						//scorre i nodi, variabile "i"
						JSONArray jArraySlots;
						for (int i = 0; i < jArray.length(); i++) {
							//legge chiave "slot"
							jArraySlots = ((JSONArray)((JSONObject) jArray.get(i)).get("slot"));
							//scorre gli "slot" disponibili
							 for (int j = 0; j < jArraySlots.length(); j++) {
								 //i � il nodo
								 //j � lo slot
								 String sNomeNodo=SoulissDevicesHelper.getSensorItemName(jArray, i, j);
								 int iTypical=SoulissDevicesHelper.getTypical(jArray, i, j);
								 aSoulissDevices.add(new Device(i,j, iTypical, sNomeNodo));
								 dbHelper.insertSoulissDevice(dbHelper.getWritableDatabase(), sNomeNodo, String.valueOf(i),String.valueOf(j), String.valueOf(iTypical), null, false);
							 }
							}
						if(aSoulissDevices != null && aSoulissDevices.size()>0 && aXivelyFeeds != null && aXivelyFeeds.size()>0) {
							 isOK=true;
						 }
					} catch (JSONException e) {
					
						Log.e("JSONException", "Error: " + e.toString());
	
					} // catch (JSONException e)
	
				 catch (Exception e) {
					e.printStackTrace();
					
				}
					
					//COSI' AGGIORNO LA UI facendo apparire i messaggi
						handler.post(new Runnable(){
							int iNodi = 0,iStreams = 0;
		                public void run() {
		                		
					if (aXivelyFeeds != null && aXivelyFeeds.size()>0) {
						 Toast.makeText(ZozOtActivity.this, "Get Xively Feeds OK", Toast.LENGTH_SHORT).show();
						 iStreams=aXivelyFeeds.size();
					}
					else {  
						Toast.makeText(ZozOtActivity.this, "Get Xively Feeds ERROR", Toast.LENGTH_SHORT).show();
						iNrFails--;
					}
								
					 if (aSoulissDevices != null && aSoulissDevices.size()>0) {
				    	Toast.makeText(ZozOtActivity.this, "Get Souliss Devices OK", Toast.LENGTH_SHORT).show();
				    	iNodi=aSoulissDevices.size();
						}
					 else {
						 Toast.makeText(ZozOtActivity.this, "Get Souliss Devices ERROR", Toast.LENGTH_SHORT).show();
						 iNrFails--;
						 }
					 
					 if (iNodi >0 && iStreams>0) Toast.makeText(ZozOtActivity.this, "Trovati " + iNodi +" dispositivi e " + iStreams + " canali", Toast.LENGTH_SHORT).show(); 

					 if(aSoulissDevices != null && aSoulissDevices.size()>0 && aXivelyFeeds != null && aXivelyFeeds.size()>0) {
						 iNrFails=Constants.CONNECTION_RETRY_NUMBERS;
						
					 }
		                
		                }
				});
					}
						}}
	}).start();
			}else {
				 Toast.makeText(ZozOtActivity.this, "Configurare...", Toast.LENGTH_SHORT).show();
			}
	}
			return super.onOptionsItemSelected(item);
		}

	// ABILITA IL MENU
		public boolean onCreateOptionsMenu(Menu main_Menu) {
			getMenuInflater().inflate(R.menu.main_menu, main_Menu);
			return true;
		}

	@Override
	protected void onDestroy() {
		
	//		doUnbindService();
		
			// Muovo i log su file
			Log.w(TAG, "Closing app, moving logs");
			try {
				File filename = new File(Environment.getExternalStorageDirectory() + "/zozot-xively.log");
				filename.createNewFile();
				
				// String cmd = "logcat -d -v time  -f " +
				// filename.getAbsolutePath()
								
				String cmd = "logcat -d -v time  -f " + filename.getAbsolutePath() + " " + Constants.TAG_LivelloAvvisiApplicazione + ":D " + Constants.TAG_LivelloAvvisiServizio+ ":D *:S ";
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e) {
				e.printStackTrace();
			}
			super.onDestroy();
		}
	

	public static Context getAppContext() {
		return ZozOtActivity.context;
	}

	private void setOpzioni(PreferenceHelper opzioni) {
		// Create an ArrayAdapter that will contain all list items
	    ArrayAdapter<String> adapter;

	    int[] powerTypicalsArray = getResources().getIntArray(R.array.powerTypicals);    

	    opzioni.setPowerTypicalsArray(powerTypicalsArray);

		this.opzioni = opzioni;
	}

	public static PreferenceHelper getOpzioni() {
		return opzioni;
	}


	
	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 * Metodo che si occupa di recuperare dati dall'activity secondaria "AssociationsActivity"
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		 
		 		//Il numero 123 serve a identificare chi ci ha restituito la risposta
	    if (requestCode == Constants.ASSOCIATIONS_ACTIVITY) {
	    	if (data !=null){
	    	//Recupero la risposta inviata
	    	aSoulissDevices = data.getParcelableArrayListExtra(getPackageName());
	    	}
	    	
	    }else if (requestCode == Constants.PREFERENCES_ACTIVITY) {
	     
	    	//recupero il valore di stato del servizio push e avvio il timer
	    //	cdt=TimerStart();
	  	
	    }
	    }
			
	private void TimerStop(){
		stopService(new Intent(ZozOtActivity.this,ZozOtService.class));
		Toast.makeText(ZozOtActivity.this, "Push STOP", Toast.LENGTH_SHORT).show();
		opzioni.setMyServiceState(false);
		//doUnbindService();
	}
	

	Intent myIntents2;
	private Intent TimerStart(){
		//avvia il servizio
				
		initService();
		myIntents2 = new Intent(getApplicationContext(), ZozOtService.class);
		String pkg=getPackageName();
		myIntents2.putParcelableArrayListExtra(pkg+"SoulissDevices", aSoulissDevices);

		//se il servizio � gi� avviato allora lo fermo, poi lo riavvio
		if(opzioni.getMyServiceState()){
					
			try {
				TimerStop();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			opzioni.setMyServiceState(false);
		}
			startService(myIntents2);
			
			opzioni.setMyServiceState(true);
		
			return myIntents2;

	}
	
/**
	 * Binds this activity to the service.
	 */
	public void initService() {
			connection = new HttpServiceConnection();
		
		Intent i = new Intent("com.xively.android.cloudservice.HttpService");
		
		boolean ret = bindService(i, connection, Context.BIND_AUTO_CREATE);
		
		Log.d(TAG, "initService() bound with " + ret);
	}

	//riceve i messaggi inviati in broadcast dal service
//in alto, nel metodo onCreate, il receiver � registrato ed � aplicato il filtro: registerReceiver(uiUpdated, new IntentFilter(Constants.PUSH_RESULT_UPDATE_INTENT_ACTION_NAME));
	
	private BroadcastReceiver uiUpdated= new BroadcastReceiver() {
		
		String sValue=null;
		long lValue;
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	
	    	
	    	
	    	sValue=intent.getExtras().getString(Constants.PUSH_RESULT_UPDATE_KEY);
	    	if (sValue!=null){
	    		    	
			resultField.append("\n"+sValue);
			//il codice sotto � stato spostato nella classe ZozOtService, nel punto in cui vengono trasmessi i dati in broadcast. Questo � per registrare i messaggi anche quanto l'acgtivity principale non � in esecuzione
//			//salvo il valore per l'eventuale ripristino in caso di rotazione dello schermo
//			MyApplication.getInstance().setTextBoxLog(resultField.getText().toString());
	    	}
	    	
	    	lValue=intent.getExtras().getLong(Constants.COUNTDOWN_KEY);
	    	//if (lValue>0){
			TextView countdownField = (TextView) findViewById(R.id.textViewProssimoPush);
			countdownField.setText("Push: " + lValue +" seconds");
	    //	}
			
	    }
	};
	HttpServiceConnection connection;
	//riceve i messaggi inviati in broadcast dal service
	//in alto, nel metodo onCreate, il receiver � registrato ed � aplicato il filtro: registerReceiver(uiUpdated, new IntentFilter(Constants.PUSH_RESULT_UPDATE_INTENT_ACTION_NAME));
		
		private BroadcastReceiver uiUpdatedPowerTypicals= new BroadcastReceiver() {
			
			String sValue=null;
			long lValue;
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	
		    	lValue=intent.getExtras().getLong(Constants.COUNTDOWN_KEY);
		    	//if (lValue>0){
				TextView countdownField = (TextView) findViewById(R.id.textViewProssimoPushPowerTypicals);
				countdownField.setText("Power: " + lValue +" seconds ");
		    //	}
				
		    }
		};

	public class HttpServiceConnection implements ServiceConnection {
					
	public void onServiceConnected(ComponentName name, IBinder boundService) {
		xivelyService = IHttpService.Stub.asInterface((IBinder) boundService);
		Log.d(ZozOtActivity.TAG, "onServiceConnected() connected");
		//Toast.makeText(ZozOtActivity.this, "Service connected",Toast.LENGTH_LONG).show();
	}
	
	public void onServiceDisconnected(ComponentName name) {
		xivelyService = null;
		Log.d(ZozOtActivity.TAG, "onServiceDisconnected() disconnected");
		//Toast.makeText(ZozOtActivity.this, "Service connected",Toast.LENGTH_LONG).show();
	}
		}

	/**
		 * getXivelyFeeds()
		 * Restituisce un ArrayList<String> che contiene gli stream di xively disponibili
		 * @param dbHelper 
		 * @return ArrayList<String>
		 */
		private ArrayList<String> getFeeds(DatabaseHelper dbHelper){
			//prima di tutto cancello in contenuto della tabella streams sul DB
			dbHelper.deleteStreams(dbHelper.getWritableDatabase());
			
			Response response = null;
			ArrayList<String> aFeeds = new ArrayList<String>();
			
			try {
				xivelyService.setApiKey(opzioni.getMyApiKey());
				response= xivelyService.getFeed(opzioni.getMyFeedId());
				// PARSING RISPOSTA JSON GET FEED
			if (response.getStatusCode()>-1) {
				JSONObject jObject = new JSONObject(response.getContent());
				JSONArray jArraySlots=jObject.getJSONArray("datastreams");
			
				//LISTA FEED DISPONIBILI
				String sNomeStream;
				for (int j = 0; j < jArraySlots.length(); j++) {
					sNomeStream=jArraySlots.getJSONObject(j).getString("id");
					aFeeds.add(sNomeStream);
					dbHelper.insertStream(dbHelper.getWritableDatabase(), sNomeStream);
				}
			}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
				return null;
			} catch (RemoteException e) {
				Log.e(ZozOtActivity.TAG, "onClick failed", e);
			
				return null;
			}
		
	return aFeeds;
		}
		
		private boolean isMyServiceRunning() {
		    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
		        if ("com.xively.android.service.ZozOtService".equals(service.service.getClassName())) {
		             return true;
		        }
		    }
		    return false;
		}
		
		void doUnbindService() {
			if (isMyServiceRunning()) {
				Log.d(TAG, "UNBIND, Detach our existing connection.");
				TimerStop();
				unbindService(connection);
			}
		}
		
}
