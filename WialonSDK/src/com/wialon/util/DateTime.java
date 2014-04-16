package com.wialon.util;

import com.wialon.core.Session;
import com.wialon.item.User;

import java.util.*;

public class DateTime {
	/**
	 * Get current user timezone
	 *
	 * @return {Integer} timezone and dst value
	 */
	public static int getTimezone() {
		int tz = TimeZone.getDefault().getRawOffset()/1000;
		User user = Session.getInstance().getCurrUser();
		if (user == null)
			return tz;
		return Integer.valueOf(user.getCustomProperty("tz", String.valueOf(tz)));
	}

	/**
	 * Return timezone offset value, in seconds
	 *
	 * @return {Integer}timezone offset, in seconds
	 */
	public static int getTimezoneOffset() {
		int tz = getTimezone();
		// old format
//		if ((tz & this.__dstFlags.TZ_TYPE_MASK) != this.__dstFlags.TZ_TYPE_WITH_DST)
//			return tz & this.__dstFlags.TZ_OFFSET_MASK;
		// new DST format
		return ((tz & 0x80000000) != 0 ? ((tz & 0xFFFF) | 0xFFFF0000) : (tz & 0xFFFF));
	}

	public static SimpleTimeZone getSimpleTimeZone() {
		int timeZoneOffset = getTimezoneOffset();
		int timezone=getTimezone();
		if((timezone & DstFlags.TZ_TYPE_WITH_DST)==DstFlags.TZ_TYPE_WITH_DST) {
			DateTime.DaylightSavingsTime daylightSavingsTime=DateTime.getDSTSettings(null);
			return new SimpleTimeZone(DateTime.getTimezoneOffset()*1000,
					String.valueOf(timeZoneOffset),
					daylightSavingsTime.mStartMonth,
					daylightSavingsTime.mStartDay,
					daylightSavingsTime.mStartDayOfWeek,
					daylightSavingsTime.mStartTime,
					daylightSavingsTime.mStartTimeMode,
					daylightSavingsTime.mEndMonth,
					daylightSavingsTime.mEndDay,
					daylightSavingsTime.mEndDayOfWeek,
					daylightSavingsTime.mEndTime,
					daylightSavingsTime.mEndTimeMode,
					daylightSavingsTime.mDaylightSavings);
		} else
			return new SimpleTimeZone(DateTime.getTimezoneOffset()*1000, String.valueOf(timeZoneOffset));
	}

	public static DaylightSavingsTime getDSTSettings(Calendar calendar) {
		DaylightSavingsTime daylightSavingsTime = new DaylightSavingsTime();
		int dst = getTimezoneOffset() & DstFlags.TZ_CUSTOM_DST_MASK;
		// check different types of DST offsets
		switch (dst) {
			case DstFlags.DST_MAR2SUN2AM_NOV1SUN2AM:
				daylightSavingsTime.updateStartTime(2, 2, Calendar.SUNDAY, (2 * 3600 * 1000));
				daylightSavingsTime.updateEndTime(10, 1, Calendar.SUNDAY, (2 * 3600 * 1000));
				break;
			case DstFlags.DST_MAR6SUN_OCT6SUN:
				daylightSavingsTime.updateStartTime(2, -1, Calendar.SUNDAY);
				daylightSavingsTime.updateEndTime(9, -1, Calendar.SUNDAY);
				break;
			case DstFlags.DST_MAR6SUN1AM_OCT6SUN1AM:
				daylightSavingsTime.updateStartTime(2, -1, Calendar.SUNDAY, (1 * 3600 * 1000));
				daylightSavingsTime.updateEndTime(9, -1, Calendar.SUNDAY, (2 * 3600 * 1000));
				break;
			case DstFlags.DST_MAR6THU_SEP6FRI:
				daylightSavingsTime.updateStartTime(2, -1, Calendar.THURSDAY);
				daylightSavingsTime.updateEndTime(8, -1, Calendar.FRIDAY);
				break;
			case DstFlags.DST_MAR6SUN2AM_OCT6SUN2AM:
				if (calendar!=null && calendar.get(Calendar.YEAR)<=2011){
					daylightSavingsTime.updateStartTime(2, -1, Calendar.SUNDAY, (2 * 3600 * 1000));
					daylightSavingsTime.updateEndTime(9, -1, Calendar.SUNDAY, (2 * 3600 * 1000));
				}
				break;
			case DstFlags.DST_MAR30_SEP21:
				daylightSavingsTime.updateStartTime(2, 30, 0);
				daylightSavingsTime.updateEndTime(8, 21, 0);
				daylightSavingsTime.mStartTimeMode=daylightSavingsTime.mEndTimeMode=SimpleTimeZone.STANDARD_TIME;
				break;
			case DstFlags.DST_APR1SUN2AM_OCT6SUN2AM:
				daylightSavingsTime.updateStartTime(3, 1, Calendar.SUNDAY, (2 * 3600 * 1000));
				daylightSavingsTime.updateEndTime(9, -1, Calendar.SUNDAY, (2 * 3600 * 1000));
				break;
			case DstFlags.DST_APR1_OCT6SUN:
				daylightSavingsTime.updateStartTime(3, 1, 0);
				daylightSavingsTime.updateEndTime(9, 6, 0);
				daylightSavingsTime.mStartTimeMode=daylightSavingsTime.mEndTimeMode=SimpleTimeZone.STANDARD_TIME;
				break;
			case DstFlags.DST_APR6THU_SEP6THU:
				daylightSavingsTime.updateStartTime(3, -1, Calendar.THURSDAY);
				daylightSavingsTime.updateEndTime(8, -1, Calendar.THURSDAY);
				break;
			case DstFlags.DST_APR1_OCT1:
				daylightSavingsTime.updateStartTime(3, 1, 0);
				daylightSavingsTime.updateEndTime(9, 1, 0);
				daylightSavingsTime.mStartTimeMode=daylightSavingsTime.mEndTimeMode=SimpleTimeZone.STANDARD_TIME;
				break;
			case DstFlags.DST_MAR21_22SUN_SEP21_22SUN:
				if (calendar!=null && calendar instanceof GregorianCalendar && ((GregorianCalendar) calendar).isLeapYear(calendar.get(Calendar.YEAR))) {
					daylightSavingsTime.updateStartTime(2, 21, 0);
					daylightSavingsTime.updateEndTime(8, 21, 0);
				} else {
					daylightSavingsTime.updateStartTime(2, 22, 0);
					daylightSavingsTime.updateEndTime(8, 22, 0);
				}
				daylightSavingsTime.mStartTimeMode=daylightSavingsTime.mEndTimeMode=SimpleTimeZone.STANDARD_TIME;
				break;
			case DstFlags.DST_SEP1SUNAFTER7_APR1SUNAFTER5:
				daylightSavingsTime.updateStartTime(8, 7, -Calendar.SUNDAY);
				daylightSavingsTime.updateEndTime(3, 5, -Calendar.SUNDAY);
				daylightSavingsTime.mStartTimeMode=daylightSavingsTime.mEndTimeMode=3;
				break;
			case DstFlags.DST_SEP1SUN_APR1SUN:
				daylightSavingsTime.updateStartTime(8, 1, Calendar.SUNDAY);
				daylightSavingsTime.updateEndTime(3, 1, Calendar.SUNDAY);
				break;
			case DstFlags.DST_SEP6SUN_APR1SUN:
				daylightSavingsTime.updateStartTime(8, -1, Calendar.SUNDAY);
				daylightSavingsTime.updateEndTime(3, 1, Calendar.SUNDAY);
				break;
			case DstFlags.DST_OCT2SUN_MAR2SUN:
				daylightSavingsTime.updateStartTime(9, 2, Calendar.SUNDAY);
				daylightSavingsTime.updateEndTime(2, 2, Calendar.SUNDAY);
				break;
			case DstFlags.DST_OCT1SUN_FEB3SUN:
				daylightSavingsTime.updateStartTime(9, 1, Calendar.SUNDAY);
				daylightSavingsTime.updateEndTime(1, 3, Calendar.SUNDAY);
				break;
			case DstFlags.DST_OCT3SUN_MAR2SUN:
				daylightSavingsTime.updateStartTime(9, 3, Calendar.SUNDAY);
				daylightSavingsTime.updateEndTime(2, 2, Calendar.SUNDAY);
				break;
			case DstFlags.DST_OCT1SUN_MAR2SUN:
				daylightSavingsTime.updateStartTime(9, 1, Calendar.SUNDAY);
				daylightSavingsTime.updateEndTime(2, 2, Calendar.SUNDAY);
				break;
			case DstFlags.DST_OCT1SUN_APR1SUN:
				daylightSavingsTime.updateStartTime(9, 1, Calendar.SUNDAY);
				daylightSavingsTime.updateEndTime(3, 1, Calendar.SUNDAY);
				break;
			case DstFlags.DST_OCT1SUN_MAR6SUN:
				daylightSavingsTime.updateStartTime(9, 1, Calendar.SUNDAY);
				daylightSavingsTime.updateEndTime(2, -1, Calendar.SUNDAY);
				break;
		}
		if ((getTimezoneOffset()&DstFlags.TZ_DST_TYPE_MASK)==DstFlags.TZ_DST_TYPE_CUSTOM_UTC)
			daylightSavingsTime.mStartTimeMode=daylightSavingsTime.mEndTimeMode=SimpleTimeZone.UTC_TIME;
		return daylightSavingsTime;
	}

	/**
	 * Convert absolute time to user time - used in format and print methods
	 * @param absVal absolute time in seconds
	 * @return user time
	 */
	public static Calendar userTime(long absVal) {
		int timeZoneOffset = getTimezoneOffset();
		int timezone=getTimezone();
		Calendar calendar=Calendar.getInstance(new SimpleTimeZone(DateTime.getTimezoneOffset() * 1000, String.valueOf(timeZoneOffset)));
		calendar.setTimeInMillis(absVal*1000);
		if((timezone & DstFlags.TZ_TYPE_WITH_DST)==DstFlags.TZ_TYPE_WITH_DST) {
			DateTime.DaylightSavingsTime daylightSavingsTime = DateTime.getDSTSettings(calendar);
			calendar.setTimeZone(new SimpleTimeZone(DateTime.getTimezoneOffset() * 1000,
					String.valueOf(timeZoneOffset),
					daylightSavingsTime.mStartMonth,
					daylightSavingsTime.mStartDay,
					daylightSavingsTime.mStartDayOfWeek,
					daylightSavingsTime.mStartTime,
					daylightSavingsTime.mStartTimeMode,
					daylightSavingsTime.mEndMonth,
					daylightSavingsTime.mEndDay,
					daylightSavingsTime.mEndDayOfWeek,
					daylightSavingsTime.mEndTime,
					daylightSavingsTime.mEndTimeMode,
					daylightSavingsTime.mDaylightSavings));
			calendar.getTime();
		}
		return calendar;
	}

	private class DstFlags {
//CONSTANTS
		// Custom DST setting mask
		private static final int TZ_CUSTOM_DST_MASK = 0x00FF0000,
		// Timezone DST mask
		TZ_DST_TYPE_MASK=0x03000000,
		// Use custom DST setting(UTC time)
		TZ_DST_TYPE_CUSTOM_UTC=0x03000000,
		// Timezone has an information about its DST
		TZ_TYPE_WITH_DST=0x08000000,
//RULES
		// Northern hemisphere
		// From second march sunday to first november sunday
		DST_MAR2SUN2AM_NOV1SUN2AM = 0x00010000,
		// From last march sunday to last october sunday
		DST_MAR6SUN_OCT6SUN = 0x00020000,
		// From last march sunday at 1am to last october sunday at 1am
		DST_MAR6SUN1AM_OCT6SUN1AM = 0x00030000,
		// From last march thursday to last september friday
		DST_MAR6THU_SEP6FRI = 0x00040000,
		// From last march sunday at 2am to last october sunday at 2am
		DST_MAR6SUN2AM_OCT6SUN2AM = 0x00050000,
		// From march 30 to september 21
		DST_MAR30_SEP21 = 0x00060000,
		// From first april sunday to last october sunday
		DST_APR1SUN2AM_OCT6SUN2AM = 0x00070000,
		// From first april to last october sunday
		DST_APR1_OCT6SUN = 0x00080000,
		// From last april thursday to last september thursday
		DST_APR6THU_SEP6THU = 0x00090000,
		// From last april friday(before april 2nd) to UNKNOWN
		DST_APR6THU_UNKNOWN = 0x000A0000,
		// From april 1st to october 1st
		DST_APR1_OCT1 = 0x000B0000,
		// From  march 22th to september 21th (21 march to 20 september from leap year)
		DST_MAR21_22SUN_SEP21_22SUN = 0x000C0000,
		// Used to distinguish DST`s
		DST_SOUTHERN_SEMISPHERE = 0x00200000,
		// Southern hemisphere DST
		// From first september sunday(after september 7th) to first april sunday(after april 5th)
		DST_SEP1SUNAFTER7_APR1SUNAFTER5 = 0x00200000,
		// From first september sunday to first april sunday
		DST_SEP1SUN_APR1SUN = 0x00210000,
		// From september last sunday to april first sunday
		DST_SEP6SUN_APR1SUN = 0x00220000,
		// From second october sunday to second march sunday
		DST_OCT2SUN_MAR2SUN = 0x00230000,
		// From first october sunday to thrid february sunday
		DST_OCT1SUN_FEB3SUN = 0x00240000,
		// From third october sunday to second march sunday
		DST_OCT3SUN_MAR2SUN = 0x00250000,
		// From first october sunday to second march sunday
		DST_OCT1SUN_MAR2SUN = 0x00260000,
		// From october first sunday to april first sunday
		DST_OCT1SUN_APR1SUN = 0x00270000,
		// From october first sunday to march last sunday
		DST_OCT1SUN_MAR6SUN = 0x00280000;
	}

	public static class DaylightSavingsTime {
		public int mStartMonth, mStartDay, mStartDayOfWeek, mStartTime, mStartTimeMode=SimpleTimeZone.WALL_TIME,
				mEndMonth, mEndDay, mEndDayOfWeek, mEndTime, mEndTimeMode=SimpleTimeZone.WALL_TIME, mDaylightSavings = 3600 * 1000;

		public void updateStartTime(int startMonth, int startDay, int startDayOfWeek){
			mStartMonth=startMonth;
			mStartDay=startDay;
			mStartDayOfWeek=startDayOfWeek;
		}

		public void updateStartTime(int startMonth, int startDay, int startDayOfWeek, int startTime){
			updateStartTime(startMonth, startDay, startDayOfWeek);
			mStartTime=startTime;
		}

		public void updateEndTime(int endMonth, int endDay, int endDayOfWeek){
			mEndMonth=endMonth;
			mEndDay=endDay;
			mEndDayOfWeek=endDayOfWeek;
		}

		public void updateEndTime(int endMonth, int endDay, int endDayOfWeek, int endTime){
			updateEndTime(endMonth, endDay, endDayOfWeek);
			mEndTime=endTime;
		}
	}
}
