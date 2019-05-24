package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.data.collection.R;
import com.data.collection.view.TitleView;

import butterknife.BindView;

public class AddCollectionActivity extends BaseActivity {

    private static final String TAG = "AddCollectionActivity";


    @BindView(R.id.title_view)
    TitleView titleView;


    public static void start(Context context, Bundle bundle){
        Intent intent = new Intent(context, AddCollectionActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_collection);
        initListener();
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->finish());
    }



}