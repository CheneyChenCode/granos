package com.grace.granos.model;

import org.springframework.stereotype.Component;

@Component
public class PayCodeModel {
	private int id;
	private int dayCode;
	private String shift;
	private String hourPartGreater;
	private String hourPartLess;
	private float coefficient;
	private String description;
	private String title;

	public String getShift() {
		return shift;
	}

	public void setShift(String shift) {
		this.shift = shift;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDayCode() {
		return dayCode;
	}

	public void setDayCode(int dayCode) {
		this.dayCode = dayCode;
	}

	public String getHourPartGreater() {
		return hourPartGreater;
	}

	public void setHourPartGreater(String hourPartGreater) {
		this.hourPartGreater = hourPartGreater;
	}

	public String getHourPartLess() {
		return hourPartLess;
	}

	public void setHourPartLess(String hourPartLess) {
		this.hourPartLess = hourPartLess;
	}

	public float getCoefficient() {
		return coefficient;
	}

	public void setCoefficient(float coefficient) {
		this.coefficient = coefficient;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
