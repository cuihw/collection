package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.classic.adapter.BaseAdapterHelper;
import com.classic.adapter.CommonAdapter;
import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.data.CacheData;
import com.data.collection.data.greendao.DaoSession;
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.data.greendao.GatherPointDao;
import com.data.collection.module.BaseBean;
import com.data.collection.module.ImageData;
import com.data.collection.module.ImageUploadBean;
import com.data.collection.module.Types;
import com.data.collection.module.UserInfoBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.LsLog;
import com.data.collection.view.TitleView;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

// 采集点列表

public class CollectionActivity extends BaseActivity {

    private static final String TAG = "CollectionActivity";

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.listview)
    ListView listView;


    @BindView(R.id.synced_button)
    RadioButton syncedButton;

    @BindView(R.id.local_button)
    RadioButton localButton;

    @BindView(R.id.radio_group)
    RadioGroup radioGroup;

    @BindView(R.id.action_sync_all)
    TextView actionSyncAll;

    CommonAdapter<GatherPoint> adapter;

    List<GatherPoint> dataList;

    List<GatherPoint> dataLocalList;

    List<Types> collectTypes;

    public static void start(Context context) {
        Intent intent = new Intent(context, CollectionActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_list);
        initListener();

        dataLocalList = getData(false); // local data
        dataList = getData(true);
        initView();

    }

    private void initView() {
        localButton.setChecked(true);

        UserInfoBean userInfoBean = CacheData.getUserInfoBean();
        collectTypes = userInfoBean.getData().getProject().getTypes();

        adapter = new CommonAdapter<GatherPoint>(this, R.layout.item_gather_point, dataLocalList) {
            @Override
            public void onUpdate(BaseAdapterHelper helper, GatherPoint item, int position) {
                helper.setText(R.id.name, item.getName());
                String type_id = item.getType_id();
                Types thisType = null;
                for (Types type: collectTypes ) {
                    if (type.getId() == type_id) {
                        thisType = type;
                    }
                }
                if (thisType != null) {
                    ImageView imageView = helper.getView(R.id.icon_type);
                    ImageLoader.getInstance().displayImage(thisType.getIcon(), imageView);
                    helper.setText(R.id.type_name, thisType.getName());
                }
                helper.setText(R.id.location, "经度：" + item.getLongitude() + "， 维度：" + item.getLatitude());
                helper.setText(R.id.time, "采集时间:" + item.getCollected_at());
                //upload_tv
                helper.setOnClickListener(R.id.upload_tv, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadLocalDataWithImage(item);
                    }
                });
            }
        };
        listView.setAdapter(adapter);
    }

    private List<GatherPoint> getData(boolean isUpload) {
        DaoSession daoSession = App.getInstence().getDaoSession();

        QueryBuilder<GatherPoint> qb = daoSession.queryBuilder(GatherPoint.class)
                .where(GatherPointDao.Properties.IsUploaded.eq(isUpload))
                .orderDesc(GatherPointDao.Properties.Collected_at);

        List<GatherPoint> list = qb.list(); // 查出当前对应的数据
        return list;
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v -> finish());
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                LsLog.w(TAG, "checkedId = " + checkedId);
                if (R.id.local_button == checkedId) {
                    // show local data;
                    showData(true);
                } else {
                    // show synced data;
                    showData(false);
                }
            }
        });

        actionSyncAll.setOnClickListener(v->uploadAllLocalData());

    }

    private void uploadAllLocalData() {

        for (GatherPoint point: dataLocalList) {
            uploadLocalDataWithImage(point);
        }
    }

    private void uploadLocalDataWithImage(GatherPoint point) {


        String picPath1 = point.getPicPath1();
        String picPath2 = point.getPicPath2();
        String picPath3 = point.getPicPath3();

        List<File> files = getFiles(picPath1,picPath2,picPath3);

        if (files.size() > 0) {
            // 1. 上传图片，2. 上传数据
            HttpRequest.upLoadImgs(files, new HttpRequest.RespListener<ImageUploadBean>() {
                @Override
                public void onResponse(int status, ImageUploadBean bean) {
                    if (status == 0) {
                        List<ImageData.FileMap> files1 = bean.getData().getFiles();
                        String sss = new Gson().toJson(files1);
                        LsLog.i(TAG, "image files = " + sss);
                        point.setImgs(sss);
                        saveToDb(point);
                        return;
                    }
                    if (bean == null) {
                        LsLog.w(TAG, "save point result: null" + bean);
                    } else {
                        LsLog.w(TAG, "save point result: " + bean.toJson());
                    }

                }
            });
        } else {
            // 上传数据.
            uploadLocalData(point);
        }

    }

    private void uploadLocalData(GatherPoint point) {
        Map<String, Object> param = new HashMap<>();
        param.put("type_id",point.getType_id());
        param.put("name",point.getName());
        param.put("longitude",point.getLongitude());
        param.put("latitude",point.getLatitude());
        param.put("height",point.getHeight());
        param.put("collected_at",point.getCollected_at());
        param.put("desc",point.getDesc());
        param.put("attrs",point.getAttrs());
        param.put("imgs",point.getImgs());

        HttpRequest.postData(Constants.SAVE_COLLECTION_POINT, param, new HttpRequest.RespListener<BaseBean>() {
            @Override
            public void onResponse(int status, BaseBean bean) {
                LsLog.w(TAG, "save point result:" + bean.toJson());
            }
        });

    }

    private void saveToDb(GatherPoint point) {
        App.getInstence().getDaoSession().update(point);
    }

    private List<File> getFiles(String picPath1, String picPath2, String picPath3) {
        List<File> files = new ArrayList<>();

        File file1 = getFileByName(picPath1);
        File file2 = getFileByName(picPath2);
        File file3 = getFileByName(picPath3);
        if (file1 != null) {
            files.add(file1);
        }
        if (file2 != null) {
            files.add(file2);
        }
        if (file3 != null) {
            files.add(file3);
        }
        return files;
    }

    private File getFileByName(String picPath1) {
        if (!TextUtils.isEmpty(picPath1)) {
            File file = new File(picPath1);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }


    private void showData(boolean isLocalData) {
        if (adapter == null) return;
        if (isLocalData) {
            if (dataLocalList != null ) {
                adapter.replaceAll(dataLocalList);
            }
            actionSyncAll.setText("全部上传");
        } else {
            if (dataList != null) {
                adapter.replaceAll( dataList);
            }
            actionSyncAll.setText("下载同步数据");
        }
    }


}
