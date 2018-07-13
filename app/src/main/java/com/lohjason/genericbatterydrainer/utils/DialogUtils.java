package com.lohjason.genericbatterydrainer.utils;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.widget.TextView;

import com.lohjason.genericbatterydrainer.R;
import com.lohjason.genericbatterydrainer.managers.BluetoothScanManager;
import com.lohjason.genericbatterydrainer.managers.WifiScanManager;

/**
 * DialogUtils
 * Created by jason on 12/7/18.
 */
public class DialogUtils {

    public static AlertDialog getOpenSettingsDialog(AppCompatActivity activity, int requestCode) {
        String permission = "";
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_CAMERA:
                permission = "The Camera Permission";
                break;
            case PermissionUtils.REQUEST_CODE_FINE_LOCATION:
                permission = "The Access Fine Location Permission";
                break;
            default:
                break;
        }
        String dialogMessage = permission + activity.getString(R.string.open_settings_message);

        return new AlertDialog.Builder(activity)
                .setTitle(R.string.permissions)
                .setMessage(dialogMessage)
                .setPositiveButton(R.string.go_to_settings,
                                   (dialog, which) -> {
                                       PermissionUtils.openSettingsPage(activity);
                                       dialog.dismiss();
                                   })
                .setNegativeButton(R.string.not_now,
                                   (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static  AlertDialog getAboutAppDialog(AppCompatActivity activity) {
        String  aboutThisAppRaw = activity.getString(R.string.about_app_text);
        Spanned aboutThisApp    = Html.fromHtml(aboutThisAppRaw);

        AlertDialog aboutDialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.about_this_app)
                .setMessage(aboutThisApp)
                .setCancelable(true)
                .setPositiveButton(R.string.dismiss, (dialog1, which) -> dialog1.dismiss())
                .show();
        TextView textView = aboutDialog.findViewById(android.R.id.message);
        if (textView != null) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            Linkify.addLinks(textView, Linkify.ALL);
        }
        return aboutDialog;
    }

    public static  AlertDialog showPermissionRationaleDialog(AppCompatActivity activity, int requestCode) {
        AlertDialog permissionRationaleDialog;
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_CAMERA: {
                permissionRationaleDialog = new AlertDialog.Builder(activity)
                        .setTitle(R.string.camera_permission_title)
                        .setMessage(R.string.camera_permission_message)
                        .setPositiveButton(R.string.ok,
                                           (dialog, which) -> {
                                               PermissionUtils.requestCameraPermission(activity);
                                               dialog.dismiss();
                                           })
                        .setNegativeButton(R.string.not_now,
                                           (dialog, which) -> dialog.dismiss())
                        .show();
                break;
            }
            case PermissionUtils.REQUEST_CODE_WRITE_SETTINGS: {
                permissionRationaleDialog = new AlertDialog.Builder(activity)
                        .setTitle(R.string.write_settings_permission_title)
                        .setMessage(R.string.write_settings_permission_message)
                        .setPositiveButton(R.string.ok,
                                           (dialog, which) -> {
                                               PermissionUtils.requestWriteSettingsPermission(activity);
                                               dialog.dismiss();
                                           })
                        .setNegativeButton(R.string.not_now,
                                           ((dialog, which) -> dialog.dismiss()))
                        .show();
                break;
            }
            case PermissionUtils.REQUEST_CODE_FINE_LOCATION: {
                permissionRationaleDialog = new AlertDialog.Builder(activity)
                        .setTitle(R.string.access_fine_location_permission_title)
                        .setMessage(R.string.access_fine_location_permission_message)
                        .setPositiveButton(R.string.ok,
                                           (dialog, which) -> {
                                               PermissionUtils.requestLocationPermission(activity);
                                               dialog.dismiss();
                                           })
                        .setNegativeButton(R.string.not_now,
                                           ((dialog, which) -> dialog.dismiss()))
                        .show();
                break;
            }
            case PermissionUtils.REQUEST_CODE_WIFI: {
                permissionRationaleDialog = new AlertDialog.Builder(activity)
                        .setTitle(R.string.wifi_on_title)
                        .setMessage(R.string.wifi_on_message)
                        .setPositiveButton(R.string.ok,
                                           (dialog, which) -> {
                                               WifiScanManager.getInstance()
                                                       .setWifiEnabled(activity.getApplication(), true);
                                               dialog.dismiss();
                                           })
                        .setNegativeButton(R.string.not_now,
                                           ((dialog, which) -> dialog.dismiss()))
                        .show();
                break;
            }
            case PermissionUtils.REQUEST_CODE_BLUETOOTH: {
                permissionRationaleDialog = new AlertDialog.Builder(activity)
                        .setTitle(R.string.bluetooth_on_title)
                        .setMessage(R.string.bluetooth_on_message)
                        .setPositiveButton(R.string.ok,
                                           (dialog, which) -> {
                                               BluetoothScanManager.getInstance().enableBluetooth(activity);
                                               dialog.dismiss();
                                           })
                        .setNegativeButton(R.string.not_now,
                                           ((dialog, which) -> dialog.dismiss()))
                        .show();
                break;
            }
            default:
                permissionRationaleDialog = null;
                break;
        }
        if(permissionRationaleDialog != null){
            TextView textView = permissionRationaleDialog.findViewById(android.R.id.message);
            if (textView != null) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            }
        }
        return permissionRationaleDialog;
    }
}
