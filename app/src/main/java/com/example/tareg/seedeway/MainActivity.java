package com.example.tareg.seedeway;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
implements SensorEventListener{
    private TextView positionView;
    private TextView rotationView;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private LocationManager locationManager;
    private LocationListener locationListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up layout resources
        positionView = findViewById(R.id.position);
        rotationView = findViewById(R.id.magfield);

        // Request all permissions needed
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);

        // Try to get orientation sensors
         mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        // Location nonsense
        locationManager = (LocationManager)
                getSystemService(this.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

        Criteria oGPSSettings = new Criteria();
        oGPSSettings.setAccuracy(Criteria.ACCURACY_FINE);
        oGPSSettings.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        oGPSSettings.setSpeedRequired(false);
        oGPSSettings.setAltitudeRequired(true);
       // oGPSSettings.setBearingRequired(false);
       // oGPSSettings.setCostAllowed(false);
       // oGPSSettings.setPowerRequirement(Criteria.POWER_MEDIUM);
        // More location nonsense
        if ( PermissionChecker.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED ) {
            locationManager.getBestProvider(oGPSSettings, true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            Toast.makeText(this, "granted", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "fuckadoodledo", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
    }
    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        String msg = sensorEvent.values[0] + ":";
        rotationView.setText(msg);
    }



    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            try {
                String msg = location.getLatitude()
                        + "/" + location.getLongitude()
                        + "/" + location.getAltitude();

                if(!location.hasAltitude()){msg+="noalt";}
                positionView.setText(msg);
                Toast.makeText(getBaseContext(), "!", Toast.LENGTH_LONG).show();
            }catch(Exception e){
                Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {

            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
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
            Toast.makeText(getBaseContext(), "j" , Toast.LENGTH_SHORT).show();

        }
    }
}

