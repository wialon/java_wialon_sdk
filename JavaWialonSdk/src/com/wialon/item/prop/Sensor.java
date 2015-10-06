package com.wialon.item.prop;

import com.google.gson.JsonObject;
import com.wialon.core.Session;
import com.wialon.item.Item;
import com.wialon.remote.RemoteHttpClient;
import com.wialon.remote.handlers.ResponseHandler;

import java.util.Map;

public class Sensor extends ItemProperties {
	public Sensor(Map<String, String> data, String propName, Item item, Enum event, String ajaxPath) {
		super(data, propName, item, event, ajaxPath);
	}

	/**
	 * Perform remote sensors calculation by the latest unit message
	 * @param sensors optional sensor indexes, pass empty array to obtain all sensors
	 * @param callback callback, that get result likes {sens1Id: val, sens2Id: val, ...}
	 */
	public void remoteCalculateLastMessage(int[] sensors, ResponseHandler callback) {
		if (sensors==null)
			sensors=new int[0];
		RemoteHttpClient.getInstance().remoteCall(
				"unit/calc_last_message",
				"{\"sensors\":"+ Session.getInstance().getGson().toJson(sensors)+",\"unitId\":"+item.getId()+"}",
				callback
		);
	}
	/**
	 * Calculate sensor value
	 * @param source empty string for the messages loader, otherwise <layerName> for renderer
	 * @param indexFrom starting index (inclusive)
	 * @param indexTo  ending index (inclusive)
	 * @param sensorId pass 0 to obtain all sensors
	 * @param callback callback, that get result in form is an array of requested sensor(s) values
	 */
	public void remoteCalculateMsgs(String source, int indexFrom, int indexTo, int sensorId, ResponseHandler callback) {
		JsonObject params=new JsonObject();
		params.addProperty("source", source);
		params.addProperty("unitId", item.getId());
		params.addProperty("indexFrom", indexFrom);
		params.addProperty("indexTo", indexTo);
		params.addProperty("sensorId", sensorId);
		RemoteHttpClient.getInstance().remoteCall(
				"unit/calc_sensors",
				params,
				callback
		);
	}

	/** Invalid sensor value constant */
	public static final double invalidValue=-348201.3876;
}
