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

package com.wialon.render;

import com.wialon.core.EventProvider;
import com.wialon.core.Session;
import com.wialon.extra.LayerSpec;
import com.wialon.extra.POISpec;
import com.wialon.extra.ZonesSpec;
import com.wialon.remote.RemoteHttpClient;
import com.wialon.remote.handlers.LayerResponseHandler;
import com.wialon.remote.handlers.ResponseHandler;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.*;

/**
 * Wialon renderer object.
 * Manages various server-based layers rendering.
 */
public class Renderer extends EventProvider {
	private List<Layer> layers;
	private int version;

	public Renderer(){
		this.layers=new ArrayList<Layer>();
	}

	/** Active, incremental version of renderer, version property is incremented when picture is changed and redraw required */
	public int getVersion() {
		return version;
	}

	/**
	 * Get layers
	 * @return layers collection
	 */
	public List<Layer> getLayers() {
		return this.layers;
	}

	/**
	 * Get report layer
	 * @return may return null
	 */
	public Layer getReportLayer() {
		for (Layer layer : this.layers)
			if (layer.getName().substring(0, 6).equals("report"))
				return layer;
		return null;
	}

	/**
	 * Get URL for 256x256 GMaps style tile
	 * @param x  X coordinate (lon?)
	 * @param y  Y coordinate (lat?)
	 * @param z  Z coordinate (zoom)
	 * @return  tile URL
	 */
	public String getTileUrl(int x, int y, int z) {
		return Session.getInstance().getBaseUrl() + "/adfurl" + this.getVersion() + "/avl_render/" + x + "_" + y + "_" + (17 - z) + "/" + Session.getInstance().getId() + ".png";
	}
	/**
	 * Initialize/update localization settings,
	 * @param tzOffset timezone offset, see wialon.util.DateTime
	 * @param language  2byte language code, e.g.: ru, en, de, ...
	 * @param callback  function that is called after renderer initialization
	 */
	public void setLocale(int tzOffset, String language, ResponseHandler callback) {
		// perform remote call for initialization
		RemoteHttpClient.getInstance().remoteCall("render/set_locale",
				"{\"tzOffset\":"+tzOffset+",\"language\":\""+language+"\"}",
				callback);
	}
	/**
	 * Create new messages layer into renderer, require ACL Item.accessFlag.execReports
	 * @param params layer parameters, format: {layerName: text, itemId: long, timeFrom: uint, timeTo: uint, tripDetector: bool, trackColor: text, trackWidth: int, arrows: bool, points: bool, pointColor: text, annotations: bool}
	 * @param callback callback that will receive information about new layer addition
	 */
	public void createMessagesLayer(LayerSpec params, LayerResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"render/create_messages_layer",
				Session.getInstance().getGson().toJson(params),
				new ResponseHandler(callback) {
					@Override
					public void onSuccess(String response) {
						onCreatedMessagesLayer(response, getCallback());
					}
				}
		);
	}
	/**
	 * Create POI layer into renderer, require ACL Resource.accessFlag.viewPoi to resource items
	 * @param layerName layer name
	 * @param pois POI to render specification in form: [{resourceId: ID, poiId: [id1, id2, id3]}]
	 * @param flags layer rendering flags, see wialon.render.Renderer.PoiFlag for more details
	 * @param callback callback that will receive information about new layer addition
	 */
	public void createPoiLayer(String layerName, POISpec[] pois, int flags, LayerResponseHandler callback) {
		for (Layer layer : this.layers)
			if (layer.getName().equals(layerName))
				this.layers.remove(layer);
		String poisArray=Session.getInstance().getGson().toJson(pois);
		RemoteHttpClient.getInstance().remoteCall(
				"render/create_poi_layer",
				"{\"layerName\":\""+layerName+"\",\"pois\":"+poisArray+",\"flags\":"+flags+"}",
		new ResponseHandler(callback) {
			@Override
			public void onSuccess(String response) {
				onCreatedSimpleLayer(response, getCallback());
			}
		}
		);
	}
	/**
	 * Create Geofence layer into renderer, require ACL Resource.accessFlag.viewZone to resource items
	 * @param layerName layer name
	 * @param zones Zones to render specification in form: [{resourceId: ID, zoneId: [id1, id2, id3]}]
	 * @param flags layer rendering flags, see wialon.render.Renderer.ZonesFlag for more details
	 * @param callback callback that will receive information about new layer addition
	 */
	public void createZonesLayer(String layerName, ZonesSpec[] zones, int flags, LayerResponseHandler callback) {
		for (Layer layer : this.layers)
			if (layer.getName().equals(layerName))
				this.layers.remove(layer);
		String zonesArray=Session.getInstance().getGson().toJson(zones);
		RemoteHttpClient.getInstance().remoteCall(
				"render/create_zones_layer",
				"{\"layerName\":\""+layerName+"\",\"zones\":"+zonesArray+",\"flags\":"+flags+"}",
				new ResponseHandler(callback) {
					@Override
					public void onSuccess(String response) {
						onCreatedSimpleLayer(response,getCallback());
					}
				}
		);
	}
	/**
	 * Remove layer from renderer
	 * @param layer  layer which to remove
	 * @param callback callback that will receive information about new layer removal
	 */
	public void removeLayer(final Layer layer, ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"render/remove_layer",
				"{\"layerName\":\""+layer.getName()+"\"}",
				new ResponseHandler(callback) {
					@Override
					public void onSuccess(String response) {
						onRemovedLayer(response, getCallback(), layer);
					}
				}
		);
	}
	/**
	 * Enable or disable layer.
	 * @param layer layer to enable or disable
	 * @param enable  true to enable layer, false - to disable
	 * @param callback  callback that will receive information about layer update
	 */
	public void enableLayer(final Layer layer, boolean enable, ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"render/enable_layer",
				"{\"layerName\":\""+layer.getName()+"\",\"enable\":"+(enable ? 1 : 0)+"}",
				new ResponseHandler(callback) {
					@Override
					public void onSuccess(String response) {
						onLayerEnabled(response, getCallback(), layer);
					}
				}
		);
	}
	/**
	 * Remove all layers from renderer
	 * @param callback  callback that will be called after all layers are removed
	 */
	public void removeAllLayers(ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"render/remove_all_layers",
				"{}",
				new ResponseHandler(callback) {
					@Override
					public void onSuccess(String response) {
						onAllLayersRemoved(response, getCallback());
					}
				}
		);
	}
	/**
	 * Perform hit test over coordinates over all layers in renderer
	 * @param lat  point latitude
	 * @param lon  point longtitude
	 * @param scale scale in some adsa-known metrics
	 * @param radius maximum possible range till object, probably in degrees
	 * @param layerName maybe zero string (value: "")
	 * @param callback callback, that get result in form callback(code, result), where zero code mean success, result is complex object, which fields depending on type of hitted layer
	 */
	public void hitTest(double lat, double lon, int scale, double radius, String layerName, ResponseHandler callback) {
		Map<String, String> nameValuePairs = new HashMap<String, String>();
		nameValuePairs.put("sid", Session.getInstance().getId());
		nameValuePairs.put("lat", String.valueOf(lat));
		nameValuePairs.put("lon", String.valueOf(lon));
		nameValuePairs.put("scale", String.valueOf(scale));
		nameValuePairs.put("radius", String.valueOf(radius));
		nameValuePairs.put("layerName", layerName);
		RemoteHttpClient.getInstance().post(
				Session.getInstance().getBaseUrl() + "/avl_hittest_pos",
				nameValuePairs,
				callback);
	}
	private void upVersion(){
		fireEvent(events.changeVersion, this, this.version, ++this.version);
	}
	/**
	 * Handle result of new messages layer creation
	 * @param result new layer information
	 * @param callback user-defined callback
	 */
	private void onCreatedMessagesLayer(String result, ResponseHandler callback){
		if (result!=null) {
			// success
			Layer layer = Session.getInstance().getGson().fromJson(result, MessagesLayer.class);
			this.layers.add(layer);
			// update version
			this.upVersion();
			// pass to callback if available
			callback.onSuccess(result);
			if (callback instanceof LayerResponseHandler)
				((LayerResponseHandler)callback).onSuccessLayer(layer);
			return;
		}
		callback.onFailure(6, null);
	}
	/**
	 * Handle result of new simple layer creation
	 * @param result layer information
	 * @param callback user-defined callback
	 */
	private void onCreatedSimpleLayer(String result, ResponseHandler callback){
		if (result!=null) {
			// success
			Layer layer=Session.getInstance().getGson().fromJson(result, Layer.class);
			this.layers.add(layer);
			// update version
			this.upVersion();
			// pass to callback if available
			callback.onSuccess(result);
			if (callback instanceof LayerResponseHandler)
				((LayerResponseHandler)callback).onSuccessLayer(layer);
			return;
		}
		callback.onFailure(6, null);
	}
	/**
	 * Handle result of layer removal
	 * @param result nothing now
	 * @param callback  user-defined callback
	 * @param layer layer that was removed
	 */
	private void onRemovedLayer(String result, ResponseHandler callback, Layer layer){
		if (result!=null) {
			this.layers.remove(layer);
			this.upVersion();
			callback.onSuccess(result);
			return;
		}
		callback.onFailure(6, null);
	}
	/**
	 * Handle result of layer enabled state updated
	 * @param result current layer state: {result.enabled}
	 * @param callback user-defined callback
	 * @param layer layer that was updated
	 */
	private void onLayerEnabled(String result, ResponseHandler callback, Layer layer){
		if (result!=null) {
			boolean enabled=!Session.getInstance().getJsonParser().parse(result).getAsJsonObject().get("enabled").getAsNumber().equals(0);
			if (enabled!=layer.isEnabled()) {
				layer.setEnabled(enabled);
				this.upVersion();
			}
			callback.onSuccess(result);
			return;
		}
		callback.onFailure(6, null);
	}
	/**
	 * Handle result of all layers removal
	 * @param result nothing now
	 * @param callback user-defined callback
	 */
	private void onAllLayersRemoved(String result, ResponseHandler callback){
		if (result!=null) {
			if (!this.layers.isEmpty()){
				this.layers.clear();
				this.upVersion();
			}
			callback.onSuccess(result);
			return;
		}
		callback.onFailure(6, null);
	}

	/** POI layers creation flags, used upon layer creation*/
	public static enum poiFlag{
		/** render POI labels */
		renderLabels(0x01),
		/** allow POI grouping */
		enableGroups(0x02);
		/** Flag value */
		private int value;

		private poiFlag (int value) {
			this.value=value;
		}

		public int getValue() {
			return value;
		}
	}
	/** Geofence layers creation flags, used upon layer creation*/
	public static enum zonesFlag {
		/** render Geofence labels */
		renderLabels(0x01);
		/** Flag value */
		private int value;

		private zonesFlag (int value) {
			this.value=value;
		}
		public int getValue() {
			return value;
		}
	}

	public static enum events{
		/** version property has changed, e.g. layer has been added, removed or its visibility was switched */
		changeVersion
	}
}
