package com.data.zwnavi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    KProgressHUD hud;

    private String mSDCardPath;
    private boolean isInitDirs;

    private static final int NORMAL = 0;
    private static final int EXTERNAL = 1;

    private static final int authBaseRequestCode = 1;
    private boolean hasInitSuccess = false;  // 百度导航初始化是否完成。
    NaviData naviData;
    private static final String[] authBaseArr = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,   // 写内存
            Manifest.permission.ACCESS_FINE_LOCATION          // 精准定位
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false);
        LocationController.getInstance().startLocation(this);
        initDirs();
        initNavi();

        //NaviData naviData = getIntent();113.746722,34.793354
        naviData = new NaviData();
        naviData.setLatitude("34.793354");
        naviData.setLongitude("113.746722");
        naviData.setName("测试站点");


        String naviDataString = getIntent().getStringExtra("naviData");
        Log.w(TAG,"naviDataString" + naviDataString);
        if (!TextUtils.isEmpty(naviDataString)) {
            naviData = NaviData.fromJson(naviDataString);
        }

        if (hasInitSuccess) {
            beginNavi();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.w(TAG,"onNewIntent");
    }

    private void calRoutePlanNode(NaviData naviData, final int coType) {
        if (!hasInitSuccess) {
            Toast.makeText(getApplicationContext(), "还未初始化!", Toast
                    .LENGTH_SHORT).show();
            //finish();
        }

        Location location = LocationController.getInstance().getLocation();
        if (location == null) {
            ToastUtil.showTextToast(this, "请打开GPS确定位置开始导航");
            //finish();
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
                                ToastUtil.showTextToast(MainActivity.this, "算路开始");
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                                ToastUtil.showTextToast(MainActivity.this, "算路成功");
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                ToastUtil.showTextToast(MainActivity.this, "算路失败");
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                ToastUtil.showTextToast(MainActivity.this, "算路成功准备进入导航");
                                Intent intent = new Intent(MainActivity.this,
                                        NaviActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                            default:
                                break;
                        }
                    }
                });
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
                            ToastUtil.showTextToast(MainActivity.this, result);
                            Log.i(TAG, "result = " + result);
                        }

                        @Override
                        public void initStart() {
                            ToastUtil.showTextToast(MainActivity.this, "百度导航引擎初始化开始");
                        }

                        @Override
                        public void initSuccess() {
                            ToastUtil.showTextToast(MainActivity.this, "百度导航引擎初始化成功");

                            hasInitSuccess = true;
                            // 初始化tts
                            initTTS();
                            BaiduNaviManagerFactory.getBaiduNaviManager().enableOutLog(true);

                            beginNavi();
                        }

                        @Override
                        public void initFailed(int errCode) {
                            ToastUtil.showTextToast(MainActivity.this, "百度导航引擎初始化失败");
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "init error!");
        }
    }

    private void beginNavi() {
        Log.i(TAG, "beginNavi");
        calRoutePlanNode(naviData, BNRoutePlanNode.CoordinateType.WGS84);
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

    @Override
    protected void onResume() {
        super.onResume();
        hud.show();
    }

    @Override
    protected void onPause() {
        hud.dismiss();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LocationController.getInstance().stopLocation();
        super.onDestroy();
    }
}
