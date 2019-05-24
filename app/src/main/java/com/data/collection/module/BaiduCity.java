package com.data.collection.module;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;

import java.util.ArrayList;

public class BaiduCity extends MKOLSearchRecord {
    public static final int NORMAAL = 0;
    public static final int DOWNLOADING = 1;
    public static final int DOWNLOADED = 2;
    public static final int WAITING_DOWNLOADED = 3;

    public MKOLSearchRecord parentRecord;

    public int state = 0;

    public BaiduCity(MKOLSearchRecord records) {
        cityID = records.cityID;
        size = records.size;
        dataSize = records.dataSize;
        cityName = records.cityName;
        cityType = records.cityType;
        childCities = records.childCities;
    }


}
