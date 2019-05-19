package com.biorecorder.bdfrecorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class BdfRecorderService extends Service {

    private static final String LOG_TAG = "BdfRecorderService";
    private final IBinder mBinder = new ServiceBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            createNotificationAndStartForeground(intent);
        }
        return Service.START_STICKY;
    }

    private void createNotificationAndStartForeground(Intent intent){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel nc = new NotificationChannel(Constants.ACTION.NOTIFICATION_CHANNEL, "Background service", NotificationManager.IMPORTANCE_LOW);
                nc.setShowBadge(false);
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.createNotificationChannel(nc);
            }
            Intent restartIntent = new Intent()
                    .setClassName(this, Constants.ACTION.INTENT_CLASS_MAIN_ACTIVITY)
                    .setAction(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER);
            PendingIntent restartPendingIntent = PendingIntent.getActivity(this, 1, restartIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.ACTION.NOTIFICATION_CHANNEL)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText("notification text")
                    .setContentIntent(restartPendingIntent)
                    .setOngoing(true);
            Notification notification = builder.build();
            startForeground(Constants.ACTION.NOTIFY_MANAGER_START_FOREGROUND_SERVICE, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ServiceBinder extends Binder {
        BdfRecorderService getService() {
            return BdfRecorderService.this;
        }
    }

}
