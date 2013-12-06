package com.pushspring.sample;

import com.pushspring.sdk.PushSpring;
import com.google.android.gcm.GCMRegistrar;
import com.pushspring.sample.R;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class MainActivity extends Activity {
    // Put in a valid GCM Sender ID here
	public static final String GCM_SENDERID = "508472107403";
    // PushSpring: Put in a valid PushSpring API key in order to properly register
    // this user with PushSpring
	// public static final String PS_API_KEY = "62b37d7a-7421-4a1d-a009-2cdffcbd04d0";
    public static final String PS_API_KEY = "3fd210ca-f136-4881-a890-779b97320bb2";

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
        //pushSpring.setOptOut(true);

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

    // This is only needed if your app will possibly run on a pre-ICS (pre android-14) device
    // where ActivityLifecycleCallbacks don't exist
    @Override
    protected void onStart() {
        super.onStart();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            PushSpring.sharedPushSpring().onSessionStart(this);
        }
    }

    // This is only needed if your app will possibly run on a pre-ICS (pre android-14) device
    // where ActivityLifecycleCallbacks don't exist
    @Override
    protected void onStop() {
        super.onStop();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            PushSpring.sharedPushSpring().onSessionEnd(this);
        }
    }
}
