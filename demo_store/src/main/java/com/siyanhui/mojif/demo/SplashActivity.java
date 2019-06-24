package com.siyanhui.mojif.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

/**
 * 启动画面 (1)判断是否是首次加载应用-采取读取SharedPreferences的方法 (2)是，则进入GuideActivity；否，则进入MainActivity (3)3s后执行2)操作
 */
public class SplashActivity extends Activity {
    private static final long SPLASH_DELAY_MILLIS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dtstore_splash_activity);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this, EntryActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DELAY_MILLIS);
    }
}
