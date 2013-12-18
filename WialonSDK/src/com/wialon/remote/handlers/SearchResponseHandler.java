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
