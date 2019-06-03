package com.data.collection.data.greendao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class GatherPoint {

    private static final String TAG = "GatherPoint";
    @Id
    private Long offline_id; // 本地数据库 id
    @Unique
    private String id; // 同步数据库ID

    @NotNull
    private String name;

    @NotNull
    private String type_id; //  类型type_id

    private String attrs; //  描述文件 ： 定义类型选项，属性名称等

    @NotNull
    private double latitude;

    @NotNull
    private double longitude;

    @NotNull
    private double height;

    @Unique
    private String collected_at; // 采集时间 格式 2019-06-05 12:30:65

    private String report = "self"; //  上报人。 上报人和采集时间确定更新一条采集记录。

    private String  desc; // 备注

    private String picPath1; // 本地图片路径
    private String picPath2; // 本地图片路径
    private String picPath3; // 本地图片路径

    private String imgs; //  网络图片路径

    private boolean isUploaded; // 是否已经上传

    @Generated(hash = 1458689392)
    public GatherPoint(Long offline_id, String id, @NotNull String name,
            @NotNull String type_id, String attrs, double latitude,
            double longitude, double height, String collected_at, String report,
            String desc, String picPath1, String picPath2, String picPath3,
            String imgs, boolean isUploaded) {
        this.offline_id = offline_id;
        this.id = id;
        this.name = name;
        this.type_id = type_id;
        this.attrs = attrs;
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
        this.collected_at = collected_at;
        this.report = report;
        this.desc = desc;
        this.picPath1 = picPath1;
        this.picPath2 = picPath2;
        this.picPath3 = picPath3;
        this.imgs = imgs;
        this.isUploaded = isUploaded;
    }

    @Generated(hash = 783740109)
    public GatherPoint() {
    }

    public Long getOffline_id() {
        return this.offline_id;
    }

    public void setOffline_id(Long offline_id) {
        this.offline_id = offline_id;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType_id() {
        return this.type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }

    public String getAttrs() {
        return this.attrs;
    }

    public void setAttrs(String attrs) {
        this.attrs = attrs;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getHeight() {
        return this.height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getCollected_at() {
        return this.collected_at;
    }

    public void setCollected_at(String collected_at) {
        this.collected_at = collected_at;
    }

    public String getReport() {
        return this.report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPicPath1() {
        return this.picPath1;
    }

    public void setPicPath1(String picPath1) {
        this.picPath1 = picPath1;
    }

    public String getPicPath2() {
        return this.picPath2;
    }

    public void setPicPath2(String picPath2) {
        this.picPath2 = picPath2;
    }

    public String getPicPath3() {
        return this.picPath3;
    }

    public void setPicPath3(String picPath3) {
        this.picPath3 = picPath3;
    }

    public String getImgs() {
        return this.imgs;
    }

    public void setImgs(String imgs) {
        this.imgs = imgs;
    }

    public boolean getIsUploaded() {
        return this.isUploaded;
    }

    public void setIsUploaded(boolean isUploaded) {
        this.isUploaded = isUploaded;
    }

    



}
