package com.example.tareg.seedeway;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

public class genisis extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        requestPermissions(new String[]{Manifest.permission.INTERNET}, 1);
        requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);

        Intent intent = new Intent(this, CamActivity.class);
        startActivity(intent);

    }
}
