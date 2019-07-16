package com.data.collection.view;

import com.data.collection.data.greendao.GatherPoint;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;

public class MyPicMarkerSymbol {

    double latitude; // 符号实际显示的坐标点位置
    double longitude;

    Point geoPoint;   // 符号实际显示的坐标点位置
    PictureMarkerSymbol myPicture;
    GatherPoint point;

    public Point getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(Point geoPoint) {
        this.geoPoint = geoPoint;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public MyPicMarkerSymbol(GatherPoint point) {
        this.point = point;
        latitude = Double.parseDouble(point.getLatitude());
        longitude = Double.parseDouble(point.getLongitude());
    }



    public PictureMarkerSymbol getMyPicture() {
        return myPicture;
    }

    public void setMyPicture(PictureMarkerSymbol myPicture) {
        this.myPicture = myPicture;
    }

    public GatherPoint getPoint() {
        return point;
    }

    public void setPoint(GatherPoint point) {
        this.point = point;
    }

    @Override
    public boolean equals( Object obj) {
        if (obj instanceof MyPicMarkerSymbol) {
            MyPicMarkerSymbol symbol = (MyPicMarkerSymbol)obj;
            GatherPoint point1 = symbol.getPoint();
            return this.point.equals(point1);
        }
        return false;
    }

    public void setGeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        geoPoint = new Point(longitude, latitude, SpatialReferences.getWgs84());
    }

    public String genericKey(double latitude, double nextlng) {
        return latitude + "|" + nextlng;
    }
}
