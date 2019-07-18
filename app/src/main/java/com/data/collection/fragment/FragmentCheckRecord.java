package com.data.collection.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.activity.AddCheckReportActivitiy;
import com.data.collection.activity.CheckReportListActivitiy;
import com.data.collection.data.MapDataUtils;
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.dialog.PopupInfoWindow;
import com.data.collection.listener.IGatherDataListener;
import com.data.collection.module.CollectType;
import com.data.collection.util.BitmapUtil;
import com.data.collection.util.LsLog;
import com.data.collection.view.MyPicMarkerSymbol;
import com.data.collection.view.TitleView;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.location.LocationDataSource;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.WrapAroundMode;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentCheckRecord#} factory method to
 * create an instance of this fragment.
 */
public class FragmentCheckRecord extends FragmentBase {
    private static final String TAG = "FragmentCheckRecord";
    private static final int GET_BOUNDS = 1;
    private static final int LINE_TYPE = 1;
    private static final int AREA_TYPE = 2;

    @BindView(R.id.mapview)
    MapView mMapView;
    @BindView(R.id.map_my_position)
    TextView myPosition;
    @BindView(R.id.title_view)
    TitleView titleView;

    List<GatherPoint> dataList;
    private ArcGISMap mArcGISMap;
    private GraphicsOverlay mGraphicsOverlay;
    private LocationDisplay locationDisplay;
    private List<GatherPoint> showCollectMarkerList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");

        view = inflater.inflate(R.layout.fragment_home_check, container, false);
        bindButterKnife();

        initMap();
        initListener();
        return view;
    }

    private void initListener() {
        titleView.getRighticon().setOnClickListener(v -> CheckReportListActivitiy.start(getContext()));
        myPosition.setOnClickListener(v -> goToMyLocation());

        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(getContext(), mMapView) {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent v) {
                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(v.getX()), Math.round(v.getY()));
                if (isTapGraphicItem(screenPoint)) {
                    return true;
                }
                return false;
            }

            private boolean isTapGraphicItem(android.graphics.Point screenPoint) {
                final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic =
                        mMapView.identifyGraphicsOverlayAsync(mGraphicsOverlay, screenPoint, 10.0,
                                false, 2);
                try {
                    IdentifyGraphicsOverlayResult identifyGraphicsOverlayResult = identifyGraphic.get();
                    List<Graphic> graphic = identifyGraphicsOverlayResult.getGraphics();
                    if (!graphic.isEmpty()) {
                        Graphic graphic1 = graphic.get(0);
                        String gatherPoint = (String) graphic1.getAttributes().get("GatherPoint");
                        GatherPoint point = new Gson().fromJson(gatherPoint, GatherPoint.class);
                        showInfoWindow(point);
                        return true;
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                beginScroll();
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });
    }

    Handler handlerScroll = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == GET_BOUNDS) {
                if (FragmentCheckRecord.this.isVisible()) {
                    getInBoundsData();
                }
            }
        }
    };

    private void beginScroll() {

        handlerScroll.removeMessages(GET_BOUNDS);
        handlerScroll.sendEmptyMessageDelayed(GET_BOUNDS, 1000);
    }

    private void showInfoWindow(GatherPoint point) {
        CollectType typeIconUrl = MapDataUtils.getTypeIconUrl(point);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.five_sided);
        if (typeIconUrl != null) {
            bitmap = ImageLoader.getInstance().loadImageSync(typeIconUrl.getIcon());
        }

        String location = point.getLongitude() + ", " + point.getLatitude();
        PopupInfoWindow dialog = PopupInfoWindow.create(getContext(), point.getName(), bitmap,
                typeIconUrl.getName(), location, "检查", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AddCheckReportActivitiy.start(getContext(), point);
                    }
                });
        dialog.show();
    }

    private void getInBoundsData() {
        //java.lang.NullPointerException: Attempt to invoke virtual method 'com.esri.arcgisruntime.geometry.Polygon
        // com.esri.arcgisruntime.mapping.view.MapView.getVisibleArea()'
        if (mMapView == null) return;
        Polygon visibleArea = mMapView.getVisibleArea();

        Envelope extent = visibleArea.getExtent();
        extent = (Envelope) GeometryEngine.project(extent, SpatialReferences.getWgs84());

        double xMin = extent.getXMin(); // lon
        double xMax = extent.getXMax();
        double yMin = extent.getYMin(); // lat
        double yMax = extent.getYMax();
        LsLog.w(TAG, "getMapBounds extent = " + extent.toString());

        MapDataUtils.asyncPointsByBounds(yMax, yMin, xMax, xMin, true, new IGatherDataListener() {
            @Override
            public void onListData(List<GatherPoint> list) {
                showCollectList(list);
            }
        });
    }

    private void showCollectList(List<GatherPoint> list) {
        if (!FragmentCheckRecord.this.isVisible()) return;
        List<GatherPoint> newPoints = new ArrayList<>();
        for (GatherPoint point : list) {
            if (!showCollectMarkerList.contains(point)) {
                newPoints.add(point);
            }
        }

        if (newPoints.size() == 0) return;

        for (GatherPoint point : newPoints) {
            creatMyPicMarker(point);
        }
        showCollectMarkerList.addAll(newPoints);
    }

    Map<String, MyPicMarkerSymbol> myPicMarkerSymbolMap = new HashMap<>();

    private MyPicMarkerSymbol creatMyPicMarker(GatherPoint point) {

        MyPicMarkerSymbol symbol = new MyPicMarkerSymbol(point);
        double latitude = symbol.getLatitude();
        double nextlng = symbol.getLongitude();
        String key = symbol.genericKey(latitude, nextlng);

        MyPicMarkerSymbol existMarker = myPicMarkerSymbolMap.get(key);
        while (existMarker != null) {
            nextlng = nextlng + Constants.DIFF2;
            existMarker = myPicMarkerSymbolMap.get(symbol.genericKey(latitude, nextlng));
        }
        symbol.setGeoPoint(latitude, nextlng);
        myPicMarkerSymbolMap.put(symbol.genericKey(latitude, nextlng), symbol);

        PictureMarkerSymbol pictureMarkerSymbol = new PictureMarkerSymbol(createMarkerBitmap(point));
        pictureMarkerSymbol.loadAsync();
        pictureMarkerSymbol.addDoneLoadingListener(() -> {
            if (pictureMarkerSymbol.getLoadStatus() == LoadStatus.LOADED) {
                LsLog.w(TAG, "load pictureMarkerSymbol");
            }
        });

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("GatherPoint", new Gson().toJson(point));
        Graphic campsiteGraphic = new Graphic(symbol.getGeoPoint(), attributes, pictureMarkerSymbol);
        mGraphicsOverlay.getGraphics().add(campsiteGraphic);
        symbol.setMyPicture(pictureMarkerSymbol);

        return symbol;
    }

    private BitmapDrawable createMarkerBitmap(GatherPoint point) {
        View view = View.inflate(getContext(), R.layout.view_point_marker, null);
        TextView viewById = view.findViewById(R.id.name_tv);
        viewById.setText(point.getName());
        CollectType typeIconUrl = MapDataUtils.getTypeIconUrl(point);
        if (typeIconUrl != null) {
            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(typeIconUrl.getIcon());
            ImageView imageView = view.findViewById(R.id.icon_iv);
            imageView.setImageBitmap(bitmap);
        }
        Bitmap bitmap = BitmapUtil.convertViewToBitmap(view);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        return drawable;
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
        initMylocaltion();
    }

    private void initMylocaltion() {
        locationDisplay = mMapView.getLocationDisplay();
        locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
        locationDisplay.startAsync();
        locationDisplay.addLocationChangedListener(new LocationDisplay.LocationChangedListener() {
            @Override
            public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {
            }
        });
        goToMyLocation();
    }

    private void goToMyLocation() {
        LocationDataSource.Location location = locationDisplay.getLocation();
        if (location != null && location.getPosition() != null && mMapView != null)
            mMapView.setViewpointCenterAsync(location.getPosition());
    }

    private void measure(int measureType, List<Point> list) {
        PointCollection points = new PointCollection(list);

        switch (measureType) {
            case LINE_TYPE:
                Polyline line = new Polyline(points);
                GeometryEngine.length(line);
                break;
            case AREA_TYPE:
                Polygon polygon = new Polygon(points);
                double area = GeometryEngine.area(polygon);
                break;
        }
    }

    @Override
    public void onPause() {
        mMapView.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.resume();
        delayRun(1000);
    }

    private void delayRun(int i) {
        new Handler().postDelayed(() -> {
            getInBoundsData();
            goToMyLocation();
        }, i);
    }

    @Override
    public void onDestroy() {
        mMapView.dispose();
        handlerScroll.removeMessages(GET_BOUNDS);
        handlerScroll = null;
        super.onDestroy();
    }


}
