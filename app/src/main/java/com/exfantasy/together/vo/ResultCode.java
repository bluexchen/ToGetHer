package com.exfantasy.together.vo;

public class ResultCode {
	public static final int SUCCEED = 0;

	public static final int COMMUNICATION_ERROR = 4444;
	public static final int SERVER_ERROR = 9999;

	public static final int REGISTER_FAILED_EMAIL_ALREADY_USED = 1000;

	public static final int LOGIN_FAILED_CANNOT_FIND_USER_BY_EMAIL = 2000;
	public static final int LOGIN_FAILED_PASSWORD_INVALID = 2001;

	public static final int CREATE_EVENT_FAILED = 3000;
}
