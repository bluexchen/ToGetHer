package com.exfantasy.together.vo;

import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable {
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

	public  Message(Parcel in){
		long[] ldata = new long[2];
		String[] sdata = new String[1];
		int[] idata = new int[2];

		in.readLongArray(ldata);
		in.readStringArray(sdata);
		in.readIntArray(idata);

		this.messageId = ldata[0];
		this.createUserId = ldata[1];
		this.content = sdata[0];
		this.date = idata[0];
		this.time = idata[1];
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLongArray(new long[] {
				this.messageId,
				this.createUserId
		});

		dest.writeStringArray(new String[]{
				this.content,
		});

		dest.writeIntArray(new int[]{
				this.date,
				this.time
		});
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

		@Override
		public Object createFromParcel(Parcel source) {
			return new Message(source);
		}

		@Override
		public Object[] newArray(int size) {
			return new Message[size];
		}
	};
}