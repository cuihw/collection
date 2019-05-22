package com.data.collection.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.data.collection.R;


/**
 * Created by ... on 18/10/7.
 */

public class TitleView extends LinearLayout {

    private Context mContext;
    private View mView;
    private TextView titleTv;
    private ImageView left_icon;

    private ImageView right_icon;

    private ImageView divideLeft;
    private ImageView divideRight;

    private int left_icon_id;
    private int right_icon_id;

    LinearLayout title_layout;
    private String titleText;
    public ImageView getLefticon() {
        return left_icon;
    }

    public ImageView getRighticon() {
        return right_icon;
    }

    public TextView getTitleTv() {
        return titleTv;
    }

    public void setTitleTv(TextView titleTv) {
        this.titleTv = titleTv;
    }


    public int getLeft_icon_id() {
        return left_icon_id;
    }

    public int getRight_icon_id() {
        return right_icon_id;
    }


    public LinearLayout getTitle_layout() {
        return title_layout;
    }

    public void setTitle_layout(LinearLayout title_layout) {
        this.title_layout = title_layout;
    }

    public String getTitleText() {
        return titleText;
    }

    public TitleView(Context context) {
        this(context, null);
    }

    public TitleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.title_bar, this, true);
        titleTv =  mView.findViewById(R.id.title_tv);
        left_icon =  mView.findViewById(R.id.left_icon);
        right_icon = mView.findViewById(R.id.right_icon);
        divideLeft = mView.findViewById(R.id.divide_left);
        divideRight = mView.findViewById(R.id.divide_right);

        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.TitleView);
        setTitleText(a.getString(R.styleable.TitleView_TitleText));
        setLeft_icon_id(a.getResourceId(R.styleable.TitleView_IconLeft, 10000));
        setRight_icon_id(a.getResourceId(R.styleable.TitleView_IconRight, 10000));
    }

    private void setRight_icon_id(int iconImgId) {
        if (iconImgId != 10000) {
            this.right_icon_id = iconImgId;
            right_icon.setImageResource(iconImgId);
            divideRight.setVisibility(VISIBLE);
            right_icon.setVisibility(VISIBLE);
        } else {
            divideRight.setVisibility(GONE);
            right_icon.setVisibility(GONE);
        }
    }

    private void setLeft_icon_id(int iconImgId) {
        if (iconImgId != 10000) {
            this.left_icon_id = iconImgId;
            left_icon.setImageResource(iconImgId);
            divideLeft.setVisibility(VISIBLE);
            left_icon.setVisibility(VISIBLE);
        } else {
            divideLeft.setVisibility(GONE);
            left_icon.setVisibility(GONE);
        }
    }

    private void setTitleText(String titleText) {
        if (titleText != null) {
            this.titleText = titleText;
            titleTv.setText(titleText);
        }
    }


}

