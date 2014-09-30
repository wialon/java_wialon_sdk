package com.wialon.util;

import android.location.Location;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gurtam.gps_trace_orange.utils.UnitUtils;
import com.wialon.core.Session;
import com.wialon.item.User;
import com.wialon.remote.RemoteHttpClient;
import com.wialon.remote.handlers.ResponseHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Gis {

	/**
	 * Geocoding (getLocations) flags, SAG
	 */
	public static class GeocodingFlags {
		/**
		 * constants for address hierarchy levels for getLevelFlags function
		 */
		public static final int
				level_countries = 1,
				level_regions = 2,
				level_cities = 3,
				level_streets = 4,
				level_houses = 5;
	}


	/**
	 * Calculate flags for geocodingParams based on levels of hierarchy addresses
	 * f.e.:
	 * Gis.getLevelFlags(wialon.util.Gis.geocodingFlags.level_houses,
	 * Gis.geocodingFlags.level_streets, wialon.util.Gis.geocodingFlags.level_cities)
	 *
	 * @param l1 {Integer} - address element for level 1
	 * @param l2 {Integer} - address element for level 2
	 * @param l3 {Integer} - address element for level 3
	 * @param l4 {Integer} - address element for level 4
	 * @param l5 {Integer} - address element for level 5
	 */
	public static int getLevelFlags(int l1, int l2, int l3, int l4, int l5) {
		if (l1 < 1 || l1 > 5) // by default - set sequence to Street, House, City, Region, Country
			return 1255211008;
		int res = l1 << 28;
		if (l2 > 0 || l2 < 6)
			res += l2 << 25;
		if (l3 > 0 || l3 < 6)
			res += l3 << 22;
		if (l4 > 0 || l4 < 6)
			res += l4 << 19;
		if (l5 > 0 || l5 < 6)
			res += l5 << 16;
		return res;
	}

	/**
	 * Detect location for text for coordinates using GurtamMaps
	 *
	 * @param coords   {String} String of locations in form [{lat: Y, lon: X}]
	 * @param callback {Function?null} callback function that is called after remote call: callback(code, locations), locations is array of strings same count as source array
	 *                 all other parameters passed through in structure geocodingParams
	 */
	public static void getLocations(String coords, int levelFlags, ResponseHandler callback) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("coords", coords));
		nameValuePairs.add(new BasicNameValuePair("flags", String.valueOf(levelFlags)));
		nameValuePairs.add(new BasicNameValuePair("uid", String.valueOf(Session.getInstance().getCurrUser().getId())));
		RemoteHttpClient.getInstance().get(Session.getInstance().getBaseGisUrl(Session.GisType.GEOCODE) + "/gis_geocode", nameValuePairs, callback);
	}
}
