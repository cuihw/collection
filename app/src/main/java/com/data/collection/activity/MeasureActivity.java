package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.data.collection.R;
import com.data.collection.data.DataUtils;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

import cn.sddman.arcgistool.common.Variable;
import cn.sddman.arcgistool.view.ArcGisZoomView;
import cn.sddman.arcgistool.view.MapRotateView;
import cn.sddman.arcgistool.view.MeasureToolView;

public class MeasureActivity extends AppCompatActivity {
    private static final String TAG = "MeasureActivity";
    private MapView mMapView;
    private String url="http://cache1.arcgisonline.cn/arcgis/rest/services/ChinaOnlineCommunity/MapServer";

    public static void start(Context context) {
        Intent intent = new Intent(context, MeasureActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        mMapView = findViewById(R.id.mapViewLayout);
        ArcGISTiledLayer tiledLayerBaseMap = new ArcGISTiledLayer(url);
        Basemap basemap = new Basemap(tiledLayerBaseMap);
        ArcGISMap map = new ArcGISMap(basemap);

        Location location = LocationController.getInstance().getLocation();

        Viewpoint vp = new Viewpoint(location.getLatitude()+ DataUtils.GOOGLE_ADJUST.adjustLat,
                location.getLongitude()+DataUtils.GOOGLE_ADJUST.adjustlng, 20000);
        // new Viewpoint();
        map.setInitialViewpoint(vp);
        mMapView.setMap(map);
        Rect clipBounds = mMapView.getClipBounds();

        MeasureToolView measureToolView=(MeasureToolView)findViewById(R.id.measure_tool);
        measureToolView.init(mMapView);
        measureToolView.setButtonWidth(55);
        measureToolView.setButtonHeight(35);
        // measureToolView.setMeasureBackground(R.color.colorAccent);
        measureToolView.setSohwText(true);
        measureToolView.setFontSize(12);
        measureToolView.setFontColor(R.color.color444);
        measureToolView.setMeasurePrevStr("撤销");
        measureToolView.setMeasureNextStr("恢复");
        measureToolView.setMeasureLengthStr("测距");
        measureToolView.setMeasureAreaStr("测面积");
        measureToolView.setMeasureClearStr("清除");
        measureToolView.setMeasureEndStr("完成");
        measureToolView.setMeasurePrevImage(R.drawable.sddman_measure_prev);
        measureToolView.setMeasureNextImage(R.drawable.sddman_measure_next);
        measureToolView.setMeasureLengthImage(R.drawable.sddman_measure_length);
        measureToolView.setMeasureAreaImage(R.drawable.sddman_measure_area);
        measureToolView.setMeasureClearImage(R.drawable.sddman_measure_clear);
        measureToolView.setMeasureEndImage(R.drawable.sddman_measure_end);
        measureToolView.setSpatialReference(SpatialReference.create(3857));
        measureToolView.setLengthType(Variable.Measure.KM);
        measureToolView.setAreaType(Variable.Measure.KM2);

        mMapView.getMap().addBasemapChangedListener(new ArcGISMap.BasemapChangedListener(){
            @Override
            public void basemapChanged(ArcGISMap.BasemapChangedEvent basemapChangedEvent) {
                LsLog.w(TAG, "basemapChanged  basemapChangedEvent");
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }
}
