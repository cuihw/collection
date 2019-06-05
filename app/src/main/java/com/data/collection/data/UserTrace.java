package com.data.collection.data;

import android.content.Context;
import android.content.Intent;

import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.track.HistoryTrackRequest;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;
import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.data.greendao.DaoSession;
import com.data.collection.data.greendao.TraceLocation;
import com.data.collection.data.greendao.TraceLocationDao;
import com.data.collection.listener.ITraceListener;
import com.data.collection.module.TraceData;
import com.data.collection.module.TraceListBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.service.TraceService;
import com.data.collection.util.LsLog;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserTrace {

    private static final String TAG = "UserTrace";
    static UserTrace baiduTrace;
    boolean isInTrace;

    Context applicationContext;

    public synchronized static UserTrace getInstance() {
        if (baiduTrace == null) {
            baiduTrace = new UserTrace();
        }
        return baiduTrace;
    }


    public synchronized void start() {
        isInTrace = true;
//        mTraceClient.startTrace(mTrace, mTraceListener);

        //启动Service
        Intent intentOne = new Intent(applicationContext, TraceService.class);
        applicationContext.startService(intentOne);
        LsLog.w(TAG, "start trace location");

    }

    public synchronized void stop() {
        isInTrace = false;
        //启动Service
        Intent intentOne = new Intent(applicationContext, TraceService.class);
        applicationContext.stopService(intentOne);
        LsLog.w(TAG, "stop trace location");
    }

    public boolean isInTrace() {
        return isInTrace;
    }

    public void init(App app) {
        applicationContext = app;
    }

    public void getHistoryTrace(long startTime, ITraceListener traceListener) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR ,0);
        calendar.set(Calendar.MINUTE ,0);
        calendar.set(Calendar.SECOND ,0);

        if (startTime == 0) {
            startTime = calendar.getTimeInMillis() / 1000;
        } else {
            calendar.setTimeInMillis(startTime * 1000);
        }

        calendar.set(Calendar.HOUR ,23);
        calendar.set(Calendar.MINUTE ,59);
        calendar.set(Calendar.SECOND ,59);

        long endTime = calendar.getTimeInMillis()/1000;

        try {
            DaoSession daoSession = App.getInstence().getDaoSession();

            QueryBuilder<TraceLocation> qb = daoSession.queryBuilder(TraceLocation.class)
                    .where(TraceLocationDao.Properties.Time.gt(startTime),
                            TraceLocationDao.Properties.Time.le(endTime))
                    .orderAsc(TraceLocationDao.Properties.Time);

            List<TraceLocation> list = qb.list();
            traceListener.onTraceList(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDataFromServer(Context context, long startTime, ITraceListener traceListener) {

        Map<String , Object> params = new HashMap<>();
        params.put("start_time", startTime);
        HttpRequest.postData(context, Constants.GET_TRACE, params, new HttpRequest.RespListener<TraceListBean>() {
            @Override
            public void onResponse(int status, TraceListBean bean) {
                if (bean == null) {
                    traceListener.onTraceList(null);
                    return;
                }
                if (bean.getCode().equals(Constants.SUCCEED)) {
                    List<TraceLocation> traceList = bean.getData().getTraceList();
                    traceListener.onTraceList(traceList);
                } else {
                    traceListener.onTraceList(null);
                }

            }
        });
    }


}
