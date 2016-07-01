package starace.com.bluetoothtest.Threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

import starace.com.bluetoothtest.BluetoothService;

/**
 * Created by mstarace on 7/1/16.
 */
public class AcceptThread extends Thread {
    private static final String TAG_ACCEPT_THREAD = "AcceptThread";
    private final BluetoothServerSocket serverSocket;
    private final BluetoothAdapter adapter;
    private final BluetoothService btService;

    public AcceptThread(BluetoothAdapter adapter, BluetoothService btService){
        this.adapter = adapter;
        this.btService = btService;
        BluetoothServerSocket tmpServerSocket = null;

        try {
            tmpServerSocket = adapter.listenUsingRfcommWithServiceRecord(BluetoothService.NAME_SECURE,ConnectThread.UUID_SECURE);
        } catch (IOException e){
            Log.e(TAG_ACCEPT_THREAD,"Failed to get ServerSocket", e);
        }

        serverSocket = tmpServerSocket;
    }

    public void run(){
        BluetoothSocket socket = null;
        
        while (btService.serviceState != BluetoothService.STATE_CONNECTED){
            try{
                socket = serverSocket.accept();
            } catch (IOException e){
                Log.e(TAG_ACCEPT_THREAD,"failed to obtain socket from ServerSocket", e);
            }
        }

        if (socket != null){
            synchronized (btService){
                switch (btService.serviceState){
                    case BluetoothService.STATE_LISTEN:

                        break;
                    case BluetoothService.STATE_CONNECTING:
                        //normal state
                        btService.connected(socket.getRemoteDevice(), socket);
                        break;
                    case BluetoothService.STATE_NONE:

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
