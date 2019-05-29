package com.data.collection.module;

import com.google.gson.Gson;

public class Bean {

    String code;
    String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String toJson(){
        return new Gson().toJson(this);
    }

    public static  <T> T formJson(String json , Class<T> cls){
        T t = new Gson().fromJson(json, cls);
        return t;
    }

    public static <T> T formJson(String json , String clsName){
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
