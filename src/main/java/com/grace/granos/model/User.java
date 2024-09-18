package com.grace.granos.model;

import org.springframework.stereotype.Component;

@Component
public class User {
	private int empId;
	private String username;
	private String nameEn;
	private String nameCn;
	private String lastNameEn;
	private String lastNameCn;
	private String position;
	private String loginMessage;
	private String email;
	private int character;
	private String characterNameEn;
	private String characterNameCn;
	
	public String getCharacterNameEn() {
		return characterNameEn;
	}
	public void setCharacterNameEn(String characterNameEn) {
		this.characterNameEn = characterNameEn;
	}
	public String getCharacterNameCn() {
		return characterNameCn;
	}
	public void setCharacterNameCn(String characterNameCn) {
		this.characterNameCn = characterNameCn;
	}
	public int getCharacter() {
		return character;
	}
	public void setCharacter(int character) {
		this.character = character;
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

}
