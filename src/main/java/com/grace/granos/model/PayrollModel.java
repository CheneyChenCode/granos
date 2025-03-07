package com.grace.granos.model;

import java.util.Objects;

import org.springframework.stereotype.Component;

@Component
public class PayrollModel {
	private int empId;
	private int year;
	private int month;
	private int day;
	private int payCode;
	private float hours;
	private float taxFreeHours;
	private String creater;
	private float fromHour;
	private float toHour;
	private String nameEn;
	private String lastNameEn;
	//pay_code table
	private String title;
	private float coefficient;

	public String getNameEn() {
		return nameEn;
	}
	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}
	public String getLastNameEn() {
		return lastNameEn;
	}
	public void setLastNameEn(String lastNameEn) {
		this.lastNameEn = lastNameEn;
	}
	public float getFromHour() {
		return fromHour;
	}
	public void setFromHour(float fromHour) {
		this.fromHour = fromHour;
	}
	public float getToHour() {
		return toHour;
	}
	public void setToHour(float toHour) {
		this.toHour = toHour;
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
    public float getTaxFreeHours() {
		return taxFreeHours;
	}
	public void setTaxFreeHours(float taxFreeHours) {
		this.taxFreeHours = taxFreeHours;
	}
	public String getCreater() {
		return creater;
	}
	public void setCreater(String creater) {
		this.creater = creater;
	}
	@Override
	public int hashCode() {
		return Objects.hash(day, empId, month, payCode, year);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PayrollModel other = (PayrollModel) obj;
		return day == other.day && empId == other.empId && month == other.month && payCode == other.payCode
				&& year == other.year;
	}

}
