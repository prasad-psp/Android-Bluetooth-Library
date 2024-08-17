package com.psp.android_bluetooth_library;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.psp.android_bluetooth_library.utils.HexUtils;
import com.psp.bluetoothlibrary.Bluetooth;
import com.psp.bluetoothlibrary.BluetoothListener;
import com.psp.bluetoothlibrary.Connection;

import java.util.ArrayList;

/**
 * Author: prasad-psp
 */
public class ConnectActivity extends AppCompatActivity {

    private final String TAG = "psp.ConnectAct";

    // UI
    private Button btnConnect,btnDisconnect,btnSend,btnSendReceive;
    private EditText edtMessage;
    private TextView txtDisplay;

    // Connection object
    private Connection connection;

    @Override
    protected void onStart() {
        super.onStart();
        if(connection.isConnected()) {
            logMsg("initialize receive listener");
            connection.setOnReceiveListener(receiveListener);
        }
        logMsg("onStart");
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
        setContentView(R.layout.activity_connect);
        init();

        // initialize connection object
        logMsg("initialize connection object");
        connection = new Connection(this);

        // set UUID ( optional )
        // connection.setUUID(your_uuid);

        // ( optional ) *New feature
//        connection.setConnectTimeout(30*1000); // 30 sec connect timeout
        logMsg("Get connect timeout "+connection.getConnectTimeout());

        // ( optional ) *New feature
//        connection.enableConnectTimeout();
        logMsg("Is enable connect timeout "+connection.isEnabledConnectTimeout());


        // Connect
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceAddressAndConnect();
            }
        });

        // Disconnect
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        // Send Data
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
                    logMsg("[TX] "+msg);
                    txtDisplay.append("\n[TX] Failed "+msg);
                    setDisplayMessageScrollBottom();
                }
            }
        });


        // Send Receive in another activity
        btnSendReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connection.isConnected()) {
                    Intent i = new Intent(ConnectActivity.this,SendReceiveActivity.class);
                    startActivity(i);
                }
                else {
                    Toast.makeText(ConnectActivity.this, "Device not connected", Toast.LENGTH_SHORT).show();
                    logMsg("Device not connected");
                }
            }
        });
    }


    private void init() {
        btnConnect = findViewById(R.id.btnConnectConnect);
        btnDisconnect = findViewById(R.id.btnConnectDisconnect);
        btnSend = findViewById(R.id.btnConnectSend);
        btnSendReceive = findViewById(R.id.btnConnectSendReceiveConnect);
        edtMessage = findViewById(R.id.edtConnectMessage);
        txtDisplay = findViewById(R.id.txtConnectDisplay);

        txtDisplay.setMovementMethod(new ScrollingMovementMethod());
    }

    private void disconnect() {
        if(connection != null) {
            connection.disconnect();
            logMsg("Disconnect manual");
            txtDisplay.append("\n[ST] Disconnect manual");
            setDisplayMessageScrollBottom();
        }
    }

    private void getDeviceAddressAndConnect() {
        // create dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select device");

        ListView modeList = new ListView(this);
        ArrayList<String> listPaired = new ArrayList<>();
        getPairedDevices(listPaired); // get paired devices
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, listPaired);
        modeList.setAdapter(modeAdapter);

        builder.setView(modeList);
        final Dialog dialog = builder.create();
        dialog.show();



        modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String[] device = listPaired.get(position).split("\n");

                // Connect Bluetooth Device --- device[1] = device mac address
                if(connection.connect(device[1],true,connectionListener,receiveListener)) {
                    Log.d(TAG,"Start connection process");
                }
                else {
                    logMsg("Start connection process failed");
                }
                dialog.dismiss();
            }
        });
    }

    private final BluetoothListener.onConnectionListener connectionListener = new BluetoothListener.onConnectionListener() {
        @Override
        public void onConnectionStateChanged(BluetoothSocket socket, int state) {
            switch (state) {
                case Connection.CONNECTING: {
                    logMsg("Connecting...");
                    txtDisplay.append("\n[ST] Connecting...");
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
                case Connection.SOCKET_NOT_FOUND: {
                    logMsg("Socket not found");
                    txtDisplay.append("\n[ST] Socket not found");
                    setDisplayMessageScrollBottom();
                    break;
                }

                case Connection.CONNECT_FAILED: {
                    logMsg("Connect Failed");
                    txtDisplay.append("\n[ST] Connect failed");
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
//            logMsg("[RX] "+receivedData);
//            txtDisplay.append("\n[RX] "+receivedData);
//            setDisplayMessageScrollBottom();
        }

        @Override
        public void onReceived(String receivedData, byte[] receivedDataInBytes) {
            byte[] filteredBytes = HexUtils.filterNonZeroBytes(receivedDataInBytes);
            String displayMsg = HexUtils.convertBytesToFormattedHex(filteredBytes);
            logMsg("[RX] "+displayMsg);
            txtDisplay.append("\n[RX] "+displayMsg);
            setDisplayMessageScrollBottom();

            // OR

//            logMsg("[RX] "+receivedData);
//            txtDisplay.append("\n[RX] "+receivedData);
//            setDisplayMessageScrollBottom();
        }
    };

    private void getPairedDevices(ArrayList<String> list) {
        // initialize bluetooth object
        Bluetooth bluetooth = new Bluetooth(this);

        ArrayList<BluetoothDevice> deviceList = bluetooth.getPairedDevices();
        if(deviceList.size() >0) {
            for(BluetoothDevice device: deviceList) {
                list.add(device.getName()+"\n"+device.getAddress());
                Log.d(TAG,"Paired device is "+device.getName());
            }
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