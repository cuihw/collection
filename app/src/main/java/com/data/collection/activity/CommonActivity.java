package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.util.PackageUtils;
import com.data.collection.util.ToastUtil;

import java.util.List;

import butterknife.BindView;

public class CommonActivity extends BaseActivity {

    private static final String TAG = "CommonActivity";

    public static void start(Context context, Bundle bundle){
        Intent intent = new Intent(context, CommonActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);
    }

    public void tranToFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }


}
