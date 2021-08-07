package com.psp.bluetoothlibrary;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * Author: prasad-psp
 */
public interface BluetoothListener {

    /**
     * You can check all the bluetooth connection state with this listener.
     */
    interface onConnectionListener {
        void onConnectionStateChanged(BluetoothSocket socket, int state);
        void onConnectionFailed(int errorCode);
    }

    /**
     * You can read data with this listener.
     */
    interface onReceiveListener {
        void onReceived(String receivedData);
    }


    /**
     * You can detect nearby devices with this listener.
     */
    interface onDetectNearbyDeviceListener {
        void onDeviceDetected(BluetoothDevice device);
    }

    /**
     * You can get paired devices with this listener.
     */
    interface onDevicePairListener {
        void onDevicePaired(BluetoothDevice device);
        void onCancelled(BluetoothDevice device);
    }

    /**
     * You can get bluetooth discovery started or finished with this listener.
     */
    interface onDiscoveryStateChangedListener {
        void onDiscoveryStateChanged(int state);
    }
}
