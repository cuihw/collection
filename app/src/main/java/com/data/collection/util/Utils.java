package com.data.collection.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    private static final String TAG = "Utils";

    // 小数点后两位小数，大于两位时返回false。
    public static boolean checkPrice(String number) {
        if (number.contains(".")) {
            String intNumber = number.substring(number.indexOf("."));
            if (intNumber.length() > 3) {
                return false;
            }
        }
        return true;
    }
    /**
     * 写入剪切板数据
     *
     * @param context
     */
    public static void copyToClip(Context context, String str) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        //创建ClipData对象 ClipData
        ClipData clipData = ClipData.newPlainText("simple text copy", str);
        // 添加ClipData对象到剪切板中
        cm.setPrimaryClip(clipData);
    }

    public static String transform2Decimal(String priceString) {
        if (isEmpty(priceString)) return "0.00";

        try {
            Float v = Float.parseFloat(priceString);
            return String.format("%1.2f", v);
        } catch (Exception e) {
            return "0.00";
        }
    }

    public static boolean isEmpty(String cs) {
        return cs == null || cs.trim().length() == 0;
    }


    public final static String SHA1 = "SHA1";

    public final static String MD5 = "MD5";

    /**
     * 返回一个签名的对应类型的字符串
     *
     * @param context
     * @param packageName
     * @param type
     * @return
     */
    public static String getSignInfo(Context context, String packageName, String type) {
        String tmp = null;
        Signature[] signs = getSignatures(context, packageName);
        for (Signature sig : signs) {
            if (SHA1.equals(type)) {
                tmp = getSignatureString(sig, SHA1);
                break;
            } else {
                tmp = getSignatureString(sig, MD5);
            }
        }
        return tmp;
    }
    /**
     * 返回对应包的签名信息
     *
     * @param context
     * @param packageName
     * @return
     */
    public static Signature[] getSignatures(Context context, String packageName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            return packageInfo.signatures;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取相应的类型的字符串（把签名的byte[]信息转换成16进制）
     *
     * @param sig
     * @param type
     * @return
     */
    public static String getSignatureString(Signature sig, String type) {
        byte[] hexBytes = sig.toByteArray();
        String fingerprint = "error!";
        try {
            MessageDigest digest = MessageDigest.getInstance(type);
            if (digest != null) {
                byte[] digestBytes = digest.digest(hexBytes);
                StringBuilder sb = new StringBuilder();
                for (byte digestByte : digestBytes) {
                    sb.append((Integer.toHexString((digestByte & 0xFF) | 0x100)).substring(1, 3));
                }
                fingerprint = sb.toString();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return fingerprint;
    }

    /**
     * V4.5.0起，保证数据不溢出，使用long型保存数据包大小结果
     */
    public static String formatDataSize(long size) {
        String ret = "";
        if (size < (1024 * 1024)) {
            ret = String.format("%dK", size / 1024);
        } else {
            ret = String.format("%.1fM", size / (1024 * 1024.0));
        }
        return ret;
    }


    public static void cacheImage(String url) {
        //if (isCachedImage(url)) return;
        LsLog.w(TAG, "cacheImage =" + url);

        ImageLoader.getInstance().loadImage(url, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            }
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                // put bitmap in the cache.
                LsLog.w(TAG, " put bitmap in the cache. =" + url);
                try {
                    if (loadedImage == null) {
                        LsLog.w(TAG, "bitmap loadedImage null");
                        return;
                    }
                    ImageLoader.getInstance().getDiskCache().save(url, loadedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
    }

    public static boolean isCachedImage(String url) {
        return ImageLoader.getInstance().getDiskCache().get(url).exists();
    }

    public static Bitmap getCachedImage(String url) {
        File file = ImageLoader.getInstance().getDiskCache().get(url);
        Bitmap bitmap= BitmapFactory.decodeFile(file.getName());
        return  bitmap;
    }

    public static String trimLastChar(String text){
        if (!TextUtils.isEmpty(text)) {
            StringBuffer sb = new StringBuffer(text);
            text = sb.deleteCharAt(sb.length() - 1).toString();
        }
        return text;
    }

    public static void swap(double a, double b) {
        double c=a;
        a=b;
        b=c;
    }
}
