package com.data.collection.listener;


public interface ISavePolygonListener {
    void onConfirm(String name, String comments);
    void onCancel();
}
