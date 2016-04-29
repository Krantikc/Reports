package com.reports.model;

import java.util.Date;

public class Leave {
	
	private String leaveId;
	
	private Date fromDate;
	
	private Date toDate;
	
	private double postedDays;
	
	private String remarks;
	
	private double computedLeaves;
	
	public Leave() {
		
	}

	public Leave(String leaveId, Date fromDate, Date toDate, double postedDays, String remarks) {
		this.leaveId = leaveId;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.postedDays = postedDays;
		this.remarks = remarks;
	}
	

	public String getLeaveId() {
		return leaveId;
	}

	public void setLeaveId(String leaveId) {
		this.leaveId = leaveId;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public double getPostedDays() {
		return postedDays;
	}

	public void setPostedDays(double postedDays) {
		this.postedDays = postedDays;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public double getComputedLeaves() {
		return computedLeaves;
	}

	public void setComputedLeaves(double computedLeaves) {
		this.computedLeaves = computedLeaves;
	}
	
	
	@Override
	public boolean equals(Object object){
		boolean isEqual = false;
		
		if (object != null && object instanceof Leave) {
			Leave leaveObj = (Leave)object;
			if (this.fromDate.getTime() == leaveObj.fromDate.getTime() &&
				this.toDate.getTime() == leaveObj.toDate.getTime()) {
				isEqual = true;
			}
		}
		return isEqual;
	}
	
	@Override
	public int hashCode() {
		return (int) (this.fromDate.getTime() + this.toDate.getTime());
	}

}
