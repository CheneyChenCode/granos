package com.grace.granos.model;

import java.sql.Timestamp;
import java.util.Objects;

import org.springframework.stereotype.Component;

@Component
public class AttendanceModel {
	
	@Override
	public int hashCode() {
		return Objects.hash(day, dayCode, empId, month, seq, shift, year);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttendanceModel other = (AttendanceModel) obj;
		return day == other.day && dayCode == other.dayCode && empId == other.empId && month == other.month
				&& seq == other.seq && Objects.equals(shift, other.shift) && year == other.year;
	}
	private int seq;
	private int dayCode;
	private int week;
	private Timestamp arrivalDatetime;
	private Timestamp leaveDatetime;
	private float workHours;
	private String overtimeReason;
	private String approval;
	private String note;
	private int month;
	private int empId;
	private int year;
	private int day;
	private Timestamp startDatetime;
	private Timestamp endDatetime;
	private String reason;
	private String shift;
	private float overtime;
	private int status;
	private String creater;
	private float compTime;
	private String compReason;
	private int period;
	private float paidLeave;
	private float remainTaxFree;
	private Timestamp overStartDatetime;
	private Timestamp overEndDatetime;
	private String abnormalCode;
	private float taxFree;
	
	public float getTaxFree() {
		return taxFree;
	}
	public void setTaxFree(float taxFree) {
		this.taxFree = taxFree;
	}
	public String getAbnormalCode() {
		return abnormalCode;
	}
	public void setAbnormalCode(String abnormalCode) {
		this.abnormalCode = abnormalCode;
	}
	public Timestamp getOverStartDatetime() {
		return overStartDatetime;
	}
	public void setOverStartDatetime(Timestamp overStartDatetime) {
		this.overStartDatetime = overStartDatetime;
	}
	public Timestamp getOverEndDatetime() {
		return overEndDatetime;
	}
	public void setOverEndDatetime(Timestamp overEndDatetime) {
		this.overEndDatetime = overEndDatetime;
	}
	public float getPaidLeave() {
		return paidLeave;
	}
	public void setPaidLeave(float paidLeave) {
		this.paidLeave = paidLeave;
	}
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}
	public float getCompTime() {
		return compTime;
	}
	public void setCompTime(float compTime) {
		this.compTime = compTime;
	}
	public String getCompReason() {
		return compReason;
	}
	public void setCompReason(String compReason) {
		this.compReason = compReason;
	}
	public void setDayCode(int dayCode) {
		this.dayCode = dayCode;
	}
	public String getCreater() {
		return creater;
	}
	public void setCreater(String creater) {
		this.creater = creater;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public float getOvertime() {
		return overtime;
	}
	public void setOvertime(float overtime) {
		this.overtime = overtime;
	}
	public String getShift() {
		return shift;
	}
	public void setShift(String shift) {
		this.shift = shift;
	}
	public Timestamp getStartDatetime() {
		return startDatetime;
	}
	public void setStartDatetime(Timestamp startDatetime) {
		this.startDatetime = startDatetime;
	}
	public Timestamp getEndDatetime() {
		return endDatetime;
	}
	public void setEndDatetime(Timestamp endDatetime) {
		this.endDatetime = endDatetime;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public int getEmpId() {
		return empId;
	}
	public void setEmpId(int empId) {
		this.empId = empId;
	}
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public int getWeek() {
		return week;
	}
	public void setWeek(int week) {
		this.week = week;
	}
	public Timestamp getArrivalDatetime() {
		return arrivalDatetime;
	}
	public void setArrivalDatetime(Timestamp arrivalDatetime) {
		this.arrivalDatetime = arrivalDatetime;
	}
	public Timestamp getLeaveDatetime() {
		return leaveDatetime;
	}
	public void setLeaveDatetime(Timestamp leaveDatetime) {
		this.leaveDatetime = leaveDatetime;
	}
	public float getWorkHours() {
		return workHours;
	}
	public void setWorkHours(float workHours) {
		this.workHours = workHours;
	}
	public String getOvertimeReason() {
		return overtimeReason;
	}
	public void setOvertimeReason(String overtimeReason) {
		this.overtimeReason = overtimeReason;
	}
	public String getApproval() {
		return approval;
	}
	public void setApproval(String approval) {
		this.approval = approval;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	public int getDayCode() {
		return dayCode;
	}
	public float getRemainTaxFree() {
		return remainTaxFree;
	}
	public void setRemainTaxFree(float remainTaxFree) {
		this.remainTaxFree = remainTaxFree;
	}

}
