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

package com.wialon.item.prop;

import com.wialon.core.Session;
import com.wialon.item.Item;
import com.wialon.remote.RemoteHttpClient;
import com.wialon.remote.handlers.ResponseHandler;

import java.util.Map;

public class ItemPropertiesData extends ItemProperties {
	private String extAjaxPath;

	public ItemPropertiesData(Map<String, String> data, String propName, Item item, Enum event, String ajaxPath, String extAjaxPath) {
		super(data, propName, item, event, ajaxPath);
		this.extAjaxPath=extAjaxPath;
	}
	/**
	 * Get property full data
	 * @param col array of id(s)
	 * @param callback callback handler
	 */
	public void getPropertyData(long[] col, ResponseHandler callback){
		RemoteHttpClient.getInstance().remoteCall(
				extAjaxPath,
				"{\"itemId\":"+item.getId()+",\"col\":"+ Session.getInstance().getGson().toJson(col)+"}",
				callback
		);
	}
}
