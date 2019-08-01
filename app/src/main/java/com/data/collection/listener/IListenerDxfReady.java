package com.data.collection.listener;

import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;

import java.util.List;

public interface IListenerDxfReady {
    public void onReady(List<GraphicsOverlay> overlays);
}
