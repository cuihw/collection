package com.data.collection.fragment;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.data.collection.R;
import com.data.collection.activity.AddCollectionActivity;
import com.data.collection.activity.ArcgisMapActivity;
import com.data.collection.activity.CollectionListActivity;
import com.data.collection.data.UserTrace;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.data.collection.util.PositionUtil;
import com.data.collection.view.TitleView;

import butterknife.BindView;

import static android.content.Context.SENSOR_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentHome#} factory method to
 * create an instance of this fragment.
 */
public class FragmentHome extends FragmentBase {
    private static final String TAG = "FragmentHome";

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.add_point)
    ImageView addPoint;

    @BindView(R.id.trace_process)
    TextView traceProcess;

    @BindView(R.id.recode_trace)
    TextView recodeTrace;

    @BindView(R.id.map_type)
    TextView mapTypeTv;

    @BindView(R.id.show_map_arcgis)
    TextView showArcgisMap;

    @BindView(R.id.baidu_container)
    LinearLayout baiduMapContainer;

    private SupportMapFragment mapFragment;
    BaiduMap mBaiduMap;

    private LocationClient mLocClient;

    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    private boolean isFirstLoc = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");

        view = inflater.inflate(R.layout.fragment_home, container, false);
        bindButterKnife();

        initMap();

        initSensor();

        initView();
        initListener();
        return view;
    }

    private void initView() {
        // 初始化，没有开始记录
        traceProcess.setVisibility(View.INVISIBLE);
    }

    private void initListener() {
        titleView.getRighticon().setOnClickListener(v->{
            // 显示采集列表。
            CollectionListActivity.start(getContext());
        });

        addPoint.setOnClickListener(v-> AddCollectionActivity.start(getContext(),null));
        recodeTrace.setOnClickListener(v-> clickTraceButton());
        mapTypeTv.setOnClickListener(v->{
            if (mBaiduMap != null) {
                int mapType = mBaiduMap.getMapType();
                if (mapType == BaiduMap.MAP_TYPE_NORMAL) {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                    mapTypeTv.setText("交通\n地图");
                } else {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    mapTypeTv.setText("卫星\n地图");
                }
            }
        });

        showArcgisMap.setOnClickListener(v->{
            ArcgisMapActivity.start(getContext());
        });
    }

    private void clickTraceButton() {
        UserTrace instance = UserTrace.getInstance();
        if (instance.isInTrace()) {
            // 停止记录轨迹
            instance.stop();
            recodeTrace.setText("记录\n轨迹");
            traceProcess.clearAnimation();
            traceProcess.setVisibility(View.INVISIBLE);
            if (UserTrace.getInstance().isInTrace()) {
                UserTrace.getInstance().stop();
            }
        } else {
            // 开始记录轨迹
            instance.start();
            recodeTrace.setText("停止\n记录");
            setFlickerAnimation(traceProcess);
            traceProcess.setVisibility(View.VISIBLE);
            if (!UserTrace.getInstance().isInTrace()) {
                UserTrace.getInstance().start();
            }
        }
    }

    private void initSensor() {
        mSensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);//获取传感器管理服务
    }

    private void initMyLocation() {
        // 跟随
        MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;

        // 定位点，默认的定位点图标为null
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                        mCurrentMode, true, null));
        mBaiduMap.setMyLocationEnabled(true);
        mLocClient = new LocationClient(getContext());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(10000);
        mLocClient.setLocOption(option);
        // mLocClient.start();
        mLocClient.start();
    }


    private MyLocationData locData;


    private BDLocationListener myListener  = new BDLocationListener(){
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置

            if (location == null || isHidden()) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    };
    FragmentManager manager;
    private void initMap() {

        MapStatus.Builder builder = new MapStatus.Builder();
        Location location = LocationController.getInstance().getLocation();
        if (location != null) {
            LatLng p = PositionUtil.GpsToBaiduLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
            builder.target(p);
        }

        builder.overlook(-20).zoom(15);
        BaiduMapOptions bo = new BaiduMapOptions().mapStatus(builder.build())
                .zoomControlsEnabled(false);
        //if (mapFragment == null)
        mapFragment = SupportMapFragment.newInstance(bo);

        manager = getChildFragmentManager();
        manager.beginTransaction().add(R.id.map_framelayout, mapFragment, "map_fragment").commit();

        view.post(()->{
            mBaiduMap = mapFragment.getMapView().getMap();
            LsLog.i(TAG, "mBaiduMap = " + mBaiduMap);

            initMyLocation();
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        mSensorManager.registerListener(sensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    SensorEventListener sensorEventListener = new SensorEventListener(){
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (mBaiduMap == null) return;
            double x = event.values[SensorManager.DATA_X];
            if (Math.abs(x - lastX) > 1.0) {
                mCurrentDirection = (int) x;
                locData = new MyLocationData.Builder()
                        .accuracy(mCurrentAccracy)
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(mCurrentDirection).latitude(mCurrentLat)
                        .longitude(mCurrentLon).build();
                mBaiduMap.setMyLocationData(locData);
            }
            lastX = x;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    @Override
    public void onPause() {
        mSensorManager.unregisterListener(sensorEventListener);

        super.onPause();
    }

    @Override
    public void onDestroy() {
        mLocClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        super.onDestroy();
    }

    //实现图片闪烁效果
    private void setFlickerAnimation(View view) {
        final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);
        view.setAnimation(animation);
    }

}
