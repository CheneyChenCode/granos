package com.grace.granos.model;

import java.sql.Timestamp;

import org.springframework.stereotype.Component;

@Component
public class ShiftModel {
	private int id;
	private String name;
	private Timestamp startTime;
	private Timestamp endTime;
	private float baseHours;
	private float restHours;
	private float restStartHour;
	private String description;;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public float getRestStartHour() {
		return restStartHour;
	}
	public void setRestStartHour(float restStartHour) {
		this.restStartHour = restStartHour;
	}
	public float getBaseHours() {
		return baseHours;
	}
	public void setBaseHours(float baseHours) {
		this.baseHours = baseHours;
	}
	public float getRestHours() {
		return restHours;
	}
	public void setRestHours(float restHours) {
		this.restHours = restHours;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public Timestamp getEndTime() {
		return endTime;
	}
	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

}
