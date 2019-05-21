package com.data.collection.module;

import com.data.collection.util.DateUtils;
import com.google.gson.Gson;

public class Bean {

    public String toJson(){
        return new Gson().toJson(this);
    }

    public <T> T formJson(String json , Class<T> cls){
        T t = new Gson().fromJson(json, cls);
        return t;
    }

    public <T> T formJson(String json , String clsName){
        Class<T> cls = null;
        try {
            cls = (Class<T>) Class.forName(clsName);
            T t = new Gson().fromJson(json, cls);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
