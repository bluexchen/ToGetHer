package com.exfantasy.together.vo;

public class ResultCode {
	public static final int SUCCEED = 0;

	public static final int COMMUNICATION_ERROR = 4444;
	public static final int SERVER_ERROR = 9999;

	public static final int REGISTER_FAILED_EMAIL_ALREADY_USED = 1000;

	public static final int LOGIN_FAILED_CANNOT_FIND_USER_BY_EMAIL = 2000;
	public static final int LOGIN_FAILED_PASSWORD_INVALID = 2001;

	public static final int CREATE_EVENT_FAILED = 3000;

	public static final int JOIN_EVENT_FAILED_WITH_USER_IS_NULL = 4000;
	public static final int JOIN_EVENT_FAILED_WITH_EVENT_IS_NULL = 4001;
	public static final int JOIN_EVENT_FAILED_WITH_JOIN_USER_CREATED = 4002;
	public static final int JOIN_EVENT_FAILED_WITH_ALREADY_JOINED = 4003;
	public static final int JOIN_EVENT_FAILED_WITH_EXCEPTION = 4004;
}
