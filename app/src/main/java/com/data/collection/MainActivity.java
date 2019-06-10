package com.data.collection;

import android.os.Bundle;
import android.widget.Toast;

import com.data.collection.activity.BaseActivity;
import com.data.collection.activity.LoginActivity;
import com.data.collection.data.UserTrace;
import com.data.collection.data.CacheData;
import com.data.collection.fragment.FragmentCheckRecord;
import com.data.collection.fragment.FragmentHome;
import com.data.collection.fragment.FragmentNavi;
import com.data.collection.fragment.FragmentNavi2;
import com.data.collection.fragment.FragmentSettings;
import com.data.collection.service.UpLocationTask;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.jpeng.jptabbar.JPTabBar;
import com.jpeng.jptabbar.OnTabSelectListener;

import butterknife.BindString;
import butterknife.BindView;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    @BindView(R.id.tabbar)
    JPTabBar jpTabBar;

    @BindString(R.string.collect)
    String collect;

    @BindString(R.string.navi)
    String navi;

    @BindString(R.string.record)
    String record;

    @BindString(R.string.settings)
    String settings;

    FragmentHome fragmentHome = new FragmentHome();
    FragmentNavi2 fragmentNavi = new FragmentNavi2();
    FragmentCheckRecord fragmentCheckRecord = new FragmentCheckRecord();
    FragmentSettings fragmentSettings = new FragmentSettings();

    private long firstExitTime = 0L;// 用来保存第一次按返回键的时间
    private static final int EXIT_TIME = 2000;// 两次按返回键的间隔判断
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tranToFragment(fragmentHome);
        initTab();
        initLisenter();
        // start location
        LocationController.getInstance().startLocation(this);
        if (!CacheData.isLogin()) {
            LoginActivity.start(this);
        }

        UpLocationTask.getInstance().startUpload();
    }

    private void initLisenter() {
        jpTabBar.setTabListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int index) {
                switch (index) {
                    case 0:
                        // 采集主页
                        tranToFragment(fragmentHome);
                        break;
                    case 1:
                        // 导航
                        tranToFragment(fragmentNavi);
                        break;
                    case 2:
                        // 检查功能
                        tranToFragment(fragmentCheckRecord);
                        break;
                    case 3:
                        // 设置中心
                        tranToFragment(fragmentSettings);
                        break;
                    default:
                        LsLog.i(TAG, "onTabSelect index = " + index);
                }
            }

            @Override
            public boolean onInterruptSelect(int index) {
                return false;
            }
        });
    }

    private void initTab() {
        setTitle("导航");
        jpTabBar.setTitles(collect, navi, record, settings)
                .setNormalIcons(R.mipmap.tab_home, R.mipmap.tab_navigation, R.mipmap.tab_check, R.mipmap.tab_setting)
                .setSelectedIcons(R.mipmap.tab_home_selected,
                        R.mipmap.tab_navigation_selected,
                        R.mipmap.tab_check_selected,
                        R.mipmap.tab_setting_selected)
                .generate();
    }

    @Override
    protected void onDestroy() {
        LocationController.getInstance().stopLocation();

        UserTrace.getInstance().stop();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        if (curTime - firstExitTime < EXIT_TIME) {
            finish();
        } else {
            Toast.makeText(this, R.string.exit_toast, Toast.LENGTH_SHORT).show();
            firstExitTime = curTime;
        }
    }



}
