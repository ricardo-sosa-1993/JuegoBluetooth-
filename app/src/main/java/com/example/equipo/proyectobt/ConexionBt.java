package com.example.equipo.proyectobt;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;


public class ConexionBt  {


    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private BluetoothAdapter mBluetoothAdapter;

    private static final UUID MY_UUID = UUID.fromString("1305b074-4e21-425b-bd6d-41b1944839b9");

    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private Handler mHandler;


    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    private int mState;

    public static final int MESSAGE_READ = 9999;
    public static final int MESSAGE_WRITE = 1111;
    public static final int MESSAGE_DEVICE_NAME = 2222;

    public static final int NO_COMPATIBLE = 3333;
    public static final int NO_HABILITADO = 4444;



    public ConexionBt(Handler handler ){
       mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mHandler = handler;

        mState = STATE_NONE;



    }






    private synchronized void setState(int state) {

        mState = state;

    }











    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                //"create() failed"
            }
            mmSocket = tmp;
        }
        public void run() {
            //BEGIN mConnectThread")

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                //conexion fallo
                setState(STATE_LISTEN);
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    //unable to close() socket during connection failure", e2)
                }
                // Start the service over to restart listening mode
                this.start();
                return;
            }
            // Reset the ConnectThread because we're done
            synchronized (ConexionBt.this) {
                mConnectThread = null;
            }
            // Start the connected thread
            connected(mmSocket, mmDevice);
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //close() of connect socket failed"
            }
        }
    }

    public synchronized void connect(BluetoothDevice device) {

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            //create ConnectedThread
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                //temp sockets not created
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            //BEGIN mConnectedThread
            byte[] buffer = new byte[1024];
            int bytes;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(ConexionBt.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    //se perdio conexion
                    setState(STATE_LISTEN);
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(ConexionBt.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                //Exception during write"
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //close() of connect socket failed//
            }
        }
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity

        Message msg = mHandler.obtainMessage(ConexionBt.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString("nombre de dispositivo", device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {

        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        setState(STATE_NONE);
    }

    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    public synchronized void start() {

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("BT Naval", MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (mState != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {

                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    synchronized (ConexionBt.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    //Could not close unwanted socket
                                }
                                break;
                        }
                    }
                }
            }
        }
        public void cancel() {

            try {
                mmServerSocket.close();
            } catch (IOException e) {

            }
        }
    }
}
