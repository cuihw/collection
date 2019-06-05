package com.data.collection.listener;

import com.data.collection.data.greendao.TraceLocation;

import java.util.List;

public interface ITraceListener {
    void onTraceList(List<TraceLocation> list);
}
