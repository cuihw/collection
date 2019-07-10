package com.data.collection.listener;

import com.data.collection.data.tiff.extended.GeoTiffImage;

public interface ITiffListener {
    void onFileReady(GeoTiffImage geoTiffImage);
}
