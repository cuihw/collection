package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.data.collection.R;
import com.data.collection.data.UserTrace;
import com.data.collection.util.BitmapUtil;
import com.data.collection.view.TitleView;

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.Set;

import butterknife.BindView;

public class OfflineMapActivity extends BaseActivity implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "OfflineMapActivity";

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.arcgis_map)
    com.esri.arcgisruntime.mapping.view.MapView mArcgisMapView;

    @BindView(R.id.osmdroidMapView)
    MapView osmdroidMapView;

    @BindView(R.id.recode_trace)
    TextView recodeTrace;

    @BindView(R.id.trace_process)
    TextView traceProcess;

    @BindView(R.id.add_point)
    ImageView addPoint;

    @BindView(R.id.show_map_arcgis)
    TextView showArcgisMap;


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

    private void initListener() {
        recodeTrace.setOnClickListener(v -> clickTraceButton());
        showArcgisMap.setOnClickListener(v -> finish());
        titleView.getLefticon().setOnClickListener(v -> finish());
        titleView.getRighticon().setOnClickListener(v -> CollectionListActivity.start(this));
        addPoint.setOnClickListener(v -> {
            AddCollectionActivity.start(this, null);
        });
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



    private void initView() {
        if (osmdroidMapView.getOverlays().size()<=0) {
            osmdroidMapView.setTileSource(TileSourceFactory.OpenTopo);
            mapViewOtherData(osmdroidMapView);
            osmdroidMapView.setDrawingCacheEnabled(true);
            osmdroidMapView.setMaxZoomLevel(19d);
            osmdroidMapView.setMinZoomLevel(0d);
            osmdroidMapView.getController().setZoom(12);
            // 113.6019350, 34.7967643
            osmdroidMapView.getController().setCenter(new GeoPoint(34.7967643, 113.6019350));
            osmdroidMapView.setUseDataConnection(true);
            osmdroidMapView.setMultiTouchControls(true);// 触控放大缩小
            osmdroidMapView.getOverlayManager().getTilesOverlay().setEnabled(true);
        }
        showMylocaltion();
    }

    public void mapViewOtherData(MapView mapView){
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
                    OfflineTileProvider tileProvider = new OfflineTileProvider(
                            registerReceiver,
                            new File[] { exitFile });
                    //mapView.setTileProvider(tileProvider);
                    TilesOverlay overlay = new TilesOverlay(tileProvider, this);
                    // overlay.setTransparency(0.5f);

                    overlay.setLoadingBackgroundColor(getResources().getColor(R.color.transparent20));
                    mapView.getOverlayManager().add(overlay);

                    String source = "";
                    IArchiveFile[] archives = tileProvider.getArchives();
                    if (archives.length > 0) {
                        Set<String> tileSources = archives[0].getTileSources();
                        if (!tileSources.isEmpty()) {
                            source = tileSources.iterator().next();

                            //mapView.setTileSource(FileBasedTileSource.getSource(source));
                        }
                    }

                    Toast.makeText(this,"Using " + exitFile.getAbsolutePath() + " "+ source, Toast.LENGTH_LONG).show();
                    mapView.invalidate();
                    return;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Toast.makeText(this,  " did not have any files I can open! Try using MOBAC", Toast.LENGTH_LONG).show();
            } else{
                Toast.makeText(this, " dir not found!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showMylocaltion() {

        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this),
                osmdroidMapView);
        Bitmap bitmap = BitmapUtil.scaleBitmap(BitmapFactory.decodeResource(getResources(), org.osmdroid.library.R.drawable.person), 0.5f) ;
        Bitmap directionArrow = BitmapUtil.scaleBitmap(BitmapFactory.decodeResource(getResources(), org.osmdroid.library.R.drawable.direction_arrow), 0.5f) ;

        mLocationOverlay.setDirectionArrow(bitmap,directionArrow);
        osmdroidMapView.getOverlays().add(mLocationOverlay);
        mLocationOverlay.enableMyLocation();
    }

    @Override
    protected void onPause() {
        if (mArcgisMapView != null) mArcgisMapView.pause();
        if (osmdroidMapView != null) osmdroidMapView.onPause();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mArcgisMapView != null) {
            mArcgisMapView.dispose();
            mArcgisMapView = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mArcgisMapView != null) mArcgisMapView.resume();
        osmdroidMapView.onResume();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
    }

}
