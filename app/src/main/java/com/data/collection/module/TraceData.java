package com.data.collection.module;

import com.data.collection.data.greendao.TraceLocation;

import java.util.List;

public class TraceData {
    private String date; //

    private List<TraceLocation> traceList ;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<TraceLocation> getTraceList() {
        return traceList;
    }

    public void setTraceList(List<TraceLocation> traceList) {
        this.traceList = traceList;
    }
}
