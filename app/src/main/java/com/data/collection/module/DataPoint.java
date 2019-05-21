package com.data.collection.module;

import android.location.Location;

import com.data.collection.util.DateUtils;

public class DataPoint extends Bean {

    private String name;
    private double latitude = 0.0;  //  纬度
    private double longitude = 0.0;
    private double altitude = 0.0f;
    private String dateTime = DateUtils.getNow(DateUtils.fmtYYYYMMDDhhmmss); // "yyyy-MM-dd HH:mm:ss"

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
