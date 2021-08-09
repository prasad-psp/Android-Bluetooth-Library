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


