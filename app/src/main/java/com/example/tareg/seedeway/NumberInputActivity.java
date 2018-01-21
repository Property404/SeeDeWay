package com.example.tareg.seedeway;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NumberInputActivity extends Activity {
    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.number_layout);

        editText = findViewById(R.id.phoneNumber);
    }

    public void submit(View view){
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("peer_number", editText.getText().toString());
        editor.apply();
        Intent intent = new Intent(this, CamActivity.class);
        startActivity(intent);
    }
}
