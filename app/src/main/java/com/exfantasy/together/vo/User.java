package com.exfantasy.together.vo;

public class User {
	private long userId;

	private String email;
	
	private String name;
	
	public User() {
	}
	
	public User(long userId, String email, String name) {
		this.userId = userId;
		this.email = email;
		this.name = name;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", email=" + email + ", name=" + name + "]";
	}
}