package com.lohjason.genericbatterydrainer.models;

/**
 * BatteryInfo
 * Created by jason on 12/7/18.
 */
public class BatteryInfo {

    public int level;
    public int voltage;
    public int temperature;
    public int scale;

    public BatteryInfo(int level, int voltage, int temperature, int scale) {
        this.level = level;
        this.voltage = voltage;
        this.temperature = temperature;
        this.scale = scale;
    }
}
