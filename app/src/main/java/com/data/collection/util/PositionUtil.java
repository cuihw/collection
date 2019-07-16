package com.data.collection.util;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.data.collection.module.Gps;

/**
 * 各地图API坐标转换;
 * WGS84坐标系：国际上通用的坐标系;
 * GCJ02坐标系：即火星坐标系。
 * BD-09： 百度坐标系
 */
public class PositionUtil {

    public static final String BAIDU_LBS_TYPE = "bd09ll";

    public static double pi = 3.1415926535897932384626;
    public static double a = 6378245.0;
    public static double ee = 0.00669342162296594323;


    /**
     * 经纬度转墨卡托
     * @param LonLat 经纬度坐标
     * @return
     */
    public static Gps lonLatToMercator(Gps LonLat){
        double x =  (LonLat.getWgLon() * 20037508.342789 / 180);
        double y =  (Math.log(Math.tan((90 + LonLat.getWgLat()) * Math.PI / 360)) / (Math.PI / 180));
        y =  (double)(y * 20037508.342789 / 180);
        Gps mercator = new Gps(x, y);
        return mercator;
    }

    /**
     * 墨卡托转经纬度
     * @param mercator 墨卡托坐标
     * @return
     */
    public static Gps mercatorToLonLat(Gps mercator){
        double x =   (mercator.getWgLon()/ 20037508.342789 * 180);
        double y =  (mercator.getWgLat() / 20037508.342789 * 180);
        y = (double) (180 / Math.PI * (2 * Math.atan(Math.exp(y * Math.PI / 180)) - Math.PI / 2));
        Gps lonlat  = new Gps(x, y);
        return lonlat;
    }


    /**
     * 84 to 火星坐标系 (GCJ-02)
     *
     * @param lat
     * @param lon
     * @return
     */
    public static Gps gps84_To_Gcj02(double lon, double lat) {
        if (outOfChina(lon, lat)) {
            return null;
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new Gps(mgLon, mgLat);
    }

    /**
     * * 火星坐标系 (GCJ-02) to 84 * * @param lon * @param lat * @return
     * */
    public static Gps gcj_To_Gps84(double lon, double lat) {
        Gps gps = transform(lon, lat);
//        double lontitude = lon * 2 - gps.getWgLon();
//        double latitude = lat * 2 - gps.getWgLat();
//        return new Gps(lontitude, latitude);
        return gps;
    }

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 GCJ-02 坐标转换成 BD-09 坐标
     *
     * @param gg_lat
     * @param gg_lon
     */
    public static Gps gcj02_To_Bd09(double gg_lon, double gg_lat) {
        double x = gg_lon, y = gg_lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * pi);
        double bd_lat = z * Math.cos(theta) + 0.0065;
        double bd_lon = z * Math.sin(theta) + 0.006;
        return new Gps(bd_lon, bd_lat);
    }

    /**
     * * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 * * 将 BD-09 坐标转换成GCJ-02 坐标 * * @param
     * bd_lat * @param bd_lon * @return
     */
    public static Gps bd09_To_Gcj02(double bd_lon, double bd_lat) {
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * pi);
        double gg_lat = z * Math.cos(theta);
        double gg_lon = z * Math.sin(theta);
        return new Gps(gg_lon, gg_lat);
    }

    /**
     * (BD-09)-->84
     * @param bd_lat
     * @param bd_lon
     * @return
     */
    public static Gps bd09_To_Gps84(double bd_lon, double bd_lat) {
        Gps gcj02 = PositionUtil.bd09_To_Gcj02(bd_lat, bd_lon);
        Gps map84 = PositionUtil.gcj_To_Gps84(gcj02.getWgLat(), gcj02.getWgLon());
        return map84;
    }

    public static boolean outOfChina(double lon, double lat) {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return false;
    }

    public static Gps transform(double lon, double lat) {
        if (outOfChina(lon, lat)) {
            return new Gps(lat, lon);
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new Gps(mgLon, mgLat);
    }

    public static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    public static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
                * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
                * pi)) * 2.0 / 3.0;
        return ret;
    }

    public static LatLng GpsToBaiduLatLng(LatLng sourceLatLng) {
        CoordinateConverter converter  = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        // sourceLatLng 待转换坐标
        converter.coord(sourceLatLng);
        return converter.convert();
    }

    public static LatLng BaiduLatLngToGps(LatLng sourceLatLng) {
        CoordinateConverter converter  = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.BD09LL);
        // sourceLatLng 待转换坐标
        converter.coord(sourceLatLng);
        return converter.convert();
    }

}


