package com.zozot.OEM.consumer;

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
		this.data = data.trim();
	}
	
	public String getValore() {
		return valore;
	}
	public void setValore(String valore) {
		this.valore = valore.trim();
	}


	public String getStream() {
		return stream;
	}


	public void setStream(String stream) {
		this.stream = stream.trim();
	}


	@Override
	public int compareTo(DataPoint arg0) {
		// TODO Auto-generated method stub
		
		int iComp= this.getStream().compareTo(arg0.getStream());
		//l a comparazione non deve mai essere uguale a zero altrimenti l'elemento viene scartato dalla lista
	    if (iComp==0) iComp=1; 
		
		return iComp;
	}


//	/* (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object o) {
//		DataPoint objDataPoint=(DataPoint) o;
//		if(objDataPoint.data.equals(this.data) && objDataPoint.stream.equals(this.stream) && objDataPoint.valore.equals(this.valore)) 
//			return true;
//		else
//			return super.equals(o);
//	}
}
