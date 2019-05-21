package com.data.collection;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.data.collection.activity.BaseActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
        goToMain();
    }

    private void goToMain() {
        Timer timer = new Timer();
        timer.schedule(new MyTask(), 1000);// 定时器延时执行任务的方法
    }

    private void requestPermission() {
        final RxPermissions rxPermissions = new RxPermissions(this);

        rxPermissions.request(Manifest.permission.CAMERA).subscribe(
                granted -> {
                    if (granted) { // Always true pre-M
                        //
                    } else {
                        // Oups permission denied
                    }
                }
        );

        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(
                granted -> {
                    if (granted) { // Always true pre-M

                    } else {
                        // Oups permission denied
                    }
                }
        );

    }



    class MyTask extends TimerTask {
        @Override
        public void run() {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
        }
    }

}
