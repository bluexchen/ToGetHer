package com.exfantasy.together.vo;

public class LoginResult {
	/**
	 * 登入結果
	 */
	private int resultCode;
	/**
	 * 登入結果訊息
	 */
	private String resultMsg;
	/**
	 * 暱稱
	 */
	private String name;
	/**
	 * 頭像 url
	 */
	private String userIconUrl;
	/**
	 * 發起活動需要
	 */
	private String email;

	public LoginResult() {
	}

	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultMsg() {
		return resultMsg;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserIconUrl() {
		return userIconUrl;
	}

	public void setUserIconUrl(String userIconUrl) {
		this.userIconUrl = userIconUrl;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "LoginResult [resultCode=" + resultCode + ", resultMsg=" + resultMsg + ", name=" + name
				+ ", userIconUrl=" + userIconUrl + ", email=" + email + "]";
	}
}