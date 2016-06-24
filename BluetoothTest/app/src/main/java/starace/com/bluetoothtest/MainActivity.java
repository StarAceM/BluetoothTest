package starace.com.bluetoothtest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_MAIN = "MainActivity";
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 13;
    private boolean btEnabled;
    private boolean isPaired;
    private IntentFilter deviceFilter;
    private BluetoothDevice device;
    public static final String DEVICE_NAME = "UE MINI BOOM";
    private BluetoothBroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enableBluetoothAdapter();
        regBroadcastReceiver();
        checkForDevices();

    }

    private void enableBluetoothAdapter() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null ){
            // device doesn't support bluetooth
        } else {

            if (!bluetoothAdapter.isEnabled()) {

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                btEnabled = true;
            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 13) {
                //bluetooth enabled
                btEnabled = true;
            }
        }

    }

    private void regBroadcastReceiver(){

        deviceFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        deviceFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        deviceFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(new BluetoothBroadcastReceiver(bluetoothAdapter), deviceFilter);

    }

    private void checkForDevices(){
        if (btEnabled) {
            checkForPairedDevices();
            if (!isPaired) {
                Log.d(TAG_MAIN, "findDevice has been called");
                findDevice();
            }
        }
    }

    //check if a device is already paired and check it is the correct device for the application
    private void checkForPairedDevices(){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        Log.d(TAG_MAIN, "paired devices size " + pairedDevices.size());
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices){
                Log.d(TAG_MAIN, "Paired Device Name" + device.getName());
                if (device.getName().equals(DEVICE_NAME)){
                    this.device = device;
                    isPaired = true;
                } else {
                    //paired device is not correct device
                    //need to tell user or manually disconnect after alert dialog
                    //before searching for the correct device
                }
            }

        }

    }

    private void findDevice() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
        boolean started = bluetoothAdapter.startDiscovery();
        Log.d(TAG_MAIN, "Discovery Started Successfully " + started);
        Log.d(TAG_MAIN, "Discovery Started Successfully " + bluetoothAdapter.isDiscovering());

    }

    @Override
    protected void onDestroy() {
        bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(broadcastReceiver);

        super.onDestroy();
    }
}
