package com.med.drawing;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;



public class CheckUpdatePlay extends MultiDexApplication {

    private AppOpenAds appOpenManager;

    @Override
    public void onCreate() {
        super.onCreate();
        appOpenManager = new AppOpenAds(this);
    }

    @Override
    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

}
