package com.data.collection.data;

import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.data.greendao.CheckPointDao;
import com.data.collection.data.greendao.DaoSession;
import com.data.collection.data.greendao.GatherPointDao;
import com.data.collection.data.greendao.MessageDataDao;
import com.data.collection.module.LoginData;
import com.data.collection.module.Project;
import com.data.collection.module.CollectType;
import com.data.collection.module.UserData;
import com.data.collection.module.UserInfo;
import com.data.collection.module.UserInfoBean;
import com.data.collection.util.PreferencesUtils;
import com.data.collection.util.ToastUtil;
import com.data.collection.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheData {

    public static LoginData LOGIN_DATA = null;
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
                    for (int i = 0; i < types.size(); i ++ ) {
                        CollectType collectType = types.get(i);
                        collectType.setIndex(i);
                        Utils.cacheImage(collectType.getIcon());
                        TypeMaps.put(collectType.getId(), collectType);
                    }
                }
            }
        }
    }

    public static boolean isValidProject(){
        if (userInfoBean == null) {
            return false;
        }

        UserData data = userInfoBean.getData();
        if (data == null) {
            return false;
        }
        UserInfo user = data.getUser();
        if (user == null) {
            return false;
        }
        String name = user.getName();
        if (name == null) {
            return false;
        }

        Project project = data.getProject();
        if (project == null) {
            return false;
        }
        List<CollectType> types = project.getTypes();
        if (types == null || types.size() == 0) {
            return false;
        }
        return true;
    }


    public static void clearProject() {
        userInfoBean = null;
        TypeMaps.clear();
        PreferencesUtils.putString(App.getInstence(), Constants.USER_INFO, "");
        PreferencesUtils.putString(App.getInstence(), Constants.LOGIN, "");
        DaoSession daoSession = App.getInstence().getDaoSession();
        CheckPointDao cpDao = daoSession.getCheckPointDao();
        cpDao.deleteAll();

        GatherPointDao gpDao = daoSession.getGatherPointDao();
        gpDao.deleteAll();

        MessageDataDao mdDao = daoSession.getMessageDataDao();
        mdDao.deleteAll();
    }
}
