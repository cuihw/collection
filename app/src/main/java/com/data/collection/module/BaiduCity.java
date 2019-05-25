package com.data.collection.module;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.google.gson.Gson;

import java.util.ArrayList;

public class BaiduCity extends MKOLSearchRecord {
    public static final int NORMAL = 0;
    public static final int DOWNLOADING = 1;
    public static final int DOWNLOADED = 2;
    public static final int WAITING_DOWNLOADED = 3;

    public BaiduCity parentRecord;

    public boolean update = false;

    public int state = 0;

    public BaiduCity(MKOLSearchRecord records) {
        cityID = records.cityID;
        size = records.size;
        dataSize = records.dataSize;
        cityName = records.cityName;
        cityType = records.cityType;
        childCities = records.childCities;
    }

    public String toJson(){
        return new Gson().toJson(this);
    }
}
