package com.data.collection.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.MainActivity;
import com.data.collection.R;
import com.data.collection.activity.LoginActivity;
import com.data.collection.data.CacheData;
import com.data.collection.data.greendao.CheckPoint;
import com.data.collection.data.greendao.CheckPointDao;
import com.data.collection.data.greendao.DaoSession;
import com.data.collection.data.greendao.GatherPointDao;
import com.data.collection.data.greendao.MessageData;
import com.data.collection.data.greendao.MessageDataDao;
import com.data.collection.module.BaseBean;
import com.data.collection.module.UserInfoBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.LsLog;
import com.data.collection.util.PackageUtils;
import com.data.collection.util.ToastUtil;

import org.greenrobot.greendao.query.QueryBuilder;
import org.json.JSONException;
import org.json.JSONObject;

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

    @BindView(R.id.unread_count)
    TextView unread_count;

    @BindView(R.id.tools_page)
    LinearLayout toolsPage;

    @BindView(R.id.trace_layout)
    LinearLayout traceLayout;

    @BindView(R.id.project_info)
    LinearLayout projectInfo;

    @BindView(R.id.message_center)
    LinearLayout messageCenter;

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

        toolsPage.setOnClickListener(v->FragmentTools.start(getContext()));
        traceLayout.setOnClickListener(v->FragmentTrace.start(getContext()));
        projectInfo.setOnClickListener(v->FragmentProject.start(getContext()));
        messageCenter.setOnClickListener(v->FragmentMessage.start(getContext()));
    }

    private void clickLoginButton() {
        String text = loginButton.getText().toString();
        if (text.equals("登录")) { // 跳转到登录页面，
//            LoginActivity.start(getContext());
            LoginActivity.startForResult(getActivity(), MainActivity.LOGIN_REQUEST);
        } else { // 已经登录，退出登录
            logOut();
        }
    }

    private void logOut() {
        CacheData.clearProject();
        username.setText("用户未登录");
        loginButton.setText("登录");
    }

    private void initView() {
        String versionName = PackageUtils.getVersionName(getContext());
        versionInfo.setText("版本号： " + versionName);

        HttpRequest.postData(Constants.GET_UNREAD_MSG,null, new HttpRequest.RespListener<BaseBean>() {

            @Override
            public void onResponse(int status, BaseBean bean) {
                if (status != 0) {
                    // unread_count.setText("未读消息：0");
                    return ;
                }
                String data = bean.getData().toString();
                LsLog.i(TAG, "read data = " + data);
                try {
                    JSONObject object = new JSONObject(data);
                    String count = object.getString("count");

                    unread_count.setText("未读消息：" + count);
                } catch (JSONException e) {
                    e.printStackTrace();
                    unread_count.setText("");
                }

            }
        });
        getUnreadCountDB();

    }
    long unreadCount;
    private void getUnreadCountDB() {
        DaoSession daoSession =  App.getInstence().getDaoSession();

        QueryBuilder<MessageData> qb = daoSession.queryBuilder(MessageData.class)
                .where(MessageDataDao.Properties.Type.eq("0"))
                .orderDesc(MessageDataDao.Properties.Create_time);

        unreadCount = qb.count();// 查出当前未读
        // TODO： 数量加黑
        unread_count.setText("未读消息：" + unreadCount);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (CacheData.isLogin()) {
            loginButton.setText("退出登录");
        } else {
            username.setText("用户未登录");
            loginButton.setText("登录");
            return;
        }

        if (CacheData.isValidProject()) {
            UserInfoBean userInfoBean = CacheData.getUserInfoBean();
            String name = userInfoBean.getData().getUser().getName();
            username.setText("用户名： " + name);
        } else {
            username.postDelayed(new Runnable() {
                @Override
                public void run() {
                    UserInfoBean userInfoBean = CacheData.getUserInfoBean();
                    String name = userInfoBean.getData().getUser().getName();
                    username.setText("用户名： " + name);
                }
            }, 1000);
        }

    }

    // 同步数据：1. 采集点   2. 兴趣点， 3 trace轨迹， 4. 项目内容，包含项目类型的图标。 5. 所在省份的离线地图
}
