package com.zozot.OEM.JSONBuilderHelper;

//questa clase viene usata per restituire in un unico corpo sia la stringa JSON che il numero di elementi che contiene e che verranno caricati
public class JSONResponseHelper {

	private String sBody;
	private boolean bResponse;
	private int iElementiCaricati;

	public JSONResponseHelper(String sBody, int iElementiCaricati) {
		// TODO Auto-generated constructor stub
		this.sBody=sBody;
		this.setElementiCaricati(iElementiCaricati);
	}
//	String sJSONBody;
//	 boolean bResponse;
//	 int iNumeroElementi;

	public String getBody() {
		return sBody;
	}

	public void setBody(String sBody) {
		this.sBody = sBody;
	}

	public int getElementiCaricati() {
		return iElementiCaricati;
	}

	public void setElementiCaricati(int iElementiCaricati) {
		this.iElementiCaricati = iElementiCaricati;
	}

	
	}
