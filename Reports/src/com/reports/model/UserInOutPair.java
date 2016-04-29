package com.reports.model;

import java.util.Date;
import java.util.List;

public class UserInOutPair {

	private String userId;
	
	private Date date;
	
	private Date inTime;
	
	private Date outTime;
	
	private double swipeEffectiveHours;
	
	private double totalEffectiveHours;
	
	private String type;
	
	private String remarks;
	
	private List<Leave> leaveDetails;

	public UserInOutPair() {
		
	}
	
	public UserInOutPair(String userId, Date date, Date inTime, Date outTime, double swipeEffective) {
		this.userId = userId;
		this.date = date;
		this.inTime = inTime;
		this.outTime = outTime;
		this.swipeEffectiveHours = swipeEffective;
	}
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getInTime() {
		return inTime;
	}

	public void setInTime(Date inTime) {
		this.inTime = inTime;
	}

	public Date getOutTime() {
		return outTime;
	}

	public void setOutTime(Date outTime) {
		this.outTime = outTime;
	}

	public double getSwipeEffectiveHours() {
		return swipeEffectiveHours;
	}

	public void setSwipeEffectiveHours(double swipeEffectiveHours) {
		this.swipeEffectiveHours = swipeEffectiveHours;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<Leave> getLeaveDetails() {
		return leaveDetails;
	}

	public void setLeaveDetails(List<Leave> leaveDetails) {
		this.leaveDetails = leaveDetails;
	}

	public double getTotalEffectiveHours() {
		return totalEffectiveHours;
	}

	public void setTotalEffectiveHours(double totalEffectiveHours) {
		this.totalEffectiveHours = totalEffectiveHours;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
	
}
