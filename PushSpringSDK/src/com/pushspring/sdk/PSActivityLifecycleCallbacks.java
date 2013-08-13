package com.pushspring.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;

@SuppressLint("NewApi")
public class PSActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {
	public String TAG = "PushSpringSDK.PSActivityLifecycleCallbacks";
	
	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		Log.d(TAG, "ActivityCreated - " + getSimpleActivityName(activity));
	}

	@Override
	public void onActivityDestroyed(Activity activity) {
		Log.d(TAG, "ActivityDestroyed - " + getSimpleActivityName(activity));
	}

	@Override
	public void onActivityPaused(Activity activity) {
		Log.d(TAG, "ActivityPaused - " + getSimpleActivityName(activity));
	}

	@Override
	public void onActivityResumed(Activity activity) {
		Log.d(TAG, "ActivityResumed - " + getSimpleActivityName(activity));
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
		
	}

	@Override
	public void onActivityStarted(Activity activity) {
		Log.d(TAG, "ActivityStarted - " + getSimpleActivityName(activity));
		PushSpring.sharedPushSpring().onSessionStart(activity);
	}

	@Override
	public void onActivityStopped(Activity activity) {
		Log.d(TAG, "ActivityStopped - " + getSimpleActivityName(activity));
		PushSpring.sharedPushSpring().onSessionEnd(activity);
	}
	
	protected String getSimpleActivityName(Activity activity) {
		return activity.getClass().getSimpleName();
	}

}
