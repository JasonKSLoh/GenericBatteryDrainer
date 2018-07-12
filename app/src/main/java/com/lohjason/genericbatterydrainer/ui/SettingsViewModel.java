package com.lohjason.genericbatterydrainer.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

/**
 * SettingsViewModel
 * Created by jason on 12/7/18.
 */
public class SettingsViewModel extends ViewModel {

    private MutableLiveData<Boolean> usesFahrenheitLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> batteryLevelProgressLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> batteryTempProgressLiveData = new MutableLiveData<>();

    public SettingsViewModel() {
        usesFahrenheitLiveData.setValue(null);
        batteryLevelProgressLiveData.setValue(null);
        batteryTempProgressLiveData.setValue(null);
    }

    public LiveData<Boolean> getUsesFahrenheitLiveData() {
        return usesFahrenheitLiveData;
    }

    public LiveData<Integer> getBatteryLevelProgressLiveData() {
        return batteryLevelProgressLiveData;
    }

    public LiveData<Integer> getBatteryTempProgressLiveData() {
        return batteryTempProgressLiveData;
    }

    public void setUsesFahrenheit(boolean usesFahrenheit){
        usesFahrenheitLiveData.postValue(usesFahrenheit);
    }
    public void setBatteryLevelProgress(int batteryLevelProgress, float lastBatteryLevel){
        float targetLevel = (float)batteryLevelProgress;
        if(targetLevel >= lastBatteryLevel){
            float newTarget = lastBatteryLevel > 1 ? lastBatteryLevel - 1 : 0;
            int closestValue = (int)newTarget;
            batteryLevelProgressLiveData.postValue(closestValue);
        } else {
            batteryLevelProgressLiveData.postValue(batteryLevelProgress);
        }
    }
    public void setBatteryTempProgress(int batteryTempProgress){
        batteryTempProgressLiveData.postValue(batteryTempProgress);
    }
}
