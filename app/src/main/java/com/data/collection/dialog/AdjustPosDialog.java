package com.data.collection.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.listener.IAdjustPosListener;

import org.greenrobot.greendao.annotation.NotNull;
import org.osmdroid.util.GeoPoint;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdjustPosDialog extends Dialog{
    IAdjustPosListener listener;


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

    PointF pixel;

    public AdjustPosDialog(Context context, IAdjustPosListener listener) {
        super(context, R.style.Dialog_Common);
        setContentView(R.layout.dialog_cal_position);
        this.listener = listener;
        setCanceledOnTouchOutside(false);
        ButterKnife.bind(this);
        mylocation.setChecked(true);

        cancelTv.setOnClickListener(v->{
            if (listener != null) listener.onCancel();
        });

        confirmTv.setOnClickListener(v->{
            if (listener != null) {
                getDataFromUI();
            }
        });
    }

    private void getDataFromUI() {
        listener.onConfirm(MyPoint, pixel);
    }

    @Override
    public void show() {
        super.show();
        title_tv.setText("坐标纠偏");
        cancelTv.setText("取消");
        confirmTv.setText("确定");
    }

    public void setPixel(PointF pixel) {
        this.pixel = pixel;
    }

    public PointF getPixel() {
        return pixel;
    }
    GeoPoint MyPoint;

    public GeoPoint getMyPoint() {
        return MyPoint;
    }

    public void setMyPoint(GeoPoint myPoint) {
        MyPoint = myPoint;
    }
}
