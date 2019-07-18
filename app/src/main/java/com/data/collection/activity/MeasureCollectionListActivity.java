package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.data.collection.module.CollectType;
import com.data.collection.module.UserInfoBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import butterknife.BindView;

// 测量采集点

public class MeasureCollectionListActivity extends BaseActivity {

    private static final String TAG = "MeasureCollectionListActivity";

    KProgressHUD hud;

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.listview)
    ListView listView;

    @BindView(R.id.area_button)
    RadioButton areaButton;

    @BindView(R.id.length_button)
    RadioButton lengthButton;

    @BindView(R.id.radio_group)
    RadioGroup radioGroup;

    @BindView(R.id.action_ok)
    TextView actionConfirmTV;

    @BindView(R.id.no_data_tv)
    TextView noDataTv;

    CommonAdapter<GatherPoint> adapter;

    List<GatherPoint> dataList;       // 所有数据

    List<CollectType> collectTypes;

    int needUploadSize = 0;

    public static void start(Context context) {
        Intent intent = new Intent(context, MeasureCollectionListActivity.class);
        context.startActivity(intent);
    }

    QueryBuilder<GatherPoint> collectDateQb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LsLog.w(TAG, "onCreate");
        setContentView(R.layout.activity_collection_measure);
        initListener();

        initQueryBuilder();
        dataList = getData();
        initView();

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false);

    }

    private void initQueryBuilder() {
        String userName = CacheData.getUserName();
        if (TextUtils.isEmpty(userName)) {
            ToastUtil.showTextToast(this, "用户名不正确，请稍后再试");
            return;
        }
        LsLog.w(TAG, "get my collection data, my name is : " + userName);

        DaoSession daoSession = App.getInstence().getDaoSession();

        collectDateQb = daoSession.queryBuilder(GatherPoint.class)
                .where(GatherPointDao.Properties.IsUploaded.eq(true))
                .orderDesc(GatherPointDao.Properties.Updated_at);
    }

    private void initView() {
        lengthButton.setChecked(true);

        if (!CacheData.isValidProject()) {
            ToastUtil.showTextToast(this, Constants.NO_PROJECT_INFO);
            return;
        }

        UserInfoBean userInfoBean = CacheData.getUserInfoBean();

        collectTypes = userInfoBean.getData().getProject().getTypes();

        adapter = new CommonAdapter<GatherPoint>(this, R.layout.item_gather_point_measure, dataList) {
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
                // helper.setText(R.id.report_tv, "采集人:" + item.getReport());
                ImageView imageView = helper.getView(R.id.icon_type);

                if (thisType != null) {
                    ImageLoader.getInstance().displayImage(thisType.getIcon(), imageView);
                } else {
                    imageView.setImageResource(R.mipmap.ic_popup_dialog_close);
                }
                helper.setText(R.id.location, "N:" + item.getFormatLongitude() + "， E:" + item.getFormatLatitude());
                helper.setText(R.id.time, item.getUpdated_at());
            }
        };
        listView.setAdapter(adapter);
    }

    private List<GatherPoint> getData() {
        return collectDateQb.list();
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v -> finish());
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.length_button == checkedId) {
                    // 长度测量

                } else {
                    // 面积测量
                }
            }
        });

        actionConfirmTV.setOnClickListener(v -> {;});
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
