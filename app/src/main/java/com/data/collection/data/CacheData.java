package com.data.collection.data;

import com.data.collection.module.LoginData;
import com.data.collection.module.UserInfoBean;

public class CacheData {

    public static UserInfoBean userInfoBean;

    public static boolean isLogin() {
        if (LOGIN_DATA != null) return true;
        return false;
    }

    public static LoginData LOGIN_DATA= null;

}
