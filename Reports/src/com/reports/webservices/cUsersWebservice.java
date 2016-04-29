package com.reports.webservices;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.google.gson.Gson;
import com.reports.dao.cUserDetailsDaoImpl;
import com.reports.model.Location;
import com.reports.model.User;
import com.reports.model.UserJSON;
import com.reports.service.cUserDetailsService;
import com.reports.util.JDBCConnectionUtil;

@Path("/users")
public class cUsersWebservice {

	@GET
	public String mGetUsersList() {	
		List<User> lUsersList 			= new ArrayList<User>();
		Map<String, Object> lResponse 	= new HashMap<String, Object>();
		lUsersList 						= new cUserDetailsService().mGetAllUsers();
		lResponse.put("usersList", lUsersList);
		String lResult 					= new Gson().toJson(lResponse);
		return lResult;
	}
	
	@GET
	@Path("/JSON")
	public String mGetUsersByPattern(@QueryParam(value="searchPattern") String lSearchPattern) {	
		Map<String, Object> lResponse 	= new HashMap<String, Object>();		
		List<UserJSON> lUsersList 		= new cUserDetailsService().mGetUsersJSON(lSearchPattern);
		lResponse.put("user", lUsersList);
		String lResult 					= new Gson().toJson(lResponse);
		return lResult;
	}
	
	@GET
	@Path("/JSONID")
	public String mGetUsersByIDPattern(@QueryParam(value="searchPattern") String lSearchPattern) {
		Map<String, Object> lResponse 	= new HashMap<String, Object>();
		List<UserJSON> lUsersList 		= new cUserDetailsService().mGetUsersJSONID(lSearchPattern);
		lResponse.put("user", lUsersList);
		String lResult 					= new Gson().toJson(lResponse);
		return lResult;
	}
	
	@GET
	@Path("{userId}")
	public String mGetUser(@PathParam(value="userId") String lUserId) {
		Map<String, Object> lResponse 	= new HashMap<String, Object>();
		User lUser 						= new cUserDetailsService().mGetUserById(lUserId);
		lResponse.put("user", lUser);
		String lResult 					= new Gson().toJson(lResponse);
		return lResult;
	}
	
	@GET
	@Path("/subordinates/{userId}/{indirectSubordinates : (\\w+)?}")
	public String mGetSubordinates(@PathParam(value="userId") String lUserId,
								   @PathParam(value="indirectSubordinates") boolean lIncludeIndirect) {
		Map<String, Object> lResponse 	= new HashMap<String, Object>();
		List<User> lUsersList 						= new cUserDetailsService().mGetSubordinates(lUserId, lIncludeIndirect);
		lResponse.put("usersList", lUsersList);
		String lResult 					= new Gson().toJson(lResponse);
		return lResult;
	}
	
	
	@GET
	@Path("/subordinates/all/{userId}")
	public String mGetAllSubordinates(@PathParam(value="userId") String lUserId) {
		Connection lCon = null;
		Statement lStmt = null;
		lCon = JDBCConnectionUtil.getJDBCConnection();
		
		Map<String, Object> lResponse 	= new HashMap<String, Object>();
		List<User> lUsersList 	= new ArrayList<User>();
		try {
			lStmt = lCon.createStatement();
			lUsersList 	= new cUserDetailsService().mGetIndirectSubordinatesRecursive(lCon, lUserId, lUsersList);
		} catch (Exception e) {
			
		} finally {
			JDBCConnectionUtil.closeConnection(lCon);
		}
		lResponse.put("usersList", lUsersList);
		String lResult 					= new Gson().toJson(lResponse);
		return lResult;
	}
	
	@GET
	@Path("/locations")
	public String mGetUserLocations() {	
		Map<String, Object> lResponse 	= new HashMap<String, Object>();		
		List<Location> lUserLocations 	= new cUserDetailsDaoImpl().mGetUserLocations();
		lResponse.put("locations", lUserLocations);
		String lResult 					= new Gson().toJson(lResponse);
		return lResult;
	}
}
