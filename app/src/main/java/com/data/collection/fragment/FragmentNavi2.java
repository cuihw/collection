package com.data.collection.fragment;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.activity.NaviCollectionListActivity;
import com.data.collection.activity.NaviListActivity;
import com.data.collection.data.CacheData;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentNavi2#} factory method to
 * create an instance of this fragment.
 */
public class FragmentNavi2 extends FragmentBase {
    private static final String TAG = "FragmentNavi2";

    @BindView(R.id.navi_list_layout)
    RelativeLayout naviListLayout;

    @BindView(R.id.localdata_list_layout)
    RelativeLayout localdataListLayout;

    @BindView(R.id.syncdata_list_layout)
    RelativeLayout syncdataListLayout;

    private static final String[] authBaseArr = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,   // 写内存
            Manifest.permission.ACCESS_FINE_LOCATION          // 精准定位
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.fragment_home_navi2, container, false);
        bindButterKnife();
        initListener();
        requestPermiss();
        return view;
    }

    private void requestPermiss() {
        final RxPermissions rxPermissions = new RxPermissions(this);

        for (String permission: authBaseArr ) {
            rxPermissions.request(permission).subscribe(
                    granted -> {
                        if (!granted) { // Always true pre-M   deny
                            LsLog.w(TAG, "request permission denied.");
                        }
                    }
            );
        }
    }


    private void initListener() {
        naviListLayout.setOnClickListener(v->NaviListActivity.start(getContext()));
        localdataListLayout.setOnClickListener(v-> {
            if (CacheData.isLogin()) {
                NaviCollectionListActivity.start(getContext(),true);
            } else {
                ToastUtil.showTextToast(getContext(), Constants.NO_PROJECT_INFO);
            }
        });
        syncdataListLayout.setOnClickListener(v-> {
            if (CacheData.isLogin()) {
                NaviCollectionListActivity.start(getContext(), false);
            } else {
                ToastUtil.showTextToast(getContext(), Constants.NO_PROJECT_INFO);
            }
        });
    }

}
