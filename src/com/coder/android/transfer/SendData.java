package com.coder.android.transfer;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


public class SendData {
	private String name;
	private JSONObject json;
	private List<Integer> idList;
	
	// FIXME: this method not good
	public void addEventId(Integer id){
		if(!idList.contains(id)){
			idList.add(id);
		}
	}
	
	// FIXME: this method not good
	public void addData(SendData data) throws JSONException{
		json.put(data.getName(), data.getJson());
		idList.addAll(data.getIdList());
	}
	
	public JSONObject getJson() {
		return this.json;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}

	public List<Integer> getIdList() {
		return this.idList;
	}

	public void setIdList(List<Integer> idList) {
		this.idList = idList;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}