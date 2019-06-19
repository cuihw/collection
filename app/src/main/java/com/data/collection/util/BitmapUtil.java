package com.data.collection.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtil {

    private static final String TAG = "BitmapUtil";

    //将bitmap调整到指定大小
    public static Bitmap sizeBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {//这时候origin还有吗？
            origin.recycle();
        }
        return newBM;
    }

    public static File trimBitmap(File file) {
        String filename = file.getAbsolutePath();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        Bitmap bitmap = BitmapFactory.decodeFile(filename, options);
        Log.i(TAG, "压缩后图片的大小" + (bitmap.getByteCount() / 1024 / 1024)
                + "M宽度为" + bitmap.getWidth() + "高度为" + bitmap.getHeight());
        saveImageToGallery(null, bitmap, filename);
        return new File(filename);
    }

    public static Bitmap trimBitmapFile(String filename){
        File file = new File(filename);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(filename);
            bitmap = scaleBitmap(bitmap, 0.5f);
            saveImageToGallery(null, bitmap, filename);

            return bitmap;
        }
        return null;
    }

    public static int saveImageToGallery(Context context, Bitmap bmp, String filename) {
        File file = new File(filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            // 通知系统相册刷新
            if (context != null){
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(new File(file.getPath()))));
                return 2;
            }
            return 1;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    //按比例缩放
    public static Bitmap scaleBitmap(Bitmap origin, float scale) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(scale, scale);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    public static Bitmap cropBitmap(Bitmap bitmap) {//从中间截取一个正方形
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();
        int cropWidth = w >= h ? h : w;// 裁切后所取的正方形区域边长

        return Bitmap.createBitmap(bitmap, (bitmap.getWidth() - cropWidth) / 2,
                (bitmap.getHeight() - cropWidth) / 2, cropWidth, cropWidth);
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap) {//把图片裁剪成圆形
        if (bitmap == null) {
            return null;
        }
        bitmap = cropBitmap(bitmap);//裁剪成正方形
        try {
            Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(circleBitmap);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());
            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight()));
            float roundPx = 0.0f;
            roundPx = bitmap.getWidth();
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.WHITE);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            final Rect src = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, rect, paint);
            return circleBitmap;
        } catch (Exception e) {
            return bitmap;
        }
    }


    public static Bitmap getRoundBitmap(Bitmap bitmap) {// 圆角
        if (bitmap == null) {
            return null;
        }
        try {
            Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(circleBitmap);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());

            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight()));
            float roundPx = 10.0f;
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.WHITE);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            final Rect src = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, rect, paint);
            return circleBitmap;
        } catch (Exception e) {
            return bitmap;
        }
    }


    public static Bitmap getRoundBitmap(Bitmap bitmap, int position, int round) {// 圆角
        if (bitmap == null) {
            return null;
        }
        try {
            Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(circleBitmap);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());

            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight()));
            float roundPx = round;

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.WHITE);

            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            final Rect src = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, rect, paint);
            return circleBitmap;
        } catch (Exception e) {
            return bitmap;
        }
    }

    public static Bitmap mergeBitmap(Bitmap bg_bitmap, Bitmap front_bitmap) {

        Bitmap newBitmap = Bitmap.createBitmap(bg_bitmap.getWidth(),
                bg_bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(newBitmap);

        int bg_w = bg_bitmap.getWidth();
        int bg_h = bg_bitmap.getHeight();
        int front_w = front_bitmap.getWidth();
        int front_h = front_bitmap.getHeight();

        int startLeft = (bg_w - front_w) / 2;
        int startTop = (bg_h - front_h) / 2 - (front_h * 3 / 10);

        canvas.drawBitmap(bg_bitmap, 0, 0, null);
        canvas.drawBitmap(front_bitmap, startLeft, startTop, null);

        return newBitmap;
    }

    /**
     * path转uri
     */
    public static Uri getUri(String path, Context context) {
        Uri uri = null;
        if (path != null) {
            path = Uri.decode(path);
            ContentResolver cr = context.getContentResolver();
            StringBuffer buff = new StringBuffer();
            buff.append("(")
                    .append(MediaStore.Images.ImageColumns.DATA)
                    .append("=")
                    .append("'" + path + "'")
                    .append(")");
            Cursor cur = cr.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.ImageColumns._ID},
                    buff.toString(), null, null);
            int index = 0;
            for (cur.moveToFirst(); !cur.isAfterLast(); cur
                    .moveToNext()) {
                index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
// set _id value
                index = cur.getInt(index);
            }
            if (index == 0) {
//do nothing
            } else {
                Uri uri_temp = Uri.parse("content://media/external/images/media/" + index);
                if (uri_temp != null) {
                    uri = uri_temp;
                }
            }
        }
        return uri;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();// 取drawable的长宽
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() !=
                PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;// 取drawable的颜色格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);// 建立对应bitmap
        Canvas canvas = new Canvas(bitmap);// 建立对应bitmap的画布
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);// 把drawable内容画到画布中
        return bitmap;
    }

    public static void showInMedia(Context context, Bitmap bitmap, String filename){
        MediaStore.Images.Media.insertImage(context.getContentResolver(),
                bitmap, filename, null);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(filename));
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

}



