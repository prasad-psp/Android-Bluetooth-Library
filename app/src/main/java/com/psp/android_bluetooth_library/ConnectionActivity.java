package com.psp.android_bluetooth_library;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Author: prasad-psp
 */
public class ConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        Button btnConnect = findViewById(R.id.btnConnect);
        Button btnAccept = findViewById(R.id.btnAccept);


        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ConnectionActivity.this,ConnectActivity.class);
                startActivity(i);
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ConnectionActivity.this,AcceptActivity.class);
                startActivity(i);
            }
        });
    }
}