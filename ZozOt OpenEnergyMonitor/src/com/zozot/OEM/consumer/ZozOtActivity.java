package com.zozot.OEM.consumer;


import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.zozot.OEM.cloudservice.Response;
import com.zozot.OEM.consumer.R;
import com.zozot.OEM.database.DatabaseHelper;

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.zozot.OEM.cloudservice.IHttpService;
import com.zozot.OEM.service.ZozOtService;

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
	public static final String TAG = ZozOtActivity.class.getSimpleName();
	IHttpService cloudService;
	//HttpServiceConnection connection;
	private static volatile Context context;
	private static PreferenceHelper opzioni;
	final DatabaseHelper dbHelper=new DatabaseHelper(this);
	final String contentType = "text/html; charset=UTF-8";
	 int iNrFails=Constants.CONNECTION_RETRY_NUMBERS;
		TextView resultField;
	ArrayList<Device> aSoulissDevices= new ArrayList<Device>();
	ArrayList<String> aFeeds=new ArrayList<String>();;
		
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
		 
		 //inizializzazione servizio per la connessione alla rete ed a xively
		 initService();
	//LETTURA DATABASE
	aFeeds=dbHelper.readDBStreams(dbHelper.getReadableDatabase(),aFeeds);
	aSoulissDevices=dbHelper.readDBSoulissDevices(dbHelper.getReadableDatabase(), aSoulissDevices);
	
	//registo il receiver che si occupa di recuperare i messaggi broadcast del service (aggiornamenti da visualizzare sull'activity principale)
	registerReceiver(uiUpdated, new IntentFilter(Constants.PUSH_RESULT_UPDATE_INTENT_ACTION_NAME));
	
	
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
			    	TimerStop();
			    	
			    }
				}	
		});
		

		Button button2 = (Button) findViewById(R.id.doForcePush);
		button2.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
								
				// *****************
				// *****************
				// RICHIESTA DATI ZOZZARIELLO E INVIO A XIVELY
//				Handler handler = new MyHandler();
//				SoulissDevicesHelper thr = new SoulissDevicesHelper(handler,opzioni, aSoulissDevices, service, Constants.PUSH_TO_XIVELY);
//				thr.start();
				
				
				}
		});

		
	}

	// GESTIONE CLICK MENU
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
	
			if (id == R.id.opzioni) {
				Intent myIntents = new Intent(this, PreferencesActivity.class);
				startActivityForResult(myIntents,Constants.PREFERENCES_ACTIVITY);
				return true; 
			} else if (id == R.id.canali) {
				if(opzioni.isConfigured){
				Intent myIntents2 = new Intent(this, AssociationsActivity.class);
				String pkg=getPackageName();
				myIntents2.putParcelableArrayListExtra(pkg+"SoulissDevices", aSoulissDevices);
				myIntents2.putStringArrayListExtra(pkg+"XivelyFeeds", aFeeds);
				startActivityForResult(myIntents2, Constants.ASSOCIATIONS_ACTIVITY);
				}else{
					 Toast.makeText(ZozOtActivity.this, "Configurare...", Toast.LENGTH_SHORT).show();
				}
					
				return true;
				
			} else if (id == R.id.GetNodesAndStream) {
				
				//avvia un thread per riempire gli array con i feeds di Xively ed i devides di Souliss solo quando il servizio di connessione di xively è partito
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
				//		if (service!=null && (aXivelyFeeds==null || aSoulissDevices==null || aXivelyFeeds.size()==0 || aSoulissDevices.size()==0)) {
							if (cloudService==null){
								//pongo la variabile a zero per uscire dal ciclo while
								iNrFails=0;
							}else{
							aFeeds= getFeeds(dbHelper);	
						
							//prima di tutto cancello in contenuto della tabella soulisDevices sul DB
							dbHelper.deleteSoulissDevices(dbHelper.getWritableDatabase());
	
					 String sURL= opzioni.getMyUrl();
					 //INVIA LA RICHIESTA A SOULISS E RACCOGLIE LA RISPOSTA 
					String s = SoulissDevicesHelper.getUrlResponse(sURL);
					// parse JSON data
					try {
						JSONArray jArray = new JSONArray(s);
						aSoulissDevices.clear();
		//scorre l'array JSON per leggerne il contenuto
						//scorre i nodi, variabile "i"
						for (int i = 0; i < jArray.length(); i++) {
							//legge chiave "id"
							JSONObject jObject = jArray.getJSONObject(i).getJSONObject("id");
							//legge chiave "slot"
							JSONArray jArraySlots = jObject.getJSONArray("slot");
							//scorre gli "slot" disponibili
							 for (int j = 0; j < jArraySlots.length(); j++) {
								 //i è il nodo
								 //j è lo slot
								 String sNomeNodo=SoulissDevicesHelper.getSensorItemName(jArray, i, j);
								 aSoulissDevices.add(new Device(i,j, sNomeNodo));
								 dbHelper.insertSoulissDevice(dbHelper.getWritableDatabase(), sNomeNodo, String.valueOf(i),String.valueOf(j), null, false);
							 }
							 if(aSoulissDevices != null && aSoulissDevices.size()>0 && aFeeds != null && aFeeds.size()>0) {
								 isOK=true;
							 }
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
		                		
					if (aFeeds != null && aFeeds.size()>0) {
						 Toast.makeText(ZozOtActivity.this, "Get Feeds OK", Toast.LENGTH_SHORT).show();
						 iStreams=aFeeds.size();
					}
					else {  
						Toast.makeText(ZozOtActivity.this, "Get Feeds ERROR", Toast.LENGTH_SHORT).show();
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
		               
					 //se la lettura degli stream e dei devices è andata a buon fine allora ripristino il contatore RETRY
					 if(aSoulissDevices != null && aSoulissDevices.size()>0 && aFeeds != null && aFeeds.size()>0) {
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
		super.onDestroy();
//		releaseService();
	}

	/**
	 * Binds this activity to the service.
	 */
//	public void initService() {
//		
//		connection = new HttpServiceConnection();
//		
//		Intent i = new Intent("com.xively.android.service.HttpService");
//		
//		boolean ret = bindService(i, connection, Context.BIND_AUTO_CREATE);
//		
//		Log.d(TAG, "initService() bound with " + ret);
//	}

//	/**
//	 * Unbinds this activity from the service.
//	 */
//	private void releaseService() {
//		unbindService(connection);
//		connection = null;
//		Log.d(TAG, "releaseService() unbound.");
//	}

	public static Context getAppContext() {
		return ZozOtActivity.context;
	}

	private void setOpzioni(PreferenceHelper opzioni) {
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
		

	// metodo che si occupa di gestire i messaggi provenienti dal thread
	//result: invia il risultato del push 
//	 public class MyHandler extends Handler {
//		 TextView resultField;   
//		 public MyHandler() {
//			 super();
//			 resultField = (TextView) findViewById(R.id.result);
//			 resultField.setMaxLines(50);
//			
//			// TODO Auto-generated constructor stub
//		}
//
//			@Override
//		    public void handleMessage(Message msg) {
//		      Bundle bundle = msg.getData();
//		      if(bundle.containsKey("result")) {
//		        String value = bundle.getString("result");
//		        
//		        //resultField.setText(value);
//		        resultField.append("\n"+value);
//		        
////		        final Layout layout = resultField.getLayout();
////		        if(layout != null){
////		            int scrollDelta = layout.getLineBottom(resultField.getLineCount() -1) - resultField.getScrollY() - resultField.getHeight();
////	            if(scrollDelta > 0)
////		            if (resultField.getGravity() != Gravity.BOTTOM){
////		            	resultField.setGravity(Gravity.BOTTOM);	
////		            }
////	            	resultField.scrollBy(0, scrollDelta);
////		        
////		        }
//	    }
//		    }
//		  }
	
	
	private void TimerStop(){
		stopService(new Intent(ZozOtActivity.this,ZozOtService.class));
		Toast.makeText(ZozOtActivity.this, "Push STOP", Toast.LENGTH_SHORT).show();
		opzioni.setMyServiceState(false);
	}
	

	Intent myIntents2;
	private Intent TimerStart(){
		//avvia il servizio
		//Parameters_Xively param =new Parameters_Xively(opzioni.getMyApiKey(), opzioni.getMyFeedId(), opzioni.getMyUrl(), opzioni.getMyInterval());
		
		
		myIntents2 = new Intent(getApplicationContext(), ZozOtService.class);
		String pkg=getPackageName();
		myIntents2.putParcelableArrayListExtra(pkg+"SoulissDevices", aSoulissDevices);
	//	myIntents2.putExtra(pkg+"Parameters_Xively", opzioni);

		//se il servizio è già avviato allora lo fermo, poi lo riavvio
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

		//avvia o ferma il counter
				
		//se il timer è già nello stato Avvio
//			if 	(opzioni.getMyServiceState()){
//			//allora lo fermo
//				if(cdt!=null) TimerStop(cdt);
//			}
//			//avvio il timer (solo se la temporizzazione è impostata ad un valore qualsiasi >0)
//			if (opzioni.getMyInterval()>0) {
//				cdt = new CountDownTimer(opzioni.getMyInterval(),1000){
//			        @Override
//			        public void onFinish() {
//			        	Log.d(TAG, "CountDownTimer - onFinish() ");        	
//			        //Cosa fare quando finisce
//			        	// *****************
//						// *****************
//						// RICHIESTA DATI ZOZZARIELLO E INVIO A XIVELY
//			        	MyHandler handler = new MyHandler();
//						SoulissDevicesHelper thr = new SoulissDevicesHelper(handler,opzioni, aSoulissDevices, service, Constants.PUSH_TO_XIVELY);
//						thr.start();
//						this.start();
//			        }
//			        
//
//			        @Override
//			        public void onTick(long millisUntilFinished) {
//			        //cosa fare ad ogni passaggio
//			        	TextView prossimoPushField = (TextView) findViewById(R.id.textViewProssimoPush);
//			        	long sec = millisUntilFinished/1000;  
//			        	prossimoPushField.setText(sec +" seconds remain");
//			        }
//			        }.start();
//				 Toast.makeText(ZozOtActivity.this, "Push START", Toast.LENGTH_SHORT).show();
//			}
//			return cdt;
////			return null;
	}
	
/**
	 * Binds this activity to the service.
	 */
	public void initService() {
			connection = new HttpServiceConnection();
		
		Intent i = new Intent("com.zozot.OEM.cloudservice.HttpService");
		
		boolean ret = bindService(i, connection, Context.BIND_AUTO_CREATE);
		
		Log.d(TAG, "initService() bound with " + ret);
	}

	//riceve i messaggi inviati in broadcast dal service
//in alto, nel metodo onCreate, il receiver è registrato ed è aplicato il filtro: registerReceiver(uiUpdated, new IntentFilter(Constants.PUSH_RESULT_UPDATE_INTENT_ACTION_NAME));
	
	private BroadcastReceiver uiUpdated= new BroadcastReceiver() {
		
		String sValue=null;
		long lValue;
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	
	    	
	    	
	    	sValue=intent.getExtras().getString(Constants.PUSH_RESULT_UPDATE_KEY);
	    	if (sValue!=null){
	    		    	
			resultField.append("\n"+sValue);
	    	}
	    	
	    	lValue=intent.getExtras().getLong(Constants.COUNTDOWN_KEY);
	    	//if (lValue>0){
			TextView countdownField = (TextView) findViewById(R.id.textViewProssimoPush);
			countdownField.setText(lValue +" seconds remains");
	    //	}
			
	    }
	};
	HttpServiceConnection connection;

	public class HttpServiceConnection implements ServiceConnection {
					
	public void onServiceConnected(ComponentName name, IBinder boundService) {
		cloudService = IHttpService.Stub.asInterface((IBinder) boundService);
		Log.d(ZozOtActivity.TAG, "onServiceConnected() connected");
		//Toast.makeText(ZozOtActivity.this, "Service connected",Toast.LENGTH_LONG).show();
	}
	
	public void onServiceDisconnected(ComponentName name) {
		cloudService = null;
		Log.d(ZozOtActivity.TAG, "onServiceDisconnected() disconnected");
		//Toast.makeText(ZozOtActivity.this, "Service connected",Toast.LENGTH_LONG).show();
	}
		}

	/**
		 * getFeeds()
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
				cloudService.setApiKey(opzioni.getMyApiKey());
				response= cloudService.getFeed();
				// PARSING RISPOSTA JSON GET FEED
			if (response.getStatusCode()>-1) {
				JSONArray jArraySlots = new JSONArray(response.getContent());
				//JSONObject jObject = new JSONObject(response.getContent());

//			JSONArray jArraySlots=jObject.getJSONArray("datastreams");
			
				//LISTA FEED DISPONIBILI
				String sNomeStream;
				for (int j = 0; j < jArraySlots.length(); j++) {
					sNomeStream=jArraySlots.getJSONObject(j).getString("name");
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
		        if ("com.zozot.OEM.service.ZozOtService".equals(service.service.getClassName())) {
		             return true;
		        }
		    }
		    return false;
		}
}
