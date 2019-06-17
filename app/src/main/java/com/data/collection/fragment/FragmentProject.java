package com.data.collection.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.data.collection.module.CollectType;
import com.data.collection.module.Options;
import com.data.collection.module.Project;
import com.data.collection.module.UserInfoBean;
import com.data.collection.util.LsLog;
import com.data.collection.view.TitleView;
import com.nostra13.universalimageloader.core.ImageLoader;

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

    CommonAdapter<CollectType> adapter;

    public static void start(Context context) {
        Bundle bundle = new Bundle();
        bundle.putInt(CommonActivity.FRAGMENT, CommonActivity.FRAGMENT_PROJECT);
        CommonActivity.start(context, bundle);
    }

    public static FragmentProject getInstance() {
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

        adapter = new CommonAdapter<CollectType>(getContext(), R.layout.item_project_attr, project.getTypes()) {
            @Override
            public void onUpdate(BaseAdapterHelper helper, CollectType item, int position) {
                helper.setText(R.id.type_name, item.getName());
                ImageView iconview = helper.getView(R.id.icon);
                ImageLoader.getInstance().displayImage(item.getIcon(), iconview);
                List<Attrs> attrs = item.getAttrs();

                List<View> listView = new ArrayList<>();
                for (Attrs attr : attrs) {
                    View infoView = getInfoView(attr);
                    if (attr.getType().equals("2")) {
                        listView.add(0, infoView);
                    } else {
                        listView.add(infoView);
                    }
                }

                if (listView.size() > 0) {
                    View view = listView.get(0);
                    View top = view.findViewById(R.id.divider_top);
                    top.setVisibility(View.GONE);

                    view = listView.get(listView.size() - 1);
                    View bottom = view.findViewById(R.id.divider_bottom);
                    bottom.setVisibility(View.GONE);
                    LinearLayout rootview = helper.getView(R.id.rootview);
                    rootview.removeAllViews();
                    for (View viewc : listView) {
                        rootview.addView(viewc);
                    }
                }
            }
        };
        listview.setAdapter(adapter);
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v -> getActivity().finish());
    }

    private View getInfoView(Attrs attr) {
        //view = inflater.inflate(R.layout.fragment_settings_project, container, false);
        LsLog.w(TAG, "getInfoView attr= " + attr.toJson());
        View view = null;
        if (attr.getType().equals("2")) {  // 选项框；
            view = LayoutInflater.from(getContext()).inflate(R.layout.view_project_attris_option, null);
            TextView ckname = view.findViewById(R.id.ckey_name);
            ckname.setText(attr.getLabel());
            TextView ckdefault = view.findViewById(R.id.ckey_default);
            ckdefault.setText("选择属性，默认值：" + attr.getValue());

            TextView ckvalues = view.findViewById(R.id.ckey_values);
            List<Options> options = attr.getOptions();
            StringBuffer sb = new StringBuffer();
            sb.append("【");
            for (Options opt : options) {
                sb.append(opt.getLabel());
            }
            sb.append("】");
            ckvalues.setText(sb.toString());
        } else if (attr.getType().equals("1")) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.view_project_attris_fill, null);
            TextView ckname = view.findViewById(R.id.ckey_name);
            ckname.setText(attr.getLabel());
        }
        return view;
    }

}
