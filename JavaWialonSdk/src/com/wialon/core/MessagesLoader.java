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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.wialon.messages.Message;
import com.wialon.remote.RemoteHttpClient;
import com.wialon.remote.handlers.MessagesResponseHandler;
import com.wialon.remote.handlers.ResponseHandler;

/**
 * Wialon messages loader object.
 * Allow to load messages from units, users, resources into session, retrieve and delete them.
 */
public class MessagesLoader {
	/**
	 * Load messages for given time interval, require ACL wialon.core.Item.accessFlag.execReports
	 *
	 * @param itemId    item Id for which to load messages
	 * @param timeFrom  interval beginning
	 * @param timeTo    interval ending
	 * @param flags     messages flags, see wialon.messages.Message.messageFlag for possible values
	 * @param flagsMask messages flags mask, see wialon.messages.Message.messageFlag for possible values
	 * @param loadCount how many message to pass initially in callback
	 * @param callback  callback function that is called after messages are loaded, result: {count: N, messages: [...]}
	 */
	public void loadInterval(long itemId, long timeFrom, long timeTo, long flags, long flagsMask, int loadCount, MessagesResponseHandler callback) {
		// perform remote call for initialization
		RemoteHttpClient.getInstance().remoteCall("messages/load_interval",
				"{\"itemId\":" + itemId + "," +
						"\"timeFrom\":" + timeFrom + "," +
						"\"timeTo\":" + timeTo + "," +
						"\"flags\":" + flags + "," +
						"\"flagsMask\":" + flagsMask + "," +
						"\"loadCount\":" + loadCount + "}",
				new ResponseHandler(callback){
					@Override
					public void onSuccess(String response) {
						onMessagesReceived(response, getCallback());
					}
				});
	}

	/**
	 * Load last N messages, require ACL wialon.core.Item.accessFlag.execReports
	 *
	 * @param itemId    item Id for which to load messages
	 * @param lastTime  time of end of interval, to get last count messages paass zero
	 * @param lastCount how many last messages will be loaded, value should be between 1 and 10000
	 * @param flags     messages flags, see wialon.item.Item.messageFlag for possible values
	 * @param flagsMask messages flags mask, see wialon.item.Item.messageFlag for possible values
	 * @param loadCount how many message to pass initially in callback
	 * @param callback  callback function that is called after messages are loaded, result: {count: N, messages: [...]}
	 */
	public void loadLast(long itemId, long lastTime, int lastCount, long flags, long flagsMask, int loadCount, MessagesResponseHandler callback) {
		// perform remote call for initialization
		RemoteHttpClient.getInstance().remoteCall("messages/load_last",
				"{\"itemId\":" + itemId + "," +
						"\"lastTime\":" + lastTime + "," +
						"\"lastCount\":" + lastCount + "," +
						"\"flags\":" + flags + "," +
						"\"flagsMask\":" + flagsMask + "," +
						"\"loadCount\":" + loadCount + "}",
				new ResponseHandler(callback){
					@Override
					public void onSuccess(String response) {
						onMessagesReceived(response, getCallback());
					}
				});
	}

	/**
	 * Unload loaded messages
	 *
	 * @param callback callback function that is called after messages unloaded
	 */
	public void unload(ResponseHandler callback) {
		// perform remote call for initialization
		RemoteHttpClient.getInstance().remoteCall("messages/unload", "{}", callback);
	}

	/**
	 * Get messages data for given indices
	 *
	 * @param indexFrom {Integer} starting index (inclusive)
	 * @param indexTo   {Integer} ending index (inclusive)
	 * @param callback  {Function?null} callback, that get result in form callback(code, col), where zero code mean success, and col is a collection of requested messages
	 */
	public void getMessages(int indexFrom, int indexTo, MessagesResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall("messages/get_messages",
				"{\"indexFrom\":" + indexFrom + ",\"indexTo\":" + indexTo + "}",
				new ResponseHandler(callback){
					@Override
					public void onSuccess(String response) {
						onMessagesReceived(response, getCallback());
					}
				});
	}

	/**
	 * Delete message from database, it is not possible to delete unit last message or latest message with position information.
	 * Require ACL wialon.core.Unit.accessFlag.deleteMessages over item.
	 * Only messages from units can be deleted.
	 *
	 * @param msgIndex message indexes from loaded into layer for deletion
	 * @param callback {Function?null} callback, that get result in form callback(code), where zero code mean successful message deletion
	 */
	public void deleteMessage(int msgIndex, ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"messages/delete_message",
				"{\"msgIndex\":" + msgIndex + "}",
				callback
		);
	}

	private void onMessagesReceived(String response, ResponseHandler callback){
		if (response!=null) {
			callback.onSuccess(response);
			JsonElement jsonElement=Session.getInstance().getJsonParser().parse(response);
			JsonArray messagesArray;
			if (jsonElement.isJsonObject())
				messagesArray=jsonElement.getAsJsonObject().get("messages").getAsJsonArray();
			else if (jsonElement.isJsonArray())
				messagesArray=jsonElement.getAsJsonArray();
			else
				return;
			Message[] messages=new Message[messagesArray.size()];
			for (int i=0; i<messagesArray.size(); i++){
				JsonElement messageElement=messagesArray.get(i);
				if (messageElement.isJsonObject() && messageElement.getAsJsonObject().has("f") && messageElement.getAsJsonObject().has("f")) {
					String tp=messageElement.getAsJsonObject().get("tp").getAsString();
					long f=messageElement.getAsJsonObject().get("f").getAsLong();
					Message.messageFlag flag=Message.messageFlag.getMessageFlag(f);
					if (flag==null)
						continue;
					Class clazz=Message.MessageType.getMessageClass(flag, tp);
					if (clazz!=null)
						messages[i]=(Message)Session.getInstance().getGson().fromJson(messageElement, clazz);
				}
			}
			if (callback instanceof MessagesResponseHandler)
				((MessagesResponseHandler) callback).onSuccessMessages(messages);
			return;
		}
		callback.onFailure(6, null);
	}
}
