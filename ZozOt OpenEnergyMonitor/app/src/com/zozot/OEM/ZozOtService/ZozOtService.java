package com.zozot.OEM.ZozOtService;

import java.util.ArrayList;

import com.zozot.OEM.cloudservice.Messages;
import com.zozot.OEM.consumer.MyApplication;
import com.zozot.OEM.consumer.R;
import com.zozot.OEM.consumer.Constants;
import com.zozot.OEM.consumer.Device;
import com.zozot.OEM.consumer.PreferenceHelper;
import com.zozot.OEM.consumer.SoulissDevicesHelper;
import com.zozot.OEM.consumer.ZozOtActivity;
import com.zozot.OEM.cloudservice.IHttpService;
import com.zozot.OEM.JSONBuilderHelper.JSONBodyBuilder;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class ZozOtService extends Service {
	CountDownTimer cdt=null;
	CountDownTimer cdtPower=null;
	private static final String TAG = ZozOtService.class.getSimpleName();
	ArrayList<Device> aSoulissDevices;
	IHttpService service;
	HttpServiceConnection connection;
	private boolean isPlaying=false;
	PreferenceHelper opzioni ;
	JSONBodyBuilder jsonBuilder= new JSONBodyBuilder();
	//array che contiene la lista dei datapoints ancora da inserire
	//SortedSet<DataPoint> sortedSetDataPoints= new TreeSet<DataPoint>();		
	boolean bFlagSemaforo;
	
	public ZozOtService() {
		// TODO Auto-generated constructor stub
	//	 opzioni=new PreferenceHelper(getApplicationContext());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		goServiceForeground();  
		
		String pkg=getPackageName();
		Bundle extras = intent.getExtras();
		
		//recupera i deviced souliss ed i valori passati al service
		aSoulissDevices=extras.getParcelableArrayList(pkg+"SoulissDevices"); //$NON-NLS-1$
	//	final Parameters_Xively param=(Parameters_Xively) extras.getParcelable(pkg+"Parameters_Xively");
		
		opzioni = ZozOtActivity.getOpzioni();
		//avvio il timer (solo se la temporizzazione � impostata ad un valore qualsiasi >0)
		if (opzioni.getMyInterval()>0) {
			cdt = new CountDownTimer(opzioni.getMyInterval(),1000){
		        @Override
		        public void onFinish() {
		        	Log.v(TAG, "CountDownTimer (cdt) - onFinish() ");        	 //$NON-NLS-1$
//l'ultimo parametro � false perch� non si deve fare il retrieving dei tipici per il controllo dei consumi		        			        	
		        	MyHandler handler = new MyHandler();
		        	// QUI VIENE FATTO IL RETRIEVING DEI DATI NON CONSUMO
		        	SoulissDevicesHelper thr = new SoulissDevicesHelper(handler,opzioni, aSoulissDevices, service, Constants.RETRIEVING, false, jsonBuilder);
		        	Log.v(TAG, "CountDownTimer (cdt) - onFinish() - Start Thread per il retrieving");

					try {
						thr.start();
						thr.join(); //interrompe questo thread fino a quando quello chiamato non termina
						thr=null;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// QUI VIENE FATTO IL PUSH DI TUTTI I DATI PRESENTI IN CODA 
	    			thr = new SoulissDevicesHelper(handler,opzioni, aSoulissDevices, service, Constants.PUSH, false,jsonBuilder);
	    			Log.v(TAG, "CountDownTimer (cdt) - onFinish() - Start Thread per il PUSH");
	    			
						thr.start();
						this.start();
		        }
		        

		        @Override
		        public void onTick(long millisUntilFinished) {
			        //cosa fare ad ogni passaggio
		        	//recuperato il valore dal thread, spedisco in broadcast in modo che l'Activity principale la possa recuperare
			        Intent i = new Intent(Constants.PUSH_RESULT_UPDATE_INTENT_ACTION_NAME);
			        //trasformazione in secondi
			        long sec = millisUntilFinished/1000;  
			        i.putExtra(Constants.COUNTDOWN_KEY, sec);
			        sendBroadcast(i);

		        }
		        }.start();
		        //String s=Messages.getString("ZozOtService.PushStart_text");
				// Toast.makeText(getApplicationContext(),s , Toast.LENGTH_SHORT).show(); //$NON-NLS-1$
		}
		
		if (opzioni.getMyIntervalForPowerTypical()>0) {
		    cdtPower= new CountDownTimer(opzioni.getMyIntervalForPowerTypical(),1000){
		    		@Override
		    		public void onFinish() {
		    			Log.v(TAG, "CountDownTimer (cdtPower) - onFinish() ");        	 //$NON-NLS-1$
		    					        	
		    			MyHandler handler = new MyHandler();
		    			//l'ultimo paramentro � false perch� deve fare il retrieving dei tipici per il controllo dei consumi
		    			//QUI VIENE FATTO SOLO IL RETRIEVING DEI DATI CONSUMO
		    			SoulissDevicesHelper thr = new SoulissDevicesHelper(handler,opzioni, aSoulissDevices, service, Constants.RETRIEVING, true, jsonBuilder);
		    			Log.v(TAG, "CountDownTimer (cdtPower) - onFinish() - Start Thread per il retrieving");
		    			thr.start();
		    			thr=null;
		    			this.start();
		    		}
		    		
		    		
		    		@Override
		    		public void onTick(long millisUntilFinished) {
		    		    //cosa fare ad ogni passaggio
		    			//recuperato il valore dal thread, spedisco in broadcast in modo che l'Activity principale la possa recuperare
		    		    Intent i = new Intent(Constants.PUSH_POWER_TYPICALS_RESULT_UPDATE_INTENT_ACTION_NAME);
		    		    //trasformazione in secondi
		    		    long sec = millisUntilFinished/1000;  
		    		    i.putExtra(Constants.COUNTDOWN_KEY, sec);
		    		    sendBroadcast(i);
		    		
		    		}
		    		}.start();
		    		//String s=Messages.getString("ZozOtService.PushStart_text");
		    		//Log.d(TAG, s);
					//Toast.makeText(getApplicationContext(),s , Toast.LENGTH_SHORT).show(); //$NON-NLS-1$
		     
		
	}
		//return super.onStartCommand(intent, flags, startId);
		  //return Service.START_NOT_STICKY;
		  return Service.START_REDELIVER_INTENT;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		 initService();
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	  public void onDestroy() {
		releaseService();
		String s=Messages.getString("ZozOtService.ServiceInterrotto_text");
		Log.d(TAG, s);
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); //$NON-NLS-1$
	  }

	/**
	 * Binds this activity to the service.
	 */
	public void initService() {
		connection = new HttpServiceConnection();
		Intent i = new Intent("com.zozot.OEM.cloudservice.HttpService"); //$NON-NLS-1$
		boolean ret = bindService(i, connection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "initService() bound with " + ret); //$NON-NLS-1$
	}

	private void goServiceForeground() {
	    if (!isPlaying) {
	      Log.w(TAG, "Got to play()!"); //$NON-NLS-1$
	      isPlaying=true;
	
	   //   NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	      NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());

	// Titolo e testo della notifica
	notificationBuilder.setContentTitle(getApplicationContext().getResources().getString(R.string.app_name));
	notificationBuilder.setContentText(getApplicationContext().getResources().getString(R.string.notifica_desc));

	// Testo che compare nella barra di stato non appena compare la notifica
	notificationBuilder.setTicker(getApplicationContext().getResources().getString(R.string.executing_Service));

	// Data e ora della notifica
	notificationBuilder.setWhen(System.currentTimeMillis());

	// Icona della notifica
	notificationBuilder.setSmallIcon(R.drawable.oem_launcher);

	// Creiamo il pending intent che verr� lanciato quando la notifica
	// viene premuta
	Intent notificationIntent = new Intent(this, ZozOtActivity.class);
	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

	notificationBuilder.setContentIntent(contentIntent);
	notificationBuilder.setDefaults(Notification.FLAG_FOREGROUND_SERVICE);
      
    startForeground(1337, notificationBuilder.build());
	    }
	  }

	private void stop() {
	    if (isPlaying) {
	      Log.w(TAG, "Got to stop()!"); //$NON-NLS-1$
	      isPlaying=false;
	      stopForeground(true);
	      releaseService();
	    }
	  }

	/**
	 * Unbinds this activity from the service.
	 */
	private void releaseService() {
		unbindService(connection);
		connection = null;
		if (cdt!=null) cdt.cancel();
		if (cdtPower!=null)	cdtPower.cancel();
		Log.d(TAG, "releaseService() unbound."); //$NON-NLS-1$
	}

	public class MyHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg) {
	      Bundle bundle = msg.getData();
	      if(bundle.containsKey("result")) { //$NON-NLS-1$
	        String value = bundle.getString("result"); //$NON-NLS-1$

	        //recuperato il valore dal thread, spedisco in broadcast in modo che l'Activity principale la possa recuperare
	        Intent i = new Intent(Constants.PUSH_RESULT_UPDATE_INTENT_ACTION_NAME);
	        i.putExtra(Constants.PUSH_RESULT_UPDATE_KEY, value);
	        sendBroadcast(i);
	      
	        //salvo il valore per l'eventuale ripristino in caso di rotazione dello schermo e riavvio dell'activity principale
			MyApplication.getInstance().appendTextBoxLog("\n" + value);
	        
	    }
	  }
	    
	    
	}

public class HttpServiceConnection implements ServiceConnection {
	private static final String TAG =  Constants.TAG_LivelloAvvisiServizio;				
	public void onServiceConnected(ComponentName name, IBinder boundService) {
		service = IHttpService.Stub.asInterface((IBinder) boundService);
		Log.d(TAG, "onServiceConnected() connected"); //$NON-NLS-1$
		//Toast.makeText(ZozOtActivity.this, "Service connected",Toast.LENGTH_LONG).show();
	}
	
	public void onServiceDisconnected(ComponentName name) {
		service = null;
		Log.d(TAG, "onServiceDisconnected() disconnected"); //$NON-NLS-1$
		//Toast.makeText(ZozOtActivity.this, "Service connected",Toast.LENGTH_LONG).show();
	}
		}

		
	
	
}
