package com.data.collection.module;

import com.google.gson.Gson;

import java.util.List;

public class Attrs {
    public static final String TYPE_TEXT = "1";
    public static final String TYPE_OPTION = "2";
    public static final String TYPE_NUMBERIC = "3";

    private String id;

    private String label;

    private List<String> options ;
    // 属性默认值
    private String value;

    // types[].attrs[].required 	string 	是否为必填项
    private String required;

    // types[].attrs[].type 	string 	属性类型 1-输入框 文本；2-下拉框 ；3- 输入框 数字
    private String type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }


}
