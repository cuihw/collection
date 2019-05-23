package com.data.collection.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.activity.CommonActivity;
import com.data.collection.util.LsLog;
import com.data.collection.view.TitleView;
import com.esri.arcgisruntime.geometry.Point;

import java.util.Iterator;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentGpsInfo#} factory method to
 * create an instance of this fragment.
 */
public class FragmentGpsInfo extends FragmentBase {
    private static final String TAG = "FragmentGpsInfo";

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.iv_compass)
    ImageView mIvCompass;

    @BindView(R.id.satellite_layout)
    FrameLayout satelliteLayout;

    @BindView(R.id.tv_coord)
    TextView mTvCoord;
    @BindView(R.id.ll_location)
    LinearLayout mLlLocation;
    @BindView(R.id.tv_altitude)
    TextView mTvAltitude;

    @BindView(R.id.log)
    TextView log;


    private LocationManager mLocationManager;
    private String mLocationProvider;// 位置提供者名称，GPS设备还是网络

    public static void start(Context context){
        Bundle bundle = new Bundle();
        bundle.putInt(CommonActivity.FRAGMENT, CommonActivity.FRAGMENT_GPS);
        CommonActivity.start(context, bundle);
    }

    public static FragmentGpsInfo getInstance(){
        return new FragmentGpsInfo();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.fragment_tools_gps, container, false);
        bindButterKnife();
        initService();
        initListener();
        return view;
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->{
            getActivity().finish();
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        updateLocationService();

        mLocationManager.addGpsStatusListener(gpsStatusListener);
    }



    private GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            satelliteLayout.removeAllViews();

            switch (event) {
                // 卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    // 获取当前状态
                    @SuppressLint("MissingPermission")
                    GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                    // 获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    // 获取所有的卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    // 卫星颗数统计
                    int count = 0;
                    StringBuilder sb = new StringBuilder();
                    while (iters.hasNext() && count <= maxSatellites) {
                        count++;
                        GpsSatellite gpsSatellite = iters.next();
                        //卫星的信噪比
                        float snr = gpsSatellite.getSnr();
                        sb.append("第").append(count).append("颗").append("卫星的信噪比：").append(snr).append("\n");
                        //public float getAzimuth ()
                        //返回卫星的方位角，方位角范围0至360度。
                        showSatellite(gpsSatellite);
                    }
                    Log.e(TAG, sb.toString());
                    log.setText(sb.toString());
                    break;
                default:
                    break;
            }
        }
    };

    private void showSatellite(GpsSatellite gpsSatellite) {
        float azimuth = gpsSatellite.getAzimuth();// 返回卫星的方位角，方位角范围0至360度。(角度)
        float elevation = gpsSatellite.getElevation();// 返回卫星的高度角，高度角范围0至90度 (远近)
        Point point = getPosition(azimuth, elevation);

        ImageView imageView = new ImageView(getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(50, 50);

        layoutParams.gravity = Gravity.LEFT|Gravity.TOP;
        layoutParams.leftMargin = (int)point.getX();
        layoutParams.topMargin = (int)point.getY();

        imageView.setImageResource(R.mipmap.satellite);
        imageView.setLayoutParams(layoutParams);
        satelliteLayout.addView(imageView);
    }

    private Point getPosition(float azimuth, float elevation) {
        // azimuth 返回卫星的方位角，方位角范围0至360度。(角度)
        // elevation 返回卫星的高度角，高度角范围0至90度 (远近)
        LsLog.i(TAG, "getPosition " + azimuth + ", " + elevation);
        float width = mIvCompass.getWidth();
        float radius = width / 2;
        radius = elevation * radius / 90;
        double x = radius * Math.cos(azimuth);
        double y = radius * Math.sin(azimuth);
        x = width / 2  + x;
        y = width / 2 - y;
        Point point = new Point(x,y);
        LsLog.i(TAG, "point " + point.getX() + ", " + point.getY());
        LsLog.i(TAG, "radius " +radius);

        return point;
    }

    @SuppressLint("MissingPermission")
    private void updateLocationService() {
        if (mLocationProvider != null) {
            updateLocation(mLocationManager.getLastKnownLocation(mLocationProvider));
            mLocationManager.requestLocationUpdates(mLocationProvider, 2000, 10, mLocationListener);// 2秒或者距离变化10米时更新一次地理位置
        } else {
            mTvCoord.setText(R.string.cannot_get_location);
        }
    }

    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (status != LocationProvider.OUT_OF_SERVICE) {
                updateLocation(mLocationManager.getLastKnownLocation(mLocationProvider));
            } else {
                mTvCoord.setText(R.string.check_location_permission);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    /**
     * 更新位置信息
     */
    @SuppressLint("StringFormatMatches")
    private void updateLocation(Location location) {
        Log.e(TAG, "location = " + location);
        if (null == location) {
            mTvCoord.setText(getString(R.string.cannot_get_location));
            mTvAltitude.setVisibility(View.GONE);
        } else {
            mTvAltitude.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            double altitude = location.getAltitude();
            if (latitude >= 0.0f) {
                sb.append(getString(R.string.location_north, latitude));
            } else {
                sb.append(getString(R.string.location_south, (-1.0 * latitude)));
            }

            sb.append("    ");

            if (longitude >= 0.0f) {
                sb.append(getString(R.string.location_east, longitude));
            } else {
                sb.append(getString(R.string.location_west, (-1.0 * longitude)));
            }
            mTvCoord.setText(getString(R.string.correct_coord, sb.toString()));
            mTvAltitude.setText(getString(R.string.correct_altitude, altitude));
        }

    }

    private void initService() {
        initLocationService();
    }


    private void initLocationService() {
        mLocationManager = (LocationManager) getContext().getSystemService(Activity.LOCATION_SERVICE);
        Criteria criteria = new Criteria();// 条件对象，即指定条件过滤获得LocationProvider
        criteria.setAccuracy(Criteria.ACCURACY_FINE);// 较高精度
        criteria.setAltitudeRequired(true);// 是否需要高度信息
        criteria.setBearingRequired(true);// 是否需要方向信息
        criteria.setCostAllowed(true);// 是否产生费用
        criteria.setPowerRequirement(Criteria.POWER_LOW);// 设置低电耗
        mLocationProvider = mLocationManager.getBestProvider(criteria, true);// 获取条件最好的Provider,若没有权限，mLocationProvider 为null
        Log.e(TAG, "mLocationProvider = " + mLocationProvider);


    }


    @Override
    public void onPause() {
        unregister();
        super.onPause();
    }

    private void unregister() {

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
            mLocationManager.removeGpsStatusListener(gpsStatusListener);
        }
    }
}
