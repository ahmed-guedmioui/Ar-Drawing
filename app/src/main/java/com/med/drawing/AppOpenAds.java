package com.med.drawing;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

public class AppOpenAds implements LifecycleObserver,Application.ActivityLifecycleCallbacks {
    private static final String LOG_TAG = "AppOpenManager";
    private AppOpenAd appOpenAd = null;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private final CheckUpdatePlay myApplication;
    private Activity currentActivity;
    private static boolean isShowingAd = false;
    String Openads;


    public  void AppOpenShow() {
        if (!isShowingAd && isAdAvailable()) {
            Log.d(LOG_TAG, "Will show ad.");
            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    AppOpenAds.this.appOpenAd = null;
                    isShowingAd = false;
                    AppOpenGetAds();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {}

                @Override
                public void onAdShowedFullScreenContent() {
                    isShowingAd = true;
                }
            };
            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(currentActivity);
        } else {
            Log.d(LOG_TAG, "Can not show ad.");
            AppOpenGetAds();
        }
    }
    public AppOpenAds(CheckUpdatePlay myApplication) {
        this.myApplication = myApplication;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    public void AppOpenGetAds() {

        if (isAdAvailable()) {
            return;
        }
        loadCallback =
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(AppOpenAd ad) {
                        AppOpenAds.this.appOpenAd = ad;
                    }
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                    }

                };
    final AdRequest request = getAdRequest();

                    AppOpenAd.load(
                            myApplication, currentActivity.getString(R.string.AdMob_OpenApp), request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);


    }

    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    public boolean isAdAvailable() {
        return appOpenAd != null;
    }
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}

    @Override
    public void onActivityDestroyed(Activity activity) {
        currentActivity = null;
    }

    @OnLifecycleEvent(ON_START)
    public void onStart() {
        AppOpenShow();
        Log.d(LOG_TAG, "onStart");

    }

}
