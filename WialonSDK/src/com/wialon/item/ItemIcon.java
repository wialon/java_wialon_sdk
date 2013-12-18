package com.wialon.item;

import com.google.gson.JsonElement;
import com.wialon.core.Session;
import com.wialon.remote.handlers.BinaryResponseHandler;
import com.wialon.remote.RemoteHttpClient;

/**
 * Contain functionality for item image manipulation and retrieval.
 */
public class ItemIcon extends Item {
	private Integer ugi;

	private void setUgi(Integer ugi) {
		if (this.ugi==null || !this.ugi.equals(ugi)) {
			Integer oldUgi=this.ugi==null ? null : new Integer(this.ugi);
			this.ugi = ugi;
			fireEvent(events.changeIcon, oldUgi, ugi);
		}
	}
	/**
	 * Get icon Url
	 * @param borderSize border size in pixels, if not specified - 32
	 * @return URL to item icon of specified size (PNG)
	 */
	public String getIconUrl(int borderSize) {
		if (borderSize<=0)
			borderSize = 32;
		return Session.getInstance().getBaseUrl() + "/avl_item_image/" + this.getId() + "/" + borderSize + "/" + ugi + ".png";
	}

	public void downloadIcon(BinaryResponseHandler callback) {
		RemoteHttpClient.getInstance().post(getIconUrl(0), null, callback);
	}

	@Override
	public boolean updateItemData(String key, JsonElement data) {
		if (super.updateItemData(key, data))
			return true;
		else {
			if (key.equals("ugi")&& data.getAsNumber()!=null)
				setUgi(data.getAsInt());
			else
				return false;
			return true;
		}
	}
//Todo
//	/**
//	 * Update icon
//	 * @param fileElement {Object} DOM FileUpload element
//	 * @param callback {?Function} function to call with result of upload call: callback(code). code is zero if no errors.
//	 * @return {Boolean} upload initialization state
//	 */
//	public void updateIcon(fileElement, callback) {
//		if (typeof fileElement == "string")
//		return wialon.core.Uploader.getInstance().uploadFiles([], "unit/upload_image", {fileUrl: fileElement, itemId: this.getId()}, callback, true);
//		else if (typeof fileElement == "number")
//		return wialon.core.Remote.getInstance().remoteCall("unit/update_image", {itemId: this.getId(), oldItemId: fileElement}, callback);
//		return wialon.core.Uploader.getInstance().uploadFiles([fileElement], "unit/upload_image", {itemId: this.getId()}, callback, true);
//	}
	/** Events */
	public static enum events {
		/** icon property has changed */
		changeIcon
	}
}
