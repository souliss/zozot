package com.zozot.OEM.JSONBuilderHelper;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;

import com.zozot.OEM.cloudservice.UriBuilder;
import com.zozot.OEM.consumer.Constants;
import com.zozot.OEM.consumer.DataPoint;

public class JSONBodyBuilder {

	private String sBody = "";
	//SortedSet<DataPoint> sortedSetDataPoints;

	// gli elementi vengono aggiunti sempre nell'arraylist, poi vengono spostati
	// nella lista ordinata prima della creazione della stringa JSON
	ArrayList<DataPoint> arrayListDataPoints;
	ArrayList<DataPoint> arrayListDataPointsTMP;

	
	
	private static final String TAG = Constants.TAG_LivelloAvvisiServizio;

	public JSONBodyBuilder() {
	//	sortedSetDataPoints = new TreeSet<DataPoint>();
		arrayListDataPoints = new ArrayList<DataPoint>();
		arrayListDataPointsTMP= new ArrayList<DataPoint>();
		Log.d(TAG, this.getClass().getName()
				+ "  JSONBodyBuilder() - Creazione oggetto creatore JSON");
	}

	public synchronized JSONResponseHelper getBody(Integer iNumeroElementiDaCaricare)
			throws InterruptedException {
		// TODO CREAZIONE BODY
		sBody="";
				// se il numero di elementi da caricare è maggiore della
				// dimensione dell'array allora pongo l'indice massimo alla dimensione dell'array
				if (iNumeroElementiDaCaricare > arrayListDataPoints.size())
				{
					iNumeroElementiDaCaricare = arrayListDataPoints.size();	
				}
				

				int iTopList = ((iNumeroElementiDaCaricare - 1) < 1) ? 1
						: iNumeroElementiDaCaricare - 1; // faccio in modo che
															// l'indice
															// superiore non sia
				
				arrayListDataPointsTMP.addAll(arrayListDataPoints.subList(0,
						iTopList));
				

//				Log.d(TAG, this.getClass().getName()
//						+ " getBody() - Creazione stringa JSON");
				int iNumeroElemento = 0;
				if (!arrayListDataPointsTMP.isEmpty()) {

					Iterator<DataPoint> it = arrayListDataPointsTMP.iterator();

					DataPoint elemento = null;
					String sStream_elementoTMP = null;
					
					Log.d(TAG, this.getClass().getName() + " Coda: "
							+ arrayListDataPointsTMP.size());
					String tmpData="";
					boolean bFlagPrimaVolta=true;
					String sPrecTime = null;
					boolean bFlagExit =false;
					while (! bFlagExit && it.hasNext()	&& iNumeroElemento <= iNumeroElementiDaCaricare) {
						
						elemento = (DataPoint) it.next();
						if (bFlagPrimaVolta) {	
							//solo la prima volta inserisco time
							sBody+="time="+ elemento.getData()+"&";
							sPrecTime=elemento.getData();
							sBody+="json={";
							bFlagPrimaVolta=false;
							sBody+= UriBuilder.encodeDatastreamAndValue(elemento.getStream(),elemento.getValore());
							iNumeroElemento++; // incremento il numero di elementi
							// inseriti
						}else if(sPrecTime!=null && sPrecTime.equals(elemento.getData())){
							//se non è la prima volta allora inserisco la virgola prima dell'elemento successivo
							sBody+=",";
							sBody+= UriBuilder.encodeDatastreamAndValue(elemento.getStream(),elemento.getValore());
							iNumeroElemento++; // incremento il numero di elementi
							sPrecTime=elemento.getData();
							// inseriti
						} else {
							sBody+="}";
							bFlagExit=true;
						};
						
						if(!it.hasNext() && !bFlagExit) {
						//se non ci sono altri elementi allora chiudo 
							sBody+="}";
						}
						Log.d(TAG, this.getClass().getName() + " " + elemento.getData() + " " +elemento.getStream()+":"+elemento.getValore());
						
					}
				}
//				Log.d(TAG, this.getClass().getName()
//						+ " getBody() - Creazione stringa JSON TERMINATA");
				arrayListDataPointsTMP.clear();
				
				return new JSONResponseHelper(sBody,  iNumeroElemento);
	}

	public static String now() {
		//return new Timestamp(new Date().getTime()).toString();
		Date date = new Date();

		return String.valueOf(date.getTime()/1000);
	}

	public ArrayList<DataPoint> getDataPoints() {
		// TODO Auto-generated method stub
		return arrayListDataPoints;
		// return sortedSetDataPoints;
	}

	public int size() {
		// TODO Auto-generated method stub
		return arrayListDataPoints.size();
		// return sortedSetDataPoints.size();
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return arrayListDataPoints.isEmpty();
		// return sortedSetDataPoints.isEmpty();
	}

	public synchronized boolean add(DataPoint dataPoint) {
			arrayListDataPoints.add(dataPoint);
			return true;
	}

//	public synchronized void clear() {
//			sortedSetDataPoints.clear();
//	}

	// la cancellazione così non funziona, e se funziona è troppo lenta
	public synchronized void clear(Integer iNumeroElementiDaCaricare) {
		// cancella dalla lista gli elementi già caricati
		// estraggo una sublist, cancello la lista
		//se il numero di elementi caricati è uguale a quelli rimasti in lista (e da cancellare) allora semplicemente cancelo tutta la lista
		if(iNumeroElementiDaCaricare>=arrayListDataPoints.size()){
			arrayListDataPoints.clear();
		} else {
			//...altrimenti salvo prima la porzione rimanente di lista, cancello tutto e poi inserisco la parte salvata (faccio così perchè in altri modi ottengo errori
					ArrayList<DataPoint> arrayListDataPointsTMP = new ArrayList<DataPoint>();
					arrayListDataPointsTMP.addAll(arrayListDataPoints.subList(iNumeroElementiDaCaricare,arrayListDataPoints.size()));
					arrayListDataPoints.clear();
					arrayListDataPoints.addAll(arrayListDataPointsTMP);
					arrayListDataPointsTMP.clear();
		}
	}
}