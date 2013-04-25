package com.mdmp.android.feedback;

public class Feedback {
	public static final int USER_NORMAL = 0;
	public static final int USER_DEVELOPER = 1;
	private int userType;
	private String userInfo;
	private String feedback;
	private String timestamp;

	public Feedback(String feedback) {
		this.feedback = feedback;
		this.userType = 0;
	}

	public int getUserType() {
		return this.userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}

	public String getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getFeedback() {
		return this.feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

	public String getUserInfo() {
		return this.userInfo;
	}

	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}
}
