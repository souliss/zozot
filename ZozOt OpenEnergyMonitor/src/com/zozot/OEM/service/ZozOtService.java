package com.zozot.OEM.service;

import java.util.ArrayList;

import com.zozot.OEM.consumer.R;
import com.zozot.OEM.cloudservice.Messages;
import com.zozot.OEM.consumer.Constants;
import com.zozot.OEM.consumer.Device;
import com.zozot.OEM.consumer.PreferenceHelper;
import com.zozot.OEM.consumer.SoulissDevicesHelper;
import com.zozot.OEM.consumer.ZozOtActivity;
import com.zozot.OEM.cloudservice.IHttpService;

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
	private static final String TAG = ZozOtService.class.getSimpleName();
	ArrayList<Device> aSoulissDevices;
	IHttpService service;
	HttpServiceConnection connection;
	private boolean isPlaying=false;
	PreferenceHelper opzioni ;
	
	public ZozOtService() {
		// TODO Auto-generated constructor stub
	//	 opzioni=new PreferenceHelper(getApplicationContext());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		goServiceForeground();  
		
		String pkg=getPackageName();
		Bundle extras = intent.getExtras();
		
		aSoulissDevices=extras.getParcelableArrayList(pkg+"SoulissDevices"); //$NON-NLS-1$
	//	final Parameters_Xively param=(Parameters_Xively) extras.getParcelable(pkg+"Parameters_Xively");
		
		// TODO Auto-generated method stub
		opzioni = ZozOtActivity.getOpzioni();
		//avvio il timer (solo se la temporizzazione è impostata ad un valore qualsiasi >0)
		if (opzioni.getMyInterval()>0) {
			cdt = new CountDownTimer(opzioni.getMyInterval(),1000){
		        @Override
		        public void onFinish() {
		        	Log.d(TAG, "CountDownTimer - onFinish() ");        	 //$NON-NLS-1$
		        //Cosa fare quando finisce
		        	// *****************
					// *****************
					// RICHIESTA DATI ZOZZARIELLO E INVIO A XIVELY
		        			        	
		        	MyHandler handler = new MyHandler();
					SoulissDevicesHelper thr = new SoulissDevicesHelper(handler,opzioni, aSoulissDevices, service, Constants.PUSH_TO_CLOUD);
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
		        
		        String s=Messages.getString("ZozOtService.PushStart_text");
			 Toast.makeText(getApplicationContext(),s , Toast.LENGTH_SHORT).show(); //$NON-NLS-1$
		
	}
		//return super.onStartCommand(intent, flags, startId);
		  return Service.START_NOT_STICKY;

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
		Toast.makeText(this, Messages.getString("ZozOtService.ServiceInterrotto_text"), Toast.LENGTH_SHORT).show(); //$NON-NLS-1$
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

	// Creiamo il pending intent che verrà lanciato quando la notifica
	// viene premuta
	Intent notificationIntent = new Intent(this, ZozOtActivity.class);
	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

	notificationBuilder.setContentIntent(contentIntent);

	// Impostiamo il suono, le luci e la vibrazione di default
	notificationBuilder.setDefaults(Notification.FLAG_FOREGROUND_SERVICE);
	      
	//final int SIMPLE_NOTIFICATION_ID = 1;
	//mNotificationManager.notify(SIMPLE_NOTIFICATION_ID, notificationBuilder.build());
	
	//******************** QUESTA FUNZIONA
//	      Notification note=new Notification(R.drawable.xively_launcher, "ZozOt Service",System.currentTimeMillis()); //$NON-NLS-1$
//	    	    
//	      Intent i=new Intent(getApplicationContext(), ZozOtService.class);
//	    
//	      i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
//	                 Intent.FLAG_ACTIVITY_SINGLE_TOP);
//	    
//	      PendingIntent pi=PendingIntent.getActivity(getApplicationContext(), 0,
//	                                                  i, 0);
//	      
//	      int stringId = getApplicationContext().getApplicationInfo().labelRes;
//	      
//
//	      note.setLatestEventInfo(getApplicationContext(), getApplicationContext().getString(stringId), getApplicationContext().getResources().getString(R.string.executing_Service) ,pi);
//
//	      note.flags|=Notification.FLAG_FOREGROUND_SERVICE;
	  	//FINE******************** QUESTA FUNZIONA
	      
	      
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
		cdt.cancel();
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
	        
	        
//	        TextView resultField = (TextView) findViewById(R.id.result);
//	        //resultField.setText(value);
//	        resultField.append("\n"+value);
//	        resultField.setMaxLines(50);
//	        final Layout layout = resultField.getLayout();
//	        if(layout != null){
//	            int scrollDelta = layout.getLineBottom(resultField.getLineCount() - 1) - resultField.getScrollY() - resultField.getHeight();
//            if(scrollDelta > 0)
//	            if (resultField.getGravity() != Gravity.BOTTOM){
//	            	resultField.setGravity(Gravity.BOTTOM);	
//	            }
//            	
//	            	//resultField.scrollBy(0, -scrollDelta);
//	        }
//	    }
	    }
	  }
	    
	    
	}

		public class HttpServiceConnection implements ServiceConnection {
					
	public void onServiceConnected(ComponentName name, IBinder boundService) {
		service = IHttpService.Stub.asInterface((IBinder) boundService);
		Log.d(ZozOtActivity.TAG, "onServiceConnected() connected"); //$NON-NLS-1$
		//Toast.makeText(ZozOtActivity.this, "Service connected",Toast.LENGTH_LONG).show();
	}
	
	public void onServiceDisconnected(ComponentName name) {
		service = null;
		Log.d(ZozOtActivity.TAG, "onServiceDisconnected() disconnected"); //$NON-NLS-1$
		//Toast.makeText(ZozOtActivity.this, "Service connected",Toast.LENGTH_LONG).show();
	}
		}
	
	
}
