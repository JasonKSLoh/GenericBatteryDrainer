package com.lohjason.genericbatterydrainer.managers;

import android.app.Application;
import android.os.PowerManager;

import com.lohjason.genericbatterydrainer.utils.Logg;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

    private static final String regexPart1 = "^(-9223372036854775808|0)$|^((-?)((?!0)\\d{1,18}|[1-8]"
                                             + "\\d{18}|9[0-1]\\d{17}|92[0-1]\\d{16}|922[0-2]\\d{15}"
                                             + "|9223[0-2]\\d{14}|92233[0-6]\\d{13}|922337[0-1]\\d{12}"
                                             + "|92233720[0-2]\\d{10}|922337203[0-5]\\d{9}|9223372036[0-7]"
                                             + "\\d{8}|92233720368[0-4]\\d{7}|922337203685[0-3]\\d{6}"
                                             + "|9223372036854[0-6]\\d{5}|92233720368547[0-6]\\d{4}"
                                             + "|922337203685477[0-4]\\d{3}|9223372036854775[0-7]"
                                             + "\\d{2}|922337203685477580[0-7]))$";
    private static final String regexPart2 = "([\\w\\s]+)([+\\-/*]+)([\\w\\s]+)([+\\-/*]+)([\\w\\s]+)";

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

    @SuppressWarnings("InfiniteLoopStatement")
    private List<Single<Boolean>> getTaxCpuObservableList() {
        int                   numCores       = getNumCores();
        List<Single<Boolean>> observableList = new ArrayList<>();

        for (int i = 0; i < numCores; i++) {
            Single<Boolean> cpuSingle = Single.fromCallable(() -> {
                boolean       matches = false;
                StringBuilder sb      = new StringBuilder();
                for (int j = 0; j < 10; j++) {
                    sb.append(new SimpleDateFormat("Y-YY-Y-M-M-d-d-H-H-m-m-s-s-S-S-S", Locale.getDefault()).format(new Date()));
                }
                String initialText = sb.toString();
                String regex = regexPart2 + regexPart1 + regexPart2 + regexPart1;
                Pattern pattern;
                String textToMatch;

                while (isComputing.get()) {
                    pattern     = Pattern.compile(regex);
                    textToMatch = initialText + new SimpleDateFormat("Y-YY-Y-M-M-d-d-H-H-m-m-s-s-S-S-S", Locale.getDefault()).format(new Date());
                    matches = pattern.matcher(textToMatch).matches();
                }
                return matches;
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
