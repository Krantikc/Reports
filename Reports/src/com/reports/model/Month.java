package com.reports.model;

import java.util.Date;

public class Month {

	private int year;
	
	private int month;

	private int days;
	
	private Date beginDate;
	
	private Date endDate;
	
	private int saturdays;
	
	private int sundays;
	
	public Month() {
		
	}

	public Month(int year, int month, int days, Date beginDate, Date endDate) {
		this.year = year;
		this.month = month;
		this.days = days;
		this.beginDate = beginDate;
		this.endDate = endDate;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
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
