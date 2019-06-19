package com.data.collection.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.classic.adapter.BaseAdapterHelper;
import com.classic.adapter.CommonAdapter;
import com.data.collection.R;
import com.data.collection.listener.INaviItemClickListener;
import com.data.collection.module.NaviData;
import com.data.collection.util.LsLog;
import com.data.navidata.LocaltionData;
import com.data.navidata.NaviDataSS;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;



public class MultipleLayoutAdapter  extends CommonAdapter<NaviData> {
    Context mContext;

    private static final String TAG = "MultipleLayoutAdapter";

    List<NaviData> dataList;


    INaviItemClickListener listener;

    @Override
    public void replaceAll(@NonNull List<NaviData> item) {
        dataList = item;
        super.replaceAll(item);
    }

    public MultipleLayoutAdapter(@NonNull Context context, List<NaviData> data) {
        super(context, R.layout.item_navi_point_site,data);
        mContext = context;
        dataList = data;
    }

    public MultipleLayoutAdapter(@NonNull Context context) {
        super(context, R.layout.item_navi_point_site);
        mContext = context;
    }

    public MultipleLayoutAdapter(@NonNull Context context, int layoutResId) {
        super(context, layoutResId);
        mContext = context;
    }

    public MultipleLayoutAdapter(@NonNull Context context, int layoutResId, List<NaviData> data) {
        super(context, layoutResId, data);
        mContext = context;
        dataList = data;
    }

    @Override
    public void onUpdate(BaseAdapterHelper helper, final NaviData item, int position) {

        View view = helper.getView(R.id.item_layout);
        if (item.isSiteData()) { // site
            helper.setText(R.id.name, item.getName());
            view.setOnClickListener(v->navito(item));
        } else { // segment
            helper.setText(R.id.name,item.getTname());
            view.setOnClickListener(v->openOrHideChild(helper,  item));
            if (item.isSelected()) {
                helper.setImageResource(R.id.expandable_icon, R.mipmap.icon_arrow_down);
            } else {
                helper.setImageResource(R.id.expandable_icon, R.mipmap.icon_arrow_right);
            }
        }
    }

    private void openOrHideChild(BaseAdapterHelper helper, NaviData item) {
        LsLog.w(TAG, "openOrHideChild");

        if (item.isSelected()) { // 目前显示，准备隐藏
            item.setSelected(false);
            showNaviData(item, false);
        }else { // 当前隐藏，准备显示
            item.setSelected(true);
            showNaviData(item, true);
        }
    }

    private void showNaviData(NaviData item, boolean isShow) {

        int index = dataList.indexOf(item);
        List<NaviData> sites = item.getSites();
        if (isShow) {
            List<NaviData> dataListTmp = new ArrayList<>();
            List<NaviData> naviData = dataList.subList(0, index + 1);
            List<NaviData> naviData2 = dataList.subList(index+1, dataList.size());

            dataListTmp.addAll(naviData);
            dataListTmp.addAll(sites);
            dataListTmp.addAll(naviData2);
            dataList = dataListTmp;

        } else {
            for (NaviData data: sites) {
                dataList.remove(data);
            }

        }
        super.replaceAll(dataList);
    }

    private void navito(NaviData subitem) {
        LsLog.w(TAG, "naviTo() = " + subitem.getName());
        if (listener != null) {
            listener.naviTo(subitem);
        }
    }

    @Override
    public int getLayoutResId(NaviData item, int position) {
        int layoutResId = -1;
        if (item.isSiteData()) {
            layoutResId = R.layout.item_navi_point_site; // 是导航站点的布局
        } else {
            layoutResId = R.layout.item_navi_point_child; // 含有子布局的
        }

        return layoutResId;
    }

    public INaviItemClickListener getListener() {
        return listener;
    }

    public void setListener(INaviItemClickListener listener) {
        this.listener = listener;
    }
}
