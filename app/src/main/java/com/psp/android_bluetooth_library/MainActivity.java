package com.psp.android_bluetooth_library;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


/**
 * Author: prasad-psp
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnBluetoothActivity = findViewById(R.id.btnBluetoothActivity);
        Button btnConnectionActivity = findViewById(R.id.btnConnectionActivity);

        btnBluetoothActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,BluetoothActivity.class);
                startActivity(i);
            }
        });


        btnConnectionActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,ConnectionActivity.class);
                startActivity(i);
            }
        });
    }
}