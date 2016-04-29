package com.reports.webservices;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.reports.dao.cAttendanceDetailsDaoImpl;
import com.reports.exceptions.InvalidAttendanceConfiguration;
import com.reports.model.AttendanceDetails;
import com.reports.model.CorrectionEntry;
import com.reports.model.Holiday;
import com.reports.model.OfficeDetails;
import com.reports.model.User;
import com.reports.model.UserInOutPair;
import com.reports.model.UserSwipe;
import com.reports.service.cAttendanceDetailsService;
import com.reports.service.cUserDetailsService;

/**
 * 
 * @author kcr
 * Web service class which contains APIs to get details related to Attendance
 * "attendance" is root path for all APIs which fall under this web service
 */
@Path("/attendance")
public class cAttendanceWebservice {
	
	static final String DATE_FORMAT = "MMM dd, yy";
	SimpleDateFormat aDateFormat 	= new SimpleDateFormat(DATE_FORMAT);
	cAttendanceDetailsService aAttendanceService = new cAttendanceDetailsService();
	cAttendanceDetailsDaoImpl aAttendanceDetails = new cAttendanceDetailsDaoImpl();
	//OfficeDetails aOfficeDetails =  cAttendanceDetailsService.mGetOfficeDetails();
	
	int aHolidayId = 1;
	
	/*String DEFAULT_SHIFT_START = "9:00";
	String DEFAULT_OUTTIME = "18:30";
	String DEFAULT_LUNCH_BEGIN = "13:00";
	String DEFAULT_LUNCH_END = "13:30";
	String DEFAULT_WORKING_HOURS = "9:00";
	String DEFAULT_MIN_FULL_DAY_WORKING_HOURS = "9:00";
	String DEFAULT_MIN_HALF_DAY_WORKING_HOURS = "4:30";
	String DEFAULT_MASTER_CONTROLLERS = "1,7";*/

	public static double DAILY_WORKING_HOURS = 9.00;// = aOfficeDetails.getWorkingHours();
	public static double MIN_FULL_DAY_WORKING_HOURS = 9.00;
	public static double MIN_HALF_DAY_WORKING_HOURS = 4.5;
	public static Calendar SHIFT_START;// = mFormatTime(aOfficeDetails.getOfficeInTime());
	public static Calendar SHIFT_END;// = mFormatTime(aOfficeDetails.getOfficeOutTime());
	public static Calendar BREAK_START;// = mFormatTime(aOfficeDetails.getLunchBegin());
	public static Calendar BREAK_END;// = mFormatTime(aOfficeDetails.getLunchEnd());
	public static String[] MASTER_CONTROLLERS;// = aOfficeDetails.getMasterControllers();
	
	
	public static List<Holiday> HOLIDAYS_LIST;
	
	private static Calendar mFormatTime(String pTime) {
		int lHours = Integer.parseInt(pTime.split(":")[0]);
		int lMinutes = Integer.parseInt(pTime.split(":")[1]);
		Calendar lCal = Calendar.getInstance();
		lCal.set(Calendar.HOUR_OF_DAY, lHours);
		lCal.set(Calendar.MINUTE, lMinutes);
		return lCal;	
	}
	
	private static double mGetTimeFromString(String pTime) {
		int lHours = Integer.parseInt(pTime.split(":")[0]);
		int lMinutes = Integer.parseInt(pTime.split(":")[1]);
		
		double lMilliseconds = lHours * (1000*60*60);
		lMilliseconds = lMilliseconds + (lMinutes*(1000*60));
		
		double lFinalHours = lMilliseconds/(1000*60*60);
		
		return lFinalHours;	
	}
	
	private User mGetUserAsManager(List<User> pUsersList) {
		User lUserAsManager = null;
		for (User lUser : pUsersList) {
			
			if (lUser.isBaseUser()) {
				lUserAsManager = lUser;
			}
		}
		return lUserAsManager;
	}
	
	/**
	 * 
	 * @return OfficeDetails
	 * To get all office details
	 *//*
	private OfficeDetails mGetOfficeDetails() {
			
			String lShiftStart = SHIFT_START.get(Calendar.HOUR_OF_DAY) + ":" + SHIFT_START.get(Calendar.MINUTE);
			String lShiftEnd = SHIFT_END.get(Calendar.HOUR_OF_DAY) + ":" + SHIFT_END.get(Calendar.MINUTE);
			String lBreakStart = BREAK_START.get(Calendar.HOUR_OF_DAY) + ":" + BREAK_START.get(Calendar.MINUTE);
			String lBreakEnd = BREAK_END.get(Calendar.HOUR_OF_DAY) + ":" + BREAK_END.get(Calendar.MINUTE);
			
			OfficeDetails lTimings = new OfficeDetails(lShiftStart, 
													   lShiftEnd, 
													   lBreakStart, 
													   lBreakEnd, 
													   MIN_FULL_DAY_WORKING_HOURS,
													   MIN_HALF_DAY_WORKING_HOURS,
													   MASTER_CONTROLLERS);
			return lTimings;

		
	}*/
	
	private void mCheckValidTimeFormat(String pTimeStr) throws Exception{
		if (pTimeStr.split(":").length != 2) {
			throw new InvalidAttendanceConfiguration("Invalid Time Format. Allowed format is HH:mm ");
		}
		
	}
	/**
	 * Sets office timings and other office related details to static class variables, which can be accessed throughout the project
	 * The json data will be received from http request.
	 * @param pAttendanceDetailsJson
	 */
	public void mSetOfficeDetails(JsonObject pAttendanceDetailsJson) throws Exception {
		
		String[] lDoorsToConsider = new String[0];
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
		
		
		
		MIN_FULL_DAY_WORKING_HOURS = mGetTimeFromString(lMinFullDayHrs);
		MIN_HALF_DAY_WORKING_HOURS = mGetTimeFromString(lMinHalfDayHrs);
		SHIFT_START = mFormatTime(lShiftStart);
		SHIFT_END = mFormatTime(lShiftEnd);
		BREAK_START = mFormatTime(lBreakStart);
		BREAK_END = mFormatTime(lBreakEnd);
		
		if (SHIFT_START.getTimeInMillis() > SHIFT_END.getTimeInMillis()) {
			throw new InvalidAttendanceConfiguration("Shift Start Time Cannot be greater than Shift End Time");
		}
		
		if (BREAK_START.getTimeInMillis() > BREAK_END.getTimeInMillis()) {
			throw new InvalidAttendanceConfiguration("Break Start Time Cannot be greater than Break End Time");
		}
		
		if (!lDoorsToConsiderStr.trim().equals("")) {
			if (lDoorsToConsiderStr.contains(" ")) {
				lDoorsToConsiderStr = lDoorsToConsiderStr.replaceAll(" ", "");
			}
			
			lDoorsToConsider =lDoorsToConsiderStr.split(",");
		}
		MASTER_CONTROLLERS =  lDoorsToConsider;
		
		
	}
	
	public void mSetHolidaysList(JsonObject pHolidaysList) {
		
	}

	
	
	/**
	 * Gets the subordinates daily attendance details as AttendanceDetails object mapped to each user id.
	 * It recieves the users list(subordinates) to process the request
	 * API is usually called for Team Report
	 * @param 	lUsers
	 * @param 	lDateStr
	 * @return  Response	Response containing subordinates attendance details, 
	 * 						provided, we must supply users list(subordinates list) whose details need to be fetched
	 * 						Response object also contains copy of 
	 * 						usersList, selectedDate, officeTimings.
	 */
	@POST
	@Path("subordinates/{date}")
	@Produces("application/json")
	public Response mGetSubordinatesAttendance(@RequestParam(value="extraDetails") String lExtraDetails,
											   @PathParam(value="date") String lDateStr/*,
											   @RequestParam(value="officeDetails") String lOfficeDetails*/) {
		Map<String, Object> lResponse 	= new HashMap<String, Object>();
		try{
			Map<String, AttendanceDetails> lSubordinatesAttendance = new HashMap<String, AttendanceDetails>();
			
			Date lDate	 				= null;
			lDate 	= aDateFormat.parse(lDateStr);
			
			JsonParser lParser = new JsonParser();
			JsonObject lJsonObj = lParser.parse(lExtraDetails).getAsJsonObject();
			
			JsonArray lUsersJSON = lJsonObj.get("usersList").getAsJsonArray();
			
			List<User> lUsersList 	= new ArrayList<User>();
			for (int i = 0; i < lUsersJSON.size(); i++) {
				
				JsonObject lUserJSON = (JsonObject) lUsersJSON.get(i);
				String lUserId = lUserJSON.get("userId").getAsString();
				int lHolidayId = lUserJSON.get("holidayId").getAsInt();
				//int lHolidayId = 1;
				User lUser = new User();
				lUser.setUserId(lUserId);
				lUser.setHolidayId(lHolidayId);
				
				lUsersList.add(lUser);
			}
			
			
			//JsonObject lOfficeDetailsJsonObj = lJsonObj.get("officeDetails").getAsJsonObject();
			
			
			//mSetOfficeDetails(lOfficeDetailsJsonObj);
			
			JsonArray lHolidaysJsonArray = lJsonObj.get("holidaysDetails").getAsJsonArray();
			
			JsonArray lOfficeDetailsJsonArray = lJsonObj.get("officeDetails").getAsJsonArray();
	
			//List<Holiday> lHolidaysList = aAttendanceDetails.mGetHolidaysByID(lHolidayIDStr, lDate, lDate);
			
			lSubordinatesAttendance 	= aAttendanceService.mGetSubordinatesAttendanceForDay(lUsersList, lDate, lHolidaysJsonArray, lOfficeDetailsJsonArray);
	
			lResponse.put("success", true);
			lResponse.put("subordinatesAttendance", lSubordinatesAttendance);
			lResponse.put("usersList", lUsersList);
			lResponse.put("selectedDate", lDate);
			//lResponse.put("holidaysList", lHolidaysList);
			lResponse.put("officeDetails", aAttendanceService.mGetOfficeDetails(lOfficeDetailsJsonArray));
		}catch(Exception e) {
			lResponse.put("success", false);
			lResponse.put("exception", e.getClass());
			lResponse.put("message", e.getMessage());
			e.printStackTrace();
		}
		return Response.ok(lResponse).build();
		//String lResult 			= new Gson().toJson(lResponse);
		//return lResult;
	}
	



	/**
	 * Gets the subordinates daily swipes as UserInOutPair object mapped to each user id.
	 * It recieves the users list(subordinates) to process the request
	 * API is usually called for Timeline Report
	 * @param 	lUsers
	 * @param 	lDateStr
	 * @return  Response	Response containing subordinates daily swipes, 
	 * 						provided, we must supply users list(subordinates list) whose details need to be fetched
	 * 						Response object also contains copy of 
	 * 						usersList, selectedDate, officeTimings.
	 */
	@POST
	@Path("subordinates/main_swipes/{date}")
	@Produces("application/json")
	public Response mGetSubordinatesMainSwipes(@PathParam(value="date") String lDateStr,
											   @RequestParam(value="extraDetails") String lExtraDetails/*, 
											   @RequestParam(value="officeDetails") String lOfficeDetails*/) {
		Map<String, Object> lResponse 	= new HashMap<String, Object>();
		try{
			
		
			JsonParser lParser = new JsonParser();
			
			
			JsonObject lJsonObj = lParser.parse(lExtraDetails).getAsJsonObject();
			
			JsonArray lUsersJSON = lJsonObj.get("usersList").getAsJsonArray();
			Date lDate	 				= null;

			lDate 	= aDateFormat.parse(lDateStr);
			
			Map<String, List<UserInOutPair>> lSubordinatesMainSwipes = new HashMap<String, List<UserInOutPair>>();
			
	
			// List<User> lUsersList 	= new cUserDetailsService().mGetSubordinates(lUserID, false);
	
			
			List<String> lUsersIDList 	= new ArrayList<String>();
			List<User> lUsersList 	= new ArrayList<User>();
			
	
			for (JsonElement lUserElm : lUsersJSON) {
				JsonObject lUserJSONObj = lUserElm.getAsJsonObject();
				String lUserId = lUserJSONObj.get("userId").getAsString();
				int lHolidayId = lUserJSONObj.get("holidayId").getAsInt();
				boolean lIsBaseUser = lUserJSONObj.get("baseUser").getAsBoolean();
				String lBranch = lUserJSONObj.get("branch").getAsString();
				//int lHolidayId = 1;
				User lUser = new User();
				lUser.setUserId(lUserId);
				lUser.setHolidayId(lHolidayId);
				lUser.setBaseUser(lIsBaseUser);
				lUser.setBranch(lBranch);
				
				lUsersIDList.add(lUserId);
				lUsersList.add(lUser);
				
			}
	
			//JsonObject lOfficeDetailsJsonObj = lJsonObj.get("officeDetails").getAsJsonObject();
			//mSetOfficeDetails(lOfficeDetailsJsonObj);
			
			JsonArray lOfficeDetailsJsonArray = lJsonObj.get("officeDetails").getAsJsonArray();
			
			
			User lUserAsManager = mGetUserAsManager(lUsersList);
			
			OfficeDetails lOfficeDetails = aAttendanceService.mGetOfficeDetailsByLocation(lUserAsManager.getHolidayId(), lOfficeDetailsJsonArray);
	
			lSubordinatesMainSwipes 	= aAttendanceService.mGetSubordinatesMainSwipes(lUsersIDList, lDate, lDate, lOfficeDetails);
			
	
			
			
	
			/*Set<Integer> lHolidayIDs = new HashSet<Integer>();
			for (User lUser : lUsersList) {
				lHolidayIDs.add(lUser.getHolidayId());
			}
			
			String lHolidayIDStr = "";
			for (Integer lHolidayID : lHolidayIDs) {
				lHolidayIDStr = lHolidayIDStr + ", " + lHolidayID;
			}
			lHolidayIDStr = lHolidayIDStr.replaceFirst(",", "");
			
			List<Holiday> lHolidaysList = aAttendanceService.mGetHolidaysByID(lHolidayIDStr, lDate, lDate);
			
			*/
			lResponse.put("success", true);
			lResponse.put("subordinatesMainSwipes", lSubordinatesMainSwipes);
			lResponse.put("usersList", lUsersList);
			lResponse.put("selectedDate", lDate);
			//lResponse.put("holidaysList", lHolidaysList);
			lResponse.put("officeDetails", aAttendanceService.mGetOfficeDetails(lOfficeDetailsJsonArray));
			
		}catch(Exception e) {
			lResponse.put("success", false);
			lResponse.put("exception", e.getClass());
			lResponse.put("message", e.getMessage());
			e.printStackTrace();
		}
		return Response.ok(lResponse).build();
		//String lResult 			= new Gson().toJson(lResponse);
		//return lResult;
	}
	
	

	/**
	 * Gets the user's attendance details as AttendanceDetails object mapped to each date fragment.
	 * It recieves the user id, date range(from & to date) and period(frequency).
	 * API is usually called for Individual (User) Report
	 * @param 	lUsers
	 * @param 	lDateStr
	 * @return  Response	Response containing user's attendance details.
	 * 						Response object also contains copy of 
	 * 						userId, fromDate, toDate, frequency, officeTimings.
	 */
	@POST
	@Path("{userId}/{fromDate}/{toDate}/{frequency}")
	@Produces("application/json")
	public Response mGetUserAttendance(@PathParam(value="userId") String lUserId, 
								       @PathParam(value="fromDate") String lFromDateStr, 
								       @PathParam(value="toDate") String lToDateStr, 
								       @PathParam(value="frequency") String lFrequency,
								       @QueryParam(value="holidayId") int lHolidayId,
								       @RequestParam(value="extraDetails") String lExtraDetails) {
		
		Map<String, Object> lResponse 	= new HashMap<String, Object>();
		try{
			
			List<AttendanceDetails> userAttendance = new ArrayList<AttendanceDetails>();
			Map<String, List<UserInOutPair>> lUserMainSwipes = new HashMap<>();
			JsonParser lParser = new JsonParser();
			Date lFromDate 					= null;
			Date lToDate 					= null;

			lFromDate 	= aDateFormat.parse(lFromDateStr);
			lToDate 	= aDateFormat.parse(lToDateStr);
			
			
			if (lHolidayId == 0) {
				lHolidayId = aHolidayId;
			}
			
			
			
			
			JsonObject lJsonObj = lParser.parse(lExtraDetails).getAsJsonObject();
			
			JsonObject lUserJSON = lJsonObj.get("user").getAsJsonObject();
			
			
			User lUser = new User();
			
			lUser.setUserId(lUserJSON.get("userId").getAsString());
			lUser.setHolidayId(Integer.parseInt(lUserJSON.get("holidayId").getAsString()));
			lUser.setName(lUserJSON.get("name").getAsString());
			lUser.setBranch(lUserJSON.get("branch").getAsString());
			
			//JsonObject lOfficeDetailsJsonObj = lJsonObj.get("officeDetails").getAsJsonObject();
			
			
			//mSetOfficeDetails(lOfficeDetailsJsonObj);
			
			JsonArray lHolidaysJsonArray = lJsonObj.get("holidaysDetails").getAsJsonArray();
			
			JsonArray lOfficeDetailsJsonArray = lJsonObj.get("officeDetails").getAsJsonArray();
	
			
	
			if(lFrequency.equalsIgnoreCase("monthly")) {
				userAttendance 	= aAttendanceService.mGetUserMonthlyAttendance(lUser, lFromDate, lToDate, lFrequency, lHolidaysJsonArray, lOfficeDetailsJsonArray);
			} else if (lFrequency.equalsIgnoreCase("weekly")) {
				userAttendance 	= aAttendanceService.mGetUserWeeklyAttendance(lUser, lFromDate, lToDate, lFrequency, lHolidaysJsonArray, lOfficeDetailsJsonArray);
			} else if (lFrequency.equalsIgnoreCase("daily")) {
				userAttendance 	= aAttendanceService.mGetUserDailyAttendance(lUser, lFromDate, lToDate, lFrequency, lHolidaysJsonArray, lOfficeDetailsJsonArray);
				
				OfficeDetails lOfficeDetails = aAttendanceService.mGetOfficeDetailsByLocation(lUser.getHolidayId(), lOfficeDetailsJsonArray);
				lUserMainSwipes = aAttendanceService.mGetUserMainSwipes(lUser.getUserId(), lFromDate, lToDate, lOfficeDetails);
			}

			lResponse.put("success", true);
			lResponse.put("userAttendance", userAttendance);
			lResponse.put("userMainSwipes", lUserMainSwipes);
			lResponse.put("officeDetails", aAttendanceService.mGetOfficeDetails(lOfficeDetailsJsonArray));
			lResponse.put("userId", lUserId);
			lResponse.put("fromDate", lFromDate);
			lResponse.put("toDate", lToDate);
			lResponse.put("frequency", lFrequency);
			//String lResult 		= new Gson().toJson(lResponse);
			//return lResult;
		}catch(Exception e) {
			lResponse.put("success", false);
			lResponse.put("exception", e.getClass());
			lResponse.put("message", e.getMessage());
			e.printStackTrace();
		}
		return Response.ok(lResponse).build();
	}
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////
	
	
	@GET
	@Path("user/main_swipes/{userId}/{fromDate}/{toDate}")
	@Produces("application/json")
	public Response mGetUserMainSwipes(@PathParam(value="userId") String lUserID,
									   @PathParam(value="fromDate") String lFromDateStr,
									   @PathParam(value="toDate") String lToDateStr) {

		Date lFromDate	 				= null;
		Date lToDate	 				= null;
		try {
			lFromDate 	= aDateFormat.parse(lFromDateStr);
			lToDate 	= aDateFormat.parse(lToDateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, List<UserInOutPair>> lUserMainSwipes = new HashMap<String, List<UserInOutPair>>();
		Map<String, Object> lResponse 	= new HashMap<String, Object>();

		try {
			//lUserMainSwipes 	= aAttendanceService.mGetUserMainSwipes(lUserID, lFromDate, lToDate);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lResponse.put("userMainSwipes", lUserMainSwipes);
		//lResponse.put("officeDetails", mGetOfficeDetails());

		//String lResult 			= new Gson().toJson(lResponse);
		//return lResult;
		return Response.ok(lResponse).build();
	}
	
	
	
    /////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////
	
	@GET
	@Path("organizations")
	public String mGetAllOrganizations() {
		Map<String, Object> lResponse 	= new HashMap<String, Object>();
		List<String> lOrganizationsList = aAttendanceDetails.mGetOrganizations();
		lResponse.put("organizationsList", lOrganizationsList);
		String lResult 					= new Gson().toJson(lResponse);
		return lResult;
	}
	
	@GET
	@Path("branches")
	public String mGetAllBranches() {
		Map<String, Object> lResponse 	= new HashMap<String, Object>();
		List<String> lBranchesList 		= aAttendanceDetails.mGetBranches();
		lResponse.put("branchesList", lBranchesList);
		String lResult 					= new Gson().toJson(lResponse);
		return lResult;
	}
	
	@GET
	@Path("departments")
	public String mGetAllDepartments() {
		Map<String, Object> lResponse 	= new HashMap<String, Object>();
		List<String> lDepartmentsList 	= aAttendanceDetails.mGetDepartments();
		lResponse.put("departmentsList", lDepartmentsList);
		String lResult 					= new Gson().toJson(lResponse);
		return lResult;
	}
	
	
	@GET
	@Path("{fromDate}/{toDate}/{department}/{branch}/{organization}/{frequency}")
	public String mGetAllUsersAttendance( 
			@PathParam(value="fromDate") String lFromDateStr,
			@PathParam(value="toDate") String lToDateStr,
			@PathParam(value="department") String lDepartment,
			@PathParam(value="branch") String lBranch,
			@PathParam(value="organization") String lOrganization,
			@PathParam(value="frequency") String lFrequency) {
		
		Map<String, List<AttendanceDetails>> allUsersAttendance = new HashMap<String, List<AttendanceDetails>>();
		Map<String, Object> lResponse 	= new HashMap<String, Object>();
		Date lFromDate 					= null;
		Date lToDate	 				= null;
		try {
			lFromDate	= aDateFormat.parse(lFromDateStr);
			lToDate 	= aDateFormat.parse(lToDateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<User> lUsersList 	= new cUserDetailsService().mGetUsers(lDepartment,  lBranch, lOrganization, lFromDate, lToDate);
		if(lFrequency.equalsIgnoreCase("monthly")) {
			allUsersAttendance 	= aAttendanceService.mGetAllUsersMonthlyAttendanceForYear(lUsersList, lFromDate, lToDate, lFrequency);
			List<String> lMonthsList = aAttendanceService.mBuildMonthlyAttendanceKeys(lFromDate, lToDate);
			lResponse.put("columns", lMonthsList);
		} else if(lFrequency.equalsIgnoreCase("weekly")) {
			allUsersAttendance 	= aAttendanceService.mGetAllUsersWeeklyAttendanceForYear(lUsersList, lFromDate, lToDate, lFrequency);
			List<String> lWeeksList = aAttendanceService.mBuildWeeklyAttendanceKeys(lFromDate, lToDate);
			lResponse.put("columns", lWeeksList);
		} else if(lFrequency.equalsIgnoreCase("daily")) {
			allUsersAttendance 	= aAttendanceService.mGetAllUsersDailyAttendanceForYear(lUsersList, lFromDate, lToDate, lFrequency);
			List<String> lDateList = aAttendanceService.mBuildDailyAttendanceKeys(lFromDate, lToDate);
			lResponse.put("columns", lDateList);
		}
		lResponse.put("allUsersAttendance", allUsersAttendance);
		lResponse.put("usersList", lUsersList);
		//lResponse.put("officeTimings", mGetOfficeDetails());
		String lResult 			= new Gson().toJson(lResponse);
		return lResult;
	}
	
	
	@GET
	@Path("subordinates/last_swipe/{userId}")
	public String mGetUserLastSwipe(@PathParam(value="userId") String lUserID) {

		List<UserSwipe> lSubordinatesLastSwipe = new ArrayList<UserSwipe>();
		Map<String, Object> lResponse 	= new HashMap<String, Object>();

		List<User> lUsersList 	= new cUserDetailsService().mGetSubordinates(lUserID, false);
		
		lSubordinatesLastSwipe 	= aAttendanceService.mGetSubordinatesLastSwipe(lUsersList);

		lResponse.put("subordinatesLastSwipe", lSubordinatesLastSwipe);
		lResponse.put("usersList", lUsersList);
		//lResponse.put("officeTimings", mGetOfficeDetails());
		String lResult 			= new Gson().toJson(lResponse);
		return lResult;
	}
	


	@GET
	@Path("holidays/{userId}/{fromDate}/{toDate}")
	public String mGetHolidays(@PathParam(value="userId") String lUserID, 
							   @PathParam(value="fromDate") String lFromDateStr, 
							   @PathParam(value="toDate") String lToDateStr) {

		Map<String, Object> lResponse 	= new HashMap<String, Object>();
		
		Date lFromDate 					= null;
		Date lToDate 					= null;
		try {
			lFromDate 	= aDateFormat.parse(lFromDateStr);
			lToDate 	= aDateFormat.parse(lToDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		List<Holiday> lUserHolidays  	= aAttendanceDetails.mGetUserHolidays(lUserID, lFromDate, lToDate);
		lResponse.put("userHolidays", lUserHolidays);
		String lResult 							= new Gson().toJson(lResponse);
		return lResult;
	}
	
	@GET
	@Path("pending_entries/{userId}/{fromDate}/{toDate}")
	public String mGetPendingEntries(@PathParam(value="userId") String lUserID, @PathParam(value="fromDate") String lFromDateStr, 
										 @PathParam(value="toDate") String lToDateStr) {

		Map<String, Object> lResponse 	= new HashMap<String, Object>();
		Date lFromDate 					= null;
		Date lToDate 					= null;
		try {
			lFromDate 	= aDateFormat.parse(lFromDateStr);
			lToDate 	= aDateFormat.parse(lToDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		List<CorrectionEntry> lPendingEntries  	= aAttendanceService.mGetUserPendingEntries(lUserID, lFromDate, lToDate);
		lResponse.put("pendingEntries", lPendingEntries);
		String lResult 							= new Gson().toJson(lResponse);
		return lResult;
	}
	

	
	@POST
	@Path("strength/{fromDate}/{toDate}")
	@Consumes(MediaType.APPLICATION_JSON)
	public String mGetDailyStrength( @PathParam(value="fromDate") String lFromDateStr, 
									 @PathParam(value="toDate") String lToDateStr,
									 User lUser) {

		Map<String, Object> lResponse 	= new HashMap<String, Object>();
		Date lFromDate 					= null;
		Date lToDate 					= null;
		try {
			lFromDate 	= aDateFormat.parse(lFromDateStr);
			lToDate 	= aDateFormat.parse(lToDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Map<String, Integer> lDailyStrength  	= aAttendanceDetails.mGetDailyStrength(lFromDate, lToDate);
		lResponse.put("dailyStrength", lDailyStrength);
		String lResult 							= new Gson().toJson(lResponse);
		return lResult;
	}
	
	

}
