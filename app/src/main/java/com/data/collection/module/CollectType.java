package com.data.collection.module;

import com.google.gson.Gson;

import java.util.List;

// 采集类型
public class CollectType {
    private String id;

    private String name;

    private String icon;

    private int index;

    private List<Attrs> attrs ;

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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<Attrs> getAttrs() {
        return attrs;
    }

    public void setAttrs(List<Attrs> attrs) {
        this.attrs = attrs;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
