package com.data.collection.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.classic.adapter.BaseAdapterHelper;
import com.classic.adapter.CommonAdapter;
import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.R;
import com.data.collection.adapter.PointTypeAdapter;
import com.data.collection.data.CacheData;
import com.data.collection.data.greendao.DaoSession;
import com.data.collection.data.greendao.GatherPoint;
import com.data.collection.dialog.ButtomDialogView;
import com.data.collection.module.Attrs;
import com.data.collection.module.CollectType;
import com.data.collection.module.CollectionImage;
import com.data.collection.module.UserInfoBean;
import com.data.collection.util.DateUtils;
import com.data.collection.util.FileUtils;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.AttributionView;
import com.data.collection.view.TitleView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class AddCollectionActivity extends BaseActivity {

    private static final String TAG = "AddCollectionActivity";
    private static final int REQUEST_PERMISSIONS = 1;

    private static final String TAKE_PICTURE = "action.take.picture";
    private static final String SAVE_POINT = "action.save.point";
    private static final int REQUEST_CODE_PIC_PHOTO = 2;
    private static final int REQUEST_CODE_TAKE_PHOTO = 3;

    private static CollectType collectType;

    String takeCameraFilename; // 照相机照片保存路径

    List<CollectionImage> imageList = new ArrayList<>();  // 采集点照片

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.longitude_tv)
    TextView longitudeTv;

    @BindView(R.id.type_spinner)
    Spinner typeSpinner;

    @BindView(R.id.reset_layout)
    LinearLayout resetLayout;

    @BindView(R.id.attribution_layout)
    LinearLayout attributionLayout;

    @BindView(R.id.attribution_layout1)
    LinearLayout attributionLayout1;

    @BindView(R.id.save_layout)
    LinearLayout saveLayout;

    AttributionView attrsView; // propreties

    List<CollectType> projectTypes;

    @BindView(R.id.laititude_tv)
    TextView laititudeTv;
    @BindView(R.id.time_tv)
    TextView timeTv;
    @BindView(R.id.altitude_tv)
    TextView altitudeTv;

    @BindView(R.id.name)
    TextView nameTv;
    @BindView(R.id.comments_tv)
    TextView commentsTv;

    @BindView(R.id.grid_view)
    GridView gridView;

    @BindView(R.id.bottom_layout)
    LinearLayout bottomLayout;

    CommonAdapter<CollectionImage> adapter;

    static GatherPoint gatherPoint;

    public static void start(Context context, GatherPoint gatherPoint){
        Intent intent = new Intent(context, AddCollectionActivity.class);
        AddCollectionActivity.gatherPoint = gatherPoint;
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_collection);
        initView();
        initListener();
    }

    private void showPoint(GatherPoint gatherPoint) {
        if (gatherPoint == null) return;
        if (gatherPoint.getIsUploaded()) {
            // 不让编辑
            // 隐藏下面的功能按钮
            bottomLayout.setVisibility(View.GONE);
        }
        String type_id = gatherPoint.getType_id();
        CollectType collectType = CacheData.getTypeMaps().get(type_id);
        if (collectType != null) {
            typeSpinner.setSelection(collectType.getIndex());
        } else {
            LsLog.w(TAG, "collectType == null");
        }
        nameTv.setText(gatherPoint.getName());
        nameTv.setEnabled(false);
        longitudeTv.setText(gatherPoint.getLongitude());
        laititudeTv.setText(gatherPoint.getLatitude());
        altitudeTv.setText(gatherPoint.getHeight());
        timeTv.setText(gatherPoint.getCollected_at());
        commentsTv.setText(gatherPoint.getDesc());

        createAttrsView(collectType.getIndex());

        // attrsView.setGatherPoint(gatherPoint);

        if (gatherPoint.getIsUploaded()) { // 加载网络图片
            String imgs = gatherPoint.getImgs();
            if (!TextUtils.isEmpty(imgs)) {
                Type type =new TypeToken<List<CollectionImage>>(){}.getType();
                List<CollectionImage> list = new Gson().fromJson(imgs, type);
                for (CollectionImage image:list) {
                    image.isUrlImage = true;
                }
                imageList.clear();
                imageList.addAll(list);
            }
        } else { // 本地图片
            // gatherPoint
            String picPath1 = gatherPoint.getPicPath1();
            if (!TextUtils.isEmpty(picPath1)) {
                Type type =new TypeToken<List<CollectionImage>>(){}.getType();
                List<CollectionImage> list = new Gson().fromJson(picPath1, type);
                imageList.clear();
                imageList.addAll(list);
            }

        }
        adapter.replaceAll(imageList);

        if (imageList.size() == 0) {
            gridView.setVisibility(View.GONE);
        }

    }

    private void initView() {
        initSpinner();

        fillLongitudeAndLaititude();

        if (hasProjectInfo()) {
            attrsView = new AttributionView(this);
            createAttrsView(0);
        }

        adapter = new CommonAdapter<CollectionImage>(this, R.layout.item_gather_point_img) {
            @Override
            public void onUpdate(BaseAdapterHelper helper, CollectionImage item, int position) {
                ImageView imageview = helper.getView(R.id.image);
                ImageView delete = helper.getView(R.id.delete_image);

                if (item.isUrlImage) {
                    ImageLoader.getInstance().displayImage(item.url, imageview);
                    delete.setVisibility(View.GONE);
                } else if (TextUtils.isEmpty(item.filename)) {
                    imageview.setImageBitmap(null);
                    delete.setVisibility(View.GONE);
                    imageview.setOnClickListener( v-> addPicture(position));
                } else {
                    delete.setVisibility(View.VISIBLE);
                    Uri uri = Uri.fromFile(new File(item.filename));
                    imageview.setImageURI(uri);
                    delete.setOnClickListener(v->deleteImage(position));
                }
            }
        };
        imageList.add(new CollectionImage());
        adapter.replaceAll(imageList);
        gridView.setAdapter(adapter);

        if (gatherPoint != null) {
            showPoint(gatherPoint);
        }
    }



    private void deleteImage(int position) {
        imageList.remove(position);
        if (position == 2) {
            imageList.add(new CollectionImage());
        }
        adapter.replaceAll(imageList);
    }

    private void addPicture(int position) {
        // 点击最后一个，是+ 号的才增加图片
        int size = imageList.size();
        if (size - 1 == position) {
            initPermission(TAKE_PICTURE);
        }
    }

    Location location;
    private void fillLongitudeAndLaititude() {

        location = LocationController.getInstance().getLocation();
        if (location == null) {
            ToastUtil.showTextToast(this, "Gps定位失败，请打开定位后再采集");
            return;
        }
        DecimalFormat df = new DecimalFormat("#.0000000");
        //String str = df.format(d);

        longitudeTv.setText(df.format(location.getLongitude()));
        laititudeTv.setText(df.format(location.getLatitude()));
        altitudeTv.setText(String.format("%.1f", location.getAltitude()));

        long time = System.currentTimeMillis();
        timeTv.setText(DateUtils.formatTime(time, DateUtils.fmtYYYYMMDDhhmmss));
    }

    private void createAttrsView(int i) {
        attrsView.clearView();
        attributionLayout.removeAllViews();
        if (projectTypes != null && projectTypes.size() > i) {
            CollectType types = projectTypes.get(i);
            List<Attrs> attrs = types.getAttrs();
            attrsView.setViewAttri(attrs);
            attributionLayout.addView(attrsView);
        } else {
            ToastUtil.showTextToast(this, "项目中的采集类型为空，请联系管理员配置");
        }

        if (attrsView.getAttrViewList().size() == 0) {
            // 隐藏属性
            attributionLayout1.setVisibility(View.GONE);
        } else {
            // 显示
            attributionLayout1.setVisibility(View.VISIBLE);
        }

        if (gatherPoint != null) {
            attrsView.setGatherPoint(gatherPoint);
        }

    }
    private void initSpinner() {
        try {

            if (!CacheData.isValidProject()) {
                ToastUtil.showTextToast(this, getString(R.string.no_project_data));
                return;
            }
            UserInfoBean userInfoBean = CacheData.getUserInfoBean();
            projectTypes = userInfoBean.getData().getProject().getTypes();

            if (projectTypes == null || projectTypes.size() == 0) {
                ToastUtil.showTextToast(this, getString(R.string.no_project_data));
                return;
            }

            PointTypeAdapter pointAdapter = new PointTypeAdapter(this, projectTypes);

            typeSpinner.setAdapter(pointAdapter);
            if (collectType != null) {
                int i = projectTypes.indexOf(collectType);
                if (i != -1) {
                    typeSpinner.setSelection(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->finish());

        resetLayout.setOnClickListener(v->resetData());

        saveLayout.setOnClickListener(v->initPermission(SAVE_POINT));//  动态请求权限);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                createAttrsView(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void resetData() {
        nameTv.setText("");
        commentsTv.setText("");
        if (attrsView != null) {
            attrsView.clearViewData();
        }
        imageList.clear();
        imageList.add(new CollectionImage());
    }


    private void savePoint() {
        if (!hasProjectInfo()) return;

        String pointName = nameTv.getText().toString().trim();

        CollectType selectedItem = (CollectType)typeSpinner.getSelectedItem();

        if (TextUtils.isEmpty(pointName)) {
            ToastUtil.showTextToast(this, "请输入采集点名称");
            return;
        }
        CollectType attrsValue = attrsView.getAttrsValue(selectedItem);

        if (attrsValue == null) {
            return;
        }

        collectType = attrsValue;

        LsLog.w(TAG, "attrs: " + new Gson().toJson(attrsValue.getAttrs()));
        String des = commentsTv.getText().toString().trim(); // 备注

        if (gatherPoint == null) {
            gatherPoint = new GatherPoint();
        }

        gatherPoint.setName(pointName);
        gatherPoint.setType_id(selectedItem.getId());
        gatherPoint.setDesc(des);
        gatherPoint.setReport(CacheData.getUserName());

        String s1 = longitudeTv.getText().toString();
        String s2 = laititudeTv.getText().toString();
        String s3 = altitudeTv.getText().toString();
        if (TextUtils.isEmpty(s1) || TextUtils.isEmpty(s2)) {
            ToastUtil.showTextToast(this, "定位失败，请打开GPS，等待定位");
            return;
        } else {
            gatherPoint.setLongitude(s1);
            gatherPoint.setLatitude(s2);
            gatherPoint.setHeight(s3);
        }

        gatherPoint.setCollected_at(timeTv.getText().toString());
        gatherPoint.setUpdated_at(timeTv.getText().toString());

        gatherPoint.setAttrs(new Gson().toJson(attrsValue.getAttrs()));

        if (imageList.size() > 0) {
            String s = new Gson().toJson(imageList);
            gatherPoint.setPicPath1(s);
        }

        DaoSession daoSession =  App.getInstence().getDaoSession();
        long id = daoSession.insertOrReplace(gatherPoint);
        Log.i(TAG, "save id = " + id);
        if(id > 0) {
            ToastUtil.showTextToast(this, "保存成功。");
            finish();
        }
    }

    private void takePicture() {
        ButtomDialogView dialogView = new ButtomDialogView(this, R.layout.popup_dialog_take_picture);
        TextView camera = dialogView.getView().findViewById(R.id.action_camera);
        TextView gallary = dialogView.getView().findViewById(R.id.action_gallary);
        TextView cancel = dialogView.getView().findViewById(R.id.cancel_tv);

        camera.setOnClickListener(
                v->{callCamera();dialogView.dismiss();});
        gallary.setOnClickListener(
                v->{callGallary();dialogView.dismiss();});
        cancel.setOnClickListener(v->dialogView.dismiss());
        dialogView.show();
        camera.setText("拍摄照片");
        gallary.setText("从手机相册选择");
        cancel.setText("取消");
    }

    private void callGallary() {
        if (!hasProjectInfo()) return;

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_CODE_PIC_PHOTO);
    }
    private void callCamera() {
        if (!hasProjectInfo()) return;
        takeCameraFilename = FileUtils.getFileDir() + "zw" + System.currentTimeMillis() + ".jpg";
        // 如果没有设置图片名称，则根据当前系统时间设置默认的图片名称

        File takePhotoTempFile = new File(takeCameraFilename);

        File f = new File(FileUtils.getSdcardDir(), FileUtils.APP_FOLDER_NAME);
        if (!f.exists()) {
            try {f.mkdir();} catch (Exception e) {e.printStackTrace();}
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(takePhotoTempFile));
        startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_PHOTO);
    }

    private void initPermission(String action) {
        String[] permissions = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        //检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 之前拒绝了权限，但没有点击 不再询问 这个时候让它继续请求权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, "用户曾拒绝打开相机权限", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
            } else {
                //注册相机权限
                ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
            }
        } else {
            if (TAKE_PICTURE.equals(action)) {
                takePicture();
            } else if (SAVE_POINT.equals(action)) {
                savePoint();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            switch (requestCode) {
                case REQUEST_CODE_PIC_PHOTO:
                    Uri data1 = data.getData();
                    takeCameraFilename = FileUtils.getFilePathByUri(this, data1);
                    LsLog.i(TAG, "onPickPhotoResult: " + takeCameraFilename);
                    addImageFile();
                    break;
                case REQUEST_CODE_TAKE_PHOTO:
                    if (takeCameraFilename != null) {
                        addImageFile();
                    }
                    break;
            }
        }
    }

    private void addImageFile(){
        CollectionImage image = new CollectionImage();
        image.filename = takeCameraFilename;
        int size = imageList.size();

        if (size == 3) {
            imageList.remove(2);
            imageList.add(image);
        } else {
            imageList.add(size -1, image);
        }
        adapter.replaceAll(imageList);
    }

    boolean hasProjectInfo (){
        UserInfoBean userInfoBean = CacheData.getUserInfoBean();
        if (userInfoBean == null) {
            ToastUtil.showTextToast(this, Constants.NO_PROJECT_INFO);
            return false;
        }
        return true;
    }

}
