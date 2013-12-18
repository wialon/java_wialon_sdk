package com.wialon.item.prop;

import com.wialon.core.Session;
import com.wialon.item.Item;
import com.wialon.remote.RemoteHttpClient;
import com.wialon.remote.handlers.ResponseHandler;

import java.util.Map;

public abstract class ItemPropertiesData extends ItemProperties {
	private String extAjaxPath;

	protected ItemPropertiesData(Map<String, String> data, String propName, Item item, Enum event, String ajaxPath, String extAjaxPath) {
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
