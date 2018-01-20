/*
* Copyright 2015 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.tareg.seedeway;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.PermissionChecker;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


// The Beacon of light
 class BeaconView extends View {
    private Paint paint = new Paint();
    private float mHorizontal;
    private final int BARS = 15;
    private final float WIDTH = 9;
    private float HEIGHT = 500;
    public BeaconView(Context context){
        this(context, 0);
    }
    public BeaconView(Context context, float horizontal) {
        super(context);
        mHorizontal = horizontal;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        HEIGHT = displayMetrics.heightPixels;
    }

    @Override
    public void onDraw(Canvas canvas) {
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1);

        paint.setColor(Color.WHITE);
        for(int i=0;i<BARS;i++){

            paint.setAlpha(200-12*Math.abs(BARS/2-i));

            canvas.drawRect(mHorizontal+(i-1)*(WIDTH), 0, WIDTH*i+mHorizontal, HEIGHT, paint );


        }

    }

}
public class CamActivity extends Activity implements SensorEventListener{


    /**
     * Id of the camera to access. 0 is the first camera.
     */
    private static final int CAMERA_ID = 0;

    private CameraPreview mPreview;
    private Camera mCamera;
    private FrameLayout preview;
    private LocationManager locationManager;
    LocationListener locationListener;
    float azimuth;
    float pitch;
    float roll;
    double latitude;
    double longitude;
    double altitude;
    private SensorManager mSensorManager;
    TextView indicatorView;
    Sensor accelerometer;
    Sensor magnetometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // Open an instance of the first camera and retrieve its info.
        mCamera = getCameraInstance(CAMERA_ID);
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(CAMERA_ID, cameraInfo);

        if (mCamera == null || cameraInfo == null) {
            // Camera is not available, display error message
            Toast.makeText(this, "Camera is not available.", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_main);
        } else {

            setContentView(R.layout.cam_layout);

            // Get the rotation of the screen to adjust the preview image accordingly.
            final int displayRotation = getWindowManager().getDefaultDisplay()
                    .getRotation();

            // Create the Preview view and set it as the content of this Activity.
            mPreview = new CameraPreview(this, mCamera, cameraInfo, displayRotation);
            preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);

            BeaconView view = new BeaconView(this, 0);
            view.setBackgroundColor(Color.TRANSPARENT);
            preview.addView(view);
            addIndicator();
        }

        // Location nonsense
        locationManager = (LocationManager)
                getSystemService(this.LOCATION_SERVICE);
        locationListener = new CamActivity.MyLocationListener();

        Criteria oGPSSettings = new Criteria();
        oGPSSettings.setAccuracy(Criteria.ACCURACY_FINE);
        oGPSSettings.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        oGPSSettings.setSpeedRequired(false);
        oGPSSettings.setAltitudeRequired(true);
        // oGPSSettings.setBearingRequired(false);
        // oGPSSettings.setCostAllowed(false);
        // oGPSSettings.setPowerRequirement(Criteria.POWER_MEDIUM);
        // More location nonsense
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.getBestProvider(oGPSSettings, true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } else {
            Toast.makeText(this, "fuckadoodledo", Toast.LENGTH_LONG).show();
        }

        // EARTH SENSORS
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    @Override
    public void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
       mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera access
        releaseCamera();
        // Stop sensor access
        mSensorManager.unregisterListener(this);
    }

    float[] mGravity;
    float[] mGeomagnetic;
    public void onSensorChanged(SensorEvent event) {
        try {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values;
            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    azimuth = orientation[0]; // orientation contains: azimut, pitch and roll
                    pitch = orientation[1];
                    roll = orientation[2];
                    String info = String.format(Locale.ENGLISH,
                            "%.3f\n%.3f\n%.3f\n\n%.9f\n%.9f\n%.9f",
                            azimuth, pitch, roll, longitude, latitude, altitude);
                    adjustBeacon();
                    indicatorView.setText(
                            info
                    );

                }
            }
        }catch(Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    /**
     * A safe way to get an instance of the Camera object.
     */
    private Camera getCameraInstance(int cameraId) {
        Camera c = null;
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Toast.makeText(this, "Camera " + cameraId + " is not available: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();

        }
        return c; // returns null if camera is unavailable
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            try {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                altitude = location.getAltitude();


            }catch(Exception e){
                Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {

            Toast.makeText(getBaseContext(), "Gps is turned off!! ",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {

            Toast.makeText(getBaseContext(), "Gps is turned on!! ",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(getBaseContext(), "Status changed!" , Toast.LENGTH_SHORT).show();

        }
    }
    private void popView() {
        preview.removeViewAt(preview.getChildCount()-1);
    }
    private void adjustBeacon(){
        try{
            popView();
            popView();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;

            BeaconView view = new BeaconView(this, width/2);
            view.setBackgroundColor(Color.TRANSPARENT);
            preview.addView(view);
            addIndicator();

        }catch(Exception e){
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    private void addIndicator(){
        try{
            FrameLayout.LayoutParams lparams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            indicatorView = new TextView(this);
            indicatorView.setLayoutParams(lparams);
            indicatorView.setText("DFDSF");
            preview.addView(indicatorView);
        }catch(Exception e){
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}

