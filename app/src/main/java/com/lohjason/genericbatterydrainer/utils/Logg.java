package com.lohjason.genericbatterydrainer.utils;

import android.util.Log;

import com.lohjason.genericbatterydrainer.BuildConfig;

/**
 * Logg
 * Created by jason on 2/7/18.
 */
public class Logg {

    private static boolean isEnabled = BuildConfig.DEBUG;

    public static void d(String tag, String message){
        Logg.d(tag, message, null);
    }
    public static void d(String tag, String message, Throwable t){
        if(isEnabled){
            Log.d(tag, message, t);
        }
    }

    public static void e(String tag, String message){
        Logg.e(tag, message, null);
    }
    public static void e(String tag, String message, Throwable t){
        if(isEnabled){
            Log.e(tag, message, t);
        }
    }

}
