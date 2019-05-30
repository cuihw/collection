package com.data.collection.module;

import com.data.collection.data.greendao.MessageData;

import java.util.List;

public class MessageDatas {
    private List<MessageData> data ;

    private String total;

    private String per_page;

    private String current_page;

    public List<MessageData> getData() {
        return data;
    }

    public void setData(List<MessageData> data) {
        this.data = data;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getPer_page() {
        return per_page;
    }

    public void setPer_page(String per_page) {
        this.per_page = per_page;
    }

    public String getCurrent_page() {
        return current_page;
    }

    public void setCurrent_page(String current_page) {
        this.current_page = current_page;
    }
}
