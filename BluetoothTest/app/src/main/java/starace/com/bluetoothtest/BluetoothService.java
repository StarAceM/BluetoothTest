package starace.com.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by mstarace on 7/1/16.
 */
public class BluetoothService {
    private static final String TAG_BT_SERVICE = "BluetoothService";

    public static final String NAME_SECURE = "BluetoothChatSecure";

    public static final UUID UUID_SECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // n

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public final Handler btHandler;
    public final BluetoothAdapter adapter;
    public int serviceState;
    public ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private AcceptThread acceptThread;



    public BluetoothService(Context context, Handler handler, BluetoothAdapter adapter){
        btHandler = handler;
        this.adapter = adapter;
        serviceState = STATE_NONE;
    }

    public synchronized void setState(int state){
        serviceState = state;
        btHandler.obtainMessage(MESSAGE_STATE_CHANGE,state,-1).sendToTarget();
    }

    public synchronized int getState(){
        return serviceState;
    }

    public synchronized void start(){
        //cancel connect and connected threads if not null
        Log.d(TAG_BT_SERVICE,"BluetoothService start() is running");
        if(connectThread != null){
            connectThread.cancel();
            connectThread = null;
        }
        if(connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        setState(STATE_LISTEN);

        if(acceptThread==null){
            acceptThread = new AcceptThread();
            acceptThread.start();
        }

    }

    public synchronized void connect(BluetoothDevice device){
        if (serviceState == STATE_CONNECTING){
            if(connectThread !=null){
                connectedThread.cancel();
                connectedThread = null;
            }
        }

        if (connectedThread != null){
            connectedThread.cancel();
            connectedThread = null;
        }

        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);

    }


    public synchronized void connected(BluetoothDevice device, BluetoothSocket socket){

        if(connectThread != null){
            connectThread.cancel();
            connectThread = null;
        }
        if(connectedThread != null){
            connectedThread.cancel();
            connectedThread = null;
        }
        if(acceptThread != null){
            acceptThread.cancel();
            acceptThread = null;
        }

        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        Message msg = btHandler.obtainMessage(MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        msg.setData(bundle);
        btHandler.sendMessage(msg);

        setState(STATE_CONNECTED);

    }

    public void connectionFailed(){
        Message msg = btHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST,"Unable to Connect to Device");
        msg.setData(bundle);
        btHandler.sendMessage(msg);

        //do something here on connectFailure
    }

    public void connectionLost(){
        // Send a failure message back to the Activity
        Message msg = btHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, "Device connection was lost");
        msg.setData(bundle);
        btHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothService.this.start();
    }

    public class AcceptThread extends Thread {
        private static final String TAG_ACCEPT_THREAD = "AcceptThread";
        private final BluetoothServerSocket serverSocket;


        public AcceptThread(){

            BluetoothServerSocket tmpServerSocket = null;

            try {
                tmpServerSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(NAME_SECURE,UUID_SECURE);
            } catch (IOException e){
                Log.e(TAG_ACCEPT_THREAD,"Failed to get ServerSocket", e);
            }

            serverSocket = tmpServerSocket;
            Log.d(TAG_ACCEPT_THREAD,"serverSocket = " + serverSocket.toString());
        }

        public void run(){
            Log.d(TAG_ACCEPT_THREAD,"Run has been called in AcceptThread, serviceState = " +serviceState  );
            BluetoothSocket socket = null;

            while (serviceState != STATE_CONNECTED){
                try{
                    Log.d(TAG_ACCEPT_THREAD, "While loop is running");
                    socket = serverSocket.accept();
                } catch (IOException e){
                    Log.e(TAG_ACCEPT_THREAD,"failed to obtain socket from ServerSocket", e);
                }

            }

            if (socket != null){
                Log.d(TAG_ACCEPT_THREAD,"socket is not null" );
                synchronized (BluetoothService.this){
                    switch (serviceState){
                        case BluetoothService.STATE_LISTEN:

                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.d(TAG_ACCEPT_THREAD,"btService.connected has been called in AcceptThread");
                            //normal state
                            connected(socket.getRemoteDevice(), socket);
                            break;
                        case STATE_NONE:

                            break;
                        case BluetoothService.STATE_CONNECTED:
                            try{
                                socket.close();
                            }catch(IOException e){
                                Log.e(TAG_ACCEPT_THREAD,"Failed to close socket run()",e);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        public void cancel(){
            try{
                serverSocket.close();
            } catch (IOException e){
                Log.e(TAG_ACCEPT_THREAD,"Failed to close serverSocket cancel()",e);
            }
        }
    }

    public class ConnectThread extends Thread {
        private static final String TAG_CONNECT_THREAD = "ConnectThread";
        private final BluetoothDevice device;
        private final BluetoothSocket socket;

        public ConnectThread(BluetoothDevice device){
            this.device = device;
            BluetoothSocket tmpSocket = null;

            try{
                tmpSocket = device.createInsecureRfcommSocketToServiceRecord(UUID_SECURE);
            } catch (IOException e){
                Log.e(TAG_CONNECT_THREAD, "Bluetooth Connection Failed", e);
            }

            socket = tmpSocket;

        }

        public void run(){
            setName(TAG_CONNECT_THREAD);
            Log.d(TAG_CONNECT_THREAD,"Run has been called in the ConnectThread");
            Log.d(TAG_CONNECT_THREAD,socket.getRemoteDevice().getName());
            try {
                Log.d(TAG_CONNECT_THREAD, "socket.connect() has been called");
                socket.connect();
            } catch (IOException eConnect){
                try{
                    Log.d(TAG_BT_SERVICE, "Socket Connected" + socket.isConnected());
                    socket.close();
                } catch (IOException eClose){
                    Log.e(TAG_CONNECT_THREAD, "unable to close during ConnectThread run()", eClose);
                }
                //btService.connectionFailed();
                Log.e(TAG_CONNECT_THREAD, "IOEXCEPTION thrown e = " + eConnect);
            }
            //finished with thread
            synchronized (BluetoothService.this){
                connectThread = null;
            }

            Log.d(TAG_CONNECT_THREAD,"socket connected = " + socket.isConnected());
            //may be an issue but not sure want to limit instances of service activity
            //btService = null;
            //start connectThread


        }

        public void cancel(){
            try {
                socket.close();
            } catch (IOException e){
                Log.e(TAG_CONNECT_THREAD,"Failed to close connectThead cancel()",e);
            }
        }
    }

    public class ConnectedThread extends Thread {
        private static final String TAG_CONNECTED_THREAD = "ConnectedThread";
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket){
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e){
                Log.e(TAG_CONNECTED_THREAD,"Could not get Input and/or Output Streams", e);
            }

            inputStream = tmpIn;
            outputStream = tmpOut;

        }

        public void run(){
            Log.d(TAG_CONNECTED_THREAD,"Run has been called in the ConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            // Keep listening to the InputStream while connected
            while (serviceState == BluetoothService.STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = inputStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    btHandler.obtainMessage(BluetoothService.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG_CONNECTED_THREAD, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    //btService.start(); already called by connectionLost()??
                    break;
                }
            }

        }

        public void write(byte[] buffer){

        }

        public void cancel(){
            try {
                socket.close();
            } catch(IOException e) {
                Log.e(TAG_CONNECTED_THREAD,"Failed to close socker cancel()",e);
            }
        }

    }



}
