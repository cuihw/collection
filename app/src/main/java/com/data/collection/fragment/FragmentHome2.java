package com.data.collection.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.activity.AddCollectionActivity;
import com.data.collection.activity.CollectionListActivity;
import com.data.collection.activity.ColorPaletteActivity;
import com.data.collection.activity.MeasureCollectionListActivity;
import com.data.collection.data.CacheData;
import com.data.collection.data.MapDataUtils;
import com.data.collection.data.UserTrace;
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.dialog.AdjustPosDialog2;
import com.data.collection.dialog.PopupDialog;
import com.data.collection.dialog.PopupInfoWindow;
import com.data.collection.listener.IAdjustPosListener2;
import com.data.collection.listener.IGatherDataListener;
import com.data.collection.module.CollectType;
import com.data.collection.module.MeasurePoint;
import com.data.collection.util.BitmapUtil;
import com.data.collection.util.FileUtils;
import com.data.collection.util.LsLog;
import com.data.collection.util.OffLineMap;
import com.data.collection.util.PreferencesUtils;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.MyPicMarkerSymbol;
import com.data.collection.view.TitleView;
import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.GeoPackage;
import com.esri.arcgisruntime.data.GeoPackageFeatureTable;
import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.data.ShapefileInfo;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.loadable.LoadStatusChangedEvent;
import com.esri.arcgisruntime.loadable.LoadStatusChangedListener;
import com.esri.arcgisruntime.location.AndroidLocationDataSource;
import com.esri.arcgisruntime.location.LocationDataSource;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapScaleChangedEvent;
import com.esri.arcgisruntime.mapping.view.MapScaleChangedListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.WrapAroundMode;
import com.esri.arcgisruntime.raster.GeoPackageRaster;
import com.esri.arcgisruntime.raster.ImageServiceRaster;
import com.esri.arcgisruntime.raster.Raster;
import com.esri.arcgisruntime.raster.RasterRenderer;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import cn.sddman.arcgistool.common.Variable;
import cn.sddman.arcgistool.util.ArcGisMeasure;

import static android.content.Context.SENSOR_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentHome2#} factory method to
 * create an instance of this fragment.
 */
public class FragmentHome2 extends FragmentBase {

    private static final String TAG = "FragmentHome2";
    private static final int GET_BOUNDS = 1;

    private static final int LINE_TYPE = 1;
    private static final int AREA_TYPE = 2;

    int mapType = 1;

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.add_point)
    ImageView addPoint;

    @BindView(R.id.trace_process)
    TextView traceProcess;

    @BindView(R.id.recode_trace)
    TextView recodeTrace;

    @BindView(R.id.measure_layout)
    LinearLayout measureLayout;

    @BindView(R.id.measure_end_layout)
    LinearLayout measureEndLayout;
    @BindView(R.id.measure_clear_layout)
    LinearLayout measureClearLayout;

    @BindView(R.id.map_type)
    TextView mapTypeTv;

    @BindView(R.id.read_tiff)
    TextView readTiff;

    @BindView(R.id.color_palette)
    TextView colorPalette;

    @BindView(R.id.load_local_map)
    TextView loadLocalMap;
    @BindView(R.id.mapview)
    MapView mMapView;
    @BindView(R.id.map_my_position)
    TextView myPosition;
    @BindView(R.id.calibration_coordinate)
    TextView calibrationCoordinate;
    @BindView(R.id.hint_title_tv)
    TextView openationHint;
    @BindView(R.id.measure_action)
    TextView measureAction;
    KProgressHUD hud;
    List<GatherPoint> showCollectMarkerList = new ArrayList<>(); // 当前显示的采集点
    List<GeoPackageRaster> geoPackageRasters;
    List<RasterLayer> geoPackageRasterLayers = new ArrayList<>();  // 离线底图包gpkg
    List<RasterLayer> imageryRasterLayers = new ArrayList<>(); // tiff 图层
    List<FeatureLayer> geoFeatureLayers = new ArrayList<>();  // shp图
    ArcGISMap mArcGISMap;
    GraphicsOverlay mGraphicsOverlay;
    String[] items = new String[]{"开放街区图", "矢量图", "带标签影像图",
            "拓扑图", "灰白底图", "空地图"};
    List<Basemap.Type> mapTypeList = new ArrayList<>();

    Map<String, MyPicMarkerSymbol> myPicMarkerSymbolMap = new HashMap<>();

    Handler handlerScroll = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == GET_BOUNDS) {
                if (FragmentHome2.this.isVisible()) {
                    getMapBounds();
                }
            }
        }
    };
    LocationDisplay locationDisplay;

    List<ShapefileFeatureTable> shapefileFeatureTables = new ArrayList<>();
    GeoPackage geoPackage;

    AdjustPosDialog2 dialog;
    private SensorManager mSensorManager;
    private boolean hasOfflineLay = false;
    private boolean isShowImageLayer = false;
    private MobileMapPackage mapPackage;


    private void refreshOperstionLayer() {
        LayerList operationalLayers = mMapView.getMap().getOperationalLayers();
        operationalLayers.clear();
        operationalLayers.addAll(geoPackageRasterLayers);
        operationalLayers.addAll(imageryRasterLayers);
        operationalLayers.addAll(geoFeatureLayers);
    }

    //private DatumTransformation mSR3857;
    //private SpatialReference mSR3857 = SpatialReference.create(3857);
    // private DatumTransformation mSR3857 = new DatumTransformation(3857)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");

        view = inflater.inflate(R.layout.fragment_home2, container, false);
        bindButterKnife();

        initMap();

        initSensor();

        initView();
        initListener();

        hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true);
        return view;
    }

    private void initView() {
        // 初始化，没有开始记录
        traceProcess.setVisibility(View.INVISIBLE);
        openationHint.setVisibility(View.INVISIBLE);
        measureLayout.setVisibility(View.INVISIBLE);
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

        colorPalette.setOnClickListener(v -> ColorPaletteActivity.start(getContext()));

        measureAction.setOnClickListener(v -> {
            // showMeasureTypeDialog()
            if (measureLayout.getVisibility() == View.VISIBLE) {
                // 测量完成
                measureLayout.setVisibility(View.INVISIBLE);
                Measurehelper.getInstance().getArcGisMeasure().clearMeasure();
                Measurehelper.getInstance().endMeasure();
            } else { // 进入测量模式
                showMeasureTypeDialog();
            }
        });

        measureEndLayout.setOnClickListener(v -> {
            Measurehelper.getInstance().getArcGisMeasure().endMeasure();

            PopupDialog popupDialog = PopupDialog.create(getContext(), "测量保存", "是否保存测量结果", "保存", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }, "取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            popupDialog.show();
        });
        measureClearLayout.setOnClickListener(v -> {
            Measurehelper.getInstance().getArcGisMeasure().clearMeasure();
        });

        //openation Hint
        calibrationCoordinate.setOnClickListener(v -> {
            if (openationHint.getVisibility() == View.GONE) {
                // 在纠偏模式下
                openationHint.setVisibility(View.VISIBLE);
            } else {
                // 普通模式下
                openationHint.setVisibility(View.GONE);
            }
        });
        mMapView.getMap().addBasemapChangedListener(new ArcGISMap.BasemapChangedListener() {
            @Override
            public void basemapChanged(ArcGISMap.BasemapChangedEvent basemapChangedEvent) {
                ArcGISMap source = basemapChangedEvent.getSource();
            }
        });

        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(getContext(), mMapView) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent v) {
                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(v.getX()), Math.round(v.getY()));

                if (measureLayout.getVisibility() == View.VISIBLE) { // 测量模式
                    Variable.DrawType drawType = Measurehelper.getInstance().getDrawType();
                    if (drawType == Variable.DrawType.LINE) {
                        Measurehelper.getInstance().getArcGisMeasure().startMeasuredLength(screenPoint);
                    } else {
                        Measurehelper.getInstance().getArcGisMeasure().startMeasuredArea(screenPoint);
                    }
                    return true;
                }

                Point clickPoint = mMapView.screenToLocation(screenPoint); // 地理坐标点；
                // Project the point to WGS84, using the transformation
                clickPoint = (Point) GeometryEngine.project(clickPoint, SpatialReferences.getWgs84());
                Log.i("sss", clickPoint.toString());
                if (openationHint.getVisibility() == View.VISIBLE) {
                    // 纠偏模式下：
                    Point toPoint = locationDisplay.getLocation().getPosition();
                    showAdjustDialog(clickPoint, toPoint);
                } else {
                    if (isTapGraphicItem(screenPoint)) {
                        return true;
                    }
                }
                return true;
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
            public void onLongPress(MotionEvent v) {
                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(v.getX()), Math.round(v.getY()));
                Point clickPoint = mMapView.screenToLocation(screenPoint); // 地理坐标点；
                // Project the point to WGS84, using the transformation
                clickPoint = (Point) GeometryEngine.project(clickPoint, SpatialReferences.getWgs84());
                // 长按采集点
                AddCollectionActivity.start(getContext(), clickPoint);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                beginScroll();
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });

        mMapView.addMapScaleChangedListener(new MapScaleChangedListener() {
            @Override
            public void mapScaleChanged(MapScaleChangedEvent mapScaleChangedEvent) {
                // Gets the current scale of the map, expressing the relationship between a distance in the MapView
                // and the corresponding distance on the ground.
                // For example, a map scale of 100,000 indicates that one centimeter on the map equates to one kilometer on the ground.
                double mapScale = mMapView.getMapScale();
                LsLog.w(TAG, "mapScale = " + mapScale);

            }
        });

        readTiff.setOnClickListener(v -> {
            if (isShowImageLayer) {
                showImageShpLayer(false);
            } else {
                String fileDir = FileUtils.getFileDir();
                new LFilePicker().withActivity(getActivity())
                        .withRequestCode(Constants.GET_FILE_PATH)
                        .withStartPath(fileDir)
                        //.withFileFilter(new String[]{".txt", ".png", ".docx"})
                        .withFileFilter(new String[]{".tif", ".shp"})
                        .withMutilyMode(false)
                        .withTitle("加载图层文件")
                        .start();
            }
        });

        titleView.getRighticon().setOnClickListener(v -> {
            // 显示采集列表。
            if (CacheData.isLogin()) {
                CollectionListActivity.start(getContext());
            } else {
                ToastUtil.showTextToast(getContext(), "请先登录系统，再进行操作");
            }
        });

        addPoint.setOnClickListener(v -> {
            if (CacheData.isLogin()) {
                AddCollectionActivity.start(getContext());
            } else {
                ToastUtil.showTextToast(getContext(), "请先登录系统，再进行操作");
            }
        });

        recodeTrace.setOnClickListener(v -> clickTraceButton());

        mapTypeTv.setOnClickListener(v -> {
            showChioceDialog();
        });

        loadLocalMap.setOnClickListener(v -> {
            if (hasOfflineLay) {
                removeOffLineLay();
                loadLocalMap.setText("离线\n底图");
            } else {
                String fileDir = FileUtils.getFileDir();
                new LFilePicker().withActivity(getActivity())
                        .withRequestCode(Constants.GET_FILE_PATH)
                        .withStartPath(fileDir)
                        //.withFileFilter(new String[]{".txt", ".png", ".docx"})
                        .withFileFilter(new String[]{".gpkg", ".mmpk"})
                        .withMutilyMode(false)
                        .withTitle("打开离线文件")
                        .start();
            }
        });

        myPosition.setOnClickListener(v -> {
            goToMyLocation();
        });
    }

    private void showInfoWindow(final GatherPoint point) {
        CollectType typeIconUrl = MapDataUtils.getTypeIconUrl(point);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.five_sided);
        if (typeIconUrl != null) {
            bitmap = ImageLoader.getInstance().loadImageSync(typeIconUrl.getIcon());
        }

        String location = point.getLongitude() + ", " + point.getLatitude();
        PopupInfoWindow dialog = PopupInfoWindow.create(getContext(), point.getName(), bitmap, typeIconUrl.getName(), location, "查看", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddCollectionActivity.start(getContext(), point);
            }
        });
        dialog.show();
    }

    private void beginScroll() {
        handlerScroll.removeMessages(GET_BOUNDS);
        handlerScroll.sendEmptyMessageDelayed(GET_BOUNDS, 1000);
    }

    private void getMapBounds() {
        if (mMapView == null) return;
        Polygon visibleArea = mMapView.getVisibleArea();
        if (visibleArea == null) return;
        Envelope extent = visibleArea.getExtent();
        extent = (Envelope) GeometryEngine.project(extent, SpatialReferences.getWgs84());

        double xMin = extent.getXMin(); // lon
        double xMax = extent.getXMax();
        double yMin = extent.getYMin(); // lat
        double yMax = extent.getYMax();
        LsLog.w(TAG, "getMapBounds extent = " + extent.toString());

        MapDataUtils.asyncPointsByBounds(yMax, yMin, xMax, xMin, false, new IGatherDataListener() {
            @Override
            public void onListData(List<GatherPoint> list) {
                showCollectList(list);
            }
        });
    }

    private void showCollectList(List<GatherPoint> list) {
        // showCollectMarkerList = list;
        // 查找出来新的marker点
        List<GatherPoint> newPoints = new ArrayList<>();
        for (GatherPoint point : list) {
            if (!showCollectMarkerList.contains(point)) {
                newPoints.add(point);
            }
        }

        if (newPoints.size() == 0) return;

        for (GatherPoint point : newPoints) {
            MyPicMarkerSymbol markerSymbol = creatMyPicMarker(point);
        }
        showCollectMarkerList.addAll(newPoints);
    }

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
        Context context = getContext();
        if (context == null) return null;
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

    private void showImageShpLayer(boolean isShow) {

    }

    private void setMapType(int mapType) {
        this.mapType = mapType;
        if (mapType == 6) {
            mArcGISMap = new ArcGISMap();
        } else {
            Basemap.Type type = mapTypeList.get(mapType - 1);
            mArcGISMap = new ArcGISMap(type, Constants.latitude,
                    Constants.longitude, Constants.levelOfDetail);
        }

        mArcGISMap.loadAsync();
        mMapView.setMap(mArcGISMap);
        mMapView.setWrapAroundMode(WrapAroundMode.ENABLE_WHEN_SUPPORTED);
        mMapView.buildDrawingCache();
        refresh(MapDataUtils.isNeedAdjust(mapType));

        geoPackageRasterLayers = new ArrayList<>();  // 离线底图包gpkg
        imageryRasterLayers = new ArrayList<>(); // tiff 图层
        geoFeatureLayers = new ArrayList<>();  //
        initMylocaltion();
    }

    private void removeOffLineLay() {
        hasOfflineLay = false;
    }

    private void initMap() {
        // ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud8361042706,none,KGE60RFLTFCJTK118097");
        mapTypeList.add(Basemap.Type.OPEN_STREET_MAP);
        mapTypeList.add(Basemap.Type.STREETS_VECTOR);
        mapTypeList.add(Basemap.Type.IMAGERY_WITH_LABELS_VECTOR);
        mapTypeList.add(Basemap.Type.STREETS);
        mapTypeList.add(Basemap.Type.LIGHT_GRAY_CANVAS_VECTOR);
        mapTypeList.add(Basemap.Type.NAVIGATION_VECTOR);

        if (mMapView != null) {
            mArcGISMap = new ArcGISMap(Basemap.Type.OPEN_STREET_MAP, Constants.latitude,
                    Constants.longitude, Constants.levelOfDetail);
            mArcGISMap.loadAsync();
            mArcGISMap.addLoadStatusChangedListener(new LoadStatusChangedListener() {
                @Override
                public void loadStatusChanged(LoadStatusChangedEvent loadStatusChangedEvent) {
                    ArcGISRuntimeException loadError = mArcGISMap.getLoadError();
                    LsLog.w(TAG, "ArcGISRuntimeException loadError " + loadError.getMessage());
                }
            });

            mMapView.setMap(mArcGISMap);
            mMapView.setWrapAroundMode(WrapAroundMode.ENABLE_WHEN_SUPPORTED);
            mMapView.buildDrawingCache();
            mGraphicsOverlay = new GraphicsOverlay();
            mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

            ArcGisMeasure arcGisMeasure = new ArcGisMeasure(getContext(), mMapView);
            Measurehelper.init(arcGisMeasure);

            mMapView.setViewpointRotationAsync(0);
        }
        initMylocaltion();
    }

    private void initMylocaltion() {

        AndroidLocationDataSource dataSource = new AndroidLocationDataSource(getContext());

        locationDisplay = mMapView.getLocationDisplay();
        LocationDataSource locationDataSource = locationDisplay.getLocationDataSource();
        if (locationDataSource instanceof AndroidLocationDataSource) {
            LsLog.w(TAG, "locationDataSource instanceof AndroidLocationDataSource 。。。 ");
        }

        locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.OFF);
        locationDisplay.startAsync();
        locationDisplay.addLocationChangedListener(new LocationDisplay.LocationChangedListener() {
            @Override
            public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {

            }
        });
        goToMyLocation();
    }

    private void goToMyLocation() {
        // 4326   GCS_WGS_1984  和  102100    WGS_1984_web_mercator_auxiliary_sphere 。  EPSG:3857 -- WGS84 Web Mercator (Auxiliary Sphere)
        LocationDataSource.Location location = locationDisplay.getLocation();
        if (location != null && location.getPosition() != null && mMapView != null) {
            mMapView.setViewpointCenterAsync(location.getPosition());
            mMapView.setViewpointRotationAsync(0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 恢复显示...
        mMapView.resume();
        delayRun(1000);
        // TODO: 查找边界，显示采集点信息

        if (geoFeatureLayers.size() > 0) {
            for (FeatureLayer featureLayer : geoFeatureLayers) {
                setFillColor(featureLayer);
            }
        }
    }

    private void setFillColor(FeatureLayer featureLayer) {
        GeometryType geometryType = featureLayer.getFeatureTable().getGeometryType();
        if (geometryType == GeometryType.POINT) {
            int pointValue = PreferencesUtils.getInt(getContext(), "pointValue", Color.BLUE);
            SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, pointValue, 10);
            SimpleRenderer pointRenderer = new SimpleRenderer(pointSymbol);
            featureLayer.setRenderer(pointRenderer);
        } else if (geometryType == GeometryType.POLYLINE) {
            int lineValue = PreferencesUtils.getInt(getContext(), "lineValue", Color.RED);
            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, lineValue, 2);
            SimpleRenderer lineRenderer = new SimpleRenderer(lineSymbol);
            featureLayer.setRenderer(lineRenderer);
        } else if (geometryType == GeometryType.POLYGON) {
            int polygonValue = PreferencesUtils.getInt(getContext(), "polygonValue", 0x50225500);
            int polygonValueEdge = 0xFF000000 | polygonValue;
            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, polygonValueEdge, 1);
            SimpleRenderer lineRenderer = new SimpleRenderer(lineSymbol);
            featureLayer.setRenderer(lineRenderer);
            SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, polygonValue, lineSymbol);
            SimpleRenderer fillRenderer = new SimpleRenderer(fillSymbol);
            featureLayer.setRenderer(fillRenderer);
        }
    }


    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mMapView != null) {
            mMapView.dispose();
        }
        handlerScroll.removeMessages(GET_BOUNDS);
        super.onDestroy();
    }

    // 实现图片闪烁效果
    private void setFlickerAnimation(View view) {
        final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);
        view.setAnimation(animation);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.GET_FILE_PATH) {
                List<String> list = data.getStringArrayListExtra("paths");
                for (String file : list) {
                    Log.w(TAG, "list get file name: " + file);
                    if (isPictureFile(file)) {
                        loadTiff(file);
                    } else if (isShapeFile(file)) {
                        loadShp(file);
                    } else if (isMmpk(file)) {
                        loadMmpk(file);
                    } else {
                        loadOfflineMapLayer(file);
                    }
                }
            } else if (requestCode == Constants.GET_MEASURE) {
                MeasurePoint[] data1 = (MeasurePoint[]) data.getSerializableExtra("data");
                int type = (int) data.getIntExtra("type", 0);
                measure(type, data1);
            }
        }
    }

    private void loadMmpk(String mmpkFile) {

        //[DocRef: Name=Open Mobile Map Package-android, Category=Work with maps, Topic=Create an offline map]
        // create the mobile map package
        mapPackage = new MobileMapPackage(mmpkFile);
        // load the mobile map package asynchronously
        mapPackage.loadAsync();

        // add done listener which will invoke when mobile map package has loaded
        mapPackage.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                // check load status and that the mobile map package has maps
                if (mapPackage.getLoadStatus() == LoadStatus.LOADED && !mapPackage.getMaps().isEmpty()) {
                    // add the map from the mobile map package to the MapView
                    mMapView.setMap(mapPackage.getMaps().get(0));
                } else {
                    // log an issue if the mobile map package fails to load
                    Log.e(TAG, mapPackage.getLoadError().getMessage());
                }
            }
        });
    }

    private boolean isMmpk(String file) {
        return file.endsWith(".mmpk");
    }

    private void loadShp(String filename) {
        File file = new File(filename);
        if (file.exists() && file.isFile()) {
            ShapefileFeatureTable shapefileFeatureTable = new ShapefileFeatureTable(filename);
            shapefileFeatureTable.loadAsync();
            shapefileFeatureTable.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    if (shapefileFeatureTable.getLoadStatus() == LoadStatus.LOADED) {
                        // create a feature layer to display the shapefile
                        ShapefileInfo info = shapefileFeatureTable.getInfo();
                        shapefileFeatureTables.add(shapefileFeatureTable);

                        FeatureLayer shapefileFeatureLayer = new FeatureLayer(shapefileFeatureTable);
                        setFillColor(shapefileFeatureLayer);

                        geoFeatureLayers.add(shapefileFeatureLayer);  // 加载shp图
                        refreshOperstionLayer();
                        // zoom the map to the extent of the shapefile
                        mMapView.setViewpointAsync(new Viewpoint(shapefileFeatureLayer.getFullExtent()));
                    } else if (shapefileFeatureTable.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                        LsLog.w(TAG, "loadFeatureTables error: ..." + shapefileFeatureTable.getLoadError().getMessage());
                    }
                }
            });
        } else {
            ToastUtil.showTextToast(getContext(), filename + " 不是一个地图文件，请重新选择");
        }
    }

    private boolean isShapeFile(String file) {
        return file.endsWith(".shp");
    }

    private boolean isPictureFile(String file) {
        return file.endsWith(".tif");
    }

    private void loadTiff(String filename) {
        File file = new File(filename);
        if (file.exists() && file.isFile()) {
            Raster imageryRaster = new Raster(filename);
            RasterLayer rasterLayer = new RasterLayer(imageryRaster);
            rasterLayer.loadAsync();
            rasterLayer.setOpacity(0.6f);
            rasterLayer.addDoneLoadingListener(() -> {
                if (rasterLayer.getLoadStatus() == LoadStatus.LOADED) {
                    mMapView.setViewpointAsync(new Viewpoint(rasterLayer.getFullExtent()));
                }
            });
            imageryRasterLayers.add(rasterLayer);
            refreshOperstionLayer();
        } else {
            ToastUtil.showTextToast(getContext(), filename + " 不是一个地图文件，请重新选择");
        }
    }

    // 离线地图图层
    private void loadOfflineMapLayer(String filename) {

        Log.d(TAG, filename);
        File file = new File(filename);
        if (file.exists() && file.isFile()) {
            // Open the GeoPackage
            geoPackage = new GeoPackage(filename);
            geoPackage.loadAsync();
            geoPackage.addDoneLoadingListener(() -> {
                if (geoPackage.getLoadStatus() == LoadStatus.LOADED) {
                    loadGeoPackage(geoPackage);
                } else if (geoPackage.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                    ToastUtil.showTextToast(getContext(), filename + " 加载失败");
                    ArcGISRuntimeException loadError = geoPackage.getLoadError();
                    LsLog.w(TAG, "loadFeatureTables error: ..." + loadError.getMessage());
                }
            });
        } else {
            ToastUtil.showTextToast(getContext(), filename + " 不是一个地图文件，请重新选择");
        }
    }

    private void loadGeoPackage(GeoPackage geoPackage) {
        geoPackageRasters = geoPackage.getGeoPackageRasters();
        boolean empty = geoPackage.getGeoPackageRasters().isEmpty();
        LsLog.w(TAG, "loadGeoPackage ...empty = " + empty);
        loadRasters(geoPackageRasters);
        List<GeoPackageFeatureTable> geoPackageFeatureTables = geoPackage.getGeoPackageFeatureTables();
        loadFeatureTables(geoPackageFeatureTables);
        hasOfflineLay = true;
    }

    private void loadFeatureTables(List<GeoPackageFeatureTable> geoPackageFeatureTables) {
        LsLog.w(TAG, "loadFeatureTables ...");
        if (geoPackageFeatureTables == null || geoPackageFeatureTables.size() == 0) {
            return;
        }
        for (FeatureTable feature : geoPackageFeatureTables) {
            FeatureLayer featureLayer = new FeatureLayer(feature);
            mMapView.getMap().getOperationalLayers().add(featureLayer);
        }
    }

    private void loadRasters(List<GeoPackageRaster> geoPackageRasters) {
        LsLog.w(TAG, "loadRasters ...");
        if (geoPackageRasters == null || geoPackageRasters.size() == 0) {
            return;
        }
        RasterLayer geoPackageRasterLayer = null;
        for (GeoPackageRaster raster : geoPackageRasters) {
            geoPackageRasterLayer = new RasterLayer(raster);
//            RasterRenderer rasterRenderer = new RasterRenderer()
//            geoPackageRasterLayer.setRasterRenderer(rasterRenderer);
//            final ImageServiceRaster imageServiceRaster = new ImageServiceRaster(raster);

            geoPackageRasterLayer.setOpacity(0.4f);

            geoPackageRasterLayers.add(geoPackageRasterLayer);
        }
        refreshOperstionLayer();
    }

    private void delayRun(int i) {
        new Handler().postDelayed(() -> {
            getMapBounds();
        }, i);
    }

    private void showMeasureTypeDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("测量方式");

        String[] measureStr = new String[]{"点屏测量长度", "点屏测量面积", "采集点选取测量"};

        builder.setSingleChoiceItems(measureStr, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // 点屏测量长度
                        Measurehelper.getInstance().setDrawType(Variable.DrawType.LINE);
                        Measurehelper.getInstance().endMeasure();
                        break;
                    case 1:
                        // 点屏测量面积
                        Measurehelper.getInstance().setDrawType(Variable.DrawType.POLYGON);
                        Measurehelper.getInstance().endMeasure();
                        break;
                    case 2:
                        // 选取采集点进行测量
                        MeasureCollectionListActivity.start(getActivity(), Constants.GET_MEASURE);
                        break;
                }
                measureLayout.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // 点屏测量长度
                        Measurehelper.getInstance().setDrawType(Variable.DrawType.LINE);
                        Measurehelper.getInstance().endMeasure();
                        break;
                }
                measureLayout.setVisibility(View.VISIBLE);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    public void showChioceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("选择地图:");

        builder.setSingleChoiceItems(items, mapType - 1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setMapType(which + 1);
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setMapType(which + 1);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    /**
     * @fromPoint 地图上的点击点
     * @toPoint 要调整到的点
     */
    private void showAdjustDialog(Point fromPoint, Point toPoint) {
        dialog = new AdjustPosDialog2(getContext(), new IAdjustPosListener2() {
            @Override
            public void onConfirm(Point fromPoint, Point toPoint) {
                // point
                adjustMapView(fromPoint, toPoint);
                LsLog.w(TAG, "point = " + fromPoint.toString());
                dialog.dismiss();
            }

            @Override
            public void onCancel() {
                dialog.dismiss();
                openationHint.setVisibility(View.GONE);
            }
        });
        dialog.setFromPoint(fromPoint);
        dialog.setToPoint(toPoint);
        dialog.show();
    }

    private void refresh(boolean needAdjust) {

    }

    /**
     * @fromPoint 地图上的点击点
     * @toPoint 要调整到的点
     */
    private void adjustMapView(Point fromPoint, Point toPoint) {
        double deltaLon = toPoint.getX() - fromPoint.getX();
        double deltaLat = toPoint.getY() - fromPoint.getY();
        Log.w(TAG, "deltaLat = " + deltaLat + ", deltaLon  = " + deltaLon);
        MapDataUtils.GOOGLE_ADJUST.adjustLat = deltaLat;
        MapDataUtils.GOOGLE_ADJUST.adjustlng = deltaLon;
        openationHint.setVisibility(View.GONE);
        refresh(true);
    }

    //

    //
    private void measure(int measureType, MeasurePoint[] pointsMeasure) {

        List<Point> list = new ArrayList<>();
        for (MeasurePoint pt : pointsMeasure) {
            String longitude = pt.getLongitude();
            String latitude = pt.getLatitude();
            Point point = new Point(Double.parseDouble(longitude), Double.parseDouble(latitude), SpatialReferences.getWgs84());
            point = (Point) GeometryEngine.project(point, SpatialReferences.getWebMercator());
            list.add(point);
            if (measureType == AREA_TYPE) {
                Measurehelper.getInstance().setDrawType(Variable.DrawType.LINE);
                Measurehelper.getInstance().getArcGisMeasure().startMeasuredArea(point);
            } else {
                Measurehelper.getInstance().setDrawType(Variable.DrawType.POLYGON);
                Measurehelper.getInstance().getArcGisMeasure().startMeasuredLength(point);
            }
        }

        Measurehelper.getInstance().getArcGisMeasure().endMeasure();
    }

    public static class Measurehelper {
        static Measurehelper instance;
        private Variable.DrawType drawType = Variable.DrawType.LINE;

        private ArcGisMeasure arcGisMeasure;

        private Measurehelper(ArcGisMeasure arcGisMeasure) {
            this.arcGisMeasure = arcGisMeasure;
        }

        public static void init(ArcGisMeasure arcGisMeasure) {
            instance = new Measurehelper(arcGisMeasure);
        }

        public static Measurehelper getInstance() {
            return instance;
        }

        public Variable.DrawType getDrawType() {
            return drawType;
        }

        public void setDrawType(Variable.DrawType drawType) {
            this.drawType = drawType;
        }

        public ArcGisMeasure getArcGisMeasure() {
            return arcGisMeasure;
        }

        public void setArcGisMeasure(ArcGisMeasure arcGisMeasure) {
            this.arcGisMeasure = arcGisMeasure;
        }

        public void  startMeasured(android.graphics.Point screenPoint){

        }

        public void endMeasure() {
            arcGisMeasure.endMeasure();
        }
    }

}
