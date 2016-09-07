package in.entrylog.chetsgani.main.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import in.entrylog.chetsgani.myprinter.Global;
import in.entrylog.chetsgani.myprinter.WorkService;
import in.entrylog.chetsgani.values.FunctionCalls;

/**
 * Created by Admin on 01-Sep-16.
 */
public class Bluetooth extends Service {
    public static Handler mHandler = null;
    public static BluetoothAdapter mBluetoothAdapter;
    static ArrayList<String> arrayListpaired;
    static ArrayList<BluetoothDevice> arrayListPairedBluetoothDevices;
    BroadcastReceiver mReceiver, mPairing;
    public static boolean btconnected = false, deviceconnected = false, devicenotconnected = false;
    boolean devicefound = false, scanningstarted = false, connectingdevice = false, devicenamenotfound = false,
            pairingstarted = false, scanningregistered = false;
    Thread bluetooththread, scanningthread;
    IntentFilter scanningdevice;
    FunctionCalls functionCalls;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        functionCalls = new FunctionCalls();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.enable();
        arrayListpaired = new ArrayList<String>();
        arrayListPairedBluetoothDevices = new ArrayList<BluetoothDevice>();

        functionCalls.LogStatus("Bluetooth Service onCreate");

        mHandler = new MHandler(this);
        WorkService.addHandler(mHandler);

        if (null == WorkService.workThread) {
            Intent intent = new Intent(this, WorkService.class);
            startService(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        functionCalls.LogStatus("Bluetooth Service onStartCommand");
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mBluetoothAdapter.isEnabled()) {
                    getPairedDevices();
                }
            }
        }, 5000);

        CheckScanning();
        BluetoothTimerThread();

        return Service.START_STICKY;
    }

    private void CheckScanning() {
        Log.d("debug", "Check scanning started");
        scanningthread = null;
        Runnable runnable = new ScanningTimer();
        scanningthread = new Thread(runnable);
        scanningthread.start();
    }

    private void BluetoothTimerThread() {
        Log.d("debug", "Bluetooth Timer started");
        bluetooththread = null;
        Runnable runnable = new BluetoothTimer();
        bluetooththread = new Thread(runnable);
        bluetooththread.start();
    }

    class ScanningTimer implements Runnable {
        int i = 0;

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doscanning();
                    Thread.sleep(10000);
                    i = i + 1;
                    Log.d("debug", "scanning timer count "+i);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                }
            }
        }
    }

    private void doscanning() {
        if (scanningstarted) {
            scanningstarted = false;
            if (devicenamenotfound){
                devicenamenotfound = false;
                Log.d("debug", "Device Name not Found So sleeping for 10 seconds");
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        Startscanning();
                    }
                }, 10000);
            } else {
                Log.d("debug", "Please Switch On Device to Scan");
                Toast.makeText(Bluetooth.this, "Please Switch On Device to Scan", Toast.LENGTH_SHORT).show();
            }
        }
        if (devicefound) {
            devicefound = false;
            Log.d("debug", "Device Found and Scanning Thread is interrupting");
            scanningthread.interrupt();
        }
        if (btconnected) {
            scanningthread.interrupt();
        }
    }

    class BluetoothTimer implements Runnable {
        int i = 0;

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    checkConnection();
                    i = i + 1;
                    Log.d("debug", "bluetooth timer count "+i);
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                }
            }
        }
    }

    private void checkConnection() {
        if (devicenotconnected) {
            devicenotconnected = false;
            Log.d("debug", "Device not connected");
            Log.d("debug", "Please Turn On the Bluetooth Printer");
            getPairedDevices();
        }
        if (btconnected) {
            bluetooththread.interrupt();
        }
    }

    public void getPairedDevices() {
        boolean devicepaired = false;
        Set<BluetoothDevice> pairedDevice = mBluetoothAdapter.getBondedDevices();
        if (pairedDevice.size() > 0) {
            try {
                for (BluetoothDevice device : pairedDevice) {
                    arrayListpaired.add(device.getName() + " " + device.getAddress());
                    arrayListPairedBluetoothDevices.add(device);
                    Log.d("debug", "Already Paired Devices: " + device.getName());
                    if (device.getName().equals("BP-201")) {
                        Log.d("debug", "Paired Devices: " + device.getName());
                        WorkService.workThread.connectBt(device.getAddress());
                        devicepaired = true;
                        break;
                    }
                }
                if (!devicepaired) {
                    Log.d("debug", "Device not Paired so starting scanning");
                    Startscanning();
                }
            } catch (Exception e) {
            }
        } else {
            Log.d("debug", "No Device Bonded");
            Startscanning();
        }
    }

    private void Startscanning() {
        Log.d("debug", "Start Scanning");
        mBluetoothAdapter.startDiscovery();
        scanningstarted = true;
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        Log.d("debug", "Devices Found: " + device.getName());
                        if (device.getName().equals("BP-201")) {
                            devicefound = true;
                            Log.d("debug", "Pairing Devices Found: " + device.getName());
                            PairDevice(device);
                        }
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.d("debug", "nodevice found to scan so starting again to scan");
                    mBluetoothAdapter.startDiscovery();
                }
            }
        };

        // Register the BroadcastReceiver
        scanningdevice = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        scanningregistered = true;
        registerReceiver(mReceiver, scanningdevice);
    }

    private void PairDevice(final BluetoothDevice device) {
        pairingstarted = true;
        Log.d("debug", "Started Device Pairing");
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPairing = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
                    try {
                        byte[] pin = (byte[]) BluetoothDevice.class.getMethod("convertPinToBytes", String.class).invoke(BluetoothDevice.class, "0000");
                        Method m = device.getClass().getMethod("setPin", byte[].class);
                        m.invoke(device, pin);
                        device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
                        Log.d("debug", "Device Paired");
                        if (!connectingdevice) {
                            connectingdevice = true;
                            WorkService.workThread.connectBt(device.getAddress());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        registerReceiver(mPairing, filter);
    }

    static class MHandler extends Handler {

        WeakReference<Bluetooth> mActivity;

        MHandler(Bluetooth Service) {
            mActivity = new WeakReference<Bluetooth>(Service);
        }

        @Override
        public void handleMessage(Message msg) {
            Bluetooth theActivity = mActivity.get();
            switch (msg.what) {

                case Global.MSG_WORKTHREAD_SEND_CONNECTBTRESULT: {
                    int result = msg.arg1;
                    if (result == 1) {
                        btconnected = true;
                        deviceconnected = true;
                        Log.d("debug", "Bluetooth Device Connected");
                        Toast.makeText(theActivity, "Bluetooth Printer connected",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        devicenotconnected = true;
                        Toast.makeText(theActivity, "Please Switch On the Bluetooth Printer",
                                Toast.LENGTH_SHORT).show();
                    }
                    Log.d("debug", "Connect Result: " + result);
                    break;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        functionCalls.LogStatus("Bluetooth Service onDestroy");
        WorkService.delHandler(mHandler);
        mHandler = null;
        if (bluetooththread.isAlive()) {
            bluetooththread.interrupt();
        }
        if (scanningthread.isAlive()) {
            scanningthread.interrupt();
        }
        mBluetoothAdapter.disable();
        if (scanningregistered) {
            this.unregisterReceiver(mReceiver);
        }
        if (pairingstarted) {
            this.unregisterReceiver(mPairing);
        }
    }
}
