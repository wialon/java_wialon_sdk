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

package com.wialon.item;

import com.wialon.core.EventProvider;
import com.wialon.core.Session;
import com.wialon.item.prop.ItemProperties;
import com.wialon.item.prop.ItemPropertiesData;
import com.wialon.remote.RemoteHttpClient;
import com.wialon.remote.handlers.ResponseHandler;
import com.wialon.messages.Message;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public abstract class Item extends EventProvider {
	//Base
	private String nm;
	private Long id;
	private Long uacl;
	private Long dataFlags;
	//Billing
	private Long crt;
	private Long bact;

	private Integer mu;
	//Custom properties
	private Map<String, String> prp;
	private Map<String, UpdateItemProperty> updateItemPropertyFunctions;

	protected ItemType itemType;

	private ItemProperties customFieldsPlugin;
	private Map<String, String> flds;
	private ItemProperties adminFieldsPlugin;
	private Map<String, String> aflds;


	public ItemProperties getCustomFieldsPlugin() {
		return customFieldsPlugin==null ? customFieldsPlugin=new ItemProperties(flds, "flds", this, events.updateCustomField, "item/update_custom_field") : customFieldsPlugin;
	}

	public ItemProperties getAdminFieldsPlugin() {
		return adminFieldsPlugin==null ? adminFieldsPlugin=new ItemProperties(aflds, "aflds", this, events.updateAdminField, "item/update_admin_field") : adminFieldsPlugin;
	}

	public interface UpdateItemProperty {
		public void updateItemProperty(JsonElement data);
	}

	/**
	 * Register item property handler of given name
	 * @param propName Property name
	 * @param function function
	 */
	public void registerItemPropertyHandler(String propName, UpdateItemProperty function){
		if (updateItemPropertyFunctions==null)
			updateItemPropertyFunctions=new HashMap<String, UpdateItemProperty>();
		if (!updateItemPropertyFunctions.containsKey(propName))
			updateItemPropertyFunctions.put(propName, function);
	}
	/**
	 * Get item type
	 * @return Item type
	 */
	public ItemType getItemType() {
		return itemType;
	}

	/**
	 * Get item name
	 * @return Item name
	 */
	public String getName() {
		return nm;
	}
	/**
	 * Get item measure units
	 * @return Item name
	 */
	public Integer getMeasureUnits() {
		return mu;
	}
	/**
	 * Get item id
	 * @return Item id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * This item billing account ID
	 * @return Item account id
	 */
	public Long getAccountId() {
		return bact;
	}

	/**
	 * This item user-creator ID
	 * @return Item creator id
	 */
	public Long getCreatorId() {
		return crt;
	}

	/**
	 * Current item data flags, e.g. which properties are actual
	 * @return Item data flags
	 */
	public Long getDataFlags() {
		return dataFlags;
	}

	/**
	 * Current user access level to this item
	 * @return User access level
	 */
	public Long getUserAccess() {
		return uacl;
	}

	/**
	 * Get custom property
	 * @param propName property name
	 * @param defaultVal default value if property not defined
	 * @return public property value
	 */
	public String getCustomProperty(String propName, String defaultVal) {
		String propertyValue=defaultVal;
		if (prp!=null && prp.get(propName)!=null)
			propertyValue=prp.get(propName);
		return propertyValue;
	}

	/**
	 * Set custom property
	 * @param propName property name
	 * @param propValue property value
	 */
	private boolean setCustomProperty(String propName, String propValue) {
		if (prp!=null) {
			String oldValue =prp.get(propName);
			if (oldValue==null)
				oldValue="";
			if (!propValue.equals(""))
				prp.put(propName, propValue);
			else if (!oldValue.equals(""))
				prp.remove(propName);
			if (!propValue.equals(oldValue))
				fireEvent(events.changeCustomProperty, propName, oldValue, propValue);
			return true;
		} else
			return false;
	}

	private void setName(String name) {
		if (this.nm==null || !this.nm.equals(name)){
			String oldName=this.nm==null ? null : new String(nm);
			nm = name;
			fireEvent(events.changeName, this, oldName, name);
		}
	}

	private void setMeasureUnits(Integer measureUnits) {
		if (this.mu==null || !this.mu.equals(measureUnits)){
			Integer oldMeasureUnits=this.mu==null ? null : new Integer(measureUnits);
			mu = measureUnits;
			fireEvent(events.changeMeasureUnits, this, oldMeasureUnits, measureUnits);
		}
	}

	private void setUserAccess(Long userAccessLevel) {
		if (this.uacl==null || !this.uacl.equals(userAccessLevel)) {
			Long oldAcl=this.uacl==null ? null : new Long(uacl);
			this.uacl = userAccessLevel;
			fireEvent(events.changeUserAccess, this, oldAcl, userAccessLevel);
		}
	}

	private void setCreatorId(Long creatorId) {
		this.crt = creatorId;
	}

	private void setAccountId(Long accountId) {
		this.bact = accountId;
	}

	private void setCustomProperties(Map<String, String> properties) {
		this.prp = properties;
	}

	public void setDataFlags(Long dataFlags) {
		if (this.dataFlags==null || !this.dataFlags.equals(dataFlags)) {
			Long oldFlags=this.dataFlags==null ? null : new Long(this.dataFlags);
			this.dataFlags = dataFlags;
			fireEvent(events.changeDataFlags, this, oldFlags, dataFlags);
		}
	}

	/**
	 * Handle new message from server for this item.
	 * @param message Message data
	 */
	public void handleMessage(Message message) {
		fireEvent(events.messageRegistered, this, null, message);
	}

	/**
	 * Update custom property, require ACL bit Item.accessFlag.editOther over item
	 * @param propName property name
	 * @param propValue value property
	 * @param callback callback that will receive information about property update
	 */
	public void updateCustomProperty(String propName, String propValue, ResponseHandler callback) {
		JsonObject params=new JsonObject();
		params.addProperty("itemId", getId());
		params.addProperty("name", propName);
		params.addProperty("value", propValue);
			RemoteHttpClient.getInstance().remoteCall(
				"item/update_custom_property",
					params,
				new ResponseHandler(callback) {
					@Override
					public void onSuccess(String response) {
						onUpdateCustomProperty(response, getCallback());
					}
				});
	}
	/**
	 * Update measure units used in item
	 * @param type {Number} units of measure type, see wialon.item.measureUnitsType for possible types
	 * @param flags {Number} set or convert all item settings, see wialon.item.measureUnitsFlag for possible flags
	 * @param callback {Function?null} callback that will receive information about convertation: callback(code), zero code is success
	 */
	public void updateMeasureUnits(int type, int flags, ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"item/update_measure_units",
				"{\"itemId\":"+getId()+",\"type\":"+type+",\"flags\":"+flags+"}",
				getOnUpdatePropertiesCallback(callback));
	}
	/**
	 * Update item name, require ACL bit Item.accessFlag.editName over item
	 * @param name new name value
	 * @param callback callback that will receive information about update
	 */
	public void updateName(String name, ResponseHandler callback) {
		JsonObject params=new JsonObject();
		params.addProperty("itemId", getId());
		params.addProperty("name", name);
		RemoteHttpClient.getInstance().remoteCall(
				"item/update_name",
				params,
				getOnUpdatePropertiesCallback(callback));
	}
	/**
	 * Add item log record
	 * @param action type of action (for unit described in Unit.logMessageAction)
	 * @param newValue log record action new value (can be empty)
	 * @param oldValue log record action old value (can be empty)
	 * @param callback callback that get result of server operation
	 */
	public void addLogRecord(String action, String newValue, String oldValue, ResponseHandler callback) {
		JsonObject params=new JsonObject();
		params.addProperty("itemId", getId());
		params.addProperty("action", action);
		params.addProperty("newValue", (newValue==null?"":newValue));
		params.addProperty("oldValue", (oldValue==null?"":oldValue));
		RemoteHttpClient.getInstance().remoteCall(
				"item/add_log_record",
				params,
				callback);
	}
	/**
	 * Handle result of update custom property
	 * @param result property new name/value pair in form {n: "", v: ""}
	 * @param callback user-defined callback
	 */
	private void onUpdateCustomProperty(String result, ResponseHandler callback) {
		if (result!=null) {
			JsonElement resultJson=Session.getInstance().getJsonParser().parse(result);
			if (resultJson!=null && resultJson.isJsonObject()){
				JsonObject resultObject=((JsonObject)resultJson);
				// success
				setCustomProperty(resultObject.get("n").getAsString(), resultObject.get("v").getAsString());
				// pass result to callback if available
				callback.onSuccess(result);
				return;
			}
		}
		callback.onFailure(6, null);
	}
	/**
	 * Handle result of some remote properties update method
	 * @param result properties data in form {pname1: pvalue1, pname2: pvalue2}
	 * @param callback user-defined callback
	 */
	protected void onUpdateProperties(String result, ResponseHandler callback) {
		if (result!=null) {
			Session.getInstance().updateItem(this, Session.getInstance().getJsonParser().parse(result).getAsJsonObject());
			callback.onSuccess(result);
			return;
		}
		callback.onFailure(6, null);
	}

	//Should to be public for item properties implementation with delegation
	public void fireItemPropertyEvent(Enum event, Object oldData, Object newData) {
		fireEvent(event, this, oldData, newData);
	}

	protected ResponseHandler getOnUpdatePropertiesCallback(ResponseHandler callback) {
		return new ResponseHandler(callback) {
			@Override
			public void onSuccess(String response) {
				onUpdateProperties(response, getCallback());
			}
		};
	}

	public boolean updateItemData(String key, JsonElement data){
		if (key.equals("nm") && data.getAsString()!=null) {
			setName(data.getAsString());
		} else if (key.equals("uacl") && data.getAsNumber()!=null) {
			setUserAccess(data.getAsNumber().longValue());
		} else if (key.equals("prp") && data.isJsonObject()) {
			setCustomProperties(Session.getInstance().getGson().fromJson(data, Map.class));
		} else if (key.equals("prpu") && data.isJsonObject()) {
			for (Map.Entry<String, JsonElement> entry : ((JsonObject)data).entrySet())
				setCustomProperty(entry.getKey(), entry.getValue().getAsString());
		} else if (key.equals("crt") && data.getAsNumber()!=null) {
			setCreatorId(data.getAsNumber().longValue());
		} else if (key.equals("bact") && data.getAsNumber()!=null) {
			setAccountId(data.getAsNumber().longValue());
		} else if (key.equals("mu") && data.getAsNumber()!=null){
			setMeasureUnits(data.getAsNumber().intValue());
		} else if (updateItemPropertyFunctions!=null && updateItemPropertyFunctions.containsKey(key)) {
			updateItemPropertyFunctions.get(key).updateItemProperty(data);
		} else {
			return false;
		}
		return true;
	}

	public static enum ItemType {
		/** unit */
		avl_unit(Unit.class),
		/** user */
		user (User.class),
		/** resource */
		avl_resource(Resource.class),
		/** unit_group */
		avl_unit_group (UnitGroup.class),
		/** hardware type */
		avl_hw (null),
		/** retranslator */
		avl_retranslator(null),
		/** route */
		avl_route (null);

		private Class mItemClass;

		private ItemType(Class itemClass) {
			this.mItemClass=itemClass;
		}

		public Class getItemClass() {
			return this.mItemClass;
		}

		public static ItemType getItemTypeByClass(Class itemClass){
			for (ItemType itemType : values()){
				if (itemClass.equals(itemType.getItemClass()))
					return itemType;
			}
			return null;
		}
	}

	/** Data flags constants */
	public static enum dataFlag {
		/** Base item information, should always be set */
		base(0x00000001),
		/** Item custom properties */
		customProps(0x00000002),
		/** Item billing/construction properties */
		billingProps(0x00000004),
		/** Custom fields plugin */
		customFields(0x00000008),
		/** Item image */
		image(0x00000010),
		/** Item messages */
		messages(0x00000020),
		/** Item GUID */
		guid (0x00000040),
		/** Admin fields plugin */
		adminFields(0x00000080);
		/** Flag value */
		private long value;

		private dataFlag (long value) {
			this.value=value;
		}

		public long getValue() {
			return value;
		}
	}

	/** ACL flags constants */
	public static enum accessFlag {
		/** User has at least view access to given item */
		view (0x1),
		/** User can view detailed item properties */
		viewProperties(0x2),
		/** User can update ACL settings for item or user */
		setAcl(0x4),
		/** User can delete item */
		deleteItem(0x8),
		/** User can change item name */
		editName(0x10),
		/** view custom fields */
		viewCFields(0x20),
		/** create, edit and delete custom fields */
		editCFields(0x40),
		/** edit other item settings, not mentioned with own mask */
		editOther(0x80),
		/** edit item image */
		editImage(0x100),
		/** execute reports over item, incl. raw messages access */
		execReports(0x200),
		/** edit ACL-propagated items, e.g. edit units in units group */
		editSubItems(0x400),
		/** view, add and delete item log records */
		manageLog(0x800),
		/** view admin fields */
		viewAFields(0x1000),
		/** create, edit and delete admin fields */
		editAFields(0x2000);
		/** Flag value */
		private long value;

		private accessFlag (long value) {
			this.value=value;
		}

		public long getValue() {
			return value;
		}
	}
	/** Log message action constants */
	public static enum logMessageAction{
		/** Common actions for all items  */

		/** Custom log message from user */
		itemCustomMessage("custom_msg"),
		/** Item name updated */
		itemUpdatedName("update_name"),
		/** Updated user access to item */
		itemUpdatedUserAccess("update_access"),
		/** Item deleted */
		itemDeleted("delete_item");
		private String value;

		private logMessageAction (String value) {
			this.value=value;
		}

		public String getValue() {
			return value;
		}
	}
	/** Events */
	public static enum events {
		/** name property has changed */
		changeName,
		/** dataFlags property has changed */
		changeDataFlags,
		/** userAccess property has changed */
		changeUserAccess,
		/** custom property value property has changed, custom property data represented com {n: name, v: value} hash */
		changeCustomProperty,
		/** item has been deleted<br/>
		 * {@see com.wialon.core.EventHandler#onEvent(java.lang.Enum event, java.lang.Object object, java.lang.Object oldData, java.lang.Object newData)} with:<br/>
		 * {@code event - } {@see Item.events#itemDeleted}<br/>
		 * {@code object - } {@see Item} {@code item}<br/>
		 * {@code oldData - }{@see Long} {@code itemID}<br/>
		 * {@code newData - null}
		 * */
		updateCustomField,
		updateAdminField,
 		itemDeleted,
		/** messages registry */
		messageRegistered,
		/** item measure units has changed*/
		changeMeasureUnits
	}

	/** Measure units flags constants */
	public static enum measureUnitsFlag{
		/** Change measure of units type */
		setMeasureUnits(0x00),
		/** Convert measure of units type */
		convertMeasureUnits(0x01);

		private int value;

		private measureUnitsFlag (int value) {
			this.value=value;
		}

		public int getValue() {
			return value;
		}
	}

	/** Measure units types constants */
	public static enum measureUnitsType {
		/** System SI - km, lt, etc. */
		si(0x00),
		/** United States - mi, gal, etc */
		us(0x01);

		private int value;

		private measureUnitsType (int value) {
			this.value=value;
		}

		public int getValue() {
			return value;
		}
	}
}
