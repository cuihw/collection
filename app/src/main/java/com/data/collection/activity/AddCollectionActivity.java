package com.data.collection.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.data.collection.R;
import com.data.collection.adapter.PointTypeAdapter;
import com.data.collection.data.CacheData;
import com.data.collection.dialog.ButtomDialogView;
import com.data.collection.module.Attrs;
import com.data.collection.module.Types;
import com.data.collection.module.UserInfoBean;
import com.data.collection.util.BitmapUtil;
import com.data.collection.util.FileUtils;
import com.data.collection.util.LsLog;
import com.data.collection.view.AttributionView;
import com.data.collection.view.TitleView;

import java.io.File;
import java.util.List;

import butterknife.BindView;

public class AddCollectionActivity extends BaseActivity {

    private static final String TAG = "AddCollectionActivity";
    private static final int REQUEST_PERMISSIONS = 1;

    private static final String TAKE_PICTURE = "action.take.picture";
    private static final String SAVE_POINT = "action.save.point";
    private static final int REQUEST_CODE_PIC_PHOTO = 2;
    private static final int REQUEST_CODE_TAKE_PHOTO = 3;

    String takeCameraFilename; // 照相机照片保存路径

    @BindView(R.id.title_view)
    TitleView titleView;

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


    List<Types> projectTypes;

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

    private void initView() {
        deleteImage1.setVisibility(View.INVISIBLE);
        deleteImage2.setVisibility(View.INVISIBLE);
        deleteImage3.setVisibility(View.INVISIBLE);
        initSpinner();

        attrsView = new AttributionView(this);

        createAttrsView(0);
    }

    private void createAttrsView(int i) {
        attrsView.clearView();
        attributionLayout.removeAllViews();
        if (projectTypes != null) {
            Types types = projectTypes.get(i);
            List<Attrs> attrs = types.getAttrs();
            attrsView.setViewAttri(attrs);
            attributionLayout.addView(attrsView);
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
        cameraLayout.setOnClickListener(v->initPermission(TAKE_PICTURE));//  动态请求权限);
        saveLayout.setOnClickListener(v->initPermission(SAVE_POINT));

        imageview1.setOnClickListener(v->{
            String tag = (String)imageview1.getTag();
            if (TextUtils.isEmpty(tag)) {
                initPermission(TAKE_PICTURE);
            }
        });
        imageview2.setOnClickListener(v->{
            String tag = (String)imageview2.getTag();
            if (TextUtils.isEmpty(tag)) {
                initPermission(TAKE_PICTURE);
            }
        });
        imageview3.setOnClickListener(v->{
            String tag = (String)imageview3.getTag();
            if (TextUtils.isEmpty(tag)) {
                initPermission(TAKE_PICTURE);
            }
        });

        deleteImage1.setOnClickListener(v->{
            String tag = (String)imageview1.getTag();
            if (!TextUtils.isEmpty(tag)) {
                imageview1.setImageResource(R.mipmap.icon_add_pic);
                imageview1.setTag("");
                deleteImage1.setVisibility(View.INVISIBLE);
            }
        });
        deleteImage2.setOnClickListener(v->{
            imageview2.setImageResource(R.mipmap.icon_add_pic);
            imageview2.setTag("");
            deleteImage2.setVisibility(View.INVISIBLE);
        });
        deleteImage3.setOnClickListener(v->{
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
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_CODE_PIC_PHOTO);
    }
    private void callCamera() {
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
                    String filePathByUri = FileUtils.getFilePathByUri(this, data1);
                    LsLog.i(TAG, "onPickPhotoResult: " + filePathByUri);
                    bitmap = BitmapFactory.decodeFile(filePathByUri);
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

}
