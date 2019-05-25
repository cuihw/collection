package com.data.collection.util;

import com.data.collection.module.Gps;

import org.junit.Test;

import static org.junit.Assert.*;

public class PositionUtilTest {
    double longitude = 113.647881;
    double latitude = 34.829793;
    @Test
    public void gps84_To_Gcj02() {
        Gps gps = PositionUtil.gps84_To_Gcj02(longitude, latitude);
        System.out.println("gps : longitude = " + gps.getWgLon() + ", latitude = " + gps.getWgLat());
        //longitude = 34.8287280094783, latitude = 113.65396374215176
    }

    @Test
    public void gcj_To_Gps84() {

    }

    @Test
    public void gcj02_To_Bd09() {
    }

    @Test
    public void bd09_To_Gcj02() {
    }

    @Test
    public void bd09_To_Gps84() {
    }

    @Test
    public void gpsToBaiduLatLng() {
    }
}