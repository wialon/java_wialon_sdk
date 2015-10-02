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

import com.wialon.item.prop.ItemPropertiesData;
import com.wialon.item.prop.Report;

import java.util.Map;

public class Resource extends Item {

	public Resource() {
		itemType=ItemType.avl_resource;
	}
	private Map<String, String> rep;
	private Map<String, String> unf;
	//Plugins
	private Report reportPlugin;
	private ItemPropertiesData notificationPlugin;

	public Report getReportPlugin(){
		return reportPlugin==null ? reportPlugin=new Report(rep, "rep", this, "report/update_report", "report/get_report_data") : reportPlugin;
	}

	public ItemPropertiesData getNotificationPlugin(){
		return notificationPlugin==null ? notificationPlugin=new ItemPropertiesData(unf, "unf", this, events.updateNotification, "resource/update_notification", "resource/get_notification_data") : notificationPlugin;
	}

	/** Data flags constants */
	public static enum dataFlag{
		/** Drivers plugin */
		drivers(0x00000100),
		/** Jobs plugin */
		jobs(0x00000200),
		/** Notifications plugin */
		notifications(0x00000400),
		/** POI plugin */
		poi(0x00000800),
		/** Geofences plugin */
		zones(0x00001000),
		/** Reports plugin */
		reports(0x00002000),
		/** Agro plugins */
		agro(0x01000000),
		/** Driver units */
		driverUnits(0x00004000),
		/** Driver groups plugin */
		driverGroups(0x00008000),
		/** Trailers plugin */
		trailers(0x00010000),
		/** Trailer groups plugin */
		trailerGroups(0x00020000),
		/** Trailer units */
		trailerUnits(0x00040000);
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
		/** view notifications */
		viewNotifications(0x100000),
		/** create, edit and delete notifications */
		editNotifications(0x200000),
		/** view POI */
		viewPoi(0x400000),
		/** edit POI */
		editPoi(0x800000),
		/** view geozones */
		viewZones(0x1000000),
		/** edit geozones */
		editZones(0x2000000),
		/** view jobs */
		viewJobs(0x4000000),
		/** edit jobs */
		editJobs(0x8000000),
		/** view reports */
		viewReports(0x10000000),
		/** edit reports */
		editReports(0x20000000),
		/** view drivers */
		viewDrivers(0x40000000),
		/** edit drivers */
		editDrivers(0x80000000),
		/** performat account-related operations */
		manageAccount(0x100000000L),
		/** agro: register & delete cultivations */
		agroEditCultivations(0x10000000000L),
		/** view agro staff */
		agroView(0x20000000000L),
		/** edit agro staff */
		agroEdit(0x40000000000L),
		/** view driver groups */
		viewDriverGroups(0x200000000L),
		/** edit driver groups */
		editDriverGroups(0x400000000L),
		/** view driver units */
		viewDriverUnits(0x800000000L),
		/** edit driver units */
		editDriverUnits(0x1000000000L),
		/** view trailers */
		viewTrailers(0x100000000000L),
		/** edit trailers*/
		editTrailers(0x200000000000L),
		/** view trailer groups */
		viewTrailerGroups(0x400000000000L),
		/** edit trailer groups */
		editTrailerGroups(0x800000000000L),
		/** view trailer units */
		viewTrailerUnits(0x1000000000000L),
		/** edit trailer units */
		editTrailerUnits(0x2000000000000L);
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
	public static enum logMessageAction {
		/** Resource specific actions */

		/** Resource created */
		resourceCreated("create_resource"),

		/** Geofence created */
		resourceCreatedZone("create_zone"),
		/** Geofence updated */
		resourceUpdatedZone("update_zone"),
		/** Geofence deleted */
		resourceDeletedZone("delete_zone"),

		/** POI created */
		resourceCreatedPoi("create_poi"),
		/** POI updated */
		resourceUpdatedPoi("update_poi"),
		/** POI deleted */
		resourceDeletedPoi("delete_poi"),

		/** Job created */
		resourceCreatedJob("create_job"),
		/** Job enabled/disabled */
		resourceSwitchedJob("switch_job"),
		/** Job updated */
		resourceUpdatedJob("update_job"),
		/** Job deleted */
		resourceDeletedJob("delete_job"),

		/** Notification created */
		resourceCreatedNotification("create_notify"),
		/** Notification enabled/disabled */
		resourceSwitchedNotification("switch_notify"),
		/** Notification updated */
		resourceUpdatedNotification("update_notify"),
		/** Notification deleted */
		resourceDeletedNotification("delete_notify"),

		/** Driver created */
		resourceCreatedDriver("create_driver"),
		/** Driver updated */
		resourceUpdatedDriver("update_driver"),
		/** Driver deleted */
		resourceDeletedDriver("delete_driver"),

		/** Drivers group created */
		resourceCreatedDriversGroup("create_drivers_group"),
		/** Drivers group  updated */
		resourceUpdatedDriversGroup("update_drivers_group"),
		/** Drivers group deleted */
		resourceDeletedDriversGroup("delete_drivers_group"),
		/** Drivers updated driver units */
		resourceUpdatedDriverUnits("update_driver_units"),

		/** Report template created */
		resourceCreatedReport("create_report"),
		/** Report template updated */
		resourceUpdatedReport("update_report"),
		/** Report template deleted */
		resourceDeletedReport("delete_report"),

		/** Resource POIs imported */
		resourceImportedPois("import_pois"),
		/** Resource geofences imported */
		resourceImportedZones("import_zones");
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
		/** name property has changed */
		updateReport,
		updateNotification
	}
}
