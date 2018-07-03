package com.lohjason.genericbatterydrainer.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.lohjason.genericbatterydrainer.BuildConfig;
import com.lohjason.genericbatterydrainer.Logg;
import com.lohjason.genericbatterydrainer.MainActivity;
import com.lohjason.genericbatterydrainer.R;
import com.lohjason.genericbatterydrainer.managers.DrainManager;

/**
 * DrainForegroundService
 * Created by jason on 3/7/18.
 */
public class DrainForegroundService extends Service {

    private static final String LOG_TAG         = "+_DrnFgSvc";
    public static final  String ACTION_START    = BuildConfig.APPLICATION_ID + ".action_start";
    public static final  String ACTION_STOP     = BuildConfig.APPLICATION_ID + ".action_stop";
    public static final  String ACTION_ACTIVITY = BuildConfig.APPLICATION_ID + ".action_activity";
    public static final  int    ID_FG_SERVICE   = 7372;
    public static final  String ID_CHANNEL      = "646464";

    public static final String KEY_FLASH     = "key_flash";
    public static final String KEY_SCREEN    = "key_screen";
    public static final String KEY_CPU       = "key_cpu";
    public static final String KEY_BLUETOOTH = "key_bluetooth";
    public static final String KEY_GPS       = "key_location";
    public static final String KEY_GPU       = "key_gpu";
    public static final String KEY_WIFI      = "key_wifi";

    boolean flashOn     = false;
    boolean screenOn    = false;
    boolean cpuOn       = false;
    boolean bluetoothOn = false;
    boolean locationOn  = false;
    boolean gpuOn       = false;
    boolean wifiOn      = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action == null) {
            return START_NOT_STICKY;
        }

        if (action.equals(ACTION_START)) {
            setupNotification();
            startDraining(intent);

        } else if (action.equals(ACTION_STOP)) {
            Logg.d(LOG_TAG, "Foreground Svc Stopped");
            stopForeground(true);
            stopSelf();
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopDraining();
        super.onDestroy();
        Logg.d(LOG_TAG, "Service: onDestroy() called");
    }


    private void setupNotification() {
        Logg.d(LOG_TAG, "Got Start Foreground Intent");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(ACTION_ACTIVITY);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent stopIntent = new Intent(this, DrainForegroundService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, 0);

        Bitmap icon        = BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.ic_battery_alert_2);
        Bitmap emptyBitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
        Logg.d(LOG_TAG, "icon null?: " + (icon == null));
        if (icon == null) {
            icon = emptyBitmap;
        }
        NotificationChannel        notificationChannel = null;
        NotificationCompat.Builder notificationBuilder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(ID_CHANNEL, "DefaultChannel", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(notificationChannel);
            notificationBuilder = new NotificationCompat.Builder(this, ID_CHANNEL);
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }


        notificationBuilder.setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setSmallIcon(R.drawable.ic_battery_alert_black_24dp)
                .setContentTitle(getString(R.string.notification_title))
                .setTicker(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_message1))
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_cancel_2, getString(R.string.stop), pendingStopIntent)
                .setColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
                .setColorized(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        startForeground(ID_FG_SERVICE, notificationBuilder.build());
    }

    private void startDraining(Intent intent){
        flashOn     = intent.getBooleanExtra(KEY_FLASH, false);
        screenOn    = intent.getBooleanExtra(KEY_SCREEN, false);
        cpuOn       = intent.getBooleanExtra(KEY_CPU, false);
        locationOn  = intent.getBooleanExtra(KEY_GPS, false);
        wifiOn      = intent.getBooleanExtra(KEY_WIFI, false);
        bluetoothOn = intent.getBooleanExtra(KEY_BLUETOOTH, false);
        gpuOn       = intent.getBooleanExtra(KEY_GPU, false);

        DrainManager drainManager = DrainManager.getInstance(getApplication());
        drainManager.startDraining(flashOn, screenOn, cpuOn, locationOn, wifiOn, bluetoothOn);
    }

    private void stopDraining(){
        DrainManager drainManager = DrainManager.getInstance(getApplication());
        drainManager.stopDraining(flashOn, screenOn, cpuOn, locationOn, wifiOn, bluetoothOn);
    }

    //To Start - e.g. in some other activity
    //Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
    //startIntent.setAction(BuildConfig.CFG_START_FG_ACTION);
    //startService(startIntent);

    //To Stop
    //Intent stopIntent = new Intent(MainActivity.this, ForegroundService.class);
    //stopIntent.setAction(BuildConfig.CFG_STOP_FG_ACTION);
    //startService(stopIntent);

}
