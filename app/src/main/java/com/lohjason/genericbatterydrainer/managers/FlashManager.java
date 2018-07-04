package com.lohjason.genericbatterydrainer.managers;

import android.app.Application;
import android.content.Intent;

import com.lohjason.genericbatterydrainer.services.FlashService;
import com.lohjason.genericbatterydrainer.utils.Logg;

/**
 * FlashManager
 * Created by jason on 2/7/18.
 */
public class FlashManager {

    private static final String LOG_TAG = "+_FlsMgr";
    private static FlashManager instance;
    private Intent flashServiceIntent;

    private FlashManager() {
    }

    public static FlashManager getInstance() {
        if (instance == null) {
            instance = new FlashManager();
        }
        return instance;
    }


    public void startFlashService(Application application){
        if(flashServiceIntent == null){
            flashServiceIntent = new Intent(application, FlashService.class);
            application.startService(flashServiceIntent);
            Logg.d(LOG_TAG, "Flash Service Started");
        }
    }

    public void stopFlashService(Application application){
        if(flashServiceIntent != null){
            application.stopService(flashServiceIntent);
            flashServiceIntent = null;
        }
    }





}
