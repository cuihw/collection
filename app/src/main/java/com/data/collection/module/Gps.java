package com.data.collection.module;

public class Gps {
    double wgLon;  // Mercator x
    double wgLat;  // Mercator y

    public Gps(){}

    public Gps( double lon, double lat) {
        wgLon = lon;
        wgLat = lat;
    }

    public double getWgLon() {
        return wgLon;
    }

    public double getWgLat() {
        return wgLat;
    }

    @Override
    public String toString() {
        return "[" + wgLon + ", " + wgLat + "]";
    }
}
