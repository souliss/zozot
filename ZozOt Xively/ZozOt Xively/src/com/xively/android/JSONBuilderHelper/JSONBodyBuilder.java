package com.xively.android.JSONBuilderHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import android.util.Log;

import com.xively.android.consumer.Constants;
import com.xively.android.consumer.DataPoint;

public class JSONBodyBuilder {

	private String sBody = "";
	SortedSet<DataPoint> sortedSetDataPoints;

	// gli elementi vengono aggiunti sempre nell'arraylist, poi vengono spostati
	// nella lista ordinata prima della creazione della stringa JSON
	ArrayList<DataPoint> arrayListDataPoints;
	private static final String TAG = Constants.TAG_LivelloAvvisiServizio;

	public JSONBodyBuilder() {
		sortedSetDataPoints = new TreeSet<DataPoint>();
		arrayListDataPoints = new ArrayList<DataPoint>();

		Log.d(TAG, this.getClass().getName()
				+ "  JSONBodyBuilder() - Creazione oggetto creatore JSON");
	}

	public synchronized JSONResponseHelper getBody(Integer iNumeroElementiDaCaricare)
			throws InterruptedException {
		// TODO CREAZIONE BODY

				// se il numero di elementi da caricare � maggiore della
				// dimensione dell'array allora pongo l'indice massimo alla dimensione dell'array
				if (iNumeroElementiDaCaricare > arrayListDataPoints.size())
					iNumeroElementiDaCaricare = arrayListDataPoints.size();

				int iTopList = ((iNumeroElementiDaCaricare - 1) < 1) ? 0
						: iNumeroElementiDaCaricare - 1; // faccio in modo che
															// l'indice
															// superiore non sia
															// mai inferiore a 0
				sortedSetDataPoints.addAll(arrayListDataPoints.subList(0,
						iTopList));

				Log.d(TAG, this.getClass().getName()
						+ " getBody() - Creazione stringa JSON");
				int iNumeroElemento = 0;
				if (!sortedSetDataPoints.isEmpty()) {

					Iterator<DataPoint> it = sortedSetDataPoints.iterator();

					DataPoint elemento;
					String sStream_elementoTMP = null;

					sBody = "{\"version\":\"1.0.0\",\"datastreams\" : [ "; // adesso
																			// manda
																			// il
																			// nome
																			// dello
																			// stream
					// myNewDatapoint = "{ \"datapoints\": [";
					Log.d(TAG, this.getClass().getName() + " Coda: "
							+ sortedSetDataPoints.size());
					
					while (it.hasNext()
							&& iNumeroElemento <= iNumeroElementiDaCaricare) {
						iNumeroElemento++; // incremento il numero di elementi
											// inseriti
						elemento = (DataPoint) it.next();

						if (sStream_elementoTMP != null
								&& (elemento.getStream()
										.equals(sStream_elementoTMP))) {
							// se � uguale allo stream precedente inserisco solo
							// "at"....
							sBody += ", {\"at\":\"" + elemento.getData()
									+ "\",\"value\":\"" + elemento.getValore()
									+ "\"}"; // inserisco
												// il
												// data/ora
												// ed
												// il
												// valore

						} else {
							if (sStream_elementoTMP == null) {
								// se sStream_elementoTMP � NULL vuol dire che
								// siamo
								// al primo elemento
								sBody += "{\"id\": \"";
							} else {
								// ...altimenti vuol dire che non siamo al primo
								// elemento ed occorre chiudere il precedente
								// prima
								// di proseguire
								sBody += "]}, {\"id\": \""; // se lo stream non
															// � il
															// primo (solo per
															// il
															// primo
															// sStream_elementoTMP
															// �
															// null), ed �
															// diverso
															// dal precedente
															// allora
															// chiudo l'elenco
															// di
															// valori per lo
															// stream
															// e passo al
															// successivo
							}
							sBody += elemento.getStream() + "\","; // aggiungo
																	// il
																	// nome
																	// dello
																	// stream
																	// alla
																	// stringa
																	// JSON
							sBody += "\"datapoints\": [";
							sBody += " {\"at\":\"" + elemento.getData()
									+ "\",\"value\":\"" + elemento.getValore()
									+ "\"} "; // inserisco
												// il
												// data/ora
												// ed
												// il
												// valore
						}

						sStream_elementoTMP = elemento.getStream();
					}

					sBody += "]} ] }";

				}

				Log.d(TAG, this.getClass().getName()
						+ " getBody() - Creazione stringa JSON TERMINATA");
				sortedSetDataPoints.clear();
								
				return new JSONResponseHelper(sBody,  iNumeroElemento+1);
	}

	public static String now() {
		// TODO Auto-generated method stub
		// formato esempio: 2014-02-10T23:45:50.492210Z

		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		s.setTimeZone((TimeZone.getTimeZone("gmt")));
		return s.format(new Date());
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
//					sortedSetDataPoints.clear();
//	}

	
	public synchronized void clear(Integer iNumeroElementiDaCaricare) {
		// cancella dalla lista gli elementi gi� caricati
		// estraggo una sublist, cancello la lista
		//se il numero di elementi caricati � uguale a quelli rimasti in lista (e da cancellare) allora semplicemente cancelo tutta la lista
		if(iNumeroElementiDaCaricare>=arrayListDataPoints.size()){
			arrayListDataPoints.clear();
		} else {
			//...altrimenti salvo prima la porzione rimanente di lista, cancello tutto e poi inserisco la parte salvata (faccio cos� perch� in altri modi ottengo errori
					ArrayList<DataPoint> arrayListDataPointsTMP = new ArrayList<DataPoint>();
					arrayListDataPointsTMP.addAll(arrayListDataPoints.subList(iNumeroElementiDaCaricare,arrayListDataPoints.size()-1));
					arrayListDataPoints.clear();
					arrayListDataPoints.addAll(arrayListDataPointsTMP);
		}
	}
}