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

package com.wialon.remote.handlers;

import com.wialon.item.Item;

public class SearchResponseHandler extends ResponseHandler {
	/**
	 * This method calls on success Session.searchItems(), Session.searchItem() and Session.create(Unit|User|UnitGroup|Resource)
	 * @param items found items array, maybe empty
	 */
	public void onSuccessSearch (Item... items) {
		if (callback!=null && callback instanceof SearchResponseHandler)
			((SearchResponseHandler)callback).onSuccessSearch(items);
	}
}
