package com.data.collection.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.constraint.solver.widgets.Rectangle;
import android.util.Log;

import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.activity.AddCollectionActivity;
import com.data.collection.data.greendao.DaoSession;
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.data.greendao.GatherPointDao;
import com.data.collection.data.tiff.extended.GeoTiffImage;
import com.data.collection.dialog.ImageDialog;
import com.data.collection.fragment.FragmentHome;
import com.data.collection.listener.IGatherDataListener;
import com.data.collection.listener.ITiffListener;
import com.data.collection.module.CollectType;
import com.data.collection.util.FileUtils;
import com.data.collection.util.LsLog;
import com.data.collection.util.PositionUtil;
import com.data.collection.util.ToastUtil;
import com.google.gson.Gson;

import org.beyka.tiffbitmapfactory.TiffBitmapFactory;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.greenrobot.greendao.query.QueryBuilder;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.GroundOverlay2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataUtils {

    private static final String TAG = "DataUtils";
    BoundingBox boundingBox;

    final static double RANGE = 0.01;


    public static void asyncPointsByBounds(BoundingBox boundingBox,
                                           IGatherDataListener lintener){
        asyncPointsByBounds(boundingBox.getLatNorth(),
                boundingBox.getLatSouth(), boundingBox.getLonEast(),
                boundingBox.getLonWest(), false,lintener);
    }

    public static void asyncPointsByBounds(BoundingBox boundingBox,
                                           boolean isUploaded,
                                           IGatherDataListener lintener){
        asyncPointsByBounds(boundingBox.getLatNorth(),
                boundingBox.getLatSouth(), boundingBox.getLonEast(),
        boundingBox.getLonWest(), isUploaded,lintener);
    }

    public static void asyncPointsByBounds(double latNorth,
                                                      double latSouth,
                                                      double lonEast,
                                                      double lonWest,
                                           boolean isUploaded,
                                           IGatherDataListener lintener){

        new AsyncTask<Double, Integer, List<GatherPoint>>(){
            @Override
            protected List doInBackground(Double... doubles) {
                DaoSession daoSession = App.getInstence().getDaoSession();
                doubles[0] = doubles[0] - RANGE;
                doubles[1] = doubles[1] + RANGE;
                doubles[2] = doubles[2] - RANGE;
                doubles[3] = doubles[3] + RANGE;

                QueryBuilder<GatherPoint> qb = daoSession.queryBuilder(GatherPoint.class)
                        .where(GatherPointDao.Properties.Latitude.ge("" + doubles[0]),
                                GatherPointDao.Properties.Latitude.le("" + doubles[1]),
                                GatherPointDao.Properties.Longitude.gt("" + doubles[2]),
                                GatherPointDao.Properties.Longitude.le("" + doubles[3]))
                        .orderDesc(GatherPointDao.Properties.Updated_at);
                if (isUploaded) {
                    qb.where(GatherPointDao.Properties.IsUploaded.eq(true));
                }
                List<GatherPoint> list = qb.list();
                return list;
            }

            @Override
            protected void onPostExecute(List<GatherPoint> list) {
                if (lintener != null) lintener.onListData(list);
            }
        }.execute(latSouth, latNorth, lonWest, lonEast);
    }

    public static CollectType getTypeIconUrl(GatherPoint gp){
        String type_id = gp.getType_id();
        Map<String, CollectType> typeMaps = CacheData.getTypeMaps();
        CollectType type = typeMaps.get(type_id);
        return type;
    }

    public static Adjust GOOGLE_ADJUST = new Adjust();

    public static GeoPoint adjustPoint(GeoPoint geoPoint, int mapType) {
        switch (mapType) {
            case Constants.GOOGLE_MAP_SOURCE:
                geoPoint.setLatitude(geoPoint.getLatitude() + GOOGLE_ADJUST.adjustLat);
                geoPoint.setLongitude(geoPoint.getLongitude() + GOOGLE_ADJUST.adjustlng);
                break;
            case Constants.OPEN_TOPO_SOURCE:
                break;
        }
        return geoPoint;
    }

    static public Adjust getGoogleAdjust(){
        return GOOGLE_ADJUST;
    }

    public static void setGoogleAdjust(Adjust googleAdjust) {
        if (googleAdjust == null) {LsLog.w(TAG, "adjust is null"); return;}
        GOOGLE_ADJUST = googleAdjust;
    }

    public static void loadTif(String filepath, ITiffListener listener) {

        new AsyncTask<String, Integer, GeoTiffImage>(){
            @Override
            protected GeoTiffImage doInBackground(String... strings) {
                File file = new File(strings[0]);
                if (!file.exists()) {
                    Log.w(TAG, "not found file. filename = " + strings[0]);
                    return null;
                }
                try {
                    GeoTiffImage tiffImage = new GeoTiffImage(file);
                    if (tiffImage.parse()) return tiffImage;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(GeoTiffImage geoTiffImage) {
                if (listener != null) listener.onFileReady(geoTiffImage);
            }
        }.execute(filepath);

    }

    public static void decodeTiff(File filename) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GeoTiffImage tiffImage = new GeoTiffImage(filename);
                    tiffImage.parse();
                    Rectangle bounds = tiffImage.getBounds();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    public static void readTiff(Context context) {
//        ssss.tif
        String path = FileUtils.getFileDir() + "zhengzhou.tif";



        Log.w(TAG, "path = " + path);
        File file = new File(path);
        if (!file.exists()) {
            Log.w(TAG, "not found file. filename = " + path);
            return;
        }
        decodeTiff(file);
        // decodeByTiffBitmapFactory(context, file);
    }

    private static void decodeByTiffBitmapFactory(Context context, File file) {
        int reqHeight = 2048;
        int reqWidth = 2048;
        //  Read data about image to Options object
        TiffBitmapFactory.Options options = new TiffBitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = TiffBitmapFactory.decodeFile(file, options);

        if (imageDialog == null) {
            imageDialog = new ImageDialog(context, bitmap);
        }
        if (!imageDialog.getDialog().isShowing()) {
            imageDialog.show();
        }

        Log.w(TAG, "file options = " + new Gson().toJson(options));

        int dirCount = options.outDirectoryCount;
        Log.w(TAG, "dirCount = " + dirCount);
        // Read and process all images in file
        for (int i = 0; i < dirCount; i++) {
            options.inDirectoryNumber = i;
            TiffBitmapFactory.decodeFile(file, options);

            Log.w(TAG, i + " file options = " + new Gson().toJson(options));
            int curDir = options.outCurDirectoryNumber;
            int width = options.outWidth;
            int height = options.outHeight;

            Log.w(TAG, i + " file options outXResolution = " + options.outXResolution + ",  outYResolution = " + options.outYResolution);

            //Change sample size if width or height bigger than required width or height
            int inSampleSize = 1;
            if (height > reqHeight && width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;

            // Specify the amount of memory available for the final bitmap and temporary storage.
            options.inAvailableMemory = 20000000; // bytes

            Bitmap bmp = TiffBitmapFactory.decodeFile(file, options);
            // processBitmap(bmp);
        }
    }

    static ImageDialog imageDialog;

    static public class Adjust{
        /*
    //shanghai
    var xOffset= -0.001889737;
    var yOffset= 0.004844069;*/
        public double adjustLat = - 0.0005;
        public double adjustlng = + 0.0063;
    }


}
