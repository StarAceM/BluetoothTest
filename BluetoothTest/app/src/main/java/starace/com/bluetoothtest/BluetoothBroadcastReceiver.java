package starace.com.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by mstarace on 6/23/16.
 */
public class BluetoothBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG_RECEIVER = "BluetoothBroadcast";
    private BluetoothDevice device;
    private BluetoothAdapter bluetoothAdapter;

    public BluetoothBroadcastReceiver(){}

    public BluetoothBroadcastReceiver(BluetoothAdapter bluetoothAdapter){
        this.bluetoothAdapter = bluetoothAdapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG_RECEIVER, "Action = " + action);
        if (action.equals(BluetoothDevice.ACTION_FOUND)){
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device.getName() != null) {
                Log.d(TAG_RECEIVER, "Device Found Name " + device.getName());
                if (device.getName().equals(MainActivity.DEVICE_NAME)) {
                    //bluetoothAdapter.cancelDiscovery();
                }
            }
        } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
            Log.d(TAG_RECEIVER, "Discovery Finished");
        } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
            Log.d(TAG_RECEIVER, "Discovery Started");
        }
    }

}
