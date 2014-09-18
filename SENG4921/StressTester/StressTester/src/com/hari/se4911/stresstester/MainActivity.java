package com.hari.se4911.stresstester;

import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.hari.se4911.stresstester.recorders.AcceleratorRecorder;
import com.hari.se4911.stresstester.recorders.HygrometerRecorder;
import com.hari.se4911.stresstester.recorders.VoiceRecorder;
import com.hari.se4911.stresstester.recorders.sensors.StressSensorEventListener;
import com.hari.se4911.stresstester.results.DataAnalyzer;
import com.hari.se4911.stresstester.results.DataParser;
import com.hari.se4911.stresstester.results.NoResultsException;
import com.hari.se4911.stresstester.results.StressResult;

public class MainActivity extends ActionBarActivity {

	private SensorManager mSensorManager;
	private SensorEventListener mSensorListener;
	AcceleratorRecorder aa;
	HygrometerRecorder ha;
	VoiceRecorder va;
	
	DataAnalyzer dataRes;
	StressResult currRes;
	
	private long ONE_MINUTE = 5*1000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initialize();
	}
	

	@Override
	protected void onResume() {
	    // Register a listener for the sensor.
		super.onResume();
		if (mSensorListener != null && mSensorManager != null) {
		    mSensorManager.registerListener(mSensorListener, 
		    		mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
		    		SensorManager.SENSOR_DELAY_NORMAL);
		    mSensorManager.registerListener(mSensorListener, 
		    		mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY),
		    		SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		if (va != null) {
			if (!va.isRecording()) {
				va.startRecord();	
			}
		}
	}
	
	@Override
	protected void onPause() {
		// Be sure to unregister the sensor when the activity pauses.
		super.onPause();
		stopSensors();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/*
	 * ********************STRESS IMPLEMENTATION*********************
	 */

	private void initialize() {
		loadData("data.csv");
	}

	private void initSensors() {
		ha = new HygrometerRecorder();
		aa = new AcceleratorRecorder();
		
		mSensorManager = (SensorManager) this
		                .getSystemService(Context.SENSOR_SERVICE);
		mSensorListener = new StressSensorEventListener(aa, ha);

		mSensorManager.registerListener(mSensorListener, 
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mSensorListener, 
				mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY),
				SensorManager.SENSOR_DELAY_NORMAL);
		
		va = new VoiceRecorder();
		va.startRecord();
	}

	private void loadData(String data) {
		DataParser dp = new DataParser(data);
		try {
			dp.parse();
			dataRes = new DataAnalyzer(dp.getResults());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			dataRes = new DataAnalyzer();
		}
		dataRes.analyze();
	}

	public boolean testStress(View v) {
		initSensors();
		
		final TextView res = (TextView) findViewById(R.id.results);
		
		Timer tim = new Timer();
		TimerTask tm = new TimerTask() {
			
			@Override
			public void run() {
				stopSensors();
				
				String temp;
				try {
					analyzeResults();
					temp = stringifyRes();
				} catch (NoResultsException e) {
					e.printStackTrace();
					temp = "Error in generated results.";
				}
				
				final String toPrint = new String(temp);
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						res.setText(toPrint);
					}
				});
				
				//TODO: CHECK IF ACTUALLY STRESSED?
				
			}
		};
		
		tim.schedule(tm, ONE_MINUTE);
		
		res.setText("Detecting stress...");
		return true;
		
	}
	
	protected void stopSensors() {
		if (mSensorListener != null && mSensorManager != null && 
				va != null) {
			mSensorManager.unregisterListener(mSensorListener);
		}
		
		if (va != null) {
			if (va.isRecording()) {
				va.stopRecord();	
			}
		}
	}

	private void analyzeResults() throws NoResultsException {
		currRes = new StressResult(aa.getResults(), ha.getResults(), 
				va.getResults());
		currRes.analyze();
	}
	
	public String stringifyRes() {
		StringBuilder sbr = new StringBuilder();
		sbr.append("Accelerometer turn count: ")
			.append("\t" + currRes.getAvgCountTurns()[0] + " " +
					currRes.getAvgCountTurns()[1])
			.append("\n");
		sbr.append("Hygrometer average: ")
			.append(currRes.getAvgHydro())
			.append("\n");
		sbr.append("Average amplitude of voice: ")
			.append("\t" + currRes.getAvgVoice()[0] + " " +
					currRes.getAvgVoice()[1])
			.append("\n");
		sbr.append("\n");
		
		String stressAns;
		if (currRes.isStressed()) stressAns = "YES!";
		else stressAns = "No.";
		
		sbr.append("Are you stressed? ").append(stressAns);
		
		return sbr.toString();
		
	}
	  
}
