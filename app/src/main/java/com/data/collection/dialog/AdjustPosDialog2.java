package com.data.collection.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.listener.IAdjustPosListener2;
import com.esri.arcgisruntime.geometry.Point;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdjustPosDialog2 extends Dialog {
    IAdjustPosListener2 listener;

    @BindView(R.id.mylocation)
    RadioButton mylocation;

    @BindView(R.id.recognize_position)
    RadioButton recognize_position;

    @BindView(R.id.title_tv)
    TextView title_tv;

    @BindView(R.id.cancel_tv)
    TextView cancelTv;

    @BindView(R.id.confirm_tv)
    TextView confirmTv;

    @BindView(R.id.lng_text)
    EditText lngEdit;

    @BindView(R.id.lat_text)
    EditText latEdit;

    Point fromPoint; // 点击的坐标

    Point toPoint;

    public Point getToPoint() {
        return toPoint;
    }

    public void setToPoint(Point toPoint) {
        this.toPoint = toPoint;
    }

    public AdjustPosDialog2(Context context, IAdjustPosListener2 iAdjustPosListener) {
        super(context, R.style.Dialog_Common);
        setContentView(R.layout.dialog_cal_position);
        this.listener = iAdjustPosListener;
        setCanceledOnTouchOutside(false);
        ButterKnife.bind(this);
        mylocation.setChecked(true);

        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onCancel();
            }
        });

        confirmTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            getDataFromUI();
                        }
                    }
                }
        );
    }

    private void getDataFromUI() {
        if (mylocation.isChecked()) {
            listener.onConfirm(toPoint, fromPoint);
        } else {
            String longtitude = lngEdit.getText().toString();
            String latitude = latEdit.getText().toString();
            listener.onConfirm(new Point(Double.parseDouble(longtitude), Double.parseDouble(latitude)), fromPoint);
        }
    }

    @Override
    public void show() {
        super.show();
        title_tv.setText("坐标纠偏");
        cancelTv.setText("取消");
        confirmTv.setText("确定");

        if (toPoint != null) {
            lngEdit.setText(toPoint.getX() + "");
            latEdit.setText(toPoint.getY() + "");
        }
    }

    public Point getFromPoint() {
        return fromPoint;
    }

    public void setFromPoint(Point fromPoint) {
        this.fromPoint = fromPoint;
    }

}
