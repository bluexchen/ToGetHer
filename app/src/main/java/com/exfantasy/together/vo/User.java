package com.exfantasy.together.vo;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
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

	public  User(Parcel in){
		long[] ldata = new long[1];
		String[] sdata = new String[2];

		in.readLongArray(ldata);
		in.readStringArray(sdata);

		this.userId = ldata[0];
		this.email = sdata[0];
		this.name = sdata[1];
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLongArray(new long[] {
				this.userId
		});

		dest.writeStringArray(new String[]{
				this.email,
				this.name
		});
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

		@Override
		public Object createFromParcel(Parcel source) {
			return new User(source);
		}

		@Override
		public Object[] newArray(int size) {
			return new User[size];
		}
	};
}