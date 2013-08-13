package com.pushspring.sdk;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.provider.Settings.Secure;
import android.util.Log;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public class PushSpring {
	public static final String PS_CLIENT_VERSION = "Android_1.0.1";


	// Standard events
	public static final String PS_CAMPAIGN_DISPLAYED = "PS_CAMPAIGN_DISPLAYED";
	public static final String PS_CUSTOMER_INFO = "PS_CUSTOMER_INFO";
	public static final String PS_DEVICE_INFO = "PS_DEVICE_INFO";
	public static final String PS_DEVICE_TOKEN = "PS_DEVICE_TOKEN";
	public static final String PS_LOCATION = "PS_LOCATION";
	public static final String PS_SESSION_START = "PS_SESSION_START";
	public static final String PS_SESSION_END = "PS_SESSION_END";

	// Standard event attributes
	public static final String PS_ATTR_APIKEY = "PS_APIKEY";
	public static final String PS_ATTR_APPVER = "PS_APPVER";
	public static final String PS_ATTR_APPBUNDLEID = "PS_APPBUNDLEID";
	public static final String PS_ATTR_CAMPAIGNID = "PS_CAMPAIGNID";
	public static final String PS_ATTR_CAMPAIGNURL = "PS_CAMPAIGNURL";
	public static final String PS_ATTR_CLIENTVER = "PS_CLIENTVER";
	public static final String PS_ATTR_CLVINCREASE = "PS_CLVINCREASE";
	public static final String PS_ATTR_CUSTOMEREMAIL = "PS_CUSTOMEREMAIL";
	public static final String PS_ATTR_CUSTOMERFACEBOOK = "PS_CUSTOMERFACEBOOK";
	public static final String PS_ATTR_CUSTOMERID = "PS_CUSTOMERID";
	public static final String PS_ATTR_CUSTOMERNAME = "PS_CUSTOMERNAME";
	public static final String PS_ATTR_CUSTOMERTWITTER = "PS_CUSTOMERTWITTER";
	public static final String PS_ATTR_DEVICEID = "PS_DEVICEID";
	public static final String PS_ATTR_DEVICEMODEL = "PS_DEVICEMODEL";
	public static final String PS_ATTR_DEVICESIZE = "PS_DEVICESIZE";
	public static final String PS_ATTR_DEVICETOKEN = "PS_DEVICETOKEN";
	public static final String PS_ATTR_EVENT_NAME = "PS_EVENT_NAME";
	public static final String PS_ATTR_ISJAILBROKEN = "PS_ISJAILBROKEN";
	public static final String PS_ATTR_LANGUAGE = "PS_LANGUAGE";
	public static final String PS_ATTR_LATITUDE = "PS_LATITUDE";
	public static final String PS_ATTR_LONGITUDE = "PS_LONGITUDE";
	public static final String PS_ATTR_SYSTEMVER = "PS_SYSTEMVER";
	public static final String PS_ATTR_TIMESTAMP = "PS_TIMESTAMP";
	public static final String PS_ATTR_TIMEZONEOFFSET = "PS_TIMEZONEOFFSET";
	public static final String PS_ATTR_USER_OPT_OUT = "PS_USER_OPT_OUT";

	// Keys on inbound notifications
	public static final String PS_CAMPAIGN_ID = "PS_CAMPAIGN_ID";
	public static final String PS_CAMPAIGN_URL = "PS_CAMPAIGN_URL";

	protected static final String TAG = "PushSpringSDK.PushSpring";

	private static PushSpring _instance;
	private Context _context;
	private String _apiKey = null;
	private LinkedList<Context> _sessions;

	private PushSpring() {
		_sessions = new LinkedList<Context>();
	}

	public static PushSpring sharedPushSpring()
	{
		if(_instance==null)
			_instance = new PushSpring();

		return _instance;
	}

	public void onCreate(Context context, final String apiKey)
	{
		// TODO: Need to figure out how to handle app launch from notification
		this._apiKey = apiKey;
		this._context = context;

		PSNetworkEngine.sharedNetworkEngine().setContext(context);

		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			PSActivityLifecycleCallbacks callbacks = new PSActivityLifecycleCallbacks();
			((Application)(context.getApplicationContext())).registerActivityLifecycleCallbacks(callbacks);
		}

		recordEvent(PS_DEVICE_INFO, getDeviceInfo());
	}

	public void onSessionStart(Context context) {
		if(_sessions.size()==0)
		{
			recordEvent(PS_SESSION_START);
		}
		_sessions.addLast(context);
	}

	public void onSessionEnd(Context context) {
		_sessions.remove(context);
		if(_sessions.size()==0)
		{
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					if(_sessions.size()==0)
					{
						recordEvent(PS_SESSION_END);
					}
				}
			}, 10 * 1000);
		}
	}

	public void onRegistered(final String registrationId)
	{
		Log.d(TAG, registrationId);
		recordEvent(PS_DEVICE_TOKEN, new HashMap<String,Object>() {{
			put(PS_ATTR_DEVICETOKEN, registrationId);
		}});
	}

	public void onUnregistered() {
		Log.d(TAG, "Unregister");
		recordEvent(PS_DEVICE_TOKEN, new HashMap<String,Object>() {{
			put(PS_ATTR_DEVICETOKEN, "");
		}});
	}

	public boolean onMessage(Context context, Intent message)
	{
		HashMap<String,Object> notification = new HashMap<String,Object>();
		Bundle extras=message.getExtras();
		 for (String key : extras.keySet()) {
			 notification.put(key, extras.getString(key));
		 }

		 return processInboundNotification(notification);
	}

	public void setCustomerId(final String customerId)
	{
		recordEvent(PS_CUSTOMER_INFO, new HashMap<String,Object>() {{
			put("PS_ATTR_CUSTOMERID", customerId);
		}});
	}

	public void setCustomerName(final String customerName)
	{
		recordEvent(PS_CUSTOMER_INFO, new HashMap<String,Object>() {{
			put("PS_ATTR_CUSTOMERNAME", customerName);
		}});
	}

	public void setCustomerTwitterFacebookEmailAccount(final String twitter, final String facebook, final String email)
	{
		HashMap<String,Object> attributes = new HashMap<String,Object>();
		if(twitter!=null)
		{
			attributes.put(PS_ATTR_CUSTOMERTWITTER, twitter);
		}

		if(facebook!=null)
		{
			attributes.put(PS_ATTR_CUSTOMERFACEBOOK, facebook);
		}

		if(email!=null)
		{
			attributes.put(PS_ATTR_CUSTOMEREMAIL, email);
		}

		recordEvent(PS_CUSTOMER_INFO, attributes);
	}

	public void setAvailableCustomerInfo()
	{
		// Get accounts
		AccountManager am = AccountManager.get(_context);

		// Google account
		Account[] googleAccounts = am.getAccountsByType("com.google");
		String googleAccountName = null;
		if(googleAccounts!=null && googleAccounts.length > 0)
		{
			googleAccountName = googleAccounts[0].name;

		}

		// Twitter account
		Account[] twitterAccounts = am.getAccountsByType("com.twitter.android.auth.login");
		String twitterAccountName = null;
		if(twitterAccounts!=null && twitterAccounts.length > 0)
		{
			twitterAccountName = twitterAccounts[0].name;
		}

		// Facebook account
		Account[] facebookAccounts = am.getAccountsByType("com.facebook.auth.login");
		String facebookAccountName = null;
		if(facebookAccounts!=null && facebookAccounts.length > 0)
		{
			facebookAccountName = facebookAccounts[0].name;
		}

		setCustomerTwitterFacebookEmailAccount(twitterAccountName, facebookAccountName, googleAccountName);
	}


	public void setLocation(final double latitude, final double longitude)
	{
		final DecimalFormat latLongFormat = new DecimalFormat("#.000000");
		recordEvent(PS_LOCATION, new HashMap<String,Object>() {{
			put(PS_ATTR_LATITUDE, latLongFormat.format(latitude));
			put(PS_ATTR_LONGITUDE, latLongFormat.format(longitude));
		}});
	}

	public void setAvailableLocationInfo()
	{
		// Wrapped in a TRY/CATCH in case the application being integrated with doesn't set the
		// permission request for GPS access in the manifest
		try
		{
			LocationManager locationManager = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);
			boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// Check if enabled and if not send user to the GSP settings
			// Better solution would be to display a dialog and suggesting to
			// go to the settings

			// TODO: Should show AlarmDialog and ask if the user wants to turn on GPS if off

			if (enabled) {
			    Criteria criteria = new Criteria();
			    String provider = locationManager.getBestProvider(criteria, false);
			    Location location = locationManager.getLastKnownLocation(provider);

			    // Initialize the location fields
			    if (location != null) {
			      Log.d(TAG, "Location provider selected: " + provider);
			      setLocation(location.getLatitude(),location.getLongitude());
			    }
			}
		}
		catch(Exception e)
		{
			Log.d(TAG, e.getMessage());
		}
	}

	public void increaseCustomerLifetimeValue(final int cents)
	{
		recordEvent(PS_CUSTOMER_INFO, new HashMap<String,Object>() {{
			put(PS_ATTR_CLVINCREASE, cents);
		}});

	}

	public void setOptOut(boolean optOut)
	{
		recordEvent(PS_CUSTOMER_INFO,new HashMap<String,Object>() {{
			put(PS_ATTR_USER_OPT_OUT, "true");
			}});
	}

	public void recordEvent(String eventName)
	{
		recordEvent(eventName, new HashMap<String,Object>());
	}


	protected void recordEvent(String eventName, HashMap<String,Object> attributes)
	{
		formatAndTransmitEvent(eventName, attributes);
	}

	protected boolean processInboundNotification(HashMap<String,Object> notification)
	{
		// Attempt to get the campaign info
		String campaignId = (String)notification.get(PS_ATTR_CAMPAIGNID);
		String campaignUrl = (String)notification.get(PS_ATTR_CAMPAIGNURL);

		// If we didn't get any campaign info get out
		if(campaignId==null || campaignUrl == null)
		{
			return false;
		}

		showWebAlert(_context, campaignUrl, campaignId);

		return true;
	}

	protected void showWebAlert(Context context, final String campaignUrl, final String campaignId)
	{
		Intent intent = new Intent(context, PushSpringNotificationActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(PS_ATTR_CAMPAIGNID, campaignId);
		intent.putExtra(PS_ATTR_CAMPAIGNURL, campaignUrl);
		context.startActivity(intent);
	}

	protected void formatAndTransmitEvent(String eventName, HashMap<String,Object> attributes)
	{
		// Make sure we have an api key
		if((_apiKey==null) || (_apiKey.length() == 0))
		{
			Log.d(TAG, "Please set an api key for the PushSpring SDK.");
			return;
		}

		// Convert to json
		Gson gson = new Gson();
		String json = gson.toJson(attributes);


		long timeStamp = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
		// Log the event
		Log.d(TAG, String.format("recordEvent %s %s",eventName, json));

		// Add the core elements
		HashMap<String, Object> dict = new HashMap<String, Object>();
		dict.put(PS_ATTR_EVENT_NAME, eventName);
		dict.put(PS_ATTR_TIMESTAMP, timeStamp);
		dict.put(PS_ATTR_DEVICEID, getDeviceId());
		dict.put(PS_ATTR_CLIENTVER, PS_CLIENT_VERSION);
		dict.put(PS_ATTR_APIKEY, _apiKey);

		// Merge in the elements
		dict.putAll(attributes);

		// Enqueue event
		PSNetworkEngine.sharedNetworkEngine().enqueueEvent(dict);
	}

	protected String getDeviceId() {
		return Secure.getString(_context.getContentResolver(),
                Secure.ANDROID_ID);
	}

	protected String getAppVersion()
	{
		return Build.VERSION.RELEASE;
	}

	protected String getDeviceModel()
	{
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return model;
		} else {
			return manufacturer + " " + model;
		}
	}

	protected String getSystemVersion()
	{
		return Integer.toString(android.os.Build.VERSION.SDK_INT);
	}

	protected String getDeviceSize()
	{
		File[] roots = File.listRoots();

		long totalSize = 0;
		for(int index = 0; index<roots.length;index++)
		{
			StatFs fs = new StatFs(roots[index].toString());
			totalSize += fs.getBlockSize() * fs.getBlockCount();
		}

		return Long.toString(totalSize);
	}

	protected String getTimeZone()
	{
		return Integer.toString(TimeZone.getDefault().getRawOffset());
	}

	protected String getLanguage()
	{
		return Locale.getDefault().getDisplayLanguage();
	}

	protected HashMap<String,Object> getDeviceInfo()
	{
		HashMap<String,Object> deviceInfo = new HashMap<String,Object>();
		deviceInfo.put(PS_ATTR_DEVICEID, getDeviceId());
		deviceInfo.put(PS_ATTR_DEVICEMODEL, getDeviceModel());
		deviceInfo.put(PS_ATTR_DEVICESIZE, getDeviceSize());
		deviceInfo.put(PS_ATTR_SYSTEMVER, getSystemVersion());
		deviceInfo.put(PS_ATTR_APPVER, getAppVersion());
		deviceInfo.put(PS_ATTR_TIMEZONEOFFSET, getTimeZone());
		deviceInfo.put(PS_ATTR_LANGUAGE, getLanguage());

		return deviceInfo;
	}

}
