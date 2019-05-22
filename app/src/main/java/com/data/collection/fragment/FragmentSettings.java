package com.data.collection.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.classic.adapter.BaseAdapterHelper;
import com.classic.adapter.CommonAdapter;
import com.data.collection.R;
import com.data.collection.activity.LoginActivity;
import com.data.collection.activity.NaviActivity;
import com.data.collection.data.CacheData;
import com.data.collection.module.DataPoint;
import com.data.collection.test.LocationData;
import com.data.collection.util.FileUtils;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.data.collection.util.NormalUtils;
import com.data.collection.util.PackageUtils;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentSettings#} factory method to
 * create an instance of this fragment.
 */
public class FragmentSettings extends FragmentBase {
    private static final String TAG = "FragmentSettings";

    @BindView(R.id.login_button)
    TextView loginButton;

    @BindView(R.id.version_info)
    TextView versionInfo;
    @BindView(R.id.username)
    TextView username;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.fragment_home_setting, container, false);
        bindButterKnife();

        initView();
        initListener();
        return view;
    }

    private void initListener() {
        loginButton.setOnClickListener(v->{
            clickLoginButton();
        });
    }

    private void clickLoginButton() {
        String text = loginButton.getText().toString();
        if (text.equals("登录")) { // 跳转到登录页面，
            LoginActivity.start(getContext(),null);
        } else { // 已经登录，退出登录
            loginButton.setText("登录");
        }
    }

    private void initView() {
        String versionName = PackageUtils.getVersionName(getContext());

        versionInfo.setText("版本号： " + versionName);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (CacheData.isLogin()) {
            loginButton.setText("退出登录");
        } else {
            username.setText("用户未登录");
            loginButton.setText("登录");
        }
    }
}
