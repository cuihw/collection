package com.data.collection.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.data.collection.R;
import com.data.collection.module.NaviData;
import com.data.collection.util.FileUtils;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.data.collection.util.NormalUtils;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.greenrobot.greendao.annotation.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

// 导航计算页面，这是一个中转页面
// 导航跳到这个页面的时候，给出经纬度坐标地点，（初始化导航，初始化文件夹，初始化语音文件）然后计算路径
// 路径计算完毕后，进入导航页面，开始导航。

public class ArcgisMapActivity extends BaseActivity {

    private static final String TAG = "ArcgisMapActivity";

    KProgressHUD hud;

    @BindView(R.id.title_view)
    TitleView titleView;

    public static void start(Context context) {
        Intent intent = new Intent(context, ArcgisMapActivity.class);
        context.startActivity(intent);
    }

    NaviData naviData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_arcgis_map);

        titleView.getLefticon().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
