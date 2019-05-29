package com.data.collection.listener;

import com.data.collection.module.UserInfo;
import com.data.collection.module.UserInfoBean;

public interface IListenerUserInfo {
    void onUserInfoChange(UserInfoBean userInfoBean);
}
