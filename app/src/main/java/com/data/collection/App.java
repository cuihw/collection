package com.data.collection;

import android.app.Application;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.crashlytics.android.Crashlytics;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.data.collection.util.Utils;

import io.fabric.sdk.android.Fabric;

public class App extends Application {


    private static final String TAG = "App ... ";

    private static App instence;

    @Override
    public void onCreate() {
        super.onCreate();
        // Fabric.with(this, new Crashlytics());
        instence = this;

        initBaiduSdk();
    }

    private void initBaiduSdk() {
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(getApplicationContext());
        // 自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        // 包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。CoordinateType.WGS84
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }

    private void showSignInfo() {
        String signInfo = Utils.getSignInfo(getApplicationContext(), getPackageName(), Utils.MD5);
        LsLog.i(TAG, "MD5 signInfo = " + signInfo);
        signInfo = Utils.getSignInfo(getApplicationContext(), getPackageName(), Utils.SHA1);
        LsLog.i(TAG, "SHA1 signInfo = " + signInfo);
    }

    public void getUserInfo() {

    }
}
