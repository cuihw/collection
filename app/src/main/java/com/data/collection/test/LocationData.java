package com.data.collection.test;

import android.location.Location;

import com.data.collection.module.DataPoint;

import java.util.ArrayList;
import java.util.List;

public class LocationData {

    public static List<DataPoint> getTestData(){
        List<DataPoint> list = new ArrayList<>();
        DataPoint point = new DataPoint();
        point.setLatitude(34.778768);
        point.setLongitude(113.600406);
        point.setName("河南省郑州市中原区");
        list.add(point);
        point = new DataPoint(); //113.717796,34.806848
        point.setLatitude(34.806848);
        point.setLongitude(113.717796);
        point.setName("河南省郑州市金水区龙湖中环西路");
        list.add(point);
        return list;
    }
}
