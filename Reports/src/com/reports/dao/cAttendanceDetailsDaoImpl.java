package com.reports.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import java.util.logging.Logger;

import com.reports.model.CorrectionEntry;
import com.reports.model.Holiday;
import com.reports.model.Leave;
import com.reports.model.OfficeDetails;
import com.reports.model.User;
import com.reports.model.UserSwipe;
import com.reports.util.JDBCConnectionUtil;

public class cAttendanceDetailsDaoImpl {
	static Connection connection = JDBCConnectionUtil.JDBCConnection;
	private final static Logger aLogger = Logger.getLogger(cAttendanceDetailsDaoImpl.class.getName());
	
	static final String TABLE_FILE_NAME = "EDS_attendance_table.properties";
	//static final String TABLE_FILE_NAME = "Ranal_attendance_table.properties";
	
	public cAttendanceDetailsDaoImpl() {
		mSetTableDetails();
	}
	
	static final String FREQ_YEARLY = "yearly";
	static final String FREQ_MONTHLY = "monthly";
	static final String FREQ_WEEKLY = "weekly";
	static final String FREQ_DAILY = "daily";
	

	public static Calendar mFormatTime(String pTime) {
		int lHours = Integer.parseInt(pTime.split(":")[0]);
		int lMinutes = Integer.parseInt(pTime.split(":")[1]);
		Calendar lCal = Calendar.getInstance();
		lCal.set(Calendar.HOUR_OF_DAY, lHours);
		lCal.set(Calendar.MINUTE, lMinutes);
		return lCal;	
	}
	/* Daily Attendance Table */
	static String DAILY_ATTENDANCE_TABLE = "EDS_DailyAttendance";
	static String DA_EMP_ID = "UserID";
	static String DA_PROCESS_DATE = "ProcessDate";
	static String DA_WORK_TIME = "worktime";
	static String DA_FIRST_HALF = "FirstHalf";
	static String DA_SECOND_HALF = "SecondHalf";
	static String DA_REASON = "PersOffReason";
	static String DA_PUNCH1_TIME = "Punch1_Time";
	static String DA_OUT_PUNCH_TIME = "OutPunch_Time";
	
	/* In Out Swipes Table */
	static String INOUT_SWIPES_TABLE = "EDS_TAInOutSwipes";
	static String IOS_EMP_ID = "UserID";
	static String IOS_SWIPE_TIME = "Edatetime";
	static String IOS_INOUT_TYPE = "IOType";
	static String IOS_DOOR_NAME = "DoorName";
	static String IOS_MASTER_CONTROLLER_ID = "MID";
	static String IOS_DOOR_CONTROLLER_ID = "DoorControllerID";
			
	/* Leave Details Table */
	static String LEAVE_DETAILS_TABLE = "EDS_LeaveData";
	static String LD_EMP_ID = "UserID";
	static String LD_LEAVE_ID = "LeaveID";
	static String LD_POSTED_DATE = "PostedDate";
	static String LD_FROM_DATE = "FromDate";
	static String LD_TO_DATE = "ToDate";
	static String LD_POSTED_DAYS = "PostedDays";
	static String LD_SANCTION_FLG = "SNCNFlg";
	static String LD_REMARKS = "Remarks";
	
	/* Holiday Schedule Table */
	static String HOLIDAY_SCHEDULE_TABLE = "EDS_HolidaySchedule";
	static String HS_HOLIDAY_DATE = "HLDDT";
	static String HS_HOLIDAY_ID = "HLDID";
	static String HS_HOLIDAY_END_DATE = "HLDDTEND";
	static String HS_HOLIDAY_NAME = "HLDName";
	
	/* User Master Table */
	static String USER_MASTER_TABLE = "EDS_UserMasterData"; // "employee"; // OLD - Ranal_UserMasterData
	static String UM_EMP_ID = "UserID";//"emp_id"; // OLD - UserID
	static String UM_NAME = "Name";//"name"; // OLD - Name
	static String UM_REPORTING_INCHARGE_ID = "ReportingIncharge1";//"reporting_incharge_id"; // OLD - ReportingIncharge1
	static String UM_EMP_ID_ENABLE = "UserIDEnbl";//"emp_id_enbl"; // OLD - UserIDEnbl
	static String UM_HOLIDAY_ID = "hldid"; //"hld_id"; // OLD - hldid
	static String UM_BRANCH = "Branch"; //"branch"; // OLD - Branch
	static String UM_JOIN_DATE = "JoinDT"; 
	
	
	public static void mSetTableDetails() {
		
		aLogger.info("Getting Table Names And Columns");
		
		InputStream lStream = cAttendanceDetailsDaoImpl.class
							  .getClassLoader()
							  .getResourceAsStream(TABLE_FILE_NAME);
		Properties lProp = new Properties();
		try {
			lProp.load(lStream);
			
			DAILY_ATTENDANCE_TABLE = lProp.getProperty("daily_attendance_table", DAILY_ATTENDANCE_TABLE);
			DA_EMP_ID = lProp.getProperty("da_emp_id", DA_EMP_ID);
			DA_PROCESS_DATE = lProp.getProperty("da_process_date", DA_PROCESS_DATE);
			DA_WORK_TIME = lProp.getProperty("da_work_time", DA_WORK_TIME);
			DA_FIRST_HALF = lProp.getProperty("da_first_half", DA_FIRST_HALF);
			DA_SECOND_HALF = lProp.getProperty("da_second_half", DA_SECOND_HALF);
			DA_PUNCH1_TIME = lProp.getProperty("da_punch1_time", DA_PUNCH1_TIME);
			DA_OUT_PUNCH_TIME = lProp.getProperty("da_out_punch_time", DA_OUT_PUNCH_TIME);
			DA_REASON = lProp.getProperty("da_reason", DA_REASON);
			
			INOUT_SWIPES_TABLE = lProp.getProperty("inout_swipes_table", INOUT_SWIPES_TABLE);
			IOS_EMP_ID = lProp.getProperty("ios_emp_id", IOS_EMP_ID);
			IOS_SWIPE_TIME = lProp.getProperty("ios_swipe_time", IOS_SWIPE_TIME);
			IOS_INOUT_TYPE = lProp.getProperty("ios_inout_type", IOS_INOUT_TYPE);
			IOS_DOOR_NAME = lProp.getProperty("ios_door_name", IOS_DOOR_NAME);
			IOS_MASTER_CONTROLLER_ID = lProp.getProperty("ios_master_controller_id", IOS_MASTER_CONTROLLER_ID);
			IOS_DOOR_CONTROLLER_ID = lProp.getProperty("ios_door_controller_id", IOS_DOOR_CONTROLLER_ID);
			
			LEAVE_DETAILS_TABLE = lProp.getProperty("leave_details_table", LEAVE_DETAILS_TABLE);
			LD_EMP_ID = lProp.getProperty("ld_emp_id", LD_EMP_ID);
			LD_LEAVE_ID = lProp.getProperty("ld_leave_id", LD_LEAVE_ID);
			LD_POSTED_DATE = lProp.getProperty("ld_posted_date", LD_POSTED_DATE);
			LD_FROM_DATE = lProp.getProperty("ld_from_date", LD_FROM_DATE);
			LD_TO_DATE = lProp.getProperty("ld_to_date", LD_TO_DATE);
			LD_POSTED_DAYS = lProp.getProperty("ld_posted_days", LD_POSTED_DAYS);
			LD_SANCTION_FLG = lProp.getProperty("ld_sanction_flg", LD_SANCTION_FLG);
			LD_REMARKS = lProp.getProperty("ld_remarks", LD_REMARKS);
			
			HOLIDAY_SCHEDULE_TABLE = lProp.getProperty("holiday_schedule_table", HOLIDAY_SCHEDULE_TABLE);
			HS_HOLIDAY_DATE = lProp.getProperty("hs_holiday_date", HS_HOLIDAY_DATE);
			HS_HOLIDAY_ID = lProp.getProperty("hs_holiday_id", HS_HOLIDAY_ID);
			HS_HOLIDAY_END_DATE = lProp.getProperty("hs_holiday_end_date", HS_HOLIDAY_END_DATE);
			HS_HOLIDAY_NAME = lProp.getProperty("hs_holiday_name", HS_HOLIDAY_NAME);
			
			
			USER_MASTER_TABLE = lProp.getProperty("user_master_table", USER_MASTER_TABLE);
			UM_EMP_ID = lProp.getProperty("um_emp_id", UM_EMP_ID);
			UM_NAME = lProp.getProperty("um_name", UM_NAME);
			UM_REPORTING_INCHARGE_ID = lProp.getProperty("um_reporting_incharge_id", UM_REPORTING_INCHARGE_ID);
			UM_EMP_ID_ENABLE = lProp.getProperty("um_emp_id_enable", UM_EMP_ID_ENABLE);
			UM_HOLIDAY_ID = lProp.getProperty("um_holiday_id", UM_HOLIDAY_ID);
			UM_BRANCH = lProp.getProperty("um_branch", UM_BRANCH);
			UM_JOIN_DATE = lProp.getProperty("um_join_date", UM_JOIN_DATE);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static List<Holiday> mGetUserHolidays(String pUserID,
			Date pFromDate, Date pToDate) {
		
		aLogger.info("Getting  Holidays List");
		Statement lStmt = null;
		List<Holiday> lUserHolidays = new ArrayList<Holiday>();
		DateFormat lDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String lFromDateStr = lDateFormat.format(pFromDate);
		String lToDateStr = lDateFormat.format(pToDate);
		try {
			
			String lQuery = "SELECT * FROM " + HOLIDAY_SCHEDULE_TABLE + " " +
					"where " + HS_HOLIDAY_ID + " = (SELECT " + UM_HOLIDAY_ID + " FROM " + USER_MASTER_TABLE + " " +
					  	"where " + UM_EMP_ID + " = '"+pUserID+"') and " + HS_HOLIDAY_DATE + "  >= '"+lFromDateStr+"' and " + HS_HOLIDAY_DATE + " <= '"+lToDateStr+"'" +
					 " ORDER BY " + HS_HOLIDAY_DATE + "";
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				String lHolidayId = rs.getString(HS_HOLIDAY_ID);
				Date lDate = rs.getDate(HS_HOLIDAY_DATE);
				Date lEndDate = rs.getDate(HS_HOLIDAY_END_DATE);
				String lName = rs.getString(HS_HOLIDAY_NAME);
				Holiday lHoliday = new Holiday(lHolidayId, lDate, lEndDate, lName);
				lUserHolidays.add(lHoliday);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUserHolidays;
	}
	
	public static List<Holiday> mGetHolidaysByID(String pHolidayIDs,
			Date pFromDate, Date pToDate) {
		Statement lStmt = null;
		List<Holiday> lUserHolidays = new ArrayList<Holiday>();
		DateFormat lDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String lFromDateStr = lDateFormat.format(pFromDate);
		String lToDateStr = lDateFormat.format(pToDate);
		try {
			
			String lQuery = "SELECT " + HS_HOLIDAY_ID + ", " + HS_HOLIDAY_DATE + ", " + HS_HOLIDAY_END_DATE + ", " + HS_HOLIDAY_NAME + " FROM " + HOLIDAY_SCHEDULE_TABLE + " " +
					"where " + HS_HOLIDAY_ID + " in (" + pHolidayIDs + ") and " + HS_HOLIDAY_DATE + "  >= '"+lFromDateStr+"' and " + HS_HOLIDAY_DATE + " <= '"+lToDateStr+"'" +
					 " ORDER BY " + HS_HOLIDAY_DATE + "";
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				String lHolidayId = rs.getString(HS_HOLIDAY_ID);
				Date lDate = rs.getDate(HS_HOLIDAY_DATE);
				Date lEndDate = rs.getDate(HS_HOLIDAY_END_DATE);
				String lName = rs.getString(HS_HOLIDAY_NAME);
				Holiday lHoliday = new Holiday(lHolidayId, lDate, lEndDate, lName);
				lUserHolidays.add(lHoliday);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUserHolidays;
	}
	
	
	/**
	 * Get Daily Employee Strength
	 */
	public static Map<String, Integer> mGetDailyStrength(Date pFromDate, Date pToDate) {
		Statement lStmt = null;
		Map<String, Integer> lDailyStrength = new HashMap<String, Integer>();
		DateFormat lDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String pFromDateStr = lDateFormat.format(pFromDate);
		String lToDateStr = lDateFormat.format(pToDate);
		try {
			String lQuery = "select CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103), count(*) " +
					"FROM " + DAILY_ATTENDANCE_TABLE + " where CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103) >= '"+ pFromDateStr +
					"' and CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103) <= '" + lToDateStr + "' and " + DA_WORK_TIME + "/60 > 4.5 group by CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)";
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				String lDateStr = rs.getString(1);
				lDailyStrength.put(lDateStr.substring(0, 10), rs.getInt(2));
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lDailyStrength;
	}
	
	/*
	 * Daily Attendance details
	 */
	
	public static Map<String, Integer> mGetMonthlyHolidaySchedule(String pUserID, int pYear, int pMonth) {
		Statement lStmt = null;
		Map<String, Integer> lHolidayList = new HashMap<String, Integer>();
		pYear = 2015;
		pMonth = 6;
		try {
			String lQuery = "SELECT " + HS_HOLIDAY_DATE + ",  COUNT(*) FROM " + HOLIDAY_SCHEDULE_TABLE + " " +
						   "where " + HS_HOLIDAY_ID + " = (SELECT " + UM_HOLIDAY_ID + " FROM " + USER_MASTER_TABLE + " " +
						   "where " + UM_EMP_ID + " = '"+pUserID+"') and MONTH(" + HS_HOLIDAY_DATE + ") = '"+ pMonth +"' and YEAR(" + HS_HOLIDAY_DATE + ") = '"+pYear+"'" +
						   " GROUP BY " + HS_HOLIDAY_DATE + "";
			
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				lHolidayList.put(rs.getString(1), rs.getInt(2));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lHolidayList;
	}
	
	public static boolean mIsUserInOffice(String pUserID) {
		aLogger.info("Checking If User in Office");
		Statement lStmt = null;
		int lInOutType = 0;
		try {			
			String lQuery = "SELECT " + IOS_EMP_ID + ", " + IOS_SWIPE_TIME + ", " + IOS_INOUT_TYPE + " FROM " + INOUT_SWIPES_TABLE + " where " + IOS_EMP_ID + "='" + pUserID + "' and " + IOS_SWIPE_TIME + " = (select MAX(" + IOS_SWIPE_TIME + ") FROM " + INOUT_SWIPES_TABLE + " where " + IOS_EMP_ID + "='" + pUserID + "' )";
			
			
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			
			while(rs.next()) {
				lInOutType = rs.getInt(IOS_INOUT_TYPE);
			}

			
			if (lInOutType == 0) {
				return true;
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return false;
	}

	public static Date mGetUserDateOfJoin(String pUserID) {

		Statement lStmt = null;
		Date dateOfJoin = null;

		try {			
			String lQuery = "SELECT " + UM_JOIN_DATE + " FROM " + USER_MASTER_TABLE + " where " + UM_EMP_ID + "='" + pUserID + "'";
			
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			
			while(rs.next()) {
					dateOfJoin = rs.getDate(1);
			}

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return dateOfJoin;
	}


	public String mGetUserShiftSchedule(String pUserId, String pFromDateStr, String pToDateStr) {
		aLogger.info("Getting User Shift Schedule");
		String lQuery = "";
	
		lQuery = "SELECT ScheduleShift " + 
				 "FROM " + DAILY_ATTENDANCE_TABLE + " where " + DA_EMP_ID + " = '"+pUserId+"'" +
				 " and CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103) >= '" + pFromDateStr + "' " +
				 " and CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103) <= '" + pToDateStr + "' ";
		
		Statement lStmt = null;
		String lScheduleShift = null;
		try {

			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);

			while(rs.next()) {
				lScheduleShift = rs.getString("ScheduleShift");
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lScheduleShift;
	}
	
	public List<List<Object>> mGetUserDailyVacations(String pUserID,
															String pFromDateStr, 
															String pToDateStr,
															String [] pVacationTypes) {
		
		aLogger.info("Getting User Vacations for given range of date and Vaction IDs");
		
		String lQuery = "";
		String lLeaveTypeBuilder = "";
		for (int i = 0; i < pVacationTypes.length; i++) {
			String lVacationType = pVacationTypes[i];
			lLeaveTypeBuilder += "(" + DA_FIRST_HALF + " = '" + lVacationType + "' or " + DA_SECOND_HALF + " = '" + lVacationType + "')" ;
			if (i != pVacationTypes.length-1) {
				lLeaveTypeBuilder += " or ";
			}
			
		}
	
		lQuery = "SELECT CONVERT(DATETIME, " + 
		         DA_PROCESS_DATE + ", 103), " + 
		         DA_FIRST_HALF +
				 ", " + DA_SECOND_HALF + 
				 ", " + DA_REASON + 
				" FROM " + DAILY_ATTENDANCE_TABLE + " where " + DA_EMP_ID + " = '"+pUserID+"'" +
				" and CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103) >= '" + pFromDateStr + "' " +
				" and CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103) <= '" + pToDateStr + "' " +
				" and " +
				"(" +
					lLeaveTypeBuilder +
				")";
		
		Statement lStmt = null;
		List<List<Object>> lUserDailyVacation = new LinkedList<List<Object>>();

		try {

			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);

			while(rs.next()) {
				List<Object> lVacationObjects = new LinkedList<Object>();
	
				Date lProcessdate = rs.getDate(1);
				String lLeaveDuration = "";
				String lFirstHalf = rs.getString(2);
				String lSecondHalf = rs.getString(3);
				String lRemarks = rs.getString(4);
				int lLeaveHalves = 0;

				if (lFirstHalf != null) {
					for (String pVacationType : pVacationTypes) {
						if(lFirstHalf.equalsIgnoreCase(pVacationType)) {
							lLeaveDuration = "FIRST";
							lLeaveHalves++;
							break;
						}
					}
				}
				
				if (lSecondHalf != null) {
					for (String pVacationType : pVacationTypes) {
						if(lSecondHalf.equalsIgnoreCase(pVacationType)) {
							lLeaveDuration = "SECOND";
							lLeaveHalves++;
							break;
						}
					}
				}
				
				if (lLeaveHalves == 2) {
					lLeaveDuration = "BOTH";
				}
				lVacationObjects.add(lProcessdate);
				lVacationObjects.add(lLeaveDuration);
				lVacationObjects.add(lRemarks);
				lUserDailyVacation.add(lVacationObjects);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUserDailyVacation;

	}
	
	public CorrectionEntry mGetUserCorrectionType(String pUserID, 
									  String pDateStr) {
		Statement lStmt = null;
		List<Object> lCorrectionTypesList = new ArrayList<>();
		CorrectionEntry lCorrectionEntry = new CorrectionEntry();
		try {
			String query = "select * " +
					"FROM " + DAILY_ATTENDANCE_TABLE + " where " + DA_EMP_ID + "='" + pUserID + "' and " + DA_PROCESS_DATE + "='" + pDateStr + "'";
			
			//query = "select distinct ScheduleShift " +
			//		"FROM EDS_DailyAttendance";
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(query);
			while(rs.next()) {
				
				for (int i = 1; i <= 12; i++) {
					lCorrectionTypesList.add(rs.getString("SPFID"+i));
				}
				
				lCorrectionEntry.setUserId(pUserID);
				lCorrectionEntry.setRemarks(rs.getString("PersOffReason"));
				lCorrectionEntry.setCorrectionIdsList(lCorrectionTypesList);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lCorrectionEntry;
	}
	
	public static boolean mIsLeave(String lSession) {
		return false;
	}

	/*
	 * Monthly Attendance details
	 */
	
	public static Map<String, Integer> mGetHolidaySchedule(int pHolidayID, 
														   String pFromDateStr, 
														   String pToDateStr, 
														   String pFrequency) {
		Statement lStmt = null;
		Map<String, Integer> lHolidayList = new HashMap<String, Integer>();
		String lQuery = "";
		try {
			
			lQuery =    "where " + HS_HOLIDAY_ID + " = '"+pHolidayID+"' and (" + HS_HOLIDAY_DATE + "  between '"+pFromDateStr+"' and '"+pToDateStr+" 23:59:59.999') " +
					    " and (DATEPART(DW, " + HS_HOLIDAY_DATE + ") != 1 and DATEPART(DW, " + HS_HOLIDAY_DATE + ") != 7) ";
			
			

			if(pFrequency.equalsIgnoreCase("yearly")) {
				lQuery = "";
				
			} else if(pFrequency.equalsIgnoreCase("monthly")) {
				lQuery = "SELECT YEAR(" + HS_HOLIDAY_DATE + "), MONTH(" + HS_HOLIDAY_DATE + "),  COUNT(*) FROM " + HOLIDAY_SCHEDULE_TABLE + " " +
					      lQuery +
						  " GROUP BY YEAR(" + HS_HOLIDAY_DATE + "), MONTH(" + HS_HOLIDAY_DATE + ")";
				
				
			} else if(pFrequency.equalsIgnoreCase("weekly")) {
				lQuery = "SELECT YEAR(" + HS_HOLIDAY_DATE + "), DATEPART(ww, " + HS_HOLIDAY_DATE + "), COUNT(*) FROM " + HOLIDAY_SCHEDULE_TABLE + " " +
						 lQuery +
						 " GROUP BY YEAR(" + HS_HOLIDAY_DATE + "), DATEPART(ww, " + HS_HOLIDAY_DATE + ")";
				
				
			} else if(pFrequency.equalsIgnoreCase("daily")) {
				lQuery = "SELECT CONVERT(VARCHAR(25), " + HS_HOLIDAY_DATE + ", 103) as Date_Format,  COUNT(*) FROM " + HOLIDAY_SCHEDULE_TABLE + " " +
						 lQuery +
						 " GROUP BY CONVERT(VARCHAR(25), " + HS_HOLIDAY_DATE + ", 103)";
				
			}

			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				String lDateStr = "";
				int lHolidays = 0;
				if (pFrequency.equalsIgnoreCase("daily")) {
					lDateStr = rs.getString(1);
					lHolidays = rs.getInt(2);
				} else {
					lDateStr = rs.getString(1) + "-" + rs.getString(2);
					lHolidays = rs.getInt(3);
				}
				
				lHolidayList.put(lDateStr, lHolidays);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lHolidayList;
	}
	
	public static Map<String, Double> mGetUserPresenceAsMap(String pUserID,
			String pFromDateStr, String pToDateStr, String pFrequency) {
		
		Statement lStmt = null;
		Map<String, Double> lUserAttendance = new HashMap<String, Double>();
		String lQuery = "";

		try {
			lQuery = "FROM " + DAILY_ATTENDANCE_TABLE + " where " + DA_EMP_ID + " = '"+pUserID+"'" +		
			" and (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103) between '"+pFromDateStr+"' and '"+pToDateStr+"')";

			if(pFrequency.equalsIgnoreCase("yearly")) {
				lQuery = "";
				
			} else if(pFrequency.equalsIgnoreCase("monthly")) {
				lQuery = "SELECT YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)) as year" +
						", MONTH (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), (SUM(" + DA_WORK_TIME + ")/60) " + 
						lQuery +
						" group by YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), " +
						" MONTH (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103))";
				
			} else if(pFrequency.equalsIgnoreCase("weekly")) {
				lQuery = "SELECT YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), " +
						"DATEPART(ww, (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103))), (SUM(" + DA_WORK_TIME + ")/60) " + 
						lQuery +
						" group by YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), " +
						" DATEPART(ww, (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)))";
			} else if(pFrequency.equalsIgnoreCase("daily")) {
				lQuery = "SELECT " + DA_PROCESS_DATE + ", (SUM(" + DA_WORK_TIME + ")/60) " + 
						lQuery +
						" group by " + DA_PROCESS_DATE + "";
			}
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			double lEffectiveHours = 0.0;
			while(rs.next()) {
				String lYearMonthStr = "";
				
				if (pFrequency.equalsIgnoreCase("daily")) {
					lYearMonthStr = rs.getString(1);
					lEffectiveHours = rs.getDouble(2);
				} else {
					lYearMonthStr = rs.getString(1) + "-" + rs.getString(2);
					lEffectiveHours = rs.getDouble(3);
				}
				
				lUserAttendance.put(lYearMonthStr, lEffectiveHours);
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUserAttendance;
	}
	
	public static Map<String, Double> mGetVacationsAsMap(String pUserID,
														 String pFromDateStr, 
														 String pToDateStr, 
														 String pFrequency,
														 String[] pVacationTypes) {
		
		aLogger.info("Getting User Fullday Vacations based on frequency, date range, vaction IDs");

		Statement lStmt = null;
		Map<String, Double> lUserAttendance = new HashMap<String, Double>();
		String lQuery = "";
		String lLeaveTypeBuilder = "";
		for (int i = 0; i < pVacationTypes.length; i++) {
			String lVacationType = pVacationTypes[i];
			lLeaveTypeBuilder += "(" + DA_FIRST_HALF + " = '" + lVacationType
					+ "' and " + DA_SECOND_HALF + " = '" + lVacationType + "')";
			if (i != pVacationTypes.length - 1) {
				lLeaveTypeBuilder += " or ";
			}

		}

		try {
			lQuery = " FROM " + DAILY_ATTENDANCE_TABLE + " where " + DA_EMP_ID + " = '" + pUserID
					+ "'"
					+ " and (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103) between '"
					+ pFromDateStr + "' and '" + pToDateStr + "')" + " and "
					+ "(" + lLeaveTypeBuilder + ")";
			
			switch(pFrequency.toLowerCase()) {
				case FREQ_YEARLY:
				case FREQ_MONTHLY:
					lQuery = "SELECT YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), "
							 + "MONTH (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), COUNT(*)"
							 + lQuery
							 + " group by YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), "
							 + " MONTH (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103))";
					break;
				case FREQ_WEEKLY:
					lQuery = "SELECT YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), "
							 + "DATEPART(ww, (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103))), COUNT(*)"
							 + lQuery
							 + " group by YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), "
							 + " DATEPART(ww, (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)))";
					break;
				case FREQ_DAILY:
					lQuery = "SELECT " + DA_PROCESS_DATE + ", COUNT(*)" + lQuery
							 + " group by " + DA_PROCESS_DATE + "";
					break;
					
				default:
					lQuery = "SELECT " + DA_PROCESS_DATE + ", COUNT(*)" + lQuery
					 		 + " group by " + DA_PROCESS_DATE + "";
			}

			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);

			while (rs.next()) {
				String lYearMonthStr = "";
				double lLeavesAvailed = 0.0;
				if (pFrequency.equalsIgnoreCase("daily")) {
					lYearMonthStr = rs.getString(1);
					lLeavesAvailed = rs.getDouble(2);
				} else {
					lYearMonthStr = rs.getString(1) + "-" + rs.getString(2);
					lLeavesAvailed = rs.getDouble(3);
				}
				lUserAttendance.put(lYearMonthStr, lLeavesAvailed);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUserAttendance;
}

public static Map<String, Double> mGetHalfdayVacationsAsMap(String pUserID,
																	String pFromDateStr, 
																	String pToDateStr, 
																	String pFrequency, 
																	String[] pVacationTypes) {

		aLogger.info("Getting User Halfday Vacations based on frequency, date range, vaction IDs");
		Statement lStmt = null;
		Map<String, Double> lUserAttendance = new HashMap<String, Double>();
		String lQuery = "";
		String lLeaveTypeBuilder = "";
		for (int i = 0; i < pVacationTypes.length; i++) {
			String lVacationType = pVacationTypes[i];
			// lLeaveTypeBuilder += "(FirstHalf = '" + lVacationType +
			// "' and SecondHalf = '" + lVacationType + "')" ;
			lLeaveTypeBuilder += "(" + DA_FIRST_HALF + " != '" + lVacationType
					+ "' and " + DA_SECOND_HALF + " = '" + lVacationType
					+ "') or (" + DA_FIRST_HALF + " = '" + lVacationType
					+ "' and " + DA_SECOND_HALF + " != '" + lVacationType + "')";
			if (i != pVacationTypes.length - 1) {
				lLeaveTypeBuilder += " or ";
			}

		}
		try {

			lQuery = " FROM " + DAILY_ATTENDANCE_TABLE + " where " + DA_EMP_ID + " = '" + pUserID
					+ "'"
					+ " and (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103) between '"
					+ pFromDateStr + "' and '" + pToDateStr + " 23:59:59.999')"
					+ " and " + "(" + lLeaveTypeBuilder + ")";
			
			switch(pFrequency.toLowerCase()) {
				case FREQ_YEARLY:
				case FREQ_MONTHLY:
					lQuery = "SELECT YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), "
							 + "MONTH (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), 0.5*COUNT(*)"
							 + lQuery
							 + " group by YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), "
							 + "MONTH (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103))";
					break;
				case FREQ_WEEKLY:
					lQuery = "SELECT YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), "
							 + "DATEPART(ww, (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103))), 0.5*COUNT(*)"
							 + lQuery
							 + " group by YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), "
							 + " DATEPART(ww, (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)))";
					break;
				case FREQ_DAILY:
					lQuery = "SELECT " + DA_PROCESS_DATE + ", 0.5*COUNT(*)" + lQuery
							 + " group by " + DA_PROCESS_DATE + "";
					break;
					
				default:
					lQuery = "SELECT " + DA_PROCESS_DATE + ", 0.5*COUNT(*)" + lQuery
					 		 + " group by " + DA_PROCESS_DATE + "";
			}


			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);

			while (rs.next()) {
				String lYearMonthStr = "";
				double lLeavesAvailed = 0.0;
				if (pFrequency.equalsIgnoreCase("daily")) {
					lYearMonthStr = rs.getString(1);
					lLeavesAvailed = rs.getDouble(2);
				} else {
					lYearMonthStr = rs.getString(1) + "-" + rs.getString(2);
					lLeavesAvailed = rs.getDouble(3);
				}
				lUserAttendance.put(lYearMonthStr, lLeavesAvailed);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUserAttendance;
}

	public static List<Leave> mGetUserLeavesDetails(String pUserID,
			String pFromDateStr, String pToDateStr) {
		
		aLogger.info("Getting User Leave Details for  date range");
		Statement lStmt = null;
		List<Leave> lUserLeaves = new ArrayList<Leave>();
		
		try {
			String lQuery = "SELECT " + LD_EMP_ID + ", " + LD_LEAVE_ID + ", " + LD_FROM_DATE + ", " + LD_TO_DATE + ", " + LD_POSTED_DAYS + ", " + LD_REMARKS + "" +
					" FROM " + LEAVE_DETAILS_TABLE + " where " + LD_EMP_ID + " = '"+pUserID+"'" +
					" and (" + LD_LEAVE_ID + " != 'OD' and " + LD_LEAVE_ID + " != 'TR') and " + LD_SANCTION_FLG + " != 0";

			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				String leaveId = rs.getString(2);	
				Date lFromDate = rs.getDate(3);
				Date lToDate = rs.getDate(4);	
				double postedDays = rs.getDouble(5);
				String remarks = rs.getString(6);	
				Leave lLeave = new Leave(leaveId, lFromDate, lToDate, postedDays, remarks);
				
				if (lLeave.getPostedDays() > 0 && !lUserLeaves.contains(lLeave)) {
					lUserLeaves.add(lLeave);
				}
				
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUserLeaves;
	}
	
	public static List<Leave> mGetUserLeavesDetailsBound(String pUserID,
			String pFromDateStr, String pToDateStr) {
		
		aLogger.info("Getting User Leave Details for  date range");
		Statement lStmt = null;
		List<Leave> lUserLeaves = new ArrayList<Leave>();
		
		try {
			String lQuery = "SELECT " + LD_EMP_ID + ", " + LD_LEAVE_ID + ", " + LD_FROM_DATE + ", " + LD_TO_DATE + ", " + LD_POSTED_DAYS + ", " + LD_REMARKS + "" +
					" FROM " + LEAVE_DETAILS_TABLE + " where " + LD_EMP_ID + " = '"+pUserID+"'" +
					" and (" + LD_LEAVE_ID + " != 'OD' and " + LD_LEAVE_ID + " != 'TR') and " + LD_SANCTION_FLG + " != 0" + 
					" AND " +
					"( " +
					
					"( " +
						"( " +
							LD_FROM_DATE + " < '" + pFromDateStr + " 23:59:59.999' AND " + LD_FROM_DATE + " < '" + pToDateStr + " 23:59:59.999' " +
						") " +
						" AND " + 
						"( " +
							LD_TO_DATE + " >= '" + pFromDateStr + " 00:00:00.000' AND " + LD_TO_DATE + " < '" + pToDateStr + " 23:59:59.999' " +
						") " +
					") " +
					
					" OR " +
					
					"( " +
						"( " +
							LD_FROM_DATE + " >= '" + pFromDateStr + " 00:00:00.000' AND " + LD_FROM_DATE + " < '" + pToDateStr + " 23:59:59.999' " +
						") " +
						" AND " + 
						"( " +
							LD_TO_DATE + " > '" + pFromDateStr + " 00:00:00.000' AND " + LD_TO_DATE + " <= '" + pToDateStr + " 23:59:59.999' " +
						") " +
					") " +
					
					" OR " +

					"( " +
						"( " +
							LD_FROM_DATE + " > '" + pFromDateStr + " 00:00:00.000' AND " + LD_FROM_DATE + " <= '" + pToDateStr + " 23:59:59.999' " +
						") " +
						" AND " + 
						"( " +
							LD_TO_DATE + " > '" + pFromDateStr + " 00:00:00.000' AND " + LD_TO_DATE + " > '" + pToDateStr + " 00:00:00.000' " +
						") " +
					") " +
					
					") "
					
					;

			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				String leaveId = rs.getString(2);	
				Date lFromDate = rs.getDate(3);
				Date lToDate = rs.getDate(4);	
				double postedDays = rs.getDouble(5);
				String remarks = rs.getString(6);	
				Leave lLeave = new Leave(leaveId, lFromDate, lToDate, postedDays, remarks);
				
				if (lLeave.getPostedDays() > 0 && !lUserLeaves.contains(lLeave)) {
					lUserLeaves.add(lLeave);
				}
				
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUserLeaves;
	}
	
	public List<UserSwipe> mGetUserSwipes(String pUserId, String pFromDateStr, String pToDateStr, OfficeDetails pOfficeDetails) {

		Statement lStmt = null;
		String lQuery = "";

		StringBuilder lDoorIDBld = new StringBuilder();
		String[] lDoorsToConsider = pOfficeDetails.getMasterControllers();
		if (lDoorsToConsider.length > 0) {
			lDoorIDBld.append("and (");
			for (int i = 0; i < lDoorsToConsider.length; i++) {
				String lDoorID = lDoorsToConsider[i];
				if (lDoorID!=null && !lDoorID.equals("")) {
					lDoorIDBld.append(" " + IOS_DOOR_CONTROLLER_ID + " = " + lDoorID.trim());
					
					if (i < lDoorsToConsider.length - 1) {
						lDoorIDBld.append(" OR ");
					}
				}
				
			}
			lDoorIDBld.append(")");
		}
		
		List<UserSwipe> lUserSwipes = new LinkedList<UserSwipe>();
		UserSwipe lUserSwipe = null;
		SimpleDateFormat lDateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
		try {
			
				lQuery = "SELECT " + IOS_EMP_ID + ", " + IOS_SWIPE_TIME + ", " +
						 IOS_INOUT_TYPE + ", " + IOS_DOOR_CONTROLLER_ID + ", " +
						 IOS_DOOR_NAME + " FROM " + 
						 INOUT_SWIPES_TABLE + " where " + IOS_EMP_ID + "='" + pUserId + "' " +
						 "and " +
						 "(" + IOS_SWIPE_TIME + " between '" + pFromDateStr + "' and '" + pToDateStr + " 23:59:59.999')" +
						 lDoorIDBld.toString() +
						 " order by " + IOS_SWIPE_TIME + "";

				lStmt = connection.createStatement();
				ResultSet rs = lStmt.executeQuery(lQuery);
				
				while(rs.next()) {
					
					Date lDate = null;
					try {
						lDate = lDateFormat.parse(rs.getString(IOS_SWIPE_TIME));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String lEntryExit = rs.getInt(IOS_INOUT_TYPE) == 0 ? "Entry" : "Exit";
					String lDoorName = rs.getString(IOS_DOOR_NAME);
					String lInOut = "";
					int lIOType = rs.getInt(IOS_INOUT_TYPE);
	
					lUserSwipe = new UserSwipe(pUserId, pUserId, lDate, lInOut, lDoorName, lEntryExit);
					lUserSwipe.setIoType(lIOType);
					lUserSwipes.add(lUserSwipe);
				}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUserSwipes;	
}
	
	public static List<Date> mGetUserSwipeInOutTime(String pUserID, String pDateStr) {
		Statement lStmt = null;
		Date lUserSwipeInTime = null;
		Date lUserSwipeOutTime = null;
		List<Date> lUserSwipeInOutTimes = new LinkedList<Date>();
		try {
			String lQuery = "select " + DA_PUNCH1_TIME + ", " + DA_OUT_PUNCH_TIME + " from " + DAILY_ATTENDANCE_TABLE + " where " + DA_EMP_ID + " = '" + pUserID + "' AND CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103) = '" + pDateStr+ "'";
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				lUserSwipeInTime = rs.getTime(1);
				lUserSwipeOutTime = rs.getTime(2);
			}
			
			lUserSwipeInOutTimes.add(lUserSwipeInTime);
			lUserSwipeInOutTimes.add(lUserSwipeOutTime);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUserSwipeInOutTimes;
	}

	/*
	 * All lUsers report
	 */
	
	
	public static Map<String, Integer> mGetAllUsersHolidaySchedule(String pFromDateStr, String pToDateStr, String pFrequency) {
		Statement lStmt = null;
		Map<String, Integer> lHolidayList = new HashMap<String, Integer>();
		try {
			String lQuery = "where " + HS_HOLIDAY_DATE + "  >= '"+pFromDateStr+"' and " + HS_HOLIDAY_DATE + " <= '"+pToDateStr+"' and (DATEPART(DW, " + HS_HOLIDAY_DATE + ") != 1 and DATEPART(DW, " + HS_HOLIDAY_DATE + ") != 7) ";
			if (pFrequency.equalsIgnoreCase("daily")) {
				lQuery = "SELECT concat(" + HS_HOLIDAY_ID + ", '&', RIGHT('0' + RTRIM(DAY(" + HS_HOLIDAY_DATE + ")), 2), '/', RIGHT('0' + RTRIM(MONTH(" + HS_HOLIDAY_DATE + ")), 2), '/', DATEPART(yy, " + HS_HOLIDAY_DATE + ")), COUNT(*) FROM " + HOLIDAY_SCHEDULE_TABLE + " " +
						 lQuery + 
						   		"GROUP BY concat(" + HS_HOLIDAY_ID + ", '&', RIGHT('0' + RTRIM(DAY(" + HS_HOLIDAY_DATE + ")), 2), '/', RIGHT('0' + RTRIM(MONTH(" + HS_HOLIDAY_DATE + ")), 2), '/', DATEPART(yy, " + HS_HOLIDAY_DATE + "))";
			} else if (pFrequency.equalsIgnoreCase("monthly")) {
				lQuery = "SELECT CONCAT(" + HS_HOLIDAY_ID + ", '&', YEAR(" + HS_HOLIDAY_DATE + "), '-', MONTH(" + HS_HOLIDAY_DATE + ")),  COUNT(*) FROM " + HOLIDAY_SCHEDULE_TABLE + " " +
						 lQuery + 
						   " GROUP BY CONCAT(" + HS_HOLIDAY_ID + ", '&', YEAR(" + HS_HOLIDAY_DATE + "), '-', MONTH(" + HS_HOLIDAY_DATE + "))";
			} else if (pFrequency.equalsIgnoreCase("weekly")) {
				lQuery = "SELECT concat(" + HS_HOLIDAY_ID + ", '&', YEAR (" + HS_HOLIDAY_DATE + "), '-', DATEPART(ww, (" + HS_HOLIDAY_DATE + "))),  COUNT(*) FROM " + HOLIDAY_SCHEDULE_TABLE + " " +
						  lQuery + 
						   " GROUP BY concat(" + HS_HOLIDAY_ID + ", '&', YEAR (" + HS_HOLIDAY_DATE + "), '-', DATEPART(ww, (" + HS_HOLIDAY_DATE + ")))";
			}
			
			
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				lHolidayList.put(rs.getString(1), rs.getInt(2));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lHolidayList;
	}
	
	public static List<String> mGetAllUsersDateOfJoin(String pFromDateStr, String pToDateStr) {
		Statement lStmt = null;
		String lUserID = null;
		List<String> lUsersDateOfJoinList = new ArrayList<String>();
		try {			
			String lQuery = "SELECT " + UM_EMP_ID + ", " + UM_JOIN_DATE + " FROM " + USER_MASTER_TABLE + " where (" + UM_JOIN_DATE + " > '" + pFromDateStr + "' or " + UM_JOIN_DATE + " > '" + pToDateStr + "' ) group by " + UM_EMP_ID + ", " + UM_JOIN_DATE + "";
			
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			
			while(rs.next()) {
				lUserID = rs.getString(1);	
				lUsersDateOfJoinList.add(lUserID);
			}

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUsersDateOfJoinList;
	}
	
	public static Map<String, Double> mGetAllUsersPresenceAsMap(
			String pFromDateStr, String pToDateStr, String pFrequency) {
		
		Statement lStmt = null;
		Map<String, Double> lUserAttendance = new HashMap<String, Double>();

		String lQuery = "";
		try {
			lQuery = " FROM " + DAILY_ATTENDANCE_TABLE + " where " +
					 "(CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103) between '"+pFromDateStr+"' and '"+pToDateStr+"')"+
					 " and " + DA_WORK_TIME + "!=0 ";
			if (pFrequency.equalsIgnoreCase("monthly")) {
				lQuery = "SELECT  concat(" + DA_EMP_ID + ",'&', YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), " +
						"'-', MONTH (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103))), (SUM(" + DA_WORK_TIME + ")/60) " +
						lQuery +
						" group by concat(" + DA_EMP_ID + ",'&',YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), '-', " +
						" MONTH (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)))";
			} else if (pFrequency.equalsIgnoreCase("weekly")) {
				lQuery = "SELECT  concat(" + DA_EMP_ID + ",'&', YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), " +
						"'-', DATEPART(ww, (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)))), (SUM(" + DA_WORK_TIME + ")/60) " + 
						lQuery +
						" group by concat(" + DA_EMP_ID + ",'&',YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), '-', " +
						" DATEPART(ww, (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103))))";
			} else if (pFrequency.equalsIgnoreCase("daily")) {
				lQuery = "SELECT  concat(" + DA_EMP_ID + ",'&', " + DA_PROCESS_DATE + "), " +
						"(SUM(" + DA_WORK_TIME + ")/60)" + 
						lQuery +
						"GROUP BY concat(" + DA_EMP_ID + ",'&', " + DA_PROCESS_DATE + "), " + DA_WORK_TIME + "";
			}

			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
					String lYearMonthStr = rs.getString(1);
					double lEffectiveHours = rs.getDouble(2);
					lUserAttendance.put(lYearMonthStr, lEffectiveHours);
			}

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUserAttendance;
	}
	


	public static Map<String, Double> mGetAllUsersLeavesAvailedAsMap(
			String pFromDateStr, String pToDateStr, String pFrequency) {
		
		Statement lStmt = null;
		Map<String, Double> lUserAttendance = new HashMap<String, Double>();
		String lQuery = "";
		try {
			
			lQuery = " FROM " + DAILY_ATTENDANCE_TABLE + " where " +
					 " (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103) between '"+pFromDateStr+"' and '"+pToDateStr+" 23:59:59.999')"+
					 " and " +
					 "((" + DA_FIRST_HALF + "='CO' and " + DA_SECOND_HALF + " = 'CO') or " +
					 "(" + DA_FIRST_HALF + "='UP' and " + DA_SECOND_HALF + " = 'UP') or " + 
					 "(" + DA_FIRST_HALF + "='PL' and " + DA_SECOND_HALF + " = 'PL') or " + 
					 "(" + DA_FIRST_HALF + "='CL' and " + DA_SECOND_HALF + " = 'CL') or " + 
					 "(" + DA_FIRST_HALF + "='AL' and " + DA_SECOND_HALF + " = 'AL') or " + 
					 "(" + DA_FIRST_HALF + "='ML' and " + DA_SECOND_HALF + " = 'ML') or " + 
					 "(" + DA_FIRST_HALF + "='RH' and " + DA_SECOND_HALF + " = 'RH'))";
			if (pFrequency.equalsIgnoreCase("monthly")) {
				lQuery = "SELECT  concat(" + DA_EMP_ID + ",'&',YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), " +
						"'-', MONTH (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103))), COUNT(*) " + 
						lQuery +
						" group by concat(" + DA_EMP_ID + ",'&',YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), '-', " +
						" MONTH (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)))";
			} else if (pFrequency.equalsIgnoreCase("weekly")) {
				lQuery = "SELECT  concat(" + DA_EMP_ID + ",'&',YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), " +
						"'-', DATEPART(ww, (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)))), COUNT(*) " + 
						lQuery +
						" group by concat(" + DA_EMP_ID + ",'&',YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), '-', " +
						" DATEPART(ww, (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103))))";
			} else if (pFrequency.equalsIgnoreCase("daily")) {
				lQuery = "SELECT  concat(" + DA_EMP_ID + ",'&', " + DA_PROCESS_DATE + "), COUNT(*) " + 
						lQuery +
						"GROUP BY concat(" + DA_EMP_ID + ",'&', " + DA_PROCESS_DATE + ")";
			}
			
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);

			while(rs.next()) {
				String lYearMonthStr = rs.getString(1);
				double lLeavesAvailed = rs.getDouble(2);	
				lUserAttendance.put(lYearMonthStr, lLeavesAvailed);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUserAttendance;
	}
	
	public static Map<String, Double> mGetAllUsersHalfdayLeavesAvailedAsMap(
			String pFromDateStr, String pToDateStr, String pFrequency) {
		
		Statement lStmt = null;
		Map<String, Double> lUserAttendance = new HashMap<String, Double>();
		String lQuery = "";
		try {
			
			lQuery = " FROM " + DAILY_ATTENDANCE_TABLE + " where " +
					 " (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103) between '"+pFromDateStr+"' and '"+pToDateStr+" 23:59:59.999')"+
					 " and " +
					 "((" + DA_FIRST_HALF + " != 'CO' and " + DA_SECOND_HALF + " = 'CO') or (" + DA_FIRST_HALF + " = 'CO' and " + DA_SECOND_HALF + " != 'CO') or " +
					 "(" + DA_FIRST_HALF + " != 'UP' and " + DA_SECOND_HALF + " = 'UP') or (" + DA_FIRST_HALF + " = 'UP' and " + DA_SECOND_HALF + " != 'UP') or " + 
					 "(" + DA_FIRST_HALF + " != 'PL' and " + DA_SECOND_HALF + " = 'PL') or (" + DA_FIRST_HALF + " = 'PL' and " + DA_SECOND_HALF + " != 'PL') or " + 
					 "(" + DA_FIRST_HALF + " != 'CL' and " + DA_SECOND_HALF + " = 'CL') or (" + DA_FIRST_HALF + " = 'CL' and " + DA_SECOND_HALF + " != 'CL') or " + 
					 "(" + DA_FIRST_HALF + " != 'AL' and " + DA_SECOND_HALF + " = 'AL') or (" + DA_FIRST_HALF + " = 'AL' and " + DA_SECOND_HALF + " != 'AL') or " + 
					 "(" + DA_FIRST_HALF + " != 'ML' and " + DA_SECOND_HALF + " = 'ML') or (" + DA_FIRST_HALF + " = 'ML' and " + DA_SECOND_HALF + " != 'ML') or " + 
					 "(" + DA_FIRST_HALF + " != 'RH' and " + DA_SECOND_HALF + " = 'RH') or (" + DA_FIRST_HALF + " = 'RH' and " + DA_SECOND_HALF + " != 'RH'))";
			
			if (pFrequency.equalsIgnoreCase("monthly")) {
				lQuery = "SELECT concat(" + DA_EMP_ID + ",'&',YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), " +
						"'-', MONTH (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103))), 0.5*COUNT(*) " + 
						lQuery +
						" group by concat(" + DA_EMP_ID + ",'&',YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), " +
						"'-', MONTH (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)))";
			} else if (pFrequency.equalsIgnoreCase("weekly")) {
				lQuery = "SELECT concat(" + DA_EMP_ID + ",'&',YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), '-', " +
						" DATEPART(ww, (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)))), 0.5*COUNT(*) " + 
						lQuery +
						" group by concat(" + DA_EMP_ID + ",'&',YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), '-', " +
						" DATEPART(ww, (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103))))";
			} else if (pFrequency.equalsIgnoreCase("daily")) {
				lQuery = "SELECT  concat(" + DA_EMP_ID + ",'&', " + DA_PROCESS_DATE + "), 0.5*COUNT(*) " + 
						lQuery +
						" GROUP BY concat(" + DA_EMP_ID + ",'&', " + DA_PROCESS_DATE + ")";
			}

			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);

			while(rs.next()) {
				String lYearMonthStr = rs.getString(1);
				double lLeavesAvailed = rs.getDouble(2);	
				lUserAttendance.put(lYearMonthStr, lLeavesAvailed);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUserAttendance;
	}
	
	public static Map<String, Double> mGetAllUsersODsAsMap(
			String pFromDateStr, String pToDateStr, String pFrequency) {
		
		Statement lStmt = null;
		Map<String, Double> lUserAttendance = new HashMap<String, Double>();
		String lQuery = "";
		try {
			
			lQuery = " FROM " + DAILY_ATTENDANCE_TABLE + " where" +
					 " (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103) between '"+pFromDateStr+"' and '"+pToDateStr+"')"+
					 " and " +
					 "(" + DA_FIRST_HALF + "='OD' OR " + DA_SECOND_HALF + " = 'OD') ";
					 
			if (pFrequency.equalsIgnoreCase("monthly")) {
				lQuery = "SELECT concat(" + DA_EMP_ID + ",'&',YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), " +
						 "'-', MONTH (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103))), COUNT(*)" + 
						 lQuery +
						 " group by concat(" + DA_EMP_ID + ",'&',YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), '-', " +
						 " MONTH (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)))";
			} else if (pFrequency.equalsIgnoreCase("weekly")) {
				lQuery = "SELECT concat(" + DA_EMP_ID + ",'&',YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), " +
						 "'-', DATEPART(ww, (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)))), COUNT(*)" + 
						 lQuery +
						 " group by concat(" + DA_EMP_ID + ",'&',YEAR (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103)), '-', " +
						 " DATEPART(ww, (CONVERT(DATETIME, " + DA_PROCESS_DATE + ", 103))))";
			} else if (pFrequency.equalsIgnoreCase("daily")) {	
				lQuery = "SELECT  concat(" + DA_EMP_ID + ",'&', " + DA_PROCESS_DATE + "), COUNT(*) " + 
						 lQuery +
						 " GROUP BY concat(" + DA_EMP_ID + ",'&', " + DA_PROCESS_DATE + ")";
			}

			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);

			while(rs.next()) {
				String lYearMonthStr = rs.getString(1);
				double lLeavesAvailed = rs.getDouble(2);	
				lUserAttendance.put(lYearMonthStr, lLeavesAvailed);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUserAttendance;
	}


	public static List<CorrectionEntry> mGetUserPendingODs(String pUserID,
			String pFromDateStr, String pToDateStr) {
		Statement lStmt = null;
		List<CorrectionEntry> lPendingODs = new ArrayList<CorrectionEntry>();
		String lQuery = "SELECT * " +
				" FROM " + LEAVE_DETAILS_TABLE + " where " + DA_EMP_ID + " = '"+pUserID+"'" +
				" and flgFinalApproval = 0 " + 
				" and (FromDate >= '" + pFromDateStr + "' " +
				" and ToDate <= '" + pToDateStr + "') " +
				" and " + LD_LEAVE_ID + " = 'OD'";
		try {

			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);

			while(rs.next()) {
				Date lAppliedDate = rs.getDate("APPLDate");
				Date lProcessDate = rs.getDate("ToDate");
				CorrectionEntry lCorrectionEntry = new CorrectionEntry(pUserID, lAppliedDate, lProcessDate, "On Duty");
				lPendingODs.add(lCorrectionEntry);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lPendingODs;
	}

	public static List<CorrectionEntry> mGetUserPendingCorrections(String pUserID,
			String pFromDateStr, String pToDateStr) {

		Statement lStmt = null;
		String lQuery = "";
		List<CorrectionEntry> lPendingCorrections = new ArrayList<CorrectionEntry>();
		try {
			
			lQuery = "select * FROM EDS_ATDCorrection where" +
					 " " + DA_EMP_ID + " = '" + pUserID + "' " +
					 " and flgFinalApproval = 0 " +
					 " and (PDate >= '"+pFromDateStr+"' and PDate <='"+pToDateStr+"')";
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);

			while(rs.next()) {
				Date lAppliedDate = rs.getDate("AppDate");
				Date lProcessDate = rs.getDate("PDate");
				CorrectionEntry lCorrectionEntry = new CorrectionEntry(pUserID, lAppliedDate, lProcessDate, "Official");
				lPendingCorrections.add(lCorrectionEntry);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lPendingCorrections;
	}
	
	public static List<CorrectionEntry> mGetUserPendingEntries(String pUserID,
			Date pFromDate, Date pToDate) {

		SimpleDateFormat lDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String lFromDateStr = lDateFormat.format(pFromDate);
		String lToDateStr = lDateFormat.format(pToDate);
		
		List<CorrectionEntry> lPendingEntries = new ArrayList<CorrectionEntry>();
		List<CorrectionEntry> lPendingODs = mGetUserPendingODs(pUserID, lFromDateStr, lToDateStr);
		
		List<CorrectionEntry> lPendingCorrections = mGetUserPendingCorrections(pUserID, lFromDateStr, lToDateStr);
		lPendingEntries.addAll(lPendingODs);
		lPendingEntries.addAll(lPendingCorrections);
		
		return lPendingEntries;
	}

	public static UserSwipe mGetUserLastSwipe(User pUser) {

			Statement lStmt = null;
			String lQuery = "";
			boolean lIsUserInOffice = mIsUserInOffice(pUser.getUserId());
			UserSwipe lUserACSSwipe = null; // In Office Doors
			UserSwipe lUserTASwipe = null; // Main Doors
			UserSwipe lUserSwipe = null;
			SimpleDateFormat lDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				
				if (lIsUserInOffice) {
					
					lQuery = "SELECT " + DA_EMP_ID + ", " + IOS_SWIPE_TIME + ", " + IOS_INOUT_TYPE + ", " + IOS_DOOR_NAME + " FROM EDS_ACSInOutSwipes where " + DA_EMP_ID + "='" + pUser.getUserId() + "' and " + IOS_SWIPE_TIME + " = (select MAX(" + IOS_SWIPE_TIME + ") FROM EDS_ACSInOutSwipes where " + DA_EMP_ID + "='" + pUser.getUserId() + "' )";

					lStmt = connection.createStatement();
					ResultSet rsACS = lStmt.executeQuery(lQuery);
					
					while(rsACS.next()) {
						
						Date lDate = null;
						try {
							lDate = lDateFormat.parse(rsACS.getString(IOS_SWIPE_TIME));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						String lEntryExit = rsACS.getInt(IOS_INOUT_TYPE) == 0 ? "Entry" : "Exit";
						String lDoorName = rsACS.getString(IOS_DOOR_NAME);
						lUserACSSwipe = new UserSwipe(pUser.getUserId(), pUser.getName(), lDate, "IN", lDoorName, lEntryExit);
					}
					lStmt = connection.createStatement();
					//rs = lStmt.executeQuery(lQuery);
					lQuery = "SELECT " + DA_EMP_ID + ", " + IOS_SWIPE_TIME + ", " + IOS_INOUT_TYPE + ", " + IOS_DOOR_NAME + " FROM " + INOUT_SWIPES_TABLE + " where " + DA_EMP_ID + "='" + pUser.getUserId() + "' and " + IOS_SWIPE_TIME + " = (select MAX(" + IOS_SWIPE_TIME + ") FROM " + INOUT_SWIPES_TABLE + " where " + DA_EMP_ID + "='" + pUser.getUserId() + "' )";
					ResultSet rsTA = lStmt.executeQuery(lQuery);

					
					while(rsTA.next()) {
						
						Date lDate = null;
						try {
							lDate = lDateFormat.parse(rsTA.getString(IOS_SWIPE_TIME));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						String lEntryExit = rsTA.getInt(IOS_INOUT_TYPE) == 0 ? "Entry" : "Exit";
						String lDoorName = rsTA.getString(IOS_DOOR_NAME);
						lUserTASwipe = new UserSwipe(pUser.getUserId(), pUser.getName(), lDate, "IN", lDoorName, lEntryExit);
					}

					if (lUserACSSwipe != null) {
						if (lUserTASwipe != null && lUserTASwipe.getSwipeTime().getTime() > lUserACSSwipe.getSwipeTime().getTime()) {
							lUserSwipe = lUserTASwipe;
						} else {
							lUserSwipe = lUserACSSwipe;
						}
					}
					
				} else {
					lQuery = "SELECT " + DA_EMP_ID + ", " + IOS_SWIPE_TIME + ", " + IOS_INOUT_TYPE + ", " + IOS_DOOR_NAME + " FROM " + INOUT_SWIPES_TABLE + " where " + DA_EMP_ID + "='" + pUser.getUserId() + "' and " + IOS_SWIPE_TIME + " = (select MAX(" + IOS_SWIPE_TIME + ") FROM " + INOUT_SWIPES_TABLE + " where " + DA_EMP_ID + "='" + pUser.getUserId() + "' )";
					
					lStmt = connection.createStatement();
					ResultSet rs = lStmt.executeQuery(lQuery);
					
					while(rs.next()) {
						Date lDate = null;
						try {
							lDate = lDateFormat.parse(rs.getString(IOS_SWIPE_TIME));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String lEntryExit = "Exit";
						String lDoorName = rs.getString(IOS_DOOR_NAME);
						lUserSwipe = new UserSwipe(pUser.getUserId(), pUser.getName(), lDate, "OUT", lDoorName, lEntryExit);
					}
					
				}
				
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				JDBCConnectionUtil.mCloseStmt(lStmt);
			}
			return lUserSwipe;
	}
	
	public static List<UserSwipe> mGetUserMainSwipesNew(String pUserId, String pDateStr) {

		Statement lStmt = null;
		String lQuery = "";

		List<UserSwipe> lUserSwipes = new ArrayList<UserSwipe>();
		UserSwipe lUserSwipe = null;
		SimpleDateFormat lDateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
		try {
	
				lQuery = "SELECT " + IOS_EMP_ID + ", " + IOS_SWIPE_TIME + ", " + IOS_INOUT_TYPE + ", " + IOS_MASTER_CONTROLLER_ID + " FROM " + INOUT_SWIPES_TABLE + " where " + IOS_EMP_ID + "='" + pUserId + "' and " + IOS_SWIPE_TIME + " like '" + pDateStr + "%' order by " + IOS_SWIPE_TIME;
				
				lStmt = connection.createStatement();
				ResultSet rs = lStmt.executeQuery(lQuery);
				
				while(rs.next()) {
					
					Date lDate = null;
					try {
						lDate = lDateFormat.parse(rs.getString(IOS_SWIPE_TIME));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String lEntryExit = rs.getInt(IOS_INOUT_TYPE) == 0 ? "Entry" : "Exit";
					String lDoorName = rs.getString(IOS_MASTER_CONTROLLER_ID);
					String lInOut = "";
					int lIOType = rs.getInt(IOS_INOUT_TYPE);
	
					lUserSwipe = new UserSwipe(pUserId, "", lDate, lInOut, lDoorName, lEntryExit);
					lUserSwipe.setIoType(lIOType);
					lUserSwipes.add(lUserSwipe);
				}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUserSwipes;	
}
	
	
	
public static List<String> mGetBranches() {
		
		Statement lStmt = null;
		List<String> lBranchesList = new ArrayList<String>();
		try {

			String lQuery = "SELECT distinct " + UM_BRANCH + " FROM " + USER_MASTER_TABLE + "";
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
					lBranchesList.add(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lBranchesList;
	}
	
	public static List<String> mGetDepartments() {
		
		Statement lStmt = null;
		List<String> lDepartmentsList = new ArrayList<String>();
		try {

			String lQuery = "SELECT distinct Department FROM " + USER_MASTER_TABLE + "";
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
					lDepartmentsList.add(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lDepartmentsList;
	}

	public static List<String> mGetOrganizations() {
		
		Statement lStmt = null;
		List<String> lOrganizationsList = new ArrayList<String>();
		try {

			String lQuery = "SELECT distinct Organization FROM " + USER_MASTER_TABLE + "";
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
					lOrganizationsList.add(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lOrganizationsList;
	}
	

	
	
	public static List<String> mGetReportingManagers() {
		
		Statement lStmt = null;
		List<String> lDepartmentsList = new ArrayList<String>();
		try {

			String lQuery = "SELECT distinct DptName FROM " + DAILY_ATTENDANCE_TABLE + "";
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
					lDepartmentsList.add(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lDepartmentsList;
	}

	
	

}
