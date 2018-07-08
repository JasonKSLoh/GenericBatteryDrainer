package com.lohjason.genericbatterydrainer.managers;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;

import com.lohjason.genericbatterydrainer.utils.Logg;
import com.lohjason.genericbatterydrainer.utils.PermissionUtils;

/**
 * BluetoothScanManager
 * Created by jason on 3/7/18.
 */
public class BluetoothScanManager {
    private static final String LOG_TAG = "+_BthMgr";
    private static BluetoothScanManager instance;
    private BroadcastReceiver           bluetoothReceiver;
    private boolean isRegistered = false;


    public static BluetoothScanManager getInstance() {
        if(instance == null){
            instance = new BluetoothScanManager();
        }
        return instance;
    }

    private BluetoothScanManager(){}

    public void setBluetoothScan(Application application, boolean setOn){
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if(!isBluetoothEnabled()){
            return;
        }
        if(setOn){
            if(isRegistered){
                return;
            }
            isRegistered = true;
            bluetoothReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    Logg.d(LOG_TAG, "Starting new scan!");
                    adapter.startDiscovery();
                }
            };
            IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            application.registerReceiver(bluetoothReceiver, intentFilter);
            adapter.startDiscovery();
            Logg.d(LOG_TAG, "Started Bluetooth Discovery");
        } else {
            if(isRegistered && bluetoothReceiver != null){
                application.unregisterReceiver(bluetoothReceiver);
                isRegistered = false;
                bluetoothReceiver = null;
                Logg.d(LOG_TAG, "Stopped Bluetooth Discovery");
            }
        }
    }

    public void enableBluetooth(AppCompatActivity activity){
//        if(hasBluetooth()){
//            BluetoothAdapter.getDefaultAdapter().enable();
//        }
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, PermissionUtils.REQUEST_CODE_BLUETOOTH);
    }

    public boolean isBluetoothEnabled(){
        return hasBluetooth() && BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    private boolean hasBluetooth(){
        return BluetoothAdapter.getDefaultAdapter() != null;
    }
}
