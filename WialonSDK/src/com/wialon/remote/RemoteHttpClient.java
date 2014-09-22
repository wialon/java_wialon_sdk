package com.wialon.remote;

import com.google.gson.JsonArray;
import com.wialon.core.Session;
import com.wialon.remote.handlers.ResponseHandler;

import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class RemoteHttpClient {
	private static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;
	private DefaultHttpClient defaultHttpClient;
	private ThreadPoolExecutor threadPool;
	private static boolean isMultiHttpClientEnabled = false;
	private static RemoteHttpClient instance = new RemoteHttpClient();
	private List<BatchCall> batchCalls=null;
	//private final HttpContext httpContext;

	public static RemoteHttpClient getInstance() {
		return instance;
	}

	public static boolean isMultiHttpClientEnabled() {
		return isMultiHttpClientEnabled;
	}

	public static void setMultiHttpClient(boolean enabled) {
		isMultiHttpClientEnabled = enabled;
	}

	private RemoteHttpClient() {
		defaultHttpClient = getNewHttpClient();
		threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	}

	public DefaultHttpClient getNewSslHttpClient(int timeoutMs) {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new TrustAllSSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			HttpProtocolParams.setHttpElementCharset(params, HTTP.UTF_8);
			HttpConnectionParams.setConnectionTimeout(params, timeoutMs);
			HttpConnectionParams.setSoTimeout(params, timeoutMs);

//			SchemeRegistry registry = new SchemeRegistry();
//			registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	public DefaultHttpClient getNewHttpClient(){
		return getNewHttpClient(DEFAULT_SOCKET_TIMEOUT);
	}

	public DefaultHttpClient getNewHttpClient(int timeoutMs) {
		return getNewSslHttpClient(timeoutMs);
//		BasicHttpParams httpParams = new BasicHttpParams();
//		HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
//		HttpProtocolParams.setHttpElementCharset(httpParams, HTTP.UTF_8);
//		// Set the timeout in milliseconds until a connection is established.
//		// The default value is zero, that means the timeout is not used.
//		HttpConnectionParams.setConnectionTimeout(httpParams, timeoutMs);
//		// Set the default socket timeout (SO_TIMEOUT)
//		// in milliseconds which is the timeout for waiting for data.
//		HttpConnectionParams.setSoTimeout(httpParams, timeoutMs);
//		//httpContext = new SyncBasicHttpContext(new BasicHttpContext());
//		return new DefaultHttpClient(httpParams);
	}

	/**
	 * Start batch - set of remote calls in one AJAX request
	 * @return {Boolean} if batch initialization has succeeded
	 */
	public boolean startBatch() {
		if (batchCalls!=null)
			return false;
//		if (owner)
//			this.__batchCallsOwner = owner;
		batchCalls = new ArrayList<BatchCall>();
		return true;
	}
	/**
	 * Finish batch - perform all delayed calls in one AJAX request
	 * @param callback function to call with result of AJAX call: callback(code, combinedCode). code is zero if no errors. combinedCode is zero if all combined requests where successfully.
	 * @return {Boolean} if batch send to the server
	 */
	public boolean finishBatch(ResponseHandler callback) {
		if (batchCalls==null) {
			callback.onFailure(2, null);
			return false;
		}
		if (batchCalls.size()==0) {
			// nothing to call
			batchCalls = null;
			callback.onFailure(0, null);
			return false;
		}
		// construct batch call json and select max timeout
		String params = "[";
		final List<ResponseHandler> callbacks = new ArrayList<ResponseHandler>();
		for (int i = 0; i < batchCalls.size(); i++) {
			BatchCall call = batchCalls.get(i);
			params+="{\"svc\":\""+call.svc+"\",\"params\":"+call.params+"}";
			if (i+1<batchCalls.size())
				params+=",";
			callbacks.add(call.callback);
		}
		params+="]";
		// reset batch call status
		batchCalls = null;
//		this.__finishBatchCalls = [];
		remoteCall("core/batch", params, new ResponseHandler(callback) {
			@Override
			public void onSuccess(String response) {
				onBatchCallCompleted(getCallback(), callbacks, response);
			}
		});
		return true;
	}

	/**
	 * Handle batch call completed event
	 * @param callback callback to call with result code
	 * @param batchCallbacks collection of callbacks for each batched call
	 * @param results resulting data
	 */
	private void onBatchCallCompleted(ResponseHandler callback, List<ResponseHandler> batchCallbacks, String results) {
		JsonArray resultsArray=Session.getInstance().getJsonParser().parse(results).getAsJsonArray();
		if (results==null || batchCallbacks==null || resultsArray==null || batchCallbacks.size() != resultsArray.size()) {
			int errorCode = 3;
			// error doing request
			// pass result for all callbacks
			if (batchCallbacks!=null)
				for (ResponseHandler batchCall : batchCallbacks)
					batchCall.onFailure(errorCode, null);
			callback.onFailure(errorCode, null);
			return;
		}
		// fire callback for each call with its data
		for (int i = 0; i < resultsArray.size(); i++) {
			batchCallbacks.get(i).onSuccess(resultsArray.get(i).toString());
		}
		// finally fire our own callback
		callback.onSuccess(results);
	}
//	/**
//	 * Handle AJAX request result and call callback
//	 * @param result {Object} result of operation, constructed from JSON
//	 * @param callback {Function?null} callback in form callback(code, data)
//	 */
//	private void handleCallResult(result, callback) {
//		if (result && typeof result.error != "undefined" && result.error != 0)
//		return callback(result.error, null);
//		else if (result)
//			return callback(0, result);
//		else
//			return callback(3, null);
//	}

	public void remoteCall(String svc, String params, ResponseHandler callback) {
		remoteCall(svc, params, null, callback);
	}

	public void remoteCall(String svc, String params, DefaultHttpClient httpClient, ResponseHandler callback) {
		if (batchCalls!=null){
			batchCalls.add(new BatchCall(svc, params, callback));
		} else {
			List<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
			if (params != null)
				nameValuePairs.add(new BasicNameValuePair("params", params));
			String url = Session.getInstance().getBaseUrl() + "/wialon/ajax.html?svc=" + svc + "&sid=" + Session.getInstance().getId();
			post(url, nameValuePairs, httpClient, callback);
		}
	}

	public void post(String url, List<NameValuePair> params, DefaultHttpClient httpClient, ResponseHandler callback) {
		try {
			HttpPost httpPost = new HttpPost(url);
			if (params != null)
				httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			if (isMultiHttpClientEnabled && httpClient == null)
				defaultHttpClient = getNewHttpClient();
			sendRequest((httpClient == null ? defaultHttpClient : httpClient), httpPost, callback);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			callback.onFailure(6, e);
		}
	}

    public void post(String svc, File file, String params, ResponseHandler callback) {
        try {
            String url = Session.getInstance().getBaseUrl() + "/wialon/ajax.html?svc=" + svc + "&sid=" + Session.getInstance().getId();
            BasicNameValuePair nameValuePair = new BasicNameValuePair("params", params);
            HttpPost httpPost = new HttpPost(url);
            MultipartEntity multipartEntity = new MultipartEntity();
            multipartEntity.addPart(nameValuePair.getName(), new StringBody(nameValuePair.getValue()));
            multipartEntity.addPart("icon_file", new FileBody(file, "image/jpeg"));
            httpPost.setEntity(multipartEntity);
         /*   defaultHttpClient = getNewHttpClient();*/
            sendRequest(defaultHttpClient, httpPost, callback);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            callback.onFailure(6, e);
        }
    }

	public void post(String url, List<NameValuePair> params, int timeoutMs, ResponseHandler callback) {
		post(url, params, getNewHttpClient(timeoutMs), callback);
	}

	public void post(String url, List<NameValuePair> params, ResponseHandler callback) {
		post(url, params, null, callback);
	}

	public void get(String url, List<NameValuePair> params, ResponseHandler callback) {
		HttpUriRequest request = new HttpGet(getUrlWithQueryString(url, params));
		sendRequest(getNewHttpClient(), request, callback);
	}

	protected void sendRequest(DefaultHttpClient client, /*HttpContext httpContext,*/ HttpUriRequest uriRequest, ResponseHandler callback) {
		Future<?> request = threadPool.submit(new HttpRequest(client, uriRequest, callback));
	}

	private static String getUrlWithQueryString(String url, List<NameValuePair> params) {
		if (params != null) {
			String paramString = URLEncodedUtils.format(params, "UTF-8");
			if (!url.contains("?")) {
				url += "?" + paramString;
			} else {
				url += "&" + paramString;
			}
		}
		return url;
	}

	private static class BatchCall{
		String svc;
		String params;
		ResponseHandler callback;

		public BatchCall(String svc, String params, ResponseHandler callback){
			this.svc=svc;
			this.params=params;
			this.callback=callback;
		}
	}
}
