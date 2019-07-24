package com.data.collection.view;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.esri.arcgisruntime.arcgisservices.TileInfo;
import com.esri.arcgisruntime.data.TileKey;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.internal.jni.CoreImageTiledLayer;
import com.esri.arcgisruntime.layers.ImageTiledLayer;

public class MBTilesLayer  extends ImageTiledLayer {
    String mMbTilesFilename;
    private SQLiteDatabase mapDb;
    //构造一个离线MBTiles图层
    public MBTilesLayer(String _mbTilesPath){
        super(MbtilesUtil.newInstance().getMBTileTileinfo(_mbTilesPath),
                MbtilesUtil.newInstance().getMBTileEnvelope(_mbTilesPath) );

        mMbTilesFilename = _mbTilesPath;

        try {
            mapDb = SQLiteDatabase.openDatabase( mMbTilesFilename, null, SQLiteDatabase.OPEN_READONLY );
            //setBufferSize(BufferSize.NONE);
        } catch (SQLException ex) {
            Log.e(this.getName(), ex.getMessage());
        }

    }
    protected MBTilesLayer(TileInfo tileInfo, Envelope fullExtent) {
        super(tileInfo, fullExtent);
    }

    protected MBTilesLayer(CoreImageTiledLayer coreTiledLayer, boolean addToCache) {
        super(coreTiledLayer, addToCache);
    }

    @Override
    protected byte[] getTile(TileKey tileKey) {
        // need to flip origin
        int nRows = (1 << tileKey.getLevel()); // Num rows = 2^level
        int tmsRow = nRows - 1 - tileKey.getRow();

        String sql = "SELECT tile_data FROM tiles WHERE zoom_level = " + Integer.toString( tileKey.getLevel() )
                + " AND tile_column = " + Integer.toString( tileKey.getColumn() ) + " AND tile_row = " + Integer.toString( tmsRow );
        System.out.println( "Sql " + sql );

        Cursor imageCur = mapDb.rawQuery(sql,null);
        if (imageCur.moveToFirst()) {
            return imageCur.getBlob(0);
        }

        return null; // Alternatively we might return a "no data" tile
    }

    public static class  MbtilesUtil{

        static MbtilesUtil instance = new MbtilesUtil();

        public static MbtilesUtil newInstance() {
            return instance;
        }

        public TileInfo getMBTileTileinfo(String mbTilesPath) {
            return null;
        }

        public Envelope getMBTileEnvelope(String mbTilesPath) {
            return null;
        }
    }
}
