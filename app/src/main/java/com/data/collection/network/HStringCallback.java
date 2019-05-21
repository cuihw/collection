package com.data.collection.network;

import android.content.Context;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.request.base.Request;

/**
 * 自定义gson回调，方便解析服务器数据
 * Created by gcy on 2017/7/18 0018.
 */

public abstract class HStringCallback extends StringCallback {

    KProgressHUD hud;

    public HStringCallback() {
    }

    public HStringCallback(Context context) {
        initProgress(context);
    }

    void initProgress(Context context) {
        if (context != null)
            hud = KProgressHUD.create(context)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setCancellable(false);
    }

    @Override
    public void onStart(Request request) {
        super.onStart(request);
        if (hud != null && !hud.isShowing())
            hud.show();
    }

    @Override
    public void onFinish() {
        super.onFinish();
        if (hud != null && hud.isShowing())
            hud.dismiss();
    }
}
