package com.mdmp.android.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cisco.ramp.api.APIConstants;
import com.cisco.ramp.api.entities.ErrorCode;
import com.cisco.ramp.api.entities.RequestResponse;

/**
 * Class Description
 *
 * @author Denny Ye
 * @since 2012-11-20
 * @version 1.0
 */
public class Utils {

  public static Map<String, String> getRandomToken() {
    Map<String, String> header = new HashMap<String, String>();
    header.put(APIConstants.USER_TOKEN, StringUtils.getRandomEmail());
    return header;
  }
  
  public static Map<String, String> getRandomToken(String token) {
    Map<String, String> header = new HashMap<String, String>();
    header.put(APIConstants.USER_TOKEN, token);
    return header;
  }
  
  public static int requestResponseStatCode(String uri, String method, Object original, 
		  Map<String, String> header) throws Exception {
	  RequestResponse result = null;
	  
	  if (original instanceof String) {
		  result = getResult(uri, method, (String) original, header, false);
	  } else {
		  result = getResult(uri, method, JsonUtils.convertFrom(original), header, false);
	  }
	  
	  if (result == null) {
		  return -1;
	  }
	  return result.getStatCode();
  }
  
  public static <T> ErrorCode requestFail(String uri, String method, Object original, 
                                        Map<String, String> header) throws Exception {
	  RequestResponse result = null;
    
    if (original instanceof String) {
      result = getResult(uri, method, (String) original, header, false);
    } else {
      result = getResult(uri, method, JsonUtils.convertFrom(original), header, false);
    }
    
    if (result == null || result.getContent().trim().isEmpty()) {
      return null;
    }
    
    return JsonUtils.convertFrom(result.getContent(), ErrorCode.class); 
  }
  
  public static <T> T request(String uri, String method, Object original, Map<String, String> header, 
                                      Class<T> clazz) throws Exception {
	  RequestResponse result = null;
    
    if (original instanceof String) {
      result = getResult(uri, method, (String) original, header, true);
    } else {
      result = getResult(uri, method, JsonUtils.convertFrom(original), header, true);
    }
    
    if (clazz == null || result == null || result.getContent().trim().isEmpty()) {
      return null;
    }
    
    return JsonUtils.convertFrom(result.getContent(), clazz); 
  }
  
  public static <T> List<T> requestList(String uri, String method, String content, Map<String, String> header, 
      Class<T> clazz) throws Exception {
	  RequestResponse result = getResult(uri, method, content, header, true);
    return JsonUtils.convertToList(result.getContent(), clazz);
  }
  
  /*public static <T> List<T> requestMap(String uri, String method, String content, Map<String, String> header, 
		  Class<T> clazz) throws Exception {
	  String result = getResult(uri, method, content, header, true);
	  return JsonUtils.convertToMap(result, clazz);
  }*/
  
  public static InputStream requestStream(String uri, String method, String content, 
      Map<String, String> header) throws Exception {
    return HttpClientUtil.requestStream(uri, header, content, true);
  }
  
  public static RequestResponse getResult(String uri, String method, String content, 
                        Map<String, String> header, boolean isSucc) throws Exception {
	  RequestResponse result = null;
    if (method.equalsIgnoreCase("POST")) {
      result = HttpClientUtil.requestPost(uri, header, content, isSucc);
    } else if (method.equalsIgnoreCase("GET")) {
      result = HttpClientUtil.requestGet(uri, header, isSucc);
    } else if (method.equalsIgnoreCase("PUT")) {
      result = HttpClientUtil.requestPut(uri, header, content, isSucc);
    } else {
      result = HttpClientUtil.requestDelete(uri, header, isSucc);
    }
    
    return result;
  }
  
  public static void setTimeout(int to){
	  HttpClientUtil.setTimeout(to);
  }
  
}



