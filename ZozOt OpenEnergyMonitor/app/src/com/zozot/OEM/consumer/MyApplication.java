package com.zozot.OEM.consumer;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {
    
	String TextBoxLog;
	


	//singleton design pattern
	static MyApplication instance; 
    public static MyApplication getInstance(){
            if(instance==null){
                    Log.v(Constants.TAG_LivelloAvvisiApplicazione, "instance created");
                    instance=new MyApplication();
            }
            Log.v(Constants.TAG_LivelloAvvisiApplicazione, "instance returned");
            return instance;
    }
   
   
    @Override
    public void onCreate() {
            super.onCreate();
            Log.v(Constants.TAG_LivelloAvvisiApplicazione, "onCreate");
            MyApplication myApp=getInstance();
           
    }



    public String getTextBoxLog() {
		return TextBoxLog;
	}


	public void setTextBoxLog(String textBoxLog) {
		TextBoxLog = textBoxLog;
	}
	
	public void appendTextBoxLog(String textBoxLog) {
		TextBoxLog = TextBoxLog + textBoxLog;
	}
}