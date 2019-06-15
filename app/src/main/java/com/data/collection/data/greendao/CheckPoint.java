package com.data.collection.data.greendao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class CheckPoint {

    private static final String TAG = "CheckPoint";

    @Id
    private Long id;

    @Unique
    private String OnlineId; // 检查内容的ID

    @NotNull
    private Long pointid; // 检查点的ID,  根据ID可以获得坐标和属性等相关信息， 本地ID。

    private String collectOnlineId; // 检查点的ID, 线上id HAO, 没有上传的时候，可能为空。

    private String name; // 检查点名字

    private String time;// 检查时间 格式 2019-06-05 12:30:65

    private String reporter; // 上报人

    private String  check; // 检查内容

    private boolean isUploaded;

    private String projectId; // 检查项所属项目id

    @Generated(hash = 2107125328)
    public CheckPoint(Long id, String OnlineId, @NotNull Long pointid,
            String collectOnlineId, String name, String time, String reporter,
            String check, boolean isUploaded, String projectId) {
        this.id = id;
        this.OnlineId = OnlineId;
        this.pointid = pointid;
        this.collectOnlineId = collectOnlineId;
        this.name = name;
        this.time = time;
        this.reporter = reporter;
        this.check = check;
        this.isUploaded = isUploaded;
        this.projectId = projectId;
    }

    @Generated(hash = 1441852858)
    public CheckPoint() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOnlineId() {
        return this.OnlineId;
    }

    public void setOnlineId(String OnlineId) {
        this.OnlineId = OnlineId;
    }

    public Long getPointid() {
        return this.pointid;
    }

    public void setPointid(Long pointid) {
        this.pointid = pointid;
    }

    public String getCollectOnlineId() {
        return this.collectOnlineId;
    }

    public void setCollectOnlineId(String collectOnlineId) {
        this.collectOnlineId = collectOnlineId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getReporter() {
        return this.reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getCheck() {
        return this.check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public boolean getIsUploaded() {
        return this.isUploaded;
    }

    public void setIsUploaded(boolean isUploaded) {
        this.isUploaded = isUploaded;
    }

    public String getProjectId() {
        return this.projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }


    
}
