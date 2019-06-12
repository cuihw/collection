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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.data.collection.data.greendao.GatherPointDao;
import com.data.collection.dialog.ButtomDialogView;
import com.data.collection.module.Attrs;
import com.data.collection.module.CollectType;
import com.data.collection.module.ImageData;
import com.data.collection.module.ImageUploadBean;
import com.data.collection.module.PointData;
import com.data.collection.module.PointListBean;
import com.data.collection.module.PointListData;
import com.data.collection.module.UserInfoBean;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.BitmapUtil;
import com.data.collection.util.DateUtils;
import com.data.collection.util.FileUtils;
import com.data.collection.util.LocationController;
import com.data.collection.util.LsLog;
import com.data.collection.util.ToastUtil;
import com.data.collection.view.AttributionView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

// 采集点详情

public class ShowPointActivity extends BaseActivity {

    private static final String TAG = "ShowPointActivity";

    private static final int REQUEST_PERMISSIONS = 1;

    private static final String TAKE_PICTURE = "action.take.picture";
    private static final String SAVE_POINT = "action.save.point";
    private static final int REQUEST_CODE_PIC_PHOTO = 2;
    private static final int REQUEST_CODE_TAKE_PHOTO = 3;

    String takeCameraFilename; // 照相机照片保存路径

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.longitude_tv)
    TextView longitudeTv;

    @BindView(R.id.type_spinner)
    Spinner typeSpinner;

    @BindView(R.id.camera_layout)
    LinearLayout cameraLayout;

    @BindView(R.id.attribution_layout)
    LinearLayout attributionLayout;

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
    EditText nameET;
    @BindView(R.id.comments_tv)
    TextView commentsTv;

    @BindView(R.id.bottom_layout)
    LinearLayout bottomLayout;


    GatherPoint gatherPoint;

    int typeIndex = 0;

    boolean isUploaded;

    public static void start(Context context, Bundle bundle){
        Intent intent = new Intent(context, ShowPointActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_point);

        getPointData();
        initView();
        initListener();
    }

    private void getPointData() {
        Bundle extras = getIntent().getExtras();
        if (extras == null)  return;

        String gatherP = extras.getString("GatherPoint");
        if (TextUtils.isEmpty(gatherP)) return;

        gatherPoint = new Gson().fromJson(gatherP, GatherPoint.class);
        isUploaded = gatherPoint.getIsUploaded();
    }

    private void initView() {
        titleView.getTitleTv().setText("采集点信息");

        nameET.setText(gatherPoint.getName());
        if (isUploaded) nameET.setEnabled(false);

        commentsTv.setText(gatherPoint.getDesc());
        if (isUploaded) commentsTv.setEnabled(false);

        deleteImage1.setVisibility(View.INVISIBLE);
        deleteImage2.setVisibility(View.INVISIBLE);
        deleteImage3.setVisibility(View.INVISIBLE);
        initSpinner();

        fillLongitudeAndLaititude();

        if (hasProjectInfo()) {
            attrsView = new AttributionView(this);
            createAttrsView(typeIndex);
        }


        String imgs = gatherPoint.getImgs();
        String picPath1 = gatherPoint.getPicPath1();
        String picPath2 = gatherPoint.getPicPath2();
        String picPath3 = gatherPoint.getPicPath3();
        if (!TextUtils.isEmpty(picPath1)
                || !TextUtils.isEmpty(picPath2)
                || !TextUtils.isEmpty(picPath3) ) { //  先找本地图片
            if (!TextUtils.isEmpty(picPath1)) {
                ImageLoader.getInstance().displayImage("file://" + picPath1, imageview1);
                imageview1.setTag(picPath1);
            }
            if (!TextUtils.isEmpty(picPath2)) {
                ImageLoader.getInstance().displayImage("file://" + picPath2, imageview2);
                imageview1.setTag(picPath2);
            }
            if (!TextUtils.isEmpty(picPath3)) {
                ImageLoader.getInstance().displayImage("file://" + picPath3, imageview3);
                imageview1.setTag(picPath3);
            }
        } else  if (!TextUtils.isEmpty(imgs)) {
            Type type =new TypeToken<List<ImageData.FileMap>>(){}.getType();
            List<ImageData.FileMap> imagelist = new Gson().fromJson(imgs, type);
            if (imagelist != null || imagelist.size() > 0) {
                for (int i =0; i< imagelist.size(); i++){
                    ImageData.FileMap file = imagelist.get(i);
                    if (i == 0) {
                        ImageLoader.getInstance().displayImage(file.getUrl(), imageview1);
                        imageview1.setTag(file.getUrl());
                    }
                    if (i == 1) {
                        ImageLoader.getInstance().displayImage(file.getUrl(), imageview2);
                        imageview2.setTag(file.getUrl());
                    }
                    if (i == 2) {
                        ImageLoader.getInstance().displayImage(file.getUrl(), imageview3);
                        imageview3.setTag(file.getUrl());
                    }
                }
            }
        }

        if (!isUploaded) { // 没有上传，允许编辑
            String tag = (String)imageview1.getTag();
            if (!TextUtils.isEmpty(tag)) {
                deleteImage1.setVisibility(View.VISIBLE);
            }

            tag = (String)imageview2.getTag();
            if (!TextUtils.isEmpty(tag)) {
                deleteImage2.setVisibility(View.VISIBLE);
            }

            tag = (String)imageview3.getTag();
            if (!TextUtils.isEmpty(tag)) {
                deleteImage3.setVisibility(View.VISIBLE);
            }

        } else {

        }

        if (isUploaded) {
            bottomLayout.setVisibility(View.GONE);
        }
    }

    private void fillLongitudeAndLaititude() {
        longitudeTv.setText("经度: " + gatherPoint.getLongitude());
        laititudeTv.setText("纬度: " + gatherPoint.getLatitude());
        altitudeTv.setText("高度: " + gatherPoint.getHeight());
        timeTv.setText("采集时间: " + gatherPoint.getCollected_at());
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
        attrsView.setGatherPoint(gatherPoint);
    }
    private void initSpinner() {
        try {
            UserInfoBean userInfoBean = CacheData.getUserInfoBean();
            projectTypes = userInfoBean.getData().getProject().getTypes();
            PointTypeAdapter pointAdapter = new PointTypeAdapter(this, projectTypes);
            typeSpinner.setAdapter(pointAdapter);

            String type_id = gatherPoint.getType_id();

            for (int i = 0; i<  projectTypes.size(); i ++){
                CollectType collectType = projectTypes.get(i);
                if (collectType.getId().equals(type_id)) {
                    typeIndex = i;
                }
            }

            typeSpinner.setSelection(typeIndex);

            if (isUploaded) typeSpinner.setEnabled(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v->finish());
        cameraLayout.setOnClickListener(v->initPermission(TAKE_PICTURE));//  动态请求权限);
        saveLayout.setOnClickListener(v->initPermission(SAVE_POINT));
        imageview1.setOnClickListener(v->{
            if (isUploaded) return;
            String tag = (String)imageview1.getTag();
            if (TextUtils.isEmpty(tag)) {
                initPermission(TAKE_PICTURE);
            }
        });
        imageview2.setOnClickListener(v->{
            if (isUploaded) return;
            String tag = (String)imageview2.getTag();
            if (TextUtils.isEmpty(tag)) {
                initPermission(TAKE_PICTURE);
            }
        });
        imageview3.setOnClickListener(v->{
            if (isUploaded) return;
            String tag = (String)imageview3.getTag();
            if (TextUtils.isEmpty(tag)) {
                initPermission(TAKE_PICTURE);
            }
        });

        deleteImage1.setOnClickListener(v->{
            if (isUploaded) return;
            String tag = (String)imageview1.getTag();
            if (!TextUtils.isEmpty(tag)) {
                imageview1.setImageResource(R.mipmap.icon_add_pic);
                imageview1.setTag("");
                deleteImage1.setVisibility(View.INVISIBLE);
            }
        });
        deleteImage2.setOnClickListener(v->{
            if (isUploaded) return;
            imageview2.setImageResource(R.mipmap.icon_add_pic);
            imageview2.setTag("");
            deleteImage2.setVisibility(View.INVISIBLE);
        });
        deleteImage3.setOnClickListener(v->{
            if (isUploaded) return;
            imageview3.setImageResource(R.mipmap.icon_add_pic);
            imageview3.setTag("");
            deleteImage3.setVisibility(View.INVISIBLE);
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

    private void savePoint() {
        if (!hasProjectInfo()) return;

        String pointName = nameET.getText().toString().trim();

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

        gatherPoint.setAttrs(new Gson().toJson(attrsValue.getAttrs()));

        String path = (String)imageview1.getTag();
        if (!TextUtils.isEmpty(path)) {
            gatherPoint.setPicPath1(path);
        }
        path = (String)imageview2.getTag();
        if (!TextUtils.isEmpty(path)) {
            gatherPoint.setPicPath2(path);
        }
        path = (String)imageview3.getTag();
        if (!TextUtils.isEmpty(path)) {
            gatherPoint.setPicPath3(path);
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
                    bitmap = BitmapFactory.decodeFile(takeCameraFilename);
                    if (bitmap != null) {
                        setImage(bitmap);
                    }
                    break;
                case REQUEST_CODE_TAKE_PHOTO:
                    if (takeCameraFilename != null) {
                        bitmap = BitmapUtil.trimBitmapFile(takeCameraFilename);
                        if (bitmap != null) {
                            setImage(bitmap);
                        }
                    }
                    break;
            }
        }
    }

    private void setImage(Bitmap bitmap) {
        if (bitmap == null) return;
        String tag1 = (String)imageview1.getTag();
        String tag2 = (String)imageview2.getTag();
        if (TextUtils.isEmpty(tag1)) {
            imageview1.setImageBitmap(bitmap);
            imageview1.setTag(takeCameraFilename);
            deleteImage1.setVisibility(View.VISIBLE);
        } else if (TextUtils.isEmpty(tag2)){
            imageview2.setImageBitmap(bitmap);
            imageview2.setTag(takeCameraFilename);
            deleteImage2.setVisibility(View.VISIBLE);
        } else { // 前两个有了，直接替换第三个
            imageview3.setImageBitmap(bitmap);
            imageview3.setTag(takeCameraFilename);
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
