package com.wialon.remote;

import java.io.File;
import java.util.Map;

public interface BaseSdkHttpClient {
	public static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;
	public abstract void post(String url, Map<String, String> params, Callback callback, int timeout);
	public abstract void get(String url,  Map<String, String> params, Callback callback, int timeout);
	public abstract void postFile(String url,  Map<String, String> params, Callback callback, int timeout, File file);

	public static abstract class Callback{
		public Throwable error;
		public abstract void done(byte[] data);
	}
}
