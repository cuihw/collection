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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.activity.AddCheckReportActivitiy;
import com.data.collection.activity.CheckReportListActivitiy;
import com.data.collection.data.CacheData;
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
 * Use the {@link FragmentCheckRecord#} factory method to
 * create an instance of this fragment.
 */
public class FragmentCheckRecord extends FragmentBase {
    private static final String TAG = "FragmentCheckRecord";

    @BindView(R.id.mapview)
    TextureMapView mMapView;
    @BindView(R.id.map_my_position)
    TextView myPosition;
    @BindView(R.id.title_view)
    TitleView titleView;

    BaiduMap mBaiduMap;
    BitmapDescriptor mMarkerBitmap;
    private LocationClient mLocClient;

    private Map<String, LatLng> markerMap = new HashMap<>();

    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    private boolean isFirstLoc = true;

    private MyLocationData locData;

    List<GatherPoint> dataList;

    private InfoWindow mInfoWindow;
    private View infoView;

    SensorEventListener sensorEventListener = new SensorEventListener() {
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
    private BDLocationListener myListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置

            if (location == null || isHidden()) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            mCurrentAccracy = 0;
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
                builder.target(ll).zoom(16.0f);

                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");

        view = inflater.inflate(R.layout.fragment_home_check, container, false);

        bindButterKnife();
        initMap();
        initMyLocation();
        initSensor();

//        initCheckPoint();
        initListener();
        infoView = creatInfoView();
        return view;
    }

    private void initListener() {

        titleView.getRighticon().setOnClickListener(v-> CheckReportListActivitiy.start(getContext()));
        myPosition.setOnClickListener(v -> goToMyLocation());

        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                LsLog.w(TAG, "setOnMapClickListener = " + latLng);
                hideInfoWindow();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Bundle extraInfo = marker.getExtraInfo();
                GatherPoint gatherPoint = (GatherPoint)extraInfo.getSerializable("GatherPoint");
                LsLog.w(TAG, "setOnMarkerClickListener = " + gatherPoint.getName() + ", marker id = " + marker.getId());

                mInfoWindow = createInfoWindow(infoView, gatherPoint);

                if (mInfoWindow != null) mBaiduMap.showInfoWindow(mInfoWindow);

                return false;
            }
        });


        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
                LatLngBounds bound = mapStatus.bound;
                LsLog.w(TAG, "onMapStatusChangeStart....bound = ： " + bound);
                getInBoundsData(bound);
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
                                GatherPointDao.Properties.Longitude.le("" + doubles[3]),
                                GatherPointDao.Properties.IsUploaded.eq(true))
                        .orderDesc(GatherPointDao.Properties.Updated_at);
                dataList = qb.list(); // 查出当前对应的数据
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
                mBaiduMap.clear();
                mBaiduMap.addOverlays(list);
            }
        }.execute(latitude1, latitude2, longitude1,longitude2);



    }
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

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_point_marker, null);
        TextView name = view.findViewById(R.id.name_tv);
        ImageView icon = view.findViewById(R.id.icon_iv);

        if (collectType != null) {
            name.setText(gatherPoint.getName());
            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(collectType.getIcon());
            if (bitmap == null) {
                icon.setImageResource(R.drawable.icon_gcoding);
            } else {
                icon.setImageBitmap(bitmap);
            }
            return BitmapDescriptorFactory.fromView(view);
        }
        name.setText("未命名");
        icon.setImageResource(R.drawable.icon_gcoding);
        return BitmapDescriptorFactory.fromView(view);
    }

    private void initSensor() {
        mSensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);// 获取传感器管理服务
    }

    private void initMyLocation() {
        MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;

        // 定位点，默认的定位点图标为null
        mBaiduMap.setMyLocationConfigeration(
                new MyLocationConfiguration(mCurrentMode, true, null));
        mBaiduMap.setMyLocationEnabled(true);
        mLocClient = new LocationClient(getContext());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    private void initMap() {

        MapStatus.Builder builder = new MapStatus.Builder();
        Location location = LocationController.getInstance().getLocation();
        if (location != null) {
            LatLng p = PositionUtil.GpsToBaiduLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
            builder.target(p);
        }

        builder.overlook(-20).zoom(16); // 数值越大，地图显示的范围越大。

        mBaiduMap = mMapView.getMap();

        mMapView.showZoomControls(false);

        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    private void goToMyLocation() {

        MapStatus.Builder builder = new MapStatus.Builder();
        Location location = LocationController.getInstance().getLocation();
        if (location != null) {
            LatLng p = PositionUtil.GpsToBaiduLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
            builder.target(p);
        }

        if (mBaiduMap == null) mBaiduMap = mMapView.getMap();

        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    @Override
    public void onPause() {
        // MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
        mMapView.onPause();
        mSensorManager.unregisterListener(sensorEventListener);
        super.onPause();
    }

    @Override
    public void onResume() {
        // MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
        super.onResume();
        mMapView.onResume();
        mSensorManager.registerListener(sensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);

        if (mBaiduMap != null) {
            getInBoundsData(mBaiduMap.getMapStatus().bound);
        }
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    private View creatInfoView() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.info_window, null);
        return view;
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

        InfoWindowHolder infoHolder = null;
        if (view.getTag() == null) {
            infoHolder = new InfoWindowHolder();
            infoHolder.name_tv = view.findViewById(R.id.name_tv);
            infoHolder.type_tv = view.findViewById(R.id.type_tv);
            infoHolder.point_tv = view.findViewById(R.id.point_tv);
            infoHolder.check_btn = view.findViewById(R.id.check_btn);
            infoHolder.type_icon = view.findViewById(R.id.type_icon);
            view.setTag(infoHolder);
        }
        infoHolder = (InfoWindowHolder) view.getTag();

        infoHolder.name_tv.setText(pointInfo.getName());
        infoHolder.type_tv.setText(type.getName());
        infoHolder.point_tv.setText(pointInfo.getFormatLatitude() + ",  " + pointInfo.getFormatLongitude());

//        infoHolder.type_icon.setImageDrawable(null);
        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(type.getIcon());
        infoHolder.type_icon.setImageBitmap(bitmap);
//        ImageLoader.getInstance().displayImage(type.getIcon(), infoHolder.type_icon);

        LatLng latLnt = pointInfo.getLatLnt();
        latLnt = PositionUtil.GpsToBaiduLatLng(latLnt);

        infoHolder.check_btn.setOnClickListener(v -> checkPointRecord(point));

        mInfoWindow = new InfoWindow(view, latLnt, -50);

        return mInfoWindow;
    }

    private void checkPointRecord(GatherPoint pointInfo) {
        LsLog.w(TAG, "checkPointRecord..");
        // 添加检查记录
        AddCheckReportActivitiy.start(getContext(),pointInfo);

        hideInfoWindow();
    }

    private void hideInfoWindow() {
        if (mInfoWindow != null) {
            mBaiduMap.hideInfoWindow();
            mMapView.postInvalidate();
        }
    }

    public static class InfoWindowHolder {
        public TextView name_tv;
        public TextView type_tv;
        public TextView point_tv;
        public Button check_btn;
        public ImageView type_icon;
    }


}
