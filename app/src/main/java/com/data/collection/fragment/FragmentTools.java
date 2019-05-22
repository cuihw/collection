package com.data.collection.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.activity.LoginActivity;
import com.data.collection.data.CacheData;
import com.data.collection.util.LsLog;
import com.data.collection.util.PackageUtils;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.fragment_tools, container, false);
        bindButterKnife();
        return view;
    }


}
