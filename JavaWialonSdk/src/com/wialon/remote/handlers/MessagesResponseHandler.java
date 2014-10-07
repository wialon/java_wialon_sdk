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
