package com.grace.granos.model;

import org.springframework.stereotype.Component;

@Component
public class PayrollDataTableModel {
	private int empId;
	private int year;
	private String month;
	private int day;
	private int payCode;
	private float hours;
	private String title;
	private float weighted;
	private float taxFreeOverTime;
	private float taxFreeOverTimeWeighted;
	
	public float getTaxFreeOverTimeWeighted() {
		return taxFreeOverTimeWeighted;
	}
	public void setTaxFreeOverTimeWeighted(float taxFreeOverTimeWeighted) {
		this.taxFreeOverTimeWeighted = taxFreeOverTimeWeighted;
	}
	public float getTaxFreeOverTime() {
		return taxFreeOverTime;
	}
	public void setTaxFreeOverTime(float taxFreeOverTime) {
		this.taxFreeOverTime = taxFreeOverTime;
	}
	public float getWeighted() {
		return weighted;
	}
	public void setWeighted(float weighted) {
		this.weighted = weighted;
	}
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
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public int getPayCode() {
		return payCode;
	}
	public void setPayCode(int payCode) {
		this.payCode = payCode;
	}
	public float getHours() {
		return hours;
	}
	public void setHours(float hours) {
		this.hours = hours;
	}
}
