package com.wialon.item;

import com.wialon.core.EventProvider;
import com.wialon.core.Session;
import com.wialon.remote.RemoteHttpClient;
import com.wialon.remote.ResponseHandler;
import com.wialon.messages.Message;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

public abstract class Item extends EventProvider {
	//Base
	private String nm;
	private Long id;
	private Long uacl;
	private Long dataFlags;
	//private int cls;
	//Billing
	private Long crt;
	private Long bact;
	//Custom properties
	private Map<String, String> prp;

	protected ItemType itemType;
	//Todo custom & admin fields
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
			fireEvent(events.changeName, oldName, name);
		}
	}

	private void setUserAccess(Long userAccessLevel) {
		if (this.uacl==null || !this.uacl.equals(userAccessLevel)) {
			Long oldAcl=this.uacl==null ? null : new Long(uacl);
			this.uacl = userAccessLevel;
			fireEvent(events.changeUserAccess, oldAcl, userAccessLevel);
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
			fireEvent(events.changeDataFlags, oldFlags, dataFlags);
		}
	}

	/**
	 * Handle new message from server for this item.
	 * @param message Message data
	 */
	public void handleMessage(Message message) {
		fireEvent(events.messageRegistered, null, message);
	}

	/**
	 * Update custom property, require ACL bit Item.accessFlag.editOther over item
	 * @param propName property name
	 * @param propValue value property
	 * @param callback callback that will receive information about property update
	 */
	public void updateCustomProperty(String propName, String propValue, ResponseHandler callback) {
			RemoteHttpClient.getInstance().remoteCall(
				"item/update_custom_property",
				"{\"itemId\":"+getId()+",\"name\":\""+propName+"\",\"value\":\""+propValue+"\"}",
				new ResponseHandler(callback) {
					@Override
					public void onSuccess(String response) {
						onUpdateCustomProperty(response, getCallback());
					}
				});
	}
	/**
	 * Update item name, require ACL bit Item.accessFlag.editName over item
	 * @param name new name value
	 * @param callback callback that will receive information about update
	 */
	public void updateName(String name, ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"item/update_name",
				"{\"itemId\":"+getId()+",\"name\":\""+name+"\"}",
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
		RemoteHttpClient.getInstance().remoteCall(
				"item/add_log_record",
				"{\"itemId\":"+getId()+",\"action\":\""+action+"\",\"newValue\":\""+(newValue==null?"":newValue)+"\",\"oldValue\":\""+(oldValue==null?"":oldValue)+"\"}",
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
			for (Map.Entry entry : ((JsonObject)data).entrySet())
				setCustomProperty(entry.getKey().toString(), entry.getValue().toString());
		} else if (key.equals("crt") && data.getAsNumber()!=null) {
			setCreatorId(data.getAsNumber().longValue());
		} else if (key.equals("bact") && data.getAsNumber()!=null) {
			setAccountId(data.getAsNumber().longValue());
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

		private Class itemClass;

		private ItemType(Class itemClass) {
			this.itemClass=itemClass;
		}

		public Class getItemClass() {
			return this.itemClass;
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
		itemDeleted,
		/** messages registry */
		messageRegistered
	}
}
