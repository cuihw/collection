package com.data.collection.data;

public class BaiduTrace {

    boolean isInTrace;

    static BaiduTrace baiduTrace;

    public synchronized static BaiduTrace getInstance(){
        if (baiduTrace == null) {
            baiduTrace = new BaiduTrace();
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