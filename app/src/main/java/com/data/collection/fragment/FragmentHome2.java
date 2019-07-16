package com.data.collection.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
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
import android.widget.TextView;
import android.widget.Toast;

import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.activity.AddCollectionActivity;
import com.data.collection.activity.CollectionListActivity;
import com.data.collection.data.CacheData;
import com.data.collection.data.MapDataUtils;
import com.data.collection.data.UserTrace;
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.dialog.AdjustPosDialog2;
import com.data.collection.listener.IAdjustPosListener2;
import com.data.collection.listener.IGatherDataListener;
import com.data.collection.module.CollectType;
import com.data.collection.util.BitmapUtil;
import com.data.collection.util.FileUtils;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.MyPicMarkerSymbol;
import com.data.collection.view.TitleView;
import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.GeoPackage;
import com.esri.arcgisruntime.data.GeoPackageFeatureTable;
import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
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
import com.esri.arcgisruntime.raster.Raster;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.util.ListenableList;
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

import static android.content.Context.SENSOR_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentHome2#} factory method to
 * create an instance of this fragment.
 */
public class FragmentHome2 extends FragmentBase {

    private static final String TAG = "FragmentHome";
    private static final int GET_BOUNDS = 1;

    int mapType = 1;

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

    @BindView(R.id.read_tiff)
    TextView readTiff;

    @BindView(R.id.load_local_map)
    TextView loadLocalMap;

    private SensorManager mSensorManager;

    @BindView(R.id.mapview)
    MapView mMapView;

    @BindView(R.id.map_my_position)
    TextView myPosition;

    @BindView(R.id.calibration_coordinate)
    TextView calibrationCoordinate;

    private View infoView;
    private boolean hasOfflineLay = false;

    @BindView(R.id.hint_title_tv)
    TextView openationHint;

    KProgressHUD hud;

    List<GatherPoint> showCollectMarkerList = new ArrayList<>(); // 当前显示的采集点

    List<GeoPackageRaster> geoPackageRasters;
    List<RasterLayer> geoPackageRasterLayers = new ArrayList<>();  // 离线底图包gpkg
    List<RasterLayer> imageryRasterLayers = new ArrayList<>(); // tiff 图层
    List<FeatureLayer> geoFeatureLayers = new ArrayList<>();  //

    ArcGISMap mArcGISMap;

    private void refreshOperstionLayer() {
        LayerList operationalLayers = mMapView.getMap().getOperationalLayers();
        operationalLayers.clear();
        operationalLayers.addAll(geoPackageRasterLayers);
        operationalLayers.addAll(imageryRasterLayers);
        operationalLayers.addAll(geoFeatureLayers);
    }

    GraphicsOverlay mGraphicsOverlay;

    String[] items = new String[]{"开放街区图", "矢量图", "带标签影像图",
            "拓扑图", "灰白底图"};

    List<Basemap.Type> mapTypeList = new ArrayList<>();

    private boolean isShowImageLayer = false;

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

        infoView = creatInfoView();

        hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true);
        return view;
    }

    private View creatInfoView() {
        return LayoutInflater.from(getActivity()).inflate(R.layout.info_window, null);
    }

    private void initView() {
        // 初始化，没有开始记录
        traceProcess.setVisibility(View.INVISIBLE);
        openationHint.setVisibility(View.INVISIBLE);
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
                        String gatherPoint = (String)graphic1.getAttributes().get("GatherPoint");
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
                        .withFileFilter(new String[]{".gpkg"})
                        .withMutilyMode(false)
                        .withTitle("打开离线文件")
                        .start();
            }
        });

        myPosition.setOnClickListener(v -> {
            goToMyLocation();
        });
    }

    private void showInfoWindow(GatherPoint point) {
        
    }

    long startScrollStamp;

    Handler handlerScroll = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == GET_BOUNDS) {
                LsLog.w(TAG, "handlerScroll 。。。");
                if (FragmentHome2.this.isVisible()) {
                    getMapBounds();
                }
            }
        }
    };

    private void beginScroll() {
        startScrollStamp = System.currentTimeMillis();
        handlerScroll.removeMessages(GET_BOUNDS);
        handlerScroll.sendEmptyMessageDelayed(GET_BOUNDS, 1000);
    }

    private void getMapBounds() {
        Polygon visibleArea = mMapView.getVisibleArea();
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


    private void showImageShpLayer(boolean isShow) {

    }

    private void setMapType(int mapType) {
        this.mapType = mapType;
        Basemap.Type type = mapTypeList.get(mapType - 1);
        mArcGISMap = new ArcGISMap(type, Constants.latitude,
                Constants.longitude, Constants.levelOfDetail);
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
//        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud8361042706,none,KGE60RFLTFCJTK118097");
        mapTypeList.add(Basemap.Type.OPEN_STREET_MAP);
        mapTypeList.add(Basemap.Type.STREETS_VECTOR);
        mapTypeList.add(Basemap.Type.IMAGERY_WITH_LABELS_VECTOR);
        mapTypeList.add(Basemap.Type.STREETS_WITH_RELIEF_VECTOR);
        mapTypeList.add(Basemap.Type.LIGHT_GRAY_CANVAS);

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

    LocationDisplay locationDisplay;

    private void initMylocaltion() {

        AndroidLocationDataSource dataSource = new AndroidLocationDataSource(getContext());

        locationDisplay = mMapView.getLocationDisplay();
        LocationDataSource locationDataSource = locationDisplay.getLocationDataSource();
        if (locationDataSource instanceof AndroidLocationDataSource) {
            LsLog.w(TAG, "locationDataSource instanceof AndroidLocationDataSource 。。。 ");
        }

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
        //4326   GCS_WGS_1984  和  102100    WGS_1984_web_mercator_auxiliary_sphere 。  EPSG:3857 -- WGS84 Web Mercator (Auxiliary Sphere)
        LocationDataSource.Location location = locationDisplay.getLocation();
        if (location != null && location.getPosition() != null)
            mMapView.setViewpointCenterAsync(location.getPosition());
    }

    @Override
    public void onResume() {
        super.onResume();
        // 恢复显示...
        mMapView.resume();
        delayRun(1000);
        // TODO: 查找边界，显示采集点信息
    }

    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // The X axis is horizontal and points to the right,
            // the Y axis is vertical and points up ,
            // the Z axis points towards the outside of the front face of the screen.
            // In this system, coordinates behind the screen have negative Z values.
//            double x = event.values[SensorManager.DATA_X];
//            if (Math.abs(x - lastX) > 1.0) {
//                mCurrentDirection = (int) x;
//            }
//            lastX = x;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(sensorEventListener);
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
        if (requestCode == Constants.GET_FILE_PATH) {
            List<String> list = data.getStringArrayListExtra("paths");
            for (String file : list) {
                Log.w(TAG, "list get file name: " + file);
                if (isPictureFile(file)) {
                    loadTiff(file);
                } else if (isSharpFile(file)) {
                    loadShp(file);
                } else {
                    loadOfflineMapLayer(file);
                }
            }
        }
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
                        FeatureLayer shapefileFeatureLayer = new FeatureLayer(shapefileFeatureTable);
                        // add the feature layer to the map
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

    private boolean isSharpFile(String file) {
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
            imageryRasterLayers.add(rasterLayer);
            refreshOperstionLayer();
        } else {
            ToastUtil.showTextToast(getContext(), filename + " 不是一个地图文件，请重新选择");
        }
    }


    GeoPackage geoPackage;

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

            geoPackageRasterLayer.setOpacity(0.6f);
            geoPackageRasterLayers.add(geoPackageRasterLayer);
        }
        refreshOperstionLayer();
    }

    private void delayRun(int i) {
        new Handler().postDelayed(() -> {
            goToMyLocation();
            getMapBounds();
        }, i);
    }

    private synchronized void showListPoint(List<GatherPoint> list, MapView mapView) {
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

    AdjustPosDialog2 dialog;

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
}
