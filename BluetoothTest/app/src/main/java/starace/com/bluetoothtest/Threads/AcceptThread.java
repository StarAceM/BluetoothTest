package starace.com.bluetoothtest.Threads;

/**
 * Created by mstarace on 7/1/16.
 */
//public class AcceptThread extends Thread {
//    private static final String TAG_ACCEPT_THREAD = "AcceptThread";
//    private final BluetoothServerSocket serverSocket;
//    private final BluetoothAdapter adapter;
//    private final BluetoothService btService;
//
//    public AcceptThread(BluetoothAdapter adapter, BluetoothService btService){
//        this.adapter = adapter;
//        this.btService = btService;
//        BluetoothServerSocket tmpServerSocket = null;
//
//        try {
//            tmpServerSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(BluetoothService.NAME_SECURE,ConnectThread.UUID_SECURE);
//        } catch (IOException e){
//            Log.e(TAG_ACCEPT_THREAD,"Failed to get ServerSocket", e);
//        }
//
//        serverSocket = tmpServerSocket;
//        Log.d(TAG_ACCEPT_THREAD,"serverSocket = " + serverSocket.toString());
//    }
//
//    public void run(){
//        Log.d(TAG_ACCEPT_THREAD,"Run has been called in AcceptThread, serviceState = " +btService.serviceState  );
//        BluetoothSocket socket = null;
//
//        while (btService.serviceState != BluetoothService.STATE_CONNECTED){
//            try{
//                Log.d(TAG_ACCEPT_THREAD, "While loop is running");
//                socket = serverSocket.accept();
//            } catch (IOException e){
//                Log.e(TAG_ACCEPT_THREAD,"failed to obtain socket from ServerSocket", e);
//            }
//
//        }
//
//        if (socket != null){
//            Log.d(TAG_ACCEPT_THREAD,"socket is not null" );
//            synchronized (btService){
//                switch (btService.serviceState){
//                    case BluetoothService.STATE_LISTEN:
//
//                        break;
//                    case BluetoothService.STATE_CONNECTING:
//                        Log.d(TAG_ACCEPT_THREAD,"btService.connected has been called in AcceptThread");
//                        //normal state
//                        btService.connected(socket.getRemoteDevice(), socket);
//                        break;
//                    case BluetoothService.STATE_NONE:
//
//                        break;
//                    case BluetoothService.STATE_CONNECTED:
//                        try{
//                            socket.close();
//                        }catch(IOException e){
//                            Log.e(TAG_ACCEPT_THREAD,"Failed to close socket run()",e);
//                        }
//                        break;
//                    default:
//                        break;
//                }
//            }
//        }
//    }
//
//    public void cancel(){
//        try{
//            serverSocket.close();
//        } catch (IOException e){
//            Log.e(TAG_ACCEPT_THREAD,"Failed to close serverSocket cancel()",e);
//        }
//    }
//}
