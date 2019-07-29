package com.data.collection.util;

import com.baidu.mapapi.map.offline.OfflineMapUtil;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapJob;
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapParameters;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask;

import java.io.File;

public class OffLineMap {

    private static final String TAG = "OffLineMap";
    private GenerateOfflineMapJob mGenerateOfflineMapJob;
    private GenerateOfflineMapParameters mGenerateOfflineMapParameters;
    private OfflineMapTask mOfflineMapTask;
    public void  start(MapView mMapView) {
        LsLog.w(TAG, "OffLineMap start ..");

        // cancel previous job request
        if (mGenerateOfflineMapJob != null) {
            mGenerateOfflineMapJob.cancel();
        }
        mOfflineMapTask = new OfflineMapTask(mMapView.getMap());

        String tempDirectoryPath = FileUtils.getFileDir()  + "offlineMap";
        mGenerateOfflineMapJob = mOfflineMapTask.generateOfflineMap(mGenerateOfflineMapParameters, tempDirectoryPath);

    }
}
