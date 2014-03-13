package com.xively.android.consumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xively.android.JSONBuilderHelper.JSONBodyBuilder;
import com.xively.android.JSONBuilderHelper.JSONResponseHelper;
import com.xively.android.cloudservice.Response;
import com.xively.android.cloudservice.IHttpService;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.app.ListActivity;
import android.os.Bundle;



public class SoulissDevicesHelper extends Thread {
	//array che contiene la lista dei datapoints ancora da inserire
	//SortedSet<DataPoint> sortedSetDataPoints= new TreeSet<DataPoint>(); 
	//SortedSet<DataPoint> sortedSetDataPoints;

	private static final String TAG =  Constants.TAG_LivelloAvvisiApplicazione;
	private Handler myHandlerWorkerThread;
	double dVal = 0;
	IHttpService xivelyService;
	//funzione, come da classe Constants
	int function=0;
	private boolean bPushOnlyPowerValues;
	JSONBodyBuilder jsonBuilder;
	static Integer iNumeroElementiPush=Constants.PUSH_MAX_PACKET;
	ArrayList<Device> aSoulissDevices;
	Calendar date = Calendar.getInstance();
	SimpleDateFormat  formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); 
	//Parameters_Xively param;
	PreferenceHelper opzioni ;
	
	public SoulissDevicesHelper(Handler handler, PreferenceHelper opzioni, ArrayList<Device> aSoulissDevices, IHttpService service2, int funct, boolean bPushOnlyPowerValuesPar, JSONBodyBuilder jsonBuilder) {
		// TODO Auto-generated constructor stub
		this.myHandlerWorkerThread = handler;
		this.opzioni=opzioni;
		//this.service=service;
		function=funct;
		this.bPushOnlyPowerValues=bPushOnlyPowerValuesPar;
		
		xivelyService=service2;
		this.aSoulissDevices=aSoulissDevices;
		this.jsonBuilder=jsonBuilder;
	}

	

	public void run() {
		
		//Looper.prepare(); 
				
		switch (function) {
		case Constants.RETRIEVING:
			//METODO RECUPERO VALORE SENSORE CON NODO E DISPOSITIVO
			int nodo=0;
			double dValSens=0;
			int iDispositivo=0;
			int iHealt=0;
			boolean bFlag=false;
			
			// parse JSON data
			JSONArray jArray = null;

			try {
							String sURL= opzioni.getMyUrl();
							 //INVIA LA RICHIESTA A SOULISS E RACCOGLIE LA RISPOSTA 
							String s = getUrlResponse(sURL);
							jArray = new JSONArray(s);
			}catch  (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("Exception", "Error: " + e.toString());
			}
			
			
			if(jArray!=null) {
			
			for (int i=0; i < aSoulissDevices.size();i++){
				//esegue il push dei valori potenza in modo separato dagli altri valori
				
				//il push è eseguito solo se il bFlag è True, e bFlag è calcolato in base a bPushOnlyPowerValues. Se si tratta di un tipico per la misurazione dei consumi allora la temporizzazione è differente rispetto agli altri tipici
				if(bPushOnlyPowerValues){
					Log.v(TAG,this.getName() + " RETRIEVING - OnlyPowerValues");
					//esegue il push se il device è abilitato e se è un tipico per la misurazione dei consumi
					bFlag=aSoulissDevices.get(i).bEnabled && isAPowerTypical(aSoulissDevices.get(i).getTypical());
					}else{
						Log.v(TAG, this.getName() + " RETRIEVING - All Typicals but not OnlyPowerValues");
						//esegue il push se il device è abilitato  e se NON è un tipico per la misurazione dei consumi
						bFlag=aSoulissDevices.get(i).bEnabled && !isAPowerTypical(aSoulissDevices.get(i).getTypical());;
					}
				
				if(bFlag){
					iDispositivo=aSoulissDevices.get(i).getIdDispositivo();
					nodo=aSoulissDevices.get(i).getIdNodo();
					try {
						iHealt=getHealtValue(jArray, nodo);
						dValSens= getSensorValue(jArray,nodo, iDispositivo);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e("Exception", "JSON Parse Error (getHealtValue and getSensorValue: " + e.toString());
					}
					
					// *****************
					// *****************
					// INVIO DATI XIVELY DI TUTTI I DISPOSITIVI SCELTI
					//***********
					String sVal;
					//se la Salute del dispositivo è bassa allora il valore è inaffidabile e viene sostituito con null
					if (iHealt>Constants.iSetupHealtLevelToSetNullValue){
						sVal=String.valueOf(dValSens);
					}else {
						sVal="null";
					}
					
				jsonBuilder.add(new DataPoint(aSoulissDevices.get(i).getStreamName(), JSONBodyBuilder.now(),sVal));
			}
			}
			}
			break;

		case Constants.PUSH:
			Log.d(TAG, this.getName() + " PUSH ");
			int iRetry=0;
			boolean bResponse=false;
			//il numero di elementi da caricare è impostato sulla dimensione della lista

			//eseguoil push solo se la lista non è vuota
			if (!jsonBuilder.isEmpty()) {
				//se il numero di elementi è maggiore di 1000 allora imposto il numero di elementi da caricare al massimo consentito
				if(jsonBuilder.size()>Constants.PUSH_MAX_PACKET) {
					iNumeroElementiPush=Constants.PUSH_MAX_PACKET;
				} else iNumeroElementiPush=jsonBuilder.size();
				
			do {
				//se il numero di elementi da caricare eccede il massimo (1000) allora faccio il push a pacchetti
				if(iNumeroElementiPush>Constants.PUSH_MAX_PACKET) dividePush();
				
				try {
					//reimposto bResponse a false. Lo faccio perchè in caso di primo ciclo positivo e secondo con eccezione devo averlo false e non true
					Log.d(TAG, this.getName() + " Tentativo " + iRetry + " - PUSH (going to putXively) ");
					bResponse = putXively(jsonBuilder,iNumeroElementiPush);
					Log.d(TAG, this.getName() + " PUSH RESULT: " + bResponse);
				} catch (TimeoutException e) {
					//AZIONI DA SVOLGERE SOLO IN CASO DI TIMEOUT
					e.printStackTrace();
					Log.e("TimeoutException", "PUSH Error: " + e.toString());
					
					dividePush();
					bResponse=false;
					//===========================================
				}catch (RemoteException e) {
					e.printStackTrace();
					Log.e("RemoteException", "PUSH Error: " + e.toString());
					bResponse=false;
				} catch (InterruptedException e) {
						e.printStackTrace();
						Log.e("InterruptedException", "PUSH Error: " + e.toString());
						bResponse=false;
				}

				
				
				if(bResponse){
					Log.d(TAG, this.getClass().getName() + " PUSH Esito: " + bResponse + ". Inseriti " + iNumeroElementiPush + " elementi");
			//		notifyMessage(formatter.format(date.getTime()) + " PUSH Esito: " + bResponse + ". Inseriti " + iNumeroElementiPush + " elementi");
					//se esistono ancora elementi da inserire allora pongo bResponse =false in modo da non uscire dal ciclo
					if(jsonBuilder.size()>Constants.PUSH_MIN_PACKET) {
						bResponse=false;
					}else {
						if(jsonBuilder.size()==0) {
							iNumeroElementiPush=Constants.PUSH_MAX_PACKET; //reset numero elementi da caricare. No pacchettizza
						}
					}
							
				} else iRetry++; //se c'è stato errore allora incremento il numero di tentativi eseguiti
				
			} while (!bResponse && iRetry<=Constants.PUSH_RETRY_NUMBERS && jsonBuilder.size()>0);
//			notifyMessage(formatter.format(date.getTime()) + " Esce da ciclo while con jsonBuilder.size() = " + jsonBuilder.size() + ", iRetry="+ iRetry + ", Esito="+bResponse);
			}//END if (!jsonBuilder.isEmpty())	
			
			if(!bResponse){
				Log.d(TAG, this.getClass().getName() + " ERRORE - Push " + jsonBuilder.size() + " datapoints rinviato");
				notifyMessage(formatter.format(date.getTime()) + " ERRORE - Eseguiti " + iRetry + " tentativo/i. Push " + jsonBuilder.size() + " datapoints rinviato");
			}
			break;
			}
		}



	private void dividePush() {
		//divido il numero di elementi da inserire
		int iDivisionePer=2;
	
		if((iNumeroElementiPush/iDivisionePer)<1){
			iNumeroElementiPush=1;
		}else {
			iNumeroElementiPush/=iDivisionePer;
		}
				Log.e("Divisione", "Divisione della lista per " + iDivisionePer + ". Verrà eseguito il PUSH a gruppi di " + iNumeroElementiPush + " elementi per volta.");
				notifyMessage(formatter.format(date.getTime()) + " ERRORE - TimeOut - Il PUSH verrà eseguito a pacchetti di " + iNumeroElementiPush + " elementi per volta.");
	}
	 
	public static String getUrlResponse(String url) {
			try {
				HttpGet get = new HttpGet(url);
				HttpClient client = new DefaultHttpClient();
				HttpResponse response = client.execute(get);
				HttpEntity entity = response.getEntity();
				return convertStreamToString(entity.getContent());
			} catch (Exception e) {
				Log.e(TAG, "Connessione fallita", e);
			}
			return null;
		}
	  
	  private static String convertStreamToString(InputStream is) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is),8 * 1024);
			StringBuilder sb = new StringBuilder();

			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
			} catch (IOException e) {
			} finally {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			return sb.toString();
		}
	  
		private void notifyMessage(String str) {
	    Message msg = myHandlerWorkerThread.obtainMessage();
	    Bundle b = new Bundle();
	    b.putString("result", str);
	    msg.setData(b);
	    myHandlerWorkerThread.sendMessage(msg);
	  }

	
		private boolean putXively(JSONBodyBuilder jsonBuilder, Integer iNumeroElementiDaCaricare) throws TimeoutException, RemoteException, InterruptedException {
			
			if(!jsonBuilder.getDataPoints().isEmpty()){
			
		//	JSONBodyBuilder Jbuilder= new JSONBodyBuilder(xsortedSetDataPoints);
		
			//String sBody=jsonBuilder.getBody(iNumeroElementiDaCaricare);
			JSONResponseHelper r=jsonBuilder.getBody(iNumeroElementiDaCaricare);
			Response response =null;
			if(!(r.getBody()==null) && !r.getBody().isEmpty()){
				xivelyService.setApiKey(opzioni.getMyApiKey());
				response = xivelyService.createDatastream(opzioni.getMyFeedId(), r.getBody());
			}
				if (response != null)
				{
					if(response.getStatusCode()==200){
						//se il push è andato a buon fine allora elimino gli elementi presenti nella lista temporanea
						//INVIO NOTIFICA ALL'ACTIVITY PRINCIPALE
						notifyMessage(formatter.format(date.getTime()) + " inseriti " + r.getElementiCaricati() + " datapoints");
						jsonBuilder.clear(iNumeroElementiDaCaricare);
						Log.d(TAG, this.getClass().getName() + " Push OK, cancellazione elementi dalla lista: " + r.getElementiCaricati() );
						//se l'array è vuoto allora la volta successiva posso caricare tutti gli elementi senza i limiti dovuti al timeout. Quindi riporto iNumeroElementiDaCaricare a Null
						if(jsonBuilder.size()==0) {
							Log.d(TAG, this.getClass().getName() + " Lista vuota. Caricati tutti gli elementi)");
						}
						return true;
					} else {
						//notifyMessage(formatter.format(date.getTime()) + " ERRORE:  " + response.getMessage() + " - Push " + jsonBuilder.size() + " datapoints");
						Log.d(TAG, this.getClass().getName() + " ERRORE: StatusCode="+response.getStatusCode() +" Desc: " + response.getContent() +" - Push " + r.getElementiCaricati()  + " datapoints");
						//altrimenti inserisco nuovamente gli elementi nella lista e cancello la lista temporanea
				//		notifyMessage(formatter.format(date.getTime()) + " ERRORE: StatusCode="+response.getStatusCode() +" Desc: " + response.getContent() +" - Push " + iNumeroElementiDaCaricare + " datapoints");
						//if (response.getContent().contains("TimeoutException") || response.getContent().contains("RequestUnsuccessfulException")) {
							if (response.getContent().contains("TimeoutException")) {
							//sollevo eccezione TIMEOUTEXCEPTION
							throw new TimeoutException();
						}
						
					}
				} //if (response != null)
				return false;
			}
	
		return true; //se arrivo qui vuol dire che l'array è vuoto. Ritorno true per uscire subito dal ciclo tentativi invio.
		}

		
		public static double getSensorValue(JSONArray jArray, int iNodo, int iDispositivo) throws JSONException {
			//seleziono il nodo 
			JSONObject jObject = jArray.getJSONObject(iNodo).getJSONObject("id");
			JSONArray jArraySlots = jObject.getJSONArray("slot");
			//a questo punto basta selezionare lo slot di interesse
			return jArraySlots.getJSONObject(iDispositivo).getDouble("val");
			
		}

		public static String getSensorItemName(JSONArray jArray, int iNodo, int iDispositivo) throws JSONException {
			//seleziono il nodo 
			JSONObject jObject = jArray.getJSONObject(iNodo).getJSONObject("id");
			JSONArray jArraySlots = jObject.getJSONArray("slot");
			//a questo punto basta selezionare lo slot di interesse
			return jArraySlots.getJSONObject(iDispositivo).getString("ddesc");
			
		}
		
		public static int getHealtValue(JSONArray jArray, int iNodo) throws JSONException {
			//seleziono il nodo 
			JSONObject jObject = jArray.getJSONObject(iNodo).getJSONObject("id");
			return jObject.getInt("hlt");
			
		}
		
		public static int getTypical(JSONArray jArray, int iNodo, int iDispositivo) throws JSONException {
			//seleziono il nodo 
			JSONObject jObject = jArray.getJSONObject(iNodo).getJSONObject("id");
			JSONArray jArraySlots = jObject.getJSONArray("slot");
			//a questo punto basta selezionare lo slot di interesse
			return Integer.parseInt(jArraySlots.getJSONObject(iDispositivo).getString("typ"));
			
		}

		private boolean isAPowerTypical(int iTypical) {
			ArrayList<Integer> arrayPowerTypicals =new ArrayList<Integer>();
			
//			ArrayList<Integer> a=opzioni.getPowerTypicalsArray();
			
			int[] powerTypicalsArray=opzioni.getPowerTypicalsArray();

			boolean bContainsTypical=false;
for(int i=0;i<powerTypicalsArray.length;i++){
	if (powerTypicalsArray[i]==iTypical){
		bContainsTypical=true;
	}
}
			if (bContainsTypical){
				return true;
			}
			return false;
		}
		
}
