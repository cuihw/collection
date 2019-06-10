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
import com.data.collection.adapter.MultipleLayoutAdapter;
import com.data.collection.data.CacheData;
import com.data.collection.data.greendao.DaoSession;
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.data.greendao.GatherPointDao;
import com.data.collection.module.BaseBean;
import com.data.collection.module.CollectType;
import com.data.collection.module.ImageData;
import com.data.collection.module.ImageUploadBean;
import com.data.collection.module.NaviData;
import com.data.collection.module.NaviListBean;
import com.data.collection.module.PointData;
import com.data.collection.module.PointListBean;
import com.data.collection.module.PointListData;
import com.data.collection.module.UserInfoBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.greendao.query.QueryBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

// 导航点列表

public class NaviListActivity extends BaseActivity {

    private static final String TAG = "NaviListActivity";

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

    CommonAdapter<NaviData> childAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, NaviListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi_point_list);

        getData();
        initListener();

        initView();

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false);
    }

    private void getData() {
        HttpRequest.postData(this, Constants.GET_NAVI_LIST, null, new HttpRequest.RespListener<NaviListBean>() {
            @Override
            public void onResponse(int status, NaviListBean bean) {
                LsLog.w(TAG, "GET_NAVI_LIST : " + (bean == null ? "" : bean.toJson()));
                if (status == 0) {
                    handleData(bean);
                }
            }
        });
    }

    private void handleData(NaviListBean bean) {
        rootData = bean.getData();
        parentAdapter.replaceAll(rootData);
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
        UserInfoBean userInfoBean = CacheData.getUserInfoBean();
        if (userInfoBean == null) {
            ToastUtil.showTextToast(this, Constants.NO_PROJECT_INFO);
            return;
        }
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

//                if (item.isSelected()) {
//                    view.setPressed(true);
//                }
//                else {
//                    view.setPressed(false);
//                }
            }
            NaviData oldItem;
            TextView oldViewHold;
        };
        listviewParent.setAdapter(parentAdapter);
        childAdapter = new MultipleLayoutAdapter(this, childSiteData);

        listviewChild.setAdapter(childAdapter);
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v -> finish());
    }

    private void hideBusy() {
        if (hud.isShowing()) {
            hud.dismiss();
        }
    }

}
