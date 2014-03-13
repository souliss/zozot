package com.zozot.OEM.consumer;

import static com.zozot.OEM.consumer.Constants.TAG;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;

import com.zozot.OEM.consumer.R;
import com.zozot.OEM.database.DatabaseHelper;

import android.R.color;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.ToggleButton;


public class AssociationsActivity extends Activity {
	PreferenceHelper opzioni;
	ArrayList<Device> aSoulissDevices;
	ArrayList<String> aXFeeds;
	ArrayList<ToggleButton> aButtons =new ArrayList<ToggleButton> ();
	ArrayList<Spinner> aSpinners =new ArrayList<Spinner> (); 
	    
	@TargetApi(11)
	public void onBuildHeaders(List<Header> target) {
		Log.i(TAG, "AssociationsActivityonBuildHeaders()");
		//loadHeadersFromResource(R.xml.preference_headers, target);
	}

	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences prefs =   PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		super.onCreate(savedInstanceState);
	
		 setContentView(R.layout.associationsactivity);

		 //recupera i dati passati all'Activity
		Intent intent0= this.getIntent();
		String pkg=getPackageName();
		aSoulissDevices=intent0.getParcelableArrayListExtra(pkg+"SoulissDevices");
		aXFeeds=intent0.getStringArrayListExtra(pkg+"XivelyFeeds");
		
		
	    opzioni = ZozOtActivity.getOpzioni();
		// Add a button to the header list.
       
	   
	    TableLayout tl = (TableLayout) findViewById(R.id.tableLayout); 
	    for (int i=0;i<aSoulissDevices.size();i++ ){
	    	TableRow tr = new TableRow(this); 
            tr.setLayoutParams(new LayoutParams( 
                           TableRow.LayoutParams.MATCH_PARENT, 
                           LayoutParams.WRAP_CONTENT));
            tr.setBackgroundColor(Color.TRANSPARENT);
            //Button con nome del device
            //***************************
            ToggleButton button =new ToggleButton(getBaseContext());
//imposta i pulsanti relativi ai devices di Souliss
	    	button.setText(aSoulissDevices.get(i).getNomeDispositivo() + "\nNodo: " + aSoulissDevices.get(i).getIdNodo());
	    	//button.setMaxEms(12);
	    	button.setTextOn(aSoulissDevices.get(i).getNomeDispositivo() + "\nNodo: " + aSoulissDevices.get(i).getIdNodo());
	    	button.setTextOff(aSoulissDevices.get(i).getNomeDispositivo() + "\nNodo: " + aSoulissDevices.get(i).getIdNodo());
	    	button.setId(i); 
	    	button.setChecked(aSoulissDevices.get(i).bEnabled);
	    	//***************************
	    	tr.addView(button); 
	    	aButtons.add(button);
	    	
	    	//SPINNER	    	
	    	Spinner spinner1 = new Spinner(getApplicationContext());
	    	//spinner1.setBackgroundResource(color.background_light);
	    	
	    	
	        String[] listaStreamPerSpinner=new String[aXFeeds.size()+1];
	        //la prima voce è la voce nulla
	        listaStreamPerSpinner[0]=Constants.NULL_STREAM_NAME;
	        //carica i nomi degli stream disponibili nello spinner
	        int iSelectedPosition=0;
	        for (int j=0;j<aXFeeds.size();j++ ){
	        	listaStreamPerSpinner[j+1]=aXFeeds.get(j);

	        	if (aSoulissDevices.get(i).getStreamName()!=null &&aSoulissDevices.get(i).getStreamName().equals(aXFeeds.get(j))){
	        		iSelectedPosition=j+1;
	        	}
	        }
	        ArrayAdapter<String>  adapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,listaStreamPerSpinner);

	       	spinner1.setAdapter( adapter);
	       	spinner1.setSelection(iSelectedPosition, true);
	      
	    	//***************************
	    	tr.addView(spinner1); 
	    	aSpinners.add(spinner1);
	    	
            /* Add row to TableLayout. */ 
            tl.addView(tr,new TableRow.LayoutParams( 
            LayoutParams.MATCH_PARENT, 
            LayoutParams.WRAP_CONTENT));
            
	    }
          
	    
         //CONFIGURA PULSANTE PER CONFERMARE I DATI   
	    Button button1 = (Button) findViewById(R.id.btnConferma); 
	   
		button1.setOnClickListener(new OnClickListener() {
			TextView resultField = (TextView) findViewById(R.id.result);

			public void onClick(View v) {
				//ArrayList<String> aStreamsXSoulissDevice =new ArrayList<String> ();
				final DatabaseHelper dbHelper=new DatabaseHelper(getApplicationContext());
				dbHelper.deleteSoulissDevices(dbHelper.getWritableDatabase());
				for (int i=0;i<aSpinners.size();i++){
					String sStream=aSpinners.get(i).getSelectedItem().toString();
					if (sStream.equals(Constants.NULL_STREAM_NAME)) sStream=""; 
					aSoulissDevices.get(i).setsStream(sStream);
					aSoulissDevices.get(i).setbEnabled(aButtons.get(i).isChecked());
					//aggiorna anche il DB
					 dbHelper.insertSoulissDevice(dbHelper.getWritableDatabase(), aSoulissDevices.get(i).getNomeDispositivo(), String.valueOf(aSoulissDevices.get(i).getIdNodo()), String.valueOf(aSoulissDevices.get(i).getIdDispositivo()), aSoulissDevices.get(i).getStreamName(), aSoulissDevices.get(i).bEnabled);
				}
				Intent risposta = new Intent();
				risposta.putParcelableArrayListExtra(getPackageName(), aSoulissDevices);
				
				setResult(RESULT_OK, risposta);
				  finish();
				}	
		});
	}

	@SuppressLint("NewApi")
	@Override
	protected void onStart() {
		super.onStart();
	
			Log.d(TAG, "Going thru associationsActivity onStart()");
			if (opzioni!=null) {
				opzioni.reload();	
			}
			
	}
		
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		//unregisterReceiver(macacoRawDataReceiver);
		  super.onPause();
		ArrayList<String> aStreamsXSoulissDevice =new ArrayList<String> ();
		
		for (int i=0;i<aSpinners.size();i++){
			aStreamsXSoulissDevice.add(aSpinners.get(i).getSelectedItem().toString());
		}
		
		Intent risposta = new Intent();
		risposta.putStringArrayListExtra(getPackageName(), aStreamsXSoulissDevice);
		
		//Restituisco il codice identificativo dell'activity corrente e la risposta
		setResult(RESULT_OK, risposta);
		  finish();
		//opzioni.reload();
	
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {

	    Log.v(TAG, "OnSharedPreferencesChanged run" ); // TODO Testing Purposes

	}

	
}

