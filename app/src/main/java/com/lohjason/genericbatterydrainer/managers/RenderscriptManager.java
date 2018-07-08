package com.lohjason.genericbatterydrainer.managers;

import android.app.Application;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.support.v8.renderscript.Type;

import com.lohjason.genericbatterydrainer.ScriptC_matmul;
import com.lohjason.genericbatterydrainer.utils.Logg;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * RenderscriptManager
 * Created by jason on 4/7/18.
 */
public class RenderscriptManager {

    private static final String LOG_TAG = "+_RndMgr";
    private static RenderscriptManager instance;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private AtomicBoolean       isComputing         = new AtomicBoolean(false);
    private Thread thread;

    public static RenderscriptManager getInstance() {
        if (instance == null) {
            instance = new RenderscriptManager();
        }
        return instance;
    }

    private RenderscriptManager() {
    }

    private Bitmap generateRandomBitmap(int width, int height) {
        Random random      = new Random();
        byte[] randomBytes = new byte[width * height * 4];
        random.nextBytes(randomBytes);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        ByteBuffer byteBuffer = ByteBuffer.wrap(randomBytes);
        bitmap.copyPixelsFromBuffer(byteBuffer);
        return bitmap;
    }

    public void setRenderingOn(Application application, boolean setOn) {
        if (setOn) {
            if (isComputing.get()) {
                return;
            }
            isComputing.set(true);
            Disposable disposable = Single.fromCallable(() -> {
                RenderScript        renderScript   = RenderScript.create(application);
                Bitmap              bitmap         = generateRandomBitmap(1000, 1000);
                Allocation          allocation     = Allocation.createFromBitmap(renderScript, bitmap);
                Type                type           = allocation.getType();
                Allocation          blurAllocation = Allocation.createTyped(renderScript, type);
                ScriptIntrinsicBlur blurScript     = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
                blurScript.setRadius(25f);
                blurScript.setInput(allocation);
                while (isComputing.get()) {
                    for (int i = 0; i < 5; i++) {
                        blurScript.forEach(blurAllocation);
                    }
                    renderScript.finish();
                }
                allocation.destroy();
                blurAllocation.destroy();
                blurScript.destroy();
                type.destroy();
                renderScript.destroy();
                Logg.d(LOG_TAG, "Renderscript destroyed");
                Runtime.getRuntime().gc();
                return true;
            })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.newThread())
                    .subscribe();
            compositeDisposable.add(disposable);
        } else {
            isComputing.set(false);
            compositeDisposable.clear();
            Logg.d(LOG_TAG, "Set rendering: false");
        }
    }


    public void setMatMulOn(Application context, boolean setOn) {
        if(setOn){
            if(isComputing.get()){
                return;
            }
            isComputing.set(true);
            thread = new Thread( () -> {
                RenderScript renderScript = RenderScript.create(context);
                int          matrixWidth  = 400;
                int          matrixSize   = matrixWidth * matrixWidth;
                float[]      matrixA      = new float[matrixSize];
                float[]      matrixB      = new float[matrixSize];
                int[]        posRow       = new int[matrixWidth];
                int[]        nSize        = new int[1];
                int[]        kSize        = new int[1];
                Random       random       = new Random();

                for (int i = 0; i < matrixSize; i++) {
                    matrixA[i] = random.nextFloat();
                    matrixB[i] = random.nextFloat();
                }
                for (int i = 0; i < matrixWidth; i++) {
                    posRow[i] = i;
                }
                nSize[0] = matrixWidth;
                kSize[0] = matrixWidth;

                Allocation allocationA = Allocation.createSized(renderScript, Element.F32(renderScript), matrixSize);
                Allocation allocationB = Allocation.createSized(renderScript, Element.F32(renderScript), matrixSize);
                Allocation allocationC = Allocation.createSized(renderScript, Element.F32(renderScript), matrixSize);

                Allocation allocationNSize  = Allocation.createSized(renderScript, Element.I32(renderScript), 1);
                Allocation allocationKSize  = Allocation.createSized(renderScript, Element.I32(renderScript), 1);
                Allocation allocationPosRow = Allocation.createSized(renderScript, Element.I32(renderScript), matrixWidth);

                allocationA.copyFrom(matrixA);
                allocationB.copyFrom(matrixB);
                allocationPosRow.copyFrom(posRow);
                allocationNSize.copyFrom(nSize);
                allocationKSize.copyFrom(kSize);

                ScriptC_matmul script = new ScriptC_matmul(renderScript);
                script.bind_matA(allocationA);
                script.bind_matB(allocationB);
                script.bind_outMatrix(allocationC);
                script.bind_nSize(allocationNSize);
                script.bind_kSize(allocationKSize);

                while (isComputing.get()) {
                    script.forEach_root(allocationPosRow, allocationPosRow);
                    renderScript.finish();
                }

                allocationA.destroy();
                allocationB.destroy();
                allocationC.destroy();
                allocationNSize.destroy();
                allocationKSize.destroy();
                allocationPosRow.destroy();
                script.destroy();
                renderScript.destroy();
                Logg.d(LOG_TAG, "Renderscript destroyed");
                Runtime.getRuntime().gc();
            });
            thread.start();

        } else {
            isComputing.set(false);
            Logg.d(LOG_TAG, "Stopped Matmul");
        }
    }
}
