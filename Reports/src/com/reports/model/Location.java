package com.reports.model;

public class Location {

	public Location() {
		
	}
	
	public Location(int locationId, String name) {
		this.locationId = locationId;
		this.name = name;
	}
	private int locationId;
	
	private String name;
	
	
	public int getLocationId() {
		return locationId;
	}

	public void setLocationId(int locationId) {
		this.locationId = locationId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	
}
