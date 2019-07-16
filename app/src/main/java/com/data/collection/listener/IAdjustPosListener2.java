package com.data.collection.listener;


import com.esri.arcgisruntime.geometry.Point;

public interface IAdjustPosListener2 {
    void onConfirm(Point fromPoint, Point toPoint);
    void onCancel();
}
