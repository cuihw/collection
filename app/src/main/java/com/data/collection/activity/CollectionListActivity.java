package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
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
import com.data.collection.module.ImageData;
import com.data.collection.module.ImageUploadBean;
import com.data.collection.module.PointData;
import com.data.collection.module.PointListBean;
import com.data.collection.module.CollectType;
import com.data.collection.module.PointListData;
import com.data.collection.module.UserInfoBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.TitleView;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.greendao.query.QueryBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

// 采集点列表

public class CollectionListActivity extends BaseActivity {

    private static final String TAG = "CollectionListActivity";

    KProgressHUD hud;

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

    @BindView(R.id.no_data_tv)
    TextView noDataTv;

    CommonAdapter<GatherPoint> adapter;

    List<GatherPoint> dataList;

    List<GatherPoint> dataLocalList;

    List<CollectType> collectTypes;

    public static void start(Context context) {
        Intent intent = new Intent(context, CollectionListActivity.class);
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

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false);
    }

    private void initView() {
        localButton.setChecked(true);

        UserInfoBean userInfoBean = CacheData.getUserInfoBean();
        if (userInfoBean == null) {
            ToastUtil.showTextToast(this, Constants.NO_PROJECT_INFO);
            return;
        }
        collectTypes = userInfoBean.getData().getProject().getTypes();

        adapter = new CommonAdapter<GatherPoint>(this, R.layout.item_gather_point, dataLocalList) {
            @Override
            public void onUpdate(BaseAdapterHelper helper, GatherPoint item, int position) {
                helper.setText(R.id.name, item.getName());
                String type_id = item.getType_id();
                CollectType thisType = null;
                for (CollectType type : collectTypes) {
                    if (type.getId().equals(type_id)) {
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
                // upload_tv
                TextView view = helper.getView(R.id.upload_tv);
                if (item.getIsUploaded()) {
                    view.setVisibility(View.INVISIBLE);
                    // view.setText("查看");
                } else {
                    view.setVisibility(View.VISIBLE);
                }

                helper.setOnClickListener(R.id.upload_tv, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadLocalDataWithImage(item);
                    }
                });
            }
        };
        listView.setAdapter(adapter);


        if (dataLocalList == null || dataLocalList.size() == 0 ) {
            noDataTv.setVisibility(View.VISIBLE);
        } else {
            noDataTv.setVisibility(View.INVISIBLE);
        }
    }

    private List<GatherPoint> getData(boolean isUpload) {
        DaoSession daoSession = App.getInstence().getDaoSession();

        QueryBuilder<GatherPoint> qb = daoSession.queryBuilder(GatherPoint.class)
                .where(GatherPointDao.Properties.IsUploaded.eq(isUpload))
                .orderDesc(GatherPointDao.Properties.Updated_at);

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
                    if (dataLocalList == null || dataLocalList.size() == 0 ) {
                        noDataTv.setVisibility(View.VISIBLE);
                    } else {
                        noDataTv.setVisibility(View.INVISIBLE);
                    }
                } else {
                    // show synced data;
                    showData(false);
                    if (dataList == null || dataList.size() == 0 ) {
                        noDataTv.setVisibility(View.VISIBLE);
                    } else {
                        noDataTv.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        actionSyncAll.setOnClickListener(v -> uploadAllLocalData());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GatherPoint item = adapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putString("GatherPoint", new Gson().toJson(item));
                ShowPointActivity.start(CollectionListActivity.this, bundle);
            }
        });
    }

    private void uploadAllLocalData() {
        String label = actionSyncAll.getText().toString().trim();
        if (label.equals("全部上传")) {
            for (GatherPoint point : dataLocalList) {
                uploadLocalDataWithImage(point);
            }
        } else {
            // 下载
            // TODO:开始下载  显示忙图标
            if (!hud.isShowing()) {
                hud.show();
            }
            requestSyncData(1);
        }
    }

    private void requestSyncData(int page) {

        Map<String, Object> param = new HashMap<>();

        if (dataList.size() > 0) {
            GatherPoint gatherPoint = dataList.get(0);
            if (gatherPoint != null && gatherPoint.getUpdated_at() != null) {
                String updated_at = gatherPoint.getUpdated_at();
                param.put("updated_at", updated_at);
                param.put("page", page);
            }
            param.put("page", page);
        }


        HttpRequest.postData(this, Constants.GET_COLLECTION_POINT, param, new HttpRequest.RespListener<PointListBean>() {
            @Override
            public void onResponse(int status, PointListBean bean) {
                if (bean != null) {
                    LsLog.w(TAG, "get collection response: " + bean.toJson());
                    handerSyncBean(bean);
                }
                hideBusy();
            }

        });
    }


    private void handerSyncBean(PointListBean bean) {
        // 1. 存储到本地数据库； 2. 继续下载更新更多数据。
        PointListData data = bean.getData();
        List<PointData> data1 = data.getData();

        for (PointData pd: data1) {
            GatherPoint gatherPoint = pd.getGatherPoint();
            insertToDb(gatherPoint);
        }
        hideBusy();
        // TODO:下载结束  隐藏忙图标
    }

    private void hideBusy(){
        if (hud.isShowing()) {
            hud.dismiss();
        }
    }

    private void uploadLocalDataWithImage(GatherPoint point) {
        String picPath1 = point.getPicPath1();
        String picPath2 = point.getPicPath2();
        String picPath3 = point.getPicPath3();
        List<File> files = getFiles(picPath1, picPath2, picPath3);

        if (files.size() > 0) {
            // 1. 上传图片，2. 上传数据
            HttpRequest.upLoadImgs(files, new HttpRequest.RespListener<ImageUploadBean>() {
                @Override
                public void onResponse(int status, ImageUploadBean bean) {
                    if (status == 0) {

                        List<ImageData.FileMap> files1 = bean.getData().getFiles();
                        String sss = new Gson().toJson(files1);
                        LsLog.i(TAG, "image files = " + sss + "; result: " + bean.toJson());
                        point.setImgs(sss);
                        saveToDb(point);
                        uploadLocalData(point);
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
        param.put("type_id", point.getType_id());
        param.put("name", point.getName());
        param.put("longitude", point.getLongitude());
        param.put("latitude", point.getLatitude());
        param.put("height", point.getHeight());
        param.put("collected_at", point.getCollected_at());
        if (point.getDesc() != null)
            param.put("desc", point.getDesc());
        try {
            if (point.getAttrs() != null) {
                String attrs = point.getAttrs();
                JSONArray jsonArray = new JSONArray(attrs);
                param.put("attrs", jsonArray);
            }

            if (point.getImgs() != null) {
                JSONArray jsonArray = new JSONArray(point.getImgs());
                param.put("imgs", jsonArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest.postData(null, Constants.SAVE_COLLECTION_POINT, param, new HttpRequest.RespListener<String>() {
            @Override
            public void onResponse(int status, String bean) {
                LsLog.w(TAG, "save point result:" + bean);
                try {
                    JSONObject json = new JSONObject(bean);
                    String code = json.getString("code");
                    String msg = json.getString("msg");
                    ToastUtil.showTextToast(CollectionListActivity.this, msg);

                    if (code.equals("1")) {
                        JSONObject data = json.getJSONObject("data");
                        point.setUpdated_at(data.getString("updated_at"));
                        point.setId(data.getString("id"));
                        point.setIsUploaded(true);
                        saveToDb(point); // 更新数据库
                        // 更新本地列表
                        dataLocalList.remove(point);
                        dataList.add(point);
                        adapter.replaceAll(dataLocalList);
                    } else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

    }

    private void saveToDb(GatherPoint point) {
        App.getInstence().getDaoSession().update(point);
    }
    private void insertToDb(GatherPoint point) {
        long insert = App.getInstence().getDaoSession().insertOrReplace(point);
        LsLog.w(TAG, "insertid = " + insert);
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
            if (dataLocalList != null) {
                adapter.replaceAll(dataLocalList);
            }
            actionSyncAll.setText("全部上传");
        } else {
            if (dataList != null) {
                adapter.replaceAll(dataList);
            }
            actionSyncAll.setText("下载同步数据");
        }
    }


}
