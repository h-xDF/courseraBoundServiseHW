package ru.coursera.module2.boundservicehw2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView tvProgressBar;
    private Button degradeBtn;

    private Intent bindIntent;
    private ServiceConnection sConn;
    private boolean bound = false;

    private ProgressBarService progressBarService;

    private IntentFilter mProgressUpdateIntentFilter;
    private IntentFilter mSuccessIntentFilter;
    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver mIntentReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.pb_horizontal);
        tvProgressBar = findViewById(R.id.tv_progress_horizontal);
        degradeBtn = findViewById(R.id.degradation_btn);

        mProgressUpdateIntentFilter = new IntentFilter(ProgressBarService.PROGRESS_ACTION);
        mSuccessIntentFilter = new IntentFilter(ProgressBarService.SUCCESS_ACTION);

        sConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                progressBarService = ((ProgressBarService.MyBinder) service).getService();
                bound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };

        mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(ProgressBarService.PROGRESS_ACTION)) {
                    int progress = intent.getIntExtra(ProgressBarService.PROGRESS_VAL, 0);
                    progressBar.setProgress(progress);
                    tvProgressBar.setText(String.valueOf(progress));
                }
                if (intent.getAction().equals(ProgressBarService.SUCCESS_ACTION)) {
                    Toast.makeText(context, "100 %", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        };

        degradeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarService.updateProgress(-50);
            }
        });

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindIntent = new Intent(this, ProgressBarService.class);
        bindService(bindIntent, sConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        localBroadcastManager.registerReceiver(mIntentReceiver, mSuccessIntentFilter);
        localBroadcastManager.registerReceiver(mIntentReceiver, mProgressUpdateIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mIntentReceiver);
        if (bound) {
            unbindService(sConn);
            bound = false;
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mIntentReceiver);
        super.onDestroy();
    }
}
