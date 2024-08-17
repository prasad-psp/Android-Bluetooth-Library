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

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * SendReceive class provides the following features:
 * <p>- Send data to connected device (using one or more Classes / Activities / Fragments).
 * <p>- Receive data from connected device (using one or more Classes / Activities / Fragments).
 */
public class SendReceive {

    private static SendReceive instance; // SendReceive Static variable
    private SendReceiveThread sendReceiveThread; // SendReceived Thread


    // Static method is used to initialize this class at one time
    public static synchronized SendReceive getInstance() {
        if(instance == null) {
            instance = new SendReceive();
        }
        return instance;
    }

    /*package*/ // start sendReceive thread
    protected void start(BluetoothSocket socket, BluetoothListener.onReceiveListener receiveListener) {
        if(sendReceiveThread == null) {
            sendReceiveThread = new SendReceiveThread(socket);
            setOnReceiveListener(receiveListener);
            sendReceiveThread.start();
        }
    }

    /*package*/ // stop sendReceive thread
    protected void stop() {
        if(sendReceiveThread != null) {
            sendReceiveThread.cancel();
            sendReceiveThread = null;
        }

        if(instance != null) {
            instance = null;
        }
    }

    /**
     * Set receive listener,you can read data with this listener.
     * @param receiveListener BluetoothListener.onReceivedListener
     */
    public void setOnReceiveListener(BluetoothListener.onReceiveListener receiveListener) {
        if(sendReceiveThread != null) {
            sendReceiveThread.attachReceiveListener(receiveListener);
        }
    }

    /**
     * Sends data in String format message to connected device.
     * @param data string to be send
     * @return true if data send successfully
     */
    public boolean send(String data) {
        if(sendReceiveThread != null) {
            return sendReceiveThread.write(data);
        }
        return false;
    }

    /**
     * Sends byte array to connected device.
     * @param b byte array to be send
     * @return true if data send successfully
     */
    public boolean send(byte[] b) {
        if(sendReceiveThread != null) {
            return sendReceiveThread.write(b);
        }
        return false;
    }

    /**
     * Sends byte array , int offset and int length to connected device.
     * @param b byte array to be send
     * @param off int offset to be send
     * @param len int length to be send
     * @return true if data send successfully
     */
    public boolean send(byte[] b,int off,int len) {
        if(sendReceiveThread != null) {
            return sendReceiveThread.write(b,off,len);
        }
        return false;
    }



    private static class SendReceiveThread extends Thread {

        // Input and Output Stream
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private BluetoothListener.onReceiveListener receiveListener = null; // Bluetooth receive listener


        public SendReceiveThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep looping to listen received data
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    setReceivedListenerResult(readMessage);
                    setReceivedListenerResult(readMessage, buffer);// send data to receive listener
                }
                catch (IOException e) {
                    break;
                }
            }
        }

        // write method String
        public boolean write(String input) {
            byte[] msgBuffer = input.getBytes();   //converts entered String into bytes

            try {
                mmOutStream.write(msgBuffer);
                return true;
            }
            catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        // write method Byte
        public boolean write(byte[] b) {
            try {
                mmOutStream.write(b);
                return true;
            }
            catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        //write method Byte ,Offset,Length
        public boolean write(byte[] b,int off,int len) {
            try {
                mmOutStream.write(b,off,len);
                return true;
            }
            catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        // cancel send receive process
        private void cancel() {
            deAttachReceiveListener();

            if(mmInStream != null) {
                try {
                    // close input stream
                    mmInStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(mmOutStream != null) {
                try {
                    // close output stream
                    mmOutStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void attachReceiveListener(BluetoothListener.onReceiveListener receiveListener) {
            if(receiveListener != null) {
                // attach receive listener
                this.receiveListener = receiveListener;
            }
        }

        private void deAttachReceiveListener() {
            if(this.receiveListener != null) {
                // deAttach receive listener
                this.receiveListener = null;
            }
        }

        private void setReceivedListenerResult(String receivedData) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (SendReceiveThread.this.receiveListener != null) {
                        SendReceiveThread.this.receiveListener.onReceived(receivedData);
                    }
                }
            });
        }

        private void setReceivedListenerResult(String receivedData, byte[] buffer) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (SendReceiveThread.this.receiveListener != null) {
                        SendReceiveThread.this.receiveListener.onReceived(receivedData);
                        SendReceiveThread.this.receiveListener.onReceived(receivedData, buffer);
                    }
                }
            });
        }

    }
}
