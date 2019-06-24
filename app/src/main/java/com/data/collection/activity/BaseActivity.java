package com.data.collection.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.data.collection.R;
import com.data.collection.util.LsLog;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    private Unbinder unbinder;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initButterKnife();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    public void initButterKnife(){
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroy();
    }

    public void tranToFragment(Fragment fragment) {
        LsLog.w(TAG, "tranToFragment = " +  fragment.getClass().getSimpleName());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        for (Fragment item: fragments){
            if (fragment != item) {
                transaction.hide(item);
            }
        }
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.container, fragment);
        }
        transaction.commit();
    }
}
