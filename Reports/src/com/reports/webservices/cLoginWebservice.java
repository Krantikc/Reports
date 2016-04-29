package com.reports.webservices;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/auth")
public class cLoginWebservice {

	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	public String mDoLogin(@FormParam("username") String lUsername,
						    @FormParam("password") String lPassword) {
		if (/*lUsername.equals("attendance") && */lPassword.equals("prc123")) {
			return "true";
		}
		
		return "false";
	}
	
	@GET
	@Path("/in")
	public String mIn() {
		
		return "true";
	}

	  public static void main(String[] args) {

	  }
	   
}
