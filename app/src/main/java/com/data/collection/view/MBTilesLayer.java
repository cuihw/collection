package com.data.collection.view;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.data.collection.util.DensityUtil;
import com.esri.arcgisruntime.arcgisservices.LevelOfDetail;
import com.esri.arcgisruntime.arcgisservices.TileInfo;
import com.esri.arcgisruntime.data.TileKey;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeographicTransformation;
import com.esri.arcgisruntime.geometry.GeographicTransformationStep;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.ImageTiledLayer;

import java.util.ArrayList;
import java.util.List;

public class MBTilesLayer extends ImageTiledLayer {
    private static final String TAG = "MBTilesLayer";
    private static SQLiteDatabase mapDb;
    private static int maxLevels = 0;
    private static int minLevels = 0;
    static String mMbTilesFilename;

    List<LevelOfDetail> levelOfDetails;

    private static SpatialReference  SpatialReference_gws_84 = SpatialReference.create(4326);

    private static SpatialReference  mercato=  SpatialReference.create(3857);

    public static MBTilesLayer create(String _mbTilesPath){
        mMbTilesFilename = _mbTilesPath;

        try {
            mapDb = SQLiteDatabase.openDatabase(_mbTilesPath, null, SQLiteDatabase.OPEN_READONLY );
        } catch (SQLException ex) {
            Log.e(TAG, ex.getMessage());
            throw (ex);
        }

        Envelope mbTileEnvelope = createMbTileEnvelope();
        TileInfo tileInfo = createTileInfo();
        return new MBTilesLayer(tileInfo, mbTileEnvelope);
    }

    private static TileInfo createTileInfo() {
//      public TileInfo​(int dpi,
//                TileInfo.ImageFormat imageFormat,
//                List<LevelOfDetail> levelOfDetails,
//                Point origin,
//                SpatialReference spatialReference,
//                int tileHeight,
//                int tileWidth)


        List<LevelOfDetail> levelOfDetails = new ArrayList<>();
        double scale = 0;
        double resolution = 0;


        Envelope envWeb = createMbTileEnvelope();

        double xMin = envWeb.getXMin();
        double yMin = envWeb.getYMin();
        Point origin = new Point(xMin, yMin, mercato);

        Cursor maxLevelCur = mapDb.rawQuery("SELECT MAX(zoom_level) AS max_zoom FROM tiles", null);
        if (maxLevelCur.moveToFirst()) {
            maxLevels = maxLevelCur.getInt(0);
        }
        Cursor minLevelCur = mapDb.rawQuery("SELECT MIN(zoom_level) AS min_zoom FROM tiles", null);
        if (minLevelCur.moveToFirst()) {
            minLevels = minLevelCur.getInt(0);
        }
        Log.w(TAG, "origin = " + origin.toString() + ", maxLevels = " + maxLevels + ", minLevels = " + minLevels);

        for (int i = 0; i <= maxLevels; i++) {
            // see the TMS spec for derivation of the level 0 scale and resolution
            // For each level the resolution (in meters per pixel) doubles
            resolution = 156543.032 / Math.pow(2, i);
            // Level 0 scale is 1:554,678,932. Each level doubles this.
            scale = 554678932 / Math.pow(2, i);
            Log.w(TAG, "level = " + i + ", scale = " + scale + ", resolution = " + resolution);
            LevelOfDetail lod = new LevelOfDetail(i, resolution ,scale);
            levelOfDetails.add(lod);
        }
        return new TileInfo(96, TileInfo.ImageFormat.PNG, levelOfDetails, origin,
                mercato,256,256);

    }
    /*          new LevelOfDetail (0,156543.033928023,591657527.591555),
                new LevelOfDetail (1,78271.5169640117,295828763.795778),
                new LevelOfDetail (2,39135.7584820059,147914381.897889),
                new LevelOfDetail (3,19567.8792410029,73957190.9489444),
                new LevelOfDetail (4,9783.93962050147,36978595.4744722),
                new LevelOfDetail (5,4891.96981025073,18489297.7372361),
                new LevelOfDetail (6,2445.98490512537,9244648.86861805),
                new LevelOfDetail (7,1222.99245256268,4622324.43430902),
                new LevelOfDetail (8,611.496226281342,2311162.21715451),
                new LevelOfDetail (9,305.748113140671,1155581.10857726),
                new LevelOfDetail (10,152.874056570335,577790.554288628),
                new LevelOfDetail (11,76.4370282851677,288895.277144314),
                new LevelOfDetail (12,38.2185141425838,144447.638572157),
                new LevelOfDetail (13,19.1092570712919,72223.8192860785),
                new LevelOfDetail (14,9.55462853564596,36111.9096430392),
                new LevelOfDetail (15,4.77731426782298,18055.9548215196),
                new LevelOfDetail (16,2.38865713391149,9027.97741075981),
                new LevelOfDetail (17,1.19432856695575,4513.98870537991),
                new LevelOfDetail (18,0.597164283477873,2256.99435268995),
                new LevelOfDetail (19,0.298582141738936,1128.49717634498),*/
    private static Envelope createMbTileEnvelope(){
        // Envelope​(double x1, double y1, double x2, double y2, SpatialReference spatialReference)
        Envelope envWGS = new Envelope(-180.0, -85.0511, 180.0, 85.0511, SpatialReference_gws_84);

        Cursor bounds = mapDb.rawQuery("SELECT value FROM metadata WHERE name = 'bounds'", null);
        if (bounds !=null && bounds.moveToFirst()) {
            String bs = bounds.getString(0);
            String[] ba = bs.split(",", 4);
            if (ba.length == 4) {
                double leftLon = Double.parseDouble(ba[0]);
                double topLat = Double.parseDouble(ba[3]);
                double rightLon = Double.parseDouble(ba[2]);
                double bottomLat = Double.parseDouble(ba[1]);

                envWGS = new Envelope(leftLon, bottomLat, rightLon, topLat, SpatialReference_gws_84);
            }
        }
        Envelope envWeb = (Envelope) GeometryEngine.project(envWGS, mercato);

        return envWeb;
    }

    public MBTilesLayer( TileInfo tileInfo,Envelope env){
        super(tileInfo, env);
    }

    @Override
    protected byte[] getTile(TileKey tileKey) {

        byte[] tile = null;
        try {
            tile = getTile(tileKey.getLevel(), tileKey.getColumn(), tileKey.getRow());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tile;
    }
    private  byte[] getTile(int level, int col, int row) throws Exception {

        // need to flip origin
        int nRows = (1 << level); // Num rows = 2^level
        int tmsRow = nRows - 1 - row;

        Cursor imageCur = mapDb.rawQuery("SELECT tile_data FROM tiles WHERE zoom_level = " + Integer.toString(level)
                + " AND tile_column = " + Integer.toString(col) + " AND tile_row = " + Integer.toString(tmsRow), null);

        Log.i(TAG, "level = " + level);
        if (imageCur.moveToFirst()) {
            return imageCur.getBlob(0);
        }
        return null; // Alternatively we might return a "no data" tile
    }

}
