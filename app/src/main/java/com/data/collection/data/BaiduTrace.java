package com.data.collection.data;

import android.content.Context;

import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.track.HistoryTrackRequest;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;

public class BaiduTrace {

    static BaiduTrace baiduTrace;
    boolean isInTrace;
    // 轨迹服务ID
    long serviceId = 212879;

    boolean isNeedObjectStorage = false;
    // 设备标识  使用用户名+ id 的方式作为跟踪的entry
    String entityName = "myTrace_Zhongwang";
    Context applicationContext;

    // 请求标识
    int tag = 1;
    Trace mTrace;
    LBSTraceClient mTraceClient;

    // 初始化轨迹服务监听器
    OnTraceListener mTraceListener = new OnTraceListener() {
        @Override
        public void onBindServiceCallback(int i, String s) {

        }

        @Override
        public void onInitBOSCallback(int i, String s) {

        }

        // 开启服务回调
        @Override
        public void onStartTraceCallback(int status, String message) {
            // start gather trace.
            mTraceClient.startGather(mTraceListener);
        }

        // 停止服务回调
        @Override
        public void onStopTraceCallback(int status, String message) {
        }

        // 开启采集回调
        @Override
        public void onStartGatherCallback(int status, String message) {
        }

        // 停止采集回调
        @Override
        public void onStopGatherCallback(int status, String message) {
        }

        // 推送回调
        @Override
        public void onPushCallback(byte messageNo, PushMessage message) {
        }

    };

    public synchronized static BaiduTrace getInstance() {
        if (baiduTrace == null) {
            baiduTrace = new BaiduTrace();
        }
        return baiduTrace;
    }

    public void init(Context context) {
        applicationContext = context.getApplicationContext();

        mTrace = new Trace(serviceId, entityName, isNeedObjectStorage);
        // 初始化轨迹服务客户端
        mTraceClient = new LBSTraceClient(applicationContext);

        // 定位周期(单位:秒)
        int gatherInterval = 60;
        // 打包回传周期(单位:秒)
        int packInterval = 240;
        // 设置定位和打包周期
        mTraceClient.setInterval(gatherInterval, packInterval);
    }

    public void start() {
        isInTrace = true;
        mTraceClient.startTrace(mTrace, mTraceListener);
    }

    public void stop() {
        isInTrace = false;
        // 停止采集
        mTraceClient.stopGather(mTraceListener);
        mTraceClient.stopTrace(mTrace, mTraceListener);
    }

    public boolean isInTrace() {
        return isInTrace;
    }

    public void getHistoryTrace(long startTime, long endTime, OnTrackListener trackListener) {
        HistoryTrackRequest historyTrackRequest = new HistoryTrackRequest(tag, serviceId, entityName);

        //设置轨迹查询起止时间
        // 开始时间(单位：秒)
        if (startTime == 0) startTime = System.currentTimeMillis() / 1000 - 12 * 60 * 60; // 24小时以内

        // 结束时间(单位：秒)
        if (endTime == 0) endTime = System.currentTimeMillis() / 1000;
        // 设置开始时间
        historyTrackRequest.setStartTime(startTime);
        // 设置结束时间
        historyTrackRequest.setEndTime(endTime);
        historyTrackRequest.setEntityName(entityName);

        // 查询历史轨迹
        mTraceClient.queryHistoryTrack(historyTrackRequest, trackListener);
    }
}
