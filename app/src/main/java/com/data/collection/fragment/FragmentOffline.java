package com.data.collection.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.data.collection.R;
import com.data.collection.activity.CommonActivity;
import com.data.collection.util.LsLog;
import com.data.collection.view.TitleView;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentOffline#} factory method to
 * create an instance of this fragment.
 */
public class FragmentOffline extends FragmentBase {
    private static final String TAG = "FragmentOffline";

    @BindView(R.id.title_view)
    TitleView titleView;

    private MKOfflineMap mOffline = null;



    public static void start(Context context){
        Bundle bundle = new Bundle();
        bundle.putInt(CommonActivity.FRAGMENT, CommonActivity.FRAGMENT_OFFLINE);
        CommonActivity.start(context, bundle);
    }

    public static FragmentOffline getInstance(){
        return new FragmentOffline();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.fragment_tools_offline, container, false);
        bindButterKnife();
        initView();
        initListener();
        return view;
    }

    MKOfflineMapListener mKOfflineMapListener = new MKOfflineMapListener(){

        @Override
        public void onGetOfflineMapState(int type, int state) {
            switch (type) {
                case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                    MKOLUpdateElement update = mOffline.getUpdateInfo(state);
                    // 处理下载进度更新提示
                    if (update != null) {
                        String updatestate = String.format("%s : %d%%", update.cityName, update.ratio);
                        LsLog.i(TAG, "updatestate = " + updatestate);
                        updateView();
                    }
                }
                break;

                case MKOfflineMap.TYPE_NEW_OFFLINE:
                    // 有新离线地图安装
                    Log.d("OfflineDemo", String.format("add offlinemap num:%d", state));
                    break;

                case MKOfflineMap.TYPE_VER_UPDATE:
                    // 版本更新提示
                    // MKOLUpdateElement e = mOffline.getUpdateInfo(state);
                    break;

                default:
                    break;
            }
        }
    };

    private void updateView() {

    }

    private void initView() {
        mOffline = new MKOfflineMap();
        mOffline.init(mKOfflineMapListener);

        ArrayList<MKOLSearchRecord> records = mOffline.getOfflineCityList();
        for (MKOLSearchRecord re: records){
            LsLog.i(TAG, "cityName = " + re.cityName + ", id:" + re.cityID);
        }
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->{
            getActivity().finish();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }
}
