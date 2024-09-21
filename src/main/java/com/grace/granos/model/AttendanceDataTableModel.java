package com.grace.granos.model;

import org.springframework.stereotype.Component;

@Component
public class AttendanceDataTableModel {
	private int empid;
	private String week;
	private int year;
	private int day;
	private String month;
	private int seq;
	private String startDate;
	private String endDate;
	private String arrivalDate;
	private String leaveDate;
	private String hours;
	private String overtime;
	private String reason;
	private String note;
	private String shift;
	private String approval;
	private String totalHours;
	private String totalOverTime;
	private int status;
	private int dayCode;
	private int period;
	private String taxFreeOverTime;
	
	public String getTaxFreeOverTime() {
		return taxFreeOverTime;
	}
	public void setTaxFreeOverTime(String taxFreeOverTime) {
		this.taxFreeOverTime = taxFreeOverTime;
	}
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}
	public int getDayCode() {
		return dayCode;
	}
	public void setDayCode(int dayCode) {
		this.dayCode = dayCode;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getTotalHours() {
		return totalHours;
	}
	public void setTotalHours(String totalHours) {
		this.totalHours = totalHours;
	}
	public String getTotalOverTime() {
		return totalOverTime;
	}
	public void setTotalOverTime(String totalOverTime) {
		this.totalOverTime = totalOverTime;
	}
	public String getApproval() {
		return approval;
	}
	public void setApproval(String approval) {
		this.approval = approval;
	}
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getArrivalDate() {
		return arrivalDate;
	}
	public void setArrivalDate(String arrivalDate) {
		this.arrivalDate = arrivalDate;
	}
	public String getLeaveDate() {
		return leaveDate;
	}
	public void setLeaveDate(String leaveDate) {
		this.leaveDate = leaveDate;
	}
	public String getHours() {
		return hours;
	}
	public void setHours(String hours) {
		this.hours = hours;
	}
	public String getOvertime() {
		return overtime;
	}
	public void setOvertime(String overtime) {
		this.overtime = overtime;
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
	public String getShift() {
		return shift;
	}
	public void setShift(String shift) {
		this.shift = shift;
	}
	public String getWeek() {
		return week;
	}
	public void setWeek(String week) {
		this.week = week;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public int getEmpid() {
		return empid;
	}
	public void setEmpid(int empid) {
		this.empid = empid;
	}
	
}
