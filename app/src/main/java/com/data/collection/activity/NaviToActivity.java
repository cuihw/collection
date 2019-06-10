package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.classic.adapter.BaseAdapterHelper;
import com.classic.adapter.CommonAdapter;
import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.adapter.MultipleLayoutAdapter;
import com.data.collection.data.CacheData;
import com.data.collection.module.NaviData;
import com.data.collection.module.NaviListBean;
import com.data.collection.module.UserInfoBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

// 导航计算页面，这是一个中转页面
// 导航跳到这个页面的时候，给出经纬度坐标地点，（初始化导航，初始化文件夹，初始化语音文件）然后计算路径
// 路径计算完毕后，进入导航页面，开始导航。

public class NaviToActivity extends BaseActivity {

    private static final String TAG = "NaviToActivity";

    KProgressHUD hud;

    @BindView(R.id.title_view)
    TitleView titleView;

    public static void start(Context context, @NotNull NaviData data) {
        Intent intent = new Intent(context, NaviToActivity.class);
        String dataString = data.toJson();
        intent.putExtra("NaviData", dataString);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_navi_point);

        getData();

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false);
        hud.show();
    }

    private void getData() {
        
    }

    @Override
    protected void onDestroy() {
        if (hud.isShowing()) {
            hud.dismiss();
        }
        super.onDestroy();
    }
}
