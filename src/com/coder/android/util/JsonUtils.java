package com.coder.android.util;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class JsonUtils {

	/**
	 * Object to string
	 * 
	 * @param obj
	 * @return
	 */
	public static byte[] convertFrom(JSONObject obj) throws Exception {
		if (obj == null) {
			return "".getBytes("utf-8");
		}
		return obj.toString().getBytes("utf-8");
	}

	/**
	 * Object to string without specified element
	 * 
	 * @param obj
	 * @param elementName
	 * @return
	 */
	public static String convertAndRemove(Object obj, String elementName) {
		JSONObject original = JSONObject.fromObject(obj);
		original.remove(elementName);
		return original.toString();
	}

	public static String convertAndReplace(Object obj, String elementName,
			String newValue) {
		JSONObject original = JSONObject.fromObject(obj);
		original.element(elementName, newValue);
		return original.toString();
	}

	public static <T> String convertFrom(List<T> list) {
		if (list == null || list.size() == 0) {
			return "[]";
		}

		JSONArray array = JSONArray.fromObject(list);
		return array.toString();
	}

	/**
	 * @param jsonStr
	 * @return
	 */
	public static <T> T convertFrom(String jsonStr, Class<T> clazz)
			throws Exception {
		return mapper.readValue(jsonStr, clazz);
	}

	public static <T> List<T> convertToList(String jsonStr, Class<T> clazz)
			throws Exception {
		List<T> list = new ArrayList<T>();
		JSONArray array = JSONArray.fromObject(jsonStr);
		for (int i = 0; i < array.size(); i++) {
			if (clazz == String.class) {
				list.add((T) array.getString(i));
			} else {
				list.add((T) mapper.readValue(array.getString(i), clazz));
			}
		}

		return list;
	}

}
