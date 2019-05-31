package com.data.collection.data.greendao;

import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class PictureMap {

    private static final String TAG = "PictureMap";
    @Id
    private String urlpath;  // 图片的网络地址

    private String localpath; // 图片的本地地址

    @Generated(hash = 1179034859)
    public PictureMap(String urlpath, String localpath) {
        this.urlpath = urlpath;
        this.localpath = localpath;
    }

    @Generated(hash = 763409174)
    public PictureMap() {
    }

    public String getUrlpath() {
        return this.urlpath;
    }

    public void setUrlpath(String urlpath) {
        this.urlpath = urlpath;
    }

    public String getLocalpath() {
        return this.localpath;
    }

    public void setLocalpath(String localpath) {
        this.localpath = localpath;
    }
}
