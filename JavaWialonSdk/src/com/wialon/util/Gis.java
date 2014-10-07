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

package com.wialon.util;

import com.wialon.core.Session;
import com.wialon.remote.RemoteHttpClient;
import com.wialon.remote.handlers.ResponseHandler;

import java.util.HashMap;
import java.util.Map;


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
	 * @param coords   String of locations in form [{lat: Y, lon: X}]
	 * @param callback callback function that is called after remote call: callback(code, locations), locations is array of strings same count as source array
	 *                 all other parameters passed through in structure geocodingParams
	 */
	public static void getLocations(String coords, int levelFlags, ResponseHandler callback) {
		Map<String, String> nameValuePairs = new HashMap<String, String>();
		nameValuePairs.put("coords", coords);
		nameValuePairs.put("flags", String.valueOf(levelFlags));
		nameValuePairs.put("uid", String.valueOf(Session.getInstance().getCurrUser().getId()));
		RemoteHttpClient.getInstance().get(Session.getInstance().getBaseGisUrl(Session.GisType.GEOCODE) + "/gis_geocode", nameValuePairs, callback);
	}
}
