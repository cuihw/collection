package com.data.collection.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
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
import com.data.collection.module.MeasurePoint;
import com.data.collection.module.UserInfoBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.sddman.arcgistool.common.Variable;

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

    CommonAdapter<MeasurePoint> adapter;

    List<MeasurePoint> dataList;       // 所有数据

    List<CollectType> collectTypes;

    public static void start(Activity act, int getMeasure) {
        Intent intent = new Intent(act, MeasureCollectionListActivity.class);
        act.startActivityForResult(intent, getMeasure);
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

        adapter = new CommonAdapter<MeasurePoint>(this, R.layout.item_gather_point_measure, dataList) {
            @Override
            public void onUpdate(BaseAdapterHelper helper, MeasurePoint item, int position) {
                helper.setText(R.id.name, item.getName());
                String type_id = item.getType_id();
                CollectType thisType = null;
                for (CollectType type : collectTypes) {
                    if (type.getId().equals(type_id)) {
                        thisType = type;
                    }
                }
                CheckBox checkBox = helper.getView(R.id.check_btn);
                checkBox.setChecked(item.isSelected());
                if (item.isSelected()) {
                    checkBox.setText((position + 1) + "");
                } else {
                    checkBox.setText("");
                }

                checkBox.setOnClickListener(v->{
                    item.setSelected(checkBox.isChecked());
                    addSelectItem(item);
                });
                ImageView upIndex = helper.getView(R.id.up_index);
                ImageView downIndex = helper.getView(R.id.down_index);
                upIndex.setOnClickListener(v->upIndexItem(item,true));
                downIndex.setOnClickListener(v->upIndexItem(item,false));

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



    private void addSelectItem(MeasurePoint item) {
        int index = dataList.indexOf(item);

        if (index > -1) {
            int i = 0;
            for (; i < dataList.size(); i++) {
                if (!dataList.get(i).isSelected()) {
                    break;
                }
            }

            if (i > 0 && item.isSelected()){
                dataList.remove(item);
                dataList.add(i-1,item);
            } else if (i > 0 && i < dataList.size()){
                dataList.remove(item);
                dataList.add(i,item);
            }
        }
        dataList = sortDataList(dataList);
        adapter.replaceAll(dataList);
    }
    List<MeasurePoint> selected;
    private List<MeasurePoint> sortDataList(List<MeasurePoint> dataList) {
        selected = new ArrayList<>();

        List<MeasurePoint> unselected = new ArrayList<>();
        for (MeasurePoint point : dataList) {
            if (point.isSelected()) {
                selected.add(point);
            } else {
                unselected.add(point);
            }
        }
        dataList.clear();
        dataList.addAll(selected);
        dataList.addAll(unselected);
        return dataList;
    }

    private void upIndexItem(MeasurePoint item, boolean isUp) {
        int index = dataList.indexOf(item);
        if (index > 0 && isUp) {
            --index;
            if (index >= 0) {
                dataList.remove(item);
                dataList.add(index, item);
            }
        } else if (index < (dataList.size()-1) && !isUp && dataList.get(index++).isSelected()) {
            dataList.remove(item);
            dataList.add(index, item);
        }
        dataList = sortDataList(dataList);
        adapter.replaceAll(dataList);
    }

    private List<MeasurePoint> getData() {
        List<GatherPoint> list = collectDateQb.list();
        List<MeasurePoint> list1 = new ArrayList<>();
        for (GatherPoint point: list) {
            MeasurePoint measurePoint = new MeasurePoint(point);
            list1.add(measurePoint);
        }
        return list1;
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v ->{
            Intent intent = new Intent();
            setResult(RESULT_CANCELED,  intent);
            finish();
        });

        actionConfirmTV.setOnClickListener(v -> {
            if (lengthButton.isChecked()) {
                if (selected.size() < 2) {
                    ToastUtil.showTextToast(this, "长度测量选择点不能少于2个");
                    return;
                }
                Intent intent = new Intent();
                MeasurePoint[] measurePoints = selected.toArray(new MeasurePoint[0]);
                intent.putExtra("data", measurePoints);
                intent.putExtra("type", 1);
                setResult(RESULT_OK,  intent);//保存数据
            } else {
                if (selected.size() < 3) {
                    ToastUtil.showTextToast(this, "长度测量选择点不能少于3个");
                    return;
                }
                Intent intent = new Intent();
                MeasurePoint[] measurePoints = selected.toArray(new MeasurePoint[0]);
                intent.putExtra("data", measurePoints);
                intent.putExtra("type", 2);
                setResult(RESULT_OK,  intent);//保存数据
            }
            finish();
        });
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
