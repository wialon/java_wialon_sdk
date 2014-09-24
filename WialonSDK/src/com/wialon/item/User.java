package com.wialon.item;

import com.google.gson.JsonElement;
import com.wialon.remote.RemoteHttpClient;
import com.wialon.remote.handlers.ResponseHandler;

public class User extends Item {
 	private Long fl;
	private String hm;

	public User () {
		itemType=ItemType.user;
	}

	/**
	 * User flags
	 * @return get user flags
	 */
	public Long getUserFlags() {
		return fl;
	}

	/**
	 * Hosts mask, restrict list of IP addresses from which user can access Wialon, wildcards supported
	 * @return host mask
	 */
	public String getHostsMask() {
		return hm;
	}

	/**
	 * Get ACL for this users over all items
	 * @param directAccess return only items with direct access for this user
	 * @param itemSuperclass item superclass
	 * @param callback  callback that will receive information about user ACL
	 * result is object {itemId1: accessMask, itemId2: accessMask, ...}
	 */
	public void getItemsAccess( boolean directAccess, ItemType itemSuperclass, ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"user/get_items_access",
				"{\"userId\":"+getId()+",\"itemSuperclass\":\""+itemSuperclass+"\",\"directAccess\":"+(directAccess ? 1 : 0)+"}",
				callback);
	}

	/**
	 * Update this user access to some other, require ACL bit Item.accessFlag.setItemsAccess over user and Item.accessFlag.setAcl over item
	 * @param item  item which access need to be updated
	 * @param accessMask new item ACL
	 * @param callback callback that will receive information about access update: callback(code), zero code is success
	 */
	public void updateItemAccess(Item item, long accessMask, ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"user/update_item_access",
				"{\"userId\":"+getId()+",\"itemId\":"+item.getId()+",\"accessMask\":"+accessMask+"}",
				callback);
	}

	/**
	 * Update this user flags, require ACL bit User.accessFlag.editUserFlags over user
	 * @param flags flags which to set, see User.userFlag for possible values
	 * canSendSMS and canCreateItems flags require all creators of this user to have them enabled
	 * @param flagsMask mask of which flags to set to ON/OFF. If mask contain invalid bits, request would be denied
	 * @param callback callback that will receive information about flags update
	 */
	public void updateUserFlags(long flags, long flagsMask, ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"user/update_user_flags",
				"{\"userId\":"+getId()+",\"flags\":"+flags+",\"flagsMask\":"+flagsMask+"}",
				getOnUpdatePropertiesCallback(callback));
	}

	/**
	 * Update hosts mask, from which user can access Wialon, require ACL bit User.accessFlag.editUserFlags over user
	 * @param hostsMask new wildcard-base hosts mask, use empty value to disable this restriction
	 * @param callback callback that will receive information about hosts mask update
	 */
	public void updateHostsMask(String hostsMask, ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"user/update_hosts_mask",
				"{\"userId\":"+getId()+",\"hostsMask\":\""+hostsMask+"\"}",
				getOnUpdatePropertiesCallback(callback));
	}

	/**
	 * Get localization settings, remote call, require ACL bit wialon.item.Item.accessFlags.viewProperties over item
	 * @param callback callback that get result of server operation in form callback(code, result), where result represents JSON data for localization settings
	 */
	public void getLocale(ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"user/get_locale",
				"{\"userId\":"+getId()+"}",
				callback
		);
	}
	/** Update this user localization settings
	 * @param locale localization parameters, format: {fd: text, wd: ubyte}
	 * @param callback callback that will receive information about localization update
	 */
	public void updateLocale(String locale, ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"user/update_locale",
				"{\"userId\":" + getId() + ",\"locale\":" + locale + "}",
				callback);
	}

	/**
	 * Update password require ACL bit User.accessFlag.operateAs over user and correct oldPassword OR
	 * User.accessFlag.editUserFlags and empty oldPassword
	 * @param oldPassword old password if only User.accessFlag.operateAs acl bit available
	 * @param newPassword new password
	 * @param callback callback that will receive information about password update
	 */
	public void updatePassword(String oldPassword, String newPassword, ResponseHandler callback) {
		RemoteHttpClient.getInstance().remoteCall(
				"user/update_password",
				"{\"userId\":"+getId()+",\"oldPassword\":\""+oldPassword+"\",\"newPassword\":\""+newPassword+"\"}",
				callback);
	}

	@Override
	public boolean updateItemData(String key, JsonElement data) {
		if (super.updateItemData(key, data))
			return true;
		else {
			if (key.equals("hm")&& data.getAsString()!=null) {
				setHostsMask(data.getAsString());
			} else if (key.equals("fl") && data.getAsNumber()!=null) {
				setUserFlags(data.getAsLong());
			} else
				return false;
			return true;
		}
	}

	private void setUserFlags(Long userFlags) {
		if (this.fl==null || !this.fl.equals(userFlags)) {
			Long oldFlags=this.fl==null ? null : new Long(this.fl);
			this.fl = userFlags;
			fireEvent(events.changeUserFlags, oldFlags, userFlags);
		}
	}

	public void setHostsMask(String hostsMask) {
		if (this.hm==null || !this.hm.equals(hostsMask)) {
			String oldMask=this.hm==null ? null :new String(hostsMask);
			this.hm = hostsMask;
			fireEvent(events.changeHostsMask, oldMask, hostsMask);
		}
	}

	/** Data flags constants */
	public static enum dataFlag{
		/** User flags */
		flags(0x00000100),
		/** User notifications plugin */
		notifications(0x00000200),
		/** User connectivity settings, e.g. hosts mask*/
		connSettings(0x00000400);
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
	public static enum accessFlag{
		/** change users's ACL: provide to given user access to other items */
		setItemsAccess(0x100000),
		/** act as given user: create items, login as it, etc */
		operateAs(0x200000),
		/** change various flags for given user */
		editUserFlags(0x400000);
		/** Flag value */
		private long value;

		private accessFlag (long value) {
			this.value=value;
		}

		public long getValue() {
			return value;
		}
	}

	/**
	 * Fetch default current user flags
	 * @return default user data flags
	 */
	public static long defaultDataFlags() {
		return 	Item.dataFlag.base.getValue() | Item.dataFlag.customProps.getValue() | Item.dataFlag.billingProps.getValue() |
				User.dataFlag.flags.getValue();
	}

	/** User flags constants */
	public static enum userFlag{
		/** User is disabled */
		isDisabled(0x00000001),
		/** User can not change its password */
		cantChangePassword(0x00000002),
		/** User can create items */
		canCreateItems(0x00000004),
		/** User can't change own settings */
		isReadonly(0x00000010),
		/** User can send SMS messages */
		canSendSMS(0x00000020);
		/** Flag value */
		private long value;

		private userFlag (long value) {
			this.value=value;
		}

		public long getValue() {
			return value;
		}
	}
	/** Log message action constants */
	/** Also user log will contain messages for all other items which user affected. */
	/** Such actions starts with "user_" and looks like "user_update_name" or "user_create_poi". */
	public static enum logMessageAction{
		/** User specific actions */

		/** User created */
		userCreated("create_user"),
		/** User hosts mask updated */
		userUpdatedHostsMask("update_hosts_mask"),
		/** User password updated */
		userUpdatedPassword("update_user_pass"),
		/** User flags updated */
		userUpdatedFlags("update_user_flags"),
		/** User notification created */
		userCreatedNotification("create_user_notify"),
		/** User notification deleted */
		userDeletedNotification("delete_user_notify");
		/** Flag value */
		private String value;

		private logMessageAction (String value) {
			this.value=value;
		}

		public String getValue() {
			return value;
		}
	}

	public static enum events {
		/** userFlags property has changed */
		changeUserFlags,
		/** hostsMask property has changed */
		changeHostsMask
	}
}
