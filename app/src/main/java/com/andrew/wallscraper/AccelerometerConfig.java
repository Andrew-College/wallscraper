package com.andrew.wallscraper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Andmin on 18/12/2014.
 */
public class AccelerometerConfig extends Activity implements SensorEventListener {


   private SensorManager senSensorManager;
   private Sensor senAccelerometer;

   TextView xMagnitude;
   TextView yMagnitude;
   TextView zMagnitude;
   TextView updatingValues;
   private float minCutoffValue = 0;
   private float strengthValue = 1;
   private SeekBar cutoffProgress;
   private SeekBar powerProgress;
   private MyGLSurfaceView renderView;
   private float[][] tiltAcceleration;

   private static final int REQUEST_CODE = 10;

   public Intent parentIntent;


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.configure_accelerometer);
      parentIntent = getIntent();

      tiltAcceleration = new float[3][3];

      xMagnitude = (TextView) findViewById(R.id.xMagnitude);
      yMagnitude = (TextView) findViewById(R.id.yMagnitude);
      zMagnitude = (TextView) findViewById(R.id.zMagnitude);
      updatingValues = (TextView) findViewById(R.id.areValuesUpdating);

      renderView = (MyGLSurfaceView) findViewById(R.id.renderView);

      cutoffProgress = (SeekBar)findViewById(R.id.accelerometerCutoffValue);
      cutoffProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
         @Override
         public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
         }

         @Override
         public void onStartTrackingTouch(SeekBar seekBar) {

         }

         @Override
         public void onStopTrackingTouch(SeekBar seekBar) {
            minCutoffValue = (seekBar.getProgress()+ 0.0f) / 100.0f;
         }
      });

      powerProgress = (SeekBar)findViewById(R.id.strengthSeekbar);
      powerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
         @Override
         public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
         }

         @Override
         public void onStartTrackingTouch(SeekBar seekBar) {

         }

         @Override
         public void onStopTrackingTouch(SeekBar seekBar) {
            strengthValue = seekBar.getProgress();
         }
      });


      Button saveButton = (Button) findViewById(R.id.saveButton);
      saveButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Log.d("app","SAVE RUN OK");
            Intent resultIntent = new Intent();
            resultIntent.putExtra("newCutoff", minCutoffValue);
            resultIntent.putExtra("strength", strengthValue);
            setResult(1,resultIntent);
            finish();
         }
      });

      /**
       * Accelerometer
       */
      //setup accelerometer
      senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
      senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_GAME);


//      this.addContentView(renderer,null);
   }

   @Override
   public void onSensorChanged(SensorEvent event) {
      Sensor mySensor = event.sensor;

      if (
         Math.abs(event.values[0]) > minCutoffValue ||
            Math.abs(event.values[1]) > minCutoffValue
            //|| event.values[2] > minCutoffValue

         ) {
         if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            updatingValues.setText("true");
            xMagnitude.setText(String.valueOf(event.values[0]));
            yMagnitude.setText(String.valueOf(event.values[1]));
//            zMagnitude.setText(String.valueOf(event.values[2]));

            tiltAcceleration[0][0] = tiltAcceleration[2][0];
            tiltAcceleration[0][1] = tiltAcceleration[2][1];
            tiltAcceleration[0][2] = tiltAcceleration[2][2];
            //current
            tiltAcceleration[2][0] = event.values[0];
            tiltAcceleration[2][1] = event.values[1];
            tiltAcceleration[2][2] = event.values[2];
            //difference
            tiltAcceleration[1][0] = strengthValue * (tiltAcceleration[0][0] - tiltAcceleration[2][0]);
            tiltAcceleration[1][1] = strengthValue * (tiltAcceleration[0][1] - tiltAcceleration[2][1]);
            tiltAcceleration[1][2] = strengthValue * (tiltAcceleration[0][2] - tiltAcceleration[2][2]);

            renderView.passArray(tiltAcceleration[1]);
         }
      }else{
         updatingValues.setText("false");
      }
   }

   @Override
   public void onAccuracyChanged(Sensor sensor, int accuracy) {

   }
}
