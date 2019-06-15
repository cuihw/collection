package com.data.collection.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.classic.adapter.BaseAdapterHelper;
import com.classic.adapter.CommonAdapter;
import com.data.collection.R;
import com.data.collection.activity.CommonActivity;
import com.data.collection.data.CacheData;
import com.data.collection.module.Attrs;
import com.data.collection.module.Options;
import com.data.collection.module.Project;
import com.data.collection.module.CollectType;
import com.data.collection.module.UserInfoBean;
import com.data.collection.util.LsLog;
import com.data.collection.util.Utils;
import com.data.collection.view.TitleView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragmentProject#} factory method to
 * create an instance of this fragment.
 */
public class FragmentProject extends FragmentBase {
    private static final String TAG = "FragmentProject";

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.listview)
    ListView listview;

    @BindView(R.id.projectname)
    TextView projectName;

    CommonAdapter<CollectType> adapter ;

    public static void start(Context context){
        Bundle bundle = new Bundle();
        bundle.putInt(CommonActivity.FRAGMENT, CommonActivity.FRAGMENT_PROJECT);
        CommonActivity.start(context, bundle);
    }

    public static FragmentProject getInstance(){
        return new FragmentProject();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LsLog.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.fragment_settings_project, container, false);
        bindButterKnife();
        initListener();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
    }

    private void initView() {
        UserInfoBean userInfoBean = CacheData.getUserInfoBean();
        if (userInfoBean == null) {
            projectName.setText("请联系管理员，设置项目内容");
            return;
        }
        Project project = userInfoBean.getData().getProject();
        projectName.setText("项目名称：" + project.getName());

        adapter = new CommonAdapter<CollectType>(getContext(),R.layout.item_project_attr, project.getTypes()) {
            @Override
            public void onUpdate(BaseAdapterHelper helper, CollectType item, int position) {
                helper.setText(R.id.type_name, "采集类型：" + item.getName());

                ImageView iconview = helper.getView(R.id.icon);

                ImageLoader.getInstance().displayImage(item.getIcon(), iconview);



                List<Attrs> attrs = item.getAttrs();
                String showOptingText = "";
                String showFillText = "";

                List<View> listView = new ArrayList<>();

                TextView textView = new TextView(getContext());
                textView.setText("测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试");
                LinearLayout view = helper.getView(R.id.rootview);
                view.addView(textView);

                for (Attrs attr: attrs) {
                    if (attr.getType().equals("2")) {  // 选项框；
                        if (TextUtils.isEmpty(showOptingText)) {
                            showOptingText = attr.getLabel() + ":【";
                        } else {
                            showOptingText = showOptingText + "\n" + attr.getLabel() + ":【";
                        }

                        List<Options> options = attr.getOptions();
                        for (Options op: options) {
                            showOptingText = showOptingText + op.getLabel() + ",";
                        }
                        if (showOptingText.contains(",")) {
                            // 删除最后一个，
                            showOptingText = Utils.trimLastChar(showOptingText);
                        }
                        showOptingText = showOptingText + "】";

                    } else {  // 填空框
                        showFillText = showFillText + attr.getLabel() + ",";
                    }
                }
                showFillText = Utils.trimLastChar(showFillText);

                helper.setText(R.id.option_view,  showOptingText);
                helper.setText(R.id.fill_view,  showFillText);
            }
        };
        listview.setAdapter(adapter);
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->getActivity().finish());
    }



}
