package com.wialon.remote.handlers;

/**
 * Callback from server request
 */
public abstract class ResponseHandler {
	protected ResponseHandler callback;

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
