package com.data.collection.Tiles;

import android.util.Log;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.MapTileIndex;

public class GoogleTileSource extends TileSourceFactory {

    private static final String TAG = "GoogleTileSource";
    //谷歌卫星混合
    public static final OnlineTileSourceBase GoogleHybrid = new XYTileSource("Google-Hybrid",
            0, 19, 512, ".png", new String[]{
            "http://mt0.google.cn",
            "http://mt1.google.cn",
            "http://mt2.google.cn",
            "http://mt3.google.cn",
    }) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            Log.w("url", getBaseUrl() + "/vt/lyrs=y&scale=2&hl=zh-CN&gl=CN&src=app&x="
                    + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex));
            return getBaseUrl() + "/vt/lyrs=y&scale=2&hl=zh-CN&gl=CN&src=app&x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex);
        }
    };

    //谷歌卫星
    public static final OnlineTileSourceBase GoogleSat = new XYTileSource("Google-Sat",
            0, 19, 512, ".png", new String[]{
            "http://mt0.google.cn",
            "http://mt1.google.cn",
            "http://mt2.google.cn",
            "http://mt3.google.cn",

    }) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            Log.w(TAG, "url = " + getBaseUrl() + "/vt/lyrs=s&scale=2&hl=zh-CN&gl=CN&src=app&x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex));
            return getBaseUrl() + "/vt/lyrs=s&scale=2&hl=zh-CN&gl=CN&src=app&x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex);
        }
    };
    //谷歌地图
    public static final OnlineTileSourceBase GoogleRoads = new XYTileSource("Google-Roads",
            0, 18, 512, ".png", new String[]{
            "http://mt0.google.cn",
            "http://mt1.google.cn",
            "http://mt2.google.cn",
            "http://mt3.google.cn",

    }) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            return getBaseUrl() + "/vt/lyrs=m&scale=2&hl=zh-CN&gl=CN&src=app&x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex);
        }
    };
    //谷歌地形
    public static final OnlineTileSourceBase GoogleTerrain = new XYTileSource("Google-Terrain",
            0, 16, 512, ".png", new String[]{
            "http://mt0.google.cn",
            "http://mt1.google.cn",
            "http://mt2.google.cn",
            "http://mt3.google.cn",

    }) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            return getBaseUrl() + "/vt/lyrs=t&scale=2&hl=zh-CN&gl=CN&src=app&x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex);
        }
    };
    //谷歌地形带标注
    public static final OnlineTileSourceBase GoogleTerrainHybrid = new XYTileSource("Google-Terrain-Hybrid",
            0, 16, 512, ".png", new String[]{
            "http://mt0.google.cn",
            "http://mt1.google.cn",
            "http://mt2.google.cn",
            "http://mt3.google.cn",

    }) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            return getBaseUrl() + "/vt/lyrs=p&scale=2&hl=zh-CN&gl=CN&src=app&x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex);
        }
    };
    //高德地图
    public static final OnlineTileSourceBase AutoNaviVector = new XYTileSource("AutoNavi-Vector",
            0, 20, 256, ".png", new String[]{
            "https://wprd01.is.autonavi.com/appmaptile?",
            "https://wprd02.is.autonavi.com/appmaptile?",
            "https://wprd03.is.autonavi.com/appmaptile?",
            "https://wprd04.is.autonavi.com/appmaptile?",

    }) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            return getBaseUrl() + "x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z="
                    + MapTileIndex.getZoom(pMapTileIndex) + "&lang=zh_cn&size=1&scl=1&style=7&ltype=7";
        }
    };
    //影像地图 _W是墨卡托投影  _c是国家2000的坐标系
    public static final OnlineTileSourceBase tianDiTuImgTileSource = new XYTileSource("TianDiTuImg", 1, 18, 768, ".png",
            new String[]{"http://t1.tianditu.com/DataServer?T=img_w",
                    "http://t2.tianditu.com/DataServer?T=img_w",
                    "http://t3.tianditu.com/DataServer?T=img_w",
                    "http://t4.tianditu.com/DataServer?T=img_w",
                    "http://t5.tianditu.com/DataServer?T=img_w",
                    "http://t6.tianditu.com/DataServer?T=img_w"}) {
        @Override
        public String getTileURLString(final long pMapTileIndex) {
            return getBaseUrl() + "&X=" + MapTileIndex.getX(pMapTileIndex) + "&Y=" + MapTileIndex.getY(pMapTileIndex)
                    + "&L=" + MapTileIndex.getZoom(pMapTileIndex);
        }
    };

    // https://a.tile.openstreetmap.org/9/420/193.png
    public static final OnlineTileSourceBase openstreetmap = new XYTileSource("openmapstreet",1, 20, 256, ".png",
            new String[]{"https://a.tile.openstreetmap.org"}){
        @Override
        public String getTileURLString(final long pMapTileIndex) {

            Log.w(TAG, getBaseUrl() + "/" + MapTileIndex.getZoom(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex)
                    + "/" + MapTileIndex.getY(pMapTileIndex)+ ".png");
            return getBaseUrl() + "/" + MapTileIndex.getZoom(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex)
                    + "/" + MapTileIndex.getY(pMapTileIndex)+ ".png";
        }
    };

    //影像标注 _W是墨卡托投影  _c是国家2000的坐标系
    public static final OnlineTileSourceBase tianDiTuCiaTileSource = new XYTileSource("TianDiTuCia",
            //1, 18, 768, ".png",
            1, 20, 256, ".png",
            new String[]{"http://t0.tianditu.gov.cn/img_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles"}) {
            String key = "&tk=b269d6e6de3bc4c49fe9a2ba6900b1e5";

//        new String[]{"http://t1.tianditu.com/DataServer?T=cia_w?tk=b269d6e6de3bc4c49fe9a2ba6900b1e5",
//                "http://t2.tianditu.com/DataServer?T=cia_w?tk=b269d6e6de3bc4c49fe9a2ba6900b1e5",
//                "http://t3.tianditu.com/DataServer?T=cia_w?tk=b269d6e6de3bc4c49fe9a2ba6900b1e5",
//                "http://t4.tianditu.com/DataServer?T=cia_w?tk=b269d6e6de3bc4c49fe9a2ba6900b1e5",
//                "http://t5.tianditu.com/DataServer?T=cia_w?tk=b269d6e6de3bc4c49fe9a2ba6900b1e5",
//                "http://t6.tianditu.com/DataServer?T=cia_w?tk=b269d6e6de3bc4c49fe9a2ba6900b1e5"}) {
        // http://t0.tianditu.gov.cn/img_w/wmts?tk=b269d6e6de3bc4c49fe9a2ba6900b1e5

        // http://t0.tianditu.gov.cn/img_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&tk=您的密钥&
        // TILEMATRIX={z}&TILEROW={x}&TILECOL={y}
        // http://t5.tianditu.gov.cn/img_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&tk=b269d6e6de3bc4c49fe9a2ba6900b1e5&TILEROW={836}&TILECOL={405}&TILEMATRIX={10}
        @Override
        public String getTileURLString(final long pMapTileIndex) {

            Log.w(TAG, getBaseUrl() + "&TILEROW={" + MapTileIndex.getX(pMapTileIndex) + "}&TILECOL={" + MapTileIndex.getY(pMapTileIndex)
                    + "}&TILEMATRIX={" + MapTileIndex.getZoom(pMapTileIndex)+ "}" + key);
            return getBaseUrl() + "&TILEROW={" + MapTileIndex.getX(pMapTileIndex) + "}&TILECOL={" + MapTileIndex.getY(pMapTileIndex)
                    + "}&TILEMATRIX={" + MapTileIndex.getZoom(pMapTileIndex)+ "}" + key;
        }
    };

}
