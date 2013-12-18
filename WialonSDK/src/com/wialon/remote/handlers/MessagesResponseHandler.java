package com.wialon.remote.handlers;

import com.wialon.messages.Message;

public class MessagesResponseHandler extends ResponseHandler {
	/**
	 * This method calls on success receive messages
	 * @param messages found messages array, maybe empty
	 */
	public void onSuccessMessages (Message... messages) {
		if (callback!=null && callback instanceof MessagesResponseHandler)
			((MessagesResponseHandler)callback).onSuccessMessages(messages);
	}
}
