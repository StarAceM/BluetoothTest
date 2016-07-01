package starace.com.bluetoothtest.Threads;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import starace.com.bluetoothtest.BluetoothService;

/**
 * Created by mstarace on 7/1/16.
 */
public class ConnectThread extends Thread {
    private static final String TAG_CONNECT_THREAD = "ConnectThread";
    private final BluetoothDevice device;
    private final BluetoothSocket socket;
    private BluetoothService btService;
    public static final UUID UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    public ConnectThread(BluetoothDevice device, BluetoothService btService){
        this.device = device;
        this.btService = btService;
        BluetoothSocket tmpSocket = null;

        try{
            tmpSocket = device.createRfcommSocketToServiceRecord(UUID_SECURE);
        } catch (IOException e){
            Log.e(TAG_CONNECT_THREAD, "Bluetooth Connection Failed", e);
        }

        socket = tmpSocket;

    }

    public void run(){
        setName(TAG_CONNECT_THREAD);

        try {
            socket.connect();
        } catch (IOException eConnect){
            try{
                socket.close();
            } catch (IOException eClose){
                Log.e(TAG_CONNECT_THREAD, "unable to close during ConnectThread run()", eClose);
            }
            btService.connectionFailed();
        }
        //finished with thread
        synchronized (btService){
            btService.connectThread = null;
        }

        //may be an issue but not sure want to limit instances of service activity
        btService = null;
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
