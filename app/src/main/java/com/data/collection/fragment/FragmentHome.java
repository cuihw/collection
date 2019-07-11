package com.data.collection.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.Tiles.GoogleTileSource;
import com.data.collection.activity.AddCollectionActivity;
import com.data.collection.activity.CollectionListActivity;
import com.data.collection.data.CacheData;
import com.data.collection.data.MapDataUtils;
import com.data.collection.data.UserTrace;
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.data.tiff.extended.GeoTiffImage;
import com.data.collection.dialog.AdjustPosDialog;
import com.data.collection.listener.IAdjustPosListener;
import com.data.collection.listener.IGatherDataListener;
import com.data.collection.listener.ITiffListener;
import com.data.collection.module.CollectType;
import com.data.collection.util.BitmapUtil;
import com.data.collection.util.FileUtils;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.MyGroundOverLay;
import com.data.collection.view.MyOsmMarker;
import com.data.collection.view.TitleView;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
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
 * Use the {@link FragmentHome#} factory method to
 * create an instance of this fragment.
 */
public class FragmentHome extends FragmentBase {
    private static final String TAG = "FragmentHome";

    int mapType = MapDataUtils.OPEN_STREET_SOURCE;

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
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;

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

    List<GatherPoint> showColletMarkerList= new ArrayList<>(); // 当前显示的采集点
    List<TilesOverlay> offlineLays = new ArrayList<>();

    private Map<String, MyOsmMarker> markerMap = new HashMap<>();

    Map<String, MyGroundOverLay> groundOverlayMap = new HashMap<>();


    OnlineTileSourceBase openTopoSource = TileSourceFactory.OpenTopo; // Open Street 拓扑图
    OnlineTileSourceBase googleHybridTilesource = GoogleTileSource.GoogleHybrid; // 谷歌卫星混合
    OnlineTileSourceBase googleTilesource = GoogleTileSource.GoogleSat; // 谷歌卫星
    OnlineTileSourceBase openstreetmap = GoogleTileSource.openstreetmap; // Open Street 交通图
    OnlineTileSourceBase autoNaviVector = GoogleTileSource.AutoNaviVector; // 高德地图
    OnlineTileSourceBase tiandituTilesource = GoogleTileSource.tianDiTuCiaTileSource; // 天地图
    String[] items = new String[]{"Open Street 拓扑图", "谷歌卫星混合", "谷歌卫星",
            "Open Street 交通图", "高德地图", "天地图"};
    //List<String> items = Arrays.asList(new String[]{"Open Street 拓扑图", "谷歌卫星混合", "谷歌卫星", "Open Street 交通图", "高德地图", "天地图"});


    MyLocationNewOverlay mLocationOverlay;

    final static float[] trans = {
            1.0f, 0, 0, 0, 0, //red
            0, 1.0f, 0, 0, 0,//green
            0, 0, 1.0f, 0, 0,//blue
            0, 0, 0, 0.8f, 0 //alpha
    };
    public final static ColorFilter transparency = new ColorMatrixColorFilter(trans);

    private boolean isShowGroundLay = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");

        view = inflater.inflate(R.layout.fragment_home, container, false);
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

    private void setMapViewListener(){
        mMapView.getOverlayManager().getTilesOverlay();
    }

    public void showGroundOverlay(boolean isShow){
        this.isShowGroundLay = isShow;
        List<MyGroundOverLay> list = new ArrayList<>(groundOverlayMap.values());
        if (isShowGroundLay) { // show
            if (list.size() == 0) {
                mMapView.invalidate();
                return;
            }
            if (hasOfflineLay) {
                mMapView.getOverlayManager().addAll(1,list);
            } else {
                mMapView.getOverlayManager().addAll(0,list);
            }
            readTiff.setText("隐藏\n图层");
        } else { // remove the tif & shp files lay
            if (list.size() == 0) {
                mMapView.invalidate();
                return;
            }
            mMapView.getOverlayManager().removeAll(list);
            readTiff.setText("加载\n图片");
        }
        mMapView.invalidate();
    }

    AdjustPosDialog dialog;

    private void showAdjustDialog(GeoPoint fromPoint, GeoPoint toPoint) {
        dialog = new AdjustPosDialog(getContext(), new IAdjustPosListener() {
            @Override
            public void onConfirm(GeoPoint fromPoint, GeoPoint toPoint) {
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

    private void initListener() {
        //openationHint
        calibrationCoordinate.setOnClickListener(v->{
            if (openationHint.getVisibility() == View.GONE) {
                // 在纠偏模式下
                openationHint.setVisibility(View.VISIBLE);
            } else {
                // 普通模式下
                openationHint.setVisibility(View.GONE);
            }
        });

        readTiff.setOnClickListener(v->{
            if (isShowGroundLay) {
                showGroundOverlay(false);
            } else {
                String fileDir = FileUtils.getFileDir();
                new LFilePicker().withActivity(getActivity())
                        .withRequestCode(Constants.GET_FILE_PATH)
                        .withStartPath(fileDir)
                        //.withFileFilter(new String[]{".txt", ".png", ".docx"})
                        .withFileFilter(new String[]{".tif"})
                        .withMutilyMode(false)
                        .withTitle("加载图层文件")
                        .start();
            }
        });

        mMapView.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver(){
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint formPoint) {
                Log.w(TAG, "singleTapConfirmedHelper GeoPoint = " + formPoint.toString());

                if (openationHint.getVisibility() == View.VISIBLE) { // in adjust mode.
                    Location location = LocationController.getInstance().getLocation();
                    showAdjustDialog(formPoint, new GeoPoint(location.getLatitude(), location.getLongitude()));
                    return true;
                }
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                Log.w(TAG, "longPressHelper GeoPoint = " + p.getLatitude() + ", " + p.getLongitude());
                return false;
            }
        }));

        mMapView.addMapListener(new MapListener() {

            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.w(TAG, "onScroll");
                BoundingBox boundingBox = event.getSource().getBoundingBox();
                MapDataUtils.asyncPointsByBounds(boundingBox, new IGatherDataListener() {
                    @Override
                    public void onListData(List<GatherPoint> list) {
                        showListPoint(list, mMapView);
                    }
                });
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.w(TAG, "onZoom");
                BoundingBox boundingBox = event.getSource().getBoundingBox();
                MapDataUtils.asyncPointsByBounds(boundingBox, new IGatherDataListener() {
                    @Override
                    public void onListData(List<GatherPoint> list) {
                        showListPoint(list, mMapView);
                    }
                });
                return true;
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
                AddCollectionActivity.start(getContext(), null);
            } else {
                ToastUtil.showTextToast(getContext(), "请先登录系统，再进行操作");
            }
        });

        recodeTrace.setOnClickListener(v -> clickTraceButton());

        mapTypeTv.setOnClickListener(v -> {
            showChioceDialog();
        });

        loadLocalMap.setOnClickListener(v -> {
            if (hasOfflineLay ){
                removeOffLineLay();
                loadLocalMap.setText("离线\n底图");
            } else {
                String fileDir = FileUtils.getFileDir();
                new LFilePicker().withActivity(getActivity())
                        .withRequestCode(Constants.GET_FILE_PATH)
                        .withStartPath(fileDir)
                        //.withFileFilter(new String[]{".txt", ".png", ".docx"})
                        .withFileFilter(new String[]{".mbtiles"})
                        .withMutilyMode(false)
                        .withTitle("打开离线文件")
                        .start();
            }
        });

        myPosition.setOnClickListener(v -> {
            goToMyLocation();
        });
    }

    private void loadBitmap(GeoTiffImage geoTiffImage) {
        MyGroundOverLay groundOverlay2 = new MyGroundOverLay();

        BoundingBox boundingBox = geoTiffImage.getBoundingBox();

        GeoPoint UL = new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonWest());
        GeoPoint RD = new GeoPoint(boundingBox.getLatSouth(), boundingBox.getLonEast());
        groundOverlay2.set84Position(UL, RD);
        UL = MapDataUtils.adjustPoint(UL, mapType);
        RD = MapDataUtils.adjustPoint(RD, mapType);
        groundOverlay2.setPosition(UL, RD);
        groundOverlay2.setImage(geoTiffImage.getImage());
        groundOverlayMap.put(geoTiffImage.getFile().getAbsolutePath(),groundOverlay2);
        showGroundOverlay(true);
    }

    /*
        OnlineTileSourceBase openTopoSource = TileSourceFactory.OpenTopo; //Open Street 拓扑图
        OnlineTileSourceBase googleHybridTilesource = GoogleTileSource.GoogleHybrid; // 谷歌卫星混合
        OnlineTileSourceBase googleTilesource = GoogleTileSource.GoogleSat; // 谷歌卫星
        OnlineTileSourceBase openstreetmap = GoogleTileSource.openstreetmap; // Open Street 交通图
        OnlineTileSourceBase autoNaviVector = GoogleTileSource.AutoNaviVector; // 高德地图
        OnlineTileSourceBase tiandituTilesource = GoogleTileSource.tianDiTuCiaTileSource; //天地图
        public static final int OPEN_TOPO_SOURCE = 1;
        public static final int GOOGLE_MAP_SOURCE = 2;
        public static final int GOOGLE_TILE_SOURCE = 3;
        public static final int OPEN_STREET_SOURCE  = 4;
        public static final int GAODE_SOURCE  = 5;
        public static final int TIANDITU_SOURCE  = 6;
    */
    private void setMapType(int mapType) {
        this.mapType = mapType;
        switch (mapType) {
            case MapDataUtils.OPEN_TOPO_SOURCE:
                mMapView.setTileSource(openTopoSource);
                break;
            case MapDataUtils.GOOGLE_MAP_SOURCE:
                mMapView.setTileSource(googleHybridTilesource);
                break;
            case MapDataUtils.GOOGLE_TILE_SOURCE:
                mMapView.setTileSource(googleTilesource);
                break;
            case MapDataUtils.OPEN_STREET_SOURCE:
                mMapView.setTileSource(openstreetmap);
                break;
            case MapDataUtils.GAODE_SOURCE:
                mMapView.setTileSource(autoNaviVector);
                break;
            case MapDataUtils.TIANDITU_SOURCE:
                mMapView.setTileSource(tiandituTilesource);
                break;
        }
        refresh(MapDataUtils.isNeedAdjust(mapType));
    }

    private void removeOffLineLay() {
        hasOfflineLay = false;
        TilesOverlay tilesOverlay = mMapView.getOverlayManager().getTilesOverlay();
        mMapView.getOverlayManager().removeAll(offlineLays);
        offlineLays.clear();
        mMapView.invalidate();
    }

    private void initMap() {
        Configuration.getInstance().setAnimationSpeedDefault(500);

        mMapView.setDrawingCacheEnabled(true);
        mMapView.setMaxZoomLevel(19d);
        mMapView.setMinZoomLevel(0d);
        // mMapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mMapView.getController().setZoom(14);
        // 113.6019350, 34.7967643
        mMapView.getController().setCenter(new GeoPoint(34.7967643, 113.6019350));
        mMapView.setUseDataConnection(true);
        mMapView.setMultiTouchControls(true);// 触控放大缩小
        mMapView.getOverlayManager().getTilesOverlay().setEnabled(true);
        setMapType(mapType); // 增加了定位
    }

    private void initMylocaltion() {
        if (mLocationOverlay != null) {
            mMapView.getOverlays().remove(mLocationOverlay);
        }

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()){
            @Override
            public void onLocationChanged(Location location) {
                if(MapDataUtils.isNeedAdjust(mapType)) {
                    location.setLatitude(MapDataUtils.adjustLatitude(location.getLatitude(), mapType));
                    location.setLongitude(MapDataUtils.adjustLongitude(location.getLongitude(), mapType));
                }
                super.onLocationChanged(location);
            }
        }, mMapView);
        mLocationOverlay.enableMyLocation();
        mMapView.getOverlays().add(mLocationOverlay);
        goToMyLocation();
    }

    private void goToMyLocation() {
        // mLocationOverlay.enableFollowLocation();
        mMapView.getController().animateTo(mLocationOverlay.getMyLocation());
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(sensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
        if (mMapView != null) {
            mMapView.onResume();
        }
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
        if (mMapView!=null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.GET_FILE_PATH) {
            // get off line map files.
            // If it is a file selection mode, you need to get the path
            // collection of all the files selected
            // List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);
            // Constant.RESULT_INFO == "paths"
            List<String> list = data.getStringArrayListExtra("paths");
            for (String file: list){
                Log.w(TAG, "list get file name: " + file);
                if (isPictureFile(file)) {
                    readTiff(file);
                } else {
                    addTheMapLayer(file);
                }
            }
        }
    }

    private boolean isPictureFile(String file) {
        return file.endsWith(".tif");
    }

    private void readTiff(String path) {
        MyGroundOverLay groundOverlay = groundOverlayMap.get(path);
        if (groundOverlay != null) {
            showGroundOverlay(true);
            return;
        }

        hud.setLabel("加载中...");
        if (!hud.isShowing()) {
            hud.show();
            MapDataUtils.loadTif(path, new ITiffListener(){
                @Override
                public void onFileReady(GeoTiffImage geoTiffImage) {
                    if (geoTiffImage == null) {
                        ToastUtil.showTextToast(getContext(), "加载的tif文件不含地理坐标信息");
                        return;
                    }
                    loadBitmap(geoTiffImage);
                    hud.dismiss();
                }
            });
        }
    }

    private void addTheMapLayer(String file) {

        File exitFile = new File(file);
        String fileName = file;

        if (exitFile.exists()) {
            fileName = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (fileName.length() == 0)
                return;
            if (ArchiveFileFactory.isFileExtensionRegistered(fileName)) {
                try {
                    IRegisterReceiver registerReceiver = new SimpleRegisterReceiver(getContext());
                    OfflineTileProvider tileProvider = new OfflineTileProvider(registerReceiver,new File[]{exitFile});
                    //mapView.setTileProvider(tileProvider);
                    TilesOverlay overlay = new TilesOverlay(tileProvider, getContext());
                    //overlay.setTransparency(0.5f);
                    overlay.setColorFilter(transparency);
                    overlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                    // 加载在最底层的图像
                    OverlayManager overlayManager = mMapView.getOverlayManager();
                    overlayManager.add(0,overlay);
                    setOfflineLay(overlay);
                    return;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                //Toast.makeText(getContext(), "did not have any files I can open", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(getContext(), " dir not found!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setOfflineLay(TilesOverlay overlay) {
        hasOfflineLay = true;
        offlineLays.add(overlay);
        loadLocalMap.setText("删除\n底图");
        mMapView.invalidate();
    }

    private void delayRun(int i) {
        new Handler().postDelayed(() -> {
            MapDataUtils.asyncPointsByBounds(mMapView.getBoundingBox(), new IGatherDataListener() {
                @Override
                public void onListData(List<GatherPoint> list) {
                    showListPoint(list, mMapView);
                }
            });
        }, i);
    }

    private synchronized void showListPoint(List<GatherPoint> list, MapView mapView) {
        List<GatherPoint> newItemMarker = new ArrayList<>();  // 地图滑动后，新增加的采集点
        List<GatherPoint> toBeRemove = new ArrayList<>(); // 地图滑动后，需要删除不显示的采集点
        for (GatherPoint gp: list) {
            if (!showColletMarkerList.contains(gp)) {
                newItemMarker.add(gp);
                LsLog.w(TAG, "new gp to show: " + gp.getName());
            }
        }
        if (newItemMarker.size() == 0) return;

        // 加上新元素。
        showColletMarkerList.addAll(newItemMarker);

        for (GatherPoint gp : newItemMarker) {
            MyOsmMarker marker = createMarker(gp, mapView);
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }

    private MyOsmMarker createMarker(GatherPoint gp, MapView mapView) {
        CollectType typeIconUrl = MapDataUtils.getTypeIconUrl(gp);
        double latitude = Double.parseDouble(gp.getLatitude());
        double nextlng = Double.parseDouble(gp.getLongitude());

        MyOsmMarker existMarker = markerMap.get(gp.getLatitude() + nextlng);

        while (existMarker != null) {
            if (existMarker.getGatherPoint().getName().equals(gp.getName())) return existMarker;
            nextlng = nextlng + Constants.DIFF2;
            existMarker = markerMap.get(gp.getLatitude() + nextlng);
        }

        MyOsmMarker marker = new MyOsmMarker(gp, mapView);

        GeoPoint geoPoint = new GeoPoint(latitude, nextlng);
        geoPoint = MapDataUtils.adjustPoint(geoPoint, mapType);
        marker.setPosition(geoPoint);

        View view = View.inflate(getContext(), R.layout.view_point_marker, null);
        TextView viewById = view.findViewById(R.id.name_tv);
        viewById.setText(gp.getName());
        if (typeIconUrl != null) {
            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(typeIconUrl.getIcon());
            ImageView imageView = view.findViewById(R.id.icon_iv);
            imageView.setImageBitmap(bitmap);
        }
        Bitmap bitmap = BitmapUtil.convertViewToBitmap(view);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        marker.setTitle(gp.getName());
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker.setIcon(drawable);
        setMarkerListener(marker, gp, mapView);
        markerMap.put(gp.getLatitude() + nextlng, marker);
        return marker;
    }

    private void setMarkerListener(Marker marker, final GatherPoint gp, MapView mapView) {
        //layout/osmdroid_info_window.xml
        MarkerInfoWindow makerInfoWindow = new MarkerInfoWindow(R.layout.osmdroid_info_window, mapView);
        View view = makerInfoWindow.getView();
        view.setTag(gp);

        marker.setInfoWindow(makerInfoWindow);
        marker.setTitle(gp.getName());
        CollectType collectType = MapDataUtils.getTypeIconUrl(gp);
        if (collectType != null) {
            //
            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(collectType.getIcon());
            Drawable drawable = new BitmapDrawable(getResources(), BitmapUtil.scaleBitmap(bitmap,2));
            marker.setImage(drawable);
            marker.setSnippet(collectType.getName());
        } else {
            marker.setSnippet("未知对象");
        }

        marker.setSubDescription(gp.getLatitude() + ", " + gp.getLongitude());

        marker.setOnMarkerClickListener((Marker markerV, MapView mapView1) -> {
            Log.w(TAG, "setOnMarkerClickListener marker");
            if (markerV.isInfoWindowShown()) {
                markerV.closeInfoWindow();
            } else {
                mapView1.getController().animateTo(markerV.getPosition());
                markerV.showInfoWindow();
            }
            mapView1.postInvalidate();
            return true;
        });
        Button checkBtn = view.findViewById(R.id.check_btn);
        checkBtn.setOnClickListener(v -> {
            Log.w(TAG, "checkBtn.setOnClickListener button.");
            marker.closeInfoWindow();
            AddCollectionActivity.start(getContext(), gp);
        });
    }
    public void showChioceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()) ;
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

    public void adjustMapView(GeoPoint fromPoint, GeoPoint toPoint){
        double  deltaLat = toPoint.getLatitude() - fromPoint.getLatitude();
        double  deltaLon = toPoint.getLongitude() - fromPoint.getLongitude();
//      Log.w(TAG, "dalteLat = " + deltaLat + ", dalteLon  = " + deltaLon);
        MapDataUtils.GOOGLE_ADJUST.adjustLat = deltaLat;
        MapDataUtils.GOOGLE_ADJUST.adjustlng = deltaLon;
        openationHint.setVisibility(View.GONE);
        refresh(true);
    }

    private void refresh(boolean needAdjust) {
        // marker
        List<GatherPoint> list = showColletMarkerList;
        showColletMarkerList = new ArrayList<>();
        List<MyOsmMarker> markers = new ArrayList<>(markerMap.values());
        mMapView.getOverlayManager().removeAll(markers);
        markerMap.clear();
        showListPoint(list, mMapView);

        // groundOverlay
        List<MyGroundOverLay> groundOverlays = new ArrayList<>(groundOverlayMap.values());
        for (MyGroundOverLay groundOverlay: groundOverlays) {
            GeoPoint rd = groundOverlay.getRD();
            GeoPoint ul = groundOverlay.getUL();
            GeoPoint unmodifiedRD = groundOverlay.getUnmodifiedRD();
            GeoPoint unmodifiedUL = groundOverlay.getUnmodifiedUL();

            if (needAdjust && rd.equals(unmodifiedRD)) {
                Log.w(TAG, "groundOverlay show adjust postion");
                rd = MapDataUtils.adjustPoint(rd, mapType);
                ul = MapDataUtils.adjustPoint(ul, mapType);
                groundOverlay.setPosition(ul, rd);
            } else {
                Log.w(TAG, "groundOverlay show unmodified postion");
                groundOverlay.setPosition(unmodifiedUL, unmodifiedRD);
            }
        }

        showGroundOverlay(isShowGroundLay);
        // myLocation.

        Log.w(TAG, "initMylocaltion");
        initMylocaltion();
    }
}
