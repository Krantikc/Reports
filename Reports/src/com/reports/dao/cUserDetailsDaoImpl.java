package com.reports.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import com.reports.model.Attendance;
import com.reports.model.Location;
import com.reports.model.User;
import com.reports.model.UserJSON;
import com.reports.util.JDBCConnectionUtil;

public class cUserDetailsDaoImpl {
	static Connection connection = JDBCConnectionUtil.JDBCConnection;
	
	static String USER_MASTERS_TABLE = "EDS_UserMasterData"; // "employee"; // OLD - Ranal_UserMasterData
	static String U_EMP_ID = "UserID";//"emp_id"; // OLD - UserID
	static String U_NAME = "Name";//"name"; // OLD - Name
	static String U_REPORTING_INCHARGE_ID = "ReportingIncharge1";//"reporting_incharge_id"; // OLD - ReportingIncharge1
	static String U_EMP_ID_ENABLE = "UserIDEnbl";//"emp_id_enbl"; // OLD - UserIDEnbl
	static String U_HOLIDAY_ID = "hldid"; //"hld_id"; // OLD - hldid
	static String U_BRANCH = "Branch"; //"branch"; // OLD - Branch
	
	public List<User> mGetAllUsers() {
		List<User> lUsersList = new ArrayList<User>();
		Statement lStmt = null;
		try {
			String lQuery = "SELECT * FROM " 
							+ USER_MASTERS_TABLE + " WHERE " 
							+ U_EMP_ID_ENABLE + " = 1 ORDER BY " + U_NAME;
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				lUsersList.add(new User(
						rs.getString(U_EMP_ID), 
						rs.getString(U_NAME),
						rs.getInt(U_HOLIDAY_ID), 
						rs.getString(U_BRANCH)
					));
			}
	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUsersList;
		
	}
	
	public List<User> mGetUsers(String pDepartment,  String pBranch, String pOrganization, Date lFromDate, Date lToDate ) {
		List<User> lUsersList = new ArrayList<User>();
		Statement lStmt = null;
		
		SimpleDateFormat lDateFormat 	= new SimpleDateFormat("yyyy-MM-dd");
		String lFromDateStr 					= null;
		String lToDateStr 					= null;
		try {
			lFromDateStr 	= lDateFormat.format(lFromDate);
			lToDateStr 	= lDateFormat.format(lToDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String lQuery = "SELECT * FROM UserMasterData WHERE UserIDEnbl = 1 and (JoinDT <= '" + lFromDateStr + "' or JoinDT <= '" + lToDateStr + "' )";
		
		if(!pDepartment.trim().equals("")) {
			lQuery = lQuery + " and Department in ('" + pDepartment + "') ";
		}
		if(!pBranch.trim().equals("")) {
			lQuery = lQuery + " and Branch in ('" + pBranch + "') ";
		}
		if(!pOrganization.trim().equals("")) {
			lQuery = lQuery + " and Organization in ('" + pOrganization + "') ";
		}
		try {
			lQuery = lQuery + "ORDER BY Name";
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				lUsersList.add(new User(
						rs.getString("UserID"), 
						rs.getString("Name"),
						rs.getInt("hldid"), 
						rs.getString("Branch")
					)
				);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUsersList;
		
	}
	
	public List<UserJSON> mGetUsersJSON(String pSearchPattern) {
		List<UserJSON> lUsersList = new ArrayList<UserJSON>();
		Statement lStmt = null;
		try {
			String lQuery = "SELECT * FROM " + USER_MASTERS_TABLE 
							+ " WHERE " + U_NAME + " LIKE '" + pSearchPattern + "%' AND " 
							+ U_EMP_ID_ENABLE + " = 1 ORDER BY " + U_NAME;
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				lUsersList.add(new UserJSON(rs.getString(1), rs.getString(2), rs.getString(2)));
			}
	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUsersList;
		
	}
	
	public List<UserJSON> mGetUsersJSONID(String pSearchPattern) {
		List<UserJSON> lUsersList = new ArrayList<UserJSON>();

		Statement lStmt = null;
		try {
			String lQuery = "SELECT * FROM " + USER_MASTERS_TABLE 
							+ " WHERE " + U_EMP_ID + " LIKE '"+pSearchPattern+"%' AND " 
							+ U_EMP_ID_ENABLE + " = 1";
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				lUsersList.add(new UserJSON(rs.getString(1), rs.getString(1), rs.getString(1)));
			}
	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUsersList;
		
	}
	
	public List<User> mGetUsers(String pSearchPattern) {
		List<User> lUsersList = new ArrayList<User>();
		Statement lStmt = null;
		try {
			String lQuery = "SELECT * FROM " + USER_MASTERS_TABLE 
							+ " where " + U_NAME + " LIKE '"+pSearchPattern+"%' AND " 
							+ U_EMP_ID_ENABLE + " = 1 ORDER BY " + U_NAME;
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				lUsersList.add(new User(
						rs.getString(U_EMP_ID), 
						rs.getString(U_NAME),
						rs.getInt(U_HOLIDAY_ID), 
						rs.getString(U_BRANCH)
					));
			}
	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUsersList;
		
	}
	
	public User mGetUserById(String pUserId) {
		Statement lStmt = null;
		User lUser = null;
		try {
			String lQuery = "SELECT * FROM " + USER_MASTERS_TABLE + " where " 
						    + U_EMP_ID + " = '"+pUserId+"' AND " + U_EMP_ID_ENABLE + " = 1";
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				lUser = new User(
						rs.getString(U_EMP_ID), 
						rs.getString(U_NAME),
						rs.getInt(U_HOLIDAY_ID), 
						rs.getString(U_BRANCH)
					);
			}
	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lUser;
		
	}
	
	
	public List<Location> mGetUserLocations() {
		Statement lStmt = null;
		Location lLocation = null;
		List<Location> lLoactionsList = new ArrayList<>();
		try {
			String lQuery = "SELECT distinct(" + U_BRANCH + "),  " + U_HOLIDAY_ID + " FROM " + USER_MASTERS_TABLE;
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			
			while(rs.next()) {
				lLocation = new Location(
						rs.getInt(U_HOLIDAY_ID), 
						rs.getString(U_BRANCH)
					);
				
				lLoactionsList.add(lLocation);
			}
	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return lLoactionsList;
		
	}
	
	
	
	/**
	 * 
	 * @param pCon
	 * @param pUserID
	 * @param pSubordinates
	 * @return
	 */
	public List<User> mGetDirectSubordinates(Connection pCon, String pUserID, List<User> pSubordinates) {
		Statement lStmt = null;
		try {
			String lQuery = "SELECT * FROM " + USER_MASTERS_TABLE + " where (" 
						    + U_REPORTING_INCHARGE_ID + " = '"+pUserID+"' OR " 
						    + U_EMP_ID + " = '"+pUserID+"') AND " + U_EMP_ID_ENABLE + " = 1";
			if (pUserID.equals("")) {
				lQuery = "SELECT * FROM " + USER_MASTERS_TABLE + " where " + U_EMP_ID_ENABLE + " = 1";
			}
			
			
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				pSubordinates.add(new User(
						rs.getString(U_EMP_ID), 
						rs.getString(U_NAME),
						rs.getInt(U_HOLIDAY_ID), 
						rs.getString(U_BRANCH)
					));
			}
			    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			JDBCConnectionUtil.mCloseStmt(lStmt);
		}
		return pSubordinates;
	}

	/**
	 * Gets Direct and Indirect Subordinates for particular reporting incharge. 
	 * @param pCon						Connection object 
	 * @param pReportingInchargeId		Employee Id of reporting incharge. It is treated as Employee Id of self if there are no subordinates under him.
	 * @param pSubordinates				ArrayList<User> to which the returned users will be pushed
	 * @return							ArrayList<User> which contains direct and indirect subordinates
	 * @throws SQLException
	 */
	public List<User> mGetIndirectSubordinatesRecursive(Connection pCon, 
														String pReportingInchargeId, 
														List<User> pSubordinates) {
		Statement lStmt = null;
		String lQuery = "SELECT " 
						+ U_EMP_ID_ENABLE + ", "
			    		+ U_EMP_ID + ", "
			    		+ U_NAME + ", "
			    		+ U_REPORTING_INCHARGE_ID + ", "
			    		+ U_HOLIDAY_ID + ", "
			    		+ U_BRANCH 
			    		+ " FROM " 
			    		+ USER_MASTERS_TABLE 
			    		+ " where (" 
						+ U_REPORTING_INCHARGE_ID + " = '"+pReportingInchargeId+"' OR " 
						+ U_EMP_ID + " = '"+pReportingInchargeId+"' "
						//+ "OR " 
						//+ U_EMP_ID + "=" + U_REPORTING_INCHARGE_ID 
						+ " ) AND " 
			    		//+"' AND " 
			    		+ U_EMP_ID_ENABLE + " = 1 "
			    		+ "group by " 
			    		+ U_EMP_ID_ENABLE + ", "
			    		+ U_REPORTING_INCHARGE_ID + ", "
			    		+ U_EMP_ID + ", "
			    		+ U_NAME + ", "
			    		+ U_HOLIDAY_ID + ", "
			    		+ U_BRANCH;
		
		try {
			lStmt = connection.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			while(rs.next()) {
				if (!rs.getString(U_EMP_ID).equals(pReportingInchargeId)) {
					pSubordinates.add(
							new User(
										rs.getString(U_EMP_ID), 
										rs.getString(U_NAME),
										rs.getInt(U_HOLIDAY_ID), 
										rs.getString(U_BRANCH)
									)
							);
				
					mGetIndirectSubordinatesRecursive(pCon, 
													  rs.getString(U_EMP_ID), 
													  pSubordinates);
				} else if (rs.getString(U_EMP_ID)
							 .equals(rs.getString(U_REPORTING_INCHARGE_ID))) {
					pSubordinates.add(
							new User(
										rs.getString(U_EMP_ID), 
										rs.getString(U_NAME),
										rs.getInt(U_HOLIDAY_ID), 
										rs.getString(U_BRANCH)
									)
							);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pSubordinates;
	}

}
