package com.data.collection.view;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.module.Attrs;
import com.data.collection.module.CollectType;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ... on 18/10/7.
 */

public class AttributionView extends LinearLayout {

    private static final String TAG = "AttributionView";
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
                if (attr.getType().equals(Attrs.TYPE_OPTION)) {
                    List<String> options = attr.getOptions();
                    createOptionAttr(attr.getLabel(), options);
                } else if (attr.getType().equals(Attrs.TYPE_TEXT)){
                    createFillAttr(attr.getLabel(), false);
                } else if (attr.getType().equals(Attrs.TYPE_NUMBERIC)){
                    createFillAttr(attr.getLabel(), true);
                }
            }
        }
        if (attrViewList.size() > 0) {
            View view = attrViewList.get(attrViewList.size() - 1);
            String tag = (String)view.getTag();
            if ("fill_attr".equals(tag)) {
                view.findViewById(R.id.bottom).setVisibility(View.VISIBLE);
            }
        }
    }

    private synchronized void createFillAttr(String name, boolean isNumber){ // 填写

        View view = inflater.inflate(R.layout.view_attribution_fill, this, false);
        view.setTag("fill_attr");
        View divider = view.findViewById(R.id.divider);
        if (isFirst) {
            divider.setVisibility(View.INVISIBLE);
            isFirst = false;
        } else {
            divider.setVisibility(View.VISIBLE);
        }
        TextView ckeyview = view.findViewById(R.id.ckey);
        ckeyview.setText(name);

        EditText et = view.findViewById(R.id.value);
        if (isNumber) {
            et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
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

    private synchronized void createOptionAttr(String name, List<String> option){

        View view = inflater.inflate(R.layout.view_attribution_option, this, false);
        view.setTag("option_attr");
        TextView ckeyview = view.findViewById(R.id.ckey);
        ckeyview.setText(name);

        Spinner spinner = view.findViewById(R.id.spinner);
        List<String> labels = new ArrayList<>();
        for (String opt: option) {
            labels.add(opt);
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

    public CollectType getAttrsValue(CollectType selectedItem) {

        List<Attrs> attrs = selectedItem.getAttrs();

        for (View view: attrViewList) {
            for (Attrs att:attrs) {
                String tag = (String)view.getTag();
                if ("fill_attr".equals(tag)) {
                    TextView ckeyview = view.findViewById(R.id.ckey);
                    String label = ckeyview.getText().toString().trim();
                    if (label.startsWith(att.getLabel())) {
                        EditText valueView = view.findViewById(R.id.value);
                        String value = valueView.getText().toString();
                        if (TextUtils.isEmpty(value)) {
                            ToastUtil.showTextToast(mContext,"请填写" + label+ "的属性值");
                            return null;
                        }
                        att.setValue(valueView.getText().toString());
                    }
                } else {
                    TextView ckeyview = view.findViewById(R.id.ckey);
                    String label = ckeyview.getText().toString().trim();
                    if (label.startsWith(att.getLabel())) {
                        Spinner spinner = view.findViewById(R.id.spinner);
                        int selectedItemPosition = spinner.getSelectedItemPosition() + 1; // 得到当前选项，选项值增加1
                        att.setValue("" + selectedItemPosition);
                    }
                }
            }
        }
        return selectedItem;
    }

    public void setGatherPoint(GatherPoint gatherPoint) {
        LsLog.w(TAG, "setGatherPoint");
        String attrs = gatherPoint.getAttrs();
        LsLog.w(TAG, "attrs" + attrs);
        Gson gson = new Gson();

        Type type =new TypeToken<List<Attrs>>(){}.getType();

        List<Attrs> attrList = gson.fromJson(attrs, type);
        LsLog.w(TAG, "setGatherPoint" + attrList);
        boolean isUploaded = gatherPoint.getIsUploaded();

        for (Attrs attr: attrList) {
            String value = attr.getValue();
            String label = attr.getLabel();
            String type1 = attr.getType();  // type == 1 填空
            List<String> options = attr.getOptions();

            for (View view: attrViewList) {
                String tag = (String)view.getTag();
                TextView ckeyview = view.findViewById(R.id.ckey);
                String labelValue = ckeyview.getText().toString().trim();
                if (Attrs.TYPE_TEXT.equals(type1) || Attrs.TYPE_NUMBERIC.equals(type1)) {
                    if (tag.equals("fill_attr") && labelValue.equals(label)) {
                        EditText valueView = view.findViewById(R.id.value);
                        valueView.setText(value);
                        if (isUploaded) {
                            valueView.setEnabled(false);
                        }
                    }
                } else if (Attrs.TYPE_OPTION.equals(type1) && tag.equals("option_attr") && labelValue.equals(label)){
                    Spinner spinner = view.findViewById(R.id.spinner);
                    int spinnerIndex = 0;
                    for (int i = 0; i < options.size(); i++) {
                        if (options.get(i).equals(value)) {
                            spinnerIndex = i;
                            break;
                        }
                    }
                    spinner.setSelection(spinnerIndex);
                    if (isUploaded) {
                        spinner.setEnabled(false);
                    }
                }
            }
        }
    }

    public void clearViewData() {
        for(View view : attrViewList) {
            String tag = (String)view.getTag();
            if (!TextUtils.isEmpty(tag)) {
                if (tag.equals("fill_attr")) {
                    EditText valueView = view.findViewById(R.id.value);
                    valueView.setText("");
                }
            }
        }
    }
}

