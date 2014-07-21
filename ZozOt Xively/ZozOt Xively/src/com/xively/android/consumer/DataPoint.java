package com.xively.android.consumer;

public class DataPoint  implements Comparable<DataPoint> {
	String data;
	String valore;
	String stream;
	
/**
	 * @param data
	 * @param valore
	 */
	public DataPoint(String stream, String data, String valore){
		super();
		this.data = data;
		this.valore = valore;
		this.stream=stream;
	}

	
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
	public String getValore() {
		return valore;
	}
	public void setValore(String valore) {
		this.valore = valore;
	}


	public String getStream() {
		return stream;
	}


	public void setStream(String stream) {
		this.stream = stream;
	}


	@Override
	public int compareTo(DataPoint arg0) {
		// TODO Auto-generated method stub
		
		int iComp= this.getStream().compareTo(arg0.getStream());
		//l a comparazione non deve mai essere uguale a zero altrimenti l'elemento viene scartato dalla lista
	    if (iComp==0) iComp=1; 
		
		return iComp;
	}


}
