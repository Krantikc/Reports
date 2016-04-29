package com.reports.model;

public class UserReport {

	private String userId;
	
	private String name;
	
	private int workingDays;
	
	private int daysReported;
	
	public UserReport(){
		
	}
	
	public UserReport(String userId, String name, int workingDays, int daysReported){
		this.userId = userId;
		this.name = name;
		this.workingDays = workingDays;
		this.daysReported = daysReported;
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

	public int getDaysReported() {
		return daysReported;
	}

	public void setDaysReported(int daysReported) {
		this.daysReported = daysReported;
	}

	public int getWorkingDays() {
		return workingDays;
	}

	public void setWorkingDays(int workingDays) {
		this.workingDays = workingDays;
	}
	
	
}
