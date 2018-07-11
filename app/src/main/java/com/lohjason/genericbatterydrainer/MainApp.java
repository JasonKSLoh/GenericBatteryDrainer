package com.lohjason.genericbatterydrainer;

import android.app.Application;

import com.lohjason.genericbatterydrainer.utils.Logg;
import com.lohjason.genericbatterydrainer.utils.SharedPrefsUtils;

import io.reactivex.plugins.RxJavaPlugins;

/**
 * MainApp
 * Created by jason on 2/7/18.
 */
public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RxJavaPlugins.setErrorHandler(e -> {
            Logg.e("RxJavaError", e.getMessage(), e);
        });
        if(!SharedPrefsUtils.getHasInitialized(this)){
            initializeSettings();
        }
    }

    private void initializeSettings(){
        SharedPrefsUtils.setUsesFahrenheit(this, false);
        SharedPrefsUtils.setTempLimit(this, 50);
        SharedPrefsUtils.setLevelLimit(this, 0);
        SharedPrefsUtils.setSwitchStates(this,
                                         false,
                                         false,
                                         true,
                                         true,
                                         false,
                                         false,
                                         false);
        SharedPrefsUtils.setHasInitialized(this);
    }


}
