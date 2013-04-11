package com.mdmp.android.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class ConfigService {
	private static SharedPreferences pref = null;
	private static String PREF_NAME = "mofan.config";
	private static String baseUrl;
	private static Map<String, String> receiveMap = new HashMap<String, String>();
	private static Map<String, String> changeMap = new HashMap<String, String>();

	public static ConfigService getInstance() {
		return Instance.service;
	}

	public void updateConfig(Context context, ConfigListener configListener) {
		if (context == null)
			throw new IllegalArgumentException("请传入正确的Context对象");
		initUrlAndAppKey(context);
		pref = context.getSharedPreferences(PREF_NAME, 0);
		try {
			SharedPreferences.Editor editor = pref.edit();
			String getStringFromServ = getRequest(baseUrl);
			JSONObject json = new JSONObject(getStringFromServ);
			getValueFromJSONObj(json);
			editor.commit();
			if (configListener != null) {
				configListener.onReceived(context, receiveMap);
				configListener.onChanged(context, changeMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (configListener != null) {
				configListener.onFailed(context);
			}
		}
	}

	public void updateConfig(Context context) {
		updateConfig(context, null);
	}

	private void initUrlAndAppKey(Context context) {
		try {
			ApplicationInfo info = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(), 128);

			String app_key = (String) info.metaData.get("MOFANG_APPKEY");
			baseUrl = "http://m.imofan.com/online_config/?app_key=" + app_key;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(
					"请在Manifest文件中配置meta-data，键为MOFANG_APPKEY,值为您的app_key");
		}
	}

	public String getConfig(String key) {
		if (pref == null) {
			return "";
		}
		return pref.getString(key, "");
	}

	private String getRequest(String url) throws ClientProtocolException,
			IOException, ParseException {
		if ((url.equals("")) || (url == null))
			return "";
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		HttpResponse response = client.execute(get);
		if (response.getStatusLine().getStatusCode() == 200) {
			String result = EntityUtils.toString(response.getEntity());
			return result;
		}
		return "";
	}

	private void getValueFromJSONObj(JSONObject json) throws JSONException {
		Iterator<?> iter = json.keys();
		SharedPreferences.Editor editor = pref.edit();

		while (iter.hasNext()) {
			String key = (String) iter.next();
			String value = json.optString(key);
			receiveMap.put(key, json.optString(key));
			if ((!pref.contains(key))
					|| (!pref.getString(key, "").equals(value))) {
				editor.putString(key, json.optString(key));
				changeMap.put(key, value);
			}
		}
		editor.commit();
	}

	private static class Instance {
		static ConfigService service = new ConfigService();
	}
}
