package com.reports.model;

import java.util.Date;

public class UserSwipe {

	private String userId;
	
	private String name;
	
	private Date swipeTime;
	
	private int deviceId;
	
	private int ioType;
	
	private String inOut;
	
	private String doorName;
	
	private String doorEntryExit;
	
	public UserSwipe() {
		
	}
	
	public UserSwipe(String userId, String name, Date swipeTime, String inOut, String doorName, String doorEntryExit) {
		this.userId = userId;
		this.name = name;
		this.swipeTime = swipeTime;
		this.inOut = inOut;
		this.doorName = doorName;
		this.doorEntryExit = doorEntryExit;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getSwipeTime() {
		return swipeTime;
	}

	public void setSwipeTime(Date swipeTime) {
		this.swipeTime = swipeTime;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public String getDoorName() {
		return doorName;
	}

	public void setDoorName(String doorName) {
		this.doorName = doorName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInOut() {
		return inOut;
	}

	public void setInOut(String inOut) {
		this.inOut = inOut;
	}

	public String getDoorEntryExit() {
		return doorEntryExit;
	}

	public void setDoorEntryExit(String doorEntryExit) {
		this.doorEntryExit = doorEntryExit;
	}

	public int getIoType() {
		return ioType;
	}

	public void setIoType(int ioType) {
		this.ioType = ioType;
	}
	
	
}
