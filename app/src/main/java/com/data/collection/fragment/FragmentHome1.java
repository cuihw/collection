package com.data.collection.fragment;

import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
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
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.activity.AddCollectionActivity;
import com.data.collection.activity.CollectionListActivity;
import com.data.collection.activity.OfflineMapActivity;
import com.data.collection.data.CacheData;
import com.data.collection.data.UserTrace;
import com.data.collection.data.greendao.DaoSession;
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.data.greendao.GatherPointDao;
import com.data.collection.module.CollectType;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.data.collection.util.PositionUtil;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

import static android.content.Context.SENSOR_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentHome1#} factory method to
 * create an instance of this fragment.
 */
public class FragmentHome1 extends FragmentBase {
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

    private MyLocationData locData;
    FragmentManager manager;


    @BindView(R.id.map_my_position)
    TextView myPosition;
    private InfoWindow mInfoWindow;
    private View infoView;

    private void initView() {
        // 初始化，没有开始记录
        traceProcess.setVisibility(View.INVISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");

        view = inflater.inflate(R.layout.fragment_home1, container, false);
        bindButterKnife();

        initMap();

        initSensor();

        initView();
        initListener();

        infoView = creatInfoView();
        return view;
    }

    private View creatInfoView() {
        return LayoutInflater.from(getActivity()).inflate(R.layout.info_window, null);
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
            if (!CacheData.isLogin()) {
                ToastUtil.showTextToast(getContext(), "用户没有登录");
                return;
            }
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

    private void initListener() {
        titleView.getRighticon().setOnClickListener(v->{
            // 显示采集列表。
            if (CacheData.isLogin()) {
                CollectionListActivity.start(getContext());
            } else {
                ToastUtil.showTextToast(getContext(), "请先登录系统，再进行操作");
            }
        });

        addPoint.setOnClickListener(v->{
            if (CacheData.isLogin()) {
                AddCollectionActivity.start(getContext(),null);
            } else {
                ToastUtil.showTextToast(getContext(), "请先登录系统，再进行操作");
            }
        });


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
            OfflineMapActivity.start(getContext());
        });
        myPosition.setOnClickListener(v -> {
            goToMyLocation();
        });
    }

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
                    .direction(mCurrentDirection).latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
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

    private void initMap() {
        MapStatus.Builder builder = new MapStatus.Builder();
        Location location = LocationController.getInstance().getLocation();
        if (location != null) {
            LatLng p = PositionUtil.GpsToBaiduLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
            builder.target(p);
        }

        builder.zoom(15);
        BaiduMapOptions bo = new BaiduMapOptions().mapStatus(builder.build())
                .zoomControlsEnabled(false);

        mapFragment = SupportMapFragment.newInstance(bo);

        manager = getChildFragmentManager();
        manager.beginTransaction().add(R.id.map_framelayout, mapFragment, "map_fragment").commit();

        view.post(()->{
            mBaiduMap = mapFragment.getMapView().getMap();
            initMyLocation();
            initMapListener();
        });
    }

    private void goToMyLocation() {

        MapStatus.Builder builder = new MapStatus.Builder();
        Location location = LocationController.getInstance().getLocation();
        if (location != null) {
            LatLng p = PositionUtil.GpsToBaiduLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
            builder.target(p);
        }

        if (mBaiduMap == null) mBaiduMap = mapFragment.getMapView().getMap();

        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    private void initMyLocation() {
        // 不跟随
        // MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;

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

    private void initMapListener() {
        if (mBaiduMap != null) {
            getInBoundsData(mBaiduMap.getMapStatus().bound);
        }

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Bundle extraInfo = marker.getExtraInfo();
                GatherPoint gatherPoint = (GatherPoint) extraInfo.getSerializable("GatherPoint");
                LsLog.w(TAG, "setOnMarkerClickListener = " + gatherPoint.getName() + ", marker id = " + marker.getId());
                mInfoWindow = createInfoWindow(infoView, gatherPoint);
                if (mInfoWindow != null) mBaiduMap.showInfoWindow(mInfoWindow);
                infoView.setOnClickListener(v->hideInfoWindow());
                return false;
            }
        });

        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
                getInBoundsData(mapStatus.bound);
            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {

            }
        });
    }

    private void hideInfoWindow() {
        if (mInfoWindow != null) {
            mBaiduMap.hideInfoWindow();
            MapView mapView = mapFragment.getMapView();
            if (mapView != null)mapView.postInvalidate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mSensorManager.registerListener(sensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);

        if (mBaiduMap != null) {
            getInBoundsData(mBaiduMap.getMapStatus().bound);
        }
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

    private Map<String, LatLng> markerMap = new HashMap<>();

    private void getInBoundsData(LatLngBounds bound) {

        double latitude1 = bound.southwest.latitude;
        double latitude2 = bound.northeast.latitude;
        double longitude1 = bound.southwest.longitude;
        double longitude2 = bound.northeast.longitude;
        latitude1 = latitude1 - Constants.RANGE;
        latitude2 = latitude2 + Constants.RANGE;
        longitude1 = longitude1 - Constants.RANGE;
        longitude2 = longitude2 + Constants.RANGE;

        LsLog.w(TAG, "bounds:  latitude1 = " + latitude1 + ", latitude2 = " + latitude2
                + ", longitude1 = " + longitude1 + ", longitude2 = " + longitude2
        );

        //public abstract class AsyncTask<Params, Progress, Result>
        new AsyncTask<Double, Integer, List<OverlayOptions>>(){

            @Override
            protected List doInBackground(Double... doubles) {
                List<OverlayOptions> options = new ArrayList<>();

                DaoSession daoSession = App.getInstence().getDaoSession();
                LsLog.w(TAG, "doubles size = " + doubles.length);

                QueryBuilder<GatherPoint> qb = daoSession.queryBuilder(GatherPoint.class)
                        .where(GatherPointDao.Properties.Latitude.gt("" + doubles[0]),
                                GatherPointDao.Properties.Latitude.le("" + doubles[1]),
                                GatherPointDao.Properties.Longitude.gt("" + doubles[2]),
                                GatherPointDao.Properties.Longitude.le("" + doubles[3])
                                )
                        .orderDesc(GatherPointDao.Properties.Updated_at);
                List<GatherPoint> dataList = qb.list(); // 查出当前对应的数据
                LsLog.w(TAG, "dataList size = " + dataList.size());
                markerMap.clear();

                for (GatherPoint point : dataList) {
                    double lat = Double.parseDouble(point.getLatitude());
                    double nextlng = Double.parseDouble(point.getLongitude());

                    LatLng latLng = markerMap.get(point.getLatitude() + nextlng);
                    while (latLng != null) {
                        nextlng = nextlng + Constants.DIFF;
                        String s = String.valueOf(nextlng);
                        latLng = markerMap.get(point.getLatitude() + s);
                    }
                    LatLng llng = new LatLng(lat, nextlng);

                    markerMap.put(point.getLatitude() + String.valueOf(nextlng) , llng);

                    llng = PositionUtil.GpsToBaiduLatLng(llng);

                    options.add(getMarker(llng, point));
                }
                return options;
            }

            @Override
            protected void onPostExecute(List<OverlayOptions> list) {
                if (mBaiduMap != null) {
                    mBaiduMap.clear();
                    mBaiduMap.addOverlays(list);
                }
            }
        }.execute(latitude1, latitude2, longitude1,longitude2);
    }

    BitmapDescriptor mMarkerBitmap;
    private OverlayOptions getMarker(LatLng point, GatherPoint gatherPoint) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("GatherPoint", gatherPoint);

        mMarkerBitmap = getMarkerBitmap(gatherPoint);

        OverlayOptions option = new MarkerOptions()
                .position(point) //必传参数
                .icon(mMarkerBitmap) //必传参数
                // 设置平贴地图，在地图中双指下拉查看效果
                .flat(false)
                .extraInfo(bundle)
                .alpha(0.8f);
        LsLog.w(TAG, "marker point = " + gatherPoint.getName());
        return option;
    }

    private BitmapDescriptor getMarkerBitmap(GatherPoint gatherPoint) {
        String type_id = gatherPoint.getType_id();
        CollectType collectType = CacheData.getTypeMaps().get(type_id);
        if (collectType != null) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_point_marker, null);
            TextView name = view.findViewById(R.id.name_tv);
            name.setText(gatherPoint.getName());
            ImageView icon = view.findViewById(R.id.icon_iv);
            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(collectType.getIcon());
            icon.setImageBitmap(bitmap);
            return BitmapDescriptorFactory.fromView(view);
        }

        return BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
    }


    private InfoWindow createInfoWindow(View view, GatherPoint pointInfo) {

        if (pointInfo == null) {
            ToastUtil.showTextToast(getContext(), "不是一个采集点");
            return null;
        }

        final GatherPoint point = pointInfo;

        Map<String, CollectType> typeMaps = CacheData.getTypeMaps();
        CollectType type = typeMaps.get(point.getType_id());

        if (type == null) {
            ToastUtil.showTextToast(getContext(), "采集点类型错误");
            return null;
        }

        FragmentCheckRecord.InfoWindowHolder infoHolder = null;
        if (view.getTag() == null) {
            infoHolder = new FragmentCheckRecord.InfoWindowHolder();
            infoHolder.name_tv = view.findViewById(R.id.name_tv);
            infoHolder.type_tv = view.findViewById(R.id.type_tv);
            infoHolder.point_tv = view.findViewById(R.id.point_tv);
            infoHolder.check_btn = view.findViewById(R.id.check_btn);
            infoHolder.type_icon = view.findViewById(R.id.type_icon);
            view.setTag(infoHolder);
        }
        infoHolder = (FragmentCheckRecord.InfoWindowHolder) view.getTag();

        infoHolder.name_tv.setText(pointInfo.getName());
        infoHolder.type_tv.setText(type.getName());
        infoHolder.point_tv.setText(pointInfo.getFormatLatitude() + ",  " + pointInfo.getFormatLongitude());
        infoHolder.check_btn.setText("查看");
//        infoHolder.type_icon.setImageDrawable(null);
        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(type.getIcon());
        infoHolder.type_icon.setImageBitmap(bitmap);
//        ImageLoader.getInstance().displayImage(type.getIcon(), infoHolder.type_icon);

        LatLng latLnt = pointInfo.getLatLnt();
        latLnt = PositionUtil.GpsToBaiduLatLng(latLnt);

        infoHolder.check_btn.setOnClickListener(v ->
                AddCollectionActivity.start(getContext(), point));

        mInfoWindow = new InfoWindow(view, latLnt, -50);

        return mInfoWindow;
    }

}
