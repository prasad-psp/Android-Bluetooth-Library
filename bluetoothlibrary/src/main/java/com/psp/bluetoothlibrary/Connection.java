package com.psp.bluetoothlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import java.io.IOException;
import java.util.UUID;

/**
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
public class Connection {

    private UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Default UUID

    private ConnectThread connectThread = null; //Connect Thread
    private AcceptThread acceptThread = null;  // Accept Thread

    private BluetoothListener.onConnectionListener connectionListener = null; // Connection listener

    private final Context context;

    // This boolean value is used for check broadcast receiver is register or not
    private boolean isRegister = false;

    //This boolean value is used for check device is connected or not
    private boolean isConnected = false;

    // This boolean value is used for check bluetooth connect timeout is enabled or not
    private boolean isEnabledConnectTimeout = false;

    // Connection Results Constant Values
    public static final int CONNECTING = 101; // Connect Thread
    public static final int CONNECTED = 102;  // Connect Thread & Accept Thread
    public static final int DISCONNECTED = 103; // Connect Thread & Accept Thread
    public static final int START_LISTENING = 104; // Accept Thread

    // Connect Thread Connection Error Results Constant Values
    public static final int CONNECT_FAILED = 201;
    public static final int SOCKET_NOT_FOUND = 202;

    // Accept Thread Connection Error Results Constant Values
    public static final int ACCEPT_FAILED = 301;
    public static final int SERVER_SOCKET_NOT_FOUND = 302;

    // 35 sec Default bluetooth connect timeout
    private long connectTimeout = 35*1000;


    /**
     * Init Connection object
     * @param context the current context you use
     */
    public Connection(Context context) {
        this.context = context;
    }

    /**
     * Set your own uuid for connection.
     * Note: this method to be used before connect method.
     * @param uuid UUID to be set
     */
    public void setUUID(UUID uuid) {
        if(uuid != null) {
            String[] components = uuid.toString().split("-");
            if (components.length != 5) {
                throw new IllegalArgumentException("Invalid UUID : " + uuid);
            }
            else {
                BTMODULEUUID = uuid;
            }
        }
    }

    public void enableConnectTimeout() {
        isEnabledConnectTimeout = true;
    }

    public void disableConnectTimeout() {
        isEnabledConnectTimeout = false;
    }

    public boolean isEnabledConnectTimeout() {
        return isEnabledConnectTimeout;
    }

    public void setConnectTimeout(long timeoutMillis) {
        this.connectTimeout = timeoutMillis;
    }

    public long getConnectTimeout() {
        return this.connectTimeout;
    }

    /**
     *  connect method is used to connect bluetooth device using its address.
     *	Note : this method doesn't response to either bluetooth CONNECTED or CONNECT_FAILED so uptil that dont interrupt with this method.
     *  because bluetoothSocket.connect() - This call blocks until it succeeds or throws an exception.
     *  @param deviceAddress - Bluetooth device mac address
     *  @param isSecureConnection - true if you want data encrypted
     *  @param connectionListener - Connection listener, you can check all the bluetooth connection state with this listener
     *  @param receiveListener - Receive listener, you can read data with this listener
     *  @return true if connect method run successfully
     */
    public boolean connect(String deviceAddress,boolean isSecureConnection, BluetoothListener.onConnectionListener connectionListener,
                           BluetoothListener.onReceiveListener receiveListener) {
        boolean isSuccess = false;
        if(acceptThread == null && connectThread == null) {
            // initialize bluetooth connection listener for receiving bluetooth connection state
            this.connectionListener = connectionListener;
            this.connectionListener.onConnectionStateChanged(null, CONNECTING);

            // initialize connect thread and start thread
            connectThread = new ConnectThread(deviceAddress,isSecureConnection,this.connectionListener, receiveListener);
            connectThread.start();
            isSuccess = true;
        }
        return isSuccess;
    }

    /**
     *  connect method is used to connect bluetooth device.
     *  Note : this method doesn't response to either bluetooth CONNECTED or CONNECT_FAILED so uptil that dont interrupt with this method.
     *  because bluetoothSocket.connect() - This call blocks until it succeeds or throws an exception.
     *  @param device - Bluetooth device
     *  @param isSecureConnection - true if you want data encrypted
     *  @param connectionListener - Connection listener, you can check all the bluetooth connection state with this listener
     *  @param receiveListener - Receive listener, you can read data with this listener
     *  @return true if connect method run successfully
     */
    public boolean connect(BluetoothDevice device, boolean isSecureConnection, BluetoothListener.onConnectionListener connectionListener,
                           BluetoothListener.onReceiveListener receiveListener) {
        if(device != null) {
            return this.connect(device.getAddress(), isSecureConnection, connectionListener, receiveListener);
        }
        return false;
    }


    /**
     * accept method is used to accept incoming bluetooth connection request.
     * @param isSecureConnection true if you want data encrypted
     * @param connectionListener Connection listener, you can check all the bluetooth connection state with this listener
     * @param receiveListener Receive listener, you can read data with this listener
     * @return true if accept method run successfully
     */
    public boolean accept(boolean isSecureConnection, BluetoothListener.onConnectionListener connectionListener,
                          BluetoothListener.onReceiveListener receiveListener) {
        boolean isSuccess = false;
        if(connectThread == null && acceptThread == null) {
            // Initialize bluetooth connection listener for receiving bluetooth connection state
            this.connectionListener = connectionListener;
            this.connectionListener.onConnectionStateChanged(null, START_LISTENING);

            // Initialize accept thread and start thread
            acceptThread = new AcceptThread(isSecureConnection,this.connectionListener, receiveListener);
            acceptThread.start();
            isSuccess = true;
        }
        return isSuccess;
    }

    /**
     * Disconnect bluetooth connection.
     */
    public void disconnect() {
        // unregister broadcast receiver
        unRegisterBroadcastReceiver();

        // remove bluetooth connection listener
        removeConnectionListener();

        // cancel connect thread
        if(connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        //cancel accept thread
        if(acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
    }

    /**
     * isConnected method returns device connection status which is either true or false.
     * @return true if connection is established
     */
    public boolean isConnected() {
        if(connectThread != null || acceptThread != null) {
            return isConnected;
        }
        return false;
    }

    /**
     * Get bluetooth socket
     * @return BluetoothSocket
     */
    public BluetoothSocket getBluetoothSocket() {
        if(connectThread != null) {
            return connectThread.getBluetoothSocket();
        }

        if(acceptThread != null) {
            return acceptThread.getBluetoothSocket();
        }
        return null;
    }

    /**
     * Sends data in String format message to connected device.
     * @param data String to be send
     * @return true if data send successfully
     */
    public boolean send(String data) {
        return SendReceive.getInstance().send(data);
    }

    /**
     * Sends byte array to connected device.
     * @param b byte array to be send
     * @return true if data send successfully
     */
    public boolean send(byte[] b) {
        return SendReceive.getInstance().send(b);
    }

    /**
     * Sends byte array , int offset and int length to connected device.
     * @param b byte array to be send
     * @param off int offset to be send
     * @param len int length to be send
     * @return true if data send successfully
     */
    public boolean send(byte[] b,int off,int len) {
        return SendReceive.getInstance().send(b,off,len);
    }

    /**
     * Set receive listener,you can read data with this listener.
     * @param receiveListener BluetoothListener.onReceivedListener
     */
    public void setOnReceiveListener(BluetoothListener.onReceiveListener receiveListener) {
        SendReceive.getInstance().setOnReceiveListener(receiveListener);
    }

    // register broadcast receiver for bluetooth disconnected
    private void registerBroadcastReceiver() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (context != null && !isRegister) {
                    IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                    context.registerReceiver(myReceiver, intentFilter);
                    isRegister = true;
                }
            }
        });
    }

    // unregister broadcast receiver
    private void unRegisterBroadcastReceiver() {
        if(context != null && isRegister) {
            context.unregisterReceiver(myReceiver);
            isRegister = false;
        }
    }

    // Broadcast Receiver class is used for detect bluetooth device disconnected
    private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action != null && !action.equals("") && action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                if(connectionListener != null) {
                    connectionListener.onConnectionStateChanged(null,DISCONNECTED); // Send data to listener
                }
            }
        }
    };

    // remove connection listener
    private void removeConnectionListener() {
        if(connectionListener != null) {
            connectionListener = null;
        }
    }


    // Thread methods
    // Send data to connection state changed listener
    private void setConnectionStateChangedListenerResult(BluetoothListener.onConnectionListener connectionListenerT,
                                                         BluetoothSocket socket, int state) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(connectionListenerT != null) {
                    connectionListenerT.onConnectionStateChanged(socket,state);
                }
            }
        });

    }

    // Send data to connection failed listener
    private void setConnectionFailedListenerResult(BluetoothListener.onConnectionListener connectionListenerT, int errorCode) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(connectionListenerT != null) {
                    connectionListenerT.onConnectionFailed(errorCode);
                }
            }
        });
    }




    // Connection Thread Class
    private class ConnectThread extends Thread {

        private BluetoothSocket mSocket = null;
        private BluetoothAdapter btAdapter = null;
        private BluetoothListener.onConnectionListener connectionListenerT = null;
        private BluetoothListener.onReceiveListener receiveListenerT = null;
        private final Handler timeoutHandler = new Handler(Looper.getMainLooper()); // connection timeout handler

        public ConnectThread(String deviceAddress, boolean isSecureConnection, BluetoothListener.onConnectionListener connectionListenerT,
                             BluetoothListener.onReceiveListener receiveListenerT) {
            this.connectionListenerT = connectionListenerT; // initialize bluetooth connection listener
            this.receiveListenerT = receiveListenerT; // initialize bluetooth received listener
            btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth default Adapter
            mSocket = createBluetoothSocket(deviceAddress,isSecureConnection); // create bluetooth socket
        }

        private BluetoothDevice getRemoteDevice(String deviceAddress) {
            try {
                return btAdapter.getRemoteDevice(deviceAddress);
            }
            catch (Exception e) {
                return null;
            }
        }

        // This method is used to create bluetooth socket using device mac address
        private BluetoothSocket createBluetoothSocket(String deviceAddress,boolean isSecureConnection) {
            BluetoothSocket socket = null;
            try {
                if(btAdapter != null) {
                    BluetoothDevice device = getRemoteDevice(deviceAddress); // get bluetooth device
                    if(device != null) {
                        if(isSecureConnection) {
                            // create secure bluetooth socket
                            socket = device.createRfcommSocketToServiceRecord(BTMODULEUUID);
                        }
                        else {
                            // create insecure bluetooth socket
                            socket = device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
                        }
                    }
                }
            }
            catch (Exception e) {
                socket = null;
                e.printStackTrace();
            }
            return socket;
        }

        @Override
        public void run() {
            if (mSocket != null) {
                try {
                    // Cancel discovery because it otherwise slows down the connection.
                    btAdapter.cancelDiscovery();

                    addConnectionTimeout();

                    // Connect to the remote device through the socket.
                    // This call blocks until it succeeds or throws an exception.
                    mSocket.connect();

                    removeConnectionTimeout();

                    // The connection attempt succeeded.
                    isConnected = true;
                    // the connection in a separate thread.
                    //start send receive
                    SendReceive.getInstance().start(mSocket, this.receiveListenerT);
                    registerBroadcastReceiver(); // register broadcast receiver for bluetooth disconnected
                    setConnectionStateChangedListenerResult(this.connectionListenerT, mSocket, CONNECTED); // send to connection listener [CONNECTED]
                }
                catch (Exception e) {
                    // Unable to connect; close the socket and return.
                    closeSocket();
                    // if socket throws an exception then message will be send to connection failed listener [CONNECT_FAILED]
                    setConnectionFailedListenerResult(this.connectionListenerT, CONNECT_FAILED);
                    isConnected = false;
                    removeConnectionTimeout();
                }
            }
            else {
                // if socket not found then message will be send to connection failed listener [SOCKET_NOT_FOUND]
                setConnectionFailedListenerResult(this.connectionListenerT,SOCKET_NOT_FOUND);
            }
        }

        // Get bluetooth socket
        public BluetoothSocket getBluetoothSocket() {
            return mSocket;
        }

        private void closeSocket() {
            try {
                if(mSocket != null) {
                    // close bluetooth socket
                    mSocket.close();
                    mSocket = null;
                }
            }
            catch (Exception e) {}
        }


        private void deAttachListener() {
            if(this.connectionListenerT != null) {
                // deAttach connectin listener
                this.connectionListenerT = null;
            }

            if(this.receiveListenerT != null ) {
                // deAttach receive listener
                this.receiveListenerT = null;
            }
        }

        // This method is used to stop bluetooth connection
        private void cancel() {
            removeConnectionTimeout();
            deAttachListener();
            SendReceive.getInstance().stop(); // stop send receive
            closeSocket();
            isConnected = false;
        }

        private void addConnectionTimeout() {
            if(isEnabledConnectTimeout) {
                timeoutHandler.postDelayed(timeoutRunnable, connectTimeout);
            }
        }

        private void removeConnectionTimeout() {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }

        private final Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if(mSocket != null) {
                    if(!mSocket.isConnected()) {
                        try {
                            mSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }



    // Accept Thread Class
    private class AcceptThread extends Thread {

        private final String APP_NAME = "BluetoothEasyToUse";

        private final BluetoothServerSocket serverSocket; // Bluetooth server socket

        private BluetoothSocket socket = null; // Bluetooth socket

        //Connection listener and Received listener
        private BluetoothListener.onConnectionListener connectionListenerT = null;
        private BluetoothListener.onReceiveListener receiveListenerT = null;


        public AcceptThread(boolean isSecureConnection, BluetoothListener.onConnectionListener connectionListenerT,
                            BluetoothListener.onReceiveListener receiveListenerT) {
            this.connectionListenerT = connectionListenerT;
            this.receiveListenerT = receiveListenerT;
            serverSocket = listenServerSocket(isSecureConnection); // Get bluetooth server socket and listning incoming connection
        }

        // Listen bluetooth server socket
        private BluetoothServerSocket listenServerSocket(boolean isSecureConnection) {
            BluetoothServerSocket tmpSocket = null;
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

            if(adapter != null) {
                try {
                    if(isSecureConnection) {
                        // listen secure server socket
                        tmpSocket = adapter.listenUsingRfcommWithServiceRecord(APP_NAME, BTMODULEUUID);
                    }
                    else {
                        // listen insecure server socket
                        tmpSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, BTMODULEUUID);
                    }
                }
                catch (Exception e) {
                    tmpSocket = null;
                    e.printStackTrace();
                }
            }
            return tmpSocket;
        }

        @Override
        public void run() {
            try {
                // Keep listening until exception occurs or a socket is returned.
                if(serverSocket != null) {
                    while (true) {
                        try {
                            socket = serverSocket.accept();
                        }
                        catch (IOException e) {
                            closeServerSocket();
                            setConnectionFailedListenerResult(this.connectionListenerT, ACCEPT_FAILED); // send to connection listener [ACCEPT_FAILED]
                            isConnected = false;
                            break;
                        }
                        // If a connection was accepted
                        if (socket != null) {
                            // A connection was accepted.
                            // Do work to manage the connection (in a separate thread)
                            isConnected = true;
                            registerBroadcastReceiver(); // register broadcast for bluetooth disconnected
                            setConnectionStateChangedListenerResult(this.connectionListenerT,socket,CONNECTED); // send to connection listener [CONNECTED]
                            SendReceive.getInstance().start(socket,this.receiveListenerT); // start send and receive
                            closeServerSocket(); // close server socket
                            break;
                        }
                    }
                }
                else {
                    // if server socket not found then message will be send to connection failed listener [SERVER_SOCKET_NOT_FOUND]
                    setConnectionFailedListenerResult(this.connectionListenerT,SERVER_SOCKET_NOT_FOUND);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Get bluetooth socket
        public BluetoothSocket getBluetoothSocket() {
            return socket;
        }

        private void closeServerSocket() {
            if(serverSocket != null) {
                try {
                    // close bluetooth server socket
                    serverSocket.close();
                }
                catch (IOException e) { }
            }
        }

        private void closeSocket() {
            try {
                if(socket != null) {
                    // close bluetooth socket
                    socket.close();
                    socket = null;
                }
            }
            catch (Exception e) { }
        }

        private void deAttachListener() {
            if(this.connectionListenerT != null) {
                // deAttach connection listener
                this.connectionListenerT = null;
            }

            if(this.receiveListenerT != null ) {
                // deAttach receive listener
                this.receiveListenerT = null;
            }
        }

        // This method is used to stop bluetooth connection
        private void cancel() {
            deAttachListener();
            SendReceive.getInstance().stop();  // stop send receive
            closeServerSocket();
            closeSocket();
            isConnected = false;
        }
    }
}
