package com.data.collection.data.greendao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class MessageData {
    @Id
    private String id;

    private String title;

    private String content;

    private String type;

    private String read_at;

    private String create_time;

    private String pusher;

    boolean isUploaded = false;



    @Generated(hash = 692796271)
    public MessageData(String id, String title, String content, String type,
            String read_at, String create_time, String pusher, boolean isUploaded) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.type = type;
        this.read_at = read_at;
        this.create_time = create_time;
        this.pusher = pusher;
        this.isUploaded = isUploaded;
    }

    @Generated(hash = 723026249)
    public MessageData() {
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRead_at() {
        return read_at;
    }

    public void setRead_at(String read_at) {
        this.read_at = read_at;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getPusher() {
        return pusher;
    }

    public void setPusher(String pusher) {
        this.pusher = pusher;
    }

    public boolean getIsUploaded() {
        return this.isUploaded;
    }

    public void setIsUploaded(boolean isUploaded) {
        this.isUploaded = isUploaded;
    }
}
