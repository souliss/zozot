package com.zozot.OEM.consumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.zozot.OEM.cloudservice.IHttpService;
import com.zozot.OEM.cloudservice.Response;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;




public class SoulissDevicesHelper extends Thread {

	private static final String TAG =  ZozOtActivity.class.getSimpleName();;
	private Handler myHandlerWorkerThread;
	double dVal = 0;
	IHttpService OEMService;
	//funzione, come da classe Constants
	int function=0;
	ArrayList<Device> aSoulissDevices;
	Calendar date = Calendar.getInstance();
	SimpleDateFormat  formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); 
	//Parameters_Xively param;
	PreferenceHelper opzioni ;
			
	public SoulissDevicesHelper(Handler handler, PreferenceHelper opzioni, ArrayList<Device> aSoulissDevices, IHttpService service2, int funct) {
		// TODO Auto-generated constructor stub
		this.myHandlerWorkerThread = handler;
		this.opzioni=opzioni;
		//this.service=service;
		function=funct;
		OEMService=service2;
		this.aSoulissDevices=aSoulissDevices;
		
	}

	public void run() {
			
		//Looper.prepare(); 
			try {
				Response response = null;
				 String sURL= opzioni.getMyUrl();
				 //INVIA LA RICHIESTA A SOULISS E RACCOGLIE LA RISPOSTA 
				String s = getUrlResponse(sURL);

				// parse JSON data
				try {
					JSONArray jArray = new JSONArray(s);

		switch (function) {
		case Constants.PUSH_TO_CLOUD:
			//METODO RECUPERO VALORE SENSORE CON NODO E DISPOSITIVO
			int nodo=0;
			int dispositivo=0;
			double dValSens=0;
			int iDispositivo=0;
			int iHealt=0;
			
			for (int i=0; i < aSoulissDevices.size();i++){
				if(aSoulissDevices.get(i).bEnabled){
					iDispositivo=aSoulissDevices.get(i).getIdDispositivo();
					nodo=aSoulissDevices.get(i).getIdNodo();
					iHealt=getHealtValue(jArray, nodo);
					dValSens= getSensorValue(jArray,nodo, iDispositivo);
					// *****************
					// *****************
					// INVIO DATI XIVELY DI TUTTI I DISPOSITIVI SCELTI

					//***********
					int iRetry=0
							;
					do {
						//se healt è = zero oppure un valore basso allora imposto il valore a null
						
						response = putToCloud(dValSens,aSoulissDevices.get(i).getStreamName(),iHealt);
							
						iRetry++;
						
					} while (response.getStatusCode()!=200 && iRetry<=Constants.PUSH_RETRY_NUMBERS);  
					//STRATEGIA DEL PUSH
					//AGGIUNGERE I valori da pushare in un array
					//fare un solo push complessivo
					//in caso di errore mantenere l'array e riproporre il contenuto per il prossimo invio
					
					//dentro la funzione di push bisogna ordinare l'array per Stream e costruire la stringa per i datapoint
					
					if (response != null)
					{
						String sValSens=Double.toString(dValSens);
						if(iHealt<Constants.iSetupHealtLevelToSetNullValue){
							sValSens="null";
						}
						//INVIO NOTIFICA ALL'ACTIVITY PRINCIPALE
						notifyMessage(formatter.format(date.getTime()) + " "+  aSoulissDevices.get(i).getNomeDispositivo() + ": " + sValSens + " - " + response.getMessage()+ ", retry " + iRetry + " times");
					}
				}
			}
			break;
		case Constants.SOULISS_DEVICES:
			//METODO RECUPERO VALORE SENSORE CON NODO E DISPOSITIVO
		//NON USATO QUI A CAUSA DI ERRORE NON RISOLTO. CODICE PORTATO NELLA CLASSE CHIAMANTE
			break;
		}
	
				} catch (JSONException e) {

					Log.e("JSONException", "Error: " + e.toString());

				} // catch (JSONException e)

			} catch (Exception e) {
				e.printStackTrace();
			}

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
			BufferedReader reader = new BufferedReader(new InputStreamReader(is),
					8 * 1024);
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

		private void notifyMessage(ArrayList<String> aList) {
			   Message msg = myHandlerWorkerThread.obtainMessage();
			    Bundle b = new Bundle();
			    b.putStringArrayList("result", aList);
			    msg.setData(b);
			    myHandlerWorkerThread.sendMessage(msg);
			
		}
		
		public Response putToCloud(double dVal, String sDatastreamName, int iHealt) throws RemoteException {
			OEMService.setApiKey(opzioni.getMyApiKey());
			String myNewDatapoint ;
			//http://emoncms.org/input/post.json?json={power:200}
				
				
			if (iHealt>Constants.iSetupHealtLevelToSetNullValue){
				myNewDatapoint =  String.valueOf(dVal);
			}else {
				myNewDatapoint = "null";
			}
		//	DATAPOINT DA CREARE
			return OEMService.createDatapoint(sDatastreamName, myNewDatapoint);
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
		
}
