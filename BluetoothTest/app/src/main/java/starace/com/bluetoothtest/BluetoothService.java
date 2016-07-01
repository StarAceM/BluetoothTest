package starace.com.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import starace.com.bluetoothtest.Threads.AcceptThread;
import starace.com.bluetoothtest.Threads.ConnectThread;
import starace.com.bluetoothtest.Threads.ConnectedThread;

/**
 * Created by mstarace on 7/1/16.
 */
public class BluetoothService {
    private static final String TAG_BT_SERVICE = "BluetoothService";

    public static final String NAME_SECURE = "BluetoothChatSecure";

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
        if(connectThread != null){
            connectThread.cancel();
            connectThread = null;
        }
        if(connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        setState(STATE_LISTEN);

        if(acceptThread!=null){
            acceptThread = new AcceptThread(adapter,this);
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

        connectThread = new ConnectThread(device,this);
        connectedThread.start();
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

        connectedThread = new ConnectedThread(socket,this);
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




}
