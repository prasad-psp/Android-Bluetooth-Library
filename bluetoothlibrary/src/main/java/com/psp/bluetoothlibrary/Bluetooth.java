/*
 * MIT License
 *
 * Copyright (c) 2021 Prasad Parshram
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.psp.bluetoothlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

/**
 * Bluetooth class provides the following features :
 * <p>- Turn on/ off bluetooth.
 * <p>- Detect nearby bluetooth devices.
 * <p>- Get bluetooth discovery started or finished.
 * <p>- Pair bluetooth device and list of paired devices.
 * <p>- Unpair bluetooth device (using reflection).
 */
public class Bluetooth {

    private final Context context;

    private static BluetoothAdapter btAdapter = null; // Bluetooth Adapter

    private static boolean isBluetoothSupported = false; // this boolean is check bluetooth is supported or not
    private boolean isRegisterPairBroadcast = false; // this boolean is check  pairing broadcast receiver is register or not
    private boolean isRegisterDiscoveryBroadcast = false; // this boolean is check bluetooth discovery broadcast is register or not

    private final Handler handler; //Handler

    private BluetoothListener.onDetectNearbyDeviceListener onDetectNearbyDeviceListener = null; // detect nearby device listener
    private BluetoothListener.onDevicePairListener onDevicePairListener = null; // device pair listener
    private BluetoothListener.onDiscoveryStateChangedListener onDiscoveryStateChangedListener = null; // discovery state changed listener

    //Constant Values
    public static final int DISCOVERY_STARTED = 113;
    public static final int DISCOVERY_FINISHED = 114;
    public static final int BLUETOOTH_ENABLE_REQUEST = 232;


    /**
     * Init Bluetooth object
     * @param context the current context you use
     */
    public Bluetooth(Context context) {
        this.context = context;
        handler = new Handler(Looper.getMainLooper());
        getAdapter();
    }


    /**
     * To get default bluetooth adapter.
     * @return BluetoothAdapter
     */
    public static BluetoothAdapter getAdapter() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter != null) {
            isBluetoothSupported = true;
            return btAdapter;
        }
        else {
            isBluetoothSupported = false;
            return null;
        }
    }

    /**
     * isBluetoothSupported method is used to check if bluetooth is supported or not.
     * @return true if bluetooth supported
     */
    public static boolean isBluetoothSupported() {
        getAdapter();
        return isBluetoothSupported;
    }

    /**
     * To get bluetooth device using its address.
     * @param address Bluetooth device mac address
     * @return BluetoothDevice
     */
    public static BluetoothDevice getRemoteDevice(String address) {
        BluetoothDevice device = null;
        if(getAdapter() != null) {
            try {
                device = btAdapter.getRemoteDevice(address);
            }
            catch (Exception e) {
                device = null;
            }
        }
        return device;
    }

    /**
     * To get bluetooth device using its address.
     * @param address Bluetooth device mac address
     * @return BluetoothDevice
     */
    public static BluetoothDevice getRemoteDevice(byte[] address) {
        BluetoothDevice device = null;
        if(getAdapter() != null) {
            try {
                device = btAdapter.getRemoteDevice(address);
            }
            catch (Exception e) {
                device = null;
            }
        }
        return device;
    }


    /**
     * Stop bluetooth service
     */
    public void onStop() {
        if(isBluetoothSupported) {
            stopBluetoothDiscovery();  //stop bluetooth discovery
            handler.removeCallbacks(runnable); // remove handler callback
        }
    }

    /**
     * Set detect nearby device listener, you can detect nearby devices with this listener.
     * @param onDetectNearbyDeviceListener BluetoothListener.onDetectNearbyDeviceListener
     */
    public void setOnDetectNearbyDeviceListener(BluetoothListener.onDetectNearbyDeviceListener onDetectNearbyDeviceListener) {
        this.onDetectNearbyDeviceListener = onDetectNearbyDeviceListener;
    }

    /**
     * Set device pair listener,you can get paired device with this listener.
     * @param onDevicePairListener BluetoothListener.onDevicePairListener
     */
    public void setOnDevicePairListener(BluetoothListener.onDevicePairListener onDevicePairListener) {
        this.onDevicePairListener = onDevicePairListener;
    }

    /**
     * Set discovery state changed,you can get bluetooth discovery started or finished with this listener.
     * @param onDiscoveryStateChangedListener BluetoothListener.onDiscoveryStateChangedListener
     */
    public void setOnDiscoveryStateChangedListener(BluetoothListener.onDiscoveryStateChangedListener onDiscoveryStateChangedListener) {
        this.onDiscoveryStateChangedListener = onDiscoveryStateChangedListener;
    }

    /**
     * Enable bluetooth with user permission.
     * @param activity Activity used to display the dialog.
     */
    public void turnOnWithPermission(AppCompatActivity activity) {
        if(btAdapter != null && !btAdapter.isEnabled() && activity != null) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, BLUETOOTH_ENABLE_REQUEST);
        }
    }

    /**
     * Enable bluetooth with user permission.
     * @param activity Fragment activity used to display the dialog.
     */
    public void turnOnWithPermission(FragmentActivity activity) {
        if(btAdapter != null && !btAdapter.isEnabled() && activity != null) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, BLUETOOTH_ENABLE_REQUEST);
        }
    }

    /**
     * Enable bluetooth without user permission.
     */
    public void turnOnWithoutPermission() {
        if(btAdapter != null && !btAdapter.isEnabled()) {
            btAdapter.enable();
        }
    }

    /**
     * Disable bluetooth without user permission.
     */
    public void turnOff() {
        if(btAdapter != null && btAdapter.isEnabled()) {
            btAdapter.disable();
        }
    }

    /**
     * isDiscovering method is used to check bluetooth is discovered or not.
     * @return true if bluetooth device is discovered
     */
    public boolean isDiscovering() {
        if(isBluetoothSupported) {
            return btAdapter.isDiscovering();
        }
        return false;
    }

    /**
     * isOn method is used to check bluetooth is on or off.
     * @return true if bluetooth is on
     */
    public boolean isOn() {
        if(isBluetoothSupported) {
            return btAdapter.isEnabled();
        }
        return false;
    }

    /**
     * Get list of paired devices.
     * @return ArrayList<BluetoothDevice> List of Paired BluetoothDevice.
     */
    public ArrayList<BluetoothDevice> getPairedDevices() {
        ArrayList<BluetoothDevice> deviceList = new ArrayList<>();

        if(isBluetoothSupported) {
            // Paired device
            Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
            if(devices.size() > 0) {
                deviceList.addAll(devices);
            }
        }
        return  deviceList;
    }

    /**
     * Start detect nearby bluetooth devices.
     */
    public void startDetectNearbyDevices() {
        if(isBluetoothSupported) {
            handler.removeCallbacks(runnable);
            stopBluetoothDiscovery();
            handler.postDelayed(runnable,500);
        }
    }

    /**
     * Pair request send to specific bluetooth device using its address.
     * @param device bluetooth device mac address
     * @return true if pair request send successfully
     */
    public boolean requestPairDevice(BluetoothDevice device) {
        boolean isSuccess = false;
        if(isBluetoothSupported && device != null) {
            try {
                isSuccess = device.createBond();
                if(isSuccess) {
                    registerPairingBroadcast(true);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isSuccess;
    }

    /**
     * Pair request send to specific bluetooth device.
     * @param deviceAddress bluetooth Device
     * @return true if pair request send successfully
     */
    public boolean requestPairDevice(String deviceAddress) {
        boolean isSuccess = false;
        if(isBluetoothSupported) {
            BluetoothDevice device = getRemoteDevice(deviceAddress);
            if (device != null) {
                try {
                    isSuccess = device.createBond();
                    if(isSuccess) {
                        registerPairingBroadcast(true);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }

    /**
     * Forget device.
     * @param device bluetooth device
     * @return true if device unpair successfully
     */
    public boolean unpairDevice(BluetoothDevice device) {
        boolean isSuccess = false;
        if(isBluetoothSupported && device != null) {
            try {
                // Bluetooth Remove Bond
                Method m = device.getClass()
                        .getMethod("removeBond", (Class[]) null);
                m.invoke(device, (Object[]) null);
                isSuccess = true;
            }
            catch (Exception e) {
                isSuccess = false;
                e.printStackTrace();
            }
        }
        return isSuccess;
    }

    /**
     * Forget device using its address.
     * @param deviceAddress bluetooth device mac address
     * @return true if device unpair successfully
     */
    public boolean unpairDevice(String deviceAddress) {
        boolean isSuccess = false;
        if(isBluetoothSupported) {
            BluetoothDevice device = getRemoteDevice(deviceAddress);
            if (device != null) {
                try {
                    // Bluetooth Remove Bond
                    Method m = device.getClass()
                            .getMethod("removeBond", (Class[]) null);
                    m.invoke(device, (Object[]) null);
                    isSuccess = true;
                }
                catch (Exception e) {
                    isSuccess = false;
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }


    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            startBluetoothDiscovery();
        }
    };

    // Stop bluetooth discovery
    private void startBluetoothDiscovery(){
        try {
            registerDiscoveryBroadcast(true);
            btAdapter.startDiscovery();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Start bluetooth discovery for detect nearby bluetooth devices
    private void stopBluetoothDiscovery(){
        try {
            if(isDiscovering()) {
                btAdapter.cancelDiscovery();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // register or unregister pairing broadcast receiver
    private void registerPairingBroadcast(boolean isRegister) {
        if(isRegister) {
            if (!isRegisterPairBroadcast) {
                // register
                IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                context.registerReceiver(pairBroadcast, intentFilter);
                isRegisterPairBroadcast = true;
            }
        }
        else {
            if(isRegisterPairBroadcast) {
                //unregister
                context.unregisterReceiver(pairBroadcast);
                isRegisterPairBroadcast = false;
            }
        }
    }

    // register or unregister discovery broadcast receiver
    private void registerDiscoveryBroadcast(boolean isRegister) {
        if(isRegister) {
            if(!isRegisterDiscoveryBroadcast) {
                // register
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
                intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                context.registerReceiver(discoveryBroadcast, intentFilter);
                isRegisterDiscoveryBroadcast = true;
            }
        }
        else {
            if(isRegisterDiscoveryBroadcast) {
                // unregister
                context.unregisterReceiver(discoveryBroadcast);
                isRegisterDiscoveryBroadcast = false;
            }
        }
    }


    // paired device broadcast receiver
    private final BroadcastReceiver pairBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(action != null && !action.equals("") && action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {

                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    registerPairingBroadcast(false);
                    //Device paired successfully
                    if(onDevicePairListener != null) {
                        onDevicePairListener.onDevicePaired(device);
                    }
                }

                if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDING) {
                    registerPairingBroadcast(false);
                    //Device paired cancelled
                    if(onDevicePairListener != null) {
                        onDevicePairListener.onCancelled(device);
                    }
                }
            }
        }
    };


    // discovery device broadcast receiver
    private final BroadcastReceiver discoveryBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(action != null && !action.equals("")) {

                if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                    //Device found
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(device != null && device.getName() != null && device.getAddress() != null) {
                        // Check device is paired or not
                        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                            if(onDetectNearbyDeviceListener != null) {
                                onDetectNearbyDeviceListener.onDeviceDetected(device);
                            }
                        }
                    }
                }

                if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                    //Device discovry started
                    if(onDiscoveryStateChangedListener != null) {
                        onDiscoveryStateChangedListener.onDiscoveryStateChanged(DISCOVERY_STARTED);
                    }
                }

                if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                    registerDiscoveryBroadcast(false);
                    //Device discovery finished
                    if(onDiscoveryStateChangedListener != null) {
                        onDiscoveryStateChangedListener.onDiscoveryStateChanged(DISCOVERY_FINISHED);
                    }
                }
            }
        }
    };
}
