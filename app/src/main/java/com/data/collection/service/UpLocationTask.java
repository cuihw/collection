package com.data.collection.service;

import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.data.greendao.DaoSession;
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.data.greendao.GatherPointDao;
import com.data.collection.data.greendao.TraceLocation;
import com.data.collection.data.greendao.TraceLocationDao;
import com.data.collection.module.BaseBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.LsLog;
import com.google.gson.Gson;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class UpLocationTask extends TimerTask {

    private static final String TAG = "UpLocationTask";
    boolean isStart;
    Timer timer;
    static UpLocationTask instance;

    public static synchronized UpLocationTask getInstance() {
        if (instance == null)
            instance = new UpLocationTask();

        return instance;
    }

    private UpLocationTask(){}

    public synchronized void  startUpload() {
        if (isStart) return;
        timer = new Timer();
        timer.schedule(instance, 1000 * 10, Constants.UPLOAD_TRACE_INTERVAL);// 定时器延时执行任务的方法
        isStart = true;
    }

    public void  stopUpload() {
        if (timer != null) {
            instance.cancel();
            timer.cancel();
        }
        timer = null;
        isStart = false;
    }



    @Override
    public void run() {
        if (!isStart) return;
        DaoSession daoSession = App.getInstence().getDaoSession();

        QueryBuilder<TraceLocation> qb = daoSession.queryBuilder(TraceLocation.class)
                .where(TraceLocationDao.Properties.IsUpload.eq(false))
                .orderAsc(TraceLocationDao.Properties.Time)
                .limit(30);

        try {
            List<TraceLocation> list = qb.list(); // 查出当前对应的数据

            if (list == null || list.size() == 0) return;

            Map<String ,Object> params = new HashMap<>();
            String s = new Gson().toJson(list);
            params.put("locations", s);

            HttpRequest.postData(Constants.UPLOAD_LOCATION, params, new HttpRequest.RespListener<BaseBean>() {
                @Override
                public void onResponse(int status, BaseBean bean) {
                    if (status != 0) return ;
                    LsLog.w(TAG, "upload_trace result = " + (bean == null ? "null": bean.toJson()));
                    if (bean.getCode().equals(Constants.SUCCEED)) {
                        for (TraceLocation traceLocation: list) {
                            traceLocation.setIsUpload(true);
                            daoSession.update(traceLocation);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
