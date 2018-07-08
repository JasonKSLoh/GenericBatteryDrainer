package com.lohjason.genericbatterydrainer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.lohjason.genericbatterydrainer.services.DrainForegroundService;
import com.lohjason.genericbatterydrainer.ui.SettingsDialogFragment;

/**
 * SharedPrefsUtils
 * Created by jason on 8/7/18.
 */
public class SharedPrefsUtils {

    private static final String KEY_SHUTOFF_TEMP = "key_shutoff_temp";
    private static final String KEY_SHUTOFF_LEVEL = "key_shutoff_level";

    public static void saveSwitchStates(Context context,
                                        boolean useFlash,
                                        boolean useScreen,
                                        boolean useCpu,
                                        boolean useGpu,
                                        boolean useLocation,
                                        boolean useWifi,
                                        boolean useBluetooth ){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DrainForegroundService.KEY_FLASH, useFlash);
        editor.putBoolean(DrainForegroundService.KEY_SCREEN, useScreen);
        editor.putBoolean(DrainForegroundService.KEY_CPU, useCpu);
        editor.putBoolean(DrainForegroundService.KEY_GPU, useGpu);
        editor.putBoolean(DrainForegroundService.KEY_GPS, useLocation);
        editor.putBoolean(DrainForegroundService.KEY_WIFI, useWifi);
        editor.putBoolean(DrainForegroundService.KEY_BLUETOOTH, useBluetooth);
        editor.apply();
    }

    public static boolean[] getSwitchStates(Context context){
        boolean[] switchStates = new boolean[7];
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        switchStates[0] = sharedPreferences.getBoolean(DrainForegroundService.KEY_FLASH, false);
        switchStates[1] = sharedPreferences.getBoolean(DrainForegroundService.KEY_SCREEN, false);
        switchStates[2] = sharedPreferences.getBoolean(DrainForegroundService.KEY_CPU, false);
        switchStates[3] = sharedPreferences.getBoolean(DrainForegroundService.KEY_GPU, false);
        switchStates[4] = sharedPreferences.getBoolean(DrainForegroundService.KEY_GPS, false);
        switchStates[5] = sharedPreferences.getBoolean(DrainForegroundService.KEY_WIFI, false);
        switchStates[6] = sharedPreferences.getBoolean(DrainForegroundService.KEY_BLUETOOTH, false);
        return  switchStates;
    }

    public static int getTempLimit(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(KEY_SHUTOFF_TEMP, SettingsDialogFragment.MAX_SAFE_TEMP);
    }

    public static int getLevelLimit(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(KEY_SHUTOFF_LEVEL, 0);
    }

    public static void setTempLimit(Context context, int limit){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_SHUTOFF_TEMP, limit);
        editor.apply();
    }
    public static void setLevelLimit(Context context, int limit){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_SHUTOFF_LEVEL, limit);
        editor.apply();
    }

}
