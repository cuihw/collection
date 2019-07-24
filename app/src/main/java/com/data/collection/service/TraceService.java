package com.data.collection.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;

import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.data.greendao.TraceLocation;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.google.gson.Gson;

import java.util.Timer;
import java.util.TimerTask;

public class TraceService extends Service {

    private static final String TAG = "TraceService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 服务启动的时候调用
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("=========onStartCommand======");
        startTrace();
        return super.onStartCommand(intent, flags, startId);
    }

    private synchronized void startTrace() {
        LocationController.getInstance().setListener(new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location == null) {
                    LsLog.w(TAG, "Location is null , can't record trace. ");
                    return;
                }
                TraceLocation tl = new TraceLocation();
                tl.setLatitude(location.getLatitude() + "");
                tl.setLongitude(location.getLongitude() + "");
                tl.setTime(System.currentTimeMillis() / 1000 + "");
                LsLog.w(TAG, "trace loaction = " + new Gson().toJson(tl));
                long insert = App.getInstence().getDaoSession().insert(tl);
                LsLog.w(TAG, "trace loaction insert = " + insert);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
    }

    /**
     * 服务销毁的时候调用
     */
    @Override
    public void onDestroy() {
        System.out.println("=========onDestroy======");
        stopTrace();
        super.onDestroy();
    }

    private void stopTrace() {
        LocationController.getInstance().setListener(null);
    }


}
