package com.lohjason.genericbatterydrainer.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;

import com.lohjason.genericbatterydrainer.BuildConfig;

/**
 * PermissionUtils
 * Created by jason on 2/7/18.
 */
public class PermissionUtils {

    public static final int REQUEST_CODE_CAMERA         = 101;
    public static final int REQUEST_CODE_WRITE_SETTINGS = 102;
    public static final int REQUEST_CODE_FINE_LOCATION  = 103;
    public static final int REQUEST_CODE_WIFI           = 104;
    public static final int REQUEST_CODE_BLUETOOTH      = 105;

    //region:: <Camera> ::
    public static boolean hasCameraPermission(Context context) {
        int hasPermission;
        if (Build.VERSION.SDK_INT < 23) {
            hasPermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.CAMERA);
        } else {
            hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        }
        return hasPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestCameraPermission(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
        }
    }

    public static boolean canRequestCameraPermission(AppCompatActivity appCompatActivity) {
        return Build.VERSION.SDK_INT >= 23
               && ActivityCompat.shouldShowRequestPermissionRationale(appCompatActivity, Manifest.permission.CAMERA);
    }
    //endregion

    //region:: <Write Settings> ::
    public static void requestWriteSettingsPermission(AppCompatActivity appCompatActivity) {
        if (Build.VERSION.SDK_INT >= 23) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
//            appCompatActivity.startActivity(intent);
            appCompatActivity.startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
        }
    }

    public static boolean hasWriteSettingsPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            return Settings.System.canWrite(context);
        } else {
            return PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_SETTINGS)
                   == PackageManager.PERMISSION_GRANTED;
        }
    }
    //endregion

    //region:: <Location> ::
    public static boolean hasLocationPermission(Context context) {
        int hasPermission;
        if (Build.VERSION.SDK_INT < 23) {
            hasPermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        }
        return hasPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_FINE_LOCATION);
        }
    }

    public static boolean canRequestLocationPermission(AppCompatActivity appCompatActivity) {
        return Build.VERSION.SDK_INT >= 23
               && ActivityCompat.shouldShowRequestPermissionRationale(appCompatActivity, Manifest.permission.ACCESS_FINE_LOCATION);
    }
    //endregion

    public static void openSettingsPage(AppCompatActivity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                   Uri.parse("package:" + BuildConfig.APPLICATION_ID));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
}
