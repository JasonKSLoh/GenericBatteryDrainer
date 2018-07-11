package com.lohjason.genericbatterydrainer.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lohjason.genericbatterydrainer.R;
import com.lohjason.genericbatterydrainer.managers.BluetoothScanManager;
import com.lohjason.genericbatterydrainer.managers.DrainManager;
import com.lohjason.genericbatterydrainer.managers.WifiScanManager;
import com.lohjason.genericbatterydrainer.services.DrainForegroundService;
import com.lohjason.genericbatterydrainer.utils.PermissionUtils;
import com.lohjason.genericbatterydrainer.utils.SharedPrefsUtils;

import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements SettingsDialogFragment.SettingsChangedListener {

    private static final String LOG_TAG = "+_ManAtv";
    private SwitchCompat      switchFlash;
    private SwitchCompat      switchCpu;
    private SwitchCompat      switchGpu;
    private SwitchCompat      switchScreen;
    private SwitchCompat      switchGps;
    private SwitchCompat      switchWifi;
    private SwitchCompat      switchBluetooth;
    private TextView          btnStart;
    private ImageView         ivAboutApp;
    private ImageView         ivSettings;
    private Disposable        isDrainingDisposable;
    private TextView          tvBattLevel;
    private TextView          tvVoltage;
    private TextView          tvBattTemp;
    private BroadcastReceiver batteryLevelReceiver;

    private AlertDialog aboutDialog;
    private AlertDialog openSettingsDialog;
    private AlertDialog permissionRationaleDialog;

    private float lastBatteryTempCelcius = 0f;
    public float lastBatteryLevel = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
        setupSwitchStates();
        setupObservables();
    }

    private void setupViews() {
        switchFlash = findViewById(R.id.switch_flash);
        switchScreen = findViewById(R.id.switch_screen);
        switchCpu = findViewById(R.id.switch_cpu);
        switchGpu = findViewById(R.id.switch_gpu);
        switchGps = findViewById(R.id.switch_gps);
        switchWifi = findViewById(R.id.switch_wifi);
        switchBluetooth = findViewById(R.id.switch_bluetooth);
        btnStart = findViewById(R.id.tv_start);
        ivAboutApp = findViewById(R.id.iv_about);
        ivSettings = findViewById(R.id.iv_settings);
        tvBattLevel = findViewById(R.id.tv_battery_percentage);
        tvBattTemp = findViewById(R.id.tv_battery_temp);
        tvVoltage = findViewById(R.id.tv_battery_voltage);

        switchFlash.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!PermissionUtils.hasCameraPermission(MainActivity.this)) {
                    switchFlash.setChecked(false);
                    showPermissionRationaleDialog(PermissionUtils.REQUEST_CODE_CAMERA);
                }
            }
        });

        switchScreen.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                if (!PermissionUtils.hasWriteSettingsPermission(MainActivity.this)) {
                    switchScreen.setChecked(false);
                    showPermissionRationaleDialog(PermissionUtils.REQUEST_CODE_WRITE_SETTINGS);
                }
            }
        }));

        switchGps.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                if (!PermissionUtils.hasLocationPermission(MainActivity.this)) {
                    switchGps.setChecked(false);
                    showPermissionRationaleDialog(PermissionUtils.REQUEST_CODE_FINE_LOCATION);
                }
            }
        }));

        switchWifi.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                if (!WifiScanManager.getInstance().isWifiScanEnabled(getApplication())) {
                    switchWifi.setChecked(false);
                    showPermissionRationaleDialog(PermissionUtils.REQUEST_CODE_WIFI);
                }
            }
        }));

        switchBluetooth.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                if (!BluetoothScanManager.getInstance().isBluetoothEnabled()) {
                    switchBluetooth.setChecked(false);
//                    showPermissionRationaleDialog(PermissionUtils.REQUEST_CODE_BLUETOOTH);
                    BluetoothScanManager.getInstance().enableBluetooth(this);
                }
            }
        }));

        btnStart.setOnClickListener(v -> {
            DrainManager drainManager = DrainManager.getInstance(getApplication());
            if (drainManager.isDraining()) {
                stopDraining();
            } else {
                startDraining();
            }

        });


        ivAboutApp.setOnClickListener(v -> showAboutAppDialog());

        ivSettings.setOnClickListener(v -> showSettingsDialogFragment());
    }

    private void setupSwitchStates() {
        boolean[] switchStates = SharedPrefsUtils.getSwitchStates(this);
        boolean   flashOn      = switchStates[0];
        boolean   screenOn     = switchStates[1];
        boolean   cpuOn        = switchStates[2];
        boolean   gpuOn        = switchStates[3];
        boolean   locationOn   = switchStates[4];
        boolean   wifiOn       = switchStates[5];
        boolean   bluetoothOn  = switchStates[6];

        if (PermissionUtils.hasCameraPermission(this)) {
            switchFlash.setChecked(flashOn);
        }
        if (PermissionUtils.hasWriteSettingsPermission(this)) {
            switchScreen.setChecked(screenOn);
        }
        switchCpu.setChecked(cpuOn);
        switchGpu.setChecked(gpuOn);
        if (PermissionUtils.hasLocationPermission(this)) {
            switchGps.setChecked(locationOn);
        }
        if (WifiScanManager.getInstance().isWifiScanEnabled(getApplication())) {
            switchWifi.setChecked(wifiOn);
        }
        if (BluetoothScanManager.getInstance().isBluetoothEnabled()) {
            switchBluetooth.setChecked(bluetoothOn);
        }
    }

    private void setupObservables() {
        isDrainingDisposable = DrainManager.getInstance(getApplication())
                .getDrainBehaviorSubject()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(b -> {
                    if (b == null) {
                        return;
                    }
                    setStartDrainingButtonState(b);
                });
    }

    private void setupBatteryLevelReceiver() {
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryLevelReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level           = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale           = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int tempDeciCelcius = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
                int milliVoltage    = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

                float voltage      = milliVoltage / 1000f;
                float temp         = (float) tempDeciCelcius / 10f;
                float batteryLevel = level / (float) scale;
                lastBatteryTempCelcius = temp;
                lastBatteryLevel = batteryLevel * 100;

                String tempUnit = "°C";
                if (SharedPrefsUtils.getUsesFahrenheit(getApplicationContext())) {
                    temp = (float) (temp * 1.8) + 32;
                    tempUnit = "°F";
                }

                String tempString    = String.format(Locale.getDefault(), "%.1f%s", temp, tempUnit);
                String levelString   = String.format(Locale.getDefault(), "%.1f%%", batteryLevel * 100);
                String voltageString = voltage + "V";

                tvVoltage.setText(voltageString);
                tvBattTemp.setText(tempString);
                tvBattLevel.setText(levelString);
            }
        };

        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }

    private void setStartDrainingButtonState(boolean isNowDraining) {
        if (isNowDraining) {
            btnStart.setText(R.string.stop);
            btnStart.setBackgroundResource(R.drawable.rounded_shape_red);
            btnStart.setTextColor(ContextCompat.getColor(this,R.color.material_red_400));
        } else {
            btnStart.setText(R.string.start);
            btnStart.setBackgroundResource(R.drawable.rounded_shape_green);
            btnStart.setTextColor(ContextCompat.getColor(this,R.color.material_green_400));
        }
    }

    private void startDraining() {
        Intent startIntent = new Intent(MainActivity.this, DrainForegroundService.class);
        startIntent.setAction(DrainForegroundService.ACTION_START);

        startIntent.putExtra(DrainForegroundService.KEY_FLASH, switchFlash.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_SCREEN, switchScreen.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_CPU, switchCpu.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_GPU, switchGpu.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_GPS, switchGps.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_WIFI, switchWifi.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_BLUETOOTH, switchBluetooth.isChecked());

        Toast.makeText(this, "Drain Started!", Toast.LENGTH_SHORT).show();
        saveSwitchStates();
        startService(startIntent);
    }

    private void stopDraining() {
        Intent stopIntent = new Intent(MainActivity.this, DrainForegroundService.class);
        stopIntent.setAction(DrainForegroundService.ACTION_STOP);
        startService(stopIntent);

    }

    private void showSettingsDialogFragment() {
        SettingsDialogFragment fragment = SettingsDialogFragment.getNewInstance(this);
        fragment.show(getSupportFragmentManager(), "SETTINGS_FRAGMENT");
    }

    private void showOpenSettingsDialog() {
        if (openSettingsDialog != null) {
            openSettingsDialog.dismiss();
        }
        openSettingsDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Permissions")
                .setMessage("The Camera Permission is needed in order to turn on the flashlight\n"
                            + "You can go to the App settings page to turn it on.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    PermissionUtils.openSettingsPage(MainActivity.this);
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.not_now), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showAboutAppDialog() {
        String aboutThisAppRaw = getString(R.string.about_app_text);
        Spanned aboutThisApp = Html.fromHtml(aboutThisAppRaw);

        if (aboutDialog != null) {
            aboutDialog.dismiss();
        }
        aboutDialog = new AlertDialog.Builder(this)
                .setTitle("About this App")
                .setMessage(aboutThisApp)
                .setCancelable(true)
                .setPositiveButton("Dismiss", (dialog1, which) -> dialog1.dismiss())
                .show();
        TextView textView = aboutDialog.findViewById(android.R.id.message);
        if (textView != null) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        }
    }

    private void showPermissionRationaleDialog(int requestCode) {
        if (permissionRationaleDialog != null) {
            permissionRationaleDialog.dismiss();
        }
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_CAMERA: {
                permissionRationaleDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.camera_permission_title)
                        .setMessage(R.string.camera_permission_message)
                        .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                            PermissionUtils.requestCameraPermission(MainActivity.this);
                            dialog.dismiss();
                        })
                        .setNegativeButton(getString(R.string.not_now), (dialog, which) -> dialog.dismiss())
                        .show();
                break;
            }
            case PermissionUtils.REQUEST_CODE_WRITE_SETTINGS: {
                permissionRationaleDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.write_settings_permission_title)
                        .setMessage(R.string.write_settings_permission_message)
                        .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                            PermissionUtils.requestWriteSettingsPermission(MainActivity.this);
                            dialog.dismiss();
                        })
                        .setNegativeButton(getString(R.string.not_now), ((dialog, which) -> dialog.dismiss()))
                        .show();
                break;
            }
            case PermissionUtils.REQUEST_CODE_FINE_LOCATION: {
                permissionRationaleDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.access_fine_location_permission_title)
                        .setMessage(R.string.access_fine_location_permission_message)
                        .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                            PermissionUtils.requestLocationPermission(MainActivity.this);
                            dialog.dismiss();
                        })
                        .setNegativeButton(getString(R.string.not_now), ((dialog, which) -> dialog.dismiss()))
                        .show();
                break;
            }
            case PermissionUtils.REQUEST_CODE_WIFI: {
                permissionRationaleDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.wifi_on_title)
                        .setMessage(R.string.wifi_on_message)
                        .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                            WifiScanManager.getInstance().setWifiEnabled(getApplication(), true);
                            dialog.dismiss();
                        })
                        .setNegativeButton(getString(R.string.not_now), ((dialog, which) -> dialog.dismiss()))
                        .show();
            }
            case PermissionUtils.REQUEST_CODE_BLUETOOTH: {
                permissionRationaleDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.bluetooth_on_title)
                        .setMessage(R.string.bluetooth_on_message)
                        .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                            BluetoothScanManager.getInstance().enableBluetooth(this);
                            dialog.dismiss();
                        })
                        .setNegativeButton(getString(R.string.not_now), ((dialog, which) -> dialog.dismiss()))
                        .show();
            }
            default:
                break;
        }
    }

    private void saveSwitchStates() {
        SharedPrefsUtils.setSwitchStates(this,
                                         switchFlash.isChecked(),
                                         switchScreen.isChecked(),
                                         switchCpu.isChecked(),
                                         switchGpu.isChecked(),
                                         switchGps.isChecked(),
                                         switchWifi.isChecked(),
                                         switchBluetooth.isChecked());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        saveSwitchStates();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_CAMERA: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        switchFlash.setChecked(true);
                    } else {
                        if (PermissionUtils.canRequestCameraPermission(MainActivity.this)) {
                            showPermissionRationaleDialog(requestCode);
                        } else {
                            showOpenSettingsDialog();
                        }
                    }
                }
                break;
            }
            case PermissionUtils.REQUEST_CODE_FINE_LOCATION: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        switchGps.setChecked(true);
                    } else {
                        if (PermissionUtils.canRequestLocationPermission(MainActivity.this)) {
                            showPermissionRationaleDialog(requestCode);
                        } else {
                            showOpenSettingsDialog();
                        }
                    }
                }
                break;
            }
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_BLUETOOTH: {
                if (resultCode == Activity.RESULT_OK) {
                    switchBluetooth.setChecked(true);
                } else {
                    showPermissionRationaleDialog(requestCode);
                }
                break;
            }
            default: {
                onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupBatteryLevelReceiver();
    }

    @Override
    protected void onPause() {
        if (batteryLevelReceiver != null) {
            unregisterReceiver(batteryLevelReceiver);
        }
        if (openSettingsDialog != null && openSettingsDialog.isShowing()) {
            openSettingsDialog.dismiss();
        }
        if (aboutDialog != null && aboutDialog.isShowing()) {
            aboutDialog.dismiss();
        }
        if (permissionRationaleDialog != null && permissionRationaleDialog.isShowing()) {
            permissionRationaleDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (isDrainingDisposable != null) {
            isDrainingDisposable.dispose();
            isDrainingDisposable = null;
        }
        super.onDestroy();
    }

    @Override
    public void appSettingsChanged(boolean usesFahrenheit) {
        String levelText;
        if (usesFahrenheit) {
            double level = (lastBatteryTempCelcius * 1.8) + 32;
            levelText = String.format(Locale.getDefault(), "%.1f%s", level, "°F");
        } else {
            levelText = String.format(Locale.getDefault(), "%.1f%s", lastBatteryTempCelcius, "°C");
        }
        tvBattTemp.setText(levelText);
    }
}
