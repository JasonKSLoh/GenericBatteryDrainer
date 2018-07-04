package com.lohjason.genericbatterydrainer.managers;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import com.lohjason.genericbatterydrainer.utils.Logg;

/**
 * WifiScanManager
 * Created by jason on 3/7/18.
 */
public class WifiScanManager {

    private static final String LOG_TAG = "+_WfiMgr";
    private static WifiScanManager instance;
    private boolean isRegistered = false;
    private BroadcastReceiver wifiScanReceiver;

    public static WifiScanManager getInstance() {
        if (instance == null) {
            instance = new WifiScanManager();
        }
        return instance;
    }

    private WifiScanManager() {
    }

    public void setWifiScanOn(Application application, boolean setOn) {
        WifiManager wifiManager = (WifiManager) application.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return;
        }
        if(!isWifiScanEnabled(application)){
            return;
        }
        if(setOn){
            if(isRegistered){
                return;
            }
            wifiScanReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if(wifiManager == null){
                        return;
                    }
                    wifiManager.startScan();
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            application.registerReceiver(wifiScanReceiver, intentFilter);
            wifiManager.startScan();
            isRegistered = true;
            Logg.d(LOG_TAG, "Starting Wifi Polling");
        } else {
            if(isRegistered && wifiScanReceiver != null){
                isRegistered = false;
                application.unregisterReceiver(wifiScanReceiver);
                wifiScanReceiver = null;
                Logg.d(LOG_TAG, "Stopping Wifi Polling");
            }
        }
    }

    public void setWifiEnabled(Application application, boolean setEnabled){
        WifiManager wifiManager = (WifiManager) application.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager == null){
            Toast.makeText(application, "Error, could not change Wifi status", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(application, "Wifi Enabled", Toast.LENGTH_SHORT).show();
        wifiManager.setWifiEnabled(setEnabled);
    }

    public boolean isWifiScanEnabled(Application application) {
        WifiManager wifiManager = (WifiManager) application.getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null && (wifiManager.isScanAlwaysAvailable() || wifiManager.isWifiEnabled());
    }

}
