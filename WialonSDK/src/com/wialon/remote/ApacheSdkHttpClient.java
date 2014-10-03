package com.wialon.remote;


import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ApacheSdkHttpClient implements BaseSdkHttpClient {
	private DefaultHttpClient defaultHttpClient;
	private ThreadPoolExecutor threadPool;
	private SchemeRegistry registry;

	public ApacheSdkHttpClient() {
		initDefaultClient();
		threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
	}

	private static BasicHttpParams getBasicHttpParams(int timeout){
		BasicHttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		HttpProtocolParams.setHttpElementCharset(params, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpConnectionParams.setSoTimeout(params, timeout);
		return params;
	}

	private void initDefaultClient(){
		BasicHttpParams httpParams = getBasicHttpParams(DEFAULT_SOCKET_TIMEOUT);
		KeyStore trustStore;
		SSLSocketFactory sf=null;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			sf = new TrustAllSSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		} catch (Exception e) {
			e.printStackTrace();
		}
		registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		if (sf!=null)
			registry.register(new Scheme("https", sf, 443));
		ThreadSafeClientConnManager clientConnectionManager = new ThreadSafeClientConnManager(httpParams, registry);
		defaultHttpClient=new DefaultHttpClient(clientConnectionManager, httpParams);
	}

	@Override
	public void post(String url, Map<String, String> params, Callback callback, int timeout) {
		try {
			HttpPost httpPost = new HttpPost(url);
			if (params != null) {
				List<NameValuePair> pairs = paramsMapToParamsList(params);
				httpPost.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
			}
			sendRequest(getHttpClient(timeout), httpPost, callback);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			callback.error=e;
			callback.done(null);
		}
	}

	@Override
	public void get(String url, Map<String, String> params, Callback callback, int timeout) {
		HttpUriRequest request = new HttpGet(getUrlWithQueryString(url, paramsMapToParamsList(params)));
		sendRequest(getHttpClient(timeout), request, callback);
	}

	@Override
	public void postFile(String url, Map<String, String> params, Callback callback, int timeout, File file) {
		try {
			HttpPost httpPost = new HttpPost(url);
			MultipartEntity multipartEntity = new MultipartEntity();
			if (params!=null)
				for (Map.Entry<String, String> entry : params.entrySet())
					multipartEntity.addPart(entry.getKey(), new StringBody(entry.getValue()));
			multipartEntity.addPart("file", new FileBody(file));
			httpPost.setEntity(multipartEntity);
			sendRequest(getHttpClient(timeout), httpPost, callback);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			callback.error=e;
			callback.done(null);
		}
	}

	private static List<NameValuePair> paramsMapToParamsList(Map<String, String> params) {
		List<NameValuePair> result = new LinkedList<NameValuePair>();
		for (Map.Entry<String, String> entry : params.entrySet())
			result.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		return result;
	}

	public DefaultHttpClient getHttpClient(int timeoutMs) {
		if (timeoutMs==0 || timeoutMs==DEFAULT_SOCKET_TIMEOUT)
			return defaultHttpClient;
		else {
			BasicHttpParams params=getBasicHttpParams(timeoutMs);
			ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager(params, registry);
			return new DefaultHttpClient(connectionManager, params);
		}
	}

	private void sendRequest(DefaultHttpClient client, HttpUriRequest uriRequest, Callback callback) {
		threadPool.submit(new HttpRequest(client, uriRequest, callback));
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

	private class HttpRequest implements Runnable {
		private final Callback callback;
		private final AbstractHttpClient client;
		private final HttpUriRequest request;

		HttpRequest(AbstractHttpClient client, HttpUriRequest request, Callback callback) {
			this.callback = callback;
			this.client = client;
			this.request = request;
		}

		@Override
		public void run() {
			try {
				HttpResponse httpResponse = client.execute(request);
				//StatusLine status = httpResponse.getStatusLine();
				HttpEntity temp = httpResponse.getEntity();
				callback.done(EntityUtils.toByteArray(temp));
			} catch (Exception e) {
				e.printStackTrace();
				callback.error=e;
				callback.done(null);
			}
		}
	}

	private class TrustAllSSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public TrustAllSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}
}
