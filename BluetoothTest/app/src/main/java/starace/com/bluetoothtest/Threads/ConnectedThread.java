package starace.com.bluetoothtest.Threads;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import starace.com.bluetoothtest.BluetoothService;

/**
 * Created by mstarace on 7/1/16.
 */
public class ConnectedThread extends Thread {
    private static final String TAG_CONNECTED_THREAD = "ConnectedThread";
    private final BluetoothSocket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private BluetoothService btService;

    public ConnectedThread(BluetoothSocket socket, BluetoothService btService){
        this.socket = socket;
        this.btService = btService;
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
        while (btService.serviceState == BluetoothService.STATE_CONNECTED) {
            try {
                // Read from the InputStream
                bytes = inputStream.read(buffer);

                // Send the obtained bytes to the UI Activity
                btService.btHandler.obtainMessage(BluetoothService.MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG_CONNECTED_THREAD, "disconnected", e);
                btService.connectionLost();
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
            btService = null;
        } catch(IOException e) {
            Log.e(TAG_CONNECTED_THREAD,"Failed to close socker cancel()",e);
        }
    }

}
