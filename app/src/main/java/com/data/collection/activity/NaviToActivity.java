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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.classic.adapter.BaseAdapterHelper;
import com.classic.adapter.CommonAdapter;
import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.adapter.MultipleLayoutAdapter;
import com.data.collection.data.CacheData;
import com.data.collection.module.DataPoint;
import com.data.collection.module.NaviData;
import com.data.collection.module.NaviListBean;
import com.data.collection.module.UserInfoBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.FileUtils;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.data.collection.util.NormalUtils;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.greenrobot.greendao.annotation.NotNull;

import java.io.File;
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
    private String mSDCardPath;
    private boolean isInitDirs;

    private static final int NORMAL = 0;
    private static final int EXTERNAL = 1;

    private static final int authBaseRequestCode = 1;
    private boolean hasInitSuccess = false;  // 百度导航初始化是否完成。


    private static final String[] authBaseArr = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,   // 写内存
            Manifest.permission.ACCESS_FINE_LOCATION          // 精准定位
    };


    public static void start(Context context, @NotNull NaviData data) {
        Intent intent = new Intent(context, NaviToActivity.class);
        String dataString = data.toJson();
        intent.putExtra("NaviData", dataString);
        context.startActivity(intent);
    }

    NaviData naviData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_navi_point);

        titleView.getLefticon().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        naviData = getData();

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false);
        hud.show();

        isInitDirs = initDirs();
        initNavi();
        calRoutePlanNode(naviData, com.baidu.navisdk.adapter.impl.BNRoutePlanNode.CoordinateType.WGS84);
    }


    private void calRoutePlanNode(NaviData naviData, final int coType) {
        if (!hasInitSuccess) {
            Toast.makeText(getApplicationContext(), "还未初始化!", Toast
                    .LENGTH_SHORT).show();
            finish();
        }

        Location location = LocationController.getInstance().getLocation();
        if (location == null) {
            ToastUtil.showTextToast(this, "请打开GPS确定位置开始导航");
            finish();
            return;
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // 开始点 我的位置
        BNRoutePlanNode sNode = new BNRoutePlanNode.Builder()
                .latitude(latitude)
                .longitude(longitude)
                .name("我的位置")
                .description("我的位置")
                .coordinateType(coType)
                .build();

        latitude = Double.parseDouble(naviData.getLatitude());
        longitude = Double.parseDouble(naviData.getLongitude());

        // 目标点
        BNRoutePlanNode eNode = new BNRoutePlanNode.Builder()
                .latitude(latitude)
                .longitude(longitude)
                .name(naviData.getName())
                .description(naviData.getName())
                .coordinateType(coType)
                .build();

        routePlanToNavi(sNode, eNode, NORMAL);
    }

    private void routePlanToNavi(BNRoutePlanNode sNode, BNRoutePlanNode eNode, final int from) {
        List<BNRoutePlanNode> list = new ArrayList<>();
        list.add(sNode);
        list.add(eNode);
        BaiduNaviManagerFactory.getCommonSettingManager().setCarNum(this, "豫AT6662");
        BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(
                list,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                null,
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                                ToastUtil.showTextToast(NaviToActivity.this, "算路开始");

                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:

                                ToastUtil.showTextToast(NaviToActivity.this, "算路成功");
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                ToastUtil.showTextToast(NaviToActivity.this, "算路失败");
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                ToastUtil.showTextToast(NaviToActivity.this, "算路成功准备进入导航");
                                Intent intent = new Intent(NaviToActivity.this,
                                        NaviActivity.class);
                                startActivity(intent);
                                // finish.
                                finish();
                                break;
                            default:
                                // nothing
                                break;
                        }
                    }
                });
    }


    private void initNavi() {
        // 申请权限
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!hasBasePhoneAuth()) {
                requestPermissions(authBaseArr, authBaseRequestCode);
                return;
            }
        }
        // libapp_BaiduMapApplib.so
        if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
            hasInitSuccess = true;
            return;
        }
        try {
            BaiduNaviManagerFactory.getBaiduNaviManager().init(getApplicationContext(),
                    mSDCardPath, FileUtils.APP_FOLDER_NAME, new IBaiduNaviManager.INaviInitListener() {

                        @Override
                        public void onAuthResult(int status, String msg) {
                            String result;
                            if (0 == status) {
                                result = "key校验成功!";
                            } else {
                                result = "key校验失败, " + msg;
                            }
                            ToastUtil.showTextToast(NaviToActivity.this, result);
                        }

                        @Override
                        public void initStart() {
                            ToastUtil.showTextToast(NaviToActivity.this, "百度导航引擎初始化开始");
                        }

                        @Override
                        public void initSuccess() {
                            ToastUtil.showTextToast(NaviToActivity.this, "百度导航引擎初始化成功");

                            hasInitSuccess = true;
                            // 初始化tts
                            initTTS();
                            BaiduNaviManagerFactory.getBaiduNaviManager().enableOutLog(true);
                        }

                        @Override
                        public void initFailed(int errCode) {
                            ToastUtil.showTextToast(NaviToActivity.this, "百度导航引擎初始化失败");
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            LsLog.i(TAG, "init error!");
        }
    }

    private void initTTS() {
        // 使用内置TTS
        BaiduNaviManagerFactory.getTTSManager().initTTS(getApplicationContext(),
                FileUtils.getSdcardDir(), FileUtils.APP_FOLDER_NAME, NormalUtils.getTTSAppID());

    }

    private boolean initDirs() {
        mSDCardPath = FileUtils.getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, FileUtils.APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private NaviData getData() {
        Intent intent = getIntent();
        String naviDataString = intent.getStringExtra("NaviData");
        LsLog.w(TAG, "naviDataString = " + naviDataString);
        if (TextUtils.isEmpty(naviDataString)) {
            ToastUtil.showTextToast(this, "坐标点不正确，导航失败");
            finish();
        }

        NaviData naviData = NaviData.fromJson(naviDataString);
        return naviData;
    }

    private boolean hasBasePhoneAuth() {
        PackageManager pm = getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, getPackageName()) != PackageManager
                    .PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onPause() {
        if (hud.isShowing()) {
            hud.dismiss();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
