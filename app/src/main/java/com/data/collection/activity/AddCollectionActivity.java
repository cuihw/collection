package com.data.collection.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.data.collection.util.BitmapUtil;
import com.data.collection.util.DateUtils;
import com.data.collection.util.FileUtils;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.AttributionView;
import com.data.collection.view.TitleView;
import com.google.gson.Gson;

import java.io.File;
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

    @BindView(R.id.image1)
    ImageView imageview1;
    @BindView(R.id.image2)
    ImageView imageview2;
    @BindView(R.id.image3)
    ImageView imageview3;

    @BindView(R.id.delete_image1)
    ImageView deleteImage1;
    @BindView(R.id.delete_image2)
    ImageView deleteImage2;
    @BindView(R.id.delete_image3)
    ImageView deleteImage3;

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

    @BindView(R.id.image_layout2)
    RelativeLayout imageLayout2;

    // 添加图片目前先用这种方式，后面使用GridView优化
    @BindView(R.id.image_layout3)
    RelativeLayout imageLayout3;

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
        initView();
        initListener();
    }

    private void showImageLayout(){
        int size = imageList.size();
        switch (size) {
            case 0:
                imageLayout2.setVisibility(View.INVISIBLE);
                imageLayout3.setVisibility(View.INVISIBLE);
                break;
            case 1:
                imageLayout2.setVisibility(View.VISIBLE);
                imageLayout3.setVisibility(View.INVISIBLE);
                break;
            case 2:
                imageLayout2.setVisibility(View.VISIBLE);
                imageLayout3.setVisibility(View.VISIBLE);
                break;
            case 3:
                imageLayout2.setVisibility(View.VISIBLE);
                imageLayout3.setVisibility(View.VISIBLE);
                break;
        }

        showImageInUI();
    }

    private void initView() {
        deleteImage1.setVisibility(View.INVISIBLE);
        deleteImage2.setVisibility(View.INVISIBLE);
        deleteImage3.setVisibility(View.INVISIBLE);
        initSpinner();

        fillLongitudeAndLaititude();

        if (hasProjectInfo()) {
            attrsView = new AttributionView(this);
            createAttrsView(0);
        }
    }

    Location location;
    private void fillLongitudeAndLaititude() {
        location = LocationController.getInstance().getLocation();
        if (location == null) {
            ToastUtil.showTextToast(this, "Gps定位失败，请打开定位后再采集");
            return;
        }
        longitudeTv.setText("经度: \n" + location.getLongitude());
        laititudeTv.setText("纬度: \n" + location.getLatitude());
        altitudeTv.setText("高度: \n" + location.getAltitude());

        long time = System.currentTimeMillis();
        timeTv.setText("采集时间: " + DateUtils.formatTime(time, DateUtils.fmtYYYYMMDDhhmmss));
    }

    private void createAttrsView(int i) {
        attrsView.clearView();
        attributionLayout.removeAllViews();
        if (projectTypes != null) {
            CollectType types = projectTypes.get(i);
            List<Attrs> attrs = types.getAttrs();
            attrsView.setViewAttri(attrs);
            attributionLayout.addView(attrsView);
        }

        if (attrsView.getAttrViewList().size() == 0) {
            // 隐藏属性
            attributionLayout1.setVisibility(View.GONE);
        } else {
            // 显示
            attributionLayout1.setVisibility(View.VISIBLE);
        }
    }
    private void initSpinner() {
        try {
            UserInfoBean userInfoBean = CacheData.getUserInfoBean();
            projectTypes = userInfoBean.getData().getProject().getTypes();
            PointTypeAdapter pointAdapter = new PointTypeAdapter(this, projectTypes);
            typeSpinner.setAdapter(pointAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->finish());
        resetLayout.setOnClickListener(v->resetData());//  动态请求权限);

        saveLayout.setOnClickListener(v->initPermission(SAVE_POINT));
        imageview1.setOnClickListener(v->{
            if (imageList.size() == 0) {
                initPermission(TAKE_PICTURE);
            }
        });
        imageview2.setOnClickListener(v->{
            if (imageList.size() == 1) {
                initPermission(TAKE_PICTURE);
            }
        });
        imageview3.setOnClickListener(v->{
            if (imageList.size() == 2) {
                initPermission(TAKE_PICTURE);
            }
        });

        deleteImage1.setOnClickListener(v->{
            if (imageList.size() > 0) {
                imageList.remove(0);
            }
            showImageLayout();
        });
        deleteImage2.setOnClickListener(v->{
            if (imageList.size() > 1) {
                imageList.remove(1);
            }

            showImageLayout();

        });
        deleteImage3.setOnClickListener(v->{
            if (imageList.size() > 2) {
                imageList.remove(2);
            }
            showImageLayout();
        });

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

    }

    GatherPoint gatherPoint;
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

        LsLog.w(TAG, "attrs: " + new Gson().toJson(attrsValue.getAttrs()));
        String des = commentsTv.getText().toString().trim(); // 备注

        if (gatherPoint == null) {
            gatherPoint = new GatherPoint();
        }

        gatherPoint.setName(pointName);
        gatherPoint.setType_id(selectedItem.getId());
        gatherPoint.setDesc(des);
        gatherPoint.setReport(CacheData.getUserName());
        if (location != null) {
            gatherPoint.setLongitude("" + location.getLongitude());
            gatherPoint.setLatitude("" + location.getLatitude());
            gatherPoint.setHeight("" + location.getAltitude());
            long time = System.currentTimeMillis();
            gatherPoint.setCollected_at(DateUtils.formatTime(time, DateUtils.fmtYYYYMMDDhhmmss));
        } else {
            ToastUtil.showTextToast(this, "定位失败，请打开GPS，等待定位");
        }
        gatherPoint.setAttrs(new Gson().toJson(attrsValue.getAttrs()));

        if (imageList.size() == 1) {
            gatherPoint.setPicPath1(imageList.get(0).filename);
        } else if(imageList.size() == 2) {
            gatherPoint.setPicPath1(imageList.get(0).filename);
            gatherPoint.setPicPath2(imageList.get(1).filename);
        }else if(imageList.size() == 3) {
            gatherPoint.setPicPath1(imageList.get(0).filename);
            gatherPoint.setPicPath2(imageList.get(1).filename);
            gatherPoint.setPicPath3(imageList.get(2).filename);
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
                    showImageLayout();
                    break;
                case REQUEST_CODE_TAKE_PHOTO:
                    if (takeCameraFilename != null) {
                        addImageFile();
                        showImageLayout();
                    }
                    break;
            }
        }
    }

    private void addImageFile(){
        CollectionImage image = new CollectionImage();
        image.filename = takeCameraFilename;
        imageList.add(image);
    }

    private void showImageInUI() {
        int size = imageList.size();
        if (size == 0) {
            imageview1.setImageBitmap(null);
            deleteImage1.setVisibility(View.INVISIBLE);

            imageview2.setImageBitmap(null);
            deleteImage2.setVisibility(View.INVISIBLE);

            imageview3.setImageBitmap(null);
            deleteImage3.setVisibility(View.INVISIBLE);

        } else if (size == 1) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageList.get(0).filename);
            imageview1.setImageBitmap(bitmap);
            deleteImage1.setVisibility(View.VISIBLE);

            imageview2.setImageBitmap(null);
            deleteImage2.setVisibility(View.INVISIBLE);

            imageview3.setImageBitmap(null);
            deleteImage3.setVisibility(View.INVISIBLE);

        } else if (size == 2){

            Bitmap bitmap = BitmapFactory.decodeFile(imageList.get(0).filename);
            imageview1.setImageBitmap(bitmap);
            deleteImage1.setVisibility(View.VISIBLE);
            bitmap = BitmapFactory.decodeFile(imageList.get(1).filename);
            imageview2.setImageBitmap(bitmap);
            deleteImage2.setVisibility(View.VISIBLE);

            imageview3.setImageBitmap(null);
            deleteImage3.setVisibility(View.INVISIBLE);

        } else if (size == 3){

            Bitmap bitmap = BitmapFactory.decodeFile(imageList.get(0).filename);
            imageview1.setImageBitmap(bitmap);
            deleteImage1.setVisibility(View.VISIBLE);

            bitmap = BitmapFactory.decodeFile(imageList.get(1).filename);
            imageview2.setImageBitmap(bitmap);
            deleteImage2.setVisibility(View.VISIBLE);

            bitmap = BitmapFactory.decodeFile(imageList.get(2).filename);
            imageview3.setImageBitmap(bitmap);
            deleteImage3.setVisibility(View.VISIBLE);
        }
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
