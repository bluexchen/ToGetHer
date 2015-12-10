package com.exfantasy.together.vo;

public class Message {
	/**
	 * 留言 ID
	 */
	private long messageId;
	/**
	 * 建立留言使用者 ID
	 */
	private long createUserId;
	/**
	 * 留言內容
	 */
	private String content;
	/**
	 * 留言日期
	 */
	private int date;
	/**
	 * 留言時間
	 */
	private int time;

	public Message() {
	}

	public Message(long messageId, long createUserId, String content, int date, int time) {
		this.messageId = messageId;
		this.createUserId = createUserId;
		this.content = content;
		this.date = date;
		this.time = time;
	}

	public long getMessageId() {
		return messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	public long getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(long createUserId) {
		this.createUserId = createUserId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getDate() {
		return date;
	}

	public void setDate(int date) {
		this.date = date;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "Message [messageId=" + messageId + ", createUserId=" + createUserId + ", content=" + content + ", date="
				+ date + ", time=" + time + "]";
	}
}