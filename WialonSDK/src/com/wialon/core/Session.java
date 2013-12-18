package com.wialon.core;

import com.wialon.extra.SearchSpec;
import com.wialon.extra.UpdateSpec;
import com.wialon.remote.RemoteHttpClient;
import com.wialon.remote.handlers.ResponseHandler;
import com.wialon.item.Item;
import com.wialon.item.User;
import com.wialon.messages.Message;
import com.wialon.remote.handlers.SearchResponseHandler;
import com.wialon.render.Renderer;
import com.wialon.util.Debug;
import com.google.gson.*;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Wialon session static object.
 * Contain all information about active Wialon server session.
 */
public class Session extends EventProvider {
	private static final Session instance = new Session();
	private String baseUrl;
	private String sessionId;
	private boolean initialized=false;
	private RemoteHttpClient httpClient;
	private User currUser;
	private JsonParser jsonParser;
	private long serverTime;
	private Gson gson;
	private long evtPollInterval;
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> poolEventHandle;
	private PoolEvents poolEvents;
	private Map<Long, Item> itemsById;
	private Map<Item.ItemType, List<Item>> itemsByType;
	private Map<Integer, Item.ItemType> classes;
	private Renderer renderer;
	/** messages loader object*/
	private MessagesLoader messagesLoader;

	public static Session getInstance() {
		return instance;
	}

	private Session() {
	}

	public JsonParser getJsonParser(){
		return jsonParser;
	}

	public Gson getGson(){
		return gson;
	}

	/**
	 * Get item com ID
	 * @param itemId {Integer} Item ID
	 * @return {wialon.item.Item} Item
	 */
	public Item getItem(long itemId) {
		return itemsById==null ? null : itemsById.get(itemId);
	}
	/**
	 * Get collection of items of given type
	 * @param itemsType {ItemType} Type of items to get, pass empty string to fetch all items (examples: "avl_unit", "avl_unit_group", "avl_resource", "avl_retranslator", "user")
	 * @return {List} collection of items
	 */
	public Collection<Item> getItems(Item.ItemType itemsType) {
		if (itemsById==null || itemsByType==null)
			return null;
		return itemsType!=null ? itemsByType.get(itemsType) : getItems();
	}

	/**
	 * Get all items
	 * @return {List} includes all items
	 */
	public Collection<Item> getItems() {
		return itemsById.values();
	}
	/**
	 * Initialize Wialon session
	 * @param baseUrl
	 */
	public boolean initSession(String baseUrl) {
		this.baseUrl=baseUrl;
		this.renderer=new Renderer();
		this.messagesLoader=new MessagesLoader();
		if (httpClient==null)
			httpClient= RemoteHttpClient.getInstance();
		if (jsonParser==null)
			jsonParser=new JsonParser();
		if (gson==null)
			gson=new GsonBuilder().registerTypeAdapter(String.class, new JsonDeserializer<String>(){
				@Override
				public String deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
					return jsonElement.isJsonPrimitive() ? jsonElement.getAsString() : jsonElement.toString();
				}
			}).create();
		return initialized=true;
	}

	/**
	 *
	 * Perform login to Wialon server.
	 * @param user {String} user name
	 * @param password {String} user password
	 * @param callback {ResponseHandler} callback function that is called after login
	 */
	public void login(String user, String password, ResponseHandler callback){
		if (currUser!=null || !isInitialized()) {
			callback.onFailure(2, null);
			return;
		}
		httpClient.remoteCall("core/login", "{\"user\":\"" + user + "\",\"password\":\"" + password + "\"}", new ResponseHandler(callback){
			@Override
			public void onSuccess(String response) {
				onLoginResult(response, this.getCallback());
			}
		});
	}
	/**
	 * Logout from Wialon server.
	 * @param callback {ResponseHandler} callback function that is called after logout: where zero is success
	 */
	public void logout(ResponseHandler callback) {
		if (currUser==null && callback!=null) {
			callback.onFailure(2, null);
			return;
		}
		httpClient.remoteCall("core/logout", "{}", new ResponseHandler(callback) {
			@Override
			public void onSuccess(String response) {
				cleanupSession();
				super.onSuccess(response);
			}
			@Override
			public void onFailure(int errorCode, Throwable throwableError) {
				cleanupSession();
				super.onFailure(errorCode, throwableError);
			}
		});
	}

	/**
	 * Create authorization hash that can be used to create copy of session from any IP
	 * @param callback {ResponseHandler} callback function that is called after login: {authHash: "XXX"}, where zero code is success
	 */
	public void createAuthHash(ResponseHandler callback) {
		if (currUser==null && callback!=null) {
			callback.onFailure(2, null);
			return;
		}
		httpClient.remoteCall("core/create_auth_hash", "{}", callback);
	}

	/**
	 * Search for items
	 * @param searchSpec
	 *		Search specification in form: {itemsType: "", propName: "", propValueMask: "", sortType: ""}
	 *		where itemsType: type of items to search for
	 *		propName - name of property for filtering, usually "sys_name"
	 *		propValueMask - value mask of property for filtering, asteriks can be used
	 *		propType[optional] - type of property, should be empty for simple properties, optional values are: "guid" - convert property value from id to GUID, "list" - search for ID in list property (e.g. unit in group), "propitemname" - search for prop item name, e.g. custom fields, sensors
	 *		sortType - name of property that will be used for sorting, if any, usually used "sys_name"
	 * @param forceRefresh  if non-zero value used, skip any caching and perform operation in realtime (try to avoid passing 1 here)
	 * @param dataFlags what data-flags returned items should have
	 * @param indexFrom  starting index for returning result, for new searches use zero value
	 * @param indexTo ending index for returning result, for new searches use ('max number of items to return'-1) value
	 * @param callback callback function that is called after remote call: callback(code, data),
	 *		where code: operation result code (zero is success)
	 *		data (result) consists of: {items: [], dataFlags: 0x10, totalItemsCount: 100, indexFrom: 0, indexTo: 9, searchSpec: {...}}
	 */
	public void searchItems(SearchSpec searchSpec, int forceRefresh, long dataFlags, int indexFrom, int indexTo, SearchResponseHandler callback) {
		if (currUser==null || searchSpec==null) {
			callback.onFailure(2, null);
			return;
		}
		httpClient.remoteCall("core/search_items",
				"{\"spec\":"+gson.toJson(searchSpec)+",\"force\":"+forceRefresh+",\"flags\":"+dataFlags+",\"from\":"+indexFrom+",\"to\":"+indexTo+"}",
				new ResponseHandler(callback) {
					@Override
					public void onSuccess(String response) {
						onSearchItemsResult(response, getCallback());
					}
				});
	}

	/**
	 * Search for item
	 * @param id ID of item to search for
	 * @param dataFlags {Integer}: what data-flags returned item should have
	 * @param callback {?Function} callback function that is called after remote call: callback(code, item)
	 */
	public void searchItem(long id, long dataFlags, SearchResponseHandler callback) {
		if (this.currUser==null) {
			callback.onFailure(2, null);
			return;
		}
		// forward call to server
		httpClient.remoteCall(
				"core/search_item",
				"{\"id\":"+id+",\"flags\":"+dataFlags+"}",
				getOnSearchItemResultCallback(callback)
		);
	}
	/**
	 * Update data flags for items: load or unload items from current session, change their data flags
	 * @param spec {Object}
	 *		Specification of what to update in form [{type: "id|type|col", data: <value-based-on-type>, flags: 100, mode: 0/1/2},...]
	 *		where flags: what flags to setup
	 *		type - selector: id - item-id, item - item type, col - collection of items ID
	 *		mode - update flags mode: 0 - set, 1 - add, 2 - remove
	 * @param callback {ResponseHandler} callback function that is called after remote call: callback(code), where zero code is success
	 */
	public void updateDataFlags(UpdateSpec[] spec, ResponseHandler callback) {
		if (currUser==null || spec==null) {
			callback.onFailure(2, null);
			return;
		}
		httpClient.remoteCall("core/update_data_flags",
				"{\"spec\":"+gson.toJson(spec)+"}",
				new ResponseHandler(callback) {
					@Override
					public void onSuccess(String response) {
						onDataFlagsUpdated(response, this.getCallback());
					}
				});
	}
	/**
	 * Get events session ID
	 * @return {String} events session ID
	 */
	public String getId() {
		return sessionId;
	}

	/**
	 * Get renderer object for given session
	 * @return renderer
	 */
	public Renderer getRenderer() {
		return this.renderer;
	}

	/**
	 * Get messages loader object for given session
	 * @return MessagesLoader renderer
	 */
	public MessagesLoader getMessagesLoader(){
		return this.messagesLoader;
	}

	/**
	 * Check on initialize Wialon session
	 * @return {boolean} initialization state
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Get base URL for server
	 * @return {String} base URL: remote://api.wialon.net or https://secure.wialon.net
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * Get base URL for GIS service
	 * @param gisType type of GIS function: render, search, geocode
	 * @return base URL suitable for prepending GIS requests of given type
	 */
	public String getBaseGisUrl(String gisType) {
		if (!this.baseUrl.equals("")) {
			// extract DNS of Wialon server from base URL (e.g. http://kit-api.wialon.com)
			String[] arr = this.baseUrl.split("//");
			if (arr.length >= 2) {
				if (gisType.equals("render"))
					return "http://render.mapsviewer.com/" + arr[1];
				else if (gisType.equals("search"))
					return "http://search.mapsviewer.com/" + arr[1];
				else if (gisType.equals("geocode"))
					return "http://geocode.mapsviewer.com/" + arr[1];
			}
		}
		return this.baseUrl;
	}
	/**
	 * Get latest known server time
	 * @return {long} Last known server time in UNIX seconds
	 */
	public long getServerTime() {
		return serverTime;
	}
	/**
	 * Get current user
	 * @return {User} Currently active user
	 */
	public User getCurrUser() {
		return currUser;
	}
	/**
	 * Change event poll interval
	 * @param interval value in milliseconds between 2000 - 120000 (2-120 seconds)
	 */
	public boolean setEvtPollInterval (long interval) {
		if (interval>=2000 && interval<=120000) {
			evtPollInterval=interval;
			if (scheduler==null)
				scheduler= Executors.newScheduledThreadPool(1);
			cancelEventsPoll();
			if (poolEvents==null)
				poolEvents=new PoolEvents();
			poolEventHandle=scheduler.scheduleAtFixedRate(poolEvents, evtPollInterval, evtPollInterval, TimeUnit.MILLISECONDS);
			return true;
		} else
			return false;
	}

	/**
	 * Get all available hardware types
	 * @param callback callback function that is called after remote call on success is a object collection of hw types: {ID: {id: ID, name: NAME}, ...}
	 */
	public void getHwTypes(ResponseHandler callback) {
		if (currUser==null) {
			callback.onFailure(2, null);
			return;
		}
		httpClient.remoteCall(
				"core/get_hw_types",
				"{}",
				callback
		);
	}

	/**
	 * Get all available hardware type commands
	 * @param deviceTypeId ID of hw type to search for
	 * @param unitId ID of avl_unit
	 * @param callback callback function that is called after remote call
	 */
	public void getHwCommands(Long deviceTypeId, Long unitId, ResponseHandler callback) {
		if (currUser==null) {
			callback.onFailure(2, null);
			return;
		}
		httpClient.remoteCall(
				"core/get_hw_cmds",
				"{\"deviceTypeId\":"+deviceTypeId+",\"unitId\":"+unitId+"}",
				callback
		);
	}

	/**
	 * Create new unit
	 * @param creator user-creator, either current user nor one of its descendants
	 * @param name unit name
	 * @param hwTypeId hardware type id, see getHwTypes() for full list of hw types
	 * @param dataFlags which flags initially to return
	 * @param callback callback function that is called after remote call with new Unit object, important: obj is not loaded into session
	 */
	public void createUnit (User creator, String name, long hwTypeId, long dataFlags, SearchResponseHandler callback) {
		if (currUser==null) {
			callback.onFailure(2, null);
			return;
		}
		httpClient.remoteCall(
				"core/create_unit",
				"{\"creatorId\":"+creator.getId()+",\"name\":\""+name+"\",\"hwTypeId\":"+hwTypeId+",\"dataFlags\":"+dataFlags+"}",
				getOnSearchItemResultCallback(callback)
		);
	}
	/**
	 * Create new user
	 * @param creator user-creator, either current user nor one of its descendants
	 * @param name unit name
	 * @param password user password
	 * @param dataFlags which flags initially to return
	 * @param callback callback function that is called after remote call with new User object, important: obj is not loaded into session
	 */
	public void createUser (User creator, String name, String password, long dataFlags, SearchResponseHandler callback) {
		if (currUser==null) {
			callback.onFailure(2, null);
			return;
		}
		httpClient.remoteCall(
				"core/create_user",
				"{\"creatorId\":"+creator.getId()+",\"name\":\""+name+"\",\"password\":\""+password+"\",\"dataFlags\":"+dataFlags+"}",
				getOnSearchItemResultCallback(callback)
		);
	}

	/**
	 * Create new unit group
	 * @param creator user-creator, either current user nor one of its descendants
	 * @param name unit group name
	 * @param dataFlags which flags initially to return
	 * @param callback callback function that is called after remote call with new UnitGroup object, important: obj is not loaded into session
	 */
	public void createUnitGroup(User creator, String name, long dataFlags, SearchResponseHandler callback) {
		if (currUser==null) {
			callback.onFailure(2, null);
			return;
		}
		httpClient.remoteCall(
				"core/create_unit_group",
				"{\"creatorId\":"+creator.getId()+",\"name\":\""+name+"\",\"dataFlags\":"+dataFlags+"}",
				getOnSearchItemResultCallback(callback)
		);
	}

	/**
	 * Create new resource
	 * @param creator user-creator, either current user nor one of its descendants
	 * @param name resource name
	 * @param dataFlags which flags initially to return
	 * @param callback callback function that is called after remote call with new Resource object, important: obj is not loaded into session
	 */
	public void createResource(User creator, String name, long dataFlags, SearchResponseHandler callback) {
		if (currUser==null) {
			callback.onFailure(2, null);
			return;
		}
		httpClient.remoteCall(
				"core/create_resource",
				"{\"creatorId\":"+creator.getId()+",\"name\":\""+name+"\",\"dataFlags\":"+dataFlags+"}",
				getOnSearchItemResultCallback(callback)
		);
	}
	/**
	 * Update item data
	 * @param item {Item} - item
	 * @param itemData - object with data from server
	 */
	public void updateItem(Item item, JsonObject itemData) {
		Debug.log.info("updateItem");
		for (Map.Entry<String, JsonElement> data : itemData.entrySet()) {
			item.updateItemData(data.getKey(), data.getValue());
		}
	}

	/**
	 * Delete item, require ACL bit Item.accessFlag.deleteItem over item
	 * Can't be deleted: current user, users that has other items created com them and billing accounts(resources).
	 * After successful deletion item will be removed from session automatically.
	 * @param item item
	 * @param callback callback that will receive information about item deletion
	 */
	public void deleteItem(Item item, ResponseHandler callback) {
		final long itemId=item.getId();
		httpClient.remoteCall(
				"item/delete_item",
				"{\"itemId\":"+itemId+"}",
				new ResponseHandler(callback) {
					@Override
					public void onSuccess(String response) {
						onDeleteItem(itemId, response, getCallback());
					}
				}
		);
	}
	/**
	 * Request reset password for Wialon user.
	 * @param user Wialon user
	 * @param email user e-mail
	 * @param emailFrom E-Mail from which confirmation e-mail will be sent
	 * @param url url that will be in e-mail reset request, user will be pointed to: <url>?user=<login>&passcode=<passcode>
	 * @param lang e-mail language
	 * @param callback callback function that is called after login
	 */
	public void resetPasswordRequest(User user, String email, String emailFrom, String url, String lang, ResponseHandler callback) {
		if (currUser==null) {
			callback.onFailure(2, null);
			return;
		}
		httpClient.remoteCall(
				"core/reset_password_request",
				"{\"user\":\""+user.getName()+"\",\"email\":\""+email+"\",\"emailFrom\":\""+emailFrom+"\",\"url\":\""+url+"\",\"lang\":\""+lang+"\"}",
				callback
		);
	}
	/**
	 * Perform password reset for Wialon user.
	 * @param user Wialon user
	 * @param code code generated com resetPasswordRequest request and sent to user com email
	 * @param callback callback function that is called after login and data.newPassword contain new password
	 */
	public void resetPasswordPerform (User user, String code, ResponseHandler callback) {
		if (currUser==null) {
			callback.onFailure(2, null);
			return;
		}
		httpClient.remoteCall(
				"core/reset_password_perform",
				"{\"user\":\""+user.getName()+"\",\"code\":\""+code+"\"}",
				callback
		);
	}
	/**
	 * Send SMS, current user should have flag User.userFlag.canSendSMS and should have enough SMS messages available for account
	 * @param phoneNumber phone number in international format
	 * @param smsText SMS message text
	 * @param callback callback that will receive information about SMS send operation
	 */
	public void sendSms(String phoneNumber, String smsText, ResponseHandler callback) {
		httpClient.remoteCall(
				"user/send_sms",
				"{\"phoneNumber\":\""+phoneNumber+"\",\"smsText\":\""+smsText+"\"}",
				callback
		);
	}
	/** Get account information for current user as JSON Object
	 * @param fullInfo return all billing account information
	 * @param callback function to call with result of remote call and data contain full account information.
	 */
	public void getAccountData(boolean fullInfo, ResponseHandler callback) {
		httpClient.remoteCall(
				"core/get_account_data",
				"{\"type\":"+(fullInfo ? 2 : 1)+"}",
				callback
		);
	}
	/** Check collection of items for billing service and access level usage possibility
	 * @param items array of ID of items to check
	 * @param serviceName billing service name
	 * @param accessFlags ACL flags to check for
	 * @param callback function to call with result of remote call and data contain collection of items IDs that can be used for such billing service.
	 */
	public void checkItemsBilling (Long[] items, String serviceName, Long accessFlags, ResponseHandler callback) {
		httpClient.remoteCall(
				"core/check_items_billing",
				"{\"items\":"+gson.toJson(items)+",\"serviceName\":\""+serviceName+"\",\"accessFlags\":"+accessFlags+"}",
				callback
		);
	}
	/** Get report tables available for current user as JSON Object
	 * @param callback function to call with result of remote call and data contain full report tables information.
	 */
	public void getReportTables(ResponseHandler callback) {
		httpClient.remoteCall(
				"report/get_report_tables",
				"{}",
				callback
		);
	}
//Todo
//	/**
//	 * Get base URL for GIS service
//	 * @param gisType {String} type of GIS function: render, search, geocode
//	 * @return {String} base URL suitable for prepending GIS requests of given type
//	 */
//	getBaseGisUrl: function(gisType) {
//		if (!this.__internalGis && this.__baseUrl != "") {
//			// extract DNS of Wialon server from base URL (e.g. remote://kit-api.wialon.com)
//			var arr = this.__baseUrl.split("//");
//			if (arr.length >= 2) {
//				if (gisType == "render")
//					return "remote://render.mapsviewer.com/" + arr[1];
//				else if (gisType == "search")
//					return "remote://search.mapsviewer.com/" + arr[1];
//				else if (gisType == "geocode")
//					return "remote://geocode.mapsviewer.com/" + arr[1];
//			}
//		}
//		return this.__baseUrl;
//	}

	private ResponseHandler getOnSearchItemResultCallback(ResponseHandler callback) {
		return new ResponseHandler(callback) {
			@Override
			public void onSuccess(String response) {
				onSearchItemResult(response, getCallback());
			}
		};
	}

	private void cancelEventsPoll() {
		if (poolEventHandle!=null) {
			poolEventHandle.cancel(true);
			poolEventHandle=null;
		}
	}

	private void cleanupSession() {
		cancelEventsPoll();
		initialized=false;
		currUser=null;
		sessionId=null;
		baseUrl=null;
		itemsById=null;
		itemsByType=null;
		classes=null;
		renderer=null;
		messagesLoader=null;
	}

	private void onLoginResult (String result, ResponseHandler callback) {
		if (parseSessionData(result))
			callback.onSuccess(result);
		else
			callback.onFailure(6, null);
	}

	private boolean parseSessionData (String sessionData) {
		if (currUser!=null)
			return false;
		try {
			JsonElement sessionJson=jsonParser.parse(sessionData);
			if (sessionJson==null || !sessionJson.isJsonObject())
				return false;
			//Init maps and collections
			itemsById=new HashMap<Long, Item>();
			itemsByType=new HashMap<Item.ItemType, List<Item>>();
			classes=new HashMap<Integer, Item.ItemType>();
			JsonObject sessionObject=((JsonObject)sessionJson);
			for (Map.Entry entry : sessionObject.get("classes").getAsJsonObject().entrySet()) {
				try {
					classes.put(Integer.parseInt(entry.getValue().toString()), Item.ItemType.valueOf(entry.getKey().toString()));
				} catch (IllegalArgumentException e) {
					//Class not compatible com SDK
					e.printStackTrace();
				}
			}
			sessionId=sessionObject.get("eid").getAsString();
			serverTime=sessionObject.get("tm").getAsLong();
			currUser=(User)constructItem(sessionObject.get("user").getAsJsonObject(), User.defaultDataFlags());//gson.fromJson(sessionObject.get("user").getAsJsonObject(), User.class);
			registerItem(currUser);
			setEvtPollInterval(5000);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Debug.log.log(Level.ALL, "Exception on parseSessionData", e);
			return false;
		}
	}
	/**
	 * Handle item search result from server
	 */
	private void onSearchItemResult(String result, ResponseHandler callback) {
		if (result==null) {
			// error
			callback.onFailure(6, null);
			return;
		}
		//Send string result
		callback.onSuccess(result);
		// create result
		// construct item
		JsonElement responseJson=jsonParser.parse(result);
		if (responseJson==null || !responseJson.isJsonObject()) {
			return;
		}
		JsonElement itemJson=responseJson.getAsJsonObject().get("item");
		JsonElement itemFlags=responseJson.getAsJsonObject().get("flags");
		if (itemJson==null || !itemJson.isJsonObject() || itemFlags==null || itemFlags.getAsNumber()==null){
			return;
		}
		if (callback instanceof SearchResponseHandler)
			((SearchResponseHandler)callback).onSuccessSearch(constructItem(itemJson.getAsJsonObject(), itemFlags.getAsLong()));
	}
	/**
	 * Handle items search result from server
	 * callback require 2nd parameter in form: {items: [], dataFlags: 0x10, totalItemsCount: 100, indexFrom: 0, indexTo: 9, searchSpec: {...}}
	 */
	private void onSearchItemsResult(String result, ResponseHandler callback) {
		if (result==null) {
			callback.onFailure(6, null);
			return;
		}
		//Send string result
		callback.onSuccess(result);
		// construct items
		JsonElement responseJson=jsonParser.parse(result);
		if (responseJson==null || !responseJson.isJsonObject()) {
			return;
		}
		JsonElement dataFlags=responseJson.getAsJsonObject().get("dataFlags");
		JsonElement itemsJson=responseJson.getAsJsonObject().get("items");
		if (itemsJson==null || !itemsJson.isJsonArray() || dataFlags==null || dataFlags.getAsNumber()==null) {
			return;
		}
		JsonArray responseItems=itemsJson.getAsJsonArray();
		Item[] items=new Item[responseItems.size()];
		for (int i=0; i<responseItems.size(); i++) {
			try{
				JsonObject itemData=responseItems.get(i).getAsJsonObject();
				Item item=constructItem(itemData, dataFlags.getAsLong());
				items[i]=item;
			} catch (Exception e){
				e.printStackTrace();
				Debug.log.info("ERROR");
			}
		}
		if (callback instanceof  SearchResponseHandler)
			((SearchResponseHandler)callback).onSuccessSearch(items);
	}

	private void onDataFlagsUpdated(String result, ResponseHandler callback) {
		if (result==null) {
			if (callback!=null)
				callback.onFailure(6, null);
			return;
		}
		JsonElement responseJson=jsonParser.parse(result);
		if (responseJson==null || !responseJson.isJsonArray()) {
			callback.onFailure(6, null);
			return;
		}
		JsonArray responseItems=((JsonArray)responseJson);
		// iterate over returned array
		for (int i=0; i<responseItems.size(); i++) {
			try {
				// update items data, construct new items
				long itemId=responseItems.get(i).getAsJsonObject().get("i").getAsLong();
				long itemFlags=responseItems.get(i).getAsJsonObject().get("f").getAsLong();
				JsonObject itemData=responseItems.get(i).getAsJsonObject().get("d").getAsJsonObject();
				// check if we need to construct this item
				Item item=itemsById.get(itemId);
				if (item==null && itemFlags!=0 && itemData!=null) {
					// construct item
					item=constructItem(itemData, itemFlags);
					if (item!=null)
						registerItem(item);
				} else {
					// remove item
					if (itemFlags==0)
						removeItem(item);
					else {
						if (item==null)
							continue;
						// update item
						if (itemData!=null)
							updateItem(item, itemData);
						item.setDataFlags(itemFlags);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				callback.onFailure(6, e);
				Debug.log.info("ERROR");
			}
		}
		Debug.log.info("END");
		callback.onSuccess(result);
	}

	/**
	 * Handle item deletion result from server
	 */
	private void onDeleteItem(long itemId, String result, ResponseHandler callback) {
		if (result!=null) {
			// remove item from session, if any
			Item item = this.getItem(itemId);
			onItemDeleted(item);
			callback.onSuccess(result);
			return;
		}
		callback.onFailure(6, null);
	}

	private void registerItem(Item item) {
		if (item==null || itemsById==null)
			return;
		itemsById.put(item.getId(), item);
		List <Item> itemsByCurrentType=itemsByType.get(item.getItemType());
		if (itemsByCurrentType==null) {
			itemsByCurrentType=new ArrayList<Item>();
			itemsByType.put(item.getItemType(), itemsByCurrentType);
		}
		itemsByCurrentType.add(item);
	}

	private void removeItem(Item item) {
		if (item==null)
			return;
		// remove item from hashes
		itemsById.remove(item.getId());
		List<Item> itemsByCurrentType=itemsByType.get(item.getItemType());
		if (itemsByCurrentType!=null)
			itemsByCurrentType.remove(item);
	}

	/**
	 * Item has been deleted.
	 * @param item {Item} item to remove
	 */
	private void onItemDeleted(Item item) {
		if (item==null)
			return;
		// remove item from session
		item.fireEvent(Item.events.itemDeleted, item.getId(), null);
		removeItem(item);
	}

	private Item constructItem(JsonObject itemData, Long itemFlags) {
		if (itemData==null || itemFlags==null)
			return null;
		Item.ItemType itemType=classes.get(itemData.get("cls").getAsInt());
		if (itemType!=null && itemType.getItemClass()!=null) {
			Item item=(Item)gson.fromJson(itemData, itemType.getItemClass());
			item.setDataFlags(itemFlags);
			return item;
			//Todo skipped updates
		}
		return null;
	}

	private void poolEvents(DefaultHttpClient httpClient) {
		if (sessionId==null)
			return;
		List<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("sid", sessionId));
		RemoteHttpClient.getInstance().post(baseUrl + "/avl_evts", nameValuePairs, httpClient, new ResponseHandler() {
			@Override
			public void onSuccess(String response) {
				Debug.log.info("onSuccessAvlEvents " + response);
				eventsResponse(response);
				super.onSuccess(response);
			}

			@Override
			public void onFailure(int errorCode, Throwable throwableError) {
				if (errorCode==1 && throwableError==null){
					fireEvent(events.invalidSession, null, null);
					cleanupSession();
				}
				Debug.log.info("onFailureAvlEvents" + "errorCode" + errorCode);
				super.onFailure(errorCode, throwableError);
			}
		});
	}

	private void eventsResponse(String response) {
		try {
			JsonElement responseJson=jsonParser.parse(response);
			if (responseJson==null || !responseJson.isJsonObject())
				return;
			JsonObject responseObject=((JsonObject)responseJson);
			// update server time
			serverTime = responseObject.get("tm").getAsLong();
			JsonArray events=responseObject.get("events").getAsJsonArray();
			if (events==null)
				return;
			for(int i=0; i<events.size(); i++) {
				JsonObject evtData=events.get(i).getAsJsonObject();
				if (evtData!=null) {
					long id=evtData.get("i").getAsLong();
					if (id>0) {
						Item item=getItem(id);
						if (item!=null) {
							String type=evtData.get("t").getAsString();
							if (type!=null) {
								if(type.equals("u"))// data update event
									updateItem(item, evtData.get("d").getAsJsonObject());
								else if (type.equals("m")) {// new message event
									String tp=evtData.get("d").getAsJsonObject().get("tp").getAsString();
									long f=evtData.get("d").getAsJsonObject().get("f").getAsLong();
									Message.messageFlag flag=Message.messageFlag.getMessageFlag(f);
									if (flag==null)
										continue;
									Class clazz=Message.MessageType.getMessageClass(flag, tp);
									if (clazz!=null)
										item.handleMessage((Message)gson.fromJson(evtData.get("d"), clazz));
								} else if (type.equals("d"))
									onItemDeleted(item);
							}
						} else
							hashCode();//Todo skipped updates
					} else if (id==-1) {
						// file upload result
						fireEvent(Session.events.fileUploaded, null, evtData.get("d"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		fireEvent(events.serverUpdated, null, serverTime);
	}

	private final class PoolEvents implements Runnable {
		private DefaultHttpClient httpClient;
		public PoolEvents () {
			httpClient= RemoteHttpClient.getInstance().getNewHttpClient();
		}
		@Override
		public void run() {
			poolEvents(httpClient);
		}
	}

	public static enum events {
		/** server time was updated, e.g. current session state was synchronized with server<br/>
		 * {@see EventHandler#onEvent(java.lang.Enum event, java.lang.Object object, java.lang.Object oldData, java.lang.Object newData)} with:<br/>
		 * {@code event - } {@see Session.events#serverUpdated}<br/>
		 * {@code object - } {@see Session} {@code session}<br/>
		 * {@code oldData - null}<br/>
		 * {@code newData - } {@see long} {@code serverTime}
		 * */
		serverUpdated,
		/** session has been lost */
		invalidSession,
		/** file(s) has been uploaded into session, event data is JSON for upload result
		 * {@see EventHandler#onEvent(java.lang.Enum event, java.lang.Object object, java.lang.Object oldData, java.lang.Object newData)} with:<br/>
		 * {@code event - } {@see Session.events#fileUploaded}<br/>
		 * {@code object - } {@see Session} {@code session}<br/>
		 * {@code oldData - null}<br/>
		 * {@code newData - } {@see JsonObject} {@code eventData}
		 * */
		fileUploaded
	}
}
