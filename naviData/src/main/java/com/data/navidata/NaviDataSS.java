package com.data.navidata;



import java.io.Serializable;

public class NaviDataSS implements Serializable {

    LocaltionData startNode;

    LocaltionData endNode;

    public LocaltionData getStartNode() {
        return startNode;
    }

    public void setStartNode(LocaltionData startNode) {
        this.startNode = startNode;
    }

    public LocaltionData getEndNode() {
        return endNode;
    }

    public void setEndNode(LocaltionData endNode) {
        this.endNode = endNode;
    }
}
