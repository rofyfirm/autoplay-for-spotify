package com.rofyfirm.autoplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.location.DetectedActivity;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class SpareActivity extends AppCompatActivity {

    private Switch switchTrack;
    private Button timerView;
    private long time;
    private CountDownTimer countDownTimer;
    public String cookie;

    private static final String CLIENT_ID = "9e8c4381caaa44ada8adbca31e5db2ec";
    private static final String REDIRECT_URI = "com.rofyfirm.autoplay://callback";
    SpotifyAppRemote mSpotifyAppRemote;
    BroadcastReceiver broadcastReceiver;

    private CharSequence[] valuesDialog = {"Mati", "15 Menit", "30 Menit", "1 Jam", "2 Jam"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spare);

        if (savedInstanceState==null){
            switchTrack = (Switch) findViewById(R.id.track_switch);
            timerView = (Button) findViewById(R.id.timer_view);

            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals("activity_intent")) {
                        int type = intent.getIntExtra("type", -1);
                        int confidence = intent.getIntExtra("confidence", 0);
                        handleUserActivity(type, confidence);
                    }
                }
            };
        } else {
            String cookieData =  savedInstanceState.getString("CookieData");
            if(!TextUtils.isEmpty(cookieData)) {
                cookie = cookieData;
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_BACK | KeyEvent.KEYCODE_HOME:
                moveTaskToBack(true);
                return true;
        }
        return false;
    }

    //Tracking
    protected void startTracking() {
        Intent intent = new Intent(SpareActivity.this, BackgroundTracking.class);
        startService(intent);
    }

    protected void stopTracking() {
        Intent intent = new Intent(SpareActivity.this, BackgroundTracking.class);
        stopService(intent);
    }

    private void handleUserActivity(int type, int confidence){
        if(confidence > 50 && (type == DetectedActivity.STILL)||((type == DetectedActivity.RUNNING))){
            onStart();
            if (type == DetectedActivity.STILL){
                //((MainActivity) getActivity()).vehicle();
                mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX40RZ67r0X8p");
            } else if (type == DetectedActivity.RUNNING){
                //((MainActivity) getActivity()).jogging();
                mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DXaDzHIZmwWjK");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("activity_intent"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(TimerBackground.TIMER_BR));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    public void onTrackActivity(View view){
       boolean on = ((Switch)view).isChecked();

       if (on){
           startTracking();
       } else stopTracking();
    }

    public void onTimerDialog(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih waktu");
        builder.setSingleChoiceItems(valuesDialog,-1 , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which!=0){
                    switch (which){
                        case 1: time = 15*60000;
                        case 2: time = 30*60000;
                        case 3: time = 60*60000;
                        case 4: time = 120*60000;
                        default: time = 0;
                    }
                    Toast.makeText(SpareActivity.this,"Akan mati dalam waktu "+valuesDialog[which],Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(SpareActivity.this,"Timer tidak aktif",Toast.LENGTH_LONG);
                }
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        timer(time);
    }

    public void timer(long l){
        countDownTimer = new CountDownTimer(l, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                pauseSpotify();
            }
        }.start();
    }

    //Spotify API
    @Override
    public void onStart() {
        super.onStart();
        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI).showAuthView(true).build();

        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("SpareActivity", throwable.getMessage(), throwable);
            }
        });
    }

    public void onStop(){
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    public void pauseSpotify(){
        mSpotifyAppRemote.getPlayerApi().pause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("CookieData",cookie);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        String cookieData = savedInstanceState.getString("CookieData");
        if (!TextUtils.isEmpty(cookieData)){
            cookie = cookieData;
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}
