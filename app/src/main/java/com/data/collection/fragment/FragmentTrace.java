package com.data.collection.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.Point;
import com.data.collection.R;
import com.data.collection.activity.CommonActivity;
import com.data.collection.data.BaiduTrace;
import com.data.collection.module.Gps;
import com.data.collection.util.DateUtils;
import com.data.collection.util.LsLog;
import com.data.collection.util.PositionUtil;
import com.data.collection.util.ToastUtil;
import com.data.collection.util.Utils;
import com.data.collection.view.TitleView;

import java.util.Calendar;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentTrace#} factory method to
 * create an instance of this fragment.
 */
public class FragmentTrace extends FragmentBase {
    private static final String TAG = "FragmentTrace";

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.datetime_layout)
    LinearLayout datetimeLayout;

    @BindView(R.id.starttime)
    TextView dateView;

    public static void start(Context context){
        Bundle bundle = new Bundle();
        bundle.putInt(CommonActivity.FRAGMENT, CommonActivity.FRAGMENT_TRACE);
        CommonActivity.start(context, bundle);
    }

    public static FragmentTrace getInstance(){
        return new FragmentTrace();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.fragment_settings_trace, container, false);
        bindButterKnife();
        initView();
        initListener();
        return view;
    }

    // 初始化轨迹监听器
    OnTrackListener mTrackListener = new OnTrackListener() {
        // 历史轨迹回调
        @Override
        public void onHistoryTrackCallback(HistoryTrackResponse response) {
            Point startPoint = response.getStartPoint();
            long locTime = startPoint.getLocTime() * 1000;
            Point endPoint = response.getEndPoint();
            long locTime1 = endPoint.getLocTime()* 1000;
            String starttime = DateUtils.formatTime(locTime, DateUtils.fmtYYYYMMDDhhmmss);
            String endtime = DateUtils.formatTime(locTime1, DateUtils.fmtYYYYMMDDhhmmss);
            LsLog.w(TAG, "start locTime" + starttime + ", endtimd = " + endtime);
            showTrace(response);
        }
    };

    private void showTrace(HistoryTrackResponse response) {

    }

    private void initView() {
        // 默认当天时间
        BaiduTrace.getInstance().getHistoryTrace(0,0, mTrackListener);
        datetimeLayout.setOnClickListener(v->showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(new ContextThemeWrapper(getActivity(),
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar),
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        dealDate(year, monthOfYear, dayOfMonth);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        DatePicker datePicker1 = datePicker.getDatePicker();
        datePicker1.setMaxDate(System.currentTimeMillis());
        datePicker.show();
    }
    private Calendar calendar = Calendar.getInstance();

    private void dealDate(int year, int monthOfYear, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DATE, dayOfMonth);
        String sDate = DateUtils.formatDate(calendar, DateUtils.fmtYYYYMMDD);
        dateView.setText(sDate);
        getTraceData(calendar); // 请求百度轨迹数据
    }

    private void getTraceData(Calendar calendar) {
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long starttime = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endTime = calendar.getTimeInMillis();

        BaiduTrace.getInstance().getHistoryTrace(starttime,endTime, mTrackListener);
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->{
            getActivity().finish();

        });
    }


}
