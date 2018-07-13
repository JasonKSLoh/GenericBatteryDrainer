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
        applySettings();
    }

    private void applySettings(){
        if(!SharedPrefsUtils.getHasInitialized(this)){
            initializeSettings();
        }
        if (SharedPrefsUtils.getResetLevelOnRestart(this)){
            SharedPrefsUtils.setLevelLimit(this, 0);
        }
    }

    private void initializeSettings(){
        SharedPrefsUtils.setTempLimit(this, 50);
        SharedPrefsUtils.setLevelLimit(this, 0);
        SharedPrefsUtils.setUsesFahrenheit(this, false);
        SharedPrefsUtils.setResetLevelOnRestart(this, false);
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
