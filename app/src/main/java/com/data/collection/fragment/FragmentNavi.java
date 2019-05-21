package com.data.collection.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.classic.adapter.BaseAdapterHelper;
import com.classic.adapter.CommonAdapter;
import com.data.collection.R;
import com.data.collection.activity.NaviActivity;
import com.data.collection.module.DataPoint;
import com.data.collection.test.LocationData;
import com.data.collection.util.FileUtils;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.data.collection.util.NormalUtils;
import com.data.collection.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentNavi#} factory method to
 * create an instance of this fragment.
 */
public class FragmentNavi extends FragmentBase {
    private static final String TAG = "FragmentNavi";
    private static final String[] labels = {"同步数据点", "本地数据点", "兴趣点"};

    @BindView(R.id.listview)
    ListView listview;

    CommonAdapter<DataPoint> adapter;

    private boolean hasInitSuccess = false;  // 百度导航初始化是否完成。

    List<DataPoint> testData = LocationData.getTestData();
    private String mSDCardPath;

    private static final int NORMAL = 0;
    private static final int EXTERNAL = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.fragment_home_navi, container, false);
        bindButterKnife();
        initDirs();
        initNavi();
        initView();
        initListener();
        return view;
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

    private static final String[] authBaseArr = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,   // 写内存
            Manifest.permission.ACCESS_FINE_LOCATION          // 精准定位
    };


    private boolean hasBasePhoneAuth() {
        PackageManager pm = getActivity().getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, getActivity().getPackageName()) != PackageManager
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
        try{
            BaiduNaviManagerFactory.getBaiduNaviManager().init(getContext().getApplicationContext(),
                    mSDCardPath, FileUtils.APP_FOLDER_NAME, new IBaiduNaviManager.INaviInitListener() {

                        @Override
                        public void onAuthResult(int status, String msg) {
                            String result;
                            if (0 == status) {
                                result = "key校验成功!";
                            } else {
                                result = "key校验失败, " + msg;
                            }
                            ToastUtil.showTextToast(getContext(), result);
                        }

                        @Override
                        public void initStart() {
                            ToastUtil.showTextToast(getContext(), "百度导航引擎初始化开始");
                        }

                        @Override
                        public void initSuccess() {
                            ToastUtil.showTextToast(getContext(), "百度导航引擎初始化成功");

                            hasInitSuccess = true;
                            // 初始化tts
                            initTTS();
                            BaiduNaviManagerFactory.getBaiduNaviManager().enableOutLog(true);
                        }

                        @Override
                        public void initFailed(int errCode) {
                            ToastUtil.showTextToast(getContext(), "百度导航引擎初始化失败");
                        }
                    });
        }catch (Exception e) {
            e.printStackTrace();
            LsLog.i(TAG, "init error!");
        }
    }

    private void initTTS() {
        // 使用内置TTS
        BaiduNaviManagerFactory.getTTSManager().initTTS(getContext().getApplicationContext(),
                FileUtils.getSdcardDir(), FileUtils.APP_FOLDER_NAME, NormalUtils.getTTSAppID());

    }


    private void initView() {

        adapter = new CommonAdapter<DataPoint>(getContext(), R.layout.item_data_point, testData) {
            @Override
            public void onUpdate(BaseAdapterHelper helper, DataPoint item, int position) {
                helper.setText(R.id.name, item.getName());
                helper.setText(R.id.location_info, item.getLongitude() + ", " + item.getLatitude());
                helper.setText(R.id.time, item.getDateTime());
            }
        };
        listview.setAdapter(adapter);
    }

    private void initListener() {
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                naviTo(testData.get(position));
            }
        });
    }

    private void naviTo(DataPoint dataPoint) {
        LsLog.i(TAG, "naviTo = " + dataPoint.toJson());
        Location location = LocationController.getInstance().getLocation();
        if (location == null) {
            ToastUtil.showTextToast(getContext(), "请打开GPS确定位置开始导航");
        }
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LsLog.i(TAG, "latitude = " + latitude + ", longitude = " + longitude);

        DataPoint start = new DataPoint();
        start.setLongitude(longitude);
        start.setName("我的位置");
        start.setLatitude(latitude);

        calRoutePlanNode(start, dataPoint, com.baidu.navisdk.adapter.impl.BNRoutePlanNode.CoordinateType.WGS84);

    }

    private void initTablayout() {
        for (String label : labels) {
            // tablayout.addTab(tablayout.newTab().setText(label));
        }
    }

    private void calRoutePlanNode(DataPoint start, DataPoint dataPoint, final int coType) {
        if (!hasInitSuccess) {
            Toast.makeText(getContext().getApplicationContext(), "还未初始化!", Toast
                    .LENGTH_SHORT).show();
        }

        BNRoutePlanNode sNode = new BNRoutePlanNode.Builder()
                .latitude(start.getLatitude())
                .longitude(start.getLongitude())
                .name(start.getName())
                .description(start.getName())
                .coordinateType(coType)
                .build();
        BNRoutePlanNode eNode = new BNRoutePlanNode.Builder()
                .latitude(dataPoint.getLatitude())
                .longitude(dataPoint.getLongitude())
                .name(dataPoint.getName())
                .description(dataPoint.getName())
                .coordinateType(coType)
                .build();

        routePlanToNavi(sNode, eNode, NORMAL);
    }

    private void routePlanToNavi(BNRoutePlanNode sNode, BNRoutePlanNode eNode, final int from) {
        List<BNRoutePlanNode> list = new ArrayList<>();
        list.add(sNode);
        list.add(eNode);
        BaiduNaviManagerFactory.getCommonSettingManager().setCarNum(getContext(), "粤B66666");
        BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(
                list,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                null,
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                                ToastUtil.showTextToast(getContext(), "算路开始");

                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:

                                ToastUtil.showTextToast(getContext(), "算路成功");
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:

                                ToastUtil.showTextToast(getContext(), "算路失败");
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:

                                ToastUtil.showTextToast(getContext(), "算路成功准备进入导航");
                                Intent intent = new Intent(getContext(),
                                        NaviActivity.class);

                                startActivity(intent);
                                break;
                            default:
                                // nothing
                                break;
                        }
                    }
                });
    }

}
