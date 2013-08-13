package com.pushspring.sdk;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressWarnings("serial")
public class PushSpringNotificationActivity extends Activity {
	   private WebView mWebView;
	   
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        Intent intent = getIntent();
	        final String campaignUrl = intent.getStringExtra(PushSpring.PS_ATTR_CAMPAIGNURL);	 
	        final String campaignId = intent.getStringExtra(PushSpring.PS_ATTR_CAMPAIGNID);
	        
	        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
	        mWebView = new WebView(this);
	        mWebView.loadUrl(campaignUrl);
	        mWebView.setWebViewClient(new WebViewClient() {
	            @Override
	            public boolean shouldOverrideUrlLoading(WebView view, String url) {
	                view.loadUrl(url);
	                return true;
	            }
	        });
	 
	        this.setContentView(mWebView);
	        
	        PushSpring.sharedPushSpring().recordEvent(PushSpring.PS_CAMPAIGN_DISPLAYED, new HashMap<String,Object>() {{
        		put(PushSpring.PS_ATTR_CAMPAIGNID, campaignId);
        		put(PushSpring.PS_ATTR_CAMPAIGNURL, campaignUrl);
        	}});

	    }
}
