package com.data.collection.data.greendao;

import android.support.annotation.Nullable;

import com.baidu.mapapi.model.LatLng;
import com.data.collection.data.CacheData;
import com.data.collection.util.Utils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;


@Entity
public class SimplePoint implements Serializable, Comparable<SimplePoint> {
    private static final long serialVersionUID = 10L;

    private static final String TAG = "SimplePoint";
    @Id(autoincrement = true)
    private Long id; // 本地数据库 id 1

    @NotNull
    private int index; // 图形中的顺序位置

    @NotNull
    private String latitude;

    @NotNull
    private String longitude;

    @NotNull
    private String height;

    @NotNull
    private long ploygonId;

    @Override
    public int compareTo(SimplePoint o) {
        return index - o.getIndex();
    }

    @Generated(hash = 1883378168)
    public SimplePoint(Long id, int index, @NotNull String latitude,
            @NotNull String longitude, @NotNull String height, long ploygonId) {
        this.id = id;
        this.index = index;
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
        this.ploygonId = ploygonId;
    }

    @Generated(hash = 2116514058)
    public SimplePoint() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getHeight() {
        return this.height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public long getPloygonId() {
        return this.ploygonId;
    }

    public void setPloygonId(long ploygonId) {
        this.ploygonId = ploygonId;
    }

}
