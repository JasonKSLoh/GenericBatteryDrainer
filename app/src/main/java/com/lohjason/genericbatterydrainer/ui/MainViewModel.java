package com.lohjason.genericbatterydrainer.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.lohjason.genericbatterydrainer.models.BatteryInfo;

import java.util.Locale;

/**
 * MainViewModel
 * Created by jason on 12/7/18.
 */
public class MainViewModel extends ViewModel {

    private MutableLiveData<String[]> batteryInfoLiveData = new MutableLiveData<>();
    private float lastBatteryTempCelsius;
    private float lastBatteryLevel;

    public MainViewModel() {
        batteryInfoLiveData.setValue(null);
        lastBatteryTempCelsius = 0;
        lastBatteryLevel = 0;
    }

    public LiveData<String[]> getBatteryInfoLiveData() {
        return batteryInfoLiveData;
    }

    public void setBatteryInfo(BatteryInfo batteryInfo, boolean usesFahrenheit){
        int level           = batteryInfo.level;
        int scale           = batteryInfo.scale;
        int tempDeciCelcius = batteryInfo.temperature;
        int milliVoltage    = batteryInfo.voltage;

        float voltage      = milliVoltage / 1000f;
        float temp         = (float) tempDeciCelcius / 10f;
        float batteryLevel = level / (float) scale;
        lastBatteryTempCelsius = temp;
        lastBatteryLevel = batteryLevel * 100;

        String tempUnit = "°C";
        if (usesFahrenheit) {
            temp = (float) (temp * 1.8) + 32;
            tempUnit = "°F";
        }

        String tempString    = String.format(Locale.getDefault(), "%.1f%s", temp, tempUnit);
        String levelString   = String.format(Locale.getDefault(), "%.1f%%", batteryLevel * 100);
        String voltageString = voltage + "V";

        batteryInfoLiveData.postValue(new String[]{levelString, tempString, voltageString});
    }

    public void settingsUpdated(boolean usesFahrenheit){
        String[] batteryInfo = batteryInfoLiveData.getValue();
        if(batteryInfo == null || batteryInfo.length != 3){
            return;
        }

        String levelText;
        if (usesFahrenheit) {
            double level = (lastBatteryTempCelsius * 1.8) + 32;
            levelText = String.format(Locale.getDefault(), "%.1f%s", level, "°F");
        } else {
            levelText = String.format(Locale.getDefault(), "%.1f%s", lastBatteryTempCelsius, "°C");
        }
        batteryInfo[1] = levelText;
        batteryInfoLiveData.postValue(batteryInfo);
    }

    public float getLastBatteryLevel(){
        return lastBatteryLevel;
    }
}
