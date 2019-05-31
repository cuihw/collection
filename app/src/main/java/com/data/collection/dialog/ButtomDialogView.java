package com.data.collection.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.data.collection.R;


/**
 * 自定义底部弹出对话框
 * Created by zhaomac on 2017/9/8.
 */

public class ButtomDialogView extends Dialog {

    private boolean iscancelable = true;//控制点击dialog外部是否dismiss
    private boolean isBackCancelable = true;//控制返回键是否dismiss
    private View view;
    private Context context;

    View.OnClickListener listener;

    public ButtomDialogView(Context context, int layoutid) {
        super(context, R.style.MyDialog);
        this.context = context;
        this.view = LayoutInflater.from(context).inflate(layoutid, null);
    }

    public ButtomDialogView(Context context, View view, boolean isCancelable) {
        super(context, R.style.MyDialog);
        this.context = context;
        this.view = view;
        this.iscancelable = isCancelable;
    }

    public View getView() {
        return view;
    }

    public ButtomDialogView(Context context, View view, boolean isCancelable,boolean isBackCancelable) {
        super(context, R.style.MyDialog);
        this.context = context;
        this.view = view;
        this.iscancelable = isCancelable;
        this.isBackCancelable = isBackCancelable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(view);//这行一定要写在前面
        setCancelable(isBackCancelable);
        setCanceledOnTouchOutside(iscancelable); //点击外部可dismiss
        Window window = this.getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }
}
