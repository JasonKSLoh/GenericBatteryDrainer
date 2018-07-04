package com.lohjason.genericbatterydrainer.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.lohjason.genericbatterydrainer.utils.Logg;

import java.io.IOException;

/**
 * FlashService
 * Created by jason on 2/7/18.
 */
public class FlashService extends Service {
    private static final String LOG_TAG = "+_FshSvc";
    private Camera camera;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logg.d(LOG_TAG, "Flash service onstartcommand");
        if (Build.VERSION.SDK_INT >= 23) {
            setFlashOn23(true);
        } else {
            setFlashOn(true);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Logg.d(LOG_TAG, "Flash service ondestroy");
        if (Build.VERSION.SDK_INT >= 23) {
            setFlashOn23(false);
        } else {
            setFlashOn(false);
        }
        super.onDestroy();
    }

    @RequiresApi(23)
    private void setFlashOn23(boolean setOn) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if(cameraManager == null){
            return;
        }
        if(setOn){
            try {
                for(String cameraId: cameraManager.getCameraIdList())
                    try{
                        cameraManager.setTorchMode(cameraId, true);
                    } catch (IllegalArgumentException iae){
                        Logg.d(LOG_TAG, iae.getMessage());
                    }
            } catch (CameraAccessException cae) {
                Logg.d(LOG_TAG, cae.getMessage());
            }
        } else {
            try {
                for(String cameraId: cameraManager.getCameraIdList())
                    try {
                        cameraManager.setTorchMode(cameraId, false);
                    } catch (IllegalArgumentException iae){
                        Logg.d(LOG_TAG, iae.getMessage());
                    }
            } catch (CameraAccessException cae) {
                Logg.d(LOG_TAG, cae.getMessage());
            }
        }

    }

    private void setFlashOn(boolean setOn) {
        String offMode   = Camera.Parameters.FLASH_MODE_OFF;
        String torchMode = Camera.Parameters.FLASH_MODE_TORCH;
        if (setOn) {
            camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            if (parameters.getSupportedFlashModes().contains(torchMode)) {
                parameters.setFlashMode(torchMode);
                camera.setParameters(parameters);
            }
            try {
                camera.setPreviewTexture(new SurfaceTexture(0));
            } catch (IOException ignored) {
            }
            camera.startPreview();
        } else {
            if (camera == null) {
                return;
            }
            Camera.Parameters parameters = camera.getParameters();
            if (parameters.getSupportedFlashModes().contains(offMode)) {
                parameters.setFlashMode(offMode);
                camera.setParameters(parameters);
            }
            camera.stopPreview();
            camera.release();
        }
    }
}
