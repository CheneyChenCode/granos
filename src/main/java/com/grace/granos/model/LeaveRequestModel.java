package com.grace.granos.model;

import java.sql.Timestamp;

import org.springframework.stereotype.Component;

@Component
public class LeaveRequestModel {
	
	private int id;
	private int empId;
	private int year;
	private int month;
	private int day;
	private int seq;
	private String shift;
	private Timestamp fromTime;
	private Timestamp toTime;
	private float hours;
	private int status;
	private String reason;
	private String note;
	private String source;
	private String requester;
	private String approvedBy;
	private Timestamp approvedTime;
	
	public Timestamp getFromTime() {
		return fromTime;
	}
	public void setFromTime(Timestamp fromTime) {
		this.fromTime = fromTime;
	}
	public Timestamp getToTime() {
		return toTime;
	}
	public void setToTime(Timestamp toTime) {
		this.toTime = toTime;
	}
	public String getApprovedBy() {
		return approvedBy;
	}
	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}
	public Timestamp getApprovedTime() {
		return approvedTime;
	}
	public void setApprovedTime(Timestamp approvedTime) {
		this.approvedTime = approvedTime;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	public String getShift() {
		return shift;
	}
	public void setShift(String shift) {
		this.shift = shift;
	}
	public float getHours() {
		return hours;
	}
	public void setHours(float hours) {
		this.hours = hours;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getRequester() {
		return requester;
	}
	public void setRequester(String requester) {
		this.requester = requester;
	}
}
