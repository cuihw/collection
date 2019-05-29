package com.data.collection.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.data.collection.R;
import com.data.collection.activity.CommonActivity;
import com.data.collection.util.LsLog;
import com.data.collection.view.TitleView;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link Fragment1Sample#} factory method to
 * create an instance of this fragment.
 */
public class Fragment1Sample extends FragmentBase {
    private static final String TAG = "Fragment1Sample";

    @BindView(R.id.title_view)
    TitleView titleView;

    public static void start(Context context){
        Bundle bundle = new Bundle();
        // TODO add the constant in  CommonActivity replace FRAGMENT_PROJECT
        bundle.putInt(CommonActivity.FRAGMENT, CommonActivity.FRAGMENT_PROJECT);
        CommonActivity.start(context, bundle);
    }

    public static Fragment1Sample getInstance(){
        return new Fragment1Sample();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");
        //  replace Layout
        view = inflater.inflate(R.layout.fragment_settings_project, container, false);
        bindButterKnife();
        initListener();
        return view;
    }


    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->getActivity().finish());
    }



}
