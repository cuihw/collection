package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.util.ToastUtil;

import org.w3c.dom.Text;

import butterknife.BindView;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    @BindView(R.id.login_button)
    TextView loginButton;

    @BindView(R.id.password_edit)
    EditText passwordEdit;

    @BindView(R.id.username_edit)
    EditText usernameEdit;

    public static void start(Context context, Bundle bundle){
        Intent intent = new Intent(context, LoginActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initListener();
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
    }


}
