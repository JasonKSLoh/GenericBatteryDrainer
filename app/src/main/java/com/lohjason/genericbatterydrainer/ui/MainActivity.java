package com.lohjason.genericbatterydrainer.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.widget.TextView;

import com.lohjason.genericbatterydrainer.R;
import com.lohjason.genericbatterydrainer.managers.BluetoothScanManager;
import com.lohjason.genericbatterydrainer.managers.DrainManager;
import com.lohjason.genericbatterydrainer.managers.WifiScanManager;
import com.lohjason.genericbatterydrainer.services.DrainForegroundService;
import com.lohjason.genericbatterydrainer.utils.PermissionUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "+_ManAtv";
    private SwitchCompat switchFlash;
    private SwitchCompat switchCpu;
    private SwitchCompat switchGpu;
    private SwitchCompat switchScreen;
    private SwitchCompat switchGps;
    private SwitchCompat switchWifi;
    private SwitchCompat switchBluetooth;
    private TextView     btnStart;
    private Disposable   isDrainingDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
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
            if (btnStart.getText().toString().equalsIgnoreCase(getString(R.string.start))) {
                startDraining();
            } else {
                stopDraining();
            }
        });
        setupSwitchStates();
    }


    private void setupSwitchStates() {
        if (PermissionUtils.hasCameraPermission(this)) {
            switchFlash.setChecked(true);
        }
        if (PermissionUtils.hasWriteSettingsPermission(this)) {
            switchScreen.setChecked(true);
        }
        switchCpu.setChecked(true);
        switchGpu.setChecked(true);
        if (PermissionUtils.hasLocationPermission(this)) {
            switchGps.setChecked(true);
        }
        if (WifiScanManager.getInstance().isWifiScanEnabled(getApplication())) {
            switchWifi.setChecked(true);
        }
        if (BluetoothScanManager.getInstance().isBluetoothEnabled()) {
            switchBluetooth.setChecked(true);
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

    private void setStartDrainingButtonState(boolean isNowDraining) {
        if (isNowDraining) {
            btnStart.setText(R.string.stop);
        } else {
            btnStart.setText(R.string.start);
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

        startService(startIntent);
    }

    private void stopDraining() {
        Intent stopIntent = new Intent(MainActivity.this, DrainForegroundService.class);
        stopIntent.setAction(DrainForegroundService.ACTION_STOP);
        startService(stopIntent);

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

    private void showOpenSettingsDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Permissions")
                .setMessage("The Camera Permission is needed in order to turn on the flashlight\n"
                            + "You can go to the App settings page to turn it on.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    PermissionUtils.openSettingsPage(MainActivity.this);
                    dialog.dismiss();
                }).setNegativeButton(getString(R.string.not_now), (dialog, which) -> {
            dialog.dismiss();
        })
                .show();
    }

    private void showPermissionRationaleDialog(int requestCode) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_CAMERA: {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.camera_permission_title)
                        .setMessage(R.string.camera_permission_message)
                        .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                            PermissionUtils.requestCameraPermission(MainActivity.this);
                            dialog.dismiss();
                        })
                        .setNegativeButton(getString(R.string.ok), (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
                break;
            }
            case PermissionUtils.REQUEST_CODE_WRITE_SETTINGS: {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.write_settings_permission_title)
                        .setMessage(R.string.write_settings_permission_message)
                        .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                            PermissionUtils.requestWriteSettingsPermission(MainActivity.this);
                            dialog.dismiss();
                        })
                        .setNegativeButton(getString(R.string.not_now), ((dialog, which) -> {
                            dialog.dismiss();
                        }))
                        .show();
                break;
            }
            case PermissionUtils.REQUEST_CODE_FINE_LOCATION: {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.access_fine_location_permission_title)
                        .setMessage(R.string.access_fine_location_permission_message)
                        .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                            PermissionUtils.requestLocationPermission(MainActivity.this);
                            dialog.dismiss();
                        })
                        .setNegativeButton(getString(R.string.not_now), ((dialog, which) -> {
                            dialog.dismiss();
                        }))
                        .show();
                break;
            }
            case PermissionUtils.REQUEST_CODE_WIFI: {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.wifi_on_title)
                        .setMessage(R.string.wifi_on_message)
                        .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                            WifiScanManager.getInstance().setWifiEnabled(getApplication(), true);
                            dialog.dismiss();
                        })
                        .setNegativeButton(getString(R.string.not_now), ((dialog, which) -> {
                            dialog.dismiss();
                        }))
                        .show();
            }
            case PermissionUtils.REQUEST_CODE_BLUETOOTH: {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.bluetooth_on_title)
                        .setMessage(R.string.bluetooth_on_message)
                        .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                            BluetoothScanManager.getInstance().enableBluetooth(this);
                            dialog.dismiss();
                        })
                        .setNegativeButton(getString(R.string.not_now), ((dialog, which) -> {
                            dialog.dismiss();
                        }))
                        .show();
            }
            default:
                break;
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
    protected void onDestroy() {
        if(isDrainingDisposable != null){
            isDrainingDisposable.dispose();
            isDrainingDisposable = null;
        }
        super.onDestroy();
    }

}
