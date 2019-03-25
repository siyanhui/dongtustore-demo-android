package com.siyanhui.mojif.demo;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.dongtu.store.DongtuStore;

public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        /*
         * 首先从AndroidManifest.xml中取得appId和appSecret，然后进行初始化
         */
        try {
            Bundle bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
            DongtuStore.initConfig(this, bundle.getString("dtstore_app_id"), bundle.getString("dtstore_app_secret"));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
