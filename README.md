# Android-Bluetooth-Library
## Introduction
This is a Android bluetooth client server library for communication any device via bluetooth with Serial Port Profile (SPP). This library allows you to easily create a socket bluetooth connection for two devices with one server and one client. **The main purpose of this library is you can establish connection in one class or Activity or Fragment and can send or receive data in multiple classes / Activities / Fragments.** This library is compatible with the Android SDK 4.4 W to 11.


## Features
**Bluetooth** class provides the following features :
- Turn on/ off bluetooth.
- Detect nearby bluetooth devices. 
- Get bluetooth discovery started or finished.
- Pair bluetooth device and list of paired devices.
- Unpair bluetooth device ( using reflaction ).

**Connection** class provides the following features :
- Set your own UUID for connection.
- Accept incoming bluetooth connection request.
- Connect bluetooth device.
- Received data from connected device.
- Send data to connected device.
- Check connection status.
- Disconnect bluetooth connection.

**SendReceive** class provides the following features:
- Send data to connected device ( using one or more classes / Activites / Fragments ).
- Receive data from connected device ( using one or more classes / Activities / Fragments ).


## Quick start
Add JitPack to your root build.gradle at the end of repositories:
```javascript
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency in your module build.gradle:
```javascript
dependencies {
  implementation 'com.github.prasad-psp:Android-Bluetooth-Library:1.0.1'
}
```

### Bluetooth
Add this in your manifest file
```javascript
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

> For Android 6.0 and upper you will need  'ACCESS_FINE_LOCATION' permissions to scan bluetooth devices
#### Turn on/off bluetooth
```javascript
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
