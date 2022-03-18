# Android-Bluetooth-Library 
[![Licence MIT](https://img.shields.io/badge/licence-MIT-blue.svg)](https://github.com/prasad-psp/Android-Bluetooth-Library/blob/master/LICENSE) [![](https://jitpack.io/v/prasad-psp/Android-Bluetooth-Library.svg)](https://jitpack.io/#prasad-psp/Android-Bluetooth-Library) 

This is an Android bluetooth client server library for communication any device via bluetooth with Serial Port Profile (SPP). This library allows you to easily create a socket bluetooth connection for two devices with one server and one client. **The main purpose of this library is you can establish connection in one Class or Activity or Fragment and can send or receive data in multiple Classes / Activities / Fragments.** This library is compatible with the Android SDK 4.4 W to up.

  
## Features
[**Support Android 12**](#Permissions)

[**Bluetooth**](#bluetooth) class provides the following features :
- Turn on/ off bluetooth.
- Detect nearby bluetooth devices. 
- Get bluetooth discovery started or finished.
- Pair bluetooth device and list of paired devices.
- Unpair bluetooth device (using reflection).

[**Connection**](#connection) class provides the following features :
- Set your own UUID for connection.
- Accept incoming bluetooth connection request.
- Connect bluetooth device.
- Received data from connected device.
- Send data to connected device.
- Check connection status.
- Disconnect bluetooth connection.

[**SendReceive**](#send-receive) class provides the following features:
- Send data to connected device (using one or more Classes / Activities / Fragments).
- Receive data from connected device (using one or more Classes / Activities / Fragments).


### Version 0.2
New feature added in Connection class
- Connect timeout

#### Enable and disable connect timeout
```java
// Enable
connection.enableConnectTimeout();
// Disable
connection.disableConnectTimeout();
```

#### Set your own connect timeout (Optional)
> Default connect timeout is 35 sec.
```java
connection.setConnectTimeout(timeoutMillis);
```

## Quick start
Add JitPack to your root build.gradle at the end of repositories:
```java
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency in your module build.gradle:
```java
dependencies {
  implementation 'com.github.prasad-psp:Android-Bluetooth-Library:1.0.2'
}
```

### Permissions
> Target Android 12 or higher
```xml
<uses-permission
        android:name="android.permission.BLUETOOTH"
        android:maxSdkVersion="30" />
<uses-permission
        android:name="android.permission.BLUETOOTH_ADMIN"
        android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"
        android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"
        android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
        android:usesPermissionFlags="neverForLocation"
        tools:targetApi="s" />
```

> Target Android 11 or lower
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```
Read more [Android Bluetooth Permissions](https://developer.android.com/guide/topics/connectivity/bluetooth/permissions).

### Bluetooth
#### Turn on/off bluetooth
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

 	// Initialize bluetooth 
	bluetooth = new Bluetooth(this);
	
	// Turn on bluetooth with user permission 
	/* if you want to check user allow or denied bluetooth turn on 	
       	request add override method onActivityResult and request code is Bluetooth.BLUETOOTH_ENABLE_REQUEST */
	bluetooth.turnOnWithPermission(this);

	// Turn on bluetooth without user permission
	// bluetooth.turnOnWithoutPermission();

	// Turn off bluetooth
	bluetooth.turnOff();
}
```

#### Stop bluetooth service
```java
@Override
protected void onStop() {
    bluetooth.onStop();
}
```

#### Detect nearby bluetooth devices
```java
// Listener
bluetooth.setOnDetectNearbyDeviceListener(new BluetoothListener.onDetectNearbyDeviceListener() {
    @Override
    public void onDeviceDetected(BluetoothDevice device) {
        // Device found
	 Log.d(TAG, "Device found "+device.getName());
    }
});

bluetooth.startDetectNearbyDevices();
```

#### Bluetooth device discovery 
```java
// Listener
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
```

#### Pair bluetooth device and List of paired devices
```java
// Listener
bluetooth.setOnDevicePairListener(new BluetoothListener.onDevicePairListener() {
    @Override
    public void onDevicePaired(BluetoothDevice device) {
	// Paired successful
        Log.d(TAG,device.getName()+" Paired successful");
    }

    @Override
    public void onCancelled(BluetoothDevice device) {
	// Pairing failed
        Log.d(TAG,device.getName()+" Pairing failed");
    }
});

// bluetooth.requestPairDevice(bluetoothDevice);
// OR
if(bluetooth.requestPairDevice(deviceAddress)) {
     // Pair request send successfully
     Log.d(TAG,"Pair request send successfully");
}

// List of paired devices
ArrayList<BluetoothDevice> devices = bluetooth.getPairedDevices();
```

#### Unpair bluetooth device (using reflaction)
```java
// bluetooth.unpairDevice(bluetoothDevice);
// OR
if(bluetooth.unpairDevice(deviceAddress)) {
    Log.d(TAG,"Unpair successfully");
}
else {
    Log.d(TAG,"Unpair failed");
}
```


### Connection
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState); 
	// Initialize connection 
    	connection = new Connection(this);
}
```
#### Set your own UUID for connection (Optional)
> Default UUID is 00001101-0000-1000-8000-00805F9B34FB
```java
connection.setUUID(your_uuid);
```

### As Server
#### Accept incoming bluetooth connection request
```java
connection.accept(isSecureConnection,connectionListener, receiveListener);

// Connection Listener
private final BluetoothListener.onConnectionListener connectionListener = new BluetoothListener.onConnectionListener() {
    @Override
    public void onConnectionStateChanged(BluetoothSocket socket, int state) {
        switch (state) {
            case Connection.START_LISTENING: {
                Log.d(TAG,"Start Listening..."); 
                break;
            }

            case Connection.CONNECTED: {
                Log.d(TAG,"Connected");
                break;
            }

            case Connection.DISCONNECTED: {
                Log.d(TAG,"Disconnected");
		 // make sure call after detect bluetooth device disconnected   
		 connection.disconnect();
                break;
            }
        }
    }

    @Override
    public void onConnectionFailed(int errorCode) {
        switch (errorCode) {
            case Connection.SERVER_SOCKET_NOT_FOUND: {
                Log.d(TAG,"Server socket not found");
 		break;
            }

            case Connection.ACCEPT_FAILED: {
                Log.d(TAG,"Accept failed");
                break;
            }
        }

	// make sure call after detect onConnectionFailed
        connection.disconnect();
    }
};
```

### As Client
#### Connect bluetooth device
```java
// connection.connect(bluetoothDevice, isSecureConnection,connectionListener,receiveListener);
// OR
connection.connect(deviceAddress,isSecureConnection,connectionListener,receiveListener);

// Connection Listener
private final BluetoothListener.onConnectionListener connectionListener = new BluetoothListener.onConnectionListener() {
    @Override
    public void onConnectionStateChanged(BluetoothSocket socket, int state) {
        switch (state) {
            case Connection.CONNECTING: {
                Log.d(TAG,"Connecting..."); 
                break;
            }
	     case Connection.CONNECTED: {
                Log.d(TAG,"Connected");
                break;
            }
	     case Connection.DISCONNECTED: {
                Log.d(TAG,"Disconnected");
	        // make sure call after detect bluetooth device disconnected
		connection.disconnect();
	       break;
            }
        }
    }

    @Override
    public void onConnectionFailed(int errorCode) {
        switch (errorCode) {
            case Connection.SOCKET_NOT_FOUND: {
                Log.d(TAG,"Socket not found");
                break;
            }
	     case Connection.CONNECT_FAILED: {
                Log.d(TAG,"Connect Failed");
                break;
            }
        }
	// make sure call after detect onConnectionFailed
       connection.disconnect();
    }
};
```

#### Receive data from connected device
```java
// Receive listener
private final BluetoothListener.onReceiveListener receiveListener = new BluetoothListener.onReceiveListener() {
    @Override
    public void onReceived(String receivedData) {
        Log.d(TAG,receivedData); 
    }
};

// Optional this will work when you will deal with multiple activities or fragments or classes
//connection.setOnReceiveListener(receiveListener);
```

#### Send data to connected device
```java
// connection.send(b);  // byte[] b
// connection.send(b,off,len);   //byte[] b , int off, int len
// OR
if(connection.send(data)) {
    Log.d(TAG,"Send successful");
}
else {
    Log.d(TAG,"Sending failed");
}
```

#### Check connection status
```java
if(connection.isConnected()) {
    Log.d(TAG,"Device connected");
}
else {
    Log.d(TAG,"Device disconnected");
}
```

#### Disconnect bluetooth connection 
```java
connection.disconnect();
```


### Send Receive 
> Note: Call SendReceive class when connection is established.
#### Send data to connected device (using one or more Classes / Activities / Fragments)
```java
// SendReceive.getInstance().send(b);  //byte[] b
// SendReceive.getInstance().send(b,off,len);  // byte[] b , int off, int len
// OR
if(SendReceive.getInstance().send(data)) {
    Log.d(TAG,"Send successful");
}
else {
    Log.d(TAG,"Sending failed");
}
```

#### Receive data from connected device (using one or more Classes / Activities / Fragments)
```java
// Receive listener
SendReceive.getInstance().setOnReceiveListener(new BluetoothListener.onReceiveListener() {
    @Override
    public void onReceived(String receivedData) {
        Log.d(TAG, receivedData);
    }
});
```

#### Complete example
See the [sample project.](https://github.com/prasad-psp/Android-Bluetooth-Library/tree/master/app/src/main/java/com/psp/android_bluetooth_library)

## Licence
```
MIT License
*
* Copyright (c) 2021 Prasad Parshram
```
See the full [licence file.]( https://github.com/prasad-psp/Android-Bluetooth-Library/blob/master/LICENSE)


## Feedback
If you have any feedback, please reach out to us at prasad.parshram123@gmail.com
