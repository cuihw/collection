package com.data.collection.module;

public class Gps {
    double wgLon;
    double wgLat;

    public Gps(double lat, double lon) {
        wgLon = lon;
        wgLat = lat;
    }

    public double getWgLon() {
        return wgLon;
    }

    public double getWgLat() {
        return wgLat;
    }
}
