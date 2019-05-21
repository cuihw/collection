package com.data.collection.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.data.collection.App;
import com.data.collection.util.LsLog;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.MemoryCookieStore;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.model.Response;

import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;

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
        OkGo.<String>post(url).tag(clz)
                //.headers("SessionKey", CacheData.SessionKey)
                .headers("platform", "Android")
                .params("params", jsonObject.toString())
                .execute(new HStringCallback(context) {
                    String body = null;

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        if (TextUtils.isEmpty(body)) {
                            listener.onResponse(1, null);
                        }
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        body = response.body();
                        try {


                            Class<T> clz = listener.getClazz();
                            if (clz != String.class) {
                                T data = new Gson().fromJson(body, clz);
                                listener.onResponse(0, data);
                            } else {
                                listener.onResponse(0, body);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onResponse(1, null);
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

}
