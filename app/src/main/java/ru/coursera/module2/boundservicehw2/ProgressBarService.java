package ru.coursera.module2.boundservicehw2;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProgressBarService extends Service {
    public static final String PROGRESS_ACTION = "PROGRESS_ACTION";
    public static final String SUCCESS_ACTION = "SUCCESS_ACTION";

    public static final String PROGRESS_VAL = "PROGRESS_VAL";

    private int progressValue = 0;

    private ScheduledExecutorService scheduledExecutorService;
    private IBinder myBinder;
    private LocalBroadcastManager localBroadcast;

    public ProgressBarService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (myBinder == null) {
            myBinder = new MyBinder();
        }
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        localBroadcast = LocalBroadcastManager.getInstance(this);
        scheduledExecutorService = Executors.newScheduledThreadPool(1);

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                updateProgress(5);
            }
        },200,200, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDestroy() {
        scheduledExecutorService.shutdown();
    }

    public synchronized void  updateProgress(int val) {
        if (progressValue == 100 && val > 0) {return;}

        progressValue += val;
        if (progressValue >= 100) {
            progressValue = 100;
            sendSuccess();
        } else if (progressValue < 0) {
            progressValue = 0;
        }

        sendProgressUpdate(progressValue);
    }

    private void sendProgressUpdate(int progressVal) {
        Intent intent = new Intent(PROGRESS_ACTION);
        intent.putExtra(PROGRESS_VAL, progressVal);
        localBroadcast.sendBroadcast(intent);
    }

    private void sendSuccess() {
        Intent intent = new Intent(SUCCESS_ACTION);
        localBroadcast.sendBroadcast(intent);
    }

    class MyBinder extends Binder {
        ProgressBarService getService() {
            return ProgressBarService.this;
        }
    }
}
