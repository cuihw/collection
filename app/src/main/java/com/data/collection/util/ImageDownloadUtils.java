package com.data.collection.util;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 */

public class ImageDownloadUtils {


    private Object lock = new Object();
    // 下载链接集合
    private List<String> listURL;
    // 下载个数
    private int size = 0;

    public static void saveNetPics(Context context, List<String> lists) {
        ImageDownloadUtils imageDownloadUtils = new ImageDownloadUtils();
        imageDownloadUtils.listURL = lists;
        imageDownloadUtils.size = 0;
        imageDownloadUtils.startDownload(context, lists);
    }

    public static void saveNetPic(Context context, String url) {
        ImageDownloadUtils imageDownloadUtils = new ImageDownloadUtils();
        ThreadPoolManager.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                // 自定义图片命名
                imageDownloadUtils.downloadImage(url, context);
            }
        });
    }
    /**
     * 开始下载
     */
    public void startDownload(final Context context, List<String> listURL) {

        for (final String url : listURL) {
            //捕获线程池拒绝执行异常
            try {
                ThreadPoolManager.getNormalThreadPoolProxy().execute(new Runnable() {
                    @Override
                    public void run() {
                        // 自定义图片命名
                         downloadImage(url, context);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * 下载图片
     *
     * @param loadUrl
     * @param context
     * @return
     */
    private void downloadImage(String loadUrl, final Context context) {

        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;

        String fileName = FileUtils.getFileDir() + System.currentTimeMillis() + ".jpg";

        try {
            // 自定义图片命名
            File cacheFile = new File(fileName);
            //对资源链接
            URL url = new URL(loadUrl);
            //打开输入流
            inputStream = url.openStream();
            fileOutputStream = new FileOutputStream(cacheFile);
            int hasRead = 0;
            while ((hasRead = inputStream.read()) != -1) {
                fileOutputStream.write(hasRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
                inputStream.close();
                PreferencesUtils.putString(context, loadUrl, fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
