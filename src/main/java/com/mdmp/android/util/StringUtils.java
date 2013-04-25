package com.mdmp.android.util;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * Class Description
 *
 * @author Denny Ye
 * @since 2012-11-20
 * @version 1.0
 */
public class StringUtils {

  
  private static Random ran = new Random();
  
  public static String getRandomStr() {
    return getRandomStr(10);
  }
  
  public static String getStrNotStartWithUnderLine() {
    while (true) {
      String str = getRandomStr();
      if (str.startsWith("_")) {
        continue;
      }
      
      return str;
    }
  }
  
  public static String getRandomStr(int len) {
    if (len <= 0) {
      len = 10;
    }
    
    StringBuffer appender = new StringBuffer();
    
    for (int i = 0; i < len; i++) {
      int index = ran.nextInt(62);
      
      if (index < 10) {
        appender.append((char) (index + 48)); // number
      } else if (index < 36) {
        appender.append((char) (index - 10 + 65)); // A-Z
      } else if (index > 37) {
        appender.append((char) (index - 37 + 97)); // a-z
      } else {
        appender.append("a");
      }
    }
    
    appender.replace(0, 1, "a");
    
    return appender.toString();
  }
  
  public static String getRandomStrWithFixedPrefix(int len) {
    return "a" + getRandomStr(len - 1);
  }
  
  public static String getLongStr(int len) {
    return getRandomStr(len);
  }
  
  public static String getSpecialStr() {
    StringBuffer appender = new StringBuffer();
    
    for (int i = 0; i < 10; i++) {
      int index = ran.nextInt(33);
      
      if (index < 16) {
        appender.append((char) (index + 32)); 
      } else if (index < 23) {
        appender.append((char) (index - 16 + 58)); 
      } else if (index > 29) {
        appender.append((char) (index - 23 + 91)); 
      } else {
        appender.append((char) (index - 29 + 123)); 
      }
    }
    
    return appender.toString();
  }
  
  public static String getDBUnfriendlyStr() {
    return "a%20or%201=1";
  }
  
  public static String getStrWithSpace() {
    return getRandomStr(5) + " " + getRandomStr(5);
  }
  
  public static String getStrWithSpaceInGet() {
    return getRandomStr(5) + "%20" + getRandomStr(5);
  }
  
  public static String getStrWithChinese() {
    return getRandomStr() + "_思科";
  }
  
  public static String getRandomEmail() {
     return getRandomStr(8) + "@cisco.com";
  }
  
  public static String[] getIllegalStrs() {
    String[] arrs = new String[] {getStrWithChinese(),
                                  getDBUnfriendlyStr(),
                                  getLongStr(1000), 
                                  getStrWithSpaceInGet(),
                                  };
    
    return arrs;
  }
  
  
  public static String getRandomIp() {
	  StringBuilder sb  = new StringBuilder();
	  sb.append(10);
	  sb.append(".");
	  sb.append(224);
	  sb.append(".");
	  sb.append(ran.nextInt(255));
	  sb.append(".");
	  sb.append(ran.nextInt(255));
	  return sb.toString();
  }
  
  public static String encodeToHex(String s) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = Integer.toHexString(ch);
			hexString.append(s4);
		}
		return hexString.toString();
	}
	
	public static String decodeFromHex(String hexString, String charsetName) throws UnsupportedEncodingException {
		byte[] baKeyword = new byte[hexString.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			baKeyword[i] = (byte) (0xff & Integer.parseInt(
					hexString.substring(i * 2, i * 2 + 2), 16));
		}
		hexString = new String(baKeyword, charsetName);// UTF-16le:Not
		return hexString;
	}
}



