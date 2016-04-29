package com.reports.model;

import java.util.Calendar;

public class OfficeDetails {

	private int locationId;
	
	private Calendar shiftStart;
	
	private Calendar shiftEnd;
	
	private Calendar breakStart;
	
	private Calendar breakEnd;
	
	private double minFullDayWorkingHours;
	
	private double minHalfDayWorkingHours;
	
	private String[] masterControllers;

	public OfficeDetails() {
		
	}
	
	public OfficeDetails(Calendar shiftStart, 
						 Calendar shiftEnd, 
						 Calendar breakStart, 
						 Calendar breakEnd, 
						 double minFullDayWorkingHours,
						 double minHalfDayWorkingHours,
						 String[] masterControllers) {
		this.shiftStart = shiftStart;
		this.shiftEnd = shiftEnd;
		this.breakStart = breakStart;
		this.breakEnd = breakEnd;
		this.minFullDayWorkingHours = minFullDayWorkingHours;
		this.minHalfDayWorkingHours = minHalfDayWorkingHours;
		this.masterControllers = masterControllers;
		
	}
	public double getMinHalfDayWorkingHours() {
		return minHalfDayWorkingHours;
	}

	public void setMinHalfDayWorkingHours(double minHalfDayWorkingHours) {
		this.minHalfDayWorkingHours = minHalfDayWorkingHours;
	}


	public Calendar getShiftStart() {
		return shiftStart;
	}

	public void setShiftStart(Calendar shiftStart) {
		this.shiftStart = shiftStart;
	}

	public Calendar getShiftEnd() {
		return shiftEnd;
	}

	public void setShiftEnd(Calendar shiftEnd) {
		this.shiftEnd = shiftEnd;
	}

	public Calendar getBreakStart() {
		return breakStart;
	}

	public void setBreakStart(Calendar breakStart) {
		this.breakStart = breakStart;
	}

	public Calendar getBreakEnd() {
		return breakEnd;
	}

	public void setBreakEnd(Calendar breakEnd) {
		this.breakEnd = breakEnd;
	}

	public double getMinFullDayWorkingHours() {
		return minFullDayWorkingHours;
	}

	public void setMinFullDayWorkingHours(double minFullDayWorkingHours) {
		this.minFullDayWorkingHours = minFullDayWorkingHours;
	}

	public String[] getMasterControllers() {
		return masterControllers;
	}

	public void setMasterControllers(String[] masterControllers) {
		this.masterControllers = masterControllers;
	}

	public int getLocationId() {
		return locationId;
	}

	public void setLocationId(int locationId) {
		this.locationId = locationId;
	}
	
	
	
}
