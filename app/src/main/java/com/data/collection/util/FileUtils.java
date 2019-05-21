package com.data.collection.util;

import android.os.Environment;

public class FileUtils {
    // 文件夹目录
    public static final String APP_FOLDER_NAME = "zwsdk";

    public static String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }
}
