package com.data.collection.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.data.collection.R;

import java.lang.reflect.Method;

/**
 * Created by ... on 2018/10/12.
 * PopupDialog.create(...).show();
 */
public class PopupInfoWindow extends AlertDialog {
    private static final String TAG = "PopupDialog";
    private View view;
    private Context context;
    private static TextView title;

    private TextView msg;
    private static TextView tv_desc2;
    private TextView confirm;
    private TextView cancel;
    private LinearLayout bottomLl;
    private RelativeLayout topRl;
    private View verticalLine;
    private int width;
    static PopupInfoWindow dialog;

    protected PopupInfoWindow(Context context, boolean cancelable, boolean canceledOnTouchOutside) {
        super(context, R.style.Dialog_Common);
        this.context = context;
        double deviceWidth = getScreenWidth(this.context);
        width = (int) (deviceWidth * 0.7);
        setCanceledOnTouchOutside(canceledOnTouchOutside);
        LayoutInflater inflater = LayoutInflater.from(this.context);
        view = inflater.inflate(R.layout.info_window, null);
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams
                .WRAP_CONTENT, 0);
        setContentView(view, params);
    }
    ImageView icon;

    private void initView() {
        this.title = view.findViewById(R.id.name_tv);
        this.msg = view.findViewById(R.id.type_tv);
        this.icon =(ImageView)view.findViewById(R.id.type_icon);
        this.tv_desc2 = (TextView) view.findViewById(R.id.point_tv);
        this.confirm = (TextView) view.findViewById(R.id.check_btn);
    }

    /**
     * Obtain a confirm dialog instance
     *
     * @param context                context
     * @param title                  title of the dialog, pass null or "" if no title is needed
     * @param confirm                name of confirm button, pass null or "" if no confirm button is needed
     * @param onClicklistener  click listener of confirm button
     * @return PopupDialog
     */
    public static PopupInfoWindow create(Context context, String title, Bitmap typeIcon, String type, String location,
                                         String confirm, View.OnClickListener onClicklistener) {
        dialog = new PopupInfoWindow(context, false, false);

        dialog.setDialogTitle(title);
        dialog.setDialogMessage(type);
        dialog.setSubinfo(location);
        dialog.setDialogButton(confirm, onClicklistener);
        dialog.setTypeIcon(typeIcon);
        return dialog;
    }

    private void setTypeIcon(Bitmap typeIcon) {
        icon.setImageBitmap(typeIcon);
    }
    View.OnClickListener clickListener;
    private void setDialogButton(String confirm, View.OnClickListener positiveClickListener) {
        this.confirm.setText(confirm);
        clickListener = positiveClickListener;
        this.confirm.setOnClickListener( v->{
            if (clickListener != null) clickListener.onClick(v);
            this.dismiss();
        });
        view.setOnClickListener(v->{
            this.dismiss();
        });
    }

    private void setSubinfo(String location) {
        tv_desc2.setText(location);
    }

    private void setDialogMessage(String type) {
        msg.setText(type);
    }

    private void setDialogTitle(String title) {
        this.title.setText(title);
    }
    private int getScreenWidth(Context context) {
        return getScreenSize(context)[0];
    }

    private int getScreenHeight(Context context) {
        return getScreenSize(context)[1];
    }

    private int[] getScreenSize(Context context) {
        WindowManager windowManager;
        try {
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        } catch (Throwable var6) {
            Log.w(TAG, var6);
            windowManager = null;
        }

        if (windowManager == null) {
            return new int[]{0, 0};
        } else {
            Display display = windowManager.getDefaultDisplay();
            if (Build.VERSION.SDK_INT < 13) {
                DisplayMetrics t1 = new DisplayMetrics();
                display.getMetrics(t1);
                return new int[]{t1.widthPixels, t1.heightPixels};
            } else {
                try {
                    Point t = new Point();
                    Method method = display.getClass().getMethod("getRealSize", new Class[]{Point.class});
                    method.setAccessible(true);
                    method.invoke(display, new Object[]{t});
                    return new int[]{t.x, t.y};
                } catch (Throwable var5) {
                    Log.w(TAG, var5);
                    return new int[]{0, 0};
                }
            }
        }
    }
}