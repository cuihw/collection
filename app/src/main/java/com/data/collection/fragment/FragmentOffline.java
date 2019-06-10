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
import android.widget.TextView;

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

    @BindView(R.id.localButton)
    Button localButton;

    @BindView(R.id.clButton)
    Button clButton;

    private MKOfflineMap mOffline = null;

    // 全国省份list
    ArrayList<BaiduCity> cityList = new ArrayList<>();

    // 全国城市数据
    Map<Integer, BaiduCity> allCityMaps = new HashMap<>();

    // 已经下载的省份；
    ArrayList<BaiduCity> localCityList = new ArrayList<>();

    CommonAdapter<BaiduCity> cityAdapter;
    CommonAdapter<BaiduCity> localCityAdapter;

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
                    MKOLUpdateElement update = mOffline.getUpdateInfo(state);
                    updateNewView(update);
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

    // 城市地图有更新
    private void updateNewView(MKOLUpdateElement update) {
        Map<Integer, BaiduCity> offlineMap = new HashMap<>();
        BaiduCity city = offlineMap.get(update.cityID);
        city.update = update.update;

        BaiduCity city1 = city; // city1 一层的省名字。
        if (update.update) {
            if (city.parentRecord != null){
                city.parentRecord.update = update.update;
                city1 = city.parentRecord;
            }
        }
        for (BaiduCity cityitem: localCityList){
            if (cityitem.cityID == city1.cityID) {
                cityitem.update = true;
            }
        }

        localCityAdapter.replaceAll(localCityList);
    }


    private void updateView(MKOLUpdateElement update) {

        BaiduCity baiduCity = allCityMaps.get(update.cityID);
        if (update.ratio == 100) {
            baiduCity.state = BaiduCity.DOWNLOADED;
        } else {
            baiduCity.state = BaiduCity.DOWNLOADING;
        }
        BaiduCity parentCity = baiduCity;
        if ( parentCity.parentRecord != null) {
            parentCity = baiduCity.parentRecord;
            if (baiduCity.state == BaiduCity.DOWNLOADED) {
                parentCity.state = BaiduCity.DOWNLOADED;
                for (MKOLSearchRecord city: parentCity.childCities) {
                    BaiduCity city1 = allCityMaps.get(city.cityID);
                    if (city1.state != BaiduCity.DOWNLOADED) {
                        parentCity.state = BaiduCity.DOWNLOADING;
                    }
                }
            } else {
                parentCity.state = BaiduCity.DOWNLOADING;
                allCityMaps.put(parentCity.cityID, parentCity);
            }
            cityAdapter.replaceAll(cityList);
        }


        if (parentCity.state == BaiduCity.DOWNLOADED) {
            localCityList.add(parentCity);
            localCityAdapter.replaceAll(localCityList);
        }

    }

    private void initView() {
        initAllCityMap();
        cityAdapter = new CommonAdapter<BaiduCity>(getContext(), R.layout.item_city, cityList) {
            @Override
            public void onUpdate(BaseAdapterHelper helper, BaiduCity item, int position) {
                helper.setText(R.id.name, item.cityName);
                String size = Utils.formatDataSize(item.dataSize);
                helper.setText(R.id.size, "离线包大小：" + size);
                helper.setText(R.id.cityid, "城市编号：" + item.cityID);
                Button download = helper.getView(R.id.download);
                download.setOnClickListener(v-> startDownloadMap(item));
                BaiduCity item1 = allCityMaps.get(item.cityID);
                if (item1.state == BaiduCity.DOWNLOADING) {
                    download.setEnabled(false);
                    download.setText("正在下载");
                } else if (item1.state == BaiduCity.NORMAL) {
                    download.setEnabled(true);
                    download.setText("下载");
                }else if (item1.state == BaiduCity.DOWNLOADED) {
                    download.setEnabled(false);
                    download.setText("下载完毕");
                } else if (item1.state == BaiduCity.WAITING_DOWNLOADED) {
                    download.setEnabled(false);
                    download.setText("等待下载");
                }
            }
        };
        cityListView.setAdapter(cityAdapter);

        // 已经下载的城市列表
        initDownloadInfo();
        cityAdapter.replaceAll(cityList);

        getDownloadInfo();
        localCityAdapter = new CommonAdapter<BaiduCity>(getContext(),R.layout.item_city_offline, localCityList){
            @Override
            public void onUpdate(BaseAdapterHelper helper, BaiduCity item, int position) {
                helper.setText(R.id.name, item.cityName);
                String sdata = Utils.formatDataSize(item.dataSize);
                helper.setText(R.id.size, "离线包大小：" + sdata);
                helper.setText(R.id.cityid, "ID ：" + item.cityID);
                TextView updateView = helper.getView(R.id.update);

                if (item.update) {
                    updateView.setEnabled(true);
                } else {
                    updateView.setEnabled(false);
                }
                updateView.setOnClickListener(v->upDateMap(item));

                TextView deleteView = helper.getView(R.id.delete);
                deleteView.setOnClickListener(v->{
                    deleteCity(item);
                });

            }
        };
    }

    private void upDateMap(BaiduCity item) {
        mOffline.update(item.cityID);
        BaiduCity city = allCityMaps.get(item.cityID);
        city.update = false;
    }

    private void initAllCityMap() {
        mOffline = new MKOfflineMap();
        mOffline.init(mKOfflineMapListener);
        ArrayList<MKOLSearchRecord> records = mOffline.getOfflineCityList();
        setUpRelative(records, null);
    }

    private void deleteCity(BaiduCity item) {
        LsLog.i(TAG, "deleteCity " + item.toJson());
        BaiduCity city = allCityMaps.get(item.cityID);

        city.state = BaiduCity.NORMAL;

        if (item.childCities != null) {  // 地级市下载状态为normal。
            for (MKOLSearchRecord cityChild: item.childCities){
                BaiduCity city1 = allCityMaps.get(cityChild.cityID);
                if (city1 != null) {
                    city1.state = BaiduCity.NORMAL;
                }
            }
        }

        mOffline.remove(city.cityID);

        cityAdapter.replaceAll(cityList);

        BaiduCity toBeRemove = city;

        localCityList.remove(toBeRemove);
        localCityAdapter.replaceAll(localCityList);
    }

    private void getDownloadInfo() {
        localCityList.clear();

        for (BaiduCity city: cityList ) {
            if (city.parentRecord == null && city.state == BaiduCity.DOWNLOADED) {
                localCityList.add(city);
            }
        }

        if (localCityAdapter != null) {
            localCityAdapter.replaceAll(localCityList);
        }
    }

    private void initDownloadInfo() {
        ArrayList<MKOLUpdateElement> allUpdateInfo = mOffline.getAllUpdateInfo();
        if (allUpdateInfo == null) {
            LsLog.i(TAG, "offline map is NONE");
            return;
        }
        for (MKOLUpdateElement element: allUpdateInfo) {
            LsLog.i(TAG, "offline map : id = " + element.cityID + ", name = " + element.cityName);
            BaiduCity baiduCity = allCityMaps.get(element.cityID);
            baiduCity.state = BaiduCity.DOWNLOADED;

            if (baiduCity.parentRecord == null ){  // 省份城市
                LsLog.i(TAG, "add to provence list offline map : id = "
                        + element.cityID + ", name = " + element.cityName);

                localCityList.add(baiduCity);
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
            if (parent == null) {
                // 省份，直辖市  Provence.
                cityList.add(city);
            } else {
                city.parentRecord = new BaiduCity(parent);
            }
            allCityMaps.put(city.cityID, city);
        }
    }

    private void startDownloadMap(BaiduCity item) {
        // 等待下载
        item.state = BaiduCity.WAITING_DOWNLOADED;
        mOffline.start(item.cityID);
        cityAdapter.replaceAll(cityList);
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->{
            getActivity().finish();
        });

        clButton.setOnClickListener(v->{
            cityListView.setAdapter(cityAdapter);
        });

        localButton.setOnClickListener(v->{
            cityListView.setAdapter(localCityAdapter);
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
