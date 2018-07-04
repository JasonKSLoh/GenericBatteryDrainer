package com.lohjason.genericbatterydrainer.utils;

import android.util.Log;

/**
 * Logg
 * Created by jason on 2/7/18.
 */
public class Logg {

    public static void d(String tag, String message, Throwable t){
        Log.d(tag, message, t);
    }
    public static void d(String tag, String message){
        Log.d(tag, message);
    }
    public static void e(String tag, String message, Throwable t){
        Log.e(tag, message, t);
    }
    public static void e(String tag, String message){
        Log.e(tag, message);
    }

}
