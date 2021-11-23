package com.psp.android_bluetooth_library;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.psp.bluetoothlibrary.Bluetooth;
import com.psp.bluetoothlibrary.BluetoothListener;

import java.util.ArrayList;

/**
 * Author: prasad-psp
 */
public class BluetoothActivity extends AppCompatActivity {

    private static final String TAG = "psp.BluetoothAct";

    // UI
    Button btnTurnOn,btnTurnOff,btnScan;
    ListView listViewPairedDevices, listViewDetectDevices;

    // List For paired devices and detect devices
    ArrayList<String> listDetectDevicesString, listPairedDevicesString;
    ArrayList<BluetoothDevice> listDetectBluetoothDevices,listPairedBluetoothDevices;
    ArrayAdapter<String> adapterDetectBluetoothDevices,adapterPairedBluetoothDevices;

    // Bluetooth object
    private Bluetooth bluetooth;


    // optional
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Bluetooth.BLUETOOTH_ENABLE_REQUEST) {
            if(resultCode == RESULT_OK) {
                Log.d(TAG,"Bluetooth on");
            }

            if(resultCode == RESULT_CANCELED) {
                Log.d(TAG,"Bluetooth turn on dialog canceled");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        init();

        // Request fine location permission
        checkRunTimePermission();

        /*
        <uses-permission android:name="android.permission.BLUETOOTH" />
        <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

        - add this in your manifests file
         */

        // initialize bluetooth
        bluetooth = new Bluetooth(this);

        // check bluetooth is supported or not
        Log.d(TAG,"Bluetooth is supported "+Bluetooth.isBluetoothSupported());
        Log.d(TAG,"Bluetooth is on "+bluetooth.isOn());
        Log.d(TAG,"Bluetooth is discovering "+bluetooth.isDiscovering());

        // turn on bluetooth
        btnTurnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // With user permission
                bluetooth.turnOnWithPermission(BluetoothActivity.this);
                // Without user permission
                // bluetooth.turnOnWithoutPermission();
            }
        });

        // turn off bluetooth
        btnTurnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth.turnOff(); // turn off
            }
        });


        // Bluetooth discovery #START
        bluetooth.setOnDiscoveryStateChangedListener(new BluetoothListener.onDiscoveryStateChangedListener() {
            @Override
            public void onDiscoveryStateChanged(int state) {
                if(state == Bluetooth.DISCOVERY_STARTED) {
                    Log.d(TAG,"Discovery started");
                }

                if(state == Bluetooth.DISCOVERY_FINISHED) {
                    Log.d(TAG,"Discovery finished");
                }
            }
        });
        // Bluetooth discovery #END

        // Detect nearby bluetooth devices #START
        bluetooth.setOnDetectNearbyDeviceListener(new BluetoothListener.onDetectNearbyDeviceListener() {
            @Override
            public void onDeviceDetected(BluetoothDevice device) {
                // check device is already in list or not
                if(!listDetectDevicesString.contains(device.getName())) {
                    Log.d(TAG, "Bluetooth device found " + device.getName());
                    listDetectDevicesString.add(device.getName()); // add to list
                    listDetectBluetoothDevices.add(device);
                    adapterDetectBluetoothDevices.notifyDataSetChanged();
                }
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // clear all devices list
                clearDetectDeviceList();
                // scan nearby bluetooth devices
                bluetooth.startDetectNearbyDevices();
            }
        });
        // Detect nearby bluetooth devices #END

        // Bluetooth Pairing #START
        bluetooth.setOnDevicePairListener(new BluetoothListener.onDevicePairListener() {
            @Override
            public void onDevicePaired(BluetoothDevice device) {
                Log.d(TAG,device.getName()+" Paired successfull");
                Toast.makeText(BluetoothActivity.this, device.getName()+" Paired successfull", Toast.LENGTH_SHORT).show();

                // remove device from detect device list
                listDetectDevicesString.remove(device.getName());
                listDetectBluetoothDevices.remove(device);
                adapterDetectBluetoothDevices.notifyDataSetChanged();

                // add device to paired device list
                listPairedDevicesString.add(device.getName());
                listPairedBluetoothDevices.add(device);
                adapterPairedBluetoothDevices.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(BluetoothDevice device) {
                Toast.makeText(BluetoothActivity.this, device.getName()+" Paired failed", Toast.LENGTH_SHORT).show();
            }
        });

        listViewDetectDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(bluetooth.requestPairDevice(listDetectBluetoothDevices.get(position))) {
                    Log.d(TAG,"Pair request send successfully");
                    Toast.makeText(BluetoothActivity.this, "Pair request send successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Bluetooth Pairing #END


        // Get Paired devices list
        getPairedDevices();


        // Unpair bluetooh device #START
        listViewPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(bluetooth.unpairDevice(listPairedBluetoothDevices.get(position))) {
                    Log.d(TAG,"Unpair successfully");

                    listPairedDevicesString.remove(position);
                    listPairedBluetoothDevices.remove(position);
                    adapterPairedBluetoothDevices.notifyDataSetChanged();
                }
                else {
                    Log.d(TAG,"Unpair failed");
                }
            }
        });
        // Unpair bluetooh device #END


    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetooth.onStop();
        Log.d(TAG,"OnStop");
    }

    private void init() {
        btnTurnOn = findViewById(R.id.btnTurnOn);
        btnTurnOff = findViewById(R.id.btnTurnOff);
        btnScan = findViewById(R.id.btnScan);
        listViewPairedDevices = findViewById(R.id.listViewPairedDevice);
        listViewDetectDevices = findViewById(R.id.listViewDetectDevice);

        listDetectDevicesString = new ArrayList<>();
        listPairedDevicesString = new ArrayList<>();

        listDetectBluetoothDevices = new ArrayList<>();
        listPairedBluetoothDevices = new ArrayList<>();

        adapterDetectBluetoothDevices = new ArrayAdapter<String>(this, R.layout.device_item, listDetectDevicesString);
        adapterPairedBluetoothDevices = new ArrayAdapter<>(this,R.layout.device_item, listPairedDevicesString);

        listViewDetectDevices.setAdapter(adapterDetectBluetoothDevices);
        listViewPairedDevices.setAdapter(adapterPairedBluetoothDevices);
    }

    private void getPairedDevices() {
        ArrayList<BluetoothDevice> devices = bluetooth.getPairedDevices();

        if(devices.size() > 0) {
            for(BluetoothDevice device: devices) {
                listPairedDevicesString.add(device.getName());
                listPairedBluetoothDevices.add(device);
                Log.d(TAG,"Paired device is "+device.getName());
            }
        }
        else {
            Log.d(TAG,"Paired device list not found");
        }
    }

    public void checkRunTimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"Fine location permission is already granted");
            } else {
                Log.d(TAG,"request fine location permission");
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        10);
            }
        }
    }

    private void clearDetectDeviceList() {
        if(listDetectDevicesString.size() > 0) {
            listDetectDevicesString.clear();
        }

        if(listDetectBluetoothDevices.size() > 0) {
            listDetectBluetoothDevices.clear();
        }
        adapterDetectBluetoothDevices.notifyDataSetChanged();
    }
}