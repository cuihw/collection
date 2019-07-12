package com.data.collection.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.data.collection.module.NaviData;
import com.data.navidata.LocaltionData;
import com.data.navidata.NaviDataSS;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CommonUtils {

    private static final String TAG = "CommonUtils";

    public static boolean navito(Activity act, NaviData item, int requestCode) {

        NaviDataSS naviDataSS = new NaviDataSS();
        LocaltionData endNode = new LocaltionData();
        endNode.setName(item.getName());
        endNode.setLatitude(Double.parseDouble(item.getLatitude()));
        endNode.setLongitude(Double.parseDouble(item.getLongitude()));
        naviDataSS.setEndNode(endNode);

        if (PackageUtils.isInstalledApp(act, "com.data.zwnavi")) {
            String activity = "com.data.zwnavi.MainActivity";
            ComponentName component = new ComponentName("com.data.zwnavi", activity);
            Intent intent = new Intent();
            intent.setComponent(component);
            intent.putExtra("NaviDataSS", new Gson().toJson(naviDataSS));
            act.startActivityForResult(intent, requestCode);
            return true;
        } else {
            return false;
        }
    }

    public static void install(Activity activity) {
        if (copied) {
            String name = FileUtils.getFileDir() + "zwnavi_release.apk";
            File apkFile = new File(name);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(
                        activity
                        ,activity.getPackageName()
                        ,apkFile);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                Log.w(TAG, "normal install");
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            }
            activity.startActivity(intent);
        }
    }

    private static boolean copyAssetsToFile(Context context, String srcPath, String dstPath) {
        boolean isSuccess;
        LsLog.w(TAG, "copyAssetsToFile");
        try {
            File file = new File(FileUtils.getFileDir());
            if (!file.exists()) file.mkdirs();
            File outFile = new File(FileUtils.getFileDir(), dstPath);
            AssetManager assets = context.getAssets();
            InputStream src = assets.open(srcPath);
            InputStream is = context.getAssets().open(srcPath);
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        }
        return isSuccess;
    }

    static boolean copied;

    public static void copy(Activity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                copied = copyAssetsToFile(activity, "zwnavi_release.mp3", "zwnavi_release.apk");
                if (copied) install(activity);
            }
        }).start();
    }
}
