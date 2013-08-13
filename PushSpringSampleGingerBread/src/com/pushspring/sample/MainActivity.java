package com.pushspring.sample;

import com.pushspring.sample.R;
import com.pushspring.sdk.PushSpring;
import com.google.android.gcm.GCMRegistrar;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static final String GCM_SENDERID = "";
	public static final String PS_API_KEY = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = (TextView)findViewById(R.id.text);

        if (PS_API_KEY.length() == 0)
        {
            textView.setText("You need to set a PS_API_KEY in the app in order to have the app register with PushSpring properly.");
        }

        PushSpring pushSpring = PushSpring.sharedPushSpring();
        pushSpring.onCreate(this, PS_API_KEY);

        pushSpring.setAvailableCustomerInfo();
        pushSpring.setAvailableLocationInfo();

        // optional customer info calls
        pushSpring.setCustomerId("279");
        pushSpring.setCustomerName("John Smith");
        pushSpring.increaseCustomerLifetimeValue(145);
        //pushSpring.setCustomerTwitterFacebookEmailAccount("@justinbieber", "JustinBieber", "justin@bieber.net");
        //pushSpring.setOptOut(false);

        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);

		// Get our registration id
		final String registrationId = GCMRegistrar.getRegistrationId(this);
		if(registrationId.length()==0)
		{
			// If we failed to get it assume we need to register separately
			GCMRegistrar.register(this, GCM_SENDERID);
		}
    }

	@Override
	protected void onStart() {
		PushSpring.sharedPushSpring().onSessionStart(this);
		super.onStart();
	}

	@Override
	protected void onStop() {
		PushSpring.sharedPushSpring().onSessionEnd(this);
		super.onStop();
	}

    @Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
