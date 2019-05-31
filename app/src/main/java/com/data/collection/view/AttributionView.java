package com.data.collection.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.module.Attrs;
import com.data.collection.module.Options;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ... on 18/10/7.
 */

public class AttributionView extends LinearLayout {

    private Context mContext;
    private View mView;
    private LinearLayout container;

    private List<View> attrViewList = new ArrayList<>(); // option and fill

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

    public void setViewAttri(List<Attrs> attrs) {
        if (attrs != null && attrs.size() > 0){
            for (Attrs attr: attrs) {
                if (attr.getType().equals("2")) {
                    List<Options> options = attr.getOptions();
                    createOptionAttr(attr.getLabel(), options);
                } else {
                    createFillAttr(attr.getLabel());
                }
            }
        }
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

    private void addChildView(View view, int index) {
        attrViewList.add(index,view);
        container.addView(view, index);
    }

    private synchronized void createOptionAttr(String name, List<Options> option){

        View view = inflater.inflate(R.layout.view_attribution_option, this, false);
        view.setTag("option_attr");
        TextView ckeyview = view.findViewById(R.id.ckey);
        ckeyview.setText(name + ":");

        Spinner spinner = view.findViewById(R.id.spinner);
        List<String> labels = new ArrayList<>();
        for (Options opt: option) {
            labels.add(opt.getLabel());
        }

        ArrayAdapter<String>  adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 加载适配器
        spinner.setAdapter(adapter);

        addChildView(view, 0);
    }

    public void clearView(){
        attrViewList.clear();
        container.removeAllViews();
    }

    public boolean hasChild(){
        return attrViewList.size() > 1;
    }
}

