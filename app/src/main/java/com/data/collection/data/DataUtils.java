package com.data.collection.data;

import android.os.AsyncTask;
import android.util.Log;

import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.data.greendao.DaoSession;
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.data.greendao.GatherPointDao;
import com.data.collection.listener.IGatherDataListener;
import com.data.collection.module.CollectType;
import com.data.collection.util.LsLog;
import com.data.collection.util.PositionUtil;
import com.data.collection.util.ToastUtil;

import org.greenrobot.greendao.query.QueryBuilder;
import org.osmdroid.util.BoundingBox;

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

}
