package com.data.collection.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.fragment.FragmentHome2;
import com.data.collection.listener.IAdjustPosListener2;
import com.data.collection.listener.ISavePolygonListener;
import com.data.collection.util.ToastUtil;
import com.esri.arcgisruntime.geometry.Point;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.sddman.arcgistool.common.Variable;

public class SavePloygonDialog extends Dialog {

    @BindView(R.id.cancel_tv)
    TextView cancelTv;

    @BindView(R.id.confirm_tv)
    TextView confirmTv;

    @BindView(R.id.type_tv)
    TextView typeTv;

    @BindView(R.id.result_tv)
    TextView resultTv;

    @BindView(R.id.points_num_tv)
    TextView pointsNumTv;


    @BindView(R.id.name)
    EditText name;

    @BindView(R.id.comments)
    EditText comments;

    ISavePolygonListener listener;

    public SavePloygonDialog(Context context, ISavePolygonListener iSavePolygonListener) {
        super(context, R.style.Dialog_Common);
        setContentView(R.layout.dialog_save_ploygon);
        setCanceledOnTouchOutside(false);
        ButterKnife.bind(this);
        listener = iSavePolygonListener;
        confirmTv.setOnClickListener(v->{
            if (listener !=  null) {
                String s = comments.getText().toString();
                String s1 = name.getText().toString();
                if (TextUtils.isEmpty(s1)) {
                    ToastUtil.showTextToast(getContext(),"名字不能为空");
                    return;
                }
                listener.onConfirm(s1, s);
            }
            dismiss();
        });
        cancelTv.setOnClickListener(v->{
            if (listener !=  null) {
                listener.onCancel();
            }
            dismiss();
        });
    }
    @Override
    public void show() {
        super.show();
        cancelTv.setText("取消");
        confirmTv.setText("保存");

        Variable.DrawType drawType = FragmentHome2.Measurehelper.getInstance().getDrawType();
        if (drawType == Variable.DrawType.LINE) {
            typeTv.setText("测量长度");
            double lineLength = FragmentHome2.Measurehelper.getInstance().getLineLength();
            resultTv.setText(String.format("%.2f", lineLength) + " 米");

        }else {
            typeTv.setText("测量面积");
            double area = FragmentHome2.Measurehelper.getInstance().getArea();
            resultTv.setText(String.format("%.2f", area) + " 平方米");
        }
        List<Point> pointList = FragmentHome2.Measurehelper.getInstance().getPointList();
        if (pointList != null) {
            pointsNumTv.setText("" + pointList.size());
        }
    }
}
