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
public class MessageRecord {

    private static final String TAG = "MessageRecord";
    @Id
    private Long id;

    @Unique
    private String time;// 采集时间 格式 2019-06-05 12:30:65
    String from;

    String to;

    String title;
    String content;
    boolean isRead = false;

    @Generated(hash = 1623281736)
    public MessageRecord(Long id, String time, String from, String to, String title,
            String content, boolean isRead) {
        this.id = id;
        this.time = time;
        this.from = from;
        this.to = to;
        this.title = title;
        this.content = content;
        this.isRead = isRead;
    }

    @Generated(hash = 772980815)
    public MessageRecord() {
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

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean getIsRead() {
        return this.isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }


}
