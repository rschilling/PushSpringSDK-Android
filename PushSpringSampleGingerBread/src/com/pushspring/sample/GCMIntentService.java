package com.pushspring.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.pushspring.sdk.PushSpring;
import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	public GCMIntentService() {
		super(MainActivity.GCM_SENDERID);
	}


	@Override
	protected void onError(Context context, String errorMessage) {
		Log.d(getClass().getSimpleName(), "onError: " + errorMessage);
	}

	@Override
	protected void onMessage(Context context, Intent message) {
		Bundle extras=message.getExtras();
		 for (String key : extras.keySet()) {
		      Log.d(getClass().getSimpleName(),
		            String.format("onMessage: %s=%s", key,
		                          extras.getString(key)));
		 }

		 PushSpring.sharedPushSpring().onMessage(context, message);
	 }

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.d(getClass().getSimpleName(), "onRegistered: " + registrationId);
		PushSpring.sharedPushSpring().onRegistered(registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		// TODO Auto-generated method stub
		Log.d(getClass().getSimpleName(), "onUnregistered: " + registrationId);
		PushSpring.sharedPushSpring().onUnregistered();
	}

}
