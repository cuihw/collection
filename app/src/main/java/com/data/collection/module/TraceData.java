package com.data.collection.module;

import com.data.collection.data.greendao.TraceLocation;

import java.util.List;

public class TraceData {
    private String date; //

    private List<TraceLocation> trace ;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<TraceLocation> getTrace() {
        return trace;
    }

    public void setTrace(List<TraceLocation> trace) {
        this.trace = trace;
    }
}
