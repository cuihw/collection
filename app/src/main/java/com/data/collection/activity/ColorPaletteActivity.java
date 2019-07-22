package com.data.collection.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.network.HttpRequest;
import com.data.collection.util.PreferencesUtils;
import com.data.collection.view.TitleView;
import com.duan.colorpicker.ColorPickerView;

import butterknife.BindView;

// 颜色选择

public class ColorPaletteActivity extends BaseActivity {

    private static final String TAG = "ColorPaletteActivity";

    @BindView(R.id.title_view)
    TitleView titleView;

    @BindView(R.id.picker)
    ColorPickerView picker;

    @BindView(R.id.point_color)  // 0
            View pointColor;

    @BindView(R.id.line_color)  // 1
            View lineColor;

    @BindView(R.id.polygon_color)  // 2
            View polygonColor;
    @BindView(R.id.point_color_tv)  // 0
            TextView pointColortv;

    @BindView(R.id.line_color_tv)  // 1
            TextView lineColortv;

    @BindView(R.id.polygon_color_tv)  // 2
            TextView polygonColortv;

    @BindView(R.id.text_hint)
    TextView textHint;

    int colorType = 0;

    int pointValue = Color.BLUE;
    int lineValue = Color.RED;
    int polygonValue = 0x50225500;

    public static void start(Context context) {
        Intent intent = new Intent(context, ColorPaletteActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_palette);
        initView();
        initListener();
    }

    private void initView() {
        pointValue = PreferencesUtils.getInt(this,"pointValue", pointValue);
        lineValue = PreferencesUtils.getInt(this,"lineValue", lineValue);
        polygonValue = PreferencesUtils.getInt(this,"polygonValue", polygonValue);

        String hex = Integer.toHexString(pointValue);
        pointColor.setBackgroundColor(pointValue);
        pointColortv.setText("#" + hex);

        lineColor.setBackgroundColor(lineValue);
        hex = Integer.toHexString(lineValue);
        lineColortv.setText("#" + hex);

        polygonColor.setBackgroundColor(polygonValue);
        hex = Integer.toHexString(polygonValue);
        polygonColortv.setText("#" + hex + ", 30%透明");
    }

    private void initListener() {
        titleView.getLefticon().setOnClickListener(v -> {
            //
            PreferencesUtils.putInt(this,"pointValue", pointValue);
            PreferencesUtils.putInt(this,"lineValue", lineValue);
            PreferencesUtils.putInt(this,"polygonValue", polygonValue);
            finish();
        });

        pointColor.setOnClickListener(v -> {
            textHint.setText("选择点的颜色");
            colorType = 0;
        });
        lineColor.setOnClickListener(v -> {
            textHint.setText("选择线的颜色");
            colorType = 1;
        });
        polygonColor.setOnClickListener(v -> {
            textHint.setText("选择区域的颜色");
            colorType = 2;
        });

        picker.setOnColorPickerChangeListener(new ColorPickerView.OnColorPickerChangeListener() {
            @Override
            public void onColorChanged(ColorPickerView picker, int color) {
                String hex = Integer.toHexString(color);
                if (colorType == 0) {
                    pointColortv.setText("#" + hex);
                    pointColor.setBackgroundColor(color);
                    pointValue = color;
                } else if (colorType == 1) {
                    lineColortv.setText("#" + hex);
                    lineColor.setBackgroundColor(color);
                    lineValue = color;
                } else if (colorType == 2) {
                    color = 0x50ffffff&color;
                    hex = Integer.toHexString(color);
                    polygonColortv.setText("#" + hex + ", 30%透明");
                    polygonColor.setBackgroundColor(color);
                    polygonValue = color;
                }
            }

            @Override
            public void onStartTrackingTouch(ColorPickerView picker) {

            }

            @Override
            public void onStopTrackingTouch(ColorPickerView picker) {

            }
        });
    }


    @Override
    protected void onDestroy() {
        HttpRequest.cancleRequest(TAG);
        HttpRequest.cancleRequest("upLoadImgs");

        super.onDestroy();
    }
}
