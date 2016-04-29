package com.reports.model;

import java.util.Date;

public class DateRange {

	private Date beginDate;
	
	private Date endDate;
	
	private int days;
	
	private int saturdays;
	
	private int sundays;
	
	public DateRange() {
		
	}

	public DateRange(Date beginDate, Date endDate, int days) {
		this.beginDate = beginDate;
		this.endDate = endDate;
		this.days = days;
	}
	
	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int getSaturdays() {
		return saturdays;
	}

	public void setSaturdays(int saturdays) {
		this.saturdays = saturdays;
	}

	public int getSundays() {
		return sundays;
	}

	public void setSundays(int sundays) {
		this.sundays = sundays;
	}
	
	
}
