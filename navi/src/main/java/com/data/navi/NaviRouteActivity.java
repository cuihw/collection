package com.data.navi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NaviRouteActivity extends AppCompatActivity {

    private static final String TAG = "NaviRouteActivity";
    private String mSDCardPath;
    private boolean hasInitSuccess;

    private static final String APP_FOLDER_NAME = "zwsdk";

    static public void goTo(Context context, double latitude, double longitude, String localName) {
        Intent intent = new Intent(context, NaviRouteActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("localName", localName);

        context.startActivity(intent);
    }

    static public void init(Context context) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDir();
        initNavi();
        initTTS();

        LocalPoint start = getMyLocalPoint();
        LocalPoint end = getEndPoint();
        calRoutePlanNode(start, end);
    }

    private LocalPoint getEndPoint() {
        Intent intent = getIntent();
        double latitude = (double) intent.getDoubleExtra("latitude", 0);
        double longitude = (double) intent.getDoubleExtra("longitude", 0);
        String localName = intent.getStringExtra("localName");
        if (TextUtils.isEmpty(localName)) {
            localName = "目的地";
        }
        Log.i(TAG, "to latitude = " + latitude + ", longitude = " + longitude);
        LocalPoint end = new LocalPoint();
        end.setLongitude(longitude);
        end.setName(localName);
        end.setLatitude(latitude);
        return end;
    }

    private LocalPoint getMyLocalPoint() {
        Location location = LocationController.getInstance().getLocation();
        if (location == null) {
            Toast.makeText(getApplicationContext(), "请打开GPS确定位置开始导航!", Toast.LENGTH_SHORT).show();
        }
        double latitude1 = location.getLatitude();
        double longitude1 = location.getLongitude();
        Log.i(TAG, "from latitude1 = " + latitude1 + ", longitude1 = " + longitude1);

        LocalPoint start = new LocalPoint();
        start.setLongitude(longitude1);
        start.setName("我的位置");
        start.setLatitude(latitude1);
        return start;
    }


    private void calRoutePlanNode(LocalPoint start, LocalPoint end) {
        if (!hasInitSuccess) {
            Toast.makeText(getApplicationContext(), "还未初始化!", Toast.LENGTH_SHORT).show();
        }

        int coType = com.baidu.navisdk.adapter.impl.BNRoutePlanNode.CoordinateType.WGS84;

        BNRoutePlanNode sNode = new BNRoutePlanNode.Builder()
                .latitude(start.getLatitude())
                .longitude(start.getLongitude())
                .name(start.getName())
                .description(start.getName())
                .coordinateType(coType)
                .build();
        BNRoutePlanNode eNode = new BNRoutePlanNode.Builder()
                .latitude(end.getLatitude())
                .longitude(end.getLongitude())
                .name(end.getName())
                .description(end.getName())
                .coordinateType(coType)
                .build();

        routePlanToNavi(sNode, eNode, NORMAL);
    }
    private static final int NORMAL = 0;

    private void routePlanToNavi(BNRoutePlanNode sNode, BNRoutePlanNode eNode, final int from) {
        List<BNRoutePlanNode> list = new ArrayList<>();
        list.add(sNode);
        list.add(eNode);
        BaiduNaviManagerFactory.getCommonSettingManager().setCarNum(this, "豫A66662");
        BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(
                list,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                null,
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                                Log.i(TAG, "算路开始");
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                                Log.i(TAG, "算路成功");

                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                Log.i(TAG, "算路失败");


                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                Log.i(TAG, "算路成功准备进入导航");
                                Intent intent = new Intent(NaviRouteActivity.this, NaviActivity.class);
                                startActivity(intent);
                                break;
                            default:
                                // nothing
                                break;
                        }
                    }
                });
    }


    private static final String[] authBaseArr = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,   // 写内存
            Manifest.permission.ACCESS_FINE_LOCATION          // 精准定位
    };

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

    private static final int authBaseRequestCode = 1;

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
                    mSDCardPath, APP_FOLDER_NAME, new IBaiduNaviManager.INaviInitListener() {

                        @Override
                        public void onAuthResult(int status, String msg) {
                            String result;
                            if (0 == status) {
                                result = "key校验成功!";
                            } else {
                                result = "key校验失败, " + msg;
                            }
                            // ToastUtil.showTextToast(getContext(), result);
                            Log.i(TAG, "Auth result = " + result);
                        }

                        @Override
                        public void initStart() {
                            //ToastUtil.showTextToast(getContext(), "百度导航引擎初始化开始");

                            Log.i(TAG, "百度导航引擎初始化开始");
                        }

                        @Override
                        public void initSuccess() {
                            Log.i(TAG, "百度导航引擎初始化成功");
                            // ToastUtil.showTextToast(getContext(), "百度导航引擎初始化成功");

                            hasInitSuccess = true;
                            // 初始化tts
                            initTTS();
                            BaiduNaviManagerFactory.getBaiduNaviManager().enableOutLog(true);
                        }

                        @Override
                        public void initFailed(int errCode) {

                            Log.i(TAG, "百度导航引擎初始化失败");
                            // ToastUtil.showTextToast(getContext(), "百度导航引擎初始化失败");
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "init error!");
        }
    }

    private void initTTS() {
        // 使用内置TTS
        BaiduNaviManagerFactory.getTTSManager().initTTS(getApplicationContext(),
                mSDCardPath, APP_FOLDER_NAME, getTTSAppID());

    }


    String getTTSAppID() {
        return "11213224";
    }
    
    private boolean initDir() {
        mSDCardPath = Environment.getExternalStorageDirectory().toString();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
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

}
