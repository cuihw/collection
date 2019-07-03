package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.data.DataUtils;
import com.data.collection.data.UserTrace;
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.listener.IGatherDataListener;
import com.data.collection.module.CollectType;
import com.data.collection.util.BitmapUtil;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.data.collection.view.MyOsmMarker;
import com.data.collection.view.TitleView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
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

public class OfflineMapActivity extends BaseActivity implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "OfflineMapActivity";

    @BindView(R.id.title_view)
    TitleView titleView;

    //@BindView(R.id.arcgis_map)
    // com.esri.arcgisruntime.mapping.view.MapView
     //MapView        mArcgisMapView;

    @BindView(R.id.osmdroidMapView)
    MapView osmdroidMapView;

    @BindView(R.id.recode_trace)
    TextView recodeTrace;
    @BindView(R.id.map_my_position)
    TextView myPosition;

    @BindView(R.id.trace_process)
    TextView traceProcess;

    @BindView(R.id.add_point)
    ImageView addPoint;

    @BindView(R.id.show_map_arcgis)
    TextView showArcgisMap;

    final static float[] trans = {
            1.0f, 0, 0, 0, 0, //red
            0, 1.0f, 0, 0, 0,//green
            0, 0, 1.0f, 0, 0,//blue
            0, 0, 0, 0.8f, 0 //alpha
    };
    List<GatherPoint> showInMap = new ArrayList<>();
    List<GatherPoint> newItemMarker = new ArrayList<>();

    public static void start(Context context) {
        Intent intent = new Intent(context, OfflineMapActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arcgis_map);
        initView();
        initListener();
    }

    List<GatherPoint> toBeRemove = new ArrayList<>();
    private Map<String, MyOsmMarker> markerMap = new HashMap<>();

    private void initListener() {
        recodeTrace.setOnClickListener(v -> clickTraceButton());
        showArcgisMap.setOnClickListener(v -> finish());
        titleView.getLefticon().setOnClickListener(v -> finish());
        titleView.getRighticon().setOnClickListener(v -> CollectionListActivity.start(this));
        addPoint.setOnClickListener(v -> {
            AddCollectionActivity.start(this, null);
        });

        osmdroidMapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.w(TAG, "onScroll");
                BoundingBox boundingBox = event.getSource().getBoundingBox();
                DataUtils.asyncPointsByBounds(boundingBox, new IGatherDataListener() {
                    @Override
                    public void onListData(List<GatherPoint> list) {
                        showListPoint(list);
                    }
                });
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.w(TAG, "onZoom");
                BoundingBox boundingBox = event.getSource().getBoundingBox();
                DataUtils.asyncPointsByBounds(boundingBox, new IGatherDataListener() {
                    @Override
                    public void onListData(List<GatherPoint> list) {
                        showListPoint(list);
                    }
                });
                return true;
            }
        });
        myPosition.setOnClickListener(v -> {
            Location location = LocationController.getInstance().getLocation();
            osmdroidMapView.getController().animateTo(new GeoPoint(location.getLatitude(), location.getLongitude()));
        });
    }

    private synchronized void showListPoint(List<GatherPoint> list) {
        // if (BuildConfig.DEBUG) return;
        newItemMarker.clear();
        for (GatherPoint gp: list) {
            if (!showInMap.contains(gp)) {
                newItemMarker.add(gp);
                LsLog.w(TAG, "new gp to show: " + gp.getName());
            } else {
                LsLog.w(TAG, "already showing: " + gp.getName());
            }
        }
        LsLog.w(TAG, "newItemMarker size : " + newItemMarker.size());
        if (newItemMarker.size() == 0) return;

        // 加上新元素。
        showInMap.addAll(newItemMarker);

        Log.w(TAG, "showListPoint");
        for (GatherPoint gp : newItemMarker) {
            MyOsmMarker marker = getMarker(gp);
            osmdroidMapView.getOverlays().add(marker);
        }
        osmdroidMapView.invalidate();
    }

    private MyOsmMarker getMarker(GatherPoint gp) {
        Log.w(TAG, "getMarker");
        CollectType typeIconUrl = DataUtils.getTypeIconUrl(gp);
        double latitude = Double.parseDouble(gp.getLatitude());
        double nextlng = Double.parseDouble(gp.getLongitude());

        MyOsmMarker existMarker = markerMap.get(gp.getLatitude() + nextlng);

        while (existMarker != null) {
            if (existMarker.getGatherPoint().getName().equals(gp.getName())) return existMarker;
            nextlng = nextlng + Constants.DIFF2;
            existMarker = markerMap.get(gp.getLatitude() + nextlng);
        }

        MyOsmMarker marker = new MyOsmMarker(osmdroidMapView);
        marker.setGatherPoint(gp);
        marker.setPosition(new GeoPoint(latitude, nextlng));

        Log.w(TAG, "getMarker = " + latitude + ", " + nextlng);

        View view = View.inflate(this, R.layout.view_point_marker, null);
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
        marker.setAnchor(Marker.ANCHOR_RIGHT, Marker.ANCHOR_BOTTOM);
        marker.setIcon(drawable);

//        int resId = BitmapUtil.getResid(typeIconUrl.getIcon());
//        marker.setIcon(getResources().getDrawable(resId));

        setMarkerListener(marker, gp);
        markerMap.put(gp.getLatitude() + nextlng, marker);
        return marker;
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
            instance.start();
            recodeTrace.setText("停止\n记录");
            setFlickerAnimation(traceProcess);
            traceProcess.setVisibility(View.VISIBLE);
            if (!UserTrace.getInstance().isInTrace()) {
                UserTrace.getInstance().start();
            }
        }
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

    private void setMarkerListener(Marker marker, final GatherPoint gp) {
        //layout/osmdroid_info_window.xml
        Log.w(TAG, "setMarkerListener");
        MarkerInfoWindow makerInfoWindow = new MarkerInfoWindow(R.layout.osmdroid_info_window, osmdroidMapView);
        View view = makerInfoWindow.getView();
        view.setTag(gp);

        marker.setInfoWindow(makerInfoWindow);
        marker.setTitle(gp.getName());
        CollectType collectType = DataUtils.getTypeIconUrl(gp);
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

        marker.setOnMarkerClickListener((Marker markerV, MapView mapView) -> {
            Log.w(TAG, "setOnMarkerClickListener marker");
            if (markerV.isInfoWindowShown()) {
                markerV.closeInfoWindow();
            } else {
                osmdroidMapView.getController().animateTo(markerV.getPosition());
                markerV.showInfoWindow();
            }
            osmdroidMapView.postInvalidate();
            return true;
        });
        Button checkBtn = view.findViewById(R.id.check_btn);
        checkBtn.setOnClickListener(v -> {
            Log.w(TAG, "checkBtn.setOnClickListener button.");
            marker.closeInfoWindow();
            AddCollectionActivity.start(this, gp);
        });
    }

    private void initView() {
        if (osmdroidMapView.getOverlays().size() <= 0) {
            osmdroidMapView.setTileSource(TileSourceFactory.OpenTopo);
            mapViewOtherData(osmdroidMapView);
            osmdroidMapView.setDrawingCacheEnabled(true);
            osmdroidMapView.setMaxZoomLevel(19d);
            osmdroidMapView.setMinZoomLevel(0d);

            osmdroidMapView.getController().setZoom(16);
            // 113.6019350, 34.7967643
            osmdroidMapView.getController().setCenter(new GeoPoint(34.7967643, 113.6019350));
            osmdroidMapView.setUseDataConnection(true);
            osmdroidMapView.setMultiTouchControls(true);// 触控放大缩小
            osmdroidMapView.getOverlayManager().getTilesOverlay().setEnabled(true);
            showMylocaltion();
        }
    }
    public final static ColorFilter transparency = new ColorMatrixColorFilter(trans);

    public void mapViewOtherData(MapView mapView) {
        String strFilepath = Environment.getExternalStorageDirectory().getPath() + "/zwsdk/zhengzhou.mbtiles";

        File exitFile = new File(strFilepath);
        String fileName = "zhengzhou.mbtiles";

        if (exitFile.exists()) {
            fileName = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (fileName.length() == 0)
                return;
            if (ArchiveFileFactory.isFileExtensionRegistered(fileName)) {
                try {
                    IRegisterReceiver registerReceiver = new SimpleRegisterReceiver(this);
                    OfflineTileProvider tileProvider = new OfflineTileProvider(registerReceiver,new File[]{exitFile});
                    //mapView.setTileProvider(tileProvider);
                    TilesOverlay overlay = new TilesOverlay(tileProvider, this);
                    // overlay.setTransparency(0.5f);
                    overlay.setColorFilter(transparency);
                    // 加载在最底层的图像
                    mapView.getOverlayManager().add(0, overlay);
                    mapView.invalidate();
                    return;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Toast.makeText(this, "did not have any files I can open", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, " dir not found!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showMylocaltion() {

        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this),
                osmdroidMapView);
        mLocationOverlay.enableMyLocation();
        osmdroidMapView.getOverlays().add(1, mLocationOverlay);
        osmdroidMapView.getController().animateTo(mLocationOverlay.getMyLocation());
    }

    @Override
    protected void onPause() {
        //if (mArcgisMapView != null) mArcgisMapView.pause();
        if (osmdroidMapView != null) osmdroidMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //if (mArcgisMapView != null) {
            //mArcgisMapView.dispose();
            //mArcgisMapView = null;
       // }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if (mArcgisMapView != null) mArcgisMapView.resume();
        osmdroidMapView.onResume();
        delayRun(1000);
    }

    private void delayRun(int i) {
        new Handler().postDelayed(() -> {
            DataUtils.asyncPointsByBounds(osmdroidMapView.getBoundingBox(), new IGatherDataListener() {
                @Override
                public void onListData(List<GatherPoint> list) {
                    showListPoint(list);
                }
            });
        }, i);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
    }

}
