package com.data.collection.data.greendao;

import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;


@Entity
public class TraceLocation {

    @Id(autoincrement = true)
    private Long id ;
    @NotNull
    private String longitude;
    @NotNull
    private String latitude;
    @Unique
    private String time;

    private boolean isUpload;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Generated(hash = 1992950212)
    public TraceLocation(Long id, @NotNull String longitude,
            @NotNull String latitude, String time, boolean isUpload) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.time = time;
        this.isUpload = isUpload;
    }

    @Generated(hash = 2045961590)
    public TraceLocation() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean getIsUpload() {
        return this.isUpload;
    }

    public void setIsUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }

    
}
