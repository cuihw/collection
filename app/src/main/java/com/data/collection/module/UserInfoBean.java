package com.data.collection.module;

import com.data.collection.data.CacheData;

public class UserInfoBean  extends Bean {

    private UserData data;

    public UserData getData() {
        return data;
    }

    public void setData(UserData data) {
        this.data = data;
    }

    public static void clearCache(){
        CacheData.clearProject();
    }
}
