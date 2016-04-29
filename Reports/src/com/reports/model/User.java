package com.reports.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="users")
public class User {

	private String userId;
	
	private String name;
	
	private int holidayId;
	
	private String branch;
	
	private boolean baseUser;
	
	public User(){
		
	}

	public User(String userId, String name, int holidayId, String branch){
		this.userId = userId;
		this.name = name;
		this.holidayId = holidayId;
		this.branch = branch;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getHolidayId() {
		return holidayId;
	}

	public void setHolidayId(int holidayId) {
		this.holidayId = holidayId;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public boolean isBaseUser() {
		return baseUser;
	}

	public void setBaseUser(boolean baseUser) {
		this.baseUser = baseUser;
	}
	
	
}
