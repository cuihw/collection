package com.data.collection.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.classic.adapter.BaseAdapterHelper;
import com.classic.adapter.CommonAdapter;
import com.data.collection.R;
import com.data.collection.activity.CommonActivity;
import com.data.collection.module.BaiduCity;
import com.data.collection.util.LsLog;
import com.data.collection.util.Utils;
import com.data.collection.view.TitleView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    @BindView(R.id.citylist)
    ListView cityListView;

    private MKOfflineMap mOffline = null;
    CommonAdapter<BaiduCity> cityAdapter;

    // 全国省份list
    ArrayList<BaiduCity> cityList;

    // 全国城市数据
    Map<Integer, BaiduCity> allCityMaps = new HashMap<>();

    // 当前下载更新省份
    MKOLSearchRecord currentDown;

    // 已经下载的省份；
    Map<Integer, MKOLSearchRecord> offlineMap = new HashMap<>();

    /**
     * 已下载的离线地图信息列表
     */
    private ArrayList<MKOLUpdateElement> localMapList = null;
    CommonAdapter<MKOLUpdateElement> localCityAdapter;
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
                        String updatestate = String.format("%s : %d%% ,id = %d", update.cityName, update.ratio, update.cityID);
                        LsLog.i(TAG, "updatestate = " + updatestate);
                        updateView(update);
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
    private void updateView(MKOLUpdateElement update) {

        // 更新update界面
        localMapList = mOffline.getAllUpdateInfo();

        BaiduCity baiduCityInfo = allCityMaps.get(update.cityID);
        currentDown = baiduCityInfo;

        if (baiduCityInfo.parentRecord != null) {
            currentDown = baiduCityInfo.parentRecord;
        }

        cityAdapter.notifyDataSetChanged();
    }

    private void initView() {
        mOffline = new MKOfflineMap();
        mOffline.init(mKOfflineMapListener);

        ArrayList<MKOLSearchRecord> records = mOffline.getOfflineCityList();

        setUpRelative(records, null);

        cityAdapter = new CommonAdapter<BaiduCity>(getContext(), R.layout.item_city, cityList) {
            @Override
            public void onUpdate(BaseAdapterHelper helper, BaiduCity item, int position) {
                helper.setText(R.id.name, item.cityName);
                String size = Utils.formatDataSize(item.dataSize);
                helper.setText(R.id.size, "离线包大小：" + size);
                helper.setText(R.id.cityid, "城市编号：" + item.cityID);
                Button download = helper.getView(R.id.download);
                download.setOnClickListener(v-> downloadMap(item));

                if (item.state == BaiduCity.DOWNLOADING) {
                    download.setEnabled(false);
                    download.setText("正在下载");
                } else if (item.state == BaiduCity.NORMAAL) {
                    download.setEnabled(true);
                    download.setText("下载");
                }else if (item.state == BaiduCity.DOWNLOADED) {
                    download.setEnabled(false);
                    download.setText("下载完毕");
                } else if (item.state == BaiduCity.WAITING_DOWNLOADED) {
                    download.setEnabled(false);
                    download.setText("等待下载");
                }

            }
        };
        cityListView.setAdapter(cityAdapter);

        // 已经下载的城市列表
        localMapList = mOffline.getAllUpdateInfo();
        CommonAdapter<MKOLUpdateElement> localCityAdapter;
        for (MKOLUpdateElement element: localMapList) {
            LsLog.i(TAG, "id = " + element.cityID + ", name = " + element.cityName);
            BaiduCity baiduCityRelative = allCityMaps.get(element.cityID);
            if (baiduCityRelative.parentRecord != null) {
                offlineMap.put(baiduCityRelative.parentRecord.cityID, baiduCityRelative.parentRecord);
            } else {
                offlineMap.put(baiduCityRelative.cityID, baiduCityRelative);
            }
        }
    }

    private void setUpRelative(ArrayList<MKOLSearchRecord> records, MKOLSearchRecord parent) {

        for (MKOLSearchRecord re: records){
            LsLog.i(TAG, "cityName = " + re.cityName + ", id:" + re.cityID);
            if (re.childCities != null) {
                setUpRelative(re.childCities, re);
            }
            BaiduCity city = new BaiduCity(re);
            city.parentRecord = parent;
            allCityMaps.put(city.cityID, city);

            if (parent == null) {
                cityList.add(city);
            }
        }
    }

    private void downloadMap(BaiduCity item) {
        // 等待下载
        item.state = BaiduCity.WAITING_DOWNLOADED;

        mOffline.start(item.cityID);
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
