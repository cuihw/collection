package com.data.collection.listener;

import com.data.collection.data.greendao.GatherPoint;

import java.util.List;

public interface IGatherDataListener {
    void onListData(List<GatherPoint> list);
}
