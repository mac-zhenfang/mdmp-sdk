package com.coder.android.transfer;

import java.util.ArrayList;
import java.util.List;

public class CrashData {
	private String name;
	private String stack;
	private List<Long> timeList;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStack() {
		return this.stack;
	}

	public void setStack(String stack) {
		this.stack = stack;
	}

	public List<Long> getTimeList() {
		return this.timeList;
	}

	public synchronized void addTime(long time) {
		if (this.timeList == null) {
			this.timeList = new ArrayList();
		}
		this.timeList.add(Long.valueOf(Math.round(time / 1000.0D)));
	}
}