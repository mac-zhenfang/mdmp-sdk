package com.mdmp.android;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.mdmp.android.AccessPath.Node;
import com.mdmp.android.db.DatabaseManager;
import com.mdmp.android.event.StatEvent;
import com.mdmp.android.transfer.CrashData;
import com.mdmp.android.transfer.SendData;
import com.mdmp.android.util.JsonUtils;

public class DataAgent {
	public static final boolean DEBUG = false;
	private static boolean developDebug = false;
	static final String SDK_VERSION = "1.0.2";
	public static final String LOG_TAG = "MDMP_DataAgent";
	public static final String APPKEY = "MDMP_APPKEY";
	private static boolean init = false;
	private static final String COLLECTOR = "http://c.mdmp.com/";
	private static final String REGISTER = "http://m.mdmp.com/register/";
	private static final int SEND_WAY_OPEN = 1;
	private static final int SEND_WAY_DAY = 2;
	private static final int SEND_WAY_REAL_TIME = 3;
	private static final int SEND_WAY_WIFI = 4;
	private static int sendWay = 1;
	private static int maxLeaveTime = 30000;
	private static int minUseTime = 3000;
	private static int maxUseTime = 2160000;

	private static String appKey = null;
	private static DatabaseManager dbManager = null;

	private static boolean newStart = true;
	private static Class currentActivity = null;
	private static String currentActivityAlias = null;
	private static long resumeTime = -1L;
	private static int activityId = 0;

	public static synchronized void init(Activity activity) {
		if (!init) {
			if (getAppKey(activity) != null) {
				if (dbManager == null) {
					dbManager = DatabaseManager.getInstance(activity);
				}

				StatInfo.init(activity);
				long now = System.currentTimeMillis();
				int[] intTime = getDateAndHour(now);
				StatInfo.updateAppAndDeviceInfo(dbManager, activity,
						intTime[0], intTime[1]);
				init = true;
			}
		} else
			newStart = false;
	}

	public static String getAppKey(Context context) {
		if (appKey == null) {
			try {
				Object appKeyObj = context.getPackageManager()
						.getApplicationInfo(context.getPackageName(), 128).metaData
						.get(APPKEY);

				if ((appKeyObj instanceof Integer))
					appKey = Integer.toString(((Integer) appKeyObj).intValue());
				else if ((appKeyObj instanceof Long))
					appKey = Long.toString(((Long) appKeyObj).intValue());
				else {
					appKey = (String) appKeyObj;
				}
				Log.i("MDMP", new StringBuilder().append("mdmp appkey: ")
						.append(appKey).toString());
			} catch (Exception e) {
				Log.e("MDMP",
						"[getAppKey]Can't find metadata \"MDMP_APPKEY\" in AndroidManifest.xml");
				e.printStackTrace();
			}
		}
		return appKey;
	}

	public static String getDevId(Context context) {
		String devId = null;
		try {
			devId = StatInfo.getString("dev_id", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return devId;
	}

	public static void debug() {
		developDebug = true;
	}

	public static boolean isDebug() {
		return developDebug;
	}

	public static void enableCrashCollector(Activity context) {
		init(context);

		ErrorHandler.init(dbManager);
	}

	public static void onResume(Activity context, String name) {
		init(context);
		
		resumeTime = System.currentTimeMillis();
		int[] intTime = getDateAndHour(resumeTime);

		currentActivity = context.getClass();
		if ((name != null) && (name.trim().length() > 0))
			currentActivityAlias = name;
		else {
			currentActivityAlias = currentActivity.getName();
		}

		if (activityId > 0)
			activityId += 1;
		else {
			activityId = (int) (resumeTime - resumeTime / 1000000000L * 1000000000L);
		}

		long activityPauseTime = StatInfo.getLong("user_pause_time", -1L);
		if ((newStart) || (resumeTime - activityPauseTime > maxLeaveTime)) {
			AccessPath.addAccessNode(0, null);

			long appUseDuration = StatInfo.getLong("user_duration", 0L);
			if ((appUseDuration > minUseTime) && (appUseDuration <= maxUseTime)) {
				recordCloseEvent(appUseDuration);
			}

			StatInfo.putLong("user_duration", 0L);

			recordOpenEvent(resumeTime);

			startEventSender(context, resumeTime);
		}

		AccessPath.addAccessNode(activityId, currentActivityAlias);
	}

	public static void onResume(Activity context) {
		onResume(context, null);
	}

	private static void onPause(Activity context, long duration) {
		long pauseTime = System.currentTimeMillis();

		StatInfo.putLong("user_pause_time", pauseTime);

		if (resumeTime > 0L) {
			long appUseDuration = StatInfo.getLong("user_duration", 0L);
			long activityUseDuration = pauseTime - resumeTime;
			appUseDuration += activityUseDuration;
			StatInfo.putLong("user_duration", appUseDuration);

			if (duration >= 0L) {
				if (duration <= maxUseTime) {
					AccessPath.updateAccessDuration(activityId, duration);
				}
			} else if (activityUseDuration <= maxUseTime)
				AccessPath
						.updateAccessDuration(activityId, activityUseDuration);
		}
	}

	public static void onPause(Activity context) {
		onPause(context, -1L);
	}

	public static void onEvent(Activity context, String eventKey, String lable,
			int count) {
		init(context);

		long time = System.currentTimeMillis();
		int[] intTime = getDateAndHour(time);
		String eventValue = new StringBuilder().append(eventKey).append(":")
				.append(lable == null ? "-" : lable).toString();

		boolean isFirst = false;
		List eventList = StatEvent.getEvents(dbManager, "developer",
				eventValue, intTime[0], false);

		List sentEventList = StatEvent.getEvents(dbManager, "developer",
				eventValue, intTime[0], true);

		if (((eventList == null) || (eventList.size() == 0))
				&& ((sentEventList == null) || (sentEventList.size() == 0))) {
			isFirst = true;
		}

		StatEvent.addEvent(dbManager, "developer", intTime[0], intTime[1],
				eventValue, count, 0L, isFirst, 0L);
	}

	public static void onEvent(Activity context, String eventKey, String lable) {
		onEvent(context, eventKey, lable, 1);
	}

	public static void onEvent(Activity context, String eventKey, int count) {
		onEvent(context, eventKey, null, count);
	}

	public static void onEvent(Activity context, String eventKey) {
		onEvent(context, eventKey, null, 1);
	}

	public static void onNotificationReceive(Context context,
			String notificationId) {
		long time = System.currentTimeMillis();
		int[] dataAndHour = getDateAndHour(time);
		if ((notificationId != null) && (notificationId.trim().length() > 0)) {
			if (dbManager == null) {
				dbManager = DatabaseManager.getInstance(context);
			}
			StatEvent.addEvent(dbManager, "notification_receive",
					dataAndHour[0], dataAndHour[1], notificationId);
		}
	}

	public static void onNotificationClick(Context context,
			String notificationId) {
		long time = System.currentTimeMillis();
		int[] dataAndHour = getDateAndHour(time);
		if ((notificationId != null) && (notificationId.trim().length() > 0)) {
			if (dbManager == null) {
				dbManager = DatabaseManager.getInstance(context);
			}
			StatEvent.addEvent(dbManager, "notification_open", dataAndHour[0],
					dataAndHour[1], notificationId);
		}
	}

	public static void onError(Activity context, Throwable exception) {
		init(context);

		JSONObject exceptionJson = new JSONObject();
		try {
			exceptionJson.put("name", exception.toString());
			exceptionJson.put("stack", Log.getStackTraceString(exception));
			Date now = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat hourFormat = new SimpleDateFormat("H");
			StatEvent.addEvent(dbManager, "crash",
					Integer.parseInt(dateFormat.format(now)),
					Integer.parseInt(hourFormat.format(now)),
					exceptionJson.toString());
		} catch (JSONException e) {
		}
	}

	private static void recordOpenEvent(long time) {
		int[] intTime = getDateAndHour(time);

		boolean first = false;
		List todayOpenEventsSent = StatEvent.getEvents(dbManager, "open", null,
				intTime[0], true);

		List todayOpenEvents = StatEvent.getEvents(dbManager, "open", null,
				intTime[0], false);
		if (((todayOpenEvents == null) || (todayOpenEvents.size() == 0))
				&& ((todayOpenEventsSent == null) || (todayOpenEventsSent
						.size() == 0))) {
			first = false;
		}

		StatEvent.addEvent(dbManager, "open", intTime[0], intTime[1], null, 1,
				0L, first, 0L);
	}

	private static void recordCloseEvent(long duration) {
		long appCloseTime = StatInfo.getLong("user_pause_time",
				System.currentTimeMillis());
		int[] intTime = getDateAndHour(appCloseTime);

		StatEvent.addEvent(dbManager, "close", intTime[0], intTime[1], null, 1,
				duration, false, appCloseTime);
	}

	// public class EventDataSender extends Thread {
	// private Context context;
	// private long time;
	//
	// public EventDataSender(Context context, long time) {
	// this.context = context;
	// this.time = time;
	// }
	//
	// public void run() {
	// SendData data = generateSendData(context, time);
	// sendEventsToServer(data, time);
	// }
	// }

	private static void startEventSender(Context context, long time) {
		switch (sendWay) {
		case 2:
			break;
		case 4:
			break;
		case 3:
			break;
		default:
			// EventDataSender dataSender = new EventDataSender(context, time);
			// dataSender.start();
			SendData data = generateSendData(context, time);
			sendEventsToServer(data, time);
		}
	}

	private static void sendEventsToServer(SendData data, long time) {
		HttpURLConnection connection = null;
		Deflater deflater = null;
		DataOutputStream output = null;
		try {
			byte[] input = JsonUtils.convertFrom(data.getJson());
			deflater = new Deflater();
			deflater.setInput(input);
			deflater.finish();
			int size = 0;
			byte[] byteData = new byte[0];
			byte[] buf = new byte[''];
			while (!deflater.finished()) {
				int byteCount = deflater.deflate(buf);
				byte[] temp = new byte[byteData.length + byteCount];
				for (int i = 0; i < byteData.length; i++) {
					temp[i] = byteData[i];
				}
				for (int i = 0; i < byteCount; i++) {
					temp[(byteData.length + i)] = buf[i];
				}
				byteData = temp;
				size += byteCount;
			}

			URL url = new URL(new StringBuilder().append(COLLECTOR)
					.append(appKey).toString());
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setReadTimeout(20000);
			connection.setConnectTimeout(20000);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type",
					"application/octet-stream");
			connection.connect();
			output = new DataOutputStream(connection.getOutputStream());
			output.write(byteData);
			output.flush();
			int httpCode = connection.getResponseCode();

			if (httpCode == 200) {
				StringBuffer sb = new StringBuffer();
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new InputStreamReader(
							connection.getInputStream()));
					String line = null;
					while ((line = reader.readLine()) != null)
						sb.append(line).append("\n");
				} catch (IOException e) {
					Log.e("Mofang",
							"[sendEventsToServer]IOException: get response");
					e.printStackTrace();
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
							Log.e("Mofang",
									"[sendEventsToServer]IOException: reader.close()");
							e.printStackTrace();
						}
					}
				}
				String response = sb.toString();

				if (response.indexOf("HTTPSQS_PUT_OK") > -1) {
					StatEvent.signSentEvent(dbManager, data.getIdList());

					StatEvent.clearAccessPath(dbManager);
					StatEvent.clearHistoryEvents(dbManager,
							getDateAndHour(time)[0]);
				}
			}
		} catch (UnsupportedEncodingException e) {
			Log.e("Mofang", "[sendEventsToServer]UnsupportedEncodingException");
			e.printStackTrace();
		} catch (MalformedURLException e) {
			Log.e("Mofang", "[sendEventsToServer]MalformedURLException");
			e.printStackTrace();
		} catch (ProtocolException e) {
			Log.e("Mofang", "[sendEventsToServer]ProtocolException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("Mofang", "[sendEventsToServer]IOException");
			e.printStackTrace();
		} catch (Exception e) {
			Log.e("Mofang", "[sendEventsToServer]Exception");
			e.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					Log.e("Mofang",
							"[sendEventsToServer]IOException: output.close()");
					e.printStackTrace();
				}
			}
			if (deflater != null) {
				deflater.end();
			}
			if (connection != null)
				connection.disconnect();
		}
	}

	private static String getNetwork(Context context) {
		String network = "NONE";
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService("connectivity");

		NetworkInfo.State state = connectivityManager.getNetworkInfo(1)
				.getState();
		if ((state == NetworkInfo.State.CONNECTED)
				|| (state == NetworkInfo.State.CONNECTING)) {
			network = "WiFi";
		} else {
			state = connectivityManager.getNetworkInfo(0).getState();
			if ((state == NetworkInfo.State.CONNECTED)
					|| (state == NetworkInfo.State.CONNECTING)) {
				network = "2G/3G";
				TelephonyManager telephonyManager = (TelephonyManager) context
						.getSystemService("phone");

				int networkType = telephonyManager.getNetworkType();
				switch (networkType) {
				case 1:
				case 2:
				case 4:
				case 7:
					network = "2G";
					break;
				case 3:
				case 5:
				case 6:
				case 8:
				case 9:
				case 10:
					network = "3G";
				}
			}

		}
		return network;
	}

	private static SendData getAppVersion(Context context, int installDate) throws Exception {
		SendData sd = SendData.newInstance("AppVersion");
		boolean appVersionUpdate = false;
		int[] dates = StatEvent.getEventDates(dbManager);

		if ((dates != null) && (dates.length > 0)) {
			JSONObject data = new JSONObject();

			for (int date : dates) {
				JSONObject dateEvent = new JSONObject();

				String oldVersion = null;
				List<StatEvent> install = StatEvent.getEvents(dbManager,
						"install", null, date, false);
				List<StatEvent> upgrade = StatEvent.getEvents(dbManager,
						"upgrade", null, date, false);
				if (((install != null) && (install.size() > 0))
						|| ((upgrade != null) && (upgrade.size() > 0))) {
					HttpParams param = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(param, 20000);
					HttpConnectionParams.setSoTimeout(param, 20000);

					HttpClient httpClient = new DefaultHttpClient(param);

					String appKey = getAppKey(context);
					String appId = Long.valueOf(
							appKey.substring(appKey.length() - 8), 16)
							.toString();
					String devId = StatInfo.getString("dev_id", "");
					//int installDate = intTime[0];
					if ((install != null) && (install.size() > 0)) {
						installDate = ((StatEvent) install.get(0)).getDate();
					}
					String channel = StatInfo.getString("channel", "");
					String appVer = StatInfo.getString("app_ver", "");

					String sum = new StringBuilder().append(devId).append(":")
							.append(appId).append(":").append(channel)
							.append(":").append(appVer).append(":")
							.append(installDate).toString();

					MessageDigest md5Digest = MessageDigest.getInstance("MD5");
					byte[] digest = md5Digest.digest(sum.getBytes());
					StringBuilder buf = new StringBuilder();
					for (int i = 0; i < digest.length; i++) {
						String b = Integer.toHexString(0xFF & digest[i]);
						if (b.length() == 1) {
							buf.append('0');
						}
						buf.append(b);
					}
					String md5 = buf.toString();

					String url = new StringBuilder().append(REGISTER)
							.append(md5).append("/").append(appKey).append("/")
							.append(devId).append("/").append(installDate)
							.append("/").append(channel).append("/")
							.append(URLEncoder.encode(appVer, "UTF-8"))
							.toString();

					HttpPost post = new HttpPost(url);
					post.setHeader("Accept-Encoding", "gzip, deflate");

					String responseStr = null;
					try {
						HttpResponse response = httpClient.execute(post);
						if (response.getStatusLine().getStatusCode() == 200) {
							InputStream is = response.getEntity().getContent();
							if (is != null) {
								Header contentEncoding = response
										.getFirstHeader("Content-Encoding");
								if ((contentEncoding != null)
										&& (contentEncoding.getValue()
												.equalsIgnoreCase("gzip"))) {
									is = new GZIPInputStream(is);
								}
								BufferedReader reader = null;
								String line = "";
								StringBuffer content = new StringBuffer();
								try {
									reader = new BufferedReader(
											new InputStreamReader(is));
									while ((line = reader.readLine()) != null) {
										content.append(line).append("\r\n");
									}
									responseStr = content.toString();
								} finally {
									if (reader != null) {
										reader.close();
									}
									if (is != null) {
										is.close();
									}
								}
							}

						}

					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						return null;
					} catch (ClientProtocolException e) {
						e.printStackTrace();
						return null;
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}

					if ((responseStr != null)
							&& (responseStr.trim().length() > 0)) {
						JSONObject responseJson = new JSONObject(responseStr);
						int status = responseJson.optInt("status", 1);
						if (status == 1) {
							oldVersion = responseJson
									.optString("app_ver", null);
						}
					}

				}

				if (oldVersion != null) {
					String appVersion = null;
					try {
						PackageInfo packageInfo = context
								.getPackageManager()
								.getPackageInfo(context.getPackageName(), 16384);

						appVersion = packageInfo.versionName;
					} catch (PackageManager.NameNotFoundException e) {
						Log.e("Mofang",
								"[generateSendData]Get app version error");
						e.printStackTrace();
					}
					if ((appVersion != null)
							&& (!appVersion.equals(oldVersion))) {
						dateEvent.put("upgrade", oldVersion);
						appVersionUpdate = true;
					}
				} else if ((install != null) && (install.size() > 0)) {
					dateEvent.put("install", ((StatEvent) install.get(install
							.size() - 1)).getHour());
				}

				if ((install != null) && (install.size() > 0)) {
					for (StatEvent installEvent : install) {
						//eventIds.add(Integer.valueOf(installEvent.getId()));
						sd.addEventId(Integer.valueOf(installEvent.getId()));
					}
				}
				if ((upgrade != null) && (upgrade.size() > 0)) {
					for (StatEvent upgradeEvent : upgrade) {
						//eventIds.add(Integer.valueOf(upgradeEvent.getId()));
						sd.addEventId(Integer.valueOf(upgradeEvent.getId()));
					}

				}

				List<StatEvent> returnUser = StatEvent.getEvents(dbManager,
						"return", null, date, false);

				if ((returnUser != null) && (returnUser.size() > 0)) {
					dateEvent.put("returned", 1);
					for (StatEvent returnEvent : returnUser) {
						//eventIds.add(Integer.valueOf(returnEvent.getId()));
						sd.addEventId(Integer.valueOf(returnEvent.getId()));
					}

				}

				List allDateEvents = StatEvent.getEvents(dbManager, null, null,
						date, true);
				if ((allDateEvents == null) || (allDateEvents.size() == 0)) {
					dateEvent.put("daily_first", 1);
				}

				List<StatEvent> openEvents = StatEvent.getEvents(dbManager,
						"open", null, date, false);
				if ((openEvents != null) && (openEvents.size() > 0)) {
					JSONObject openJson = new JSONObject();
					Map hourData = new HashMap();
					for (StatEvent openEvent : openEvents) {
						int hourCount = 0;
						if (hourData.containsKey(Integer.valueOf(openEvent
								.getHour()))) {
							hourCount = ((Integer) hourData.get(Integer
									.valueOf(openEvent.getHour()))).intValue();
						}
						hourData.put(Integer.valueOf(openEvent.getHour()),
								Integer.valueOf(hourCount + 1));
						//eventIds.add(Integer.valueOf(openEvent.getId()));
						sd.addEventId(Integer.valueOf(openEvent.getId()));
					}
					Iterator iter = hourData.keySet().iterator();
					while (iter.hasNext()) {
						Integer key = (Integer) iter.next();
						openJson.put(Integer.toString(key.intValue()),
								hourData.get(key));
					}
					dateEvent.put("open", openJson);
				}

				List<StatEvent> closeEvents = StatEvent.getEvents(dbManager,
						"close", null, date, false);
				if ((closeEvents != null) && (closeEvents.size() > 0)) {
					JSONArray close = new JSONArray();
					for (StatEvent closeEvent : closeEvents) {
						close.put(Math.round(closeEvent.getDuration() / 1000.0D));
						//eventIds.add(Integer.valueOf(closeEvent.getId()));
						sd.addEventId(Integer.valueOf(closeEvent.getId()));
					}
					dateEvent.put("close", close);
				}

				List<StatEvent> devEvents = StatEvent.getEvents(dbManager,
						"developer", null, date, false);

				if ((devEvents != null) && (devEvents.size() > 0)) {
					JSONObject devJson = new JSONObject();
					Map devDataMap = new HashMap();
					for (StatEvent devEvent : devEvents) {
						StatEvent mapData = (StatEvent) devDataMap.get(devEvent
								.getValue());
						if (mapData == null) {
							mapData = new StatEvent();
							mapData.setValue(devEvent.getValue());
						}
						mapData.setCount(mapData.getCount()
								+ devEvent.getCount());
						if (devEvent.isFirst()) {
							mapData.setFirst(true);
						}
						devDataMap.put(devEvent.getValue(), mapData);
						//eventIds.add(Integer.valueOf(devEvent.getId()));
						sd.addEventId(Integer.valueOf(devEvent.getId()));
					}
					Iterator iter = devDataMap.keySet().iterator();
					while (iter.hasNext()) {
						String key = (String) iter.next();
						JSONArray devJsonValue = new JSONArray();
						StatEvent mapData = (StatEvent) devDataMap.get(key);
						devJsonValue.put(mapData.getCount()).put(
								mapData.isFirst() ? 1 : 0);
						devJson.put(key, devJsonValue);
					}
					dateEvent.put("event", devJson);
				}

				data.put(Integer.toString(date), dateEvent);
			}
			
			List<StatEvent> updateEvents = StatEvent.getEvents(dbManager,
					"update", null, 0, false);
			if ((updateEvents != null) && (updateEvents.size() > 0)) {
				JSONArray update = new JSONArray();
				boolean existAppVersionUpdate = false;
				for (StatEvent updateEvent : updateEvents) {
					if ("app_ver".equals(updateEvent.getValue())) {
						existAppVersionUpdate = true;
					}
					update.put(updateEvent.getValue());
					//eventIds.add(Integer.valueOf(updateEvent.getId()));
					sd.addEventId(Integer.valueOf(updateEvent.getId()));
				}
				if ((appVersionUpdate) && (!existAppVersionUpdate)) {
					update.put("app_ver");
				}
				data.put("update", update);
			}
			
			sd.setJson(data);
			return sd;
		}
		return sd;
	}

	private static SendData generateSendData(Context context, long time) {
		SendData sendData = SendData.newInstance("data_root");
		
		int[] intTime = getDateAndHour(time);
		List<Integer> eventIds = new ArrayList<Integer>();
		JSONObject json = new JSONObject();
		try {
			json.put("send_time", Math.round(time / 1000.0D));

			json.put("install_date",
					StatInfo.getInt("install_date", intTime[0]));

			json.put("dev_id", StatInfo.getString("dev_id", null));

			int isDevIdUndefined = StatInfo.getInt("dev_id_udf", 0);
			if (isDevIdUndefined == 1) {
				json.put("dev_id_udf", isDevIdUndefined);
			}

			json.put("sdk_ver", StatInfo.getString("sdk_ver", null));

			json.put("app_ver", StatInfo.getString("app_ver", null));

			json.put("channel", StatInfo.getString("channel", null));

			json.put("os", "Android");

			json.put("os_ver", StatInfo.getString("os_ver", null));

			json.put("carrier", StatInfo.getString("carrier", null));

			json.put("access", getNetwork(context));

			json.put("resolution", StatInfo.getString("resolution", null));

			json.put("model", StatInfo.getString("model", null));

			json.put("timezone", StatInfo.getInt("timezone", 8));

			json.put("language", StatInfo.getString("language", null));

			json.put("country", StatInfo.getString("country", null));

			json.put("device_id", StatInfo.getString("device_id", null));

			json.put("mac_addr", StatInfo.getString("mac_addr", null));

			//putAppVersion(json);
			try {
				sendData.addData(getAppVersion(context, intTime[0]));
			} catch (Exception e) {
				e.printStackTrace();
			}

			List<Node> accessNodes = AccessPath.getAccessNodes();
			if ((accessNodes != null) && (accessNodes.size() > 0)) {
				Map<String, Integer> pathMap = new HashMap<String, Integer>();
				Map<String, Node> nodeMap = new HashMap<String, Node>();
				for (int i = 0; i < accessNodes.size(); i++) {
					Node accessNode = accessNodes.get(i);
					if (accessNode.getActivity() != null) {
						Node mapNode = nodeMap.get(accessNode.getActivity());
						if (mapNode != null) {
							mapNode.incCount();
							mapNode.setDuration(mapNode.getDuration()
									+ accessNode.getDuration());
						} else {
							mapNode = new Node();
							mapNode.setActivity(accessNode.getActivity());
							mapNode.setCount(1);
							mapNode.setDuration(accessNode.getDuration());
						}

						String accessPath = null;
						if (((i > 0) && ((accessNodes.get(i - 1)).getActivity() == null))
								|| (i == 0))
							accessPath = new StringBuilder().append(">")
									.append(mapNode.getActivity()).toString();
						else {
							accessPath = new StringBuilder()
									.append((accessNodes.get(i - 1))
											.getActivity()).append(">")
									.append(mapNode.getActivity()).toString();
						}
						int pathCount = 1;
						if (pathMap.containsKey(accessPath)) {
							pathCount += ((Integer) pathMap.get(accessPath))
									.intValue();
						}
						pathMap.put(accessPath, Integer.valueOf(pathCount));
						if (((i < accessNodes.size() - 1) && ((accessNodes
								.get(i + 1)).getActivity() == null))
								|| (i == accessNodes.size() - 1)) {
							mapNode.incExit();
							accessPath = new StringBuilder()
									.append(mapNode.getActivity()).append(">")
									.toString();
							pathCount = 1;
							if (pathMap.containsKey(accessPath)) {
								pathCount += ((Integer) pathMap.get(accessPath))
										.intValue();
							}
							pathMap.put(accessPath, Integer.valueOf(pathCount));
						}

						nodeMap.put(mapNode.getActivity(), mapNode);
					}

				}

				List<StatEvent> pathEvents = StatEvent.getEvents(dbManager,
						"path", null, 0, false);
				if ((pathEvents != null) && (pathEvents.size() > 0)) {
					for (StatEvent pathEvent : pathEvents) {
						if (pathMap.containsKey(pathEvent.getValue())) {
							pathMap.put(pathEvent.getValue(), Integer
									.valueOf(((Integer) pathMap.get(pathEvent
											.getValue())).intValue()
											+ pathEvent.getCount()));
						} else {
							pathMap.put(pathEvent.getValue(),
									Integer.valueOf(pathEvent.getCount()));
						}
					}

				}

				List<StatEvent> nodeEvents = StatEvent.getEvents(dbManager,
						"node", null, 0, false);
				if ((nodeEvents != null) && (nodeEvents.size() > 0)) {
					for (StatEvent nodeEvent : nodeEvents) {
						AccessPath.Node mapNode = (AccessPath.Node) nodeMap
								.get(nodeEvent.getValue());
						if (mapNode != null) {
							mapNode.setCount(mapNode.getCount()
									+ nodeEvent.getCount());
							mapNode.setExit(mapNode.getExit()
									+ nodeEvent.getExit());
							mapNode.setDuration(mapNode.getDuration()
									+ nodeEvent.getDuration());
							nodeMap.put(nodeEvent.getValue(), mapNode);
						} else {
							mapNode = new AccessPath.Node();
							mapNode.setActivity(nodeEvent.getValue());
							mapNode.setCount(nodeEvent.getCount());
							mapNode.setExit(nodeEvent.getExit());
							mapNode.setDuration(nodeEvent.getDuration());
							nodeMap.put(nodeEvent.getValue(), mapNode);
						}
					}

				}

				StatInfo.putString("user_activities", "");
				StatEvent.clearAccessPath(dbManager);

				if (pathMap.size() > 0) {
					JSONObject paths = new JSONObject();
					Iterator<String> pathKeys = pathMap.keySet().iterator();
					while (pathKeys.hasNext()) {
						String pathKey = (String) pathKeys.next();
						paths.put(
								pathKey.substring(pathKey.lastIndexOf(".") + 1),
								pathMap.get(pathKey));
						StatEvent.addEvent(dbManager, "path", intTime[0],
								intTime[1], pathKey,
								((Integer) pathMap.get(pathKey)).intValue(),
								0L, false, resumeTime);
					}

					json.put("path", paths);
				}

				if (nodeMap.size() > 0) {
					JSONObject pages = new JSONObject();
					Iterator pageKeys = nodeMap.keySet().iterator();
					while (pageKeys.hasNext()) {
						String pageKey = (String) pageKeys.next();
						AccessPath.Node node = (AccessPath.Node) nodeMap
								.get(pageKey);
						JSONArray pageInfo = new JSONArray();
						pageInfo.put(node.getCount());
						pageInfo.put(node.getExit());
						pageInfo.put(Math.round(node.getDuration() / 1000.0D));
						pages.put(
								pageKey.substring(pageKey.lastIndexOf(".") + 1),
								pageInfo);
						StatEvent.addEvent(dbManager, "node", intTime[0],
								intTime[1], pageKey, node.getCount(),
								node.getExit(), node.getDuration(), false,
								resumeTime);
					}

					json.put("page", pages);
				}

			}


			List<StatEvent> notificationReciveEvents = StatEvent.getEvents(
					dbManager, "notification_receive", null, 0, false);

			if ((notificationReciveEvents != null)
					&& (notificationReciveEvents.size() > 0)) {
				JSONArray pushReceive = new JSONArray();
				Map<String, String> pushReceiveMap = new HashMap<String, String>();
				for (StatEvent notificationEvent : notificationReciveEvents) {
					if (!pushReceiveMap.containsKey(notificationEvent
							.getValue())) {
						pushReceive.put(notificationEvent.getValue());
						pushReceiveMap.put(notificationEvent.getValue(),
								notificationEvent.getValue());
						eventIds.add(Integer.valueOf(notificationEvent.getId()));
					}
				}
				json.put("push_recv", pushReceive);
			}

			List<StatEvent> notificationOpenEvents = StatEvent.getEvents(
					dbManager, "notification_open", null, 0, false);

			if ((notificationOpenEvents != null)
					&& (notificationOpenEvents.size() > 0)) {
				JSONArray pushOpen = new JSONArray();
				Map<String, String> pushOpenMap = new HashMap<String, String>();
				for (StatEvent notificationEvent : notificationOpenEvents) {
					if (!pushOpenMap.containsKey(notificationEvent.getValue())) {
						pushOpen.put(notificationEvent.getValue());
						pushOpenMap.put(notificationEvent.getValue(),
								notificationEvent.getValue());
						eventIds.add(Integer.valueOf(notificationEvent.getId()));
					}
				}
				json.put("push_open", pushOpen);
			}

			
			sendData.setJson(json);
			sendData.setIdList(eventIds);
			SendData crash = getCrashInfo();
			if (crash != null) {
				// FIXME
				//json.put("crash", crash);
				sendData.addData(crash);
			}
			return sendData;
		} catch (JSONException e) {
			Log.e("Mofang", "[generateSendData]JSONException");
			if (isDebug())
				e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			Log.e("Mofang", "[generateSendData]NoSuchAlgorithmException");
			if (isDebug())
				e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			Log.e("Mofang", "[generateSendData]UnsupportedEncodingException");
			if (isDebug()) {
				e.printStackTrace();
			}
		}
		return sendData;
	}

	private static SendData getCrashInfo() throws UnsupportedEncodingException,
			NoSuchAlgorithmException, JSONException {
		SendData sd = new SendData();
		sd.setName("crash");
		List<StatEvent> crashEvents = StatEvent.getEvents(dbManager, "crash",
				null, 0, false);
		Map<String, CrashData> crashMap = new HashMap<String, CrashData>();
		List<Integer> eventIds = new ArrayList<Integer>();
		if ((crashEvents != null) && (crashEvents.size() > 0)) {
			for (StatEvent event : crashEvents) {
				JSONObject exceptionJson = new JSONObject(event.getValue());
				String exceptionName = exceptionJson.optString("name");
				String exceptionStack = exceptionJson.optString("stack");

				MessageDigest md5Encoder = MessageDigest.getInstance("MD5");
				byte[] md5Bytes = md5Encoder.digest(exceptionStack
						.getBytes("utf-8"));
				StringBuilder md5Hex = new StringBuilder();
				for (byte b : md5Bytes) {
					md5Hex.append(Integer.toHexString(0xFF & b));
				}
				String md5 = md5Hex.toString().toLowerCase();

				CrashData crashData = (CrashData) crashMap.get(md5);
				if (crashData == null) {
					crashData = new CrashData();
					crashData.setName(exceptionName);
					crashData.setStack(exceptionStack);
				}
				crashData.addTime(event.getTime());
				crashMap.put(md5, crashData);
				eventIds.add(Integer.valueOf(event.getId()));
			}
		}

		if (crashMap.size() > 0) {
			JSONObject crash = new JSONObject();
			Iterator crashMd5s = crashMap.keySet().iterator();
			while (crashMd5s.hasNext()) {
				String md5 = (String) crashMd5s.next();
				CrashData crashData = (CrashData) crashMap.get(md5);
				JSONObject crashObject = new JSONObject();
				crashObject.put("name", crashData.getName());
				crashObject.put("stack", crashData.getStack());
				JSONArray timeArray = new JSONArray();
				for (Iterator i$ = crashData.getTimeList().iterator(); i$
						.hasNext();) {
					long crashTime = ((Long) i$.next()).longValue();
					timeArray.put(crashTime);
				}
				crashObject.put("time", timeArray);
				crash.put(md5, crashObject);
			}
			sd.setJson(crash);
			sd.setIdList(eventIds);
			return sd;
		}
		return null;
	}

	private static int[] getDateAndHour(long time) {
		Date today = new Date(time);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat hourFormat = new SimpleDateFormat("H");
		return new int[] { Integer.parseInt(dateFormat.format(today)),
				Integer.parseInt(hourFormat.format(today)) };
	}

	public static void main(String[] args) {
		String accessPath = "";
		System.out.println(new StringBuilder()
				.append("[updateAccessDuration] id = 719155321, accessPath = ")
				.append(accessPath).toString());
		String str2 = ";719155321,";
		System.out.println(new StringBuilder().append("str2 = ").append(str2)
				.toString());
		String[] str1And3456 = accessPath.split(str2);
		System.out.println(new StringBuilder().append("str1And3456.length = ")
				.append(str1And3456.length).toString());
		System.out.println(new StringBuilder().append("str1And3456[0] = ")
				.append(str1And3456[0]).toString());
		if (str1And3456.length == 1) {
			str2 = "719155321,";
			str1And3456 = new String[2];
			str1And3456[0] = "";
			str1And3456[1] = accessPath.substring(accessPath.indexOf(",") + 1);
		}
		System.out.println(new StringBuilder().append("str1And3456[1] = ")
				.append(str1And3456[1]).toString());
		String str1 = str1And3456[0];
		System.out.println(new StringBuilder().append("str1 = ").append(str1)
				.toString());
		System.out.println(new StringBuilder().append("str2 = ").append(str2)
				.toString());
		String str3 = str1And3456[1].substring(0, str1And3456[1].indexOf(","));
		System.out.println(new StringBuilder().append("str3 = ").append(str3)
				.toString());
		String str4 = ",";
		System.out.println(new StringBuilder().append("str4 = ").append(str4)
				.toString());
		String str56 = str1And3456[1]
				.substring(str1And3456[1].indexOf(",") + 1);
		String str5 = Long.toString(555L);
		System.out.println(new StringBuilder().append("str5 = ").append(str5)
				.toString());
		String str6 = "";
		if (str56.indexOf(";") > 0) {
			str6 = str56.substring(str56.indexOf(";"));
		}
		System.out.println(new StringBuilder().append("str6 = ").append(str6)
				.toString());
		accessPath = new StringBuilder().append(str1).append(str2).append(str3)
				.append(str4).append(str5).append(str6).toString();
		System.out.println(new StringBuilder().append("accessPath = ")
				.append(accessPath).toString());
	}

	public static void onKillProcess(OnClickListener onClickListener) {
		// TODO Auto-generated method stub
		
	}

	public static String getConfigParams(Activity context,
			String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void onEvent(Activity context, String eventKey,
			HashMap<String, String> map) {
		// TODO Auto-generated method stub
		
	}

	public static void onEventDuration(Activity context,
			String string, int i) {
		// TODO Auto-generated method stub
		
	}

	public static void onEventDuration(Activity context,
			String string, String string2, int i) {
		// TODO Auto-generated method stub
		
	}

	public static void onEventDuration(Activity context, String string,
			HashMap<String, String> map, int i) {
		// TODO Auto-generated method stub
		
	}

	public static void onEventBegin(Activity context, String string) {
		// TODO Auto-generated method stub
		
	}

	public static void onEventBegin(Activity context, String string,
			String string2) {
		// TODO Auto-generated method stub
		
	}

	public static void onEventEnd(Activity context, String string) {
		// TODO Auto-generated method stub
		
	}

	public static void onEventEnd(Activity context, String string,
			String string2) {
		// TODO Auto-generated method stub
		
	}

	public static void onKVEventBegin(Activity context,
			String string, HashMap<String, String> map, String string2) {
		// TODO Auto-generated method stub
		
	}

	public static void onKVEventEnd(Activity context, String string,
			String string2) {
		// TODO Auto-generated method stub
		
	}

	public static void flush(Activity context) {
		// TODO Auto-generated method stub
		
	}

	

}
