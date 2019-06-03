package com.data.collection.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.activity.CommonActivity;
import com.data.collection.module.Gps;
import com.data.collection.util.LsLog;
import com.data.collection.util.PositionUtil;
import com.data.collection.util.ToastUtil;
import com.data.collection.util.Utils;
import com.data.collection.view.TitleView;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentPositionTransform#} factory method to
 * create an instance of this fragment.
 */
public class FragmentPositionTransform extends FragmentBase {
    private static final String TAG = "FragmentPositionTransform";

    @BindView(R.id.title_view)
    TitleView titleView;
    @BindView(R.id.action_transform)
    TextView actionTransform;

    @BindView(R.id.longitude_edit)
    EditText longitudeEdit;
    @BindView(R.id.latitude_edit)
    EditText latitudeEdit;

    @BindView(R.id.spinner)
    Spinner spinner;

    @BindView(R.id.result_text)
    TextView resultText;
    @BindView(R.id.copy_result)
    TextView copyResult;

    @BindView(R.id.scroll_view)
    ScrollView scroll_view;



    public static void start(Context context){
        Bundle bundle = new Bundle();
        bundle.putInt(CommonActivity.FRAGMENT, CommonActivity.FRAGMENT_POSITION);
        CommonActivity.start(context, bundle);
    }

    public static FragmentPositionTransform getInstance(){
        return new FragmentPositionTransform();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.fragment_tools_position, container, false);
        bindButterKnife();
        initListener();
        scroll_view.setVisibility(View.INVISIBLE);
        return view;
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->{
            getActivity().finish();
        });

        actionTransform.setOnClickListener(v-> startTransform());

        copyResult.setOnClickListener(v->copyResultToClipBroad());
    }

    private void copyResultToClipBroad() {
        String s = resultText.getText().toString();
        Utils.copyToClip(getContext(), s);
        ToastUtil.showTextToast(getContext(), "已经复制到剪贴板");
    }

    private void startTransform() {
        int pos = spinner.getSelectedItemPosition();
        String s = longitudeEdit.getText().toString();
        String s1 = latitudeEdit.getText().toString();
        try {
            double longitude = Double.parseDouble(s);
            double latitude = Double.parseDouble(s1);

            if (longitude > 360 || latitude > 360) {
                ToastUtil.showTextToast(getContext(),"请输入正确的经纬度");
                return;
            }
            Gps gps =  null;
            switch (pos) {
                case 0: // WGS84 -> GCJ02
                    gps = PositionUtil.gps84_To_Gcj02(longitude, latitude);
                    break;
                case 1: // WGS84 -> BD09
                    gps = PositionUtil.gps84_To_Gcj02(longitude, latitude);
                    gps = PositionUtil.gcj02_To_Bd09(gps.getWgLon(), gps.getWgLat());
                    break;
                case 2: // GCJ02 -> BD09
                    gps = PositionUtil.gcj02_To_Bd09(longitude, latitude);
                    break;
                case 3: // GCJ02 ->WGS84
                    gps = PositionUtil.gcj_To_Gps84(longitude, latitude);
                    break;
                case 4: // BD09 -> GCJ02
                    gps = PositionUtil.bd09_To_Gcj02(longitude, latitude);
                    break;
                case 5: // BD09 -> WGS84
                    gps = PositionUtil.bd09_To_Gcj02(longitude, latitude);
                    gps = PositionUtil.gcj_To_Gps84(gps.getWgLon(), gps.getWgLat());
                    break;
            }

            resultText.setText( gps.getWgLon() + ", " + gps.getWgLat());
        }catch (Exception e) {
            ToastUtil.showTextToast(getContext(),"请输入正确的经纬度");
        }

    }


}
