package com.reports.service;

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

import com.reports.dao.cUserDetailsDaoImpl;
import com.reports.model.Attendance;
import com.reports.model.User;
import com.reports.model.UserJSON;
import com.reports.util.JDBCConnectionUtil;

public class cUserDetailsService {
	static cUserDetailsDaoImpl aUserDetails = new cUserDetailsDaoImpl();
	
	public List<User> mGetAllUsers() {
		return aUserDetails.mGetAllUsers();	
	}
	
	public List<User> mGetUsers(String pDepartment,  String pBranch, String pOrganization, Date pFromDate, Date pToDate ) {
		return aUserDetails.mGetUsers(pDepartment, pBranch, pOrganization, pFromDate, pToDate);
		
	}
	
	public List<UserJSON> mGetUsersJSON(String pSearchPattern) {
		return aUserDetails.mGetUsersJSON(pSearchPattern);
		
	}
	
	public List<UserJSON> mGetUsersJSONID(String pSearchPattern) {
		return aUserDetails.mGetUsersJSONID(pSearchPattern);
	}
	
	public List<User> mGetUsers(String pSearchPattern) {
		return aUserDetails.mGetUsers(pSearchPattern);
	}
	
	public User mGetUserById(String pUserId) {
		return aUserDetails.mGetUserById(pUserId);
	}
	
	
	public List<User> mGetSubordinates(String pUserID, boolean pIncludeIndirectSubordinates) {
		Connection lCon = null;
		List<User> lSubordinates = new ArrayList<User>();
		lCon = JDBCConnectionUtil.getJDBCConnection();
		if (pIncludeIndirectSubordinates) {
			lSubordinates = aUserDetails.mGetIndirectSubordinatesRecursive(lCon, pUserID, lSubordinates);
		} else {
			lSubordinates = aUserDetails.mGetDirectSubordinates(lCon, pUserID, lSubordinates);
		}

		return lSubordinates;
	}
	
	
	
	/**
	 * 
	 * @param pCon
	 * @param pUserID
	 * @param pSubordinates
	 * @return
	 */
	public List<User> mGetDirectSubordinates(Connection pCon, String pUserID, List<User> pSubordinates) {
		return aUserDetails.mGetDirectSubordinates(pCon, pUserID, pSubordinates);
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
		return aUserDetails.mGetIndirectSubordinatesRecursive(pCon, pReportingInchargeId, pSubordinates);
	}

}
