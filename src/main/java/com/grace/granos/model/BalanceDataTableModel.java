package com.grace.granos.model;

import org.springframework.stereotype.Component;

@Component
public class BalanceDataTableModel {
	private int empId;
	private int year;
	private String month;
	private String shift;
	private float usedHours;
	private float remainingHours;
	private String title;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
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
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
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
