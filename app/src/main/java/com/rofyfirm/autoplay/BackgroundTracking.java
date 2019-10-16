package com.rofyfirm.autoplay;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class BackgroundTracking extends Service {

    private Intent intent;
    private PendingIntent pendingIntent;
    private ActivityRecognitionClient activityRecognitionClient;

    IBinder mBinder = new BackgroundTracking.LocalBinder();

    public class LocalBinder extends Binder {
        public BackgroundTracking getServerInstance() {
            return BackgroundTracking.this;
        }
    }

    public BackgroundTracking(){

    }

    public void onCreate(){
        super.onCreate();
        activityRecognitionClient = new ActivityRecognitionClient(this);
        intent = new Intent(this, DetectedActivitiesIntent.class);
        pendingIntent = PendingIntent.getService(this,1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        requestActivityButtonHandler();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public int onStartCommand(Intent intent, int flags, int StartId){
        super.onStartCommand(intent, flags, StartId);
        return START_STICKY;
    }

    public void requestActivityButtonHandler(){
        Task<Void> task = activityRecognitionClient.requestActivityUpdates(
        25*1000, pendingIntent);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(getApplicationContext(),
                        "Berhasil melakukan permintaan deteksi aktivitas",
                        Toast.LENGTH_SHORT).show();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),
                        "Gagal melakukan permintaan deteksi aktivitas",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void removeActivityUpdatesButtonHandler() {
        Task<Void> task = activityRecognitionClient.removeActivityUpdates(
                pendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(getApplicationContext(),
                        "Berhasil melakukan penghapusan deteksi aktivitas",
                        Toast.LENGTH_SHORT).show();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),
                        "Gagal melakukan penghapusan deteksi aktivitas",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeActivityUpdatesButtonHandler();
    }
}
