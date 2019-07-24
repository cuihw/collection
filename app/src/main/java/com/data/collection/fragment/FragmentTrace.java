package com.data.collection.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.activity.CommonActivity;
import com.data.collection.data.UserTrace;
import com.data.collection.data.greendao.TraceLocation;
import com.data.collection.listener.ITraceListener;
import com.data.collection.util.DateUtils;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.WrapAroundMode;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

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

    @BindView(R.id.mapview)
    MapView mMapView;
    private ArcGISMap mArcGISMap;
    private GraphicsOverlay mGraphicsOverlay;

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

        initMap();
        initView();
        initListener();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    public void onPause() {
        mMapView.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.dispose();
        super.onDestroy();
    }

    private void initMap() {
        if (mMapView != null) {
            mArcGISMap = new ArcGISMap(Basemap.Type.OPEN_STREET_MAP, Constants.latitude,
                    Constants.longitude, Constants.levelOfDetail);
            mMapView.setMap(mArcGISMap);
            mMapView.setWrapAroundMode(WrapAroundMode.ENABLE_WHEN_SUPPORTED);
            mMapView.buildDrawingCache();
            mGraphicsOverlay = new GraphicsOverlay();
            mMapView.getGraphicsOverlays().add(mGraphicsOverlay);
        }
        getAndShowTrace();
    }

    private void getAndShowTrace() {
        UserTrace.getInstance().getHistoryTrace(0, traceListener);
    }

    ITraceListener traceListener = (list)->{showLocalTrace(0, list);};

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

    Map<String, TraceLocation> traceMap = new HashMap<>();

    private void showTrace(List<TraceLocation> list) {
        mMapView.getGraphicsOverlays().clear();
        mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

        if (list == null || list.size() == 0) {
            ToastUtil.showTextToast(getContext(), "当前没有轨迹数据");
            return;
        }
        ToastUtil.showTextToast(getContext(), "您有" + list.size() + "条轨迹数据");
        TraceLocation startLocation = list.get(0);
        double lai = Double.parseDouble(startLocation.getLatitude());
        double lon = Double.parseDouble(startLocation.getLongitude());
        // 把地图定位到开始点为中心
        Point startPoint = new Point(lon, lai, SpatialReferences.getWgs84());
        startPoint = (Point) GeometryEngine.project(startPoint, SpatialReferences.getWebMercator());
        mMapView.setViewpointCenterAsync(startPoint);

        // line graphic
        PolylineBuilder lineGeometry = new PolylineBuilder(SpatialReferences.getWgs84());

        SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.BLUE, 5);
        GraphicsOverlay pointGraphicOverlay = new GraphicsOverlay();
        SimpleRenderer pointRenderer = new SimpleRenderer(pointSymbol);
        pointGraphicOverlay.setRenderer(pointRenderer);
        for (TraceLocation tracePoint: list) {
            lai = Double.parseDouble(tracePoint.getLatitude());
            lon = Double.parseDouble(tracePoint.getLongitude());
            Point point =  new Point(lon,lai,SpatialReferences.getWgs84());
            lineGeometry.addPoint(point);

            Graphic pointGraphic = new Graphic(point);
            pointGraphicOverlay.getGraphics().add(pointGraphic);
        }

        mMapView.getGraphicsOverlays().add(pointGraphicOverlay);

        // solid blue line symbol
        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.GREEN, 2);
        // create graphic for polyline
        Graphic lineGraphic = new Graphic(lineGeometry.toGeometry());
        SimpleRenderer lineRenderer = new SimpleRenderer(lineSymbol);
        mGraphicsOverlay.setRenderer(lineRenderer);
        mGraphicsOverlay.getGraphics().add(lineGraphic);
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
