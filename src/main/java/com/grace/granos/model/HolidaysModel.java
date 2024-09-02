package com.grace.granos.model;

import java.sql.Date;

import org.springframework.stereotype.Component;

@Component
public class HolidaysModel {
	private Date date;
	private int dayCode;
	private String description;
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public int getDayCode() {
		return dayCode;
	}
	public void setDayCode(int dayCode) {
		this.dayCode = dayCode;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
