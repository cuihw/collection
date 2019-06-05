package com.data.collection.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.data.collection.App;
import com.data.collection.Constants;
import com.data.collection.data.CacheData;
import com.data.collection.module.ImageUploadBean;
import com.data.collection.util.BitmapUtil;
import com.data.collection.util.LsLog;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.MemoryCookieStore;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.PostRequest;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static okhttp3.MediaType.*;

public class HttpRequest {
    private static final String TAG = "HttpRequest";

    private static OkHttpClient.Builder builder;
    private static OkHttpClient okHttpclient;

    public static void initOkGo(App appContext) {

        okHttpclient = new OkHttpClient();
        builder = okHttpclient.newBuilder();

        // 全局的读取超时时间
        builder.readTimeout(5000, TimeUnit.MILLISECONDS);
        // 全局的写入超时时间
        builder.writeTimeout(5000, TimeUnit.MILLISECONDS);
        // 全局的连接超时时间
        builder.connectTimeout(5000, TimeUnit.MILLISECONDS);
        builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("CHRIS");
        // log打印级别，决定了log显示的详细程度
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
        // log颜色级别，决定了log在控制台显示的颜色
        loggingInterceptor.setColorLevel(Level.INFO);

        //if (BuildConfig.DEBUG)
        builder.addInterceptor(loggingInterceptor);

        OkGo.getInstance().init(appContext)
                .setOkHttpClient(builder.build());
    }

    // 接口的使用：
    // status = 0 的时候，认为返回了正确的响应
    // 其他情况下根据不同的情况，status 等于一个负数进行判断
    public interface ResponseListener<T> {
        void onResponse(int status, T bean);
    }

    public static <T> void postData(String url, Map<String, Object> params, final RespListener listener) {
        postData(null, url, params, listener);
    }

    public static <T> void postJson(final Context context, String url, Map<String, Object> params, final RespListener listener) {
        if (params == null) {
            params = new HashMap<>();
        }
        JSONObject jsonObject = new JSONObject(params);
        String clz = null;
        if (context != null) {
            clz = context.getClass().getSimpleName();
            LsLog.i(TAG, "set the class name as tag clz = " + clz);
        }
        OkGo.<String>post(url).tag(clz).headers("platform", "Android")
                .headers("token", CacheData.LOGIN_DATA == null ? "" : CacheData.LOGIN_DATA.getToken())
                .upJson(jsonObject)
                .execute(new HStringCallback(context) {
                    String body = null;

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        if (TextUtils.isEmpty(body)) {
                            listener.onResponse(-1, null);
                        }
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        body = response.body();
                        try {
                            Class<T> clz = listener.getClazz();
                            if (clz != String.class || clz != null) {
                                T data = new Gson().fromJson(body, clz);
                                listener.onResponse(0, data);
                            } else {
                                listener.onResponse(0, body);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onResponse(-1, null);
                        }
                    }
                });
    }

    public static <T> void postData(final Context context, String url, Map<String, Object> params, final RespListener listener) {
        if (params == null) {
            params = new HashMap<>();
        }
        JSONObject jsonObject = new JSONObject(params);
        Log.i(TAG, "params:" + jsonObject.toString());
        String clz = null;
        if (context != null) {
            clz = context.getClass().getSimpleName();
            LsLog.i(TAG, "set the class name as tag clz = " + clz);
        }
        HttpParams hparams = new HttpParams();
        for (String key : params.keySet()) {
            String s = params.get(key).toString();
            LsLog.i(TAG, "params = " + s);
            hparams.put(key, s);
        }

        OkGo.<String>post(url).tag(clz).headers("platform", "Android")
                .headers("token", CacheData.LOGIN_DATA == null ? "" : CacheData.LOGIN_DATA.getToken())
                .params(hparams)
                .execute(new HStringCallback(context) {
            String body = null;

            @Override
            public void onFinish() {
                super.onFinish();
                if (TextUtils.isEmpty(body)) {
                    listener.onResponse(-1, null);
                }
            }

            @Override
            public void onSuccess(Response<String> response) {
                body = response.body();
                try {
                    Class<T> clz = listener.getClazz();
                    String name = clz.getName();
                    if (name.equals("java.lang.String")) {
                        listener.onResponse(0, body);
                    } else if (clz != null) {
                        T data = new Gson().fromJson(body, clz);
                        listener.onResponse(0, data);
                    } else {
                        listener.onResponse(0, body);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onResponse(-1, null);
                }
            }
        });
    }

    static public abstract class RespListener<T> implements ResponseListener<T> {
        private Class<T> clazz;

        public RespListener() {
            ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
            this.clazz = (Class<T>) type.getActualTypeArguments()[0];
        }

        public Class<T> getClazz() {
            return clazz;
        }
    }

    public static void cancleRequest(final Context context) {
        String clz = null;
        if (context != null) {
            clz = context.getClass().getSimpleName();
        }
        OkGo.getInstance().cancelTag(clz);
    }

    public static void cancleRequest(final String tag) {
        OkGo.getInstance().cancelTag(tag);
    }


    public static void getRequest(Context context, String url, final ResponseListener<String> listener) {
        OkGo.<String>get(url).tag(context.getClass().getSimpleName())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        listener.onResponse(0, response.body());
                    }
                });
    }

    public static void upLoadImgs(List<File> files, final ResponseListener<ImageUploadBean> listener) {
        OkHttpClient client = new OkHttpClient();

//        MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
//        RequestBody body = RequestBody.create(mediaType, "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition:" +
//                " form-data; name=\"image[]\"; filename=\"1.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n\r\n" +
//                "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n" +
//                "Content-Disposition: form-data; name=\"image[]\"; filename=\"2.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n\r\n" +
//                "------WebKitFormBoundary7MA4YWxkTrZu0gW--");
//        Request request = new Request.Builder()
//                .url("http://127.0.0.1/collect/index.php/app/v1/uploadMultiImg")
//                .post(body)
//                .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
//                .addHeader("token", "endfY29sbGVjdGlvbl8xMjM0NTZ7ImlkIjoiMTAiLCJleHBpcmVkX2F0IjoxNTU5NzE4NzMyfQ==")
//                .addHeader("User-Agent", "PostmanRuntime/7.13.0")
//                .addHeader("Accept", "*/*")
//                .addHeader("Cache-Control", "no-cache")
//                .addHeader("Postman-Token", "62e336d5-377a-4809-aa68-60bf2954d52a,d09e6614-901f-4062-b8a8-262c18645259")
//                .addHeader("Host", "127.0.0.1")
//                .addHeader("accept-encoding", "gzip, deflate")
//                .addHeader("content-length", "106477")
//                .addHeader("Connection", "keep-alive")
//                .addHeader("cache-control", "no-cache")
//                .build();

//        try {
//            okhttp3.Response response = client.newCall(request).execute();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        if (files.size() > 0) {
            for (File file: files) {
                if (file.length() > 1024 * 1024) {
                    BitmapUtil.trimBitmap(file);
                }
            }
        }

        OkGo.<String>post(Constants.UPLOAD_IMAGES).tag("upLoadImgs")
                .headers("token", CacheData.LOGIN_DATA == null ? "" : CacheData.LOGIN_DATA.getToken())
                .addFileParams("image[]", files).isMultipart(true)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        String body = response.body();
                        ImageUploadBean bean = null;
                        try {
                            bean = new Gson().fromJson(body, ImageUploadBean.class);
                        }catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (listener!= null) {
                                int status = bean == null ? -1 : 0;
                                listener.onResponse(status, bean);
                            }
                        }
                    }
                });
    }

}
