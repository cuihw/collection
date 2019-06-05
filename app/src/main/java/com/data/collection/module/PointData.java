package com.data.collection.module;

import com.data.collection.data.greendao.GatherPoint;
import com.google.gson.Gson;

import java.util.List;

public class PointData {
    private String id;

    private String type_id;

    private String name;

    private String height;

    private String longitude;

    private String latitude;

    private String collected_at;

    private String updated_at;

    private String desc;

    private String uname;


    private List<ImageData.FileMap> imgs ;

    private List<Attrs> attrs ;

    private CollectType type;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
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

    public String getCollected_at() {
        return collected_at;
    }

    public void setCollected_at(String collected_at) {
        this.collected_at = collected_at;
    }

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }

    public List<ImageData.FileMap> getImgs() {
        return imgs;
    }

    public void setImgs(List<ImageData.FileMap> imgs) {
        this.imgs = imgs;
    }

    public List<Attrs> getAttrs() {
        return attrs;
    }

    public void setAttrs(List<Attrs> attrs) {
        this.attrs = attrs;
    }

    public CollectType getType() {
        return type;
    }

    public void setType(CollectType type) {
        this.type = type;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public GatherPoint getGatherPoint(){
        GatherPoint gp = new GatherPoint();
        gp.setId(id);
        gp.setType_id(type_id);

        gp.setUpdated_at(updated_at);
        Gson gson = new Gson();
        gp.setImgs(gson.toJson(imgs));
        gp.setAttrs(gson.toJson(attrs));

        gp.setIsUploaded(true);
        gp.setCollected_at(collected_at);
        gp.setHeight(height);
        gp.setLatitude(latitude);
        gp.setLongitude(longitude);
        gp.setName(name);
        gp.setDesc(desc);
        gp.setReport(uname);
        return gp;
    }

}
