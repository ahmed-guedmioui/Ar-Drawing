package com.med.drawing.splash.presentaion;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.med.drawing.AdsManager;
import com.med.drawing.R;
import com.med.drawing.StartActivity;


public class SplashActivity extends Activity {


    @Override
    protected void onCreate(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            getWindow().setFlags(1024, 1024);
        }
        super.onCreate(bundle);
        setContentView(R.layout.activity_splash);
        AdsManager.getInstance().init(SplashActivity.this);
        AdsManager.getInstance().loadInterstitialAd(SplashActivity.this, getString(R.string.AdMob_Interstitial));

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                AdsManager.getInstance().showInterstitialAd(SplashActivity.this, new AdsManager.AdCloseListener() {
                    @Override
                    public void onAdClosed() {
                        startActivity(new Intent(SplashActivity.this, StartActivity.class));
                    }
                });
            }
        }, 6000);
    }


}
