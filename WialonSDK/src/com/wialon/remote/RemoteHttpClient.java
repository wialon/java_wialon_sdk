package com.wialon.remote;

import com.wialon.core.Session;
import com.wialon.remote.handlers.ResponseHandler;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class RemoteHttpClient {
	private static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;
	private DefaultHttpClient defaultHttpClient;
	private ThreadPoolExecutor threadPool;
	private static boolean isMultiHttpClientEnabled=false;
	private static RemoteHttpClient instance=new RemoteHttpClient();
	//private final HttpContext httpContext;

	public static RemoteHttpClient getInstance() {
		return instance;
	}

	public static boolean isMultiHttpClientEnabled(){
		return isMultiHttpClientEnabled;
	}

	public static void setMultiHttpClient(boolean enabled) {
		isMultiHttpClientEnabled=enabled;
	}

	private RemoteHttpClient() {
		defaultHttpClient=getNewHttpClient();
		threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	}

	public DefaultHttpClient getNewHttpClient() {
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
		HttpProtocolParams.setHttpElementCharset(httpParams, HTTP.UTF_8);
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);
		//httpContext = new SyncBasicHttpContext(new BasicHttpContext());
		return new DefaultHttpClient(httpParams);
	}

	public void remoteCall(String svc, String params, ResponseHandler callback) {
		remoteCall(svc, params, null, callback);
	}

	public void remoteCall(String svc, String params, DefaultHttpClient httpClient, ResponseHandler callback) {
		String url = Session.getInstance().getBaseUrl() + "/wialon/ajax.html?svc=" + svc + "&sid=" + Session.getInstance().getId();
		List<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
		if (params!=null)
			nameValuePairs.add(new BasicNameValuePair("params", params));
		post(url, nameValuePairs, httpClient, callback);
	}

	public void post(String url, List<NameValuePair> params, DefaultHttpClient httpClient, ResponseHandler callback){
		try {
			HttpPost httpPost=new HttpPost(url);
			if (params!=null)
				httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			if (isMultiHttpClientEnabled && httpClient==null)
				defaultHttpClient=getNewHttpClient();
			sendRequest((httpClient==null ? defaultHttpClient : httpClient) , httpPost, callback);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			callback.onFailure(6, e);
		}
	}

	public void post(String url, List<NameValuePair> params, ResponseHandler callback){
		post(url, params, null, callback);
	}

	protected void sendRequest(DefaultHttpClient client, /*HttpContext httpContext,*/ HttpUriRequest uriRequest, ResponseHandler callback) {
		Future<?> request = threadPool.submit(new HttpRequest(client, uriRequest, callback));
	}
}
