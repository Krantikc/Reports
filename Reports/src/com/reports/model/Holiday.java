package com.reports.model;

import java.util.Date;

public class Holiday {
	private String holidayId;

	private Date date;
	
	private Date endDate;
	
	private String name;
	
	private String type;

	public Holiday() {
		
	}
	
    public Holiday(String holidayId, Date date, Date endDate, String name) {
		this.holidayId = holidayId;
		this.date = date;
		this.endDate = endDate;
		this.name = name;
	}


	public String getHolidayId() {
		return holidayId;
	}

	public void setHolidayId(String holidayId) {
		this.holidayId = holidayId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
	
}
