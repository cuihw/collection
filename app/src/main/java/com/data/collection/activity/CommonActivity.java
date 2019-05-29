package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.data.collection.R;
import com.data.collection.fragment.FragmentCompass;
import com.data.collection.fragment.FragmentGpsInfo;
import com.data.collection.fragment.FragmentOffline;
import com.data.collection.fragment.FragmentPositionTransform;
import com.data.collection.fragment.FragmentProject;
import com.data.collection.fragment.FragmentTools;
import com.data.collection.fragment.FragmentTrace;

public class CommonActivity extends BaseActivity {

    public static final String FRAGMENT = "fragment";
    public static final int FRAGMENT_TOOLS = 1;
    public static final int FRAGMENT_COMPASS = 2;
    public static final int FRAGMENT_GPS = 3;
    public static final int FRAGMENT_OFFLINE = 4;
    public static final int FRAGMENT_POSITION = 5;
    public static final int FRAGMENT_TRACE = 6;
    public static final int FRAGMENT_PROJECT = 7;
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

        Bundle extras = getIntent().getExtras();
        int fragment = extras.getInt(FRAGMENT);
        switch (fragment) {
            case FRAGMENT_TOOLS:
                tranToFragment(FragmentTools.getInstance());
                break;
            case FRAGMENT_COMPASS:
                tranToFragment(FragmentCompass.getInstance());
                break;
            case FRAGMENT_GPS:
                tranToFragment(FragmentGpsInfo.getInstance());
                break;
            case FRAGMENT_OFFLINE:
                tranToFragment(FragmentOffline.getInstance());
                break;
            case FRAGMENT_POSITION:
                tranToFragment(FragmentPositionTransform.getInstance());
                break;
            case FRAGMENT_TRACE:
                tranToFragment(FragmentTrace.getInstance());
                break;
            case FRAGMENT_PROJECT:
                tranToFragment(FragmentProject.getInstance());
                break;

        }
    }

    public void tranToFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }


}
