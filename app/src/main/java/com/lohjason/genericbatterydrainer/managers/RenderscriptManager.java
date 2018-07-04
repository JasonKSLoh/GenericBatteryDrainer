package com.lohjason.genericbatterydrainer.managers;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.support.v8.renderscript.Type;

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
                RenderScript renderScript = RenderScript.create(application);
                Bitmap       bitmap       = generateRandomBitmap(1000, 1000);
                Allocation allocation = Allocation.createFromBitmap(renderScript, bitmap);
                Type                type           = allocation.getType();
                Allocation          blurAllocation = Allocation.createTyped(renderScript, type);
                ScriptIntrinsicBlur blurScript     = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
                Bitmap              blankBitmap    = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
                blurScript.setRadius(25f);
                blurScript.setInput(allocation);
                while (isComputing.get()) {
                    for(int i = 0; i < 10; i++){
                        blurScript.forEach(blurAllocation);
                    }
//                    blurAllocation.copyTo(blankBitmap);
                    renderScript.finish();
//                    Logg.d(LOG_TAG, "Render round done");
                }
                allocation.destroy();
                blurAllocation.destroy();
                blurScript.destroy();
                type.destroy();
                renderScript.destroy();
                Logg.d(LOG_TAG, "Renderscript destroyed");
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

    private static Bitmap blurBitmap(Context context, Bitmap bitmap, float radius) {
        RenderScript renderScript   = RenderScript.create(context);
        Allocation   allocation     = Allocation.createFromBitmap(renderScript, bitmap);
        Type         type           = allocation.getType();
        Allocation   blurAllocation = Allocation.createTyped(renderScript, type);

        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        blurScript.setRadius(radius);
        blurScript.setInput(allocation);
        blurScript.forEach(blurAllocation);

        blurAllocation.copyTo(bitmap);
        allocation.destroy();
        blurAllocation.destroy();
        blurScript.destroy();
        type.destroy();
        renderScript.destroy();
        return bitmap;
    }


}
