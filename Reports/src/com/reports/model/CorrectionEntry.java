package com.reports.model;

import java.util.Date;
import java.util.List;

public class CorrectionEntry {

	private int correctionEntryId;
	
	private String userId;
	
	private Date appliedDate;
	
	private Date processDate;
	
	private String correctionType;
	
	private String remarks;
	
	private List<Object> correctionIdsList;

	
	public CorrectionEntry() {
		
	}
	
	public CorrectionEntry(String userId, Date appliedDate, Date processDate, String correctionType) {
		this.userId = userId;
		this.appliedDate = appliedDate;
		this.processDate = processDate;
		this.correctionType = correctionType;
	}
	
	public int getCorrectionEntryId() {
		return correctionEntryId;
	}

	public void setCorrectionEntryId(int correctionEntryId) {
		this.correctionEntryId = correctionEntryId;
	}

	public Date getAppliedDate() {
		return appliedDate;
	}

	public void setAppliedDate(Date appliedDate) {
		this.appliedDate = appliedDate;
	}

	
	public Date getProcessDate() {
		return processDate;
	}

	public void setProcessDate(Date processDate) {
		this.processDate = processDate;
	}

	public String getCorrectionType() {
		return correctionType;
	}

	public void setCorrectionType(String correctionType) {
		this.correctionType = correctionType;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<Object> getCorrectionIdsList() {
		return correctionIdsList;
	}

	public void setCorrectionIdsList(List<Object> correctionIdsList) {
		this.correctionIdsList = correctionIdsList;
	}
	
	
	
}
