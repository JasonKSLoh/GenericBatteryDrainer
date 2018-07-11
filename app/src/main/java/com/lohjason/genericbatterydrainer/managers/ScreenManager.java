package com.lohjason.genericbatterydrainer.managers;

import android.app.Application;
import android.content.ContentResolver;
import android.provider.Settings;

import com.lohjason.genericbatterydrainer.utils.Logg;

/**
 * ScreenManager
 * Created by jason on 2/7/18.
 */
public class ScreenManager {

    private static final String LOG_TAG = "+_ScnMgr";
    private static final int MAX_BRIGHTNESS = 255;
    private static final int MAX_SCREEN_TIMEOUT = Integer.MAX_VALUE;
    private static final int DEFAULT_TIMEOUT = 60000;
    private static ScreenManager instance = null;

    private int originalBrightness = -1;
    private int originalTimeout = DEFAULT_TIMEOUT;
    private Boolean brightnessWasAutomatic;

    private boolean isMaxBrightnessOn = false;
    private boolean isMaxScreenTimeoutOn = false;


    public static ScreenManager getInstance(){
        if(instance == null){
            instance = new ScreenManager();
        }
        return instance;
    }

//    public void keepScreenOn(AppCompatActivity activity, boolean keepOn){
//        Logg.d(LOG_TAG, "Keep screen on? " + keepOn);
//        if(keepOn){
//            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        } else {
//            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        }
//    }

    public void setScreenTimeoutToMax(Application application, boolean setMax){
        if(isMaxScreenTimeoutOn == setMax){
            return;
        }
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

            Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, MAX_SCREEN_TIMEOUT);
            Logg.d(LOG_TAG, "Set screen timeout to max");
            isMaxScreenTimeoutOn = true;
        } else {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, originalTimeout);
            Logg.d(LOG_TAG, "Set timeout to " + originalTimeout);
            isMaxScreenTimeoutOn = false;
        }
    }

    public void setBrightnessToMax(Application application, boolean setMax){
        if(setMax == isMaxBrightnessOn){
            return;
        }
        ContentResolver contentResolver = application.getContentResolver();
        if(!isMaxBrightnessOn){
            brightnessWasAutomatic = wasBrightnessAutomatic(application);
        }
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
                originalBrightness = MAX_BRIGHTNESS / 2;
                Logg.d(LOG_TAG, "Screen brightness not found");
            }

            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, MAX_BRIGHTNESS);
            isMaxBrightnessOn = true;
            Logg.d(LOG_TAG, "Set brightness to max");
        } else {
            if(originalBrightness > 0){
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, originalBrightness);
            }
            if(brightnessWasAutomatic){
                Settings.System.putInt(
                        contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            }
            isMaxBrightnessOn = false;
            Logg.d(LOG_TAG, "Set brightness to " + originalBrightness);
        }
    }
    private boolean wasBrightnessAutomatic(Application application){
        ContentResolver contentResolver = application.getContentResolver();
        int brightnessMode;
        try {
            brightnessMode =
                    Settings.System.getInt(
                            contentResolver,
                            Settings.System.SCREEN_BRIGHTNESS_MODE);
            boolean wasAutomatic = brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            Logg.d(LOG_TAG, "Screen brightness mode was : " + wasAutomatic);
            return wasAutomatic;
        } catch (Settings.SettingNotFoundException e) {
            Logg.d(LOG_TAG, "Screen brightness mode not found");
            return false;
        }
    }

}
