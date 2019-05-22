package com.data.collection.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.activity.CommonActivity;
import com.data.collection.util.LsLog;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentTools#} factory method to
 * create an instance of this fragment.
 */
public class FragmentTools extends FragmentBase {
    private static final String TAG = "FragmentSettings";


    @BindView(R.id.compass)
    LinearLayout compass;

    public static void start(Context context){
        Bundle bundle = new Bundle();
        bundle.putInt(CommonActivity.FRAGMENT, CommonActivity.FRAGMENT_TOOLS);
        CommonActivity.start(context, bundle);
    }

    public static FragmentTools getInstance(){
        return new FragmentTools();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.fragment_tools, container, false);
        bindButterKnife();
        initListener();
        return view;
    }

    private void initListener() {
        compass.setOnClickListener(v->showCompass());
    }

    private void showCompass() {
        FragmentCompass.start(getContext());
    }


}
