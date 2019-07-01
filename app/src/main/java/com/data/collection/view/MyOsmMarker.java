package com.data.collection.view;

import com.data.collection.data.greendao.GatherPoint;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MyOsmMarker extends Marker {

    GatherPoint gatherPoint;

    public MyOsmMarker(MapView mapView) {
        super(mapView);
    }

    public GatherPoint getGatherPoint() {
        return gatherPoint;
    }

    public void setGatherPoint(GatherPoint gatherPoint) {
        this.gatherPoint = gatherPoint;
    }
}
