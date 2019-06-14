package com.data.collection.module;

import com.data.collection.data.greendao.CheckPoint;

public class CheckData {
    private String id;

    private String content;

    private String longitude;

    private String latitude;

    private String height;

    private String created_at;

    private String user_name;

    private String collect_name;

    private String collect_id;

    /* private String OnlineId; // 检查内容的ID

    @NotNull
    private Long pointid; // 检查点的ID,  根据ID可以获得坐标和属性等相关信息， 本地ID。

    private String pointOnlineId; // 检查点的ID, 线上id HAO, 没有上传的时候，可能为空。

    private String name; // 检查点名字

    private String time;// 检查时间 格式 2019-06-05 12:30:65

    private String reporter; // 上报人

    private String  check; // 检查内容

    private boolean isUploaded;*/
    public CheckPoint getCheckpoint(){
        CheckPoint checkPoint = new CheckPoint();
        checkPoint.setOnlineId(id);
        checkPoint.setCollectOnlineId(collect_id);
        checkPoint.setPointid(0l);
        checkPoint.setName(collect_name);
        checkPoint.setTime(created_at);
        checkPoint.setReporter(user_name);
        checkPoint.setCheck(content);

        return checkPoint;
    }



    public String getCollect_id() {
        return collect_id;
    }

    public void setCollect_id(String collect_id) {
        this.collect_id = collect_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getCollect_name() {
        return collect_name;
    }

    public void setCollect_name(String collect_name) {
        this.collect_name = collect_name;
    }
}
