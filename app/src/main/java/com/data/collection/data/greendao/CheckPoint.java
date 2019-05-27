package com.data.collection.data.greendao;

import com.data.collection.util.LsLog;
import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@Entity
public class CheckPoint {

    private static final String TAG = "CheckPoint";
    @Id
    private Long id;

    @NotNull
    private Long pointid; // 检查点的ID,  根据ID可以获得坐标和属性等相关信息。

    @Unique
    private String time;// 检查时间 格式 2019-06-05 12:30:65

    private String reporter; // 上报人

    private String  check; // 检查内容

    @Generated(hash = 363647605)
    public CheckPoint(Long id, @NotNull Long pointid, String time, String reporter,
            String check) {
        this.id = id;
        this.pointid = pointid;
        this.time = time;
        this.reporter = reporter;
        this.check = check;
    }

    @Generated(hash = 1441852858)
    public CheckPoint() {
    }

    public String toJson(){
        return new Gson().toJson(this);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPointid() {
        return this.pointid;
    }

    public void setPointid(Long pointid) {
        this.pointid = pointid;
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

}
