package com.data.collection.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.activity.LoginActivity;
import com.data.collection.data.CacheData;
import com.data.collection.util.LsLog;
import com.data.collection.util.PackageUtils;

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


    @BindView(R.id.tools_page)
    LinearLayout toolsPage;




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

        toolsPage.setOnClickListener(v->showFragment());
    }

    private void showFragment() {
        FragmentTools.start(getContext());
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
