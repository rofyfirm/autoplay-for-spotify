package com.rofyfirm.autoplay;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.DetectedActivity;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class SettingFragment extends PreferenceFragment {
    private Context mContext;
    private Activity mActivity;
    private SwitchPreference track;
    private ListPreference timer;
    BroadcastReceiver broadcastReceiver;
    private static CountDownTimer count;

    private static final String CLIENT_ID = "9e8c4381caaa44ada8adbca31e5db2ec";
    private static final String REDIRECT_URI = "com.rofyfirm.autoplay://callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        addPreferencesFromResource(R.xml.pref_general);

        mContext = this.getActivity();
        mActivity = this.getActivity();

        track = (SwitchPreference) findPreference(this
                .getResources().getString(R.string.sp_key_on_off_track));
        timer = (ListPreference) findPreference(this.getResources().getString(R.string.list_timer));

        track.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!track.isChecked()){
                    startTracking();
                    track.setChecked(true);
                } else {
                    stopTracking();
                    track.setChecked(false);
                }
                return false;
            }
        });


        timer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final int index = timer.findIndexOfValue(newValue.toString());

                if(index != 0){
                    Toast.makeText(mActivity,"Akan mati dalam waktu "+timer.getEntries()[index],Toast.LENGTH_LONG).show();
                    long mLong= Long.parseLong(timer.getEntryValues()[index].toString());
                    long time = mLong*60000;
                    timer(time);
                } else {
                    Toast.makeText(mActivity, "Timer tidak aktif",Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
    }

    //Tracking Activity
    protected void startTracking() {
        Intent intent = new Intent(getActivity(), BackgroundTracking.class);
        getActivity().startService(intent);
    }

    protected void stopTracking() {
        Intent intent = new Intent(getActivity(), BackgroundTracking.class);
        getActivity().stopService(intent);
    }

    private void handleUserActivity(int type, int confidence){
        String playlist = null;
        onStart();
        if (confidence>50 & type == DetectedActivity.IN_VEHICLE){
            playlist = "spotify:playlist:37i9dQZF1DX40RZ67r0X8p";
        } else if (confidence>50 & type == DetectedActivity.RUNNING){
            playlist = "spotify:playlist:37i9dQZF1DXaDzHIZmwWjK";
        } else if (confidence>50 & type == DetectedActivity.STILL){
            playlist = "spotify:playlist:37i9dQZF1DX4WYpdgoIcn6";
        }
        playSpotify(playlist);

        if(type == (DetectedActivity.IN_VEHICLE|DetectedActivity.RUNNING)) {
            stopTracking();
        }
    }

    public void timer(final long l){
        count = new CountDownTimer(l, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }
            @Override
            public void onFinish() {
                pauseSpotify();
            }
        }.start();
    }

    @Override
    public void onResume() {
        super.onResume();
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver,
                new IntentFilter("activity_intent"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
    }

    //Spotify API
    @Override
    public void onStart() {
        super.onStart();
        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI).showAuthView(true).build();

        SpotifyAppRemote.connect(mContext, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("SettingFragment", throwable.getMessage(), throwable);
            }
        });
    }

    public void onStop() { super.onStop();}

    public void playSpotify(String s){
        mSpotifyAppRemote.getPlayerApi().play(s);
    }

    public void pauseSpotify(){
        mSpotifyAppRemote.disconnect(mSpotifyAppRemote);
        mSpotifyAppRemote.getPlayerApi().pause();
    }
}
