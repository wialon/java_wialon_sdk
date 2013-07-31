package com.wialon.remote;

import com.wialon.item.Item;

/**
 * Callback from server request
 */
public abstract class ResponseHandler {
	private ResponseHandler callback;

	public ResponseHandler(){}

	public ResponseHandler(ResponseHandler callback) {
		this.callback=callback;
	}

	/**
	 * This method calls on any success server request
	 * @param response answer from server
	 */
	public void onSuccess (String response){
		if (callback!=null)
			callback.onSuccess(response);
	}

	/**
	 * This method calls on success Session.searchItems(), Session.searchItem() and Session.create(Unit|User|UnitGroup|Resource)
	 * @param items found items array, maybe empty
	 */
	public void onSuccessSearch (Item... items) {
		if (callback!=null)
			callback.onSuccessSearch(items);
	}

	/**
	 * This method calls if server request is unsuccessful or any other error
	 * @param errorCode code com which can be obtained error text at Errors.getErrorText(int errorCode)
	 * @param throwableError if has Exception on error, else null
	 */
	public void onFailure (int errorCode, Throwable throwableError){
		if (callback!=null)
			callback.onFailure(errorCode, throwableError);
	}

	public final ResponseHandler getCallback() {
		return callback;
	}
}
