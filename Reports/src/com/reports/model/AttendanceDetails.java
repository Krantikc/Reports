package com.reports.model;

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;


public class AttendanceDetails {
	
	private String userId;
	
	private Date fromDate;
	
	private Date toDate;
	
	private String frequency;
	
	private Date date;
	
	private int day;
	
	private int month;
	
	private int week;
	
	private int year;
	
	private Date beginDate;
	
	private Date endDate;
	
	private int holidays;
	
	private int workingDays;
	
	private double daysPresent;
	
	private double onDuty;
	
	private double tour;
	
	private double leavesTaken;
	
	private String leavesTooltip;
	
	@Expose
	private List<Leave> leavesDetails;
	
	private double requiredHours;
	
	private double effectiveHours;
	
	private double difference;
	
	private Date firstSwipeInTime;
	
	private Date swipeInTime;
	
	private Date swipeOutTime;
	
	private boolean isInOffice;
	
	private UserSwipe lastSwipe;
	
	private String status;
	
	
	public AttendanceDetails() {
		
	}
	
	public AttendanceDetails(String userId, String frequency, int year, int month, Date beginDate, Date endDate, int workingDays, double daysPresent, double onDuty, double leavesTaken, String leavesTooltip, double requiredHours,  double effectiveHours, double difference) {
		
		this.userId = userId;
		
		this.frequency = frequency;
		
		this.year = year;
		
		this.month = month;
		
		this.beginDate = beginDate;
		
		this.endDate = endDate;
		
		this.workingDays = workingDays;
		
		this.daysPresent = daysPresent;
		
		this.onDuty = onDuty;
		
		this.leavesTaken = leavesTaken;
		
		this.leavesTooltip = leavesTooltip;
		
		this.requiredHours = requiredHours;
		
		this.effectiveHours = effectiveHours;
		
		this.difference = difference;
	}
	
	public AttendanceDetails(String userId, String frequency, int year, int month, Date beginDate, Date endDate, int workingDays, double daysPresent, double onDuty, double leavesTaken, String leavesTooltip, List<Leave> leavesDetails, double requiredHours,  double effectiveHours, double difference) {
		
		this.userId = userId;
		
		this.frequency = frequency;
		
		this.year = year;
		
		this.month = month;
		
		this.beginDate = beginDate;
		
		this.endDate = endDate;
		
		this.workingDays = workingDays;
		
		this.daysPresent = daysPresent;
		
		this.onDuty = onDuty;
		
		this.leavesTaken = leavesTaken;
		
		this.leavesTooltip = leavesTooltip;
		
		this.leavesDetails = leavesDetails;
		
		this.requiredHours = requiredHours;
		
		this.effectiveHours = effectiveHours;
		
		this.difference = difference;
	}
	
public AttendanceDetails(String userId, String frequency, int year, int week, Date beginDate, Date endDate, int workingDays, double daysPresent, double onDuty, double leavesTaken,  List<Leave> leavesDetails, double requiredHours,  double effectiveHours, double difference) {
		
		this.userId = userId;
		
		this.frequency = frequency;
		
		this.year = year;
		
		this.week = week;
		
		this.beginDate = beginDate;
		
		this.endDate = endDate;
		
		this.workingDays = workingDays;
		
		this.daysPresent = daysPresent;
		
		this.onDuty = onDuty;
		
		this.leavesTaken = leavesTaken;
		
		this.leavesDetails = leavesDetails;
		
		this.requiredHours = requiredHours;
		
		this.effectiveHours = effectiveHours;
		
		this.difference = difference;
	}

public AttendanceDetails(String userId, String frequency, Date date, Date beginDate, Date endDate, int workingDays, double daysPresent, double onDuty, double leavesTaken,  List<Leave> leavesDetails, double requiredHours,  double effectiveHours, double difference) {
	
	this.userId = userId;
	
	this.frequency = frequency;
	
	this.date = date;

	this.beginDate = beginDate;
	
	this.endDate = endDate;
	
	this.workingDays = workingDays;
	
	this.daysPresent = daysPresent;
	
	this.onDuty = onDuty;
	
	this.leavesTaken = leavesTaken;
	
	this.leavesDetails = leavesDetails;
	
	this.requiredHours = requiredHours;
	
	this.effectiveHours = effectiveHours;
	
	this.difference = difference;
	
	
}

	public double getOnDuty() {
		return onDuty;
	}

	public void setOnDuty(double onDuty) {
		this.onDuty = onDuty;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getWeek() {
		return week;
	}

	public void setWeek(int week) {
		this.week = week;
	}

	public double getWorkingDays() {
		return workingDays;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public String getLeavesTooltip() {
		return leavesTooltip;
	}

	public void setLeavesTooltip(String leavesTooltip) {
		this.leavesTooltip = leavesTooltip;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public double getDaysPresent() {
		return daysPresent;
	}

	public void setDaysPresent(double daysPresent) {
		this.daysPresent = daysPresent;
	}

	public double getLeavesTaken() {
		return leavesTaken;
	}

	public void setLeavesTaken(double leavesTaken) {
		this.leavesTaken = leavesTaken;
	}

	public double getRequiredHours() {
		return requiredHours;
	}

	public void setRequiredHours(double requiredHours) {
		this.requiredHours = requiredHours;
	}

	public double getEffectiveHours() {
		return effectiveHours;
	}

	public List<Leave> getLeavesDetails() {
		return leavesDetails;
	}

	public void setLeavesDetails(List<Leave> leavesDetails) {
		this.leavesDetails = leavesDetails;
	}

	public void setEffectiveHours(double effectiveHours) {
		this.effectiveHours = effectiveHours;
	}

	public double getDifference() {
		return difference;
	}

	public void setDifference(double difference) {
		this.difference = difference;
	}

	public void setWorkingDays(int workingDays) {
		this.workingDays = workingDays;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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

	public int getHolidays() {
		return holidays;
	}

	public void setHolidays(int holidays) {
		this.holidays = holidays;
	}

	public Date getSwipeInTime() {
		return swipeInTime;
	}

	public void setSwipeInTime(Date swipeInTime) {
		this.swipeInTime = swipeInTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getSwipeOutTime() {
		return swipeOutTime;
	}

	public void setSwipeOutTime(Date swipeOutTime) {
		this.swipeOutTime = swipeOutTime;
	}

	public boolean isInOffice() {
		return isInOffice;
	}

	public void setInOffice(boolean isInOffice) {
		this.isInOffice = isInOffice;
	}

	public UserSwipe getLastSwipe() {
		return lastSwipe;
	}

	public void setLastSwipe(UserSwipe lastSwipe) {
		this.lastSwipe = lastSwipe;
	}

	public Date getFirstSwipeInTime() {
		return firstSwipeInTime;
	}

	public void setFirstSwipeInTime(Date firstSwipeInTime) {
		this.firstSwipeInTime = firstSwipeInTime;
	}

	public double getTour() {
		return tour;
	}

	public void setTour(double tour) {
		this.tour = tour;
	}

}
