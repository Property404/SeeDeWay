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
import com.loopj.android.http.*;
import android.content.Context;
import android.content.Intent;
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
import android.preference.PreferenceManager;
import android.support.v4.content.PermissionChecker;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;


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

    // Peer number
    String peer_number="<undefined_peer>";
    // And self
    String own_number="<undefined_host>";

    // Position/orientation parameters of the self
    float azimuth;
    float pitch;
    float roll;
    double latitude;
    double longitude;
    double altitude;

    // And of the peer
    double peer_latitude=2.5;
    double peer_longitude=-2.5;

    // And the joint
    double radius;
    double raw_theta;
    double theta_max;

    // Used for bar animation
    float bar_x;

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

        // Get Peer identity
        peer_number = PreferenceManager.getDefaultSharedPreferences(this).getString("peer_number", "<unset>");
        // Find peer
        getPeerLocation();

        // Identify self
        TelephonyManager tMgr =(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        own_number = tMgr.getLine1Number();
        if (own_number==null){
            own_number = "<na>";
        }


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
        //releaseCamera();
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
                    adjustBeacon();
                    updateText();
                }
            }
        }catch(Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void updateText(){
        String info = String.format(Locale.ENGLISH,
                "%.5f rad\n%.5f\n%.5f\n\n%.15f deg\n%.15f deg\n%.15f\n\n"+
                        "peer: %s\n%.15f\n%.15f\n\n%.5f km\n%.5f rad",
                azimuth, pitch, roll, longitude, latitude, altitude,
                peer_number, peer_longitude, peer_latitude,
                radius, raw_theta);
        indicatorView.setText(
                info
        );
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
                radius = Haversine.distance(latitude, longitude, peer_latitude, peer_longitude);
                double y = Math.sin(Math.toRadians(peer_longitude-longitude))
                        *Math.cos(Math.toRadians(peer_latitude));
                double x = Math.cos(Math.toRadians(latitude))*Math.sin(Math.toRadians(peer_latitude))-
                        Math.sin(Math.toRadians(latitude))*
                                Math.cos(Math.toRadians(peer_latitude))
                                *Math.cos(Math.toRadians(peer_longitude-longitude));
                raw_theta = Math.atan2(y, x);
                theta_max = Math.atan(radius*4000);
                getPeerLocation();
                sendLocation();

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
            double theta_relative = -raw_theta + azimuth;
            double xr =width*(1-theta_relative+theta_max)/(2*theta_max);
            /*if(bar_x>xr+20){
                bar_x-=20;
            }else if(bar_x<xr-20){
                bar_x+=20;
            }else{*/
                bar_x = (float)xr;
            BeaconView view = new BeaconView(this, bar_x);
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
            preview.addView(indicatorView);
        }catch(Exception e){
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendLocation(){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("number", own_number);
        params.put("latitude", latitude);
        params.put("longitude", longitude);
        client.post("http://138.68.251.123/mainPage/", params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }
    private void getPeerLocation(){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("number", peer_number);
        client.get("http://138.68.251.123/mainPage", params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    String resp = new String(response, "UTF-8");
                    JSONObject json = new JSONObject(resp);
                    peer_latitude = json.getDouble("latitude");
                    peer_longitude = json.getDouble("longitude");
                }catch(Exception e){
                    Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }

    public void editNumber(View view){
        Intent intent = new Intent(this, NumberInputActivity.class);
        startActivity(intent);
    }
}

