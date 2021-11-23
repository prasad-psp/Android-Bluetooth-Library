package com.psp.android_bluetooth_library;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.psp.bluetoothlibrary.BluetoothListener;
import com.psp.bluetoothlibrary.Connection;

/**
 * Author: prasad-psp
 */
public class AcceptActivity extends AppCompatActivity {

    private final String TAG = "psp.AcceptAct";

    // UI
    Button btnListen,btnDisconnect,btnSend,btnSendReceive;
    EditText edtMessage;
    TextView txtDisplay;

    // Connection
    private Connection connection;

    @Override
    protected void onStart() {
        super.onStart();
        if(connection.isConnected()) {
            logMsg("initialize receive listener");
            connection.setOnReceiveListener(receiveListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logMsg("onDestroy");
        disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept);
        init();

        // initialize connection object
        logMsg("initialize connection object");
        connection = new Connection(this);

        // set UUID ( optional )
        // connection.setUUID(your_uuid);

        // Send data
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = edtMessage.getText().toString().trim();

                if(msg.isEmpty()) {
                    return;
                }

                if(connection.send(msg)) {
                    logMsg("[TX] "+msg);
                    txtDisplay.append("\n[TX] "+msg);
                    setDisplayMessageScrollBottom();
                }
                else {
                    logMsg("[TX] Failed "+msg);
                    txtDisplay.append("\n[TX] Failed"+msg);
                    setDisplayMessageScrollBottom();
                }
            }
        });


        // Send Receive in another activity
        btnSendReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connection.isConnected()) {
                    Intent i = new Intent(AcceptActivity.this,SendReceiveActivity.class);
                    startActivity(i);
                }
                else {
                    Toast.makeText(AcceptActivity.this, "Device not connected", Toast.LENGTH_SHORT).show();
                    logMsg("Device not connected");
                }
            }
        });

        // Disconnect
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });


        // Listen connection
        btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connection.accept(true,connectionListener,receiveListener)) {
                    logMsg("Start listening process");
                }
                else {
                    logMsg("Start listening process failed");
                }
            }
        });
    }

    private final BluetoothListener.onConnectionListener connectionListener = new BluetoothListener.onConnectionListener() {
        @Override
        public void onConnectionStateChanged(BluetoothSocket socket, int state) {
            switch (state) {
                case Connection.START_LISTENING: {
                    logMsg("Start Listening...");
                    txtDisplay.append("\n[ST] Start listening...");
                    setDisplayMessageScrollBottom();
                    break;
                }

                case Connection.CONNECTED: {
                    logMsg("Connected");
                    txtDisplay.append("\n[ST] Connected");
                    setDisplayMessageScrollBottom();
                    break;
                }

                case Connection.DISCONNECTED: {
                    logMsg("Disconnected");
                    txtDisplay.append("\n[ST] Disconnected");
                    setDisplayMessageScrollBottom();
                    disconnect();
                    break;
                }
            }
        }

        @Override
        public void onConnectionFailed(int errorCode) {
            switch (errorCode) {
                case Connection.SERVER_SOCKET_NOT_FOUND: {
                    logMsg("Server socket not found");
                    txtDisplay.append("\n[ST] Server socket not found");
                    setDisplayMessageScrollBottom();
                    break;
                }

                case Connection.ACCEPT_FAILED: {
                    logMsg("Accept failed");
                    txtDisplay.append("\n[ST] Accept failed");
                    setDisplayMessageScrollBottom();
                    break;
                }
            }

            disconnect();
        }
    };

    private final BluetoothListener.onReceiveListener receiveListener = new BluetoothListener.onReceiveListener() {
        @Override
        public void onReceived(String receivedData) {
            logMsg("[RX] "+receivedData);
            txtDisplay.append("\n[RX] "+receivedData);
            setDisplayMessageScrollBottom();
        }
    };


    private void init() {
        btnListen = findViewById(R.id.btnAcceptListen);
        btnDisconnect = findViewById(R.id.btnAcceptDisconnect);
        btnSend = findViewById(R.id.btnAcceptSend);
        btnSendReceive = findViewById(R.id.btnAcceptSendReceive);
        edtMessage = findViewById(R.id.edtAcceptMessage);
        txtDisplay = findViewById(R.id.txtAcceptDisplay);

        txtDisplay.setMovementMethod(new ScrollingMovementMethod());
    }

    private void disconnect() {
        if (connection != null) {
            logMsg("Disconnect manual");
            connection.disconnect();
            txtDisplay.append("\n[ST] Disconnect manual");
            setDisplayMessageScrollBottom();
        }
    }

    private void setDisplayMessageScrollBottom() {
        final Layout layout = txtDisplay.getLayout();
        if(layout != null){
            int scrollDelta = layout.getLineBottom(txtDisplay.getLineCount() - 1)
                    - txtDisplay.getScrollY() - txtDisplay.getHeight();
            if(scrollDelta > 0)
                txtDisplay.scrollBy(0, scrollDelta);
        }
    }



    private void logMsg(String msg) {
        Log.d(TAG,msg);
    }
}