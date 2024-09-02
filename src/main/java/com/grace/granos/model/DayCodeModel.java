package com.grace.granos.model;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class DayCodeModel {
	private int dayCode;
	private String description;
	private List<PayCodeModel> payCode;

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
	public List<PayCodeModel> getPayCode() {
		return payCode;
	}
	public void setPayCode(List<PayCodeModel> payCode) {
		this.payCode = payCode;
	}

	
}
