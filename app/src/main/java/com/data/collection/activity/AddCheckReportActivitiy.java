package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.module.UserInfoBean;
import com.data.collection.util.DateUtils;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;
import com.google.gson.Gson;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import butterknife.BindView;

public class AddCheckReportActivitiy extends BaseActivity {

    private static GatherPoint gatherPoint;

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.listview)
    ListView listview;

    @BindView(R.id.no_data_tv)
    TextView no_data_tv;

    @BindView(R.id.commit_tv)
    TextView commitTv;

    @BindView(R.id.content_et)
    TextView contentEt;

    List<CheckPoint> checkPointList;

    CommonAdapter<CheckPoint> adapter;


    public static void start(Context context, GatherPoint point) {
        Intent intent = new Intent(context, AddCheckReportActivitiy.class);
        intent.putExtra("point", new Gson().toJson(point));
        gatherPoint = point;
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_check_report);
        initView();
        initListener();

        getData();
    }

    private void getData() {
        String onLineId = gatherPoint.getId();
        DaoSession daoSession = App.getInstence().getDaoSession();

        QueryBuilder<CheckPoint> qb = daoSession.queryBuilder(CheckPoint.class)
                .where(CheckPointDao.Properties.CollectOnlineId.eq(onLineId))
                .orderDesc(CheckPointDao.Properties.Time);

        checkPointList = qb.list(); // 查出当前对应的数据
        if (checkPointList == null || checkPointList.size() == 0) {
            no_data_tv.setVisibility(View.VISIBLE);
            return;
        } else {
            no_data_tv.setVisibility(View.GONE);
        }
        adapter.replaceAll(checkPointList);
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v -> finish());
        commitTv.setOnClickListener(v -> addReport());
    }

    private void addReport() {
        String content = contentEt.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            ToastUtil.showTextToast(this, "检查记录为空");
            return;
        }
        CheckPoint checkPoint = new CheckPoint();
        checkPoint.setPointid(gatherPoint.getOffline_id());
        checkPoint.setCollectOnlineId(gatherPoint.getId());
        checkPoint.setCheck(content);
        checkPoint.setName(gatherPoint.getName());
        checkPoint.setReporter(CacheData.getUserName());
        checkPoint.setTime(DateUtils.getNow(DateUtils.fmtYYYYMMDDhhmmss));
        long id = App.getInstence().getDaoSession().insertOrReplace(checkPoint);

        String message = "保存成功";
        if (id > 0) {
            getData();
        } else {
            message = "保存失败";
        }

        ToastUtil.showTextToast(this, message);
    }

    private void initView() {
        if (!hasProjectInfo()) {
            ToastUtil.showTextToast(this, "请联系管理员，分配项目");
            return;
        }
        titleView.getTitleTv().setText(gatherPoint.getName());

        adapter = new CommonAdapter<CheckPoint>(this, R.layout.item_check_report) {
            @Override
            public void onUpdate(BaseAdapterHelper helper, CheckPoint item, int position) {
                String name = item.getName();
                if (TextUtils.isEmpty(name)) {
                    name = gatherPoint.getName();
                }
                helper.setText(R.id.name_tv, name);
                helper.setText(R.id.report_tv, item.getCheck());
                helper.setText(R.id.report_user, item.getReporter());
                helper.setText(R.id.time_tv, item.getTime());
            }
        };
        listview.setAdapter(adapter);
    }


    boolean hasProjectInfo() {
        UserInfoBean userInfoBean = CacheData.getUserInfoBean();
        if (userInfoBean == null) {
            ToastUtil.showTextToast(this, Constants.NO_PROJECT_INFO);
            return false;
        }
        return true;
    }

}
