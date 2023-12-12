package com.med.drawing.other;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.med.drawing.R;


public class StartActivity extends AppCompatActivity {
    public static Activity start_activity;
    Animation push_animation;
    TextView privacypolicy;

    RelativeLayout rel_trace_paper, start_trace_layout;
    boolean doubleBackToExitPressedOnce = false;

    public void NativeADmob() {
        AdsManager.getInstance().showNativeAds(StartActivity.this, (FrameLayout) findViewById(R.id.flNative), getString(R.string.AdMob_Native));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AdsManager.destroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        AdsManager.getInstance().loadInterstitialAd(StartActivity.this, getString(R.string.AdMob_Interstitial));
    }
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_start);
        start_activity = this;
        NativeADmob();
        this.push_animation = AnimationUtils.loadAnimation(this, R.anim.view_push);

        this.rel_trace_paper = (RelativeLayout) findViewById(R.id.rel_trace_paper);
        this.start_trace_layout = (RelativeLayout) findViewById(R.id.rel_start_trace);
        this.privacypolicy = (TextView) findViewById(R.id.privacypolicy);
        privacypolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, PrivacyPolicyActivity.class));
            }
        });


        if (Build.VERSION.SDK_INT >= 24) {
            try {
                StrictMode.class.getMethod("disableDeathOnFileUriExposure", new Class[0]).invoke(null, new Object[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.start_trace_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(StartActivity.this.push_animation);
                AppConstant.selected_id = AppConstant.TraceDirect;
                StartActivity.this.DrawingListScreen();
            }
        });
        this.rel_trace_paper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(StartActivity.this.push_animation);
                AppConstant.selected_id = AppConstant.TracePaper;
                StartActivity.this.DrawingListScreen();
            }
        });

    }

    public void DrawingListScreen() {
        AdsManager.getInstance().showInterstitialAd(StartActivity.this, new AdsManager.AdCloseListener() {
            @Override
            public void onAdClosed() {
                startActivity(new Intent(StartActivity.this, SketchListActivity.class));
            }
        });
    }


    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "double tap to exit!", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}
