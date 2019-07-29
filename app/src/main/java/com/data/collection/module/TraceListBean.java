package com.data.collection.module;

import java.util.List;

public class TraceListBean extends Bean {
    List<TraceData>  data;

    public List<TraceData> getData() {
        return data;
    }

    public void setData(List<TraceData> data) {
        this.data = data;
    }
}
