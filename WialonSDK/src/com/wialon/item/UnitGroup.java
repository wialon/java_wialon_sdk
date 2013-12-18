package com.wialon.item;

import com.wialon.core.Session;
import com.wialon.remote.RemoteHttpClient;
import com.wialon.remote.handlers.ResponseHandler;
import com.google.gson.JsonElement;

public class UnitGroup extends ItemIcon {
	private Long[] u;

	public UnitGroup(){
		this.itemType=ItemType.avl_unit_group;
	}

	/**
	 * Units (ID array)
	 * @return units id array
	 */
	public Long[] getUnits() {
		return u;
	}

	/**
	 * Update units in group, require ACL bit wialon.item.Item.accessFlag.editSubItems over group
	 * @param units {Array} collection of unit identifiers in form [id1, id2, ...] for group
	 * @param callback {?Function} callback that will receive information about update: callback(code), zero code is success
	 */
	public void updateUnits(Long[] units, ResponseHandler callback) {
		String unitsJson=Session.getInstance().getGson().toJson(units);
		RemoteHttpClient.getInstance().remoteCall(
				"unit_group/update_units",
				"{\"itemId\":"+getId()+",\"units\":"+unitsJson+"}",
				getOnUpdatePropertiesCallback(callback));
	}

	/**
	 * Check unit in group
	 * @param group group
	 * @param unit unit
	 * @return true - if unit in the group
	 */
	public static boolean checkUnit(UnitGroup group, Unit unit) {
		if (group==null || unit==null)
			return false;
		Long[] ids=group.getUnits();
		Long id = unit.getId();
		for (Long unitId : ids)
			if (unitId.equals(id))
				return true;
		return false;
	}

	@Override
	public boolean updateItemData(String key, JsonElement data) {
		if (super.updateItemData(key, data))
			return true;
		else {
			if (key.equals("u")&& data.getAsJsonArray()!=null) {
				setUnits((Long[])Session.getInstance().getGson().fromJson(data.toString(), Long[].class));
			} else
				return false;
			return true;
		}
	}

	private void setUnits(Long[] units) {
		if (this.u==null || !this.u.equals(units)) {
			Long[] oldUnits=this.u==null? null : u.clone();
			this.u = units;
			fireEvent(events.changeUnits, oldUnits, units);
		}
	}

	/** Log message action constants */
	public static enum logMessageAction{
		/** Unit group specific actions */

		/** Unit group created */
		unitGroupCreated("create_group"),
		/** Unit group units updated */
		unitGroupUnitsUpdated("units_group");
		/** Flag value */
		private String value;

		private logMessageAction (String value) {
			this.value=value;
		}

		public String getValue() {
			return value;
		}
	}

	public static enum events{
		/** Units property has changed */
		changeUnits
	}
}
