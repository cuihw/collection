package com.data.collection.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.data.collection.R;

/**
 * Created by Chris on 2019/05/10.
 */

public class ForegroundService extends Service {

    private static final String TAG = "ForegroundService";
    private static final int RES_ID = R.layout.activity_navi;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String channelId = "my_service";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("my_service", "My Background Service");
        }

        Notification notification = new Notification();
        try {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this, channelId);
//            Intent intent = new Intent(this, LiteActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//            PendingIntent pendingIntent =
//                    PendingIntent.getActivity(this, 0, intent, 0);
            notification = builder.setSmallIcon(R.mipmap.launch_icon).setTicker("正在导航")
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("正在导航")
                    .setContentText("数据采集地图导航")
                    .build();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        startForeground(RES_ID, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
    }
}
