package com.lohjason.genericbatterydrainer.managers;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.PowerManager;

import com.lohjason.genericbatterydrainer.utils.Logg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.POWER_SERVICE;

/**
 * CpuManager
 * Created by jason on 2/7/18.
 */
public class CpuManager {
    private static final String LOG_TAG = "+_CpuMgr";
    private static CpuManager instance;
    private PowerManager.WakeLock wakeLock = null;

    private CpuManager() {
    }

    private AtomicBoolean       isComputing         = new AtomicBoolean(false);
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static CpuManager getInstance() {
        if (instance == null) {
            instance = new CpuManager();
        }
        return instance;
    }


    public void setComputationOn(boolean setOn) {
        compositeDisposable.clear();
        if (setOn) {
            Logg.d(LOG_TAG, "Set CPU Computation On");
            compositeDisposable.clear();
            isComputing.set(true);
            List<Single<Boolean>> cpuObservables = getTaxCpuObservableList();
            for (Single<Boolean> single : cpuObservables) {
                Disposable disposable = single.subscribe();
                compositeDisposable.add(disposable);
            }
        } else {
            Logg.d(LOG_TAG, "Set CPU Computation Off");
            isComputing.set(false);
            compositeDisposable.clear();
        }
    }

    private List<Single<Boolean>> getTaxCpuObservableList() {
        int                   numCores       = getNumCores();
        List<Single<Boolean>> observableList = new ArrayList<>();

        for (int i = 0; i < numCores; i++) {
            Single<Boolean> cpuSingle = Single.fromCallable(() -> {
                int newInt = 0;
                float newFloat = 0;
                double newDouble = 0;
                Random random = new Random();
                while (isComputing.get()) {
                    newInt = random.nextInt() * random.nextInt() / random.nextInt() / random.nextInt() ^ random.nextInt();
                    newFloat = random.nextFloat() * random.nextFloat() / random.nextFloat() * random.nextFloat();
                    newDouble = Math.pow(Math.sqrt(random.nextDouble()), Math.sqrt(random.nextDouble()));
                }
                return (newDouble - newFloat) > newInt;
            })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation());
            observableList.add(cpuSingle);
        }
        return observableList;
    }


    private int getNumCores() {
        int listedCores         = getListedNumCores();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int numCores            = listedCores > availableProcessors ? listedCores : availableProcessors;
        Logg.d(LOG_TAG, "Found " + numCores + " Cores");
        return numCores;
    }

    private int getListedNumCores() {
        try {
            File   dir   = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(pathname -> Pattern.matches("cpu[0-9]+", pathname.getName()));
            Logg.d("CpuMgr", "CPU Count: " + files.length);
            return files.length;
        } catch (Exception e) {
            Logg.d(LOG_TAG, e.getMessage(), e);
            return 1;
        }
    }

    @SuppressLint("WakelockTimeout")
    public void setWakelockOn(Application application, boolean wakelockOn) {
        Logg.d(LOG_TAG, "Set wakelock? " + wakelockOn);
        if (wakelockOn) {
            PowerManager powerManager = (PowerManager) application.getSystemService(POWER_SERVICE);
            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GenericBatteryDrainerWakelock");
                wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GenericBatteryDrainerWakelock");
                wakeLock.acquire();
            }
        } else {
            if (wakeLock != null) {
                wakeLock.release();
            }
        }
    }

}
