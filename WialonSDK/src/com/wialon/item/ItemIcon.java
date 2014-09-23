package com.wialon.item;

import com.google.gson.JsonElement;
import com.wialon.core.Session;
import com.wialon.remote.handlers.BinaryResponseHandler;
import com.wialon.remote.RemoteHttpClient;
import com.wialon.remote.handlers.ResponseHandler;

import java.io.File;
import java.io.InputStream;

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


    public void updateIcon(File file, ResponseHandler callback) {
        RemoteHttpClient.getInstance().post("unit/upload_image", file, "{\"itemId\":" + getId() + "}", callback);
    }


	/** Events */
	public static enum events {
		/** icon property has changed */
		changeIcon
	}
}
