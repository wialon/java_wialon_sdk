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
