package com.lohjason.genericbatterydrainer.managers;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.lohjason.genericbatterydrainer.utils.Logg;
import com.lohjason.genericbatterydrainer.utils.PermissionUtils;

/**
 * FineLocationManager
 * Created by jason on 3/7/18.
 */
public class FineLocationManager implements LocationListener {

    private static final String LOG_TAG = "+_FLocMgr";
    private static FineLocationManager instance;

    private FineLocationManager() {
    }

    public static FineLocationManager getInstance() {
        if (instance == null) {
            instance = new FineLocationManager();
        }
        return instance;
    }

    @SuppressLint("MissingPermission")
    public void setRequestingGpsOn(Application application, boolean setOn) {
        LocationManager locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null || !isLocationAvailable(application)) {
            Logg.d(LOG_TAG, "Location manager was null?:" + (locationManager == null) + " Or location was not available");
            return;
        }
        if (setOn && PermissionUtils.hasLocationPermission(application)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1200, 0, this);
            Logg.d(LOG_TAG, "Started requesting GPS updates");
        } else {
            locationManager.removeUpdates(this);
            Logg.d(LOG_TAG, "Location requests stopped");
        }
    }


    private boolean isLocationAvailable(Application application) {
        boolean locationEnabled = false;
        if (!PermissionUtils.hasLocationPermission(application)) {
            return false;
        }
        LocationManager manager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            return false;
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Logg.d(LOG_TAG, "GPS is enabled for use");
            locationEnabled = true;
        }
        return locationEnabled;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location location) {
        Logg.d(LOG_TAG, "Got location: " + location.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Logg.d(LOG_TAG, "Status changed: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Logg.d(LOG_TAG, "Provider Enabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Logg.d(LOG_TAG, "Provider Disabled: " + provider);
    }

}
