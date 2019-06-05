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
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.api.track.TrackPoint;
import com.baidu.trace.model.Point;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
            LsLog.i(TAG, "mBaiduMap = " + mBaiduMap);
        });
    }

    // 初始化轨迹监听器
    OnTrackListener mTrackListener = new OnTrackListener() {
        // 历史轨迹回调  HistoryTrackResponse  鉴权失败
        @Override
        public void onHistoryTrackCallback(HistoryTrackResponse response) {
            Point startPoint = response.getStartPoint();
            LsLog.w(TAG, response.message);
            if (startPoint == null) {
                ToastUtil.showTextToast(getContext(), "没有记录轨迹数据");
                return;
            } else {
                if ("成功".equals(response.message))
                ToastUtil.showTextToast(getContext(), "请求轨迹记录" + response.message);
            }

            long locTime = startPoint.getLocTime() * 1000;
            Point endPoint = response.getEndPoint();
            long locTime1 = endPoint.getLocTime()* 1000;
            String starttime = DateUtils.formatTime(locTime, DateUtils.fmtYYYYMMDDhhmmss);
            String endtime = DateUtils.formatTime(locTime1, DateUtils.fmtYYYYMMDDhhmmss);
            LsLog.w(TAG, "start locTime:" + starttime + ", endtimd = " + endtime);
            showTrace(response);
        }
    };



    private void showTrace(HistoryTrackResponse response) {
        Point startPoint = response.getStartPoint();
        com.baidu.trace.model.LatLng location = startPoint.getLocation();
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(ll).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        List<TrackPoint> trackPoints = response.getTrackPoints();
        List<LatLng> points= new ArrayList<>();

        for (TrackPoint point : trackPoints) {
            double latitude = point.getLocation().getLatitude();
            double longitude = point.getLocation().getLongitude();
            points.add(new LatLng(latitude, longitude));
        }
        if (points.size() < 1) {
            ToastUtil.showTextToast(getContext(), "没有轨迹数据");
            return ;
        }
        if (points.size() < 3) { //points count can not less than 2
            points.add(points.get(0));
        }

        OverlayOptions ooPolyline11 = new PolylineOptions()
                .width(20)
                .points(points)
//                .dottedLine(false)
                .customTexture(mBlueTexture);
        mTexturePolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline11);
    }

    private void initView() {
        // 默认当天时间
        //UserTrace.getInstance().getHistoryTrace(0,0, mTrackListener);
        datetimeLayout.setOnClickListener(v->showDatePicker());
        UserTrace.getInstance().getHistoryTrace(0, new ITraceListener(){
            @Override
            public void onTraceList(List<TraceLocation> list) {

            }
        });
    }

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
        getTraceData(calendar); // 请求百度轨迹数据
    }

    private void getTraceData(Calendar calendar) {
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long starttime = calendar.getTimeInMillis() / 1000;

        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endTime = calendar.getTimeInMillis() / 1000;

        // UserTrace.getInstance().getHistoryTrace(starttime, endTime, mTrackListener);
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->{
            getActivity().finish();

        });
    }


}
