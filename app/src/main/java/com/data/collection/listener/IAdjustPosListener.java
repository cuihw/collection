package com.data.collection.listener;

import org.osmdroid.util.GeoPoint;
@Deprecated
public interface IAdjustPosListener {
    void onConfirm(GeoPoint fromPoint, GeoPoint toPoint);
    void onCancel();
}
