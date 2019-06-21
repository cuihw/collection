package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.data.CacheData;
import com.data.collection.module.UserInfoBean;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;
import com.kaopiz.kprogresshud.KProgressHUD;

import butterknife.BindView;

// 采集点的导航列表

public class GatherNaviListActivity extends BaseActivity {

    private static final String TAG = "GatherNaviListActivity";

    KProgressHUD hud;

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.listview_child)
    ListView listviewChild;

    static  boolean isLocalList = true;

    public static void start(Context context, boolean isLocalList) {
        Intent intent = new Intent(context, GatherNaviListActivity.class);
        GatherNaviListActivity.isLocalList = isLocalList;
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi_point_list);

        initListener();
        initView();

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false);
    }

    private void initView() {
        UserInfoBean userInfoBean = CacheData.getUserInfoBean();
        if (userInfoBean == null) {
            ToastUtil.showTextToast(this, Constants.NO_PROJECT_INFO);
            return;
        }
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
