package com.data.collection.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
 * Use the {@link FragmentTrace#} factory method to
 * create an instance of this fragment.
 */
public class FragmentTrace extends FragmentBase {
    private static final String TAG = "FragmentTrace";

    @BindView(R.id.title_view)
    TitleView titleView;

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
        initListener();
        return view;
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->{
            getActivity().finish();
        });
    }


}
