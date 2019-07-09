package com.data.collection.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.data.collection.R;

public class ImageDialog {
    View view;
    AlertDialog.Builder builder;
    AlertDialog dialog;

    ImageDialogOnClick listener;
    TextView confirm;
    TextView cancel;
    Bitmap bitmap;

    public AlertDialog getDialog() {
        return dialog;
    }

    public void setDialog(AlertDialog dialog) {
        this.dialog = dialog;
    }

    public ImageDialog(Context context, Bitmap bitmap) {
        view = LayoutInflater.from(context).inflate(R.layout.dialog_bitmap, null);
        builder = new AlertDialog.Builder(context);
        this.bitmap = bitmap;
        builder.setView(view);

        confirm = view.findViewById(R.id.confirm_tv);
        cancel = view.findViewById(R.id.cancel_tv);
        cancel.setOnClickListener(v->{
            dialog.dismiss();
        });
        confirm.setOnClickListener(v->{
            dialog.dismiss();
        });

        dialog = builder.create();
    }

    public void setOnClickListener(ImageDialogOnClick listener){
        cancel.setOnClickListener(v->{
            if (listener != null)
                listener.onCancel();
            dialog.dismiss();
        });

        confirm.setOnClickListener(v->{
            if (listener != null)
                listener.onConfirm();
            dialog.dismiss();
        });
    }

    public void show() {
        dialog.show();

        ImageView imageView = view.findViewById(R.id.imageview);
        imageView.setImageBitmap(bitmap);
        confirm.setText("OK");
        cancel.setText("Cancel");

    }

    public boolean isShow() {
        return dialog.isShowing();
    }

    public interface ImageDialogOnClick{
        public void onCancel();
        public void onConfirm();
    }
}
