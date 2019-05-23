package com.data.collection.data;

public class BaiduOffline {

    boolean isInTrace;

    static BaiduOffline baiduTrace;

    public synchronized static BaiduOffline getInstance(){
        if (baiduTrace == null) {
            baiduTrace = new BaiduOffline();
        }
        return baiduTrace;
    }

    public void start() {
        isInTrace = true;
    }

    public void stop(){
        isInTrace = false;
    }


    public boolean isInTrace() {
        return isInTrace;
    }

    public void setInTrace(boolean inTrace) {
        isInTrace = inTrace;
    }

}
