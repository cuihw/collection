package com.data.collection.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.activity.CommonActivity;
import com.data.collection.data.UserTrace;
import com.data.collection.data.greendao.TraceLocation;
import com.data.collection.listener.ITraceListener;
import com.data.collection.util.DateUtils;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.data.collection.util.PositionUtil;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentTrace#} factory method to
 * create an instance of this fragment.
 */
public class FragmentTrace extends FragmentBase {
    private static final String TAG = "FragmentTrace";

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.datetime_layout)
    LinearLayout datetimeLayout;

    @BindView(R.id.starttime)
    TextView dateView;

    // 纹理折线，点击时获取折线上点数及width.
    Polyline mTexturePolyline ;
    private BitmapDescriptor mBlueTexture;

    public static void start(Context context){
        Bundle bundle = new Bundle();
        bundle.putInt(CommonActivity.FRAGMENT, CommonActivity.FRAGMENT_TRACE);
        CommonActivity.start(context, bundle);
    }

    public static FragmentTrace getInstance(){
        return new FragmentTrace();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.fragment_settings_trace, container, false);
        bindButterKnife();

        mBlueTexture = BitmapDescriptorFactory.fromResource(R.mipmap.road_blue);
        initMap();
        initView();
        initListener();

        return view;
    }

    private SupportMapFragment mapFragment;
    BaiduMap mBaiduMap;

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
        mapFragment = SupportMapFragment.newInstance(bo);
        FragmentManager manager = getChildFragmentManager();
        manager.beginTransaction().add(R.id.map_framelayout, mapFragment, "map_fragment").commit();

        view.post(()->{
            mBaiduMap = mapFragment.getMapView().getMap();
            LsLog.w(TAG, "mBaiduMap = " + mBaiduMap);
            getAndShowTrace();
        });
    }

    private void getAndShowTrace() {
        UserTrace.getInstance().getHistoryTrace(0, traceListener);
    }

    ITraceListener traceListener = new ITraceListener(){

        @Override
        public void onTraceList(List<TraceLocation> list) {
            showLocalTrace(0, list);
        }
    };

    private void initView() {


        dateView.setText(DateUtils.formatDate(Calendar.getInstance(),DateUtils.fmtYYYYMMDD));
    }

    private void showLocalTrace(long time ,List<TraceLocation> list) {
        if (list == null || list.size() == 0) {
            UserTrace.getInstance().getDataFromServer(getContext(),time, new ITraceListener(){
                @Override
                public void onTraceList(List<TraceLocation> list) {
                    showTrace(list);
                }
            } );
            return ;
        }
        showTrace(list);
    }
    Map<String, LatLng> traceMap= new HashMap<>();
    private void showTrace(List<TraceLocation> list) {
        if (list == null || list.size() == 0) {
            ToastUtil.showTextToast(getContext(), "当前没有轨迹数据");
            return;
        }
        TraceLocation startLocation = list.get(0);
        double la = Double.parseDouble(startLocation.getLatitude());
        double lo = Double.parseDouble(startLocation.getLongitude());
        // 把地图定位到开始点为中心
        LatLng ll = new LatLng(la, lo);
        traceMap.put(getKey(startLocation.getLatitude(),lo),ll);

        ll = PositionUtil.GpsToBaiduLatLng(ll);

        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(ll).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        List<LatLng> points= new ArrayList<>();

        for (TraceLocation point : list) {
            LsLog.w(TAG, "trace point:" + new Gson().toJson(point));

            double latitude = Double.parseDouble(point.getLatitude());
            double nextlong = Double.parseDouble(point.getLongitude());

            LatLng latLng1 = traceMap.get(getKey(point.getLatitude(), nextlong));

            while (latLng1 != null) {
                nextlong += Constants.DIFF;
                latLng1 = traceMap.get(getKey(point.getLatitude(), nextlong));
            }
            traceMap.put(getKey(point.getLatitude(), nextlong), new LatLng(latitude, nextlong));

            LsLog.w(TAG, "traceMap.put = " + getKey(point.getLatitude(), nextlong));
            // 坐标转换到百度09 BD09LL
            LatLng latLng = PositionUtil.GpsToBaiduLatLng(new LatLng(latitude, nextlong));
            points.add(latLng);
        }

        if (points.size() < 3) { //points count can not less than 2
            points.add(points.get(0));
            ToastUtil.showTextToast(getContext(), "轨迹点小于两个点");
        }

        ooPolyline11 = new PolylineOptions()
                .width(40)
                .points(points)
                .customTexture(mBlueTexture);
        mTexturePolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline11);

    }

    private String getKey(String latitude, double nextlong) {
        return latitude + "|" + nextlong;
    }

    OverlayOptions ooPolyline11;
    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(new ContextThemeWrapper(getActivity(),
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar),
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        dealDate(year, monthOfYear, dayOfMonth);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        DatePicker datePicker1 = datePicker.getDatePicker();
        datePicker1.setMaxDate(System.currentTimeMillis());
        datePicker.show();
    }
    private Calendar calendar = Calendar.getInstance();

    private void dealDate(int year, int monthOfYear, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DATE, dayOfMonth);
        String sDate = DateUtils.formatDate(calendar, DateUtils.fmtYYYYMMDD);
        dateView.setText(sDate);

        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        long time = calendar.getTimeInMillis()/1000;

        UserTrace.getInstance().getHistoryTrace(time, traceListener);
    }



    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->{
            getActivity().finish();
        });
        // 默认当天时间
        datetimeLayout.setOnClickListener(v->showDatePicker());
    }


}
