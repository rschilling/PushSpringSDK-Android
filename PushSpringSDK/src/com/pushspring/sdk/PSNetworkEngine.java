package com.pushspring.sdk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.entity.ByteArrayEntity;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.content.Context;
import android.util.Log;

public class PSNetworkEngine {
	protected static final String TAG = "PushSpringSDK.PSNetworkEngine";
	protected static final String PS_DIR = ".pushspring";
	protected static final String PS_EVENTTEXT = ".psevent";
	protected static final String API_ENDPOINT = "https://api.pushspring.com/v1/i";

	private static PSNetworkEngine _instance;

	protected Context _context;

	protected LinkedBlockingQueue<String> _eventQueue;
	protected AsyncHttpClient _httpClient;
	protected Timer _retryTimer;


	private PSNetworkEngine()
	{
		_httpClient = new AsyncHttpClient();
		_eventQueue = new LinkedBlockingQueue<String>();
		new Thread(new Consumer()).start();
		_retryTimer = new Timer();
		_retryTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// Get the list of files in the directory
				File eventStore = new File(getPathToEventStore());
				File[] files = eventStore.listFiles();
				for(File file : files)
				{
					_eventQueue.add(file.getName());
				}
			}
		}, 5 * 60 * 1000, 5 * 60 * 1000);
	}

	public static PSNetworkEngine sharedNetworkEngine()
	{
		if(_instance==null)
			_instance = new PSNetworkEngine();

		return _instance;
	}

	public void enqueueEvent(HashMap<String,Object> event)
	{
		String eventId = persistEvent(event);
		_eventQueue.add(eventId);
	}

	protected void removeEvent(String eventId)
	{
		File eventFile = new File(pathToEventId(eventId));

		if(eventFile.exists())
		{
			eventFile.delete();
		}
	}

	private String persistEvent(HashMap<String, Object> event) {
		String eventId = String.format("%s%s", event.get(PushSpring.PS_ATTR_TIMESTAMP), event.get(PushSpring.PS_ATTR_EVENT_NAME));
		File eventFile = new File(pathToEventId(eventId));

	    // it's possible the path may already exist if this is a re-enqueued event being retried.  If so,
	    // don't overwrite the file
		if(!eventFile.exists())
		{
			writeEventToFile(eventFile, event);
		}

		return eventId;
	}

	private String loadEvent(String eventId) {
		File eventFile = new File(pathToEventId(eventId));

		if(eventFile.exists())
		{
			return readEventFromFile(eventFile);
		}
		else
		{
			return null;
		}
	}

	private void writeEventToFile(File eventFile, HashMap<String, Object> event)
	{
		BufferedWriter writer = null;
		try {
			Gson gson = new Gson();
			String json = gson.toJson(event);

			writer = new BufferedWriter(new FileWriter(eventFile));
			writer.write(json);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally {
			try {
				writer.close();
			} catch(Exception e) {

			}
		}
	}

	private String readEventFromFile(File eventFile)
	{
		BufferedReader reader = null;
		String event = null;
		try {
			reader = new BufferedReader(new FileReader(eventFile));
			StringBuilder builder = new StringBuilder();
			String line = "";

			while ((line = reader.readLine()) != null) {
			    builder.append(line);
			}

			event = builder.toString();
		}
		catch(Exception e) {
			// Log.d(TAG, e.getStackTrace().toString());
			// e.printStackTrace();
		}
		finally {
			try {
				reader.close();
			}
			catch(Exception e) {

			}
		}

		return event;
	}


	private String pathToEventId(String eventId) {
		return new File(getPathToEventStore(), String.format("%s%s", eventId, PS_EVENTTEXT)).getPath();
	}


	private String getPathToEventStore() {
		File eventStorePath =  new File(_context.getApplicationInfo().dataDir, PS_DIR);
		if(!eventStorePath.exists())
		{
			eventStorePath.mkdirs();
		}

		return eventStorePath.getPath();
	}


	class Consumer implements Runnable
	{
		@Override
		public void run() {
			try {
				while(true)
				{
					String eventId = _eventQueue.take();
//					Log.d(TAG, "Dequeued event");

					transmitEvent(eventId);
				}
			}
			catch(InterruptedException e) {
//				Log.d(TAG, "Consumer interrupted");
			}

		}

		private void transmitEvent(final String eventId) {
			String eventJson = loadEvent(eventId);
			if(eventJson!=null && eventJson.length()!=0)
			{
				_httpClient.addHeader("X-ClientVersion", PushSpring.PS_CLIENT_VERSION);
				_httpClient.addHeader("Content-Type", "application/json");

				ByteArrayEntity entity = null;
				try {
					entity = new ByteArrayEntity(eventJson.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					// Log.d(TAG, e.getStackTrace().toString());
					// e.printStackTrace();
				}
				if(entity!=null)
				{
					_httpClient.post(_context, API_ENDPOINT, entity, "application/json", new AsyncHttpResponseHandler() {
						@Override
						public void onFailure(Throwable e, String response) {
							Log.d(TAG, String.format("EventId: %s failed. response: %s.",eventId,response));
							super.onFailure(e, response);
						}

						@Override
					    public void onSuccess(String response) {
					        removeEvent(eventId);
					    }
					});
				}
				else
				{
					Log.d(TAG, "Failed to convert json to ByteArrayEntity");
				}
			}
		}

	}

	public void setContext(Context context) {
		_context = context;
	}

}
