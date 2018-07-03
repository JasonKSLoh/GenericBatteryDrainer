package com.lohjason.genericbatterydrainer;

import android.app.Application;

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
    }



}
