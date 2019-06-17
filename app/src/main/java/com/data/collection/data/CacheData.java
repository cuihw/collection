package com.data.collection.data;

import com.data.collection.App;
import com.data.collection.module.LoginData;
import com.data.collection.module.Project;
import com.data.collection.module.CollectType;
import com.data.collection.module.UserData;
import com.data.collection.module.UserInfo;
import com.data.collection.module.UserInfoBean;
import com.data.collection.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheData {

    public static LoginData LOGIN_DATA= null;
    private static UserInfoBean userInfoBean;
    private static Map<String, CollectType> TypeMaps = new HashMap<>();

    public static Map<String, CollectType> getTypeMaps() {
        return TypeMaps;
    }

    public static UserInfoBean getUserInfoBean() {
        if (userInfoBean == null) {
            App.getInstence().getUserInfoCache();
        }
        return userInfoBean;
    }

    public static String getUserName(){
        if (userInfoBean != null) {
            UserData data = userInfoBean.getData();
            if (data != null) {
                UserInfo user = data.getUser();
                return user== null ? null : user.getName();
            }
        }
        return null;
    }

    public static void setUserInfoBean(UserInfoBean userBean){
        userInfoBean = userBean;
        cacheProjectIcon();
    }

    public static boolean isLogin() {
        if (LOGIN_DATA != null) return true;
        return false;
    }

    public static void cacheProjectIcon(){
        if (userInfoBean != null) {
            Project project = userInfoBean.getData()==null ? null : userInfoBean.getData().getProject();
            if (project != null) {
                List<CollectType> types = project.getTypes();
                if (types != null) {
                    for (CollectType item: types) {
                        Utils.cacheImage(item.getIcon());
                        TypeMaps.put(item.getId(), item);
                    }
                }
            }
        }
    }


}
