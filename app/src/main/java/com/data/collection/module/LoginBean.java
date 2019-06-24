package com.data.collection.module;

import android.content.Context;

import com.data.collection.Constants;
import com.data.collection.data.CacheData;
import com.data.collection.util.DateUtils;
import com.data.collection.util.PreferencesUtils;

public class LoginBean extends Bean {
    LoginData data;

    public LoginData getData() {
        return data;
    }

    public void setData(LoginData data) {
        this.data = data;
    }

    public void cacheData(Context context) {
        if (Constants.SUCCEED.equals(code)) {
            CacheData.LOGIN_DATA = data;
            PreferencesUtils.putString(context, Constants.LOGIN, toJson());
        } else {
            CacheData.LOGIN_DATA = null;
            PreferencesUtils.putString(context, Constants.LOGIN, "");
        }
    }
}
