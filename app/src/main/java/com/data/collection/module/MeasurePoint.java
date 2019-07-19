package com.data.collection.module;

import com.data.collection.data.greendao.GatherPoint;
import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.List;

public class MeasurePoint extends GatherPoint{
    boolean isSelected = false;

    public MeasurePoint (GatherPoint point){
        super(point.getOffline_id(),point.getId(),point.getName(),
                point.getType_id(), point.getAttrs(), point.getLatitude(),
                point.getLongitude(), point.getHeight(),
                point.getCollected_at(), point.getUpdated_at(), point.getReport(),
                point.getDesc(),point.getPicPath1(),point.getPicPath2(),point.getPicPath3(),
                point.getImgs(),point.getIsUploaded());

    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
