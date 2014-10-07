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

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.wialon.core.Session;
import com.wialon.remote.handlers.BinaryResponseHandler;
import com.wialon.remote.handlers.ResponseHandler;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
/**
 * Wialon remote connection instance.
 * Contain all methods for interacting with remote server.
 */
public class RemoteHttpClient {
	private static RemoteHttpClient instance = new RemoteHttpClient();
	private Map<Long, List<BatchCall>> batchCalls;
	private BaseSdkHttpClient httpClient;

	public static RemoteHttpClient getInstance() {
		return instance;
	}

	private RemoteHttpClient() {
		batchCalls = new HashMap<Long, List<BatchCall>>();
	}

	public void setHttpClient(BaseSdkHttpClient client) {
		httpClient = client;
	}

	public BaseSdkHttpClient getHttpClient() {
		return httpClient;
	}

	private void lazyInitDefaultHttpClient() {
		if (httpClient == null)
			httpClient = new ApacheSdkHttpClient();
	}

	/**
	 * Start batch - set of remote calls in one AJAX request
	 *
	 * @return if batch initialization has succeeded
	 */
	public boolean startBatch() {
		long threadId = Thread.currentThread().getId();
		if (batchCalls.containsKey(threadId))
			return false;
		batchCalls.put(threadId, new ArrayList<BatchCall>());
		return true;
	}

	/**
	 * Finish batch - perform all delayed calls in one AJAX request
	 *
	 * @param callback function to call with result of AJAX call: callback(code, combinedCode). code is zero if no errors. combinedCode is zero if all combined requests where successfully.
	 * @return if batch send to the server
	 */
	public boolean finishBatch(ResponseHandler callback) {
		long threadId = Thread.currentThread().getId();
		if (!batchCalls.containsKey(threadId)) {
			callback.onFailure(2, null);
			return false;
		}
		List<BatchCall> curCalls=batchCalls.get(threadId);
		if (curCalls.size() == 0) {
			// nothing to call
			batchCalls.remove(threadId);
			callback.onFailure(0, null);
			return false;
		}
		// construct batch call json and select max timeout
		String params = "[";
		final List<ResponseHandler> callbacks = new ArrayList<ResponseHandler>();
		for (int i = 0; i < curCalls.size(); i++) {
			BatchCall call = curCalls.get(i);
			params += "{\"svc\":\"" + call.svc + "\",\"params\":" + call.params + "}";
			if (i + 1 < curCalls.size())
				params += ",";
			callbacks.add(call.callback);
		}
		params += "]";
		// reset batch call status
		batchCalls.remove(threadId);
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
	 * @param callback       callback to call with result code
	 * @param batchCallbacks collection of callbacks for each batched call
	 * @param results        resulting data
	 */
	private void onBatchCallCompleted(ResponseHandler callback, List<ResponseHandler> batchCallbacks, String results) {
		JsonArray resultsArray = Session.getInstance().getJsonParser().parse(results).getAsJsonArray();
		if (results == null || batchCallbacks == null || resultsArray == null || batchCallbacks.size() != resultsArray.size()) {
			int errorCode = 3;
			// error doing request
			// pass result for all callbacks
			if (batchCallbacks != null)
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

	private String getServicesUrl(String svc) {
		return Session.getInstance().getBaseUrl() + "/wialon/ajax.html?svc=" + svc + "&sid=" + Session.getInstance().getId();
	}

	/**
	 * Perform remote service AJAX request
	 * @param svc service name
	 * @param params request properties
	 * @param callback function to call with result of AJAX call
	 */
	public void remoteCall(String svc, String params, ResponseHandler callback) {
		long threadId = Thread.currentThread().getId();
		if (batchCalls.containsKey(threadId)) {
			batchCalls.get(threadId).add(new BatchCall(svc, params, callback));
		} else {
			Map<String, String> nameValuePairs = new HashMap<String, String>();
			if (params != null)
				nameValuePairs.put("params", params);
			post(getServicesUrl(svc), nameValuePairs, callback);
		}
	}

	/**
	 * Upload file method
	 * @param file     File object to send
	 * @param svc      service name
	 * @param params   optional parameters
	 * @param callback callback that will receive file upload result
	 * @param timeout  int - sec
	 */
	public void uploadFile(File file, String svc, String params, ResponseHandler callback, int timeout) {
		lazyInitDefaultHttpClient();
		Map<String, String> nameValuePair = new HashMap<String, String>();
		nameValuePair.put("params", params);
		httpClient.postFile(getServicesUrl(svc), nameValuePair, new RemoteCallback(callback), timeout, file);
	}

	public void post(String url, Map<String, String> params, ResponseHandler callback, int timeoutMs) {
		lazyInitDefaultHttpClient();
		httpClient.post(url, params, new RemoteCallback(callback), timeoutMs);
	}

	public void post(String url, Map<String, String> params, ResponseHandler callback) {
		post(url, params, callback, 0);
	}

	public void get(String url, Map<String, String> params, ResponseHandler callback) {
		lazyInitDefaultHttpClient();
		httpClient.get(url, params, new RemoteCallback(callback), 0);
	}

	private static class RemoteCallback extends BaseSdkHttpClient.Callback {
		private ResponseHandler handler;

		RemoteCallback(ResponseHandler handler) {
			this.handler = handler;
		}

		@Override
		public void done(byte[] data) {
			if (data != null) {
				if (handler instanceof BinaryResponseHandler) {
					((BinaryResponseHandler) handler).onSuccessBinary(data);
				} else {
					processStringAnswer(data, handler);
				}
			} else
				handler.onFailure(6, error);
		}

		private static void processStringAnswer(byte data[], ResponseHandler callback) {
			String response = new String(data, Charset.forName("UTF-8"));
			if (response.contains("error") &&
					Session.getInstance().getJsonParser().parse(response).isJsonObject() &&
					Session.getInstance().getJsonParser().parse(response).getAsJsonObject().has("error")) {
				int error = new JsonParser().parse(response).getAsJsonObject().get("error").getAsInt();
				if (error != 0)
					callback.onFailure(error, null);
				else
					callback.onSuccess(response);
			} else
				callback.onSuccess(response);
		}
	}

	private static class BatchCall {
		String svc;
		String params;
		ResponseHandler callback;

		public BatchCall(String svc, String params, ResponseHandler callback) {
			this.svc = svc;
			this.params = params;
			this.callback = callback;
		}
	}
}
