package com.data.collection;

import android.app.Application;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.data.collection.data.UserTrace;
import com.data.collection.data.CacheData;
import com.data.collection.data.greendao.DaoMaster;
import com.data.collection.data.greendao.DaoSession;
import com.data.collection.data.greendao.MyOpenDaoHelper;
import com.data.collection.listener.IListenerUserInfo;
import com.data.collection.module.LoginBean;
import com.data.collection.module.UserInfoBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.CrashHandler;
import com.data.collection.util.LsLog;
import com.data.collection.util.PreferencesUtils;
import com.data.collection.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.greenrobot.greendao.database.Database;
import org.osmdroid.config.Configuration;

import java.util.HashMap;
import java.util.Map;

public class App extends Application {


    private static final String TAG = "App ... ";

    private static App instence;

    DaoSession daoSession;

    public static App getInstence() {
        return instence;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Fabric.with(this, new Crashlytics());
        instence = this;

        initDaoSession();

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        initMapsdk();
        initLogin();
        getUserInfoCache();
        CrashHandler.getInstance().init(getApplicationContext());
        getPersonalSettings();
    }

    private void getPersonalSettings() {
        boolean dms = PreferencesUtils.getBoolean(this, Constants.DEGREE_MIN_SENCOND, true);
        CacheData.setDMS(dms);
    }

    private void initDaoSession() {
        MyOpenDaoHelper helper = new MyOpenDaoHelper(this, "info-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
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
        UserInfoBean userInfoBean = null;
        if (!TextUtils.isEmpty(userInfoStr)) {
            LsLog.i(TAG, "userInfoStr = " + userInfoStr);
            try {
                userInfoBean = UserInfoBean.formJson(userInfoStr, UserInfoBean.class);
                CacheData.setUserInfoBean(userInfoBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 请求新的
        getUserInfo(null);
        return userInfoBean;
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

    private void initMapsdk() {
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(getApplicationContext());
        // 自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        // 包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。CoordinateType.WGS84
        SDKInitializer.setCoordType(CoordType.BD09LL);

        UserTrace.getInstance().init(this);

        Configuration.getInstance().setAnimationSpeedDefault(500);
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
    }

    private void showSignInfo() {
        String signInfo = Utils.getSignInfo(getApplicationContext(), getPackageName(), Utils.MD5);
        LsLog.i(TAG, "MD5 signInfo = " + signInfo);
        signInfo = Utils.getSignInfo(getApplicationContext(), getPackageName(), Utils.SHA1);
        LsLog.i(TAG, "SHA1 signInfo = " + signInfo);
    }

    public void getUserInfo(IListenerUserInfo listenerUserInfo) {
        if (CacheData.isLogin()){
            //http://192.168.1.201/collect/index.php/app/v1/getUserInfo
            HttpRequest.postData(Constants.USER_INFO,null,  new HttpRequest.RespListener<UserInfoBean>() {
                @Override
                public void onResponse(int status, UserInfoBean bean) {
                    if (status == 0) {
                        if (bean.getCode().equals(Constants.SUCCEED)) {
                            UserInfoBean userInfoBean = CacheData.getUserInfoBean();
                            String local = "";
                            if (userInfoBean != null) {
                                local = userInfoBean.getData().getProject().getId();
                            }

                            String id = bean.getData().getProject().getId();
                            if (!id.equals(local)) {
                                // TODO： 删除数据库里面的项目数据。
                                Log.w(TAG, "new project id ,delete old project data.");
                                if (daoSession != null) {
                                    daoSession.getGatherPointDao().deleteAll();
                                    daoSession.getCheckPointDao().deleteAll();
                                }
                            }
                            saveUserInfo(bean);
                            if (listenerUserInfo != null) {
                                listenerUserInfo.onUserInfoChange(bean);
                            }
                        }
                    }
                }
            });
        }
    }

    private void saveUserInfo(UserInfoBean bean) {
        if (bean == null) return;
        Log.w(TAG, "saveUserInfo = " + bean.toJson());
        PreferencesUtils.putString(this, Constants.USER_INFO, bean.toJson());
        CacheData.setUserInfoBean(bean);
    }
}
