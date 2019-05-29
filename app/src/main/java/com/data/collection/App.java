package com.data.collection;

import android.app.Application;
import android.text.TextUtils;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.data.collection.data.BaiduTrace;
import com.data.collection.data.CacheData;
import com.data.collection.listener.IListenerUserInfo;
import com.data.collection.module.LoginBean;
import com.data.collection.module.UserInfoBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.LsLog;
import com.data.collection.util.PreferencesUtils;
import com.data.collection.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class App extends Application {


    private static final String TAG = "App ... ";

    private static App instence;

    public static App getInstence() {
        return instence;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Fabric.with(this, new Crashlytics());
        instence = this;

        initBaiduSdk();

        initLogin();
        getUserInfoCache();
    }

    private void initLogin() {
        String loginStr = PreferencesUtils.getString(this, Constants.LOGIN);
        if (!TextUtils.isEmpty(loginStr)) {
            LoginBean loginBean = LoginBean.formJson(loginStr, LoginBean.class);
            CacheData.LOGIN_DATA = loginBean.getData();
            refreshToken();
        }

    }

    public UserInfoBean getUserInfoCache() {
        String userInfoStr = PreferencesUtils.getString(this, Constants.USER_INFO);
        if (!TextUtils.isEmpty(userInfoStr)) {
            UserInfoBean userInfoBean = UserInfoBean.formJson(userInfoStr, UserInfoBean.class);
            CacheData.userInfoBean = userInfoBean;
            // 请求新的
            getUserInfo(null);
            return userInfoBean;
        }
        return null;
    }

    private void refreshToken() {
        if (CacheData.isLogin()) {
            String expired_at = CacheData.LOGIN_DATA.getExpired_at();
            long expired = Long.parseLong(expired_at) * 1000;
            if (System.currentTimeMillis() < expired)  {
                // 还没有过期，刷新token
                Map<String, Object> param = new HashMap<>();
                param.put("token",CacheData.LOGIN_DATA.getToken());
                HttpRequest.postData(Constants.REFRESH_TOKEN, param, new HttpRequest.RespListener<LoginBean>() {
                    @Override
                    public void onResponse(int status, LoginBean bean) {
                        if (status == 0) {
                            if (Constants.SUCCEED.equals(bean.getCode())) {
                                bean.cacheData(getApplicationContext());
                            }
                        }
                    }
                });
            }
        }
    }

    private void initBaiduSdk() {
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(getApplicationContext());
        // 自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        // 包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。CoordinateType.WGS84
        SDKInitializer.setCoordType(CoordType.BD09LL);

        BaiduTrace.getInstance().init(this);

    }

    private void showSignInfo() {
        String signInfo = Utils.getSignInfo(getApplicationContext(), getPackageName(), Utils.MD5);
        LsLog.i(TAG, "MD5 signInfo = " + signInfo);
        signInfo = Utils.getSignInfo(getApplicationContext(), getPackageName(), Utils.SHA1);
        LsLog.i(TAG, "SHA1 signInfo = " + signInfo);
    }

    public void getUserInfo(IListenerUserInfo listenerUserInfo) {
        if (CacheData.isLogin()){
            HttpRequest.postData( Constants.USER_INFO,null,  new HttpRequest.RespListener<UserInfoBean>() {
                @Override
                public void onResponse(int status, UserInfoBean bean) {
                    saveUserInfo(bean);
                    if (listenerUserInfo != null) {
                        listenerUserInfo.onUserInfoChange(bean);
                    }
                }
            });
        } else {
            listenerUserInfo.onUserInfoChange(null);
        }
    }

    private void saveUserInfo(UserInfoBean bean) {
        CacheData.userInfoBean = bean;
        PreferencesUtils.putString(this, Constants.USER_INFO, bean.toJson());
    }
}
