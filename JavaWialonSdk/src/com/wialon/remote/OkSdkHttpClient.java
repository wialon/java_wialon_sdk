/*
 * Copyright 2014 Gurtam
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 */

package com.wialon.remote;

import com.squareup.okhttp.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class OkSdkHttpClient implements BaseSdkHttpClient {
	private OkHttpClient defaultClient;
	private ThreadPoolExecutor threadPool;

	public OkSdkHttpClient(){
		defaultClient = getHttpClient(DEFAULT_SOCKET_TIMEOUT);
		threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
	}

	@Override
	public void post(String url, Map<String, String> params, final Callback callback, int timeout) {
		Request request=new Request.Builder().url(url).post(paramsMapToRequestBody(params)).build();
		threadPool.submit(new HttpRequest(getHttpClient(timeout), request, callback));
	}

	@Override
	public void get(String url, Map<String, String> params, Callback callback, int timeout) {
		Request request=new Request.Builder().url(getUrlWithQueryString(url, params)).get().build();
		threadPool.submit(new HttpRequest(getHttpClient(timeout), request, callback));
	}

	@Override
	public void postFile(String url, Map<String, String> params, Callback callback, int timeout, File file) {
		//Method will work at 2.1 version of library https://github.com/square/okhttp/issues/963 and https://github.com/square/okhttp/pull/969
		MultipartBuilder builder=new MultipartBuilder();
		builder.type(MultipartBuilder.FORM);
		RequestBody paramsBody=paramsMapToRequestBody(params);
		if (paramsBody!=null) {
			builder.addPart(paramsBody);
		}
		builder.addPart(RequestBody.create(MediaType.parse(""), file));
		Request request=new Request.Builder().url(url).post(builder.build()).build();
		threadPool.submit(new HttpRequest(getHttpClient(timeout), request, callback));
	}

	private static RequestBody paramsMapToRequestBody(Map<String, String> params){
		if (params!=null){
			FormEncodingBuilder builder=new FormEncodingBuilder();
			for (Map.Entry<String, String> entry : params.entrySet())
				builder.add(entry.getKey(), entry.getValue());
			return builder.build();
		}
		return null;
	}

	public OkHttpClient getHttpClient(int timeout){
		if ((timeout==0 || timeout==DEFAULT_SOCKET_TIMEOUT) && defaultClient!=null)
			return defaultClient;
		else {
			OkHttpClient client = new OkHttpClient();
			timeout=timeout==0 ? DEFAULT_SOCKET_TIMEOUT : timeout;
			client.setConnectTimeout(timeout, TimeUnit.MILLISECONDS);
			client.setWriteTimeout(timeout, TimeUnit.MILLISECONDS);
			client.setReadTimeout(timeout, TimeUnit.MILLISECONDS);
			return client;
		}
	}

	private static String getUrlWithQueryString(String url, Map<String, String> params) {
		StringBuilder content = new StringBuilder();
		content.append(url);
		if (!url.contains("?"))
			content.append("?");
		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()){
				if (!content.toString().endsWith("?"))
					content.append("&");
				try {
					content.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
							.append("=")
							.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return content.toString();
	}

	private class HttpRequest implements Runnable{
		private final Request request;
		private final Callback callback;
		private final OkHttpClient client;

		HttpRequest(OkHttpClient client, Request request, Callback callback){
			this.request=request;
			this.callback=callback;
			this.client=client;
		}

		@Override
		public void run() {
			try {
				Response response=client.newCall(request).execute();
				callback.done(response.body().bytes());
			} catch (IOException e) {
				e.printStackTrace();
				callback.error=e;
				callback.done(null);
			}
		}
	}
}
