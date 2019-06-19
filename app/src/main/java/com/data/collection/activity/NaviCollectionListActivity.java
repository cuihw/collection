package com.data.collection.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.classic.adapter.BaseAdapterHelper;
import com.classic.adapter.CommonAdapter;
import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.data.CacheData;
import com.data.collection.data.greendao.DaoSession;
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.data.greendao.GatherPointDao;
import com.data.collection.module.BaseBean;
import com.data.collection.module.CollectType;
import com.data.collection.module.CollectionImage;
import com.data.collection.module.ImageData;
import com.data.collection.module.ImageUploadBean;
import com.data.collection.module.PointData;
import com.data.collection.module.PointListBean;
import com.data.collection.module.PointListData;
import com.data.collection.module.UserInfoBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;
import com.data.navidata.LocaltionData;
import com.data.navidata.NaviDataSS;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.greendao.query.QueryBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

// 采集点导航

public class NaviCollectionListActivity extends BaseActivity {

    private static final String TAG = "NaviCollectionListActivity";

    KProgressHUD hud;

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.listview)
    ListView listView;

    @BindView(R.id.no_data_tv)
    TextView noDataTv;

    CommonAdapter<GatherPoint> adapter;

    List<GatherPoint> dataList;       // 同步网络数据

    List<CollectType> collectTypes;

    static boolean isShowLocalData = true;

    public static void start(Context context, boolean isLocalData) {
        Intent intent = new Intent(context, NaviCollectionListActivity.class);
        context.startActivity(intent);
        isShowLocalData = isLocalData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi_collection_list);
        initListener();

        dataList = getData(isShowLocalData);
        initView();

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false);
    }

    private void initView() {
        if (!CacheData.isValidProject()) {
            ToastUtil.showTextToast(this, Constants.NO_PROJECT_INFO);
            return;
        }

        UserInfoBean userInfoBean = CacheData.getUserInfoBean();

        collectTypes = userInfoBean.getData().getProject().getTypes();

        adapter = new CommonAdapter<GatherPoint>(this, R.layout.item_gather_point, dataList) {
            @Override
            public void onUpdate(BaseAdapterHelper helper, GatherPoint item, int position) {
                helper.setText(R.id.name, item.getName());
                String type_id = item.getType_id();
                CollectType thisType = null;
                for (CollectType type : collectTypes) {
                    if (type.getId().equals(type_id)) {
                        thisType = type;
                    }
                }
                helper.setText(R.id.report_tv, item.getReport());

                ImageView imageView = helper.getView(R.id.icon_type);

                if (thisType != null) {
                    ImageLoader.getInstance().displayImage(thisType.getIcon(), imageView);
                } else {
                    imageView.setImageResource(R.mipmap.ic_popup_dialog_close);
                }
                helper.setText(R.id.location, "经度：" + item.getLongitude() + "， 维度：" + item.getLatitude());
                helper.setText(R.id.time, "更新时间: " + item.getUpdated_at());
                helper.setVisible(R.id.is_uploaded, View.GONE);
                ImageView naviIV = helper.getView(R.id.icon_navi);
                naviIV.setVisibility(View.VISIBLE);
                naviIV.setOnClickListener(v->naviTo(item));

            }
        };
        listView.setAdapter(adapter);
        showData();
    }

    private void naviTo(GatherPoint item) {
        LsLog.w(TAG, "naviTo() = " + item.getName());
        NaviDataSS naviDataSS = new NaviDataSS();
        LocaltionData endNode  = new  LocaltionData();
        endNode.setName(item.getName());
        endNode.setLatitude(Double.parseDouble(item.getLatitude()));
        endNode.setLongitude(Double.parseDouble(item.getLongitude()));
        naviDataSS.setEndNode(endNode);

        String activity = "com.data.zwnavi.MainActivity";
        ComponentName component = new ComponentName("com.data.zwnavi", activity);
        Intent intent = new Intent();
        intent.setComponent(component);
        intent.putExtra("NaviDataSS", new Gson().toJson(naviDataSS));
        startActivity(intent);
    }

    private void showData() {
        if (adapter == null) return;

        if (dataList != null && dataList.size() > 0) {
            adapter.replaceAll(dataList);
        } else {
            adapter.replaceAll(null);
        }

        if (adapter.getCount() > 0) {
            noDataTv.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
        } else {
            noDataTv.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }
    }

    private List<GatherPoint> getData(boolean isMyCollectionData) {

        DaoSession daoSession = App.getInstence().getDaoSession();
        QueryBuilder<GatherPoint> qb = daoSession.queryBuilder(GatherPoint.class)
                .orderDesc(GatherPointDao.Properties.Collected_at)
                .orderDesc(GatherPointDao.Properties.Updated_at);

        if (isMyCollectionData) { //
            String userName = CacheData.getUserName();
            if (TextUtils.isEmpty(userName)) {
                ToastUtil.showTextToast(this, "用户名不正确，请稍后再试");
                return null;
            }
            LsLog.w(TAG, "get my collection data, my name is : " + userName);
            qb.where(GatherPointDao.Properties.Report.eq(userName));
        } else {
            qb.where(GatherPointDao.Properties.IsUploaded.eq(true));
        }

        List<GatherPoint> list = qb.list(); // 查出当前对应的数据
        return list;
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v -> finish());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GatherPoint item = adapter.getItem(position);
                AddCollectionActivity.start(NaviCollectionListActivity.this, item);
            }
        });
    }


    @Override
    protected void onDestroy() {
        HttpRequest.cancleRequest(TAG);
        HttpRequest.cancleRequest("upLoadImgs");
        super.onDestroy();
    }
}
