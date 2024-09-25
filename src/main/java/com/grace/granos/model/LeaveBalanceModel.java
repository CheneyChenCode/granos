package com.grace.granos.model;

import org.springframework.stereotype.Component;

@Component
public class LeaveBalanceModel {

	private int empId;
	private int year;
	private int month;
	private String shift;
	private float usedHours;
	private float remainingHours;
	private String creater;
	private String description;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCreater() {
		return creater;
	}
	public void setCreater(String creater) {
		this.creater = creater;
	}
	public int getEmpId() {
		return empId;
	}
	public void setEmpId(int empId) {
		this.empId = empId;
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
	public String getShift() {
		return shift;
	}
	public void setShift(String shift) {
		this.shift = shift;
	}
	public float getUsedHours() {
		return usedHours;
	}
	public void setUsedHours(float usedHours) {
		this.usedHours = usedHours;
	}
	public float getRemainingHours() {
		return remainingHours;
	}
	public void setRemainingHours(float remainingHours) {
		this.remainingHours = remainingHours;
	}

	
}
