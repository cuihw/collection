package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.classic.adapter.BaseAdapterHelper;
import com.classic.adapter.CommonAdapter;
import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.data.CacheData;
import com.data.collection.data.greendao.CheckPoint;
import com.data.collection.data.greendao.CheckPointDao;
import com.data.collection.data.greendao.DaoSession;
import com.data.collection.module.BaseBean;
import com.data.collection.module.CheckData;
import com.data.collection.module.CheckReportBean;
import com.data.collection.module.CheckReportResult;
import com.data.collection.module.UploadCheckReportBean;
import com.data.collection.module.UserInfoBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.DateUtils;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

public class CheckReportListActivitiy extends BaseActivity {

    private static final String TAG = "CheckReport";

    @BindView(R.id.title_view)
    TitleView titleView;
    @BindView(R.id.no_data_tv)
    TextView no_data_tv;

    @BindView(R.id.listview)
    ListView listview;

    @BindView(R.id.commit_tv)
    TextView commitTv;

    CommonAdapter<CheckPoint> adapter;

    List<CheckPoint> checkPointList;
    private boolean uploadCompleted;

    CheckReportResult data;

    public static void start(Context context){
        Intent intent = new Intent(context, CheckReportListActivitiy.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_report_list);
        initView();
        initListener();

        getData();
    }

    private void getData() {
        DaoSession daoSession = App.getInstence().getDaoSession();

        QueryBuilder<CheckPoint> qb = daoSession.queryBuilder(CheckPoint.class)
                .orderDesc(CheckPointDao.Properties.Time);

        checkPointList = qb.list(); // 查出当前对应的数据

        if (no_data_tv == null) return;

        if (checkPointList == null || checkPointList.size() == 0) {
            no_data_tv.setVisibility(View.VISIBLE);
            return;
        } else {
            no_data_tv.setVisibility(View.GONE);
        }
        adapter.replaceAll(checkPointList);
    }

    private void initView() {
        adapter = new CommonAdapter<CheckPoint>(this, R.layout.item_check_report) {
            @Override
            public void onUpdate(BaseAdapterHelper helper, CheckPoint item, int position) {
                String name = item.getName();
                helper.setText(R.id.name_tv, name);
                helper.setText(R.id.report_tv, item.getCheck());
                helper.setText(R.id.report_user, item.getReporter());
                helper.setText(R.id.time_tv, item.getTime());
                if (item.getIsUploaded()) {
                    helper.setVisible(R.id.local_indicate, View.GONE);
                } else {
                    helper.setVisible(R.id.local_indicate, View.VISIBLE);
                }

            }
        };
        listview.setAdapter(adapter);
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->finish());

        commitTv.setOnClickListener(v->syncData());
    }

    private void syncData() {

        DaoSession daoSession = App.getInstence().getDaoSession();
        QueryBuilder<CheckPoint> qb = daoSession.queryBuilder(CheckPoint.class)
                .where(CheckPointDao.Properties.IsUploaded.eq(true))
                .orderDesc(CheckPointDao.Properties.Time)
                .limit(1);
        List<CheckPoint> list = qb.list();

        // 时间增量更新。
        String time = "0";
        if (list != null && list.size() > 0) {
            CheckPoint checkPoint = list.get(0);
            time = checkPoint.getTime();
            time = DateUtils.getSecond(time).toString();
        }

        // 下载更新
        // page 	否 	string int 	页码不填或小于0则默认为第一页
        // collect_id 	否 	string 	采集点id无则查询当前项目下所有检查记录
        // updated_at 	否 	string 	更新时间
        Map<String, Object> param = new HashMap<>();
        param.put("updated_at", time);

        HttpRequest.postData(this, Constants.GET_CHECK_LIST, param, new HttpRequest.RespListener<CheckReportBean>() {
            @Override
            public void onResponse(int status, CheckReportBean bean) {
                if (status == 0) {
                    if (bean.getCode().equals(Constants.SUCCEED)) {
                        handlerCheckReport(bean);
                    }
                } else {
                    ToastUtil.showTextToast(CheckReportListActivitiy.this, "请求数据失败，请检查网络");
                }

            }
        });
    }

    private void handlerCheckReport(CheckReportBean bean) {
        CheckReportResult data = bean.getData();
        String total = data.getTotal() ;
        List<CheckData> datalist = data.getData();

        if (datalist != null && datalist.size() > 0) {
            for (CheckData item:datalist) {
                CheckPoint checkPoint = item.getCheckpoint();
                checkPoint.setIsUploaded(true);

                long id = getItemid(item);
                // 没有记录，插入到数据库中
                if (id == 0) {
                    id = App.getInstence().getDaoSession().insertOrReplace(checkPoint);
                }
                LsLog.w(TAG, "insertOrReplace = " + id);
            }

            if (total.compareTo("1") > 0) { //  有多页
                LsLog.w(TAG, "keep sync 。。。");
                syncData();
            } else {
                // 下载完毕，开始上传
                ToastUtil.showTextToast(this, "下载完毕，开始上传");
                upLoadData();
            }
        } else {
            // 下载完毕，开始上传
            ToastUtil.showTextToast(this, "下载完毕，开始上传");
            upLoadData();
        }
    }

    private long getItemid(CheckData  checkData) {
        DaoSession daoSession = App.getInstence().getDaoSession();

        QueryBuilder<CheckPoint> qb = daoSession.queryBuilder(CheckPoint.class)
                .where(CheckPointDao.Properties.Reporter.eq(checkData.getUser_name()),
                    CheckPointDao.Properties.Name.eq(checkData.getCollect_name()),
                    CheckPointDao.Properties.Time.eq(checkData.getCreated_at()));

        List<CheckPoint> list = qb.list();// 查出当前对应的数据
        if (list == null || list.size() == 0) {
            return 0;
        }
        return list.get(0).getId();
    }

    private void upLoadData() {
        DaoSession daoSession = App.getInstence().getDaoSession();
        QueryBuilder<CheckPoint> qb = daoSession.queryBuilder(CheckPoint.class)
                .where(CheckPointDao.Properties.IsUploaded.eq(false)) // 本地数据
                .orderAsc(CheckPointDao.Properties.Time).limit(50);

        List<CheckPoint> list = qb.list();

        uploadCompleted = false;

        if (list != null && list.size() > 0) {
            for(CheckPoint item: list) {
                // HttpRequest.postData();
                final CheckPoint itemPoint = item;
                uploadCheckReport(this, itemPoint, new HttpRequest.RespListener<UploadCheckReportBean>() {
                    @Override
                    public void onResponse(int status, UploadCheckReportBean bean) {
                        if (status == 0) {
                            LsLog.w(TAG, "upload... " + bean.getMsg());
                            if (bean.getCode().equals(Constants.SUCCEED)) {
                                handlerUploadResult(itemPoint,bean);
                            }
                        }
                    }
                });
            }
            if (list.size() < 50) {
                uploadCompleted = true;
            } else {
                upLoadData();
            }
        } else {
            uploadCompleted = true;
            ToastUtil.showTextToast(this, "上传完毕");
        }

        if  (uploadCompleted) {
            delayGetData(2000);
        }
    }

    private void delayGetData(int mills) {
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                getData();
            }
        }, mills);
    }


    private void handlerUploadResult(final CheckPoint itemPoint, UploadCheckReportBean bean) {
        itemPoint.setIsUploaded(true);
        itemPoint.setOnlineId(bean.getData().getId());
        long id = App.getInstence().getDaoSession().insertOrReplace(itemPoint);
        LsLog.w(TAG, "update data id = " + id);
    }

    public static void uploadCheckReport(Context context, final CheckPoint point, HttpRequest.RespListener listener){
        /*
        collect_id 	是 	string 	采集点id
        longitude 	是 	string 	经度
        latitude 	是 	string 	纬度
        height 	是 	string 	海拔（米）
        content 	是 	string 	检查内容
        */
        Map<String, Object> param = new HashMap<>();
        Location location = LocationController.getInstance().getLocation();
        if (location == null) {
            param.put("longitude", "000");
            param.put("latitude", "000");
            param.put("height", "000");
        } else {
            param.put("longitude", location.getLongitude());
            param.put("latitude", location.getLatitude());
            param.put("height", location.getAltitude());
        }

        param.put("collect_id", point.getCollectOnlineId());
        param.put("content", point.getCheck());
        param.put("create_time", point.getTime());
        HttpRequest.postData(context,Constants.SAVE_CHECK, param, listener);
    }

    boolean hasProjectInfo (){
        UserInfoBean userInfoBean = CacheData.getUserInfoBean();
        if (userInfoBean == null) {
            ToastUtil.showTextToast(this, Constants.NO_PROJECT_INFO);
            return false;
        }
        return true;
    }

}
