package com.zozot.OEM.cloudservice;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.zozot.OEM.cloudservice.Response;
import com.zozot.OEM.cloudservice.UriBuilder;
import com.zozot.OEM.cloudservice.IHttpService;
import com.zozot.OEM.cloudservice.exception.RequestUnsuccessfulException;

/**
 * Implementation of the AIDL service. An {@link AsyncTask} is created per HTTP
 * request and the response is returned from the {@link AsyncTask}. The parsing
 * of the response is managed by the {@link Response} class.
 * 
 * <p>
 * The intent this service listens to is as per HttpService.INTENT_NAME.
 * <p>
 * The response is expected to return from AsyncTask within the time as
 * specified in HttpService.DEFAULT_TIMEOUT. An exception will be thrown
 * otherwise.
 * 
 * @author s0pau
 * 
 */
public class HttpService extends Service
{
	public static final String INTENT_NAME = "com.zozot.OEM.cloudservice.HttpService";
	private static final String TAG = HttpService.class.getSimpleName();
	public static final long DEFAULT_TIMEOUT = 5000;

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "onCreate()");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return new IHttpService.Stub()
		{
			private String apiKey = null;

			@Override
			public void setApiKey(String apiKey) throws RemoteException
			{
				this.apiKey = apiKey;
			}

			@Override
			public Response getFeed() throws RemoteException
			{
				return get(new UriBuilder().feeds().resource()+new UriBuilder().apiKeys().resource(apiKey), apiKey);
			}

			@Override
			public Response createDatapoint(String datastreamId, String sValue) throws RemoteException
			{
				String sUri=new UriBuilder().datapoints(datastreamId, sValue).resources(apiKey);
				//sUri="http://emoncms.org/input/post.json?json=%7B" + datastreamId + ":" + sValue + "%7D&apikey=" + apiKey;
				
				
//				 Builder b=new Uri.Builder();
//		 		 Uri u=b.build();
//		 		String t=u.encode(sUri);
		 		return get( sUri, "",apiKey);
				
					
			}
		};
	}

	/**
	 * Helper method to make http get request with given params.
	 * 
	 * @param uri
	 * @return response from the request made.
	 */
	private Response get(String uri, String apiKey)
	{
		Request request = new Request(Request.HTTP_METHOD_GET, uri, apiKey, null);
		return executeRequest(request);
	}

	/**
	 * Helper method to make http put request with given params.
	 * 
	 * @param uri
	 * @param body
	 * @param apiKey
	 * @return response from the request made.
	 */
	private Response put(String uri, String body, String apiKey)
	{
		Request request = new Request(Request.HTTP_METHOD_PUT, uri, apiKey, body);
		return executeRequest(request);
	}

	/**
	 * Helper method to make http put request with given params.
	 * 
	 * @param uri
	 * @param body
	 * @param apiKey
	 * @return response from the request made.
	 */
	private Response get(String uri, String body, String apiKey)
	{
		Request request = new Request(Request.HTTP_METHOD_GET, uri, body, apiKey);
		return executeRequest(request);
	}

	/**
	 * Helper method to make http delete request with given params.
	 * 
	 * @param uri
	 * @param apiKey
	 * @return response from the request made.
	 */
	private Response delete(String uri, String apiKey)
	{
		Request request = new Request(Request.HTTP_METHOD_DELETE, uri, apiKey, null);
		return executeRequest(request);
	}

	/**
	 * @param request
	 * @return the response from making the request; return a response with bad
	 *         code if exception is encountered in the process.
	 */
	private Response executeRequest(Request request)
	{
		AsyncTask<Request, Integer, Response[]> task = null;

		try
		{
			task = new HttpTask().execute(request);
		} catch (RequestUnsuccessfulException e)
		{
			String msg = "Unable to execute request";
			Log.w(TAG, msg, e);
			return ResponseHelper.writeException(msg, e);
		}

		Response[] responses = null;
		try
		{
			responses = task.get(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e)
		{
			String msg = "Unable to retrieve results from httpTask";
			Log.w(TAG, msg, e);
			return ResponseHelper.writeException(msg, e);
		} catch (ExecutionException e)
		{
			String msg = "Unable to retrieve results from httpTask";
			Log.w(TAG, msg, e);
			return ResponseHelper.writeException(msg, e);
		} catch (TimeoutException e)
		{
			String msg = "Unable to retrieve results from httpTask";
			Log.w(TAG, msg, e);
			return ResponseHelper.writeException(msg, e);
		}

		return responses != null ? responses[0] : null;
	}
}
