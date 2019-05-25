package com.data.collection.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.activity.CommonActivity;
import com.data.collection.util.LsLog;
import com.data.collection.view.TitleView;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentCompass#} factory method to
 * create an instance of this fragment.
 */
public class FragmentCompass extends FragmentBase implements SensorEventListener {
    private static final String TAG = "FragmentCompass";

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.iv_compass)
    ImageView mIvCompass;

    @BindView(R.id.tv_coord)
    TextView mTvCoord;
    @BindView(R.id.ll_location)
    LinearLayout mLlLocation;
    @BindView(R.id.tv_altitude)
    TextView mTvAltitude;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagneticField;
    private LocationManager mLocationManager;
    private String mLocationProvider;// 位置提供者名称，GPS设备还是网络
    private float mCurrentDegree = 0f;
    private float[] mAccelerometerValues = new float[3];
    private float[] mMagneticFieldValues = new float[3];
    private float[] mValues = new float[3];
    private float[] mMatrix = new float[9];


    public static void start(Context context){
        Bundle bundle = new Bundle();
        bundle.putInt(CommonActivity.FRAGMENT, CommonActivity.FRAGMENT_COMPASS);
        CommonActivity.start(context, bundle);
    }

    public static FragmentCompass getInstance(){
        return new FragmentCompass();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.fragment_tools_compass, container, false);
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

    @Override
    public void onResume() {
        super.onResume();
        registerSensorService();
        updateLocationService();
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

    private void registerSensorService() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initService() {
        initSensorService();
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

    private void initSensorService() {
        mSensorManager = (SensorManager) getContext().getSystemService(Activity.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccelerometerValues = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mMagneticFieldValues = event.values;
        }

        //调用getRotaionMatrix获得变换矩阵mMatrix[]
        SensorManager.getRotationMatrix(mMatrix, null, mAccelerometerValues, mMagneticFieldValues);
        SensorManager.getOrientation(mMatrix, mValues);
        //经过SensorManager.getOrientation(R, values);得到的values值为弧度
        //values[0]  ：azimuth 方向角，但用（磁场+加速度）得到的数据范围是（-180～180）,也就是说，0表示正北，90表示正东，180/-180表示正南，-90表示正西。
        // 而直接通过方向感应器数据范围是（0～359）360/0表示正北，90表示正东，180表示正南，270表示正西。
        float degree = (float) Math.toDegrees(mValues[0]);
        setImageAnimation(degree);
        mCurrentDegree = -degree;
        LsLog.i(TAG, "values[0] = " + mValues[0]);
        LsLog.i(TAG, "degree = " + degree);
    }

    // 设置指南针图片的动画效果
    private void setImageAnimation(float degree) {
        RotateAnimation ra = new RotateAnimation(mCurrentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        ra.setDuration(200);
        ra.setFillAfter(true);
        mIvCompass.startAnimation(ra);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPause() {
        unregister();
        super.onPause();
    }

    private void unregister() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }
}
