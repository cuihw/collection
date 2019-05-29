package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.module.LoginBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.LsLog;
import com.data.collection.util.PackageUtils;
import com.data.collection.util.ToastUtil;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    @BindView(R.id.login_button)
    TextView loginButton;

    @BindView(R.id.password_edit)
    EditText passwordEdit;

    @BindView(R.id.username_edit)
    EditText usernameEdit;

    @BindView(R.id.version_info)
    TextView versionInfo;

    public static void start(Context context){
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initListener();
    }

    private void initView() {

        String versionName = PackageUtils.getVersionName(this);
        versionInfo.setText("众望科技\n版本号：" + versionName);
    }

    private void initListener() {
        // 登录
        loginButton.setOnClickListener(v->{
            startLogin();
        });
    }

    private void startLogin() {
        String password = passwordEdit.getText().toString();
        String username = usernameEdit.getText().toString();
        if (TextUtils.isEmpty(username)) {
            ToastUtil.showTextToast(this, "请输入用户名");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            ToastUtil.showTextToast(this, "请输入密码");
            return;
        }

        Map<String, Object> param = new HashMap<>();
        param.put("username", username);
        param.put("password", password);

        HttpRequest.postData(this, Constants.LOGIN, param, new HttpRequest.RespListener<LoginBean>() {
            @Override
            public void onResponse(int status, LoginBean loginBean) {
                if (loginBean == null) {
                    return;
                }
                LsLog.i(TAG, "loginBean = " + loginBean.toJson());
                handleLogin(loginBean);
            }
        });

    }

    private void handleLogin(LoginBean loginBean) {
        if (loginBean.getCode().equals(Constants.SUCCEED)) {
            loginBean.cacheData(this);
            ToastUtil.showTextToast(this, "登录成功!");
            App.getInstence().getUserInfo(null);
        } else {
            loginBean.cacheData(this);
            ToastUtil.showTextToast(this, "登录失败!");
        }
        finish();
    }


}
