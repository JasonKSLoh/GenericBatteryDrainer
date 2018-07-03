package com.lohjason.genericbatterydrainer.managers;

import android.app.Application;

/**
 * DrainManager
 * Created by jason on 2/7/18.
 */
public class DrainManager {

    private static final String LOG_TAG = "+_DrnMgr";

    private static DrainManager instance;
    private Application application;
    private FlashManager flashManager;
    private ScreenManager screenManager;
    private CpuManager cpuManager;
    private WifiScanManager wifiScanManager;
    private FineLocationManager fineLocationManager;
    BluetoothScanManager bluetoothScanManager;

    private DrainManager(){}

    public static DrainManager getInstance(Application application) {
        if(instance == null){
            instance = new DrainManager();
            instance.flashManager = FlashManager.getInstance();
            instance.screenManager = ScreenManager.getInstance();
            instance.cpuManager = CpuManager.getInstance();
            instance.fineLocationManager = FineLocationManager.getInstance();
            instance.wifiScanManager = WifiScanManager.getInstance();
            instance.bluetoothScanManager = BluetoothScanManager.getInstance();
            instance.application = application;
        }
        return instance;
    }

    public void startDraining(boolean useFlash,
                              boolean useScreen,
                              boolean useCpu,
                              boolean useLocation,
                              boolean useWifi,
                              boolean useBluetooth){
        if(useFlash){
            flashManager.startFlashService(application);
        }
        if(useScreen){
            screenManager.setBrightnessToMax(application, true);
            screenManager.setScreenTimeoutToMax(application, true);
        }
        if(useCpu){
            cpuManager.setWakelockOn(application, true);
            cpuManager.setComputationOn(true);
        }
        if(useLocation){
            fineLocationManager.setRequestingGpsOn(application, true);
        }
        if(useWifi){
            wifiScanManager.setWifiScanOn(application, true);
        }
        if(useBluetooth){
            bluetoothScanManager.setBluetoothScan(application, true);
        }
    }
    public void stopDraining(boolean useFlash,
                             boolean useScreen,
                             boolean useCpu,
                             boolean useLocation,
                             boolean useWifi,
                             boolean useBluetooth){
        if(useFlash){
            flashManager.stopFlashService(application);
        }
        if(useScreen){
            screenManager.setBrightnessToMax(application, false);
            screenManager.setScreenTimeoutToMax(application, false);
        }
        if(useCpu){
            cpuManager.setWakelockOn(application, false);
            cpuManager.setComputationOn(false);
        }
        if(useLocation){
            fineLocationManager.setRequestingGpsOn(application, false);
        }
        if(useWifi){
            wifiScanManager.setWifiScanOn(application, false);
        }
        if(useBluetooth){
            bluetoothScanManager.setBluetoothScan(application, false);
        }
    }
}
