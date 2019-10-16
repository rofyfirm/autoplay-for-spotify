package com.rofyfirm.autoplay;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.Fragment;

import com.spotify.android.appremote.api.SpotifyAppRemote;

public class TimerBackground extends Service {

    private Intent intent;
    private final String TAG = "BroadcastTimer";
    private CountDownTimer countDownTimer;
    private Context mContext;

    public static final String TIMER_BR = "com.rofyfirm.autoplay.timer_br";

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(TIMER_BR);
        long longFromFragment = intent.getLongExtra("timer",5000);
        countDownTimer = new CountDownTimer(longFromFragment, 1000) {
            public void onTick(long millisUntilFinished) {
                sendBroadcast(intent);
            }
            public void onFinish() {

            }
        }.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        countDownTimer.cancel();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
}