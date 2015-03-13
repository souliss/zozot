package com.zozot.OEM.consumer;

import android.os.Parcel;
import android.os.Parcelable;

public class Device implements Parcelable {
	private int iNodo;
	private int iDispositivo;
	private String sNome;
	private String sStream;
	public boolean bEnabled;
	private String iTypical;

	public void setbEnabled(boolean bEnabled) {
		this.bEnabled = bEnabled;
	}

	/**
	 * @param iNodo
	 * @param iDispositivo
	 * @param iTypical 
	 * @param nome
	 */
	public Device(int iNodo, int iDispositivo, String iTypical, String nome) {
		super();
		this.setIdNodo(iNodo);
		this.setiIdDispositivo(iDispositivo);
		this.setNomeDispositivo(nome);
		this.setTypical(iTypical);
	}

	public Device(Parcel in) {
		setIdNodo(in.readInt());
		setiIdDispositivo(in.readInt());
		setNomeDispositivo(in.readString());
		setTypical(in.readString());
		setsStreamName(in.readString());
		bEnabled = in.readByte() != 0;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(getIdNodo());
		dest.writeInt(getIdDispositivo());
		dest.writeString(getNomeDispositivo());
		dest.writeString(getTypical());
		dest.writeString(getStreamName());
		dest.writeByte((byte) (bEnabled ? 1 : 0)); 
	}

	public static final Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>() {
		public Device createFromParcel(Parcel in) {
			return new Device(in);
		}

		public Device[] newArray(int size) {
			return new Device[size];
		}
	};

	public void setsStream(String sStream) {
		this.setsStreamName(sStream);
	}

	public int getIdDispositivo() {
		return iDispositivo;
	}

	public void setiIdDispositivo(int iDispositivo) {
		this.iDispositivo = iDispositivo;
	}

	public int getIdNodo() {
		return iNodo;
	}

	public void setIdNodo(int iNodo) {
		this.iNodo = iNodo;
	}
	
	public String getStreamName() {
		return sStream;
	}

	public void setsStreamName(String sStream) {
		this.sStream = sStream;
	}

	public String getNomeDispositivo() {
		return sNome;
	}

	public void setNomeDispositivo(String sNome) {
		this.sNome = sNome;
	}

	public String getTypical() {
		return iTypical;
	}

	public void setTypical(String iTypical) {
		this.iTypical = iTypical;
	}

}
