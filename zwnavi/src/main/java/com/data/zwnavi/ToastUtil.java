package com.data.zwnavi;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtil {
    private static Toast toast = null;

    public static void showTextToast(Context context, String msg) {
        showTextToast(context, msg, false);
    }

    public static void showTextToast(Context context, String msg, boolean isCenter) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        if (isCenter) toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


}
