package com.data.collection.data.greendao;

import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Sample {

    private static final String TAG = "CheckPoint";
    @Id
    private Long id;

    @NotNull
    private Long pointid; // 检查点的ID,  根据ID可以获得坐标和属性等相关信息。

    @Unique
    private String checkTime;// 采集时间 格式 2019-06-05 12:30:65

    private String  check; // 备注

    @Generated(hash = 775122181)
    public Sample(Long id, @NotNull Long pointid, String checkTime, String check) {
        this.id = id;
        this.pointid = pointid;
        this.checkTime = checkTime;
        this.check = check;
    }

    @Generated(hash = 976859954)
    public Sample() {
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

    public String getCheckTime() {
        return this.checkTime;
    }

    public void setCheckTime(String checkTime) {
        this.checkTime = checkTime;
    }

    public String getCheck() {
        return this.check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

}
