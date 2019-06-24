package com.data.collection.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ListView;
import android.widget.TextView;

import com.classic.adapter.BaseAdapterHelper;
import com.classic.adapter.CommonAdapter;
import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.adapter.MultipleLayoutAdapter;
import com.data.collection.module.NaviData;
import com.data.collection.module.NaviListBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;
import com.data.navidata.LocaltionData;
import com.data.navidata.NaviDataSS;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

// 导航点列表

public class NaviListActivity extends BaseActivity {

    private static final String TAG = "NaviListActivity";
    private static final int START_NAVI = 1;

    KProgressHUD hud;

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.listview_parent)
    ListView listviewParent;

    @BindView(R.id.listview_child)
    ListView listviewChild;

    List<NaviData> rootData;
    List<NaviData> childSiteData = new ArrayList<>();

    CommonAdapter<NaviData> parentAdapter;

    MultipleLayoutAdapter childAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, NaviListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi_point_list);

        getData();
        initView();

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false);

        initListener();
    }

    private void getData() {
        HttpRequest.postData(this, Constants.GET_NAVI_LIST, null, new HttpRequest.RespListener<NaviListBean>() {
            @Override
            public void onResponse(int status, NaviListBean bean) {
                LsLog.w(TAG, "GET_NAVI_LIST : " + (bean == null ? "" : bean.toJson()));
                if (status == 0) {
                    handleData(bean);
                } else {
                    ToastUtil.showTextToast(NaviListActivity.this, "请求数据失败，请检查网络");
                }
            }
        });
    }

    private void handleData(NaviListBean bean) {
        if (bean.getCode().equals(Constants.SUCCEED)) {
            rootData = bean.getData();
            parentAdapter.replaceAll(rootData);
        } else {
            ToastUtil.showTextToast(this, bean.getMsg());
        }
    }

    private void showChildList(NaviData item) {
        List<NaviData> sites = item.getSites();
        List<NaviData> children = item.getChildren();
        childSiteData.clear();
        childSiteData.addAll(sites);
        childSiteData.addAll(children);
        childAdapter.replaceAll(childSiteData);
    }

    private void initView() {
//        导航点导航，用户不用登录。

        parentAdapter = new CommonAdapter<NaviData>(this, R.layout.item_navi_parent_point, rootData) {
            @Override
            public void onUpdate(BaseAdapterHelper helper, NaviData item, int position) {
                LsLog.w(TAG, "onUpdate posidion = " + position + ", item = " + new Gson().toJson(item));
                helper.setText(R.id.parent_name, item.getTname());
                TextView view = helper.getView(R.id.parent_name);
                view.setOnClickListener(v -> {
                    if (item.getId().equals(oldItem.getId())) return;

                    oldViewHold.setSelected(false);
                    view.setSelected(true);
                    oldViewHold = view;

                    item.setSelected(true);
                    oldItem.setSelected(false);
                    oldItem = item;
                    showChildList(item);
                });
                if (oldItem == null) {
                    oldItem = item;
                    item.setSelected(true);
                    showChildList(item);
                    view.setSelected(true);
                    oldViewHold = view;
                }
            }
            NaviData oldItem;
            TextView oldViewHold;
        };
        listviewParent.setAdapter(parentAdapter);

        LsLog.w(TAG, "new MultipleLayoutAdapter ");
        childAdapter = new MultipleLayoutAdapter(this, childSiteData);
        listviewChild.setAdapter(childAdapter);
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v -> finish());

        LsLog.w(TAG, "initListener new setlistener ...");
        childAdapter.setListener(item->naviToPos(item));
    }

    private void naviToPos(NaviData item) {
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
        startActivityForResult(intent, START_NAVI);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case START_NAVI:
                    String data1 = data.getStringExtra("data");
                    if (data1.equals("finish.quit")) { // 用户退出导航
                        ToastUtil.showTextToast(this, "退出导航,");
                    } else if (data1.equals("finish.arrived")){ // 用户到达目的地
                        ToastUtil.showTextToast(this, "到达目的地");
                    }
                    break;
            }
        } else if (resultCode == RESULT_CANCELED){

        }


    }

    private void hideBusy() {
        if (hud.isShowing()) {
            hud.dismiss();
        }
    }

}
