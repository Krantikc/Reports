package com.reports.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.reports.dao.cAttendanceDetailsDaoImpl;
import com.reports.exceptions.InvalidAttendanceConfiguration;
import com.reports.model.AttendanceDetails;
import com.reports.model.CorrectionEntry;
import com.reports.model.DateRange;
import com.reports.model.Leave;
import com.reports.model.Month;
import com.reports.model.OfficeDetails;
import com.reports.model.User;
import com.reports.model.UserInOutPair;
import com.reports.model.UserSwipe;
import com.reports.model.Week;
import com.reports.util.JDBCConnectionUtil;
import com.reports.webservices.cAttendanceWebservice;

/**
 * 
 * @author kcr
 * Services that manipulates attendance data and return back the final data to the webservices
 * as required by client
 *
 */
public class cAttendanceDetailsService {
	static Connection connection = JDBCConnectionUtil.JDBCConnection;

	static cAttendanceDetailsDaoImpl aAttendanceDetailsDao = new cAttendanceDetailsDaoImpl();
	//static OfficeDetails aOfficeDetails = new cAttendanceWebservice().mGetOfficeDetails();
	
	static final String LEAVES_ID = "leaves";
	static final String ONDUTY_ID = "onDuty";
	static final String TOUR_ID = "tour";
	static final String PRESENT_ID = "present";
	static final String OFFICIAL_IN_ID = "officialIn";
	static final String SHORTLEAVE_IN_ID = "shortLeaveIn";
	static final String SHORTLEAVE_OUT_ID = "shortLeaveOut";
	static final String OFFICIAL_OUT_ID = "officialOut";
	
	static final String OFFICIAL_IN_CORRECTION_ID = "1";
	static final String OFFICIAL_OUT_CORRECTION_ID = "2";
	static final String SHORTLEAVE_IN_CORRECTION_ID = "3";
	static final String SHORTLEAVE_OUT_CORRECTION_ID = "4";
	
	
	static final String FREQ_YEARLY = "yearly";
	static final String FREQ_MONTHLY = "monthly";
	static final String FREQ_WEEKLY = "weekly";
	static final String FREQ_DAILY = "daily";
	
	static final String[] LEAVE_IDS = new String[] {"CL", "PL", "AL", "CO", "ML", "RH", "UP"};
	
	static final String GENERAL_SHIFT_ID = "GS";
	static final String US_SHIFT_ID = "US";
	
	//static final String[] DOOR_IDS = new String[] {"1", "7"};
	
/*	static OfficeDetails aOfficeDetails = mGetOfficeDetails();
	static double MIN_FULL_DAY_WORKING_HOURS = aOfficeDetails.getWorkingHours();
	static CalendarpOfficeDetails.getShiftStart() = mFormatTime(aOfficeDetails.getOfficeInTime());
	static Calendar aOfficeDetails.getOfficeOutTime() = mFormatTime(aOfficeDetails.getOfficeOutTime());
	static Calendar  cAttendanceWebservice.LUNCH_BEGIN = mFormatTime( cAttendanceWebservice.LUNCH_BEGIN);
	static Calendar LUNCH_END = mFormatTime(aOfficeDetails.getLunchEnd());
	static String[] aOfficeDetails.getMasterControllers() = aOfficeDetails.getMasterControllers();*/
	public static Calendar mFormatTime(String pTime) {
		int lHours = Integer.parseInt(pTime.split(":")[0]);
		int lMinutes = Integer.parseInt(pTime.split(":")[1]);
		Calendar lCal = Calendar.getInstance();
		lCal.set(Calendar.HOUR_OF_DAY, lHours);
		lCal.set(Calendar.MINUTE, lMinutes);
		return lCal;	
	}

	/**
	 * Gets Holidays Count for supplied date reange and location
	 * @param 	pLocationId		int		location id for which holidays count 
	 * @param 	pFromDate		Date	from date
	 * @param 	pToDate			Date	to date
	 * @return 	count 			int 	returns holidays count
	 */
	public static int mGetHolidaysCount(int pLocationId, Date pFromDate, Date pToDate, String pFrequency) {
		
		return 0;
	}
	
	
	public static double mGetTimeFromString(String pTime) {
		int lHours = Integer.parseInt(pTime.split(":")[0]);
		int lMinutes = Integer.parseInt(pTime.split(":")[1]);
		
		double lMilliseconds = lHours * (1000*60*60);
		lMilliseconds = lMilliseconds + (lMinutes*(1000*60));
		
		double lFinalHours = lMilliseconds/(1000*60*60);
		
		return lFinalHours;	
	}
	
	private static void mCheckValidTimeFormat(String pTimeStr) throws Exception{
		if (pTimeStr.split(":").length != 2) {
			throw new InvalidAttendanceConfiguration("Invalid Time Format. Allowed format is HH:mm ");
		}
		
	}
	
	private static OfficeDetails mSetOfficeDetails(JsonObject pAttendanceDetailsJson) throws Exception{
		String[] lDoorsToConsider = new String[0];
		int lLocationId = pAttendanceDetailsJson.get("locationId").getAsInt();
		String lMinFullDayHrs = pAttendanceDetailsJson.get("requiredHoursForFullDay").getAsString();
		String lMinHalfDayHrs = pAttendanceDetailsJson.get("requiredHoursForHalfDay").getAsString();
		String lShiftStart = pAttendanceDetailsJson.get("shiftStart").getAsString();
		String lShiftEnd = pAttendanceDetailsJson.get("shiftEnd").getAsString();
		String lBreakStart = pAttendanceDetailsJson.get("breakStart").getAsString();
		String lBreakEnd = pAttendanceDetailsJson.get("breakEnd").getAsString();
		String lDoorsToConsiderStr =  pAttendanceDetailsJson.get("doorsToConsider").getAsString();
		
		mCheckValidTimeFormat(lMinFullDayHrs);
		mCheckValidTimeFormat(lMinHalfDayHrs);
		mCheckValidTimeFormat(lShiftStart);
		mCheckValidTimeFormat(lShiftEnd);
		mCheckValidTimeFormat(lBreakStart);
		mCheckValidTimeFormat(lBreakEnd);
		
		String[] lMinFullDayHrsArr = lMinFullDayHrs.split(":");
		int lMinFullDayHrsInt = Integer.parseInt(lMinFullDayHrsArr[0]);
		int lMinFullDayMinsInt = Integer.parseInt(lMinFullDayHrsArr[1]);
		if ((lMinFullDayHrsInt*60 + lMinFullDayMinsInt) <= 0) {
			throw new InvalidAttendanceConfiguration("Minimum Working Hours for Full Day cannot be zero or negative");
		}
		
		String[] lMinHalfDayHrsArr = lMinHalfDayHrs.split(":");
		int lMinHalfDayHrsInt = Integer.parseInt(lMinHalfDayHrsArr[0]);
		int lMinHalfDayMinsInt = Integer.parseInt(lMinHalfDayHrsArr[1]);
		
		
		
		if ((lMinHalfDayHrsInt*60 + lMinHalfDayMinsInt) <= 0) {
			throw new InvalidAttendanceConfiguration("Minimum Working Hours for Half Day cannot be zero or negative");
		} else if ((lMinHalfDayHrsInt*60 + lMinHalfDayMinsInt) > (lMinFullDayHrsInt*60 + lMinFullDayMinsInt)) {
			throw new InvalidAttendanceConfiguration("Minimum Working Hours for Half Day cannot be greater than that of Full Day");
		}
		
		if ((lMinFullDayHrsInt*60 + lMinFullDayMinsInt) > (24 * 60) || (lMinHalfDayHrsInt*60 + lMinHalfDayMinsInt)  > (24 * 60)) {
			throw new InvalidAttendanceConfiguration("Minimum Working Hours Cannot be greater 24 hrs");
		}
		
		
		
		double lMinFullDayWorkingHours = mGetTimeFromString(lMinFullDayHrs);
		double lMinHalfDayWorkingHours = mGetTimeFromString(lMinHalfDayHrs);
		Calendar lShiftStartCal = mFormatTime(lShiftStart);
		Calendar lShiftEndCal = mFormatTime(lShiftEnd);
		Calendar lBreakStartCal = mFormatTime(lBreakStart);
		Calendar lBreakEndCal = mFormatTime(lBreakEnd);
		
		if (lShiftStartCal.getTimeInMillis() > lShiftEndCal.getTimeInMillis()) {
			throw new InvalidAttendanceConfiguration("Shift Start Time Cannot be greater than Shift End Time");
		}
		
		if (lBreakStartCal.getTimeInMillis() > lBreakEndCal.getTimeInMillis()) {
			throw new InvalidAttendanceConfiguration("Break Start Time Cannot be greater than Break End Time");
		}
		
		if (!lDoorsToConsiderStr.trim().equals("")) {
			if (lDoorsToConsiderStr.contains(" ")) {
				lDoorsToConsiderStr = lDoorsToConsiderStr.replaceAll(" ", "");
			}
			
			lDoorsToConsider =lDoorsToConsiderStr.split(",");
		}
		String[] lDoorsToConsiderArr =  lDoorsToConsider;
		
		OfficeDetails lOfficeDetails = new OfficeDetails(lShiftStartCal, lShiftEndCal, lBreakStartCal, lBreakEndCal, lMinFullDayWorkingHours, lMinHalfDayWorkingHours, lDoorsToConsiderArr);
		lOfficeDetails.setLocationId(lLocationId);
		return lOfficeDetails;
				
	}
	public static OfficeDetails mGetOfficeDetailsByLocation(int pLocationId, JsonArray pOfficeDetailsList) throws Exception {
		OfficeDetails lOfficeDetails = null;
		for (JsonElement lOfficeDetailsElement : pOfficeDetailsList) {
			
			JsonObject lOfficeDetailsObj = lOfficeDetailsElement.getAsJsonObject();
			lOfficeDetails = mSetOfficeDetails(lOfficeDetailsObj);
			if (lOfficeDetailsObj.get("locationId").getAsInt() == pLocationId) {
				return lOfficeDetails;
			}
		}
		return lOfficeDetails;
	}
	
	public static List<OfficeDetails> mGetOfficeDetails(JsonArray pOfficeDetailsList) throws Exception {
		List<OfficeDetails> lOfficeDetailsList = new ArrayList<>();
		for (JsonElement lOfficeDetailsElement : pOfficeDetailsList) {
			
			JsonObject lOfficeDetailsObj = lOfficeDetailsElement.getAsJsonObject();
			OfficeDetails lOfficeDetails = mSetOfficeDetails(lOfficeDetailsObj);
			lOfficeDetailsList.add(lOfficeDetails);
		}
		return lOfficeDetailsList;
	}
	public static Month mGetMonthDetails(int pYear, int pMonth, Date pFromDate, Date pToDate, boolean includeSaturday, boolean includeSunday) {
		Calendar lCalendar = Calendar.getInstance();
		lCalendar.setTime(pFromDate);
		boolean lIsFromMonth = (lCalendar.get(Calendar.MONTH) == pMonth && lCalendar.get(Calendar.YEAR) == pYear)?true:false;
		lCalendar.setTime(pToDate);
		boolean isToMonth = (lCalendar.get(Calendar.MONTH) == pMonth && lCalendar.get(Calendar.YEAR) == pYear)?true:false;
		int daysInMonth = 0;
		Date beginDate = null;
		Date endDate = null;
		
		if(lIsFromMonth && isToMonth) {
			lCalendar.setTime(pFromDate);
			beginDate = lCalendar.getTime();
			while(lCalendar.getTimeInMillis() <= pToDate.getTime()) {
		        int lDayOfWeek = lCalendar.get(Calendar.DAY_OF_WEEK);
		        if ((lDayOfWeek != Calendar.SATURDAY || includeSaturday) && (lDayOfWeek != Calendar.SUNDAY || includeSunday)) {
		        	daysInMonth++;
		        }
		        lCalendar.add(Calendar.DATE, 1);
		    }
			lCalendar.add(Calendar.DATE, -1);
			endDate = lCalendar.getTime();
			
		} else if(lIsFromMonth) {
			lCalendar.set(pYear, pMonth+1, 0);
			endDate = lCalendar.getTime();
			lCalendar.setTime(pFromDate);
			beginDate = lCalendar.getTime();
			
			while(lCalendar.getTimeInMillis() <= endDate.getTime()) {
		        int lDayOfWeek = lCalendar.get(Calendar.DAY_OF_WEEK);
		        if ((lDayOfWeek != Calendar.SATURDAY || includeSaturday) && (lDayOfWeek != Calendar.SUNDAY || includeSunday)) {
		        	daysInMonth++;
		        }
		        lCalendar.add(Calendar.DATE, 1);
		    }
			lCalendar.add(Calendar.DATE, -1);
			endDate = lCalendar.getTime();
		} else if(isToMonth) {
			endDate = pToDate;
			lCalendar.set(pYear, pMonth, 1);
			beginDate = lCalendar.getTime();
			
			while(lCalendar.getTimeInMillis() <= endDate.getTime()) {
		        int lDayOfWeek = lCalendar.get(Calendar.DAY_OF_WEEK);
		        if ((lDayOfWeek != Calendar.SATURDAY || includeSaturday) && (lDayOfWeek != Calendar.SUNDAY || includeSunday)) {
		        	daysInMonth++;
		        }
		        lCalendar.add(Calendar.DATE, 1);
		    }
		} else {
			lCalendar.set(pYear, pMonth, 1);
			beginDate = lCalendar.getTime();
			while(lCalendar.get(Calendar.MONTH) == pMonth && lCalendar.get(Calendar.YEAR) == pYear) {
		        int lDayOfWeek = lCalendar.get(Calendar.DAY_OF_WEEK);
		        if ((lDayOfWeek != Calendar.SATURDAY || includeSaturday) && (lDayOfWeek != Calendar.SUNDAY || includeSunday)) {
		        	daysInMonth++;
		        }
		        lCalendar.add(Calendar.DATE, 1);
		    }
			lCalendar.add(Calendar.DATE, -1);
			endDate = lCalendar.getTime();
		}
		
		Month lMonthObj = new Month(pYear, pMonth, daysInMonth, beginDate, endDate);
		return lMonthObj;
	}
	
	public static Week mGetWeekDetails(int pYear, int pWeek, Date pFromDate, Date pToDate, boolean includeSaturday, boolean includeSunday) {
		Calendar lCalendar = Calendar.getInstance();
		lCalendar.setTime(pFromDate);
		boolean isFromWeek = (lCalendar.get(Calendar.WEEK_OF_YEAR) == pWeek && lCalendar.get(Calendar.YEAR) == pYear)?true:false;
		lCalendar.setTime(pToDate);
		boolean isToWeek = (lCalendar.get(Calendar.WEEK_OF_YEAR) == pWeek && lCalendar.get(Calendar.YEAR) == pYear)?true:false;
		int lDaysInWeek = 0;
		
		Date beginDate = null;
		Date endDate = null;
		
		if(isFromWeek && isToWeek) {
			lCalendar.setTime(pFromDate);
			beginDate = lCalendar.getTime();
			while(lCalendar.getTimeInMillis() <= pToDate.getTime()) {
		        int lDayOfWeek = lCalendar.get(Calendar.DAY_OF_WEEK);
		        if ((lDayOfWeek != Calendar.SATURDAY || includeSaturday) && (lDayOfWeek != Calendar.SUNDAY || includeSunday)) {
		        	lDaysInWeek++;
		        }
		        lCalendar.add(Calendar.DATE, 1);
		    }
			lCalendar.add(Calendar.DATE, -1);
			endDate = lCalendar.getTime();
			
			
		} else if(isFromWeek) {
			lCalendar.set(Calendar.WEEK_OF_YEAR, pWeek+1);
			lCalendar.set(Calendar.YEAR, pYear);
			lCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			lCalendar.add(Calendar.DATE, -1);
			endDate = lCalendar.getTime();
			lCalendar.setTime(pFromDate);
			beginDate = lCalendar.getTime();
			
			while(lCalendar.getTimeInMillis() < endDate.getTime()) {
		        int lDayOfWeek = lCalendar.get(Calendar.DAY_OF_WEEK);
		        if ((lDayOfWeek != Calendar.SATURDAY || includeSaturday) && (lDayOfWeek != Calendar.SUNDAY || includeSunday)) {
		        	lDaysInWeek++;
		        }
		        lCalendar.add(Calendar.DATE, 1);
		    }
		} else if(isToWeek) {
			lCalendar.set(Calendar.WEEK_OF_YEAR, pWeek);
			lCalendar.set(Calendar.YEAR, pYear);
			lCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			beginDate = lCalendar.getTime();
			endDate = pToDate;
			while(lCalendar.getTimeInMillis() <= endDate.getTime()) {
		        int lDayOfWeek = lCalendar.get(Calendar.DAY_OF_WEEK);
		        if ((lDayOfWeek != Calendar.SATURDAY || includeSaturday) && (lDayOfWeek != Calendar.SUNDAY || includeSunday)) {
		        	lDaysInWeek++;
		        }
		        lCalendar.add(Calendar.DATE, 1);
		    }
		} else {
			lCalendar.set(Calendar.WEEK_OF_YEAR, pWeek);
			lCalendar.set(Calendar.YEAR, pYear);
			lCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			beginDate = lCalendar.getTime();
			while(lCalendar.get(Calendar.WEEK_OF_YEAR) == pWeek){
		        int lDayOfWeek = lCalendar.get(Calendar.DAY_OF_WEEK);
		        if ((lDayOfWeek != Calendar.SATURDAY || includeSaturday) && (lDayOfWeek != Calendar.SUNDAY || includeSunday)) {
		        	lDaysInWeek++;
		        }
		        lCalendar.add(Calendar.DATE, 1);
		    }
			endDate = lCalendar.getTime();
		}
		
		Week lWeekObj = new Week(pYear, pWeek, lDaysInWeek, beginDate, endDate);
		
		return lWeekObj;
	}
	
	private DateRange mGetDateRangeDetails(Date pFromDate, Date pToDate,
			 boolean includeSaturday, boolean includeSunday) {
		
		Calendar lCalendar = Calendar.getInstance();
		int lDaysInRange = 0;
		
		Date beginDate = null;
		Date endDate = null;
		
		lCalendar.setTime(pFromDate);
		beginDate = lCalendar.getTime();
		while(lCalendar.getTimeInMillis() < pToDate.getTime()){
	        int lDayOfWeek = lCalendar.get(Calendar.DAY_OF_WEEK);
	        if ((lDayOfWeek != Calendar.SATURDAY || includeSaturday) && (lDayOfWeek != Calendar.SUNDAY || includeSunday)) {
	        	lDaysInRange++;
	        }
	        lCalendar.add(Calendar.DATE, 1);
	    }
		endDate = lCalendar.getTime();
		
		DateRange dateRangeObj = new DateRange(beginDate, endDate, lDaysInRange);
		return dateRangeObj;
	}
	
	public static int mGetDateOfJoiningDifference(String pUserID, 
												  Date pFromDate, 
												  int pYear, 
												  int pMonth,  
												  boolean includeSaturday, 
												  boolean includeSunday) {
		Date dateOfJoin = cAttendanceDetailsDaoImpl.mGetUserDateOfJoin(pUserID);
		int lDiffInDays = 0;
		Calendar lCalendar = Calendar.getInstance();
		if(dateOfJoin != null) {
			lCalendar.setTime(pFromDate);
		}
		
		boolean lIsFromMonth = (lCalendar.get(Calendar.MONTH) == pMonth && lCalendar.get(Calendar.YEAR) == pYear)?true:false;
		
		lCalendar.set(pYear, pMonth, 1);
		Date lMonthOfYear =  lCalendar.getTime();
		
		lCalendar.setTime(dateOfJoin);
		if(lCalendar.getTimeInMillis() > pFromDate.getTime()) {

			if(lIsFromMonth) {
				while(lCalendar.getTimeInMillis()>pFromDate.getTime()) {
					lCalendar.add(Calendar.DATE, -1);
					int lDayOfWeek = lCalendar.get(Calendar.DAY_OF_WEEK);
			        if ((lDayOfWeek != Calendar.SATURDAY || includeSaturday) && (lDayOfWeek != Calendar.SUNDAY || includeSunday)) {
			        	lDiffInDays++;
			        }
				}
				return lDiffInDays;
			} else if (lMonthOfYear.getTime() < pFromDate.getTime()) {
				return 0;
				// Make everything to 0
			} else {
				return -1;
				// everything normal calculation
			}
		} else {
			return -1;
			// everything normal calculation
		}
		
	}
	
	public List<String> mBuildMonthlyAttendanceKeys(Date pFromDate, Date pToDate) {
		List<String> lMonthYearKeys = new ArrayList<String>();
		Calendar lToDateCal = Calendar.getInstance();
		lToDateCal.setTime(pToDate);
		Calendar lCalendar = Calendar.getInstance();
		lCalendar.setTime(pFromDate);
		while (!(lCalendar.get(Calendar.YEAR) == lToDateCal.get(Calendar.YEAR) && lCalendar.get(Calendar.MONTH) == lToDateCal.get(Calendar.MONTH))) {
			lMonthYearKeys.add(lCalendar.get(Calendar.YEAR) + "-" + (lCalendar.get(Calendar.MONTH)+1));
			lCalendar.add(Calendar.MONTH, 1);
		} 
		
		if (lCalendar.get(Calendar.YEAR) == lToDateCal.get(Calendar.YEAR) && lCalendar.get(Calendar.MONTH) == lToDateCal.get(Calendar.MONTH)) {
			lMonthYearKeys.add(lCalendar.get(Calendar.YEAR) + "-" + (lCalendar.get(Calendar.MONTH)+1));
		}
		
		return lMonthYearKeys;
	}
	public List<String> mBuildWeeklyAttendanceKeys(Date pFromDate, Date pToDate) {
		List<String> lWeekYearKeys = new ArrayList<String>();
		Calendar lCalendar = Calendar.getInstance();
		lCalendar.setTime(pFromDate);
		
		if (lCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
			lWeekYearKeys.add(lCalendar.get(Calendar.YEAR) + "-" + lCalendar.get(Calendar.WEEK_OF_YEAR));
			lCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			lCalendar.add(Calendar.WEEK_OF_YEAR, 1);
		}
		while (lCalendar.getTimeInMillis() <= pToDate.getTime()) {
			int lYear = lCalendar.get(Calendar.YEAR);
			if(lCalendar.get(Calendar.MONTH) == Calendar.DECEMBER && (31 - lCalendar.get(Calendar.DATE)) < 7) {
				lYear = lYear + 1;
			}
			lWeekYearKeys.add(lYear + "-" + lCalendar.get(Calendar.WEEK_OF_YEAR));
			lCalendar.add(Calendar.WEEK_OF_YEAR, 1);
		}
		
	/*	Calendar lToDateCal = Calendar.getInstance();
		lToDateCal.setTime(pToDate);
		if (lCalendar.get(Calendar.WEEK_OF_YEAR) == lToDateCal.get(Calendar.WEEK_OF_YEAR)) {
			lWeekYearKeys.add(lCalendar.get(Calendar.YEAR) + "-" + lCalendar.get(Calendar.WEEK_OF_YEAR));
		}*/
		return lWeekYearKeys;
	}
	public List<String> mBuildDailyAttendanceKeys(Date pFromDate, Date pToDate) {
		List<String> lDatesList = new ArrayList<String>();
		Calendar lCalendar = Calendar.getInstance();
		lCalendar.setTime(pFromDate);
		SimpleDateFormat lDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		while (lCalendar.getTimeInMillis() <= pToDate.getTime()) {
			String lDateStrObj = lDateFormat.format(lCalendar.getTime());
			lDatesList.add(lDateStrObj);
			lCalendar.add(Calendar.DATE, 1);
		}
		return lDatesList;
	}
	
	public static List<String> mBuildAllUsersMonthlyAttendanceKeys(List<User> lUsersList, Date pFromDate, Date pToDate) {
		List<String> lMonthYearKeys = new ArrayList<String>();
		for (User lUser : lUsersList) {

			Calendar lCalendar = Calendar.getInstance();
			lCalendar.setTime(pFromDate);
			while (lCalendar.getTimeInMillis() <= pToDate.getTime()) {
				lMonthYearKeys.add(lUser.getUserId() + "&" + lCalendar.get(Calendar.YEAR) + "-" + (lCalendar.get(Calendar.MONTH)+1));
				lCalendar.add(Calendar.MONTH, 1);
			}
			Calendar lToDateCal = Calendar.getInstance();
			lToDateCal.setTime(pToDate);
			if (lCalendar.get(Calendar.YEAR) == lToDateCal.get(Calendar.YEAR) && lCalendar.get(Calendar.MONTH) == lToDateCal.get(Calendar.MONTH)) {
				lMonthYearKeys.add(lUser.getUserId() + "&" + lCalendar.get(Calendar.YEAR) + "-" + (lCalendar.get(Calendar.MONTH)+1));
			}
		}
		
		return lMonthYearKeys;
	}
	
	public static List<String> mBuildAllUsersWeeklyAttendanceKeys(List<User> lUsersList, Date pFromDate, Date pToDate) {
		List<String> lWeekYearKeys = new ArrayList<String>();
		for (User lUser : lUsersList) {
			Calendar lCalendar = Calendar.getInstance();
			
			lCalendar.setTime(pFromDate);
			if (lCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
				lWeekYearKeys.add(lUser.getUserId() + "&" + lCalendar.get(Calendar.YEAR) + "-" + lCalendar.get(Calendar.WEEK_OF_YEAR));
				lCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				lCalendar.add(Calendar.WEEK_OF_YEAR, 1);
			}
			while (lCalendar.getTimeInMillis() <= pToDate.getTime()) {
				int lYear = lCalendar.get(Calendar.YEAR);
				if(lCalendar.get(Calendar.MONTH) == Calendar.DECEMBER && (31 - lCalendar.get(Calendar.DATE)) < 7) {
					lYear = lYear + 1;
				}
				lWeekYearKeys.add(lUser.getUserId() + "&" + lYear + "-" + lCalendar.get(Calendar.WEEK_OF_YEAR));
				lCalendar.add(Calendar.WEEK_OF_YEAR, 1);
			}
			/*Calendar lToDateCal = Calendar.getInstance();
			lToDateCal.setTime(pToDate);
			if (lCalendar.get(Calendar.WEEK_OF_YEAR) == lToDateCal.get(Calendar.WEEK_OF_YEAR) && lCalendar.get(Calendar.YEAR) == lToDateCal.get(Calendar.YEAR)) {
				lWeekYearKeys.add(lUser.getUserId() + "&" + lCalendar.get(Calendar.YEAR) + "-" + lCalendar.get(Calendar.WEEK_OF_YEAR));
			}*/
			
		}

		return lWeekYearKeys;
	}
	
	public static double mGetEffectiveHoursFromSwipes(String pUserID, 
													  Date pFromDate, 
													  Date pToDate, 
													  boolean pGetElapsedTime, 
													  boolean pConsiderCurrentTime, 
													  OfficeDetails pOfficeDetails)  throws Exception {
		
		double lEffectiveHours = 0.00;
		Calendar lFromCal = Calendar.getInstance();
		lFromCal.setTime(pFromDate);
		
		long lCalculatedFullDayWorkingHours = pOfficeDetails.getShiftEnd().getTimeInMillis() - pOfficeDetails.getShiftStart().getTimeInMillis();
		
		List<UserInOutPair> lUserInOutPairs = new ArrayList<>();
		
		while (lFromCal.getTimeInMillis() <= pToDate.getTime()) {
			lUserInOutPairs = mGetUserInOutPairs(pUserID, lFromCal.getTime(), lFromCal.getTime(), pOfficeDetails);
			//boolean lConsiderAllDoors = false;
			
			Date lFirstInTime = null;
			Date lLastOutTime = null;
			
			
			boolean lIsFirstHalfTour = false;
			boolean lIsSecondHalfTour = false;
			boolean lIsFirstHalfOnLeave = false;
			boolean lIsSecondHalfOnLeave = false;
			Calendar lDateCal = Calendar.getInstance();

			
			
			if (lUserInOutPairs.size() > 0) {
				boolean lIsInOffice = cAttendanceDetailsDaoImpl.mIsUserInOffice(pUserID);
				if (pGetElapsedTime) {
					UserInOutPair lFirstInoutPair = lUserInOutPairs.get(0);
					UserInOutPair lLastInoutPair = lUserInOutPairs.get(lUserInOutPairs.size() - 1);
					if (lFirstInoutPair.getType().equalsIgnoreCase(PRESENT_ID) || lFirstInoutPair.getType().equalsIgnoreCase(OFFICIAL_OUT_ID)) {
						lFirstInTime = lFirstInoutPair.getInTime();
					}
					
					if (lLastInoutPair.getType().equalsIgnoreCase(PRESENT_ID) || lLastInoutPair.getType().equalsIgnoreCase(OFFICIAL_OUT_ID)) {
						lLastOutTime = lLastInoutPair.getOutTime();
					}
					
					lEffectiveHours = lEffectiveHours + (lLastOutTime.getTime() - lFirstInTime.getTime());
					
					if (lIsInOffice) {
						lEffectiveHours = lEffectiveHours + (new Date().getTime() - lFirstInTime.getTime());
					}
					
				} else {
					for (UserInOutPair lInOutPair : lUserInOutPairs) {
						if(lInOutPair.getType().equals(TOUR_ID) || lInOutPair.getType().equals(ONDUTY_ID)) {
							
							lDateCal.setTime(lInOutPair.getDate());
							lDateCal.set(Calendar.HOUR_OF_DAY, pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
							lDateCal.set(Calendar.MINUTE, pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
							
							if (lInOutPair.getInTime().getTime() == lDateCal.getTimeInMillis()) {
								lIsFirstHalfTour = true;
							}
							
							lDateCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftEnd().get(Calendar.HOUR_OF_DAY));
							lDateCal.set(Calendar.MINUTE,pOfficeDetails.getShiftEnd().get(Calendar.MINUTE));
							
							if (lInOutPair.getOutTime().getTime() == lDateCal.getTimeInMillis()) {
								lIsSecondHalfTour = true;
							}
						}
						
						if(lInOutPair.getType().equals(LEAVES_ID)) {
							
							lDateCal.setTime(lInOutPair.getDate());
							lDateCal.set(Calendar.HOUR_OF_DAY, pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
							lDateCal.set(Calendar.MINUTE, pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
							
							if (lInOutPair.getInTime().getTime() == lDateCal.getTimeInMillis()) {
								lIsFirstHalfOnLeave = true;
							}
							
							lDateCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftEnd().get(Calendar.HOUR_OF_DAY));
							lDateCal.set(Calendar.MINUTE,pOfficeDetails.getShiftEnd().get(Calendar.MINUTE));
							
							if (lInOutPair.getOutTime().getTime() == lDateCal.getTimeInMillis()) {
								lIsSecondHalfOnLeave = true;
							}
						}
								
					}
				}
				
				if ((lIsFirstHalfTour && lIsSecondHalfTour) || (lIsFirstHalfOnLeave && lIsSecondHalfOnLeave)) {
					Calendar lDayBeginTimeCal = Calendar.getInstance();
					lDayBeginTimeCal.setTime(pFromDate);
					
					lDayBeginTimeCal.set(Calendar.HOUR_OF_DAY, 0);
					lDayBeginTimeCal.set(Calendar.MINUTE, 0);
					lDayBeginTimeCal.set(Calendar.SECOND, 0);


					Calendar lInTimeCal = Calendar.getInstance();
					lInTimeCal.setTime(pFromDate);
					
					lInTimeCal.set(Calendar.HOUR_OF_DAY, pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
					lInTimeCal.set(Calendar.MINUTE, pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
					lInTimeCal.set(Calendar.SECOND, pOfficeDetails.getShiftStart().get(Calendar.SECOND));
					
					lEffectiveHours = lEffectiveHours  + mGetUserShiftScheduleEffectiveHours(lUserInOutPairs, lDayBeginTimeCal.getTime(), lInTimeCal.getTime());
					
					
					
					Calendar lOutTimeCal = Calendar.getInstance();
					lOutTimeCal.setTime(pFromDate);
					
					lOutTimeCal.set(Calendar.HOUR_OF_DAY, pOfficeDetails.getShiftEnd().get(Calendar.HOUR_OF_DAY));
					lOutTimeCal.set(Calendar.MINUTE, pOfficeDetails.getShiftEnd().get(Calendar.MINUTE));
					lOutTimeCal.set(Calendar.SECOND, pOfficeDetails.getShiftEnd().get(Calendar.SECOND));


					Calendar lDayEndTimeCal = Calendar.getInstance();
					lDayEndTimeCal.setTime(pFromDate);
					
					lDayEndTimeCal.set(Calendar.HOUR_OF_DAY, 23);
					lDayEndTimeCal.set(Calendar.MINUTE, 59);
					lDayEndTimeCal.set(Calendar.SECOND, 59);
					
					lEffectiveHours = lEffectiveHours + mGetUserShiftScheduleEffectiveHours(lUserInOutPairs, lOutTimeCal.getTime(), lDayEndTimeCal.getTime());
				} else if ((!lIsFirstHalfTour && lIsSecondHalfTour)  || (!lIsFirstHalfOnLeave && lIsSecondHalfOnLeave)) {
					Calendar lInTimeCal = Calendar.getInstance();
					lInTimeCal.setTime(lFromCal.getTime());
					
					lInTimeCal.set(Calendar.HOUR_OF_DAY, 0);
					lInTimeCal.set(Calendar.MINUTE, 0);
					lInTimeCal.set(Calendar.SECOND, 0);


					Calendar lLunchTimeCal = Calendar.getInstance();
					lLunchTimeCal.setTime(lFromCal.getTime());

					lLunchTimeCal.set(Calendar.HOUR_OF_DAY, pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
					lLunchTimeCal.set(Calendar.MINUTE, pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
					lLunchTimeCal.set(Calendar.SECOND, pOfficeDetails.getShiftStart().get(Calendar.SECOND));
					lLunchTimeCal.add(Calendar.MILLISECOND, (int) (lCalculatedFullDayWorkingHours/2));
					lEffectiveHours = lEffectiveHours + mGetUserShiftScheduleEffectiveHours(lUserInOutPairs, lInTimeCal.getTime(), lLunchTimeCal.getTime());
				} else if ((lIsFirstHalfTour && !lIsSecondHalfTour) || (lIsFirstHalfOnLeave && !lIsSecondHalfOnLeave)) {
					Calendar lLunchTimeCal = Calendar.getInstance();
					lLunchTimeCal.setTime(lFromCal.getTime());

					
					lLunchTimeCal.set(Calendar.HOUR_OF_DAY, pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
					lLunchTimeCal.set(Calendar.MINUTE, pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
					lLunchTimeCal.set(Calendar.SECOND, pOfficeDetails.getShiftStart().get(Calendar.SECOND));
					lLunchTimeCal.add(Calendar.MILLISECOND, (int) (lCalculatedFullDayWorkingHours/2));


					Calendar lOutTimeCal = Calendar.getInstance();
					lOutTimeCal.setTime(lFromCal.getTime());
					
					lOutTimeCal.set(Calendar.HOUR_OF_DAY, 23);
					lOutTimeCal.set(Calendar.MINUTE, 59);
					lOutTimeCal.set(Calendar.SECOND, 59);
					
					lEffectiveHours = lEffectiveHours  + mGetUserShiftScheduleEffectiveHours(lUserInOutPairs,  lLunchTimeCal.getTime(), lOutTimeCal.getTime());
				} else {
					Calendar lLunchTimeCal = Calendar.getInstance();
					lLunchTimeCal.setTime(lFromCal.getTime());
					
					lLunchTimeCal.set(Calendar.HOUR_OF_DAY, 0);
					lLunchTimeCal.set(Calendar.MINUTE, 0);
					lLunchTimeCal.set(Calendar.SECOND, 0);


					Calendar lOutTimeCal = Calendar.getInstance();
					lOutTimeCal.setTime(lFromCal.getTime());
					
					lOutTimeCal.set(Calendar.HOUR_OF_DAY, 23);
					lOutTimeCal.set(Calendar.MINUTE, 59);
					lOutTimeCal.set(Calendar.SECOND, 59);

					lEffectiveHours = lEffectiveHours + mGetUserShiftScheduleEffectiveHours(lUserInOutPairs,  lLunchTimeCal.getTime(), lOutTimeCal.getTime());
				}
				
				
				Calendar lCal = Calendar.getInstance();
				lCal.set(Calendar.HOUR_OF_DAY, 0);
				lCal.set(Calendar.MINUTE, 0);
				lCal.set(Calendar.SECOND, 0);
				lCal.set(Calendar.MILLISECOND, 0);
				if (lIsInOffice && pConsiderCurrentTime) {
					UserInOutPair lInOutPair = lUserInOutPairs.get(lUserInOutPairs.size()-1);

					Calendar lOutCal = Calendar.getInstance();
					lOutCal.setTime(lInOutPair.getOutTime());
					lOutCal.set(Calendar.HOUR_OF_DAY, 0);
					lOutCal.set(Calendar.MINUTE, 0);
					lOutCal.set(Calendar.SECOND, 0);
					lOutCal.set(Calendar.MILLISECOND, 0);
					if (lOutCal.getTimeInMillis() == lCal.getTimeInMillis()) {
						
						lEffectiveHours = lEffectiveHours + (new Date().getTime() - lInOutPair.getOutTime().getTime());
						
					}
				}
			}
			
			lFromCal.add(Calendar.DATE, 1);
		}
		lEffectiveHours = lEffectiveHours / (1000 * 60 * 60);

		return lEffectiveHours;
	}
	public static List<String> mBuildAllUsersDailyAttendanceKeys(List<User> lUsersList, Date pFromDate, Date pToDate) {
		List<String> lDatesList = new ArrayList<String>();
		for (User lUser : lUsersList) {
			Calendar lCalendar = Calendar.getInstance();
			lCalendar.setTime(pFromDate);
			SimpleDateFormat lDateFormat = new SimpleDateFormat("dd/MM/yyyy");
			while (lCalendar.getTimeInMillis() <= pToDate.getTime()) {
				String lDateStrObj = lDateFormat.format(lCalendar.getTime());
				lDatesList.add(lUser.getUserId() + "&" + lDateStrObj);
				lCalendar.add(Calendar.DATE, 1);
			}
		}
		return lDatesList;
	}
	
	public List<AttendanceDetails> mGetUserMonthlyAttendance(User pUser,
															 Date pFromDate, 
															 Date pToDate, 
															 String pFrequency, 
															 JsonArray pHolidayList, 
															 JsonArray pOfficeDetailsList)  throws Exception{
		
		//Date lUserJoinDate = new Date();
		List<AttendanceDetails> lMonthlyAttendanceList = new ArrayList<AttendanceDetails>();
		
		/*if ((lUserJoinDate = mGetUserDateOfJoin(pUserID)) == null) {
			return lMonthlyAttendanceList;
		}
		
		
		if (lUserJoinDate.getTime() > pFromDate.getTime()) {
			pFromDate = lUserJoinDate;
		}
		
		if (lUserJoinDate.getTime() > pToDate.getTime()) {
			pToDate = lUserJoinDate;
		}
		
		List<String> lMonthYearKeys = new ArrayList<String>();
		if(lUserJoinDate.getTime() < pToDate.getTime()) {
			lMonthYearKeys = mBuildMonthlyAttendanceKeys(pFromDate, pToDate);
		}*/

		List<String> lMonthYearKeys = new ArrayList<String>();
		lMonthYearKeys = mBuildMonthlyAttendanceKeys(pFromDate, pToDate);

		SimpleDateFormat lDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String lFromDateStr = lDateFormat.format(pFromDate);
		String lToDateStr = lDateFormat.format(pToDate);
		

		//Map<String, Integer> lHolidayList = cAttendanceDetailsDaoImpl.mGetHolidaySchedule(pUser.getHolidayId(), lFromDateStr, lToDateStr, pFrequency);
		
		Map<String, Integer> lHolidayList = mGetHolidaysAsMap(pUser, pFromDate, pToDate, pFrequency, pHolidayList);
		
		
		Map<String, Double> lUserPresenceList = cAttendanceDetailsDaoImpl.mGetUserPresenceAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency);
		
		
		 /*=== Vacations ===*/
		// Leaves
		Map<String, Double> lUserLeaveAvailedList = cAttendanceDetailsDaoImpl.mGetVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, LEAVE_IDS);
		Map<String, Double> lUserHalfdayLeaveAvailedList = cAttendanceDetailsDaoImpl.mGetHalfdayVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, LEAVE_IDS);
		
		// On Duty
		Map<String, Double> lUserODsList = cAttendanceDetailsDaoImpl.mGetVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, new String[] {"OD"});
		Map<String, Double> lUserHalfdayODsList = cAttendanceDetailsDaoImpl.mGetHalfdayVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, new String[] {"OD"});
		
		// On Tour
		Map<String, Double> lUserTourList = cAttendanceDetailsDaoImpl.mGetVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, new String[] {"TR"});
		Map<String, Double> lUserHalfdayTourList = cAttendanceDetailsDaoImpl.mGetHalfdayVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, new String[] {"TR"});
		
		// Leave Details
		List<Leave> lUserLeaves = cAttendanceDetailsDaoImpl.mGetUserLeavesDetails(pUser.getUserId(), lFromDateStr, lToDateStr);
		
		OfficeDetails lOfficeDetails = mGetOfficeDetailsByLocation(pUser.getHolidayId(), pOfficeDetailsList);
		
		for (String lYearMonth : lMonthYearKeys) {
			int lYear = Integer.parseInt(lYearMonth.split("-")[0]);
			int lMonth = Integer.parseInt(lYearMonth.split("-")[1]);
			
			//int dateOfJoiningDiffDays = mGetDateOfJoiningDifference(pUser.getUserId(), pFromDate, lYear, lMonth-1,  false, false);
			int dateOfJoiningDiffDays = -1;
			Month lMonthObj = null; //mGetMonthDetails(lYear, lMonth-1, pFromDate, pToDate, true, true);
			int lHolidays = 0;
			double lOnDuty = 0.0;
			double lTour = 0.0;
			double lEffectiveHours = 0.0;
			double lLeavesAvailed = 0.0;
			double lHalfdayLeavesAvailed = 0.0;
			
			if(lHolidayList.get(lYearMonth)!= null) {
				lHolidays = lHolidayList.get(lYearMonth);
			}
			
			if(lUserODsList.get(lYearMonth)!=null) {
				lOnDuty = lOnDuty + lUserODsList.get(lYearMonth);
			}
			
			if(lUserHalfdayODsList.get(lYearMonth)!=null) {
				lOnDuty = lOnDuty + lUserHalfdayODsList.get(lYearMonth);
			}
			
			if(lUserTourList.get(lYearMonth)!=null) {
				lTour = lTour + lUserTourList.get(lYearMonth);
			}
			
			if(lUserHalfdayTourList.get(lYearMonth)!=null) {
				lTour = lTour + lUserHalfdayTourList.get(lYearMonth);
			}
			
			if(lUserPresenceList.get(lYearMonth)!=null) {
				//lEffectiveHours = lUserPresenceList.get(lYearMonth);
			}

			if(lUserLeaveAvailedList.get(lYearMonth)!=null) {
				lLeavesAvailed = lLeavesAvailed + lUserLeaveAvailedList.get(lYearMonth);
			}
			
			
			if(lUserHalfdayLeaveAvailedList.get(lYearMonth)!=null) {
				lLeavesAvailed = lLeavesAvailed + lUserHalfdayLeaveAvailedList.get(lYearMonth);
			}
			
			
			
			
			lMonthObj = mGetMonthDetails(lYear, lMonth-1, pFromDate, pToDate, false, false);
			int daysInMonth = lMonthObj.getDays();	
			boolean lConsiderCurrentTime = true;
			lEffectiveHours = mGetEffectiveHoursFromSwipes(pUser.getUserId(), lMonthObj.getBeginDate(), lMonthObj.getEndDate(), false, lConsiderCurrentTime,  lOfficeDetails);
			
			
			int lWorkingDays = 0;
			double lDaysPresent = 0;
			double lRequiredHours = 0.0;
			double lDifference = 0.0;

			/*if(dateOfJoiningDiffDays == -1) {*/
			
				lWorkingDays = daysInMonth - lHolidays; 
				
				// To check false leave application. To check if user applied leave on holidays
				if (daysInMonth < lWorkingDays  + lLeavesAvailed) {
					lLeavesAvailed = (lWorkingDays + lLeavesAvailed) - daysInMonth;
				}
				lDaysPresent = lWorkingDays - lLeavesAvailed;

				double lDaysFractionalPart = lDaysPresent % 1;
				double lDaysIntegralPart = lDaysPresent - lDaysFractionalPart;
				
				if (lDaysPresent > 0) {
					//lRequiredHours = (lDaysIntegralPart * cAttendanceWebservice.MIN_FULL_DAY_WORKING_HOURS) + (lDaysFractionalPart * (cAttendanceWebservice.MIN_HALF_DAY_WORKING_HOURS*2));
					lRequiredHours = (lDaysIntegralPart * lOfficeDetails.getMinFullDayWorkingHours()) + (lDaysFractionalPart * (lOfficeDetails.getMinHalfDayWorkingHours() * 2));
				} else {
					lRequiredHours = 0;
				}
				//lRequiredHours = (lDaysPresent > 0 ? lDaysPresent : 0) * cAttendanceWebservice.MIN_FULL_DAY_WORKING_HOURS;
				
				
				lDifference = lEffectiveHours - lRequiredHours;
				
			/*} else if (dateOfJoiningDiffDays == 0) {
				lEffectiveHours = 0;
			} else {
				lWorkingDays = daysInMonth - (lHolidays + dateOfJoiningDiffDays); 
				lDaysPresent = lWorkingDays - lLeavesAvailed;
				lRequiredHours = (lWorkingDays - lLeavesAvailed)* aOfficeDetails.getWorkingHours();
				lDifference = lEffectiveHours - lRequiredHours;
			}*/
			
			List<Leave> lUserLeaveDetailsList = new ArrayList<Leave>();
			for (Leave lUserLeave : lUserLeaves) {
				Calendar lUserLeaveFromDate = Calendar.getInstance();
				Calendar lUserLeaveToDate = Calendar.getInstance();
				lUserLeaveFromDate.setTime(lUserLeave.getFromDate());
				lUserLeaveToDate.setTime(lUserLeave.getToDate());
				if(((lUserLeaveFromDate.get(Calendar.YEAR) == lYear && lUserLeaveFromDate.get(Calendar.MONTH) == (lMonth-1)) || 
				   (lUserLeaveToDate.get(Calendar.YEAR) == lYear && lUserLeaveToDate.get(Calendar.MONTH) == (lMonth-1))) && 
				   (lUserLeave.getToDate().getTime() >= pFromDate.getTime() && lUserLeave.getFromDate().getTime() <= pToDate.getTime())) {
					
					if (lLeavesAvailed == 0 && lWorkingDays > 0) {
						lLeavesAvailed = lUserLeave.getPostedDays() > daysInMonth ? daysInMonth :  lUserLeave.getPostedDays();
					}
					lUserLeave.setComputedLeaves(lLeavesAvailed);
					lUserLeaveDetailsList.add(lUserLeave);
				}
			}
			
			String lMFromDate = lDateFormat.format(lMonthObj.getBeginDate());
			String lMToDate = lDateFormat.format(lMonthObj.getEndDate());
			//List<Leave> lUserLeaveDetailsList = cAttendanceDetailsDaoImpl.mGetUserLeavesDetailsBound(pUser.getUserId(), lMFromDate, lMToDate);
			
			String lTodayStr = lDateFormat.format(new Date());
			
			if (lTodayStr.equalsIgnoreCase(lToDateStr) && lRequiredHours != 0) {
				//lRequiredHours = lRequiredHours - aOfficeDetails.getWorkingHours();
			}
			
			AttendanceDetails lMonthlyAttendance = new AttendanceDetails(pUser.getUserId(), 
																		 pFrequency, 
																		 lYear, 
																		 lMonth, 
																		 lMonthObj.getBeginDate(), 
																		 lMonthObj.getEndDate(), 
																		 lWorkingDays, 
																		 lDaysPresent, 
																		 lOnDuty, 
																		 lLeavesAvailed, 
																		 "",
																		 lUserLeaveDetailsList, 
																		 lRequiredHours, 
																		 lEffectiveHours, 
																		 lDifference);
			lMonthlyAttendance.setTour(lTour);
			lMonthlyAttendanceList.add(lMonthlyAttendance);
		}

				return lMonthlyAttendanceList;
	}
	
	public List<AttendanceDetails> mGetUserWeeklyAttendance(User pUser,
															Date pFromDate, 
															Date pToDate, 
															String pFrequency, 
															JsonArray pHolidayList, 
															JsonArray pOfficeDetailsList)  throws Exception {
		
		Date lUserJoinDate = new Date();
		List<AttendanceDetails> lWeeklyAttendanceList = new ArrayList<AttendanceDetails>();
		
		/*if ((lUserJoinDate = mGetUserDateOfJoin(pUserID)) == null) {
			return lWeeklyAttendanceList;
		}
		if (lUserJoinDate.getTime() > pFromDate.getTime()) {
			pFromDate = lUserJoinDate;
		}
		
		if (lUserJoinDate.getTime() > pToDate.getTime()) {
			pToDate = lUserJoinDate;
		}
		
		List<String> lYearWeekKeys = new ArrayList<String>();
		if(lUserJoinDate.getTime() < pToDate.getTime()) {
			lYearWeekKeys = mBuildWeeklyAttendanceKeys(pFromDate, pToDate);
		}*/
		
		List<String> lYearWeekKeys = new ArrayList<String>();
		lYearWeekKeys = mBuildWeeklyAttendanceKeys(pFromDate, pToDate);
		
		SimpleDateFormat lDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String lFromDateStr = lDateFormat.format(pFromDate);
		String lToDateStr = lDateFormat.format(pToDate);
		
		
		//Map<String, Integer> lHolidayList = cAttendanceDetailsDaoImpl.mGetHolidaySchedule(pUser.getHolidayId(), lFromDateStr, lToDateStr, pFrequency);
		Map<String, Integer> lHolidayList = mGetHolidaysAsMap(pUser, pFromDate, pToDate, pFrequency, pHolidayList);
		
		Map<String, Double> lUserPresenceList = cAttendanceDetailsDaoImpl.mGetUserPresenceAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency);
		
		/*=== Vacations ===*/
		// Leaves
		Map<String, Double> lUserLeaveAvailedList = cAttendanceDetailsDaoImpl.mGetVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, LEAVE_IDS);
		Map<String, Double> lUserHalfdayLeaveAvailedList = cAttendanceDetailsDaoImpl.mGetHalfdayVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, LEAVE_IDS);
		
		
		// On Duty
		Map<String, Double> lUserODsList = cAttendanceDetailsDaoImpl.mGetVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, new String[] {"OD"});
		Map<String, Double> lUserHalfdayODsList = cAttendanceDetailsDaoImpl.mGetHalfdayVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, new String[] {"OD"});
		
		// On Tour
		Map<String, Double> lUserTourList = cAttendanceDetailsDaoImpl.mGetVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, new String[] {"TR"});
		Map<String, Double> lUserHalfdayTourList = cAttendanceDetailsDaoImpl.mGetHalfdayVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, new String[] {"TR"});
		
		
		List<Leave> lUserLeaves = cAttendanceDetailsDaoImpl.mGetUserLeavesDetails(pUser.getUserId(), lFromDateStr, lToDateStr);

		OfficeDetails lOfficeDetails = mGetOfficeDetailsByLocation(pUser.getHolidayId(), pOfficeDetailsList);
		
		for (String lYearWeek : lYearWeekKeys) {
			int lHolidays = 0;
			double lOnDuty = 0.0;
			double lTour = 0.0;
			double lEffectiveHours = 0.0;
			double lLeavesAvailed = 0.0;
			double lHalfdayLeavesAvailed = 0.0;
			
			if(lHolidayList.get(lYearWeek)!= null) {
				lHolidays = lHolidayList.get(lYearWeek);
			}
			
			if(lUserODsList.get(lYearWeek)!=null) {
				lOnDuty = lOnDuty + lUserODsList.get(lYearWeek);
			}
			
			if(lUserHalfdayODsList.get(lYearWeek)!=null) {
				lOnDuty = lOnDuty + lUserHalfdayODsList.get(lYearWeek);
			}
			
			if(lUserTourList.get(lYearWeek)!=null) {
				lTour = lTour + lUserTourList.get(lYearWeek);
			}
			
			if(lUserHalfdayTourList.get(lYearWeek)!=null) {
				lTour = lTour + lUserHalfdayTourList.get(lYearWeek);
			}
					
			if(lUserPresenceList.get(lYearWeek)!=null) {
				//lEffectiveHours = lUserPresenceList.get(lYearWeek);
			}
			
			
			
			if(lUserLeaveAvailedList.get(lYearWeek)!=null) {
				lLeavesAvailed = lLeavesAvailed + lUserLeaveAvailedList.get(lYearWeek);
			}
					
			if(lUserHalfdayLeaveAvailedList.get(lYearWeek)!=null) {
				lLeavesAvailed = lLeavesAvailed + lUserHalfdayLeaveAvailedList.get(lYearWeek);
			}
			

			
			int lYear = Integer.parseInt(lYearWeek.split("-")[0]);
			int lWeek = Integer.parseInt(lYearWeek.split("-")[1]);
			Week lWeekObj = mGetWeekDetails(lYear, lWeek, pFromDate, pToDate, false, false);
			
			boolean lConsiderCurrentTime = true;
			lEffectiveHours = mGetEffectiveHoursFromSwipes(pUser.getUserId(), lWeekObj.getBeginDate(), lWeekObj.getEndDate(), false, lConsiderCurrentTime,  lOfficeDetails);

			int lDaysInWeek = lWeekObj.getDays();
			
			
			
			int lWorkingDays = 0;
			//if (lHolidays == 0) {
				lWorkingDays = lDaysInWeek - lHolidays; 
			//}
			
			// To check false leave application. To check if user applied leave on holidays
			if (lDaysInWeek < lWorkingDays  + lLeavesAvailed) {
				lLeavesAvailed = (lWorkingDays + lLeavesAvailed) - lDaysInWeek;
			}
			
			List<Leave> lUserLeaveDetailsList = new ArrayList<Leave>();
			for (Leave lUserLeave : lUserLeaves) {
				Calendar lUserLeaveFromDate = Calendar.getInstance();
				Calendar lUserLeaveToDate = Calendar.getInstance();
				lUserLeaveFromDate.setTime(lUserLeave.getFromDate());
				lUserLeaveToDate.setTime(lUserLeave.getToDate());
				if(((lUserLeaveFromDate.get(Calendar.YEAR) == lYear && 
						lUserLeaveFromDate.get(Calendar.WEEK_OF_YEAR) == lWeek) || 
				   (lUserLeaveToDate.get(Calendar.YEAR) == lYear && 
				   lUserLeaveToDate.get(Calendar.WEEK_OF_YEAR) == lWeek)) && 
				   (lUserLeave.getToDate().getTime() >= pFromDate.getTime() && 
				   lUserLeave.getFromDate().getTime() <= pToDate.getTime())) {
					if (lLeavesAvailed == 0&& lWorkingDays > 0) { // Usually for future leaves
						 lLeavesAvailed = lUserLeave.getPostedDays() > lDaysInWeek ? lDaysInWeek :  lUserLeave.getPostedDays();
					}
					lUserLeave.setComputedLeaves(lLeavesAvailed);
					lUserLeaveDetailsList.add(lUserLeave);					
				}
			}
			String lWKFromDate = lDateFormat.format(lWeekObj.getBeginDate());
			String lWKToDate = lDateFormat.format(lWeekObj.getEndDate());
			
			//List<Leave> lUserLeaveDetailsList = cAttendanceDetailsDaoImpl.mGetUserLeavesDetailsBound(pUser.getUserId(), lWKFromDate, lWKToDate);
			double lRequiredHours = 0;
			double lDaysPresent = lWorkingDays - lLeavesAvailed;
			
			double lDaysFractionalPart = lDaysPresent % 1;
			double lDaysIntegralPart = lDaysPresent - lDaysFractionalPart;
			
			if (lDaysPresent > 0) {
				lRequiredHours = (lDaysIntegralPart * lOfficeDetails.getMinFullDayWorkingHours()) + (lDaysFractionalPart * (lOfficeDetails.getMinHalfDayWorkingHours() * 2));
				//lRequiredHours = (lDaysIntegralPart * cAttendanceWebservice.MIN_FULL_DAY_WORKING_HOURS) + (lDaysFractionalPart * (cAttendanceWebservice.MIN_HALF_DAY_WORKING_HOURS*2));
			} else {
				lRequiredHours = 0;
			}
			//double lRequiredHours = (lDaysPresent > 0 ? lDaysPresent : 0) * cAttendanceWebservice.MIN_FULL_DAY_WORKING_HOURS;
			double lDifference = lEffectiveHours - lRequiredHours;
			
			String lTodayStr = lDateFormat.format(new Date());
			if (lTodayStr.equalsIgnoreCase(lToDateStr) && lRequiredHours != 0) {
				//lRequiredHours = lRequiredHours - aOfficeDetails.getWorkingHours();
			}
			
			
			AttendanceDetails lWeeklyAttendance = new AttendanceDetails(pUser.getUserId(), 
																		pFrequency, 
																		lYear, 
																		lWeek, 
																		lWeekObj.getBeginDate(), 
																		lWeekObj.getEndDate(), 
																		lWorkingDays, 
																		lDaysPresent, 
																		lOnDuty, 
																		lLeavesAvailed, 
																		lUserLeaveDetailsList, 
																		lRequiredHours, 
																		lEffectiveHours, 
																		lDifference);
			lWeeklyAttendance.setTour(lTour);
			lWeeklyAttendanceList.add(lWeeklyAttendance);
		}
		
		Calendar lFromDateCalendar = Calendar.getInstance();
		lFromDateCalendar.setTime(pFromDate);
		
		lFromDateCalendar.set(Calendar.WEEK_OF_YEAR, 1);
		lFromDateCalendar.set(Calendar.YEAR, lFromDateCalendar.get(Calendar.YEAR)+1);
		lFromDateCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		Calendar lToDateCalendar = Calendar.getInstance();
		lToDateCalendar.setTime(pToDate);
		if (lFromDateCalendar.get(Calendar.YEAR)+1 == lToDateCalendar.get(Calendar.YEAR)) {
			
			lFromDateStr = lDateFormat.format(lFromDateCalendar.getTime());
			
			lFromDateCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
			lFromDateCalendar.set(Calendar.DATE, 31);
			lToDateStr = lDateFormat.format(lFromDateCalendar.getTime());
			
			
			Map<String, Double> lUserWeeklyYearTransitionPresenceList = cAttendanceDetailsDaoImpl.mGetUserPresenceAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, "monthly");

			double lModifiedEffective = lUserWeeklyYearTransitionPresenceList.get(lFromDateCalendar.get(Calendar.YEAR) + "-" + (lFromDateCalendar.get(Calendar.MONTH)+1));
			
			int lCount = -1;
			int lIndex = -1;
			AttendanceDetails lModifiedAttendanceDetails = null;
			for (AttendanceDetails lAttendanceDetails : lWeeklyAttendanceList) {
				lCount++;
				if (/*lAttendanceDetails.getYear() == (lFromDateCalendar.get(Calendar.YEAR)  + 1) && */lAttendanceDetails.getWeek() == 1 ) {
					double lEffectiveHours = lAttendanceDetails.getEffectiveHours();// + lModifiedEffective;
					double lDifference = lAttendanceDetails.getDifference() + lModifiedEffective;
					lAttendanceDetails.setEffectiveHours(lEffectiveHours);
					lAttendanceDetails.setDifference(lDifference);
					lModifiedAttendanceDetails = lAttendanceDetails;
					lIndex = lCount;
				}
			}
			
			//lWeeklyAttendanceList.remove(count);
			lWeeklyAttendanceList.set(lIndex, lModifiedAttendanceDetails);
		}
		
		return lWeeklyAttendanceList;
	}
	
	
	public List<AttendanceDetails> mGetUserDailyAttendance(User pUser,
														   Date pFromDate, 
														   Date pToDate, 
														   String pFrequency, 
														   JsonArray pHolidayList, 
														   JsonArray pOfficeDetailsList)  throws Exception{
		Date lUserJoinDate = new Date();
		List<AttendanceDetails> lDailyAttendanceList = new ArrayList<AttendanceDetails>();

		/*if ((lUserJoinDate = mGetUserDateOfJoin(pUserID)) == null) {
			return lDailyAttendanceList;
		}
		if (lUserJoinDate.getTime() > pFromDate.getTime()) {
			pFromDate = lUserJoinDate;
		}
		
		if (lUserJoinDate.getTime() > pToDate.getTime()) {
			pToDate = lUserJoinDate;
		}
		
		List<String> lDates = new ArrayList<String>();
		if(lUserJoinDate.getTime() < pToDate.getTime()) {
			lDates = mBuildDailyAttendanceKeys(pFromDate, pToDate);
		}*/
		
		List<String> lDates = new ArrayList<String>();
		lDates = mBuildDailyAttendanceKeys(pFromDate, pToDate);
		
		SimpleDateFormat lDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String lFromDateStr = lDateFormat.format(pFromDate);
		String lToDateStr = lDateFormat.format(pToDate);
		
		//Map<String, Integer> lHolidayList = cAttendanceDetailsDaoImpl.mGetHolidaySchedule(pUser.getHolidayId(), lFromDateStr, lToDateStr, pFrequency);
		Map<String, Integer> lHolidayList = mGetHolidaysAsMap(pUser, pFromDate, pToDate, pFrequency, pHolidayList);
		
		
		Map<String, Double> lUserPresenceList = cAttendanceDetailsDaoImpl.mGetUserPresenceAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency);
		
		/*=== Vacations ===*/
		// Leaves
		Map<String, Double> lUserLeaveAvailedList = cAttendanceDetailsDaoImpl.mGetVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, LEAVE_IDS);
		Map<String, Double> lUserHalfdayLeaveAvailedList = cAttendanceDetailsDaoImpl.mGetHalfdayVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, LEAVE_IDS);
		
		// On Duty
		Map<String, Double> lUserODsList = cAttendanceDetailsDaoImpl.mGetVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, new String[] {"OD"});
		Map<String, Double> lUserHalfdayODsList = cAttendanceDetailsDaoImpl.mGetHalfdayVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, new String[] {"OD"});
		
		// On Tour
		Map<String, Double> lUserTourList = cAttendanceDetailsDaoImpl.mGetVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, new String[] {"TR"});
		Map<String, Double> lUserHalfdayTourList = cAttendanceDetailsDaoImpl.mGetHalfdayVacationsAsMap(pUser.getUserId(), lFromDateStr, lToDateStr, pFrequency, new String[] {"TR"});
		
		List<Date> lUserSwipeInOutTime = cAttendanceDetailsDaoImpl.mGetUserSwipeInOutTime(pUser.getUserId(), lFromDateStr);
		boolean lIsUserInOffice = cAttendanceDetailsDaoImpl.mIsUserInOffice(pUser.getUserId());
		//User lUser = new cUserDetailsService().mGetUserById(pUser.getUserId());
		UserSwipe lUserLastSwipe 	= cAttendanceDetailsDaoImpl.mGetUserLastSwipe(pUser);

		List<Leave> lUserLeaves = cAttendanceDetailsDaoImpl.mGetUserLeavesDetails(pUser.getUserId(), lFromDateStr, lToDateStr);
		
		OfficeDetails lOfficeDetails = mGetOfficeDetailsByLocation(pUser.getHolidayId(), pOfficeDetailsList);
		
		for (String lDateStr : lDates) {
			Date lDate = null;
			try {
				lDateFormat = new SimpleDateFormat("dd/MM/yyyy");
				lDate = lDateFormat.parse(lDateStr);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int lHoliday = 0;
			double lOnDuty = 0.0;
			double lTour = 0.0;
			double lLeaveAvailed = 0.0;
			double lEffectiveHours = 0.0;
			
			if(lHolidayList.get(lDateStr)!= null) {
				lHoliday = lHolidayList.get(lDateStr);
			}
			
			if(lUserODsList.get(lDateStr)!=null) {
				lOnDuty = lUserODsList.get(lDateStr);
			}
			
			if(lUserHalfdayODsList.get(lDateStr)!=null) {
				lOnDuty = lUserHalfdayODsList.get(lDateStr);
			}
			
			if(lUserTourList.get(lDateStr)!=null) {
				lTour = lUserTourList.get(lDateStr);
			}
			
			if(lUserHalfdayTourList.get(lDateStr)!=null) {
				lTour = lUserHalfdayTourList.get(lDateStr);
			}
			
			if(lUserPresenceList.get(lDateStr)!=null) {
				//lEffectiveHours = (double)lUserPresenceList.get(lDateStr);
			}
			
			Calendar lCal = Calendar.getInstance();
			lCal.set(Calendar.HOUR_OF_DAY, 0);
			lCal.set(Calendar.MINUTE, 0);
			lCal.set(Calendar.SECOND, 0);
			lCal.set(Calendar.MILLISECOND, 0);
			//if (lDate.getTime() == lCal.getTimeInMillis()) {
			boolean lGetElapsedTime = false;
			boolean lConsiderCurrentTime = true;
			lEffectiveHours = mGetEffectiveHoursFromSwipes(pUser.getUserId(), lDate, lDate, lGetElapsedTime, lConsiderCurrentTime,  lOfficeDetails);
			//}

			if(lUserLeaveAvailedList.get(lDateStr) != null) {
				lLeaveAvailed = lUserLeaveAvailedList.get(lDateStr);
			}
			
			if(lUserHalfdayLeaveAvailedList.get(lDateStr) != null) {
				lLeaveAvailed = lUserHalfdayLeaveAvailedList.get(lDateStr);
			}
			
			DateRange dateRangeObj = mGetDateRangeDetails(pFromDate, pToDate, false, false);
			int lDayInRange = 0;
			Calendar lCalendar = Calendar.getInstance();
			lCalendar.setTime(lDate);
			int lDayOfWeek = lCalendar.get(Calendar.DAY_OF_WEEK);
			if (lDayOfWeek != Calendar.SATURDAY && lDayOfWeek != Calendar.SUNDAY) {
				lDayInRange = 1;
				String lHolidayDateStrKey = lDateFormat.format(lDate);
				if(lHolidayList.get(lHolidayDateStrKey)!= null) {
					lHoliday = lHolidayList.get(lHolidayDateStrKey);
				}			
			}
		
			int lWorkingDay = 0;
			if (lHoliday == 0) {
				lWorkingDay = lDayInRange - lHoliday; 
			}

			
			// Setting leave details
			List<Leave> lUserLeaveDetailsList = new ArrayList<Leave>();
			for (Leave lUserLeave : lUserLeaves) {
				Calendar lUserLeaveFromDate = Calendar.getInstance();
				Calendar lUserLeaveToDate = Calendar.getInstance();
				lUserLeaveFromDate.setTime(lUserLeave.getFromDate());
				lUserLeaveToDate.setTime(lUserLeave.getToDate());
				if(
				   /*(lUserLeaveFromDate.getTimeInMillis() == date.getTime() || 
					lUserLeaveToDate.getTimeInMillis() == date.getTime()) && */
				   (lUserLeaveToDate.getTimeInMillis() >= lDate.getTime() && 
				    lUserLeaveFromDate.getTimeInMillis() <= lDate.getTime())
						) {
					if (lLeaveAvailed == 0 && lWorkingDay > 0 && lUserLeaveFromDate.getTimeInMillis() > new Date().getTime()) {// Usually for future leaves
						lLeaveAvailed = lUserLeave.getPostedDays() > lDayInRange ? lDayInRange :  lUserLeave.getPostedDays();
						
					}
					if (lWorkingDay == 0) { // When Holiday, Leaves availed will not be considered
						//lLeaveAvailed = 0;
					}
					lUserLeave.setComputedLeaves(lLeaveAvailed);
					lUserLeaveDetailsList.add(lUserLeave);
					
				}
			}
			
			double lDaysPresent = 0.0;
			if (lWorkingDay > 0) {
				lDaysPresent = lWorkingDay - lLeaveAvailed;
			}
					
			double lRequiredHours = 0;
			double lDaysFractionalPart = lDaysPresent % 1;
			double lDaysIntegralPart = lDaysPresent - lDaysFractionalPart;
			
			if (lDaysPresent > 0) {
				lRequiredHours = (lDaysIntegralPart * lOfficeDetails.getMinFullDayWorkingHours()) + (lDaysFractionalPart * (lOfficeDetails.getMinHalfDayWorkingHours() * 2));
			} else {
				lRequiredHours = 0;
			}
			//double lRequiredHours = lDaysPresent * cAttendanceWebservice.MIN_FULL_DAY_WORKING_HOURS;
			double lDifference = lEffectiveHours - lRequiredHours;

			//lDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String lTodayStr = lDateFormat.format(new Date());
			
			if (lTodayStr.equalsIgnoreCase(lDateStr) && lRequiredHours != 0) {
				
				//lRequiredHours = lRequiredHours - cAttendanceWebservice.MIN_FULL_DAY_WORKING_HOURS;
			}
			
			
			if (lRequiredHours < 0) {
				lRequiredHours = 0;
			}
			AttendanceDetails lDailyAttendance = new AttendanceDetails(pUser.getUserId(), 
																	   pFrequency, 
																	   lDate, 
																	   dateRangeObj.getBeginDate(), 
																	   dateRangeObj.getEndDate(), 
																	   lWorkingDay, 
																	   lDaysPresent, 
																	   lOnDuty, 
																	   lLeaveAvailed, 
																	   lUserLeaveDetailsList, 
																	   lRequiredHours, 
																	   lEffectiveHours, 
																	   lDifference);
			lDailyAttendance.setTour(lTour);
			lDailyAttendance.setHolidays(lHoliday);
			lDailyAttendance.setSwipeInTime(lUserSwipeInOutTime.get(0));
			lDailyAttendance.setSwipeOutTime(lUserSwipeInOutTime.get(1));
			lDailyAttendance.setInOffice(lIsUserInOffice);
			lDailyAttendance.setLastSwipe(lUserLastSwipe);

			lDailyAttendanceList.add(lDailyAttendance);

		}

				return lDailyAttendanceList;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 *  All Users Attendance Services for given frequency
	 */
	
	public Map<String, List<AttendanceDetails>> mGetAllUsersMonthlyAttendanceForYear(List<User> lUsersList, Date pFromDate, Date pToDate, String pFrequency) {
		Map<String, List<AttendanceDetails>> allUsersMonthlyAttendanceList = new HashMap<String, List<AttendanceDetails>>();

		SimpleDateFormat lDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String lFromDateStr = lDateFormat.format(pFromDate);
		String lToDateStr = lDateFormat.format(pToDate);
		List<AttendanceDetails> lMonthlyAttendanceList = new ArrayList<AttendanceDetails>();
		Map<String, Integer> lHolidayList = cAttendanceDetailsDaoImpl.mGetAllUsersHolidaySchedule(lFromDateStr, lToDateStr, pFrequency);
		List<String> lUsersDateOfJoinList = cAttendanceDetailsDaoImpl.mGetAllUsersDateOfJoin(lFromDateStr, lToDateStr);
		Map<String, Double> lUserPresenceList = cAttendanceDetailsDaoImpl.mGetAllUsersPresenceAsMap(lFromDateStr, lToDateStr, pFrequency);
		Map<String, Double> lUserLeaveAvailedList = cAttendanceDetailsDaoImpl.mGetAllUsersLeavesAvailedAsMap(lFromDateStr, lToDateStr, pFrequency);
		Map<String, Double> lUserHalfdayLeaveAvailedList = cAttendanceDetailsDaoImpl.mGetAllUsersHalfdayLeavesAvailedAsMap(lFromDateStr, lToDateStr, pFrequency);
		Map<String, Double> lUserODsList = cAttendanceDetailsDaoImpl.mGetAllUsersODsAsMap( lFromDateStr, lToDateStr, pFrequency);
		List<String> lUserYearMonthList = mBuildAllUsersMonthlyAttendanceKeys(lUsersList, pFromDate, pToDate);
		Map<String, Integer> lUserHolidayIdList = new HashMap<String, Integer>();
		
		for (User lUser : lUsersList) {
			lUserHolidayIdList.put(lUser.getUserId(), lUser.getHolidayId());
		}
		String lRefUserID = "";
		for (String lUserYearMonth : lUserYearMonthList) {
			
			String lUserID = lUserYearMonth.split("&")[0];
			String lYearMonth = lUserYearMonth.split("&")[1];
			
			//if (lUsersDateOfJoinList.get(lUserID) == null) {
			
				int lHolidays = 0;
				double lOnDuty = 0.0;
				double lEffectiveHours = 0.0;
				double lLeavesAvailed = 0.0;
				double lHalfdayLeavesAvailed = 0.0;
				
				int lUserHolidayId = lUserHolidayIdList.get(lUserID);
				
				if(lHolidayList.get(lUserHolidayId + "&" + lYearMonth)!= null) {
					lHolidays = lHolidayList.get(lUserHolidayId + "&" + lYearMonth);
				}
				if(lUserODsList.get(lUserYearMonth)!=null) {
					lOnDuty = lUserODsList.get(lUserYearMonth);
				}
				if(lUserPresenceList.get(lUserYearMonth)!=null) {
					lEffectiveHours = lUserPresenceList.get(lUserYearMonth) + (lOnDuty * cAttendanceWebservice.MIN_FULL_DAY_WORKING_HOURS);
				}
				
				if(lUserLeaveAvailedList.get(lUserYearMonth)!=null) {
					lLeavesAvailed = lUserLeaveAvailedList.get(lUserYearMonth);
				}
	
				if(lUserHalfdayLeaveAvailedList.get(lUserYearMonth)!=null) {
					lHalfdayLeavesAvailed = lUserHalfdayLeaveAvailedList.get(lUserYearMonth);
				}
				
				lLeavesAvailed = lLeavesAvailed + lHalfdayLeavesAvailed;
				
				int lYear = Integer.parseInt(lYearMonth.split("-")[0]);
				int lMonth = Integer.parseInt(lYearMonth.split("-")[1]);
				Month lMonthObj = mGetMonthDetails(lYear, lMonth-1, pFromDate, pToDate, false, false);
				int daysInMonth = lMonthObj.getDays();
				int lWorkingDays = daysInMonth - lHolidays; 
				double lDaysPresent = lWorkingDays - lLeavesAvailed;
				double lRequiredHours = (lWorkingDays - lLeavesAvailed)* cAttendanceWebservice.MIN_FULL_DAY_WORKING_HOURS;
				double lDifference = lEffectiveHours - lRequiredHours;
				lMonthlyAttendanceList = allUsersMonthlyAttendanceList.get(lUserID);
				if(lMonthlyAttendanceList == null)
				{
					lMonthlyAttendanceList = new ArrayList<AttendanceDetails>();
				}
				AttendanceDetails lMonthlyAttendance = new AttendanceDetails(lUserID, pFrequency, lYear, lMonth, lMonthObj.getBeginDate(), lMonthObj.getEndDate(), lWorkingDays, lDaysPresent, lOnDuty, lLeavesAvailed, "", lRequiredHours, lEffectiveHours, lDifference);
				lMonthlyAttendanceList.add(lMonthlyAttendance);
				lRefUserID = lUserID;
				allUsersMonthlyAttendanceList.put(lRefUserID, lMonthlyAttendanceList);
			
			//}
		}
		
		/*for (String lUserId : lUsersDateOfJoinList) {
			lMonthlyAttendanceList =  new cAttendanceDetailsService().mGetUserMonthlyAttendance(lUserId, pFromDate, pToDate, pFrequency);
			allUsersMonthlyAttendanceList.put(lUserId, lMonthlyAttendanceList);
		}*/

				return allUsersMonthlyAttendanceList;
	}
	
	public Map<String, List<AttendanceDetails>> mGetAllUsersWeeklyAttendanceForYear(List<User> lUsersList, Date pFromDate, Date pToDate, String pFrequency) {
		Map<String, List<AttendanceDetails>> lAllUsersWeeklyAttendanceList = new HashMap<String, List<AttendanceDetails>>();

		SimpleDateFormat lDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String lFromDateStr = lDateFormat.format(pFromDate);
		String lToDateStr = lDateFormat.format(pToDate);
		List<AttendanceDetails> lWeeklyAttendanceList = new ArrayList<AttendanceDetails>();
		Map<String, Integer> lHolidayList = cAttendanceDetailsDaoImpl.mGetAllUsersHolidaySchedule(lFromDateStr, lToDateStr, pFrequency);
		List<String> lUsersDateOfJoinList = cAttendanceDetailsDaoImpl.mGetAllUsersDateOfJoin(lFromDateStr, lToDateStr);
		Map<String, Double> lUserPresenceList = cAttendanceDetailsDaoImpl.mGetAllUsersPresenceAsMap(lFromDateStr, lToDateStr, pFrequency);
		Map<String, Double> lUserLeaveAvailedList = cAttendanceDetailsDaoImpl.mGetAllUsersLeavesAvailedAsMap(lFromDateStr, lToDateStr, pFrequency);
		Map<String, Double> lUserHalfdayLeaveAvailedList = cAttendanceDetailsDaoImpl.mGetAllUsersHalfdayLeavesAvailedAsMap(lFromDateStr, lToDateStr, pFrequency);
		Map<String, Double> lUserODsList = cAttendanceDetailsDaoImpl.mGetAllUsersODsAsMap( lFromDateStr, lToDateStr, pFrequency);
		List<String> lUserYearWeekList = mBuildAllUsersWeeklyAttendanceKeys(lUsersList, pFromDate, pToDate);
		String lRefUserID = "";
		Map<String, Integer> lUserHolidayIdList = new HashMap<String, Integer>();
		
		for (User lUser : lUsersList) {
			lUserHolidayIdList.put(lUser.getUserId(), lUser.getHolidayId());
		}
		for (String lUserYearWeek : lUserYearWeekList) {

			String lUserID = lUserYearWeek.split("&")[0];
			String lYearWeek = lUserYearWeek.split("&")[1];
			int lHolidays = 0;
			double lOnDuty = 0.0;
			double lEffectiveHours = 0.0;
			double lLeavesAvailed = 0.0;
			double lHalfdayLeavesAvailed = 0.0;
			
			int lUserHolidayId = lUserHolidayIdList.get(lUserID);
			
			if(lHolidayList.get(lUserHolidayId + "&" + lYearWeek)!= null) {
				lHolidays = lHolidayList.get(lUserHolidayId + "&" + lYearWeek);
			}
			
			if(lUserODsList.get(lUserYearWeek)!=null) {
				lOnDuty = lUserODsList.get(lUserYearWeek);
			}
			
			if(lUserPresenceList.get(lUserYearWeek)!=null) {
				lEffectiveHours = lUserPresenceList.get(lUserYearWeek) + (lOnDuty * cAttendanceWebservice.MIN_FULL_DAY_WORKING_HOURS);
			}

			
			if(lUserLeaveAvailedList.get(lUserYearWeek)!=null) {
				lLeavesAvailed = lUserLeaveAvailedList.get(lUserYearWeek);
			}
			
			
			if(lUserHalfdayLeaveAvailedList.get(lUserYearWeek)!=null) {
				lHalfdayLeavesAvailed = lUserHalfdayLeaveAvailedList.get(lUserYearWeek);
			}
			
			lLeavesAvailed = lLeavesAvailed + lHalfdayLeavesAvailed;
			
			int lYear = Integer.parseInt(lYearWeek.split("-")[0]);
			int lWeek = Integer.parseInt(lYearWeek.split("-")[1]);
			Week lWeekObj = mGetWeekDetails(lYear, lWeek, pFromDate, pToDate, false, false);
			int lDaysInWeek = lWeekObj.getDays();
			int lWorkingDays = lDaysInWeek - lHolidays; 
			double lDaysPresent = lWorkingDays - lLeavesAvailed;
			double lRequiredHours = (lWorkingDays - lLeavesAvailed) * cAttendanceWebservice.MIN_FULL_DAY_WORKING_HOURS;
			double lDifference = lEffectiveHours - lRequiredHours;
			lWeeklyAttendanceList = lAllUsersWeeklyAttendanceList.get(lUserID);
			if(lWeeklyAttendanceList == null)
			{
				lWeeklyAttendanceList = new ArrayList<AttendanceDetails>();
			}
			AttendanceDetails lWeeklyAttendance = new AttendanceDetails(lUserID, pFrequency, lYear, lWeek, lWeekObj.getBeginDate(), lWeekObj.getEndDate(), lWorkingDays, lDaysPresent, lOnDuty, lLeavesAvailed, new ArrayList<Leave>(), lRequiredHours, lEffectiveHours, lDifference);
			
			lWeeklyAttendanceList.add(lWeeklyAttendance);
			lRefUserID = lUserID;
			lAllUsersWeeklyAttendanceList.put(lRefUserID, lWeeklyAttendanceList);
			
		}
		
		/*for (String lUserId : lUsersDateOfJoinList) {
			lWeeklyAttendanceList =  new cAttendanceDetailsService().mGetUserWeeklyAttendance(lUserId, pFromDate, pToDate, pFrequency);
			lAllUsersWeeklyAttendanceList.put(lUserId, lWeeklyAttendanceList);
		}*/

		for (User lUser : lUsersList) {
			
			AttendanceDetails lModifiedAttendance = new AttendanceDetails();
			Calendar lFromDateCalendar = Calendar.getInstance();
			lFromDateCalendar.setTime(pFromDate);
			
			lFromDateCalendar.set(Calendar.WEEK_OF_YEAR, 1);
			lFromDateCalendar.set(Calendar.YEAR, lFromDateCalendar.get(Calendar.YEAR)+1);
			lFromDateCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			Calendar lToDateCalendar = Calendar.getInstance();
			lToDateCalendar.setTime(pToDate);
			
			double lModifiedEffective = 0.0;
			if (lFromDateCalendar.get(Calendar.YEAR)+1 == lToDateCalendar.get(Calendar.YEAR)) {
				
				lFromDateStr = lDateFormat.format(lFromDateCalendar.getTime());
				
				lFromDateCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
				lFromDateCalendar.set(Calendar.DATE, 31);
				lToDateStr = lDateFormat.format(lFromDateCalendar.getTime());
				
				
				Map<String, Double> lUserWeeklyYearTransitionPresenceList = cAttendanceDetailsDaoImpl.mGetUserPresenceAsMap(lUser.getUserId(), lFromDateStr, lToDateStr, "monthly");
	
				if (lUserWeeklyYearTransitionPresenceList.get(lFromDateCalendar.get(Calendar.YEAR) + "-" + (lFromDateCalendar.get(Calendar.MONTH)+1)) != null) {
					lModifiedEffective = lUserWeeklyYearTransitionPresenceList.get(lFromDateCalendar.get(Calendar.YEAR) + "-" + (lFromDateCalendar.get(Calendar.MONTH)+1));
				}
				
				int lCount = -1;
				int lIndex = -1;
				//Map<Integer, AttendanceDetails> lModifiedDetails = new HashMap<Integer, AttendanceDetails>();

				lWeeklyAttendanceList = lAllUsersWeeklyAttendanceList.get(lUser.getUserId());
				 
				if (lWeeklyAttendanceList != null && lWeeklyAttendanceList.size()>0) {
					for (AttendanceDetails lAttendanceDetails : lWeeklyAttendanceList) {
						lCount++;

						if (lAttendanceDetails.getYear() == (lFromDateCalendar.get(Calendar.YEAR)  + 1) && lAttendanceDetails.getWeek() == 1 ) {
							double lEffectiveHours = lAttendanceDetails.getEffectiveHours() + lModifiedEffective;
							double lDifference = lAttendanceDetails.getDifference() + lModifiedEffective;
							lAttendanceDetails.setEffectiveHours(lEffectiveHours);
							lAttendanceDetails.setDifference(lDifference);
							lIndex = lCount;
							lModifiedAttendance = lAttendanceDetails;
						}
					}
					
					//lWeeklyAttendanceList.remove(count);
					if (lIndex > -1) {
						lWeeklyAttendanceList.set(lIndex, lModifiedAttendance);
						lAllUsersWeeklyAttendanceList.put(lUser.getUserId(), lWeeklyAttendanceList);
					}
				}
			}
		}

				return lAllUsersWeeklyAttendanceList;
	}
	


	public Map<String, List<AttendanceDetails>> mGetAllUsersDailyAttendanceForYear(List<User> lUsersList, Date pFromDate, Date pToDate, String pFrequency) {
		Map<String, List<AttendanceDetails>> lAllUsersDailyAttendanceList = new HashMap<String, List<AttendanceDetails>>();
		SimpleDateFormat lDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String lFromDateStr = lDateFormat.format(pFromDate);
		String lToDateStr = lDateFormat.format(pToDate);
		List<AttendanceDetails> lDailyAttendanceList = new ArrayList<AttendanceDetails>();
		Map<String, Integer> lHolidayList = cAttendanceDetailsDaoImpl.mGetAllUsersHolidaySchedule(lFromDateStr, lToDateStr, pFrequency);
		List<String> lUsersDateOfJoinList = cAttendanceDetailsDaoImpl.mGetAllUsersDateOfJoin(lFromDateStr, lToDateStr);
		Map<String, Double> lUserPresenceList = cAttendanceDetailsDaoImpl.mGetAllUsersPresenceAsMap(lFromDateStr, lToDateStr, pFrequency);
		Map<String, Double> lUserLeaveAvailedList = cAttendanceDetailsDaoImpl.mGetAllUsersLeavesAvailedAsMap(lFromDateStr, lToDateStr, pFrequency);
		Map<String, Double> lUserHalfdayLeaveAvailedList = cAttendanceDetailsDaoImpl.mGetAllUsersHalfdayLeavesAvailedAsMap(lFromDateStr, lToDateStr, pFrequency);
		Map<String, Double> lUserODsList = cAttendanceDetailsDaoImpl.mGetAllUsersODsAsMap( lFromDateStr, lToDateStr, pFrequency);
		List<String> lUserDateList = mBuildAllUsersDailyAttendanceKeys(lUsersList, pFromDate, pToDate);
		String lRefUserID = "";
		Map<String, Integer> lUserHolidayIdList = new HashMap<String, Integer>();
		for (User lUser : lUsersList) {
			lUserHolidayIdList.put(lUser.getUserId(), lUser.getHolidayId());
		}
		for (String lUserDate : lUserDateList) {
			String lUserID = lUserDate.split("&")[0];
			String lDateStr = lUserDate.split("&")[1];
			Date date = null;
			try {
				lDateFormat = new SimpleDateFormat("dd/MM/yyyy");
				date = lDateFormat.parse(lDateStr);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int lHoliday = 0;
			double lOnDuty = 0.0;
			double lEffectiveHours = 0.0;
			double lLeaveAvailed = 0.0;
			double lHalfdayLeaveAvailed = 0.0;
			
			int lUserHolidayId = lUserHolidayIdList.get(lUserID);

			if(lHolidayList.get(lUserHolidayId + "&" + lDateStr)!= null) {
				lHoliday = lHolidayList.get(lUserHolidayId + "&" + lDateStr);
			}
			
			if(lUserODsList.get(lUserDate)!=null) {
				lOnDuty = lUserODsList.get(lUserDate);
			}
			if(lUserPresenceList.get(lUserDate)!=null) {
				lEffectiveHours = lUserPresenceList.get(lUserDate) + (lOnDuty * cAttendanceWebservice.MIN_FULL_DAY_WORKING_HOURS);
			}
			
			if(lUserLeaveAvailedList.get(lUserDate)!=null) {
				lLeaveAvailed = lUserLeaveAvailedList.get(lUserDate);
			}
			
			
			if(lUserHalfdayLeaveAvailedList.get(lUserDate)!=null) {
				lHalfdayLeaveAvailed = lUserHalfdayLeaveAvailedList.get(lUserDate);
			}
			
			lLeaveAvailed = lLeaveAvailed + lHalfdayLeaveAvailed;

			int lDayInRange = 0;
			Calendar lCalendar = Calendar.getInstance();
			lCalendar.setTime(date);
			int lDayOfWeek = lCalendar.get(Calendar.DAY_OF_WEEK);
			if (lDayOfWeek != Calendar.SATURDAY && lDayOfWeek != Calendar.SUNDAY) {
				lDayInRange = 1;
			}
			int lWorkingDay = lDayInRange - lHoliday; 
			double lDaysPresent = lWorkingDay - lLeaveAvailed;
			double lRequiredHours = (lWorkingDay - lLeaveAvailed) * cAttendanceWebservice.MIN_FULL_DAY_WORKING_HOURS;
			double lDifference = lEffectiveHours - lRequiredHours;
			lDailyAttendanceList = lAllUsersDailyAttendanceList.get(lUserID);
			if(lDailyAttendanceList == null)
			{
				lDailyAttendanceList = new ArrayList<AttendanceDetails>();
			}
			AttendanceDetails lDailyAttendance = new AttendanceDetails(lUserID, pFrequency, date, date, date, lWorkingDay, lDaysPresent, lOnDuty, lLeaveAvailed, new ArrayList<Leave>(), lRequiredHours, lEffectiveHours, lDifference);
			lDailyAttendance.setHolidays(lHoliday);
			lDailyAttendanceList.add(lDailyAttendance);
			lRefUserID = lUserID;
			lAllUsersDailyAttendanceList.put(lRefUserID, lDailyAttendanceList);
			
		}
		
		/*for (String lUserId : lUsersDateOfJoinList) {
			lDailyAttendanceList =  new cAttendanceDetailsService().mGetUserDailyAttendance(lUserId, pFromDate, pToDate, pFrequency);
			lAllUsersDailyAttendanceList.put(lUserId, lDailyAttendanceList);
		}*/

				return lAllUsersDailyAttendanceList;
	}
	
	
	public static List<CorrectionEntry> mGetUserPendingEntries(String pUserID,
			Date pFromDate, Date pToDate) {

		SimpleDateFormat lDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String lFromDateStr = lDateFormat.format(pFromDate);
		String lToDateStr = lDateFormat.format(pToDate);
		
		List<CorrectionEntry> lPendingEntries = new ArrayList<CorrectionEntry>();
		List<CorrectionEntry> lPendingODs = cAttendanceDetailsDaoImpl.mGetUserPendingODs(pUserID, lFromDateStr, lToDateStr);
		
		List<CorrectionEntry> lPendingCorrections = cAttendanceDetailsDaoImpl.mGetUserPendingCorrections(pUserID, lFromDateStr, lToDateStr);
		lPendingEntries.addAll(lPendingODs);
		lPendingEntries.addAll(lPendingCorrections);
		
		return lPendingEntries;
	}

	public Map<String, AttendanceDetails> mGetSubordinatesAttendanceForDay(List<User> pUsersList, 
			                                                               Date pDate,
			                                                               JsonArray pHolidayList, 
			                                                               JsonArray pOfficeDetailsList)  throws Exception{
		Map<String, AttendanceDetails> lSubordinateAttendanceDetails = new HashMap<String, AttendanceDetails>();

		for (User lUser : pUsersList) {
			List<AttendanceDetails> lAttendanceDetails = new cAttendanceDetailsService().mGetUserDailyAttendance(lUser, pDate, pDate, "daily", pHolidayList, pOfficeDetailsList);
			if (lAttendanceDetails.size() > 0) {
				lSubordinateAttendanceDetails.put(lUser.getUserId(), lAttendanceDetails.get(0));
			} else {
				lSubordinateAttendanceDetails.put(lUser.getUserId(), new AttendanceDetails());
			}
		}
		return lSubordinateAttendanceDetails;
	}
	
	public List<UserSwipe> mGetSubordinatesLastSwipe(List<User> pUsersList) {
		List<UserSwipe> lSubordinatesLastSwipes = new ArrayList<UserSwipe>();
		for (User lUser : pUsersList) {
			UserSwipe lUserSwipe = cAttendanceDetailsDaoImpl.mGetUserLastSwipe(lUser);
			lSubordinatesLastSwipes.add(lUserSwipe);
		}
		return lSubordinatesLastSwipes;
	}
	
	private static double mGetUserShiftScheduleEffectiveHours(List<UserInOutPair> lUserInOutPairs, Date pFromDate, Date pToDate) {
		double lEffectiveHours = 0.0;
		
		for (UserInOutPair lUserInOutPair : lUserInOutPairs) {
			if (!lUserInOutPair.getType().equalsIgnoreCase(LEAVES_ID)) {  // Calculate Effective hours if the pair is not of type 'leaves'
				if (
						 lUserInOutPair.getInTime().getTime() < pFromDate.getTime() &&  // current pair's in time less than from datetime
						 (lUserInOutPair.getOutTime().getTime() > pFromDate.getTime() &&
						  lUserInOutPair.getOutTime().getTime() < pToDate.getTime())
					   ) {
						
						lEffectiveHours = lEffectiveHours + (lUserInOutPair.getOutTime().getTime() - pFromDate.getTime());
					} else if (
							  lUserInOutPair.getInTime().getTime() > pFromDate.getTime() &&
							  lUserInOutPair.getOutTime().getTime() < pToDate.getTime()
							) {
						lEffectiveHours = lEffectiveHours + (lUserInOutPair.getOutTime().getTime() - lUserInOutPair.getInTime().getTime());
					} else if (
							 lUserInOutPair.getInTime().getTime() > pFromDate.getTime() &&
							 lUserInOutPair.getInTime().getTime() < pToDate.getTime() &&
							 lUserInOutPair.getOutTime().getTime() > pToDate.getTime()
						 ) {
						lEffectiveHours = lEffectiveHours + (pToDate.getTime() - lUserInOutPair.getInTime().getTime());
					}
			}
			
		}
		
		return lEffectiveHours;
	}
	public static int mReverseDoorEntry(int pDoorEntryID) {
		int lReversedEntry = (pDoorEntryID + 1) % 2;
		return lReversedEntry;
	}

	public static List<UserInOutPair> mGetUserInOutPairs(String pUserId, Date pFromDate, Date pToDate, OfficeDetails pOfficeDetails)  throws Exception {
		
		Calendar lTodayCal = Calendar.getInstance();
		lTodayCal.set(Calendar.HOUR_OF_DAY, 0);
		lTodayCal.set(Calendar.MINUTE, 0);
		lTodayCal.set(Calendar.SECOND, 0);
		lTodayCal.set(Calendar.MILLISECOND, 0);
		
		double lTotalEffective = 0.0;
		long lCalculatedFullDayWorkingHours = pOfficeDetails.getShiftEnd().getTimeInMillis() - pOfficeDetails.getShiftStart().getTimeInMillis();
		boolean lHasShortLeaveIn = false;
		boolean lHasShortLeaveOut = false;
		boolean lHasOfficialIn = false;
		boolean lHasOfficialOut = false;
		DateFormat lDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String lFromDateStr = lDateFormat.format(pFromDate);
		String lToDateStr = lDateFormat.format(pToDate);
		
		
		List<UserSwipe> lUserSwipes = aAttendanceDetailsDao.mGetUserSwipes(pUserId, lFromDateStr, lToDateStr, pOfficeDetails);
		List<List<Object>> lUserODs = aAttendanceDetailsDao.mGetUserDailyVacations(pUserId, lFromDateStr, lToDateStr, new String []{"OD"});
		List<List<Object>> lUserTours = aAttendanceDetailsDao.mGetUserDailyVacations(pUserId, lFromDateStr, lToDateStr, new String []{"TR"});
		List<List<Object>> lUserLeaves = aAttendanceDetailsDao.mGetUserDailyVacations(pUserId, lFromDateStr, lToDateStr, LEAVE_IDS);

		DateFormat lDDDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String lCorrectionDateStr = lDDDateFormat.format(pFromDate);
		
		
		CorrectionEntry lUserCorrection = aAttendanceDetailsDao.mGetUserCorrectionType(pUserId, lCorrectionDateStr);
		
		if (lUserCorrection != null && lUserCorrection.getCorrectionIdsList() != null) {
			
			for (Object lUserCorrectionType : lUserCorrection.getCorrectionIdsList()) {
				// To check if user applied Short Leave In
				if (lUserCorrectionType != null && lUserCorrectionType.equals(SHORTLEAVE_IN_CORRECTION_ID)) {
					//lHasShortLeaveIn = true;
				}
				// To check if user applied Short Leave Out
				if (lUserCorrectionType != null && lUserCorrectionType.equals(SHORTLEAVE_OUT_CORRECTION_ID)) {
					//lHasShortLeaveOut = true;
				}
				
				// To check if user applied Official In
				if (lUserCorrectionType != null && lUserCorrectionType.equals(OFFICIAL_IN_CORRECTION_ID)) {
					lHasOfficialIn = true;
				}
				// To check if user applied Official Out
				if (lUserCorrectionType != null && lUserCorrectionType.equals(OFFICIAL_OUT_CORRECTION_ID)) {
					lHasOfficialOut = true;
				}
			}
		}
		
		
		
		List<Leave> lLeaveDetails = aAttendanceDetailsDao.mGetUserLeavesDetails(pUserId, lFromDateStr, lToDateStr);
		
		
		String lUserShiftSchedule = aAttendanceDetailsDao.mGetUserShiftSchedule(pUserId, lFromDateStr, lToDateStr);

		//aAttendanceDetailsDao.mGetUserPresenceAsMap(pUserId, lFromDateStr, lToDateStr, "daily");
		
		
		List<Date> lInOutTimes = new LinkedList<Date>();
		Map<Date, String> lInOutTypes = new HashMap<Date, String>();
		Map<Date, String> lInOutRemarks = new HashMap<Date, String>();
		List<UserInOutPair> lUserInOutPair = new LinkedList<UserInOutPair>();
		
		
		// On Duty Swipe Adjustments
		for (List<Object> lODObjects : lUserODs) {
			Date lDate = (Date) lODObjects.get(0);
			Calendar lCal = Calendar.getInstance();
			lCal.setTime(lDate);
			
			lCal.set(Calendar.SECOND, 0);
			lCal.set(Calendar.MILLISECOND, 0);
			lCal.set(Calendar.MINUTE, 0);
			String lOnDutyDuration = (String) lODObjects.get(1); // Duration
			String lOnDutyRemarks = (String) lODObjects.get(2); // Remarks
			if(lOnDutyDuration.equalsIgnoreCase("FIRST") || lOnDutyDuration.equalsIgnoreCase("BOTH")) {
				lCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
				lCal.set(Calendar.MINUTE,pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), ONDUTY_ID);
				lInOutRemarks.put(lCal.getTime(), lOnDutyRemarks);
				
				lCal.add(Calendar.MILLISECOND, (int) (lCalculatedFullDayWorkingHours/2));

				if (lOnDutyDuration.equalsIgnoreCase("BOTH")) {
					lCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftEnd().get(Calendar.HOUR_OF_DAY));
					lCal.set(Calendar.MINUTE,pOfficeDetails.getShiftEnd().get(Calendar.MINUTE));
				}
				
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), ONDUTY_ID);
				lInOutRemarks.put(lCal.getTime(), lOnDutyRemarks);
			}
			if (lOnDutyDuration.equalsIgnoreCase("SECOND") || lOnDutyDuration.equalsIgnoreCase("BOTH")) {
				lCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
				lCal.set(Calendar.MINUTE,pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
				lCal.add(Calendar.MILLISECOND, (int) (lCalculatedFullDayWorkingHours/2));
				
				if (lOnDutyDuration.equalsIgnoreCase("BOTH")) {
					lCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftEnd().get(Calendar.HOUR_OF_DAY));
					lCal.set(Calendar.MINUTE,pOfficeDetails.getShiftEnd().get(Calendar.MINUTE));
				}
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), ONDUTY_ID);
				lInOutRemarks.put(lCal.getTime(), lOnDutyRemarks);
				
				lCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftEnd().get(Calendar.HOUR_OF_DAY));
				lCal.set(Calendar.MINUTE,pOfficeDetails.getShiftEnd().get(Calendar.MINUTE));
				
				
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), ONDUTY_ID);
				lInOutRemarks.put(lCal.getTime(), lOnDutyRemarks);
			}
		}
		
		// On Tour Swipe Adjustments
		for (List<Object> lTourObjects : lUserTours) {
			Date lDate = (Date) lTourObjects.get(0);
			Calendar lCal = Calendar.getInstance();
			lCal.setTime(lDate);
			
			lCal.set(Calendar.SECOND, 0);
			lCal.set(Calendar.MILLISECOND, 0);
			lCal.set(Calendar.MINUTE, 0);
			String lTourDuration = (String) lTourObjects.get(1);  // Duration
			String lTourRemarks = (String) lTourObjects.get(2); // Remarks
			if(lTourDuration.equalsIgnoreCase("FIRST") || lTourDuration.equalsIgnoreCase("BOTH")) {
				lCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
				lCal.set(Calendar.MINUTE,pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), TOUR_ID);
				lInOutRemarks.put(lCal.getTime(), lTourRemarks);
				
				
				/*Calendar lLunchTimeCal = Calendar.getInstance();
				lLunchTimeCal.setTime(lFromCal.getTime());*/
				
				//lLunchTimeCal.set(Calendar.HOUR_OF_DAY, cAttendanceWebservice.LUNCH_BEGIN.get(Calendar.HOUR_OF_DAY));
				//lLunchTimeCal.set(Calendar.MINUTE, cAttendanceWebservice.LUNCH_BEGIN.get(Calendar.MINUTE));
				//lLunchTimeCal.set(Calendar.SECOND, cAttendanceWebservice.LUNCH_BEGIN.get(Calendar.SECOND));
				
				
/*				
				
				lLunchTimeCal.set(Calendar.HOUR_OF_DAY, pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
				lLunchTimeCal.set(Calendar.MINUTE, pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
				lLunchTimeCal.set(Calendar.SECOND, pOfficeDetails.getShiftStart().get(Calendar.SECOND));*/
				lCal.add(Calendar.MILLISECOND, (int) (lCalculatedFullDayWorkingHours/2));
				
				//lCal.set(Calendar.HOUR_OF_DAY,  cAttendanceWebservice.LUNCH_BEGIN.get(Calendar.HOUR_OF_DAY));
				//lCal.set(Calendar.MINUTE,  cAttendanceWebservice.LUNCH_BEGIN.get(Calendar.MINUTE));
				if (lTourDuration.equalsIgnoreCase("BOTH")) {
					lCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftEnd().get(Calendar.HOUR_OF_DAY));
					lCal.set(Calendar.MINUTE,pOfficeDetails.getShiftEnd().get(Calendar.MINUTE));
				}
				
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), TOUR_ID);
				lInOutRemarks.put(lCal.getTime(), lTourRemarks);
			}
			if (lTourDuration.equalsIgnoreCase("SECOND") || lTourDuration.equalsIgnoreCase("BOTH")) {
				/*lCal.set(Calendar.HOUR_OF_DAY,cAttendanceWebservice.LUNCH_END.get(Calendar.HOUR_OF_DAY));
				lCal.set(Calendar.MINUTE,cAttendanceWebservice.LUNCH_END.get(Calendar.MINUTE));*/
				lCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
				lCal.set(Calendar.MINUTE,pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
				lCal.add(Calendar.MILLISECOND, (int) (lCalculatedFullDayWorkingHours/2));
				
				if (lTourDuration.equalsIgnoreCase("BOTH")) {
					lCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftEnd().get(Calendar.HOUR_OF_DAY));
					lCal.set(Calendar.MINUTE,pOfficeDetails.getShiftEnd().get(Calendar.MINUTE));
				}
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), TOUR_ID);
				lInOutRemarks.put(lCal.getTime(), lTourRemarks);
				
				lCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftEnd().get(Calendar.HOUR_OF_DAY));
				lCal.set(Calendar.MINUTE,pOfficeDetails.getShiftEnd().get(Calendar.MINUTE));
				
				
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), TOUR_ID);
				lInOutRemarks.put(lCal.getTime(), lTourRemarks);
			}
		}
		
		boolean lIsSecondHalfOnLeave = false;
		boolean lIsFirstHalfOnLeave = false;
		// On Leave Swipe Adjustments
		for (List<Object> lUserLeaveObjects : lUserLeaves) {
			Date lDate = (Date) lUserLeaveObjects.get(0);
			
			Calendar lCal = Calendar.getInstance();
			lCal.setTime(lDate);
			
			lCal.set(Calendar.SECOND, 0);
			lCal.set(Calendar.MILLISECOND, 0);
			lCal.set(Calendar.MINUTE, 0);
			String lLeaveDuration = (String) lUserLeaveObjects.get(1);
			if(lLeaveDuration.equalsIgnoreCase("FIRST") || lLeaveDuration.equalsIgnoreCase("BOTH")) {
				Calendar lCalFirstBegin = Calendar.getInstance();
				lCalFirstBegin.setTime(lDate);
				lCalFirstBegin.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
				lCalFirstBegin.set(Calendar.MINUTE,pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
				lInOutTimes.add(lCalFirstBegin.getTime());
				lInOutTypes.put(lCalFirstBegin.getTime(), LEAVES_ID);
				
				Calendar lCalFirstEnd = Calendar.getInstance();
				lCalFirstEnd.setTime(lDate);
				//lCalFirstEnd.set(Calendar.HOUR_OF_DAY,  cAttendanceWebservice.LUNCH_BEGIN.get(Calendar.HOUR_OF_DAY));
				//lCalFirstEnd.set(Calendar.MINUTE,  cAttendanceWebservice.LUNCH_BEGIN.get(Calendar.MINUTE));
				lCalFirstEnd.set(Calendar.HOUR_OF_DAY,  pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
				lCalFirstEnd.set(Calendar.MINUTE,  pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
				lCalFirstEnd.add(Calendar.MILLISECOND, (int) (lCalculatedFullDayWorkingHours/2));
				if (lLeaveDuration.equalsIgnoreCase("BOTH")) {
					lCalFirstEnd.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftEnd().get(Calendar.HOUR_OF_DAY));
					lCalFirstEnd.set(Calendar.MINUTE,pOfficeDetails.getShiftEnd().get(Calendar.MINUTE));
				}
				lInOutTimes.add(lCalFirstEnd.getTime());
				lInOutTypes.put(lCalFirstEnd.getTime(), LEAVES_ID);
				
				lIsFirstHalfOnLeave = true;

			}
			if (lLeaveDuration.equalsIgnoreCase("SECOND") || lLeaveDuration.equalsIgnoreCase("BOTH")) {
				Calendar lCalSecondtBegin = Calendar.getInstance();
				lCalSecondtBegin.setTime(lDate);
				
				//lCalSecondtBegin.set(Calendar.HOUR_OF_DAY,cAttendanceWebservice.LUNCH_END.get(Calendar.HOUR_OF_DAY));
				//lCalSecondtBegin.set(Calendar.MINUTE,cAttendanceWebservice.LUNCH_END.get(Calendar.MINUTE));
				lCalSecondtBegin.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
				lCalSecondtBegin.set(Calendar.MINUTE,pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
				lCalSecondtBegin.add(Calendar.MILLISECOND, (int) (lCalculatedFullDayWorkingHours/2));
				
				lInOutTimes.add(lCalSecondtBegin.getTime());
				lInOutTypes.put(lCalSecondtBegin.getTime(), LEAVES_ID);
				
				Calendar lCalSecondtEnd = Calendar.getInstance();
				lCalSecondtEnd.setTime(lDate);
				lCalSecondtEnd.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftEnd().get(Calendar.HOUR_OF_DAY));
				lCalSecondtEnd.set(Calendar.MINUTE,pOfficeDetails.getShiftEnd().get(Calendar.MINUTE));
				lInOutTimes.add(lCalSecondtEnd.getTime());
				lInOutTypes.put(lCalSecondtEnd.getTime(), LEAVES_ID);
				
				lIsSecondHalfOnLeave = true;
			}
		}
		
		
		// Actual User Swipe Adjustments
		if (lUserSwipes.size() > 0) {
		
			UserSwipe lFirstSwipe = lUserSwipes.get(0);
			int lLastIOType = lUserSwipes.get(0).getIoType();
			List<UserSwipe> lUserSwipesNew = new LinkedList<>(); 
			
			if (lLastIOType == 1) {  // When first swipe is Out,
				
				UserSwipe lInitialSwipe = new UserSwipe();
				Calendar lCal = Calendar.getInstance();
				lCal.setTime(lFirstSwipe.getSwipeTime());
				lCal.set(Calendar.HOUR, 0);
				lCal.set(Calendar.MINUTE, 0);
				lCal.set(Calendar.SECOND, 0);
				lCal.set(Calendar.MILLISECOND, 1);
				lInitialSwipe.setDoorName(lFirstSwipe.getDoorName());
				lInitialSwipe.setDeviceId(lFirstSwipe.getDeviceId());
				lInitialSwipe.setName(lFirstSwipe.getName());
				lInitialSwipe.setUserId(lFirstSwipe.getUserId());
				lInitialSwipe.setSwipeTime(lCal.getTime());
				lInitialSwipe.setDoorEntryExit("Entry");
				lInitialSwipe.setIoType(0);
				
				//  then get last swipe of Prev Day
				lCal = Calendar.getInstance();
				lCal.setTime(pToDate);
				lCal.add(Calendar.DATE, -1);
				String lPrevDateStr = lDateFormat.format(lCal.getTime());
				List<UserSwipe> lPrevDaySwipes = aAttendanceDetailsDao.mGetUserSwipes(pUserId, lPrevDateStr, lPrevDateStr, pOfficeDetails);
				
				if (lPrevDaySwipes.size() > 0 && lUserShiftSchedule.equalsIgnoreCase(US_SHIFT_ID)) {
					UserSwipe lPrevDayLastSwipe = lPrevDaySwipes.get(lPrevDaySwipes.size() - 1);
					//if (lPrevDayLastSwipe.getIoType() == 0) {
						lUserSwipesNew.add(lInitialSwipe);
					//}
				}

			}
			boolean lFoundNextDay = false;
			Calendar lCalNext = Calendar.getInstance();
			lCalNext.setTime(pToDate);
			lCalNext.add(Calendar.DATE, 1);
			String lNextDateStr = lDateFormat.format(lCalNext.getTime());
			List<UserSwipe> lNextDaySwipes = aAttendanceDetailsDao.mGetUserSwipes(pUserId, lNextDateStr, lNextDateStr, pOfficeDetails);
			
			if (lNextDaySwipes.size() > 0) {
				UserSwipe lNextDayFirstSwipe = lNextDaySwipes.get(0);
				if (lNextDayFirstSwipe.getIoType() == 1) {
					lFoundNextDay = true;
				}
			}
			
			
			boolean lHasAValidEntry = false;
			boolean lFoundZero = false;
			for (UserSwipe lUserSwipe : lUserSwipes) {
				if (lUserSwipe.getIoType() == 0 && !lFoundZero) {
					lFoundZero = true;
				}
				
				
				Calendar lCalCurrent = Calendar.getInstance();
				lCalCurrent.setTime(lUserSwipe.getSwipeTime());
				lCalCurrent.set(Calendar.HOUR_OF_DAY, 0);
				lCalCurrent.set(Calendar.MINUTE, 0);
				lCalCurrent.set(Calendar.SECOND, 0);
				lCalCurrent.set(Calendar.MILLISECOND, 0);
				
				
				if (lFoundZero && (lUserSwipe.getIoType() == 1 || lTodayCal.getTimeInMillis() == lCalCurrent.getTimeInMillis()) ) {
					lHasAValidEntry = true;
					break;
				}
			
			}
			if(!lHasAValidEntry && lFoundNextDay)
				lUserSwipes.clear();
			
			if (lUserSwipes.size() > 0) {
				lUserSwipesNew.addAll(lUserSwipes);
				UserSwipe lLastSwipe = lUserSwipes.get(lUserSwipes.size()-1);
				lLastIOType = lLastSwipe.getIoType();
				if (lLastIOType == 0) {
					UserSwipe lFinalSwipe = new UserSwipe();
					Calendar lCal = Calendar.getInstance();
					lCal.setTime(lLastSwipe.getSwipeTime());
					lCal.set(Calendar.HOUR_OF_DAY, 23);
					lCal.set(Calendar.MINUTE, 59);
					lCal.set(Calendar.SECOND, 59);
					lCal.set(Calendar.MILLISECOND, 0);
					lFinalSwipe.setDoorName(lLastSwipe.getDoorName());
					lFinalSwipe.setDeviceId(lLastSwipe.getDeviceId());
					lFinalSwipe.setName(lLastSwipe.getName());
					lFinalSwipe.setUserId(lLastSwipe.getUserId());
					lFinalSwipe.setSwipeTime(lCal.getTime());
					lFinalSwipe.setDoorEntryExit("Exit");
					lFinalSwipe.setIoType(1);
					
					lCal = Calendar.getInstance();
					lCal.setTime(pToDate);
					lCal.add(Calendar.DATE, 1);
					//String lNextDateStr = lDateFormat.format(lCal.getTime());
					//List<UserSwipe> lNextDaySwipes = aAttendanceDetailsDao.mGetUserSwipes(pUserId, lNextDateStr, lNextDateStr, pDoorsToConsider);
					
					if (lNextDaySwipes.size() > 0) {
						UserSwipe lNextDayFirstSwipe = lNextDaySwipes.get(0);
						if (lNextDayFirstSwipe.getIoType() == 1 &&  lUserShiftSchedule.equalsIgnoreCase(US_SHIFT_ID)) {
							lUserSwipesNew.add(lFinalSwipe);
						}
					}
					
				}
				
				lLastIOType = 0;//lUserSwipesNew.get(0).getIoType();
				
				
				// Considers first IN and Last OUT swipes, when there are multiple wrong swipes
				/** 
				 * SWIPE TYPE ID    -  0   0   1    0   1    1
				 * SWIPE TYPE       -  IN  IN  OUT  IN  OUT  OUT
				 * SWIPE INDEX      -  0   1   2    3   4    5
				 * In such cases the first IN swipe i.e.  0th index SWIPE is considered, followed by 2nd, 3rd. And Last OUT swipe i.e. 5th index SWIPE will be considered.
				 * 
				 * FINAL SWIPES    -   0  2  3  5
				 * 
				 */
				for (int i = 0; i < lUserSwipesNew.size(); i++) {
					UserSwipe lCurUserSwipe = lUserSwipesNew.get(i);
					UserSwipe lNextUserSwipe = null;
					if ((i + 1) < lUserSwipesNew.size() && lUserSwipesNew.get(i + 1) != null) {
						lNextUserSwipe = lUserSwipesNew.get(i + 1);
					}
					// To Eliminate multiple wrong IN swipes
					if(lLastIOType == lCurUserSwipe.getIoType()) {
						
						// To consider only last valid OUT swipe (out of multiple wrong OUT swipes)
						if (lNextUserSwipe == null || !(lNextUserSwipe.getIoType() == 1 && lLastIOType == 1)) {
							lInOutTimes.add(lCurUserSwipe.getSwipeTime());
							
							
							lInOutTypes.put(lCurUserSwipe.getSwipeTime(), PRESENT_ID);
							lLastIOType = mReverseDoorEntry(lLastIOType);
						}
					}

				}
			}

		}
		int lCount = 0;
		while(lCount < lInOutTimes.size()) {
			
			// On indexing to lCount gives Swipe In time & indexing to (lCount + 1) gives Swipe Out time
			Date lInTime = null;
			Date lOutTime = null;
			
            // If Swipe In available
			if(lInOutTimes.get(lCount) != null) {
				lInTime = lInOutTimes.get(lCount);
			} else {   // If Swipe In not available or employee not yet came to office
				lInTime =  pFromDate;
			}
			
			// If Out swipe is available
			if(lCount < lInOutTimes.size()-1 && lInOutTimes.get(lCount + 1) != null) {
				lOutTime = lInOutTimes.get(lCount + 1);
				
				long lInTimeLng = lInOutTimes.get(lCount).getTime();
				long lOutTimeLng = lInOutTimes.get(lCount + 1).getTime();
				long lHoursInOffice = (( lOutTimeLng - lInTimeLng )/(1000 * 60 * 60));
				if (lHoursInOffice > 24) { // If Employee is in office more than 24 hours (Can be assumed as wrong 'In Swipe')
					lOutTime = lInTime;
				}
			} else { // If Out swipe is not available
				
				Calendar lCal = Calendar.getInstance();
				lCal.setTime(lInTime);
				lCal.set(Calendar.HOUR_OF_DAY, 0);
				lCal.set(Calendar.MINUTE, 0);
				lCal.set(Calendar.SECOND, 0);
				lCal.set(Calendar.MILLISECOND, 0);
				

				
				if (lCal.getTimeInMillis() == lTodayCal.getTimeInMillis()) {
					lOutTime = new Date();
				}
				
				// If out swipe not available, for selected day other than current day (exceptional)
				/*if (pFromDate.getTime() != lCal.getTime().getTime()) {
					lOutTime = pFromDate;
				}*/
			}
			

			List<Leave> lUserLeaveDetailsList = new ArrayList<Leave>();
			for (Leave lUserLeave : lLeaveDetails) {
				Calendar lUserLeaveFromDate = Calendar.getInstance();
				Calendar lUserLeaveToDate = Calendar.getInstance();
				lUserLeaveFromDate.setTime(lUserLeave.getFromDate());
				lUserLeaveToDate.setTime(lUserLeave.getToDate());
				if(
				   /*(lUserLeaveFromDate.getTimeInMillis() == date.getTime() || 
					lUserLeaveToDate.getTimeInMillis() == date.getTime()) && */
				   (lUserLeaveToDate.getTimeInMillis() >= pFromDate.getTime() && 
				    lUserLeaveFromDate.getTimeInMillis() <= pFromDate.getTime())
						) {
					lUserLeaveDetailsList.add(lUserLeave);
					
				}
			}
			
			double lEffectiveHours = 0;
			if (lInTime != null && lOutTime != null) {
				lEffectiveHours = lOutTime.getTime() - lInTime.getTime();
			}
			
			String lInOutType = lInOutTypes.get(lInTime);
			String lInOutRemark = lInOutRemarks.get(lInTime);

			UserInOutPair lInOutPair = new UserInOutPair(pUserId, 
					 pFromDate, 
					 lInTime, 
					 lOutTime, 
					 lEffectiveHours);
			lInOutPair.setType(lInOutType);
			lInOutPair.setRemarks(lInOutRemark);
			
			lInOutPair.setLeaveDetails(lUserLeaveDetailsList);
			if (lEffectiveHours > 0) {
				lUserInOutPair.add(lInOutPair);
			}
			lCount = lCount + 2;
			
			
		}
		
		List<UserInOutPair> lUserInOutPairFiltered = new LinkedList<UserInOutPair>();
		/*boolean lIsFirstHalfTour = false;
		boolean lIsSecondHalfTour = false;
		Calendar lDateCal = Calendar.getInstance();
		
		for (UserInOutPair lInOutTour : lUserInOutPair) {
			if(lInOutTour.getType().equals(TOUR_ID) || lInOutTour.getType().equals(ONDUTY_ID)) {
				
				lDateCal.setTime(lInOutTour.getDate());
				lDateCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
				lDateCal.set(Calendar.MINUTE,pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
				
				if (lInOutTour.getInTime().getTime() == lDateCal.getTimeInMillis()) {
					lIsFirstHalfTour = true;
				}
				
				lDateCal.set(Calendar.HOUR_OF_DAY, aOfficeDetails.getOfficeOutTime().get(Calendar.HOUR_OF_DAY));
				lDateCal.set(Calendar.MINUTE, aOfficeDetails.getOfficeOutTime().get(Calendar.MINUTE));
				
				if (lInOutTour.getOutTime().getTime() == lDateCal.getTimeInMillis()) {
					lIsSecondHalfTour = true;
				}
			}
		}
		*/
		
		
		
		if (lUserInOutPair.size() > 0) {
			// For Short Leave In or Official In
			if (!lIsFirstHalfOnLeave && (lHasShortLeaveIn || lHasOfficialIn)) {
				UserInOutPair lUserFirstInOutSwipe = lUserInOutPair.get(0);
				
				Calendar lCal = Calendar.getInstance();
				lCal.setTime(lUserFirstInOutSwipe.getDate());
				lCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
				lCal.set(Calendar.MINUTE,pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
				
				
			
				if ((lUserFirstInOutSwipe.getType().equals(PRESENT_ID)) && lUserFirstInOutSwipe.getInTime().getTime() > lCal.getTimeInMillis()) {
					double lEffectiveHours =  lUserFirstInOutSwipe.getInTime().getTime() - lCal.getTimeInMillis();
					UserInOutPair lOfficialOutPair = new UserInOutPair(pUserId, pFromDate, lCal.getTime(), lUserFirstInOutSwipe.getInTime(), lEffectiveHours);
					if (lHasShortLeaveIn) {
						lOfficialOutPair.setType(SHORTLEAVE_IN_ID);
					} else if (lHasOfficialIn) {
						lOfficialOutPair.setType(OFFICIAL_IN_ID);
					}
					
					lUserInOutPair.add(0, lOfficialOutPair);
					lOfficialOutPair.setRemarks(lUserCorrection.getRemarks());
				}
					
				
				
			}
			
			// For Official Out or Short Leave Out
			if (!lIsSecondHalfOnLeave && (lHasOfficialOut || lHasShortLeaveOut)) {
				UserInOutPair lUserLastInOutSwipe = lUserInOutPair.get(lUserInOutPair.size() - 1);
				
				
				Calendar lCal = Calendar.getInstance();
				lCal.setTime(lUserLastInOutSwipe.getDate());
				lCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftEnd().get(Calendar.HOUR_OF_DAY));
				lCal.set(Calendar.MINUTE,pOfficeDetails.getShiftEnd().get(Calendar.MINUTE));
				
				
				if (lUserLastInOutSwipe.getDate().getTime() != lTodayCal.getTimeInMillis() || 
						new Date().getTime() >pOfficeDetails.getShiftEnd().getTimeInMillis()) {
					
					if ((lUserLastInOutSwipe.getType().equals(PRESENT_ID)) && lUserLastInOutSwipe.getOutTime().getTime() < lCal.getTimeInMillis()) {
						double lEffectiveHours = lCal.getTimeInMillis() - lUserLastInOutSwipe.getOutTime().getTime();
						UserInOutPair lOfficialOutPair = new UserInOutPair(pUserId, pFromDate, lUserLastInOutSwipe.getOutTime(), lCal.getTime(), lEffectiveHours);
						if (lHasOfficialOut) {
							lOfficialOutPair.setType(OFFICIAL_OUT_ID);
						} else if (lHasShortLeaveOut) {
							lOfficialOutPair.setType(SHORTLEAVE_OUT_ID);
						}
						lOfficialOutPair.setRemarks(lUserCorrection.getRemarks());
						lUserInOutPair.add(0, lOfficialOutPair);
					}
					
				}
			}
			

		}
		
		// Set total effective hours
		/*if (lUserInOutPair.size() > 0) {
			for (UserInOutPair userInOutPair : lUserInOutPair) {
				lTotalEffective = lTotalEffective + userInOutPair.getSwipeEffectiveHours();
			}
			lUserInOutPair.get(lUserInOutPair.size() - 1).setTotalEffectiveHours(lTotalEffective);
		}*/
		
		Date lFirstInTime = null;
		Date lLastOutTime = null;
		
		
		boolean lIsFirstHalfTour = false;
		boolean lIsSecondHalfTour = false;
		Calendar lDateCal = Calendar.getInstance();

		
		
		if (lUserInOutPair.size() > 0) {
			boolean lIsInOffice = cAttendanceDetailsDaoImpl.mIsUserInOffice(pUserId);

			for (UserInOutPair lInOutPair : lUserInOutPair) {
				if(lInOutPair.getType().equals(TOUR_ID) || lInOutPair.getType().equals(ONDUTY_ID)) {
					
					lDateCal.setTime(lInOutPair.getDate());
					lDateCal.set(Calendar.HOUR_OF_DAY, pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
					lDateCal.set(Calendar.MINUTE, pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
					
					if (lInOutPair.getInTime().getTime() == lDateCal.getTimeInMillis()) {
						lIsFirstHalfTour = true;
					}
					
					lDateCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftEnd().get(Calendar.HOUR_OF_DAY));
					lDateCal.set(Calendar.MINUTE,pOfficeDetails.getShiftEnd().get(Calendar.MINUTE));
					
					if (lInOutPair.getOutTime().getTime() == lDateCal.getTimeInMillis()) {
						lIsSecondHalfTour = true;
					}
				}
				
				if (lInOutPair.getType().equalsIgnoreCase(PRESENT_ID) || lInOutPair.getType().equalsIgnoreCase(OFFICIAL_OUT_ID)) {
					//lEffectiveHours = lEffectiveHours + lInOutPair.getSwipeEffectiveHours();
				}			
			}
		
			
			if ((lIsFirstHalfTour && lIsSecondHalfTour) || (lIsFirstHalfOnLeave && lIsSecondHalfOnLeave)) {
				
				Calendar lDayBeginTimeCal = Calendar.getInstance();
				lDayBeginTimeCal.setTime(pFromDate);
				
				lDayBeginTimeCal.set(Calendar.HOUR_OF_DAY, 0);
				lDayBeginTimeCal.set(Calendar.MINUTE, 0);
				lDayBeginTimeCal.set(Calendar.SECOND, 0);


				Calendar lInTimeCal = Calendar.getInstance();
				lInTimeCal.setTime(pFromDate);
				
				lInTimeCal.set(Calendar.HOUR_OF_DAY, pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
				lInTimeCal.set(Calendar.MINUTE, pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
				lInTimeCal.set(Calendar.SECOND, pOfficeDetails.getShiftStart().get(Calendar.SECOND));
				
				lTotalEffective = lTotalEffective  + mGetUserShiftScheduleEffectiveHours(lUserInOutPair, lDayBeginTimeCal.getTime(), lInTimeCal.getTime());
				
				
				
				Calendar lOutTimeCal = Calendar.getInstance();
				lOutTimeCal.setTime(pFromDate);
				
				lOutTimeCal.set(Calendar.HOUR_OF_DAY, pOfficeDetails.getShiftEnd().get(Calendar.HOUR_OF_DAY));
				lOutTimeCal.set(Calendar.MINUTE, pOfficeDetails.getShiftEnd().get(Calendar.MINUTE));
				lOutTimeCal.set(Calendar.SECOND, pOfficeDetails.getShiftEnd().get(Calendar.SECOND));


				Calendar lDayEndTimeCal = Calendar.getInstance();
				lDayEndTimeCal.setTime(pFromDate);
				
				lDayEndTimeCal.set(Calendar.HOUR_OF_DAY, 23);
				lDayEndTimeCal.set(Calendar.MINUTE, 59);
				lDayEndTimeCal.set(Calendar.SECOND, 59);
				
				lTotalEffective = lTotalEffective + mGetUserShiftScheduleEffectiveHours(lUserInOutPair, lOutTimeCal.getTime(), lDayEndTimeCal.getTime());
				
				if (lIsFirstHalfTour && lIsSecondHalfTour) {
					lTotalEffective = lTotalEffective + lCalculatedFullDayWorkingHours;
				}
				
			} else if ((!lIsFirstHalfTour && lIsSecondHalfTour) || (!lIsFirstHalfOnLeave && lIsSecondHalfOnLeave)) {
				
				Calendar lInTimeCal = Calendar.getInstance();
				lInTimeCal.setTime(pFromDate);
				
				lInTimeCal.set(Calendar.HOUR_OF_DAY, 0);
				lInTimeCal.set(Calendar.MINUTE, 0);
				lInTimeCal.set(Calendar.SECOND, 0);


				Calendar lLunchTimeCal = Calendar.getInstance();
				lLunchTimeCal.setTime(pFromDate);
				
				
				lLunchTimeCal.set(Calendar.HOUR_OF_DAY, pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
				lLunchTimeCal.set(Calendar.MINUTE, pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
				lLunchTimeCal.set(Calendar.SECOND, pOfficeDetails.getShiftStart().get(Calendar.SECOND));
				lLunchTimeCal.add(Calendar.MILLISECOND, (int) (lCalculatedFullDayWorkingHours/2));
				
				if (!lIsFirstHalfTour && lIsSecondHalfTour) {
					lTotalEffective = lTotalEffective + (lCalculatedFullDayWorkingHours/2);
				}
				lTotalEffective = lTotalEffective + mGetUserShiftScheduleEffectiveHours(lUserInOutPair, lInTimeCal.getTime(), lLunchTimeCal.getTime());
			} else if ((lIsFirstHalfTour && !lIsSecondHalfTour) || (lIsFirstHalfOnLeave && !lIsSecondHalfOnLeave)) {
				
				Calendar lLunchTimeCal = Calendar.getInstance();
				lLunchTimeCal.setTime(pFromDate);
				
				lLunchTimeCal.set(Calendar.HOUR_OF_DAY, pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
				lLunchTimeCal.set(Calendar.MINUTE, pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
				lLunchTimeCal.set(Calendar.SECOND, pOfficeDetails.getShiftStart().get(Calendar.SECOND));
				lLunchTimeCal.add(Calendar.MILLISECOND, (int) (lCalculatedFullDayWorkingHours/2));


				Calendar lOutTimeCal = Calendar.getInstance();
				lOutTimeCal.setTime(pFromDate);
				
				lOutTimeCal.set(Calendar.HOUR_OF_DAY, 23);
				lOutTimeCal.set(Calendar.MINUTE, 59);
				lOutTimeCal.set(Calendar.SECOND, 999);
				if (lIsFirstHalfTour && !lIsSecondHalfTour) {
					lTotalEffective = lTotalEffective + (lCalculatedFullDayWorkingHours/2);
				}
				lTotalEffective = lTotalEffective + mGetUserShiftScheduleEffectiveHours(lUserInOutPair,  lLunchTimeCal.getTime(), lOutTimeCal.getTime());
			} else {

				Calendar lLunchTimeCal = Calendar.getInstance();
				lLunchTimeCal.setTime(pFromDate);
				
				lLunchTimeCal.set(Calendar.HOUR_OF_DAY, 0);
				lLunchTimeCal.set(Calendar.MINUTE, 0);
				lLunchTimeCal.set(Calendar.SECOND, 0);


				Calendar lOutTimeCal = Calendar.getInstance();
				lOutTimeCal.setTime(pFromDate);
				
				lOutTimeCal.set(Calendar.HOUR_OF_DAY, 23);
				lOutTimeCal.set(Calendar.MINUTE, 59);
				lOutTimeCal.set(Calendar.SECOND, 999);
				
				lTotalEffective = lTotalEffective + mGetUserShiftScheduleEffectiveHours(lUserInOutPair,  lLunchTimeCal.getTime(), lOutTimeCal.getTime());
			}
			
			
			Calendar lCal = Calendar.getInstance();
			lCal.set(Calendar.HOUR_OF_DAY, 0);
			lCal.set(Calendar.MINUTE, 0);
			lCal.set(Calendar.SECOND, 0);
			lCal.set(Calendar.MILLISECOND, 0);
			if (lIsInOffice) {  // If user is in office
				UserInOutPair lInOutPair = lUserInOutPair.get(lUserInOutPair.size()-1);

				Calendar lOutCal = Calendar.getInstance();
				lOutCal.setTime(lInOutPair.getOutTime());
				lOutCal.set(Calendar.HOUR_OF_DAY, 0);
				lOutCal.set(Calendar.MINUTE, 0);
				lOutCal.set(Calendar.SECOND, 0);
				lOutCal.set(Calendar.MILLISECOND, 0);
				if (lOutCal.getTimeInMillis() == lCal.getTimeInMillis()) {
					lTotalEffective = lTotalEffective + (new Date().getTime() - lInOutPair.getOutTime().getTime());
					
				}
			}
			
			lUserInOutPair.get(lUserInOutPair.size() - 1).setTotalEffectiveHours(lTotalEffective);
			
		}

		return lUserInOutPair;
	}
/*
	public static List<UserInOutPair> mGetUserInOutPairs(User pUser, Date pFromDate, Date pToDate) {
		
		DateFormat lDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String lFromDateStr = lDateFormat.format(pFromDate);
		String lToDateStr = lDateFormat.format(pToDate);
		
		List<UserSwipe> lUserSwipes = cAttendanceDetailsDaoImpl.mGetUserSwipes(pUser.getUserId(), lFromDateStr, lToDateStr);
		List<List<Object>> lUserODs = cAttendanceDetailsDaoImpl.mGetUserDailyODs(pUser.getUserId(), lFromDateStr, lToDateStr);
		List<List<Object>> lUserLeaves = cAttendanceDetailsDaoImpl.mGetUserDailyLeaves(pUser.getUserId(), lFromDateStr, lToDateStr);
		
		int lLastIOType = 0;
		List<Date> lInOutTimes = new LinkedList<Date>();
		Map<Date, String> lInOutTypes = new HashMap<Date, String>();
		List<UserInOutPair> lUserInOutPair = new LinkedList<UserInOutPair>();
		
		for (List<Object> lODObjects : lUserODs) {
			Date lDate = (Date) lODObjects.get(0);
			Calendar lCal = Calendar.getInstance();
			lCal.setTime(lDate);
			
			lCal.set(Calendar.SECOND, 0);
			lCal.set(Calendar.MILLISECOND, 0);
			lCal.set(Calendar.MINUTE, 0);
			String lODDuration = (String) lODObjects.get(1);
			if(lODDuration.equalsIgnoreCase("FIRST") || lODDuration.equalsIgnoreCase("BOTH")) {
				lCal.set(Calendar.HOUR_OF_DAY,pOfficeDetails.getShiftStart().get(Calendar.HOUR_OF_DAY));
				lCal.set(Calendar.MINUTE,pOfficeDetails.getShiftStart().get(Calendar.MINUTE));
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), "OD");
				
				lCal.set(Calendar.HOUR_OF_DAY,  cAttendanceWebservice.LUNCH_BEGIN.get(Calendar.HOUR_OF_DAY));
				lCal.set(Calendar.MINUTE,  cAttendanceWebservice.LUNCH_BEGIN.get(Calendar.MINUTE));
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), "OD");
			}
			if (lODDuration.equalsIgnoreCase("SECOND") || lODDuration.equalsIgnoreCase("BOTH")) {
				lCal.set(Calendar.HOUR_OF_DAY, LUNCH_END.get(Calendar.HOUR_OF_DAY));
				lCal.set(Calendar.MINUTE, LUNCH_END.get(Calendar.MINUTE));
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), "OD");
				
				lCal.set(Calendar.HOUR_OF_DAY, aOfficeDetails.getOfficeOutTime().get(Calendar.HOUR_OF_DAY));
				lCal.set(Calendar.MINUTE, aOfficeDetails.getOfficeOutTime().get(Calendar.MINUTE));
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), "OD");
			}
		}

		for (List<Object> lUserLeaveObjects : lUserLeaves) {
			Date lDate = (Date) lUserLeaveObjects.get(0);
			
			Calendar lCal = Calendar.getInstance();
			lCal.setTime(lDate);
			
			lCal.set(Calendar.SECOND, 0);
			lCal.set(Calendar.MILLISECOND, 0);
			lCal.set(Calendar.MINUTE, 0);
			String lLeaveDuration = (String) lUserLeaveObjects.get(1);
			if(lLeaveDuration.equalsIgnoreCase("FIRST") || lLeaveDuration.equalsIgnoreCase("BOTH")) {
				lCal.set(Calendar.HOUR_OF_DAY, 9);
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), "Leave");
				
				lCal.set(Calendar.HOUR_OF_DAY, 9);
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), "Leave");
			}
			if (lLeaveDuration.equalsIgnoreCase("SECOND") || lLeaveDuration.equalsIgnoreCase("BOTH")) {
				lCal.set(Calendar.HOUR_OF_DAY, 13);
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), "Leave");
				
				lCal.set(Calendar.HOUR_OF_DAY, 13);
				lInOutTimes.add(lCal.getTime());
				lInOutTypes.put(lCal.getTime(), "Leave");
			}
		}
		
		for (UserSwipe lUserSwipe : lUserSwipes) {
			if(lLastIOType == lUserSwipe.getIoType()) {
				lInOutTimes.add(lUserSwipe.getSwipeTime());
				lInOutTypes.put(lUserSwipe.getSwipeTime(), PRESENT_ID);
				lLastIOType = lLastIOType == 0 ? 1 : 0;
			}
		}
		int lCount = 0;
		while(lCount < lInOutTimes.size()) {
			
			// On indexing to lCount gives Swipe In time & indexing to (lCount + 1) gives Swipe Out time
			Date lInTime = new Date();
			Date lOutTime = new Date();
			
            // If Swipe In available
			if(lInOutTimes.get(lCount) != null) {
				lInTime = lInOutTimes.get(lCount);
			} else {   // If Swipe In not available or employee not yet came to office
				lInTime =  pFromDate;
			}
			
			// If Out swipe is available
			if(lCount < lInOutTimes.size()-1 && lInOutTimes.get(lCount + 1) != null) {
				lOutTime = lInOutTimes.get(lCount + 1);
			} else { // If Out swipe is not available
				Calendar lCal = Calendar.getInstance();
				lCal.set(Calendar.HOUR_OF_DAY, 0);
				lCal.set(Calendar.MINUTE, 0);
				lCal.set(Calendar.SECOND, 0);
				lCal.set(Calendar.MILLISECOND, 0);
				
				// If out swipe not available, for selected day other than current day (exceptional)
				if (pFromDate.getTime() != lCal.getTime().getTime()) {
					lOutTime = lInTime;
				}
			}
			
			double lEffectiveHours = lOutTime.getTime() - lInTime.getTime();
			String lInOutType = lInOutTypes.get(lInTime);
			UserInOutPair lInOutPair = new UserInOutPair(pUser.getUserId(), pFromDate, lInTime, lOutTime, lEffectiveHours);
			lInOutPair.setType(lInOutType);
			lUserInOutPair.add(lInOutPair);
			lCount = lCount + 2;
		}
		
		return lUserInOutPair;
	}
*/
	public Map<String, List<UserInOutPair>> mGetSubordinatesMainSwipes(
			List<String> pUsersList, Date pFromDate, Date pToDate, OfficeDetails pOfficeDetails) throws Exception{
		
		Map<String, List<UserInOutPair>> lUserInOutPairList = new HashMap<String, List<UserInOutPair>>();
		for (String lUserId : pUsersList) {
			//List<UserInOutPair> lUserInOutPair = mGetUserInOutPairs(lUser, pDate);
			List<UserInOutPair> lUserInOutPair = mGetUserInOutPairs(lUserId, pFromDate, pToDate,  pOfficeDetails);
			lUserInOutPairList.put(lUserId, lUserInOutPair);
		}
		
		return lUserInOutPairList;
	}
	
	public Map<String, List<UserInOutPair>> mGetSubordinatesMainSwipesNew(
			List<String> pUsersList, Date pFromDate, Date pToDate, OfficeDetails pOfficeDetails)  throws Exception {
		
		Map<String, List<UserInOutPair>> lUserInOutPairList = new HashMap<String, List<UserInOutPair>>();
		for (String lUserId : pUsersList) {
			
			//List<UserInOutPair> lUserInOutPair = mGetUserInOutPairs(lUser, pDate);
			List<UserInOutPair> lUserInOutPair = mGetUserInOutPairs(lUserId, pFromDate, pToDate,  pOfficeDetails);
			lUserInOutPairList.put(lUserId, lUserInOutPair);
		}
		
		return lUserInOutPairList;
	}

	
	public Map<String, List<UserInOutPair>> mGetUserMainSwipes(
			String pUserID, Date pFromDate, Date pToDate, OfficeDetails pOfficeDetails)  throws Exception{
		DateFormat lDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		Map<String, List<UserInOutPair>> lUserInOutPairList = new HashMap<String, List<UserInOutPair>>();
		Calendar lCal = Calendar.getInstance();
		lCal.setTime(pFromDate);
		while(lCal.getTimeInMillis() <= pToDate.getTime()) {
			//List<UserInOutPair> lUserInOutPair = mGetUserInOutPairs(lUser, lCal.getTime());
			List<UserInOutPair> lUserInOutPair = mGetUserInOutPairs(pUserID, pFromDate, pToDate,  pOfficeDetails);
			lUserInOutPairList.put(lDateFormat.format(lCal.getTime()), lUserInOutPair);
			lCal.add(Calendar.DATE, 1);
		}
		
		return lUserInOutPairList;
	}
	public Map<String, Integer> mGetHolidaysAsMap(User lUser, Date lFromDate,
			Date lToDate, String lFrequency, JsonArray lHolidaysJsonArray) {

		DateFormat dateFormat = new SimpleDateFormat("MMM dd, yy");
		DateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
		Map<String, Integer> lHolidaysAsMap = new HashMap<>();
		
		
		for (JsonElement lLocationHolidayJsonElm : lHolidaysJsonArray) {
			JsonObject lHolidaysJsonObj = lLocationHolidayJsonElm.getAsJsonObject();
			
			int lLocationId = lHolidaysJsonObj.get("locationId").getAsInt();
			JsonArray lHolidaysListJson = lHolidaysJsonObj.get("holidaysList").getAsJsonArray();
		
			for (JsonElement lHolidayJsonElm : lHolidaysListJson) {
				JsonObject lHolidayJsonObj = lHolidayJsonElm.getAsJsonObject();
				
				if (lUser.getHolidayId() == lLocationId) {
					String lDateStr = lHolidayJsonObj.get("date").getAsString();
					Date lDate = null;
					Calendar lCal = Calendar.getInstance();
					try {
						lDate = dateFormat.parse(lDateStr);
						lCal.setTime(lDate);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (lCal.getTimeInMillis() >= lFromDate.getTime() && lCal.getTimeInMillis() <= lToDate.getTime()) {
						String lKey = "";
						switch(lFrequency.toLowerCase()) {
						
							case FREQ_YEARLY:
							case FREQ_MONTHLY:
								lKey = lCal.get(Calendar.YEAR) + "-" + (lCal.get(Calendar.MONTH) + 1);
								break;
							case FREQ_WEEKLY:
								lKey = lCal.get(Calendar.YEAR) + "-" + lCal.get(Calendar.WEEK_OF_YEAR);
								break;
							case FREQ_DAILY:
								lKey = outputFormat.format(lDate);
								break;
							default:
								
								
						}
						
						int lHolidayCount = 1;
						if (lHolidaysAsMap.containsKey(lKey)) {
							lHolidayCount = lHolidaysAsMap.get(lKey) + 1;
						}
						lHolidaysAsMap.put(lKey, lHolidayCount);
					}
				}
			}
		}
		
		
		
		return lHolidaysAsMap;
	}
	

}
