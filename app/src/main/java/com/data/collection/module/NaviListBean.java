package com.data.collection.module;

import java.util.List;

// 导航点
public class NaviListBean extends Bean {
    List<NaviData> data;

    public List<NaviData> getData() {
        return data;
    }

    public void setData(List<NaviData> data) {
        this.data = data;
    }
}
