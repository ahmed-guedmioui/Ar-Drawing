package com.med.drawing.other;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.med.drawing.R;


public class PrivacyPolicyActivity extends Activity {


    @Override 
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        AppConstants.overridePendingTransitionEnter(this);
        requestWindowFeature(1);
        setContentView(R.layout.activity_privacy_policy);


        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(getString(R.string.privacy));
    }



    @Override
    public void onBackPressed() {
       super.onBackPressed();
    }

}
