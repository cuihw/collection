package com.data.collection.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.data.collection.R;

import java.util.List;

/**
 * Created by ... on 18/10/7.
 */

public class AttributionView extends LinearLayout {

    private Context mContext;
    private View mView;
    private LinearLayout container;

    private List<View> attrViewList; // option and fill

    LayoutInflater inflater;

    boolean isFirst = true;

    public List<View> getAttrViewList() {
        return attrViewList;
    }

    public void setAttrViewList(List<View> attrViewList) {
        this.attrViewList = attrViewList;
    }

    public AttributionView(Context context) {
        this(context, null);
    }

    public AttributionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AttributionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.view_attribution, this, true);
        container = mView.findViewById(R.id.container);
    }

    public void setViewAttri(String attrs) {

    }

    private synchronized void createFillAttr(String name){ // 填写
        View view = inflater.inflate(R.layout.view_attribution_fill, this, false);
        view.setTag("fill_attr");
        TextView ckeyview = view.findViewById(R.id.ckey);
        ckeyview.setText(name + ":");

        addChildView(view);
    }

    private void addChildView(View view) {
        attrViewList.add(view);
        container.addView(view);
    }

    private synchronized void createOptionAttr(String name, List<String> option){

        View view = inflater.inflate(R.layout.view_attribution_option, this, false);
        view.setTag("option_attr");
        TextView ckeyview = view.findViewById(R.id.ckey);
        ckeyview.setText(name + ":");
        if (isFirst) {
            isFirst = false;
        } else {
            setBackgroundColor(getResources().getColor(R.color.background3));
        }

        Spinner spinner = view.findViewById(R.id.spinner);

        ArrayAdapter<String>  adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, option);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 加载适配器
        spinner.setAdapter(adapter);

        addChildView(view);
    }
}

