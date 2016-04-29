package com.reports.model;

import java.util.Date;


public class Attendance {

	private String userId;
	
	private Date date;
	
	private String firstIn;
	
	private String lastOut;
	
	private double effectiveHours;
	
	public Attendance(){
		
	}
	
	public Attendance(String userId, Date date, String firstIn, String lastOut, double effectiveHours){
		this.userId = userId;
		this.date = date;
		this.firstIn = firstIn;
		this.lastOut = lastOut;
		this.effectiveHours = effectiveHours;
	}
	
	public Attendance(String userId, Date date, double effectiveHours) {
		this.userId = userId;
		this.date = date;
		this.effectiveHours = effectiveHours;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getFirstIn() {
		return firstIn;
	}

	public void setFirstIn(String firstIn) {
		this.firstIn = firstIn;
	}

	public String getLastOut() {
		return lastOut;
	}

	public void setLastOut(String lastOut) {
		this.lastOut = lastOut;
	}

	public double getEffectiveHours() {
		return effectiveHours;
	}

	public void setEffectiveHours(double effectiveHours) {
		this.effectiveHours = effectiveHours;
	}

	
	
}
