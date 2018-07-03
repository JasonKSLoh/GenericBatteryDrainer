package com.lohjason.genericbatterydrainer.managers;

import android.app.Application;
import android.content.ContentResolver;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.lohjason.genericbatterydrainer.Logg;

/**
 * ScreenManager
 * Created by jason on 2/7/18.
 */
public class ScreenManager {

    private static final String LOG_TAG = "+_ScnMgr";
    private static final int MAX_BRIGHTNESS = 255;
    private static final int MAX_SCREEN_TIMEOUT = Integer.MAX_VALUE;
    private static ScreenManager instance = null;

    private int originalBrightness = -1;
    private int originalTimeout = -1;
    private boolean brightnessWasAutomatic = false;


    public static ScreenManager getInstance(){
        if(instance == null){
            instance = new ScreenManager();
        }
        return instance;
    }

    public void keepScreenOn(AppCompatActivity activity, boolean keepOn){
        Logg.d(LOG_TAG, "Keep screen on? " + keepOn);
        if(keepOn){
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public void setScreenTimeoutToMax(Application application, boolean setMax){
        ContentResolver contentResolver = application.getContentResolver();
        if(setMax){
            try {
                originalTimeout =
                        Settings.System.getInt(
                                contentResolver,
                                Settings.System.SCREEN_OFF_TIMEOUT);
            } catch (Settings.SettingNotFoundException e) {
                Logg.d(LOG_TAG, "Screen timeout not found, setting to 60s");
                originalTimeout = 60000;
            }

            if(originalTimeout == MAX_SCREEN_TIMEOUT){
                return;
            }
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, MAX_SCREEN_TIMEOUT);
            Logg.d(LOG_TAG, "Set screen timeout to max");
        } else {
            if(originalTimeout < 0){
                return;
            }
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, originalTimeout);
            Logg.d(LOG_TAG, "Set timeout to " + originalTimeout);
        }
    }

    public void setBrightnessToMax(Application application, boolean setMax){
        ContentResolver contentResolver = application.getContentResolver();
        brightnessWasAutomatic = wasBrightnessAutomatic(application);
        Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

        if(setMax){
            try {
                originalBrightness =
                        Settings.System.getInt(
                                contentResolver,
                                Settings.System.SCREEN_BRIGHTNESS);
            } catch (Settings.SettingNotFoundException e) {
                Logg.d(LOG_TAG, "Screen brightness not found");
            }

            if(originalBrightness == MAX_BRIGHTNESS){
                return;
            }
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, MAX_BRIGHTNESS);
            Logg.d(LOG_TAG, "Set brightness to max");
        } else {
            if(originalBrightness < 0){
                return;
            }
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, originalBrightness);
            Logg.d(LOG_TAG, "Set brightness to " + originalBrightness);
        }
    }
    private boolean wasBrightnessAutomatic(Application application){
        ContentResolver contentResolver = application.getContentResolver();
        Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        int wasAutomatic;
        try {
            wasAutomatic =
                    Settings.System.getInt(
                            contentResolver,
                            Settings.System.SCREEN_BRIGHTNESS_MODE);
            return wasAutomatic == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            Logg.d(LOG_TAG, "Screen brightness mode not found");
            return false;
        }
    }

}
