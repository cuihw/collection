package com.data.collection.data.greendao;

import com.data.collection.util.LsLog;
import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@Entity
public class GatherPoint {

    private static final String TAG = "GatherPoint";
    @Id
    private Long id;

    @NotNull
    private String type; //  类型

    private String description; //  描述文件 ： 定义类型选项，属性名称等

    @NotNull
    private double latitude;

    @NotNull
    private double longitude;

    @NotNull
    private double altitude;

    @Unique
    private String gatherTime; // 采集时间 格式 2019-06-05 12:30:65

    private String report; //  上报人。 上报人和采集时间确定更新一条采集记录。

    private String uploadedTime; // 上传时间默认时间是空 格式 2019-06-05 12:30:65  先下载，然后再上传本地数据。

    private String  comments; // 备注

    private String  gatherInfo; // json组合而成的属性信息。


    private String picPath1; // 图片路径
    private String picPath2; // 图片路径
    private String picPath3; // 图片路径

    private boolean isUploaded; // 是否已经上传

    public String toJson(){
        return new Gson().toJson(this);
    }

    public void putKeyValue(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        JSONObject jsonObject = new JSONObject(map);
        gatherInfo = jsonObject.toString();
        LsLog.w(TAG, "jsonObject = " + jsonObject.toString());
    }

    public  Map<String, Object> getKeyValue(){
        try {
            Map<String, Object> map = new HashMap<>();
            JSONObject jsonObject = new JSONObject(gatherInfo);
            return map;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Generated(hash = 830396368)
    public GatherPoint(Long id, @NotNull String type, String description, double latitude,
            double longitude, double altitude, String gatherTime, String report,
            String uploadedTime, String comments, String gatherInfo, String picPath1,
            String picPath2, String picPath3, boolean isUploaded) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.gatherTime = gatherTime;
        this.report = report;
        this.uploadedTime = uploadedTime;
        this.comments = comments;
        this.gatherInfo = gatherInfo;
        this.picPath1 = picPath1;
        this.picPath2 = picPath2;
        this.picPath3 = picPath3;
        this.isUploaded = isUploaded;
    }

    @Generated(hash = 783740109)
    public GatherPoint() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
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
    public double getAltitude() {
        return this.altitude;
    }
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
    public String getGatherTime() {
        return this.gatherTime;
    }
    public void setGatherTime(String gatherTime) {
        this.gatherTime = gatherTime;
    }
    public String getComments() {
        return this.comments;
    }
    public void setComments(String comments) {
        this.comments = comments;
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
    public String getGatherInfo() {
        return this.gatherInfo;
    }
    public void setGatherInfo(String gatherInfo) {
        this.gatherInfo = gatherInfo;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReport() {
        return this.report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getUploadedTime() {
        return this.uploadedTime;
    }

    public void setUploadedTime(String uploadedTime) {
        this.uploadedTime = uploadedTime;
    }

    public boolean getIsUploaded() {
        return this.isUploaded;
    }

    public void setIsUploaded(boolean isUploaded) {
        this.isUploaded = isUploaded;
    }

}
