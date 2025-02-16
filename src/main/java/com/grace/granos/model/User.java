package com.grace.granos.model;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Component
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class User {
	private static final AtomicLong idCounter = new AtomicLong();  // 全局的自增计数器
	private long id;
	private int empId;
	private String username;
	private String nameEn;
	private String nameCn;
	private String lastNameEn;
	private String lastNameCn;
	private String position;
	private String loginMessage;
	private String email;
//	private int character;
//	private String characterNameEn;
//	private String characterNameCn;
//	private String characterLastNameEn;
//	private String characterLastNameCn;
	private int jobId;
	private String organization;
	private String gender;
	private User character;
	
	public User() {
		this.id=idCounter.incrementAndGet();
	}
	public User getCharacter() {
		return character;
	}
	public void setCharacter(User character) {
		this.character = character;
	}
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public int getJobId() {
		return jobId;
	}
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	public int getEmpId() {
		return empId;
	}
	public void setEmpId(int empId) {
		this.empId = empId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getNameEn() {
		return nameEn;
	}
	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}
	public String getNameCn() {
		return nameCn;
	}
	public void setNameCn(String nameCn) {
		this.nameCn = nameCn;
	}
	public String getLastNameEn() {
		return lastNameEn;
	}
	public void setLastNameEn(String lastNameEn) {
		this.lastNameEn = lastNameEn;
	}
	public String getLastNameCn() {
		return lastNameCn;
	}
	public void setLastNameCn(String lastNameCn) {
		this.lastNameCn = lastNameCn;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getLoginMessage() {
		return loginMessage;
	}
	public void setLoginMessage(String loginMessage) {
		this.loginMessage = loginMessage;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
}
