package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.data.UserTrace;
import com.data.collection.util.FileUtils;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.popup.PopupDefinition;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.Raster;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import java.io.File;

import butterknife.BindView;

// 导航计算页面，这是一个中转页面
// 导航跳到这个页面的时候，给出经纬度坐标地点，（初始化导航，初始化文件夹，初始化语音文件）然后计算路径
// 路径计算完毕后，进入导航页面，开始导航。

public class ArcgisMapActivity extends BaseActivity implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "ArcgisMapActivity";

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.arcgis_map)
    MapView mMapView;

    @BindView(R.id.recode_trace)
    TextView recodeTrace;

    @BindView(R.id.trace_process)
    TextView traceProcess;


    @BindView(R.id.add_point)
    ImageView addPoint;

    @BindView(R.id.show_map_arcgis)
    TextView showArcgisMap;

    private GraphicsOverlay mGraphicsOverlay;

    public static void start(Context context) {
        Intent intent = new Intent(context, ArcgisMapActivity.class);
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
        recodeTrace.setOnClickListener(v-> clickTraceButton());
        showArcgisMap.setOnClickListener(v->finish());
        titleView.getLefticon().setOnClickListener(v->finish());
        titleView.getRighticon().setOnClickListener(v-> CollectionListActivity.start(this));
        addPoint.setOnClickListener(v->{AddCollectionActivity.start(this,null);});
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

    //实现图片闪烁效果
    private void setFlickerAnimation(View view) {
        final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);
        view.setAnimation(animation);
    }

    private void initView() {
        ArcGISMap map = new ArcGISMap();
        // add the map to a map view
        mMapView.setMap(map);

        String fileName = FileUtils.getFileDir() + "offline_map.tif";
//        fileName = FileUtils.getFileDir() + "ssss.tif";
//        String fileName = FileUtils.getFileDir() + "aaaa.tif";
        File file = new File(fileName);
        if (file.exists()) {
            Raster imageryRaster = new Raster(fileName);
            RasterLayer rasterLayer =  new RasterLayer(imageryRaster);

            mMapView.getMap().setBasemap(new Basemap(rasterLayer));

            createGraphicsOverlay();
            createPointGraphics();
        } else {
            ToastUtil.showTextToast(this, "本地文件不存在");
        }


    }

    private void createPointGraphics() {
        //34.7967643, 113.6019350,
        Point point = new Point(113.6019350, 34.7967643, SpatialReferences.getWgs84());
        SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.rgb(226, 119, 40), 20.0f);
        pointSymbol.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 2.0f));
        Graphic pointGraphic = new Graphic(point, pointSymbol);
        mGraphicsOverlay.getGraphics().add(pointGraphic);

//        PictureMarkerSymbol pointMarker ;// = getPictureMarkerSymbolFromUrl();
//        pointMarker= getPictureMarkerSymbolFromUrl(R.mipmap.launch_icon);
//
//        point = new Point(113.7019350, 34.7967643, SpatialReferences.getWgs84());
//        pointGraphic = new Graphic(point, pointMarker);
//        mGraphicsOverlay.getGraphics().add(pointGraphic);

//        mGraphicsOverlay.getSelectedGraphics();
//        mGraphicsOverlay.setPopupDefinition(new PopupDefinition());
    }

    private void createGraphicsOverlay() {
        if (mGraphicsOverlay == null) {
            mGraphicsOverlay = new GraphicsOverlay();
            mMapView.getGraphicsOverlays().add(mGraphicsOverlay);
        }
    }

    private PictureMarkerSymbol getPictureMarkerSymbolFromUrl(int id ) {
        Bitmap bitmap = BitmapFactory.decodeResource (getResources(), id);
        BitmapDrawable draw = new BitmapDrawable(bitmap);
        return new PictureMarkerSymbol(draw);
    }

    @Override
    protected void onPause() {
        if (mMapView != null)  mMapView.pause();
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        if (mMapView != null)  mMapView.dispose();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMapView != null)  mMapView.resume();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
    }

}
