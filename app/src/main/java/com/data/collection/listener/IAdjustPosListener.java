package com.data.collection.listener;

import android.graphics.PointF;

import com.data.collection.data.greendao.TraceLocation;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public interface IAdjustPosListener {
    void onConfirm(GeoPoint point, PointF pPixel);
    void onCancel();
}
