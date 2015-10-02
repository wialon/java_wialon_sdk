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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wialon.core.Session;
import com.wialon.item.prop.ItemProperties;
import com.wialon.item.prop.Sensor;
import com.wialon.messages.Message;
import com.wialon.messages.UnitData;
import com.wialon.remote.RemoteHttpClient;
import com.wialon.remote.handlers.ResponseHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Unit extends ItemIcon {
	private String uid;
	private Long hw;
	private String ph;
	private String ph2;
	private String psw;
	private List cmds;
	private UnitData.Position pos;
	private UnitData lmsg;
	private Long cfl;
	private Long cnm;
	private Long cneh;
	private Long cnkb;
	private Map<String, Object> prms;
	private Sensor sensorPlugin;
	private Map<String, String> sens;
	private Map<String, String> cml;
	private ItemProperties commandDefinitionsPlugin;

	public ItemProperties getCommandDefinitionsPlugin() {
		return commandDefinitionsPlugin == null ? commandDefinitionsPlugin = new ItemProperties(cml, "cml", this, events.updateCommandDefinition, "unit/update_command_definition") : commandDefinitionsPlugin;
	}

	public Sensor getSensorPlugin() {
		return sensorPlugin==null ? sensorPlugin=new Sensor(sens, "sens", this, events.updateSensor, "unit/update_sensor") : sensorPlugin;
	}

	public Unit () {
		itemType=ItemType.avl_unit;
	}

	/**
	 * Unit unique ID
	 * @return unit unique ID
	 */
	public String getUniqueId() {
		return uid;
	}

	/**
	 * Unit unique ID
	 * @param uniqueId new id
	 */
	private void setUniqueId(String uniqueId) {
		if (this.uid==null || !this.uid.equals(uniqueId)) {
			String oldUniqueId=this.uid==null ? null : new String(uid);
			this.uid = uniqueId;
			fireEvent(events.changeUniqueId, this, oldUniqueId, uniqueId);
		}
	}

	/**
	 * Unit device type ID
	 * @return device type
	 */
	public Long getDeviceTypeId() {
		return hw;
	}

	/**
	 * Unit device type ID
	 * @param deviceTypeId new id
	 */
	private void setDeviceTypeId(Long deviceTypeId) {
		if (this.hw==null || !this.hw.equals(deviceTypeId)) {
			Long oldHw=this.hw==null ? null : new Long(this.hw);
			this.hw = deviceTypeId;
			fireEvent(events.changeDeviceTypeId, this, oldHw, hw);
		}
	}

	/**
	 * Unit phone number
	 * @return phone number
	 */
	public String getPhoneNumber() {
		return ph;
	}

	/**
	 * Unit phone number
	 * @param phoneNumber new phoneNumber()
	 */
	private void setPhoneNumber(String phoneNumber) {
		if (this.ph==null || !this.ph.equals(phoneNumber)){
			String oldPhone=this.ph==null ? null : new String(phoneNumber);
			this.ph = phoneNumber;
			fireEvent(events.changePhoneNumber, this, oldPhone, ph);
		}
	}

	/**
	 * Secondary unit phone number
	 * @return second phone number
	 */
	public String getPhoneNumber2() {
		return ph2;
	}

	/**
	 * Secondary unit phone number
	 * @param phoneNumber2 new second phone number
	 */
	private void setPhoneNumber2(String phoneNumber2) {
		if (this.ph2==null || !this.ph2.equals(phoneNumber2)){
			String oldPhone2=this.ph2==null ? null : new String(phoneNumber2);
			this.ph2 = phoneNumber2;
			fireEvent(events.changePhoneNumber2, this, oldPhone2, ph2);
		}

	}

	/**
	 * Unit access password
	 * @return access password
	 */
	public String getAccessPassword() {
		return psw;
	}

	/**
	 * Unit access password
	 * @param accessPassword new access password
	 */
	private void setAccessPassword(String accessPassword) {
		if (this.psw==null || !this.psw.equals(accessPassword)) {
			String oldPassword=this.psw==null ? null : new String(psw);
			this.psw = accessPassword;
			fireEvent(events.changeAccessPassword, this, oldPassword, psw);
		}
	}

	/**
	 * Unit commands list
	 * @return list of commands
	 */
	public List getCommands() {
		return cmds;
	}

	/**
	 * Unit commands list
	 * @param commands new list of commands
	 */
	private void setCommands(List commands) {
		this.cmds = commands;
		fireEvent(events.changeCommands, this, null, commands);
	}

	/**
	 * Unit calculation flags
	 * @return flags value
	 */
	public Long getCalcFlags() {
		return cfl;
	}

	/**
	 * Unit calculation flags
	 * @param calcFlags new flags value
	 */
	private void setCalcFlags(Long calcFlags) {
		if (this.cfl==null || !this.cfl.equals(calcFlags)) {
			Long oldFlags=this.cfl==null ? null : new Long(cfl);
			this.cfl = calcFlags;
			fireEvent(events.changeCalcFlags, this, oldFlags, cfl);
		}
	}

	/**
	 * Unit mileage counter
	 * @return mileage counter current value
	 */
	public Long getMileageCounter() {
		return cnm;
	}

	/**
	 * Unit mileage counter, in km
	 * @param mileageCounter new mileage counter value
	 */
	private void setMileageCounter(Long mileageCounter) {
		if (this.cnm==null || !this.cnm.equals(mileageCounter)) {
			Long oldMileage=this.cnm==null ? null : new Long(cnm);
			this.cnm = mileageCounter;
			fireEvent(events.changeMileageCounter, this, oldMileage, cnm);
		}
	}

	/**
	 * Unit engine hours counter, in hours
	 * @return engine hours counter current value
	 */
	public Long getEngineHoursCounter() {
		return cneh;
	}

	/**
	 * Unit engine hours counter, in hours
	 * @param engineHoursCounter new engine hours counter value
	 */
	private void setEngineHoursCounter(Long engineHoursCounter) {
		if (this.cneh==null || !this.cneh.equals(engineHoursCounter)) {
			Long oldEngine=this.cneh==null ? null : new Long(cneh);
			this.cneh = engineHoursCounter;
			fireEvent(events.changeEngineHoursCounter, this, oldEngine, cneh);
		}
	}

	/**
	 * Unit GPRS traffic counter, in KB
	 * @return traffic counter current value
	 */
	public Long getTrafficCounter() {
		return cnkb;
	}

	/**
	 *Unit GPRS traffic counter, in KB
	 * @param trafficCounter new traffic counter value
	 */
	private void setTrafficCounter(Long trafficCounter) {
		if (cnkb==null || !cnkb.equals(trafficCounter)) {
			Long oldTraffic= cnkb==null ? null : new Long(cnkb);
			this.cnkb = trafficCounter;
			fireEvent(events.changeTrafficCounter, this, oldTraffic, cnkb);
		}
	}

	/**
	 * Message parameters change
	 * @return map of parameters
	 */
	public Map<String, Object> getMessageParams() {
		return prms;
	}

	/**
	 * Message parameters change
	 * @param messageParams new map of parameters
	 */
	private void setMessageParams(Map<String, Object> messageParams) {
		this.prms = messageParams;
		fireEvent(events.changeMessageParams, this, null, messageParams);
	}

	/**
	 * Unit position
	 * @return get unit position
	 */
	public UnitData.Position getPosition() {
		return pos;
	}

	/**
	 * Unit last message
	 * @return get unit last message
	 */
	public UnitData getLastMessage() {
		return lmsg;
	}
	/**
	 * Schedule remote command to unit, require ACL of current user not lower then specified in command properties
	 * @param commandName command name
	 * @param linkType link type, can be empty for auto-select or one of: gsm, tcp, udp, vrt
	 * @param param optional command textual parameter
	 * @param timeout timeout for command to wait in commands queue, in seconds
	 * @param flags additional flags
	 * @param callback callback that get result of command scheduling
	 */
	public void remoteCommand(String commandName, String linkType, String param, int timeout, long flags, ResponseHandler callback) {
		JsonObject params=new JsonObject();
		params.addProperty("itemId", getId());
		params.addProperty("commandName", commandName);
		params.addProperty("linkType", linkType);
		params.addProperty("param", param);
		params.addProperty("timeout", timeout);
		params.addProperty("flags", flags);
		RemoteHttpClient.getInstance().remoteCall(
				"unit/exec_cmd",
				params,
				callback);
	}

	/**
	 * Update unit device type configuration, require ACL bit Unit.accessFlag.editDevice over item
	 * @param deviceTypeId ID of device type
	 * @param uniqueId unique id for unit (IMEI, etc)
	 * @param callback callback that get result of server operation
	 */
	public void updateDeviceSettings (long deviceTypeId, String uniqueId, ResponseHandler callback) {
		JsonObject params=new JsonObject();
		params.addProperty("itemId", getId());
		params.addProperty("deviceTypeId", deviceTypeId);
		params.addProperty("uniqueId", uniqueId);
		RemoteHttpClient.getInstance().remoteCall(
				"unit/update_device_type",
				params,
				getOnUpdatePropertiesCallback(callback)
		);
	}

	/**
	 * Update unit phone number, require ACL bit Unit.accessFlag.editDevice over item
	 * @param phoneNumber new phone number
	 * @param callback callback that get result of server operation
	 */
	public void updatePhoneNumber(String phoneNumber, ResponseHandler callback) {
		JsonObject params=new JsonObject();
		params.addProperty("itemId", getId());
		params.addProperty("phoneNumber", phoneNumber);
		RemoteHttpClient.getInstance().remoteCall(
				"unit/update_phone",
				params,
				getOnUpdatePropertiesCallback(callback));
	}
	/**
	 * Update secondary unit phone number, require ACL bit Unit.accessFlag.editDevice over item
	 * @param phoneNumber new phone number
	 * @param callback callback that get result of server operation
	 */
	public void updatePhoneNumber2(String phoneNumber, ResponseHandler callback) {
		JsonObject params=new JsonObject();
		params.addProperty("itemId", getId());
		params.addProperty("phoneNumber", phoneNumber);
		RemoteHttpClient.getInstance().remoteCall(
				"unit/update_phone2",
				params,
				getOnUpdatePropertiesCallback(callback));
	}
	/**
	 * Update unit access password, require ACL bit Unit.accessFlag.editDevice over item
	 * @param accessPassword new access password
	 * @param callback callback that get result of server operation
	 */
	public void updateAccessPassword(String accessPassword, ResponseHandler callback) {
		JsonObject params=new JsonObject();
		params.addProperty("itemId", getId());
		params.addProperty("accessPassword", accessPassword);
		RemoteHttpClient.getInstance().remoteCall(
				"unit/update_access_password",
				params,
				getOnUpdatePropertiesCallback(callback));
	}
	/**
	 * Update mileage counter, require ACL bit Unit.accessFlag.editCounters over item
	 * @param newValue new value for counter, in km
	 * @param callback callback that get result of server operation
	 */
	public void updateMileageCounter(Long newValue, ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"unit/update_mileage_counter",
				"{\"itemId\":"+getId()+",\"newValue\":"+newValue+"}",
				getOnUpdatePropertiesCallback(callback));
	}

	/**
	 * Update engine hours counter, require ACL bit Unit.accessFlag.editCounters over item
	 * @param newValue new value for counter, in hours
	 * @param callback callback that get result of server operation
	 */
	public void updateEngineHoursCounter(Long newValue, ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"unit/update_eh_counter",
				"{\"itemId\":"+getId()+",\"newValue\":"+newValue+"}",
				getOnUpdatePropertiesCallback(callback));
	}
	/**
	 * Update traffic counter, require ACL bit Unit.accessFlag.editCounters over item
	 * @param newValue new value for counter, in kb
	 * @param regReset Register reset traffic counter message in unit events
	 * @param callback callback that get result of server operation
	 */
	public void updateTrafficCounter(Long newValue, boolean regReset,ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"unit/update_traffic_counter",
				"{\"itemId\":"+getId()+",\"newValue\":"+newValue+",\"regReset\":"+(regReset ? 1 : 0)+"}",
				getOnUpdatePropertiesCallback(callback));
	}
	/**
	 * Update calculation flags, see Unit.calcFlags for possible flags, require ACL bit Unit.accessFlag.editCounters over item
	 * @param newValue new value for calculation flags
	 * @param callback callback that get result of server operation
	 */
	public void updateCalcFlags(Long newValue, ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"unit/update_calc_flags",
				"{\"itemId\":"+getId()+",\"newValue\":"+newValue+"}",
				getOnUpdatePropertiesCallback(callback));
	}
	/**
	 * Handle new message from server for this unit.
	 * @param message Message data
	 */
	@Override
	public void handleMessage(Message message) {
		// check for position/last message update
		//Log.d("Updating message", "Updating message");
		if (message!=null && message.getMessageType().equals(Message.MessageType.UnitData)) {
			// check last message
			UnitData currLastMsg = this.getLastMessage();
			if (currLastMsg==null || currLastMsg.getTime() < message.getTime()) {
				if (currLastMsg==null)
					setLastMessage((UnitData) message);
				else {
					setLastMessage((UnitData)message);
					//Todo: create copy of new message and add all properties that exists in previous message and not overridden in new one
				}
			}
			// check position
			if (((UnitData)message).getPosition()!=null) {
				UnitData.Position currPosition = getPosition();
				if (currPosition==null || currPosition.getTime() < message.getTime()) {
					((UnitData)message).getPosition().setTime(message.getTime());
					//var newPos = qx.lang.Object.clone(msg.pos);
					//newPos.t = msg.t;
					setPosition(((UnitData) message).getPosition());
				}
			}
		}
		// call base implementation
		super.handleMessage(message);
	}

	private void setLastMessage(UnitData message) {
		this.lmsg=message;
		fireEvent(events.changeLastMessage, this, null, message);
	}

	private void setPosition(UnitData.Position position) {
		this.pos=position;
		fireEvent(events.changePosition, this, null, position);
	}

	@Override
	public boolean updateItemData(String key, JsonElement data) {
		if (super.updateItemData(key, data))
			return true;
		else {
			if (key.equals("uid") && data.getAsString()!=null) {
				setUniqueId(data.getAsString());
			} else if (key.equals("hw") && data.getAsNumber()!=null) {
				setDeviceTypeId(data.getAsLong());
			} else if (key.equals("ph") && data.getAsString()!=null) {
				setPhoneNumber(data.getAsString());
			} else if (key.equals("ph2") && data.getAsString()!=null) {
				setPhoneNumber2(data.getAsString());
			} else if (key.equals("psw") && data.getAsString()!=null) {
				setAccessPassword(data.getAsString());
			} else if (key.equals("cmds") && data.isJsonArray()) {
				setCommands(Session.getInstance().getGson().fromJson(data, List.class));
			} else if (key.equals("pos") && data.isJsonObject()) {
				setPosition(Session.getInstance().getGson().fromJson(data, UnitData.Position.class));
			} else if (key.equals("lmsg") && data.isJsonObject()) {
				setLastMessage(Session.getInstance().getGson().fromJson(data, UnitData.class));
			} else if (key.equals("cfl") && data.getAsNumber()!=null) {
				setCalcFlags(data.getAsLong());
			} else if (key.equals("cnm") && data.getAsNumber()!=null) {
				setMileageCounter(data.getAsLong());
			} else if (key.equals("cneh") && data.getAsNumber()!=null) {
				setEngineHoursCounter(data.getAsLong());
			} else if (key.equals("cnkb") && data.getAsNumber()!=null) {
				setTrafficCounter(data.getAsLong());
			} else if (key.equals("prms") && data.isJsonObject()) {
				Map<String, Object> dataParams=new HashMap<String, Object>();
				dataParams=Session.getInstance().getGson().fromJson(data, dataParams.getClass());
				Map<String, Object> oldParams = getMessageParams();
				if (oldParams==null)
					oldParams = new HashMap<String, Object>();
				for (Map.Entry<String, Object> entry : dataParams.entrySet()){
					if (entry.getValue() instanceof Number){
						Object oldParam=oldParams.get(entry.getKey());
						if (oldParam!=null && oldParam instanceof Map)
							((Map) oldParam).put("at", ((Number) entry.getValue()).longValue());
					} else {
						oldParams.put(entry.getKey(), entry.getValue());
					}
				}
				setMessageParams(oldParams);
			} else
				return false;
			return true;
		}
	}

	/** Data flags constants */
	public static enum dataFlag {
		/** Unit restricted props*/
		restricted(0x00000100),
		/** Unit commands*/
		commands(0x00000200),
		/** Unit last message & position */
		lastMessage(0x00000400),
		/** Unit driver code */
		driverCode(0x00000800),
		/** Unit sensors data */
		sensors(0x00001000),
		/** Unit counters and calc flags */
		counters(0x00002000),
		/** Unit route control plugin */
		routeControl(0x00004000),
		/** Unit maintenance plugin */
		maintenance(0x00008000),
		/** Unit log */
		log(0x00010000),
		/** Unit settings used in report: trip detector, fuel consumption */
		reportSettings(0x00020000),
		/** Other unit settings not specified directly */
		other(0x00040000),
		/** Unit commands aliases */
		commandAliases(0x00080000),
		/** Message parameters */
		messageParams(0x00100000),
		/** Position */
		position(0x00400000);
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
		/** view & edit connectivity settings: HW, UID, Phone, Password, input-messages-filter */
		editDevice(0x100000),
		/** create, edit and delete sensors */
		editSensors(0x200000),
		/** edit counters methodic and values */
		editCounters(0x400000),
		/** delete messages */
		deleteMessages(0x800000),
		/** execute commands over unit */
		executeCommands(0x1000000),
		/** register various unit events & store counters & bind/unbind drivers & change unit status */
		registerEvents(0x2000000),
		/** view routes */
		viewRoutes(0x4000000),
		/** create, edit and delete routes */
		editRoutes(0x8000000),
		/** view service intervals (maintenance) */
		viewServiceIntervals(0x10000000),
		/** create, edit and delete service intervals (maintenance schedule) */
		editServiceIntervals(0x20000000),
		/** import messages */
		importMessages(0x40000000),
		/** export messages */
		exportMessages(0x80000000),
		/** view command aliases */
		viewCmdAliases(0x400000000L),
		/** edit command aliases */
		editCmdAliases(0x800000000L),
		/** view events */
		viewEvents(0x1000000000L),
		/** create, edit and delete events */
		editEvents(0x2000000000L),
		/** edit unit properties used in reports: trip detector, fuel consumption settings */
		editReportSettings(0x4000000000L),
		/** Include unit in various auto-processing systems like retranslation, jobs, notifications */
		monitorState(0x8000000000L);
		/** Flag value */
		private long value;

		private accessFlag (long value) {
			this.value=value;
		}

		public long getValue() {
			return value;
		}
	}
	/** Calculation flags constants */
	public static enum calcFlag {
		/** Flag for mileage calctype detection */
		mileageMask(0xF),
		/** Mileage: GPS based */
		mileageGps(0x0),
		/** Mileage: absolute mileage sensor based (mileage) */
		mileageAbsOdometer(0x1),
		/** Mileage: relative odometer sensor based (odometer) */
		mileageRelOdometer(0x2),
		/** Mileage: GPS based + IGN should be ON (engine operation) */
		mileageGpsIgn(0x3),
		/** Flag for engine hours calctype detection */
		engineHoursMask(0xF0),
		/** EngineHours: digital engine ignition sensor (engine operation) */
		engineHoursIgn(0x10),
		/** EngineHours: absolute engine hours sensor based (engine hours) */
		engineHoursAbs(0x20),
		/** EngineHours: relative engine hours sensor based (engine hours) */
		engineHoursRel(0x40),
		/** Auto-calculate mileage from new messages */
		mileageAuto(0x100),
		/** Auto-calculate engine hours from new messages */
		engineHoursAuto(0x200),
		/** Auto-calculate GPRS traffic bytes */
		trafficAuto(0x400);
		/** Flag value */
		private long value;

		private calcFlag (long value) {
			this.value=value;
		}

		public long getValue() {
			return value;
		}
	}
	/** Commands flags */
	public static enum execCmdFlag {
		/** Send unit command to a primary phone number */
		primaryPhone(0x01),
		/** Send unit command to a secondary phone number */
		secondaryPhone(0x02);
		/** Flag value */
		private long value;

		private execCmdFlag (long value) {
			this.value=value;
		}

		public long getValue() {
			return value;
		}
	}
	/** Log message action constants */
	public static enum logMessageAction{
		/** Unit specific actions */

		/** Unit created */
		unitCreated("create_unit"),
		/** Unit password updated */
		unitUpdatedPassword("update_unit_pass"),
		/** Unit phone updated */
		unitUpdatedPhone("update_unit_phone"),
		/** Secondary unit phone updated */
		unitUpdatedPhone2("update_unit_phone2"),
		/** Unit calculation flags updated */
		unitUpdatedCalcFlags("update_unit_calcflags"),
		/** Unit milage counter updated */
		unitChangeMilageCounter("update_unit_milcounter"),
		/** Unit byte (traffic) counter updated */
		unitChangeByteCounter("update_unit_bytecounter"),
		/** Unit engine hours counter updated */
		unitChangeEngineHoursCounter("update_unit_ehcounter"),
		/** Unit unique ID updated */
		unitUpdatedUniqueId("update_unit_uid"),
		/** Unit hardware type updated */
		unitUpdatedHwType("update_unit_hw"),
		/** Unit updated hardware config */
		unitUpdatedHwConfig("update_unit_hw_config"),
		/** Unit fuel consumption settings updated */
		unitUpdatedFuelConsumptionSettings("update_unit_fuel_cfg"),
		/** Unit trip detector settings updated */
		unitUpdatedTripDetectorSettings("update_unit_trip_cfg"),
		/** Unit sensor created */
		unitCreatedSensor("create_sensor"),
		/** Unit sensor updated */
		unitUpdatedSensor("update_sensor"),
		/** Unit sensor deleted */
		unitDeletedSensor("delete_sensor"),
		/** Unit command alias created */
		unitCreatedCommandAlias("create_alias"),
		/** Unit command alias updated */
		unitUpdatedCommandAlias("update_alias"),
		/** Unit command alias deleted */
		unitDeletedCommandAlias("delete_alias"),
		/** Unit service interval created */
		unitCreatedServiceInterval("create_service_interval"),
		/** Unit service interval updated */
		unitUpdatedServiceInterval("update_service_interval"),
		/** Unit service interval deleted */
		unitDeletedServiceInterval("delete_service_interval"),
		/** Unit settings imported */
		unitSettingsImported("import_unit_cfg"),
		/** Unit messages imported */
		unitMessagesImported("import_unit_msgs"),
		/** Unit message deleted */
		unitMessageDeleted("delete_unit_msg"),
		/** Unit messages deleted */
		unitMessagesDeleted("delete_unit_msgs"),
		/** Unit driver bound */
		unitDriverBinded("bind_unit_driver"),
		/** Unit driver unbound */
		unitDriverUnbinded("unbind_unit_driver"),
		/** Unit trailer bound */
		unitTrailerBinded("bind_unit_trailer"),
		/** Unit trailer unbound */
		unitTrailerUnbinded("unbind_unit_trailer"),
		/** Unit report settings updated */
		unitReportSettingsUpdated("update_unit_report_cfg"),
		/** Unit messages filter settings updated */
		unitMessagesFilterSettingsUpdated("update_msgs_filter_cfg");
		/** Flag value */
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
		/** Unique ID property has changed */
		changeUniqueId,
		/** Device type property has changed */
		changeDeviceTypeId,
		/** Phone number property has changed */
		changePhoneNumber,
		/** Secondary phone number property has changed */
		changePhoneNumber2,
		/** Access password property has changed */
		changeAccessPassword,
		/** Commands list property has changed */
		changeCommands,
		/** Last position property has changed */
		changePosition,
		/** Last message property has changed */
		changeLastMessage,
		/** Driver code property has changed
		changeDriverCode,*/
		/** Unit calculation flags property has changed */
		changeCalcFlags,
		/** Unit mileage counter property has changed */
		changeMileageCounter,
		/** Unit engine hours counter property has changed */
		changeEngineHoursCounter,
		/** Unit GPRS traffic counter property has changed */
		changeTrafficCounter,
		/** Message params has changed */
		changeMessageParams,
		updateSensor,
		updateCommandDefinition
	}
}
