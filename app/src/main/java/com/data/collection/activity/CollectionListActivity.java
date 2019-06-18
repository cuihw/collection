package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.data.collection.module.BaseBean;
import com.data.collection.module.CollectionImage;
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
import com.google.gson.reflect.TypeToken;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.greendao.query.QueryBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    List<GatherPoint> dataList;       // 同步网络数据

    List<GatherPoint> myCollectDataList;  // 我的数据

    List<CollectType> collectTypes;

    int needUploadSize = 0;

    boolean hasLocalData = false;

    public static void start(Context context) {
        Intent intent = new Intent(context, CollectionListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_list);
        initListener();

        myCollectDataList = getData(true); // local data
        dataList = getData(false);
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

        adapter = new CommonAdapter<GatherPoint>(this, R.layout.item_gather_point, myCollectDataList) {
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

                int showValue = View.VISIBLE;
                if (isShowLocalData) {
                    showValue = View.VISIBLE;
                } else {
                    showValue = View.GONE;
                }

                helper.setVisible(R.id.is_uploaded, showValue);

                ImageView imageView = helper.getView(R.id.icon_type);

                if (thisType != null) {
                    ImageLoader.getInstance().displayImage(thisType.getIcon(), imageView);
                    helper.setText(R.id.type_name, thisType.getName());
                } else {
                    imageView.setImageResource(R.mipmap.ic_popup_dialog_close);
                    helper.setText(R.id.type_name, "没有类型");
                }
                helper.setText(R.id.location, "经度：" + item.getLongitude() + "， 维度：" + item.getLatitude());
                helper.setText(R.id.time, "更新时间: " + item.getUpdated_at());
                // upload_tv
                TextView view = helper.getView(R.id.upload_tv);
                if (item.getIsUploaded()) {
                    view.setVisibility(View.INVISIBLE);
                    helper.setText(R.id.is_uploaded, "已上传");
                } else {
                    hasLocalData = true; // 有本地数据；
                    view.setVisibility(View.VISIBLE);
                    helper.setText(R.id.is_uploaded, "未上传");
                }

                helper.setOnClickListener(R.id.upload_tv, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        needUploadSize = 1;
                        uploadLocalDataWithImage(item);
                    }
                });
            }
        };
        listView.setAdapter(adapter);

        if (myCollectDataList == null || myCollectDataList.size() == 0) {
            noDataTv.setVisibility(View.VISIBLE);
        } else {
            noDataTv.setVisibility(View.INVISIBLE);
        }
    }

    private List<GatherPoint> getData(boolean isMyCollectionData) {

        DaoSession daoSession = App.getInstence().getDaoSession();
        QueryBuilder<GatherPoint> qb = daoSession.queryBuilder(GatherPoint.class)
                .orderDesc(GatherPointDao.Properties.Collected_at)
                .orderDesc(GatherPointDao.Properties.Updated_at);

        if (isMyCollectionData) { //
            String userName = CacheData.getUserName();
            if (TextUtils.isEmpty(userName)) {
                ToastUtil.showTextToast(this, "用户名不正确，请稍后再试");
                finish();
            }

            LsLog.w(TAG, "get my collection data, my name is : " + userName);
            qb.where(GatherPointDao.Properties.Report.eq(userName));
        } else {
            qb.where(GatherPointDao.Properties.IsUploaded.eq(true));
        }

        List<GatherPoint> list = qb.list(); // 查出当前对应的数据
        return list;
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v -> finish());
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.local_button == checkedId) {
                    myCollectDataList = getData(true);
                    // show local data;
                    showData(true);
                    if (myCollectDataList == null || myCollectDataList.size() == 0) {
                        noDataTv.setVisibility(View.VISIBLE);
                    } else {
                        noDataTv.setVisibility(View.INVISIBLE);
                    }
                } else {
                    dataList = getData(false);
                    // show synced data;
                    showData(false);
                    if (dataList == null || dataList.size() == 0) {
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
                AddCollectionActivity.start(CollectionListActivity.this, item);
//                Bundle bundle = new Bundle();
//                bundle.putString("GatherPoint", new Gson().toJson(item));
//                ShowPointActivity.start(CollectionListActivity.this, bundle);
            }
        });
    }

    private void uploadAllLocalData() {
        String label = actionSyncAll.getText().toString().trim();
        if (label.equals("全部上传")) {
            if (!hasLocalData) {
                ToastUtil.showTextToast(this, "我采集的数据已全部上传");
                return;
            }
            if (!hud.isShowing()) {
                hud.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (hud.isShowing()) hud.dismiss();
                    }
                }, 1000 * 60);
            }

            needUploadSize = myCollectDataList.size();
            for (GatherPoint point : myCollectDataList) {
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

    // 请求更新采集点列表数据。
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
                    handlerSyncBean(bean);
                }
                if (status != 0) {
                    hideBusy();
                }
            }
        });
    }


    private void handlerSyncBean(PointListBean bean) {
        // 1. 存储到本地数据库； 2. 继续下载更新更多数据。
        if (bean == null) {
            ToastUtil.showTextToast(this, "同步完成");
            hideBusy();
            return;
        }


        PointListData data = bean.getData();
        if (data == null) {
            ToastUtil.showTextToast(this, "同步完成");
            hideBusy();
            return;
        }

        List<PointData> data1 = data.getData();
        if (data1 == null) {
            ToastUtil.showTextToast(this, "同步完成");
            hideBusy();
            return;
        }

        for (PointData pd : data1) {
            GatherPoint gatherPoint = pd.getGatherPoint();
            insertToDb(gatherPoint);
            dataList.add(0, gatherPoint);
        }

        Collections.sort(dataList); //
        // TODO:下载结束  隐藏忙图标 刷新显示数据。
        delayShowData(2000);

        int total = Integer.parseInt(data.getTotal());
        int size = data1.size();
        int per_page = Integer.parseInt(data.getPer_page());

        if (total < per_page || size < per_page) {
            // 下载完毕
            ToastUtil.showTextToast(this, "同步完成");
            hideBusy();
        } else {
            requestSyncData(1);
        }

    }

    private void delayShowData(int mills) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showData(false);
            }
        }, mills);
    }


    private void hideBusy() {
        if (hud.isShowing()) {
            hud.dismiss();
        }
    }

    private void uploadLocalDataWithImage(GatherPoint point) {
        if (point == null) return;

        List<File> files = getFiles(point);

        if (files.size() > 0) {
            // 1. 上传图片，2. 上传数据
            HttpRequest.upLoadImgs(files, new HttpRequest.RespListener<ImageUploadBean>() {
                @Override
                public void onResponse(int status, ImageUploadBean bean) {
                    LsLog.w(TAG, "upload image result..");
                    if (status == 0) {
                        List<ImageData.FileMap> files1 = bean.getData().getFiles();
                        String imageData = new Gson().toJson(files1);
                        LsLog.i(TAG, "image files = " + imageData);
                        point.setImgs(imageData);
                        saveToDb(point);
                    } else if (status == HttpRequest.NET_ERROR) {
                        ToastUtil.showTextToast(CollectionListActivity.this, "网络错误, 无法上传");
                    }

                    // 有图片的情况下，要等图片上传返回结果后再上传采集点信息。
                    uploadLocalData(point);
                }
            });
        } else {
            // 上传数据.
            uploadLocalData(point);
        }

    }


    private void uploadLocalData(GatherPoint point) {

        if (point == null) return;
        LsLog.w(TAG, "uploadLocalData : " + point.getName());

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

        HttpRequest.postData(null,
                Constants.SAVE_COLLECTION_POINT, TAG, param, new HttpRequest.RespListener<String>() {
                    @Override
                    public void onResponse(int status, String bean) {
                        if (status == 0) {
                            try {
                                BaseBean basebean = new Gson().fromJson(bean, BaseBean.class);
                                LsLog.w(TAG, "save point result:" + basebean.toJson());
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
                                    // myCollectDataList.remove(point);
                                    dataList.add(point);
                                    adapter.replaceAll(myCollectDataList);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        int size = myCollectDataList.size();
                        LsLog.w(TAG, "upload size: " + size);
                        needUploadSize--;
                        LsLog.w(TAG, "needUploadSize = " + needUploadSize);

                        if (needUploadSize == 0) {
                            if (hud.isShowing()) {
                                hud.dismiss();
                            }
                        }
                    }
                });

    }

    private void saveToDb(GatherPoint point) {
        App.getInstence().getDaoSession().update(point);
    }

    private void insertToDb(GatherPoint point) {
        String id = point.getId();
        DaoSession daoSession = App.getInstence().getDaoSession();
        QueryBuilder<GatherPoint> qb = daoSession.queryBuilder(GatherPoint.class)
                .where(GatherPointDao.Properties.Id.eq(id));

        List<GatherPoint> list = qb.list(); // 查出当前对应的数据
        long insert;
        if (list != null && list.size() > 0) {
            GatherPoint point1 = list.get(0);
            insert = point1.getOffline_id();
            point.setOffline_id(insert);
            App.getInstence().getDaoSession().update(point);
        } else {
            insert = App.getInstence().getDaoSession().insert(point);
        }
        LsLog.w(TAG, "insertToDb insertid = " + insert);
    }

    private List<File> getFiles(GatherPoint point) {
        List<File> files = new ArrayList<>();
        String imagesdata = point.getPicPath1();
        if (!TextUtils.isEmpty(imagesdata)) {
            Type type = new TypeToken<List<CollectionImage>>() {
            }.getType();
            List<CollectionImage> imagelists = new Gson().fromJson(imagesdata, type);

            for (CollectionImage image : imagelists) {
                File fileByName = getFileByName(image.filename);
                if (fileByName != null) {
                    files.add(fileByName);
                }
            }
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

    boolean isShowLocalData = true;

    private void showData(boolean isLocalData) {
        isShowLocalData = isLocalData;
        if (adapter == null) return;
        if (isLocalData) {
            if (myCollectDataList != null) {
                adapter.replaceAll(myCollectDataList);
            }
            actionSyncAll.setText("全部上传");
        } else {
            if (dataList != null) {
                adapter.replaceAll(dataList);
            }
            actionSyncAll.setText("下载同步数据");
        }
    }

    @Override
    protected void onDestroy() {
        HttpRequest.cancleRequest(TAG);
        HttpRequest.cancleRequest("upLoadImgs");
        super.onDestroy();
    }
}
