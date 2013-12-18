package com.wialon.remote;

import com.google.gson.JsonParser;
import com.wialon.core.Session;
import com.wialon.remote.handlers.BinaryResponseHandler;
import com.wialon.remote.handlers.ResponseHandler;
import com.wialon.util.Debug;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpRequest implements Runnable {
	private final AbstractHttpClient client;
	private final HttpUriRequest request;
	private final ResponseHandler callback;

	public HttpRequest(AbstractHttpClient client, HttpUriRequest request, ResponseHandler callback) {
		this.client=client;
		this.request=request;
		this.callback=callback;
	}
	@Override
	public void run() {
		try {
			Debug.log.info("Start sending request");
			HttpResponse httpResponse=client.execute(request);
			StatusLine status = httpResponse.getStatusLine();
			HttpEntity temp = httpResponse.getEntity();
			if (callback instanceof BinaryResponseHandler) {
				processBinaryAnswer(temp, status);
			} else {
				processStringAnswer(temp, status);
			}
		} catch (Exception e) {
			e.printStackTrace();
			callback.onFailure(6, e);
		}
	}

	private void processBinaryAnswer(HttpEntity temp, StatusLine status) throws IOException{
		HttpEntity entity = null;
		byte[] responseBody;
		if(temp != null) {
			entity = new BufferedHttpEntity(temp);
			}
			responseBody = EntityUtils.toByteArray(entity);
		if(status.getStatusCode() != HttpStatus.SC_OK) {
			callback.onFailure(6, new HttpResponseException(status.getStatusCode(), status.getReasonPhrase()));
		} else {
			((BinaryResponseHandler)callback).onSuccessBinary(responseBody);
		}
	}

	private void processStringAnswer(HttpEntity temp, StatusLine status) throws IOException{
		HttpEntity entity;
		String responseBody=null;
		if(temp != null) {
			entity = new BufferedHttpEntity(temp);
			responseBody = EntityUtils.toString(entity, "UTF-8");
		}
		if(status.getStatusCode() != HttpStatus.SC_OK)
			callback.onFailure(6, new HttpResponseException(status.getStatusCode(), status.getReasonPhrase() + "|" + responseBody));
		else {
			if (responseBody!=null) {
				if (responseBody.contains("error") &&
						Session.getInstance().getJsonParser().parse(responseBody).isJsonObject() &&
						Session.getInstance().getJsonParser().parse(responseBody).getAsJsonObject().has("error")) {
					int error=new JsonParser().parse(responseBody).getAsJsonObject().get("error").getAsInt();
					if (error!=0)
						callback.onFailure(error, null);
					else
						callback.onSuccess(responseBody);
				} else
					callback.onSuccess(responseBody);
			}
		}
	}
}
