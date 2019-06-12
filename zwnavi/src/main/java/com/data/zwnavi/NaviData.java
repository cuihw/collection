package com.data.zwnavi;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.List;

public class NaviData {
    private String id;

    private String pid;

    private String tname;

    private List<NaviData> children ;

    private List<NaviData> sites ;

    private String name;

    private String section_id;

    private String longitude;

    private String latitude;

    private boolean isSelected;

    public boolean isSiteData(){
        return TextUtils.isEmpty(tname);
    }

    public List<NaviData> getSites() {
        return sites;
    }

    public void setSites(List<NaviData> sites) {
        this.sites = sites;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSection_id() {
        return section_id;
    }

    public void setSection_id(String section_id) {
        this.section_id = section_id;
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getTname() {
        return tname;
    }

    public void setTname(String tname) {
        this.tname = tname;
    }

    public List<NaviData> getChildren() {
        return children;
    }

    public void setChildren(List<NaviData> children) {
        this.children = children;
    }

    public String toJson(){
        return new Gson().toJson(this);
    }

    public static NaviData fromJson(String json){
        return TextUtils.isEmpty(json) ? null: new Gson().fromJson(json, NaviData.class);
    }
}
