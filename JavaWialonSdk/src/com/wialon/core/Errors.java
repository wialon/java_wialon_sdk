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

package com.wialon.core;

import java.util.HashMap;
import java.util.Map;

public class Errors {
	private final static Map<Integer, String> errors;

	static {
		errors = new HashMap<Integer, String>();
		errors.put(0, "");
		errors.put(1, "Invalid session");
		errors.put(2, "Invalid service");
		errors.put(3, "Invalid result");
		errors.put(4, "Invalid input");
		errors.put(5, "Error performing request");
		errors.put(7, "Access denied");
		errors.put(8, "Invalid user name or password");
		errors.put(9, "Authorization server is unavailable, please try again later");
		errors.put(1001, "No message for selected interval");
		errors.put(1002, "Item with such unique property already exists");
		errors.put(1003, "Only one request of given time is allowed at the moment");
	}

	/**
	 * Get text for given error code
	 *
	 * @param code {int} error code
	 * @return {String} Translated text for given error code
	 */
	public static String getErrorText(int code) {
		return errors.get(code) == null ? "Unknown error" : errors.get(code);
	}
}
