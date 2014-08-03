package com.piasy.simpletravel.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
//import com.baidu.mapapi.BMapManager;
import com.piasy.simpletravel.SimpleTravelApplication;
import com.piasy.simpletravel.dao.DBManager;
import com.piasy.simpletravel.model.CommitModule;
import com.piasy.simpletravel.model.Constant;
import com.piasy.simpletravel.model.HotelSeacher;
import com.piasy.simpletravel.model.LocationModule;
import com.piasy.simpletravel.model.PlanGenerator;
import com.piasy.simpletravel.model.RouteEntry;
import com.piasy.simpletravel.model.SignupModule;
import com.piasy.simpletravel.model.SpotsSeacher;
import com.piasy.simpletravel.model.VerifyModule;

public class Controller
{
	public static Controller getController()
	{
//		if (instanceCount == 0)
//		{
//			instance = new Controller();
//			instanceCount ++;
//		}
		return instance;
	}
	
	static Handler toastHandler;
	public static void setToastHandler(Handler handler)
	{
		toastHandler = handler;
	}
	
	public static void makeToast(String text)
	{
		Message msg = Message.obtain();
		msg.obj = text;
		synchronized (toastHandler)
		{
			toastHandler.sendMessage(msg);
		}
	}
	
	Handler activityHandler;
	public void setActivityHandler(Handler activityHandler)
	{
		this.activityHandler = activityHandler;
	}
	
	String token = "";
	public void signup(String username, String password, JSONObject info)
	{
		this.username = username;
		this.password = password;
		this.info = info;
		SignupModule signupModule = new SignupModule(username, password, info);
		signupModule.signup();
	}
	
	boolean verified = false;
	public void verify(String username, String password)
	{
		this.username = username;
		this.password = password;
		VerifyModule verifyModule = new VerifyModule(username, password);
		verifyModule.verify();
	}
	
	public boolean verified()
	{
		return verified;
	}
	
	public boolean committed(String name, String city)
	{
		boolean ret = false;
		synchronized (committed)
		{
			for (int i = 0; i < committed.length(); i ++)
			{
				try
				{
					if (committed.getJSONObject(i).getString("name").equals(name)
					 && committed.getJSONObject(i).getString("city").equals(city))
					{
						ret = true;
						break;
					}
				}
				catch (JSONException e)
				{
					if (e.getMessage() == null)
		        	{
						Log.e(Constant.LOG_LEVEL_ERROR, "Controller committed : JSONException");
		        	}
		        	else
		        	{
		        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller committed : " + e.getMessage());
					}
				}
			}
		}
		return ret;
	}
	
	CommitModule commitModule = new CommitModule();
	/**
	 * before call this method, call committed(name, city) at first
	 * */
	public void commit(String name, String city, float score, String comment)
	{
		try
		{
			JSONObject eval = new JSONObject();
			eval.put("username", username);
			String timeString = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Calendar.getInstance().getTime());
			Log.d(Constant.LOG_LEVEL_DEBUG, "time : " + timeString);
			eval.put("time", timeString);
			eval.put("comment", comment);
			
			commitModule.commit(username, token, name, city, eval.toString(), score);
		}
		catch (JSONException e)
		{
			if (e.getMessage() == null)
        	{
				Log.e(Constant.LOG_LEVEL_ERROR, "Controller commit : JSONException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller commit : " + e.getMessage());
			}
		}
	}
	
	int maxpage = 0, requestPage = 0;
	/**
	 * plain text
	 * */
	String username = "", password = "";
	JSONObject info;
	SpotsSeacher spotsSeacher = new SpotsSeacher();
	
	/**
	 * @param pagenum : start from 0
	 * */
	public boolean query(String keyword, int type, int pagenum)
	{
		boolean ret = false;
		try
		{
			if (pagenum < maxpage)
			{
				//return previous spots
				JSONObject msgInfo = new JSONObject();
				msgInfo.put("type", "query");
				msgInfo.put("result", "success");
				
				JSONArray spots = new JSONArray();
				for (int i = pagenum * Constant.SPOTS_PAGE_SIZE; 
						i < pagenum * (Constant.SPOTS_PAGE_SIZE + 1); i ++)
				{
					synchronized (allSpots)
					{
						if (i < allSpots.length())
						{
							spots.put(allSpots.getJSONObject(i));
						}
						else
						{
							break;
						}
					}
				}
				msgInfo.put("spots", spots);
				
				synchronized (activityHandler)
				{
					Message msg = Message.obtain();
					msg.obj = msgInfo.toString();
					activityHandler.sendMessage(msg);
				}
				
				ret = true;
			}
			else if (spotsSeacher.search(username, token, keyword, type, pagenum))
			{
				requestPage = pagenum;
				makeToast("正在努力地为你搜索景点，请稍等...");
				ret = true;
			}
		}
		catch (JSONException e)
		{
			if (e.getMessage() == null)
        	{
				Log.e(Constant.LOG_LEVEL_ERROR, "Controller query : JSONException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller query : " + e.getMessage());
			}
		}
		
		return ret;
	}
	
	JSONArray committed = new JSONArray();
	public void setCommitted(JSONArray committed)
	{
		synchronized (committed)
		{
			this.committed = committed;
		}
	}
	
	JSONArray aroundSpots = new JSONArray();
	public void setAroundSpots(JSONArray spots)
	{
		aroundSpots = spots;
	}

	public void searchAroundSpots()
	{
		BDLocation location = getLocation();
		if (location != null && aroundSpots.length() == 0 
				&& !username.equals("") && !token.equals(""))
		{
			String city = location.getCity();
			city = city.substring(0, city.length() - 1);
			spotsSeacher.search(username, token, city, Constant.SEARCH_BY_CITY, 0);
		}
	}
	
	public ArrayList<JSONObject> getAroundSpots()
	{
		ArrayList<JSONObject> spots = new ArrayList<JSONObject>();
		for (int i = 0; i < aroundSpots.length(); i ++)
		{
			try
			{
				spots.add(aroundSpots.getJSONObject(i));
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return spots;
	}
	
	JSONArray recSpots = new JSONArray();
	public void setRecSpots(JSONArray recSpots)
	{
		synchronized (recSpots)
		{
			this.recSpots = recSpots;
		}
	}
	
	public void setUserInfo(JSONObject info)
	{
		this.info = info;
	}
	
	public JSONObject getUserInfo()
	{
		return info;
	}
	
	public String getCurVer()
	{
		return "Beta";
	}
	
	public JSONArray getRecSpots()
	{
		return recSpots;
	}
	
	JSONArray allSpots = new JSONArray();
	public void setSuccessResult(int type, String info)
	{
		try
		{
			JSONObject msgInfo = new JSONObject();
			switch (type)
			{
			case Constant.SIGNUP:
				msgInfo.put("type", "signup");
				verify(username, password);
				break;
			case Constant.VERIFY:
				msgInfo.put("type", "verify");
				token = info;
				verified = true;
				break;
			case Constant.QUERY:
				msgInfo.put("type", "query");
				
				JSONArray spots = new JSONArray(info);
				if (0 < spots.length())
				{
					maxpage = requestPage;
					for (int i = 0; i < spots.length(); i ++)
					{
						synchronized (allSpots)
						{
							allSpots.put(spots.getJSONObject(i));
						}
					}
				}
				
				msgInfo.put("spots", spots);
				break;
			case Constant.COMMIT:
				msgInfo.put("type", "commit");
				makeToast("评价成功");
				break;
			case Constant.AROUND_SPOTS:
				setAroundSpots(new JSONArray(info));
				makeToast("定位成功，可以查看附近的景点啦~");
				break;
			default:
				break;
			}
			msgInfo.put("result", "success");
			
			synchronized (activityHandler)
			{
				Message msg = Message.obtain();
				msg.obj = msgInfo.toString();
				activityHandler.sendMessage(msg);
				Log.d(Constant.LOG_LEVEL_DEBUG, "send msg : " + msgInfo.toString());
			}
		}
		catch (JSONException e)
		{
			if (e.getMessage() == null)
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller setSuccessResult : JSONException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller setSuccessResult : " + e.getMessage());
			}
		}
	}
	
	public void setFailResult(int type, String reason)
	{
		Log.d(Constant.LOG_LEVEL_DEBUG, "" + type + " fail , reason : " + reason);
		try
		{
			JSONObject msgInfo = new JSONObject();
			switch (type)
			{
			case Constant.SIGNUP:
				msgInfo.put("type", "signup");
				break;
			case Constant.VERIFY:
				msgInfo.put("type", "verify");
				break;
			case Constant.QUERY:
				msgInfo.put("type", "query");
				break;
			case Constant.COMMIT:
				msgInfo.put("type", "commit");
				break;
			default:
				break;
			}
			msgInfo.put("result", "fail");
			msgInfo.put("reason", reason);
			
			synchronized (activityHandler)
			{
				Message msg = Message.obtain();
				msg.obj = msgInfo.toString();
				activityHandler.sendMessage(msg);
			}
		}
		catch (JSONException e)
		{
			if (e.getMessage() == null)
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller setFailResult : JSONException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller setFailResult : " + e.getMessage());
			}
		}
	}
	
	int mYear = 2014, mMonth = 2, mDay = 14;
	public void setPlanStartDate(int mYear, int mMonth, int mDay)
	{
		mMonth ++;
		this.mDay = mDay;
		this.mMonth = mMonth;
		this.mYear = mYear;
		
		fromDate = "" + mYear + "-";
		if (mMonth < 10)
		{
			fromDate += "0" + mMonth + "-";
		}
		else
		{
			fromDate += mMonth + "-";
		}
		if (mDay < 10)
		{
			fromDate += "0" + mDay;
		}
		else
		{
			fromDate += mDay;
		}
	}
	
	HotelSeacher hotelSeacher = new HotelSeacher();
	JSONArray allHotels = new JSONArray();
	int hotelMaxPage = 0, hotelSearchPage = 0;
	String toCity = null, fromDate = null, toDate = null, limit = "";
	public boolean searchHotel(String limit, int pageNum)
	{
		if (toDate == null)
		{
			Calendar endDate = Calendar.getInstance();
			endDate.setTime(new Date());
			endDate.set(Calendar.YEAR, mYear);
			endDate.set(Calendar.MONTH, mMonth);
			endDate.set(Calendar.DAY_OF_MONTH, mDay);
			endDate.add(Calendar.DAY_OF_MONTH, planDays);
			toDate =  "" + endDate.get(Calendar.YEAR) + "-";
			if (endDate.get(Calendar.MONTH) < 10)
			{
				toDate += "0" + endDate.get(Calendar.MONTH) + "-";
			}
			else
			{
				toDate += endDate.get(Calendar.MONTH) + "-";
			}
			if (endDate.get(Calendar.DAY_OF_MONTH) < 10)
			{
				toDate += "0" + endDate.get(Calendar.DAY_OF_MONTH);
			}
			else
			{
				toDate += endDate.get(Calendar.DAY_OF_MONTH);
			}
		}
		
		this.limit = limit;
		boolean ret = false;
		try
		{
			if (pageNum < hotelMaxPage)
			{
				//return previous spots
				JSONObject msgInfo = new JSONObject();
				msgInfo.put("type", "hotelsearch");
				msgInfo.put("result", "success");
				
				JSONArray hotels = new JSONArray();
				for (int i = pageNum * Constant.SPOTS_PAGE_SIZE; 
						i < pageNum * (Constant.SPOTS_PAGE_SIZE + 1); i ++)
				{
					synchronized (allHotels)
					{
						if (i < allHotels.length())
						{
							hotels.put(allHotels.getJSONObject(i));
						}
						else
						{
							break;
						}
					}
				}
				msgInfo.put("hotels", hotels);
				
				synchronized (activityHandler)
				{
					Message msg = Message.obtain();
					msg.obj = msgInfo.toString();
					activityHandler.sendMessage(msg);
				}
				ret = true;
			}
			else if (hotelSeacher.search(toCity, limit, fromDate, toDate, pageNum))
			{
				hotelSearchPage = pageNum;
				makeToast("正在努力地为你搜索酒店，请稍等...");
				ret = true;
			}
		}
		catch (JSONException e)
		{
			if (e.getMessage() == null)
        	{
				Log.e(Constant.LOG_LEVEL_ERROR, "Controller query : JSONException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller query : " + e.getMessage());
			}
		}
		
		return ret;
	}
	
	public void setToCity(String toCity)
	{
		this.toCity = toCity;
	}
	
	public String getToCity()
	{
		return toCity;
	}
	
	public String getFromDate()
	{
		return fromDate;
	}
	
	public String getToDate()
	{
		return toDate;
	}
	
	public String getLimit()
	{
		return limit;
	}
	
	public void showSortType()
	{
		JSONObject info = new JSONObject();
		try
		{
			info.put("type", "sort");
			
			Message msg = Message.obtain();
			msg.obj = info.toString();
			
			synchronized (activityHandler)
			{
				activityHandler.sendMessage(msg);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	public Calendar getPlanDate()
	{
		//TODO to be changed
		Calendar date = Calendar.getInstance();
		date.setTime(new Date());
		date.set(Calendar.YEAR, mYear);
		date.set(Calendar.MONTH, mMonth - 1);
		date.set(Calendar.DAY_OF_MONTH, mDay);
		
		return date;
	}
	
	public void setHotelResult(JSONArray hotels)
	{
		if (0 < hotels.length())
		{
			try
			{
				hotelMaxPage = hotelSearchPage;
				for (int i = 0; i < hotels.length(); i ++)
				{
					synchronized (allHotels)
					{
						allHotels.put(hotels.getJSONObject(i));
					}
				}
				
				JSONObject msgInfo = new JSONObject();
				msgInfo.put("type", "hotelsearch");
				msgInfo.put("result", "success");
				msgInfo.put("hotels", hotels);
				
				synchronized (activityHandler)
				{
					Message msg = Message.obtain();
					msg.obj = msgInfo.toString();
					activityHandler.sendMessage(msg);
				}
			}
			catch (JSONException e)
			{
				if (e.getMessage() == null)
	        	{
	        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller setHotelResult : JSONException");
	        	}
	        	else
	        	{
	        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller setHotelResult : " + e.getMessage());
				}
			}
		}
	}
	
	JSONObject viewHotel = new JSONObject();
	public void seeHotelDetail(JSONObject hotel)
	{
		viewHotel = hotel;
	}
	
	public JSONObject getHotelDetail()
	{
		return viewHotel;
	}
	
	int viewMapType = Constant.VIEW_POS_MAP;
	public void viewMap(int type)
	{
		viewMapType = type;
	}
	
	public int getViewMapType()
	{
		return viewMapType;
	}
	
	boolean exiting = false;
	public void exit()
	{
		exiting = true;
		
		Timer timer = new Timer();
		TimerTask exit = new TimerTask()
		{
			
			@Override
			public void run()
			{
				//TODO stop all work...
				locationClient.stop();
				System.out.println("exiting...");
				System.exit(0);
			}
		};
		
		timer.schedule(exit, 100);
	}
	
	public boolean exiting()
	{
		return exiting;
	}
		
//	BMapManager bMapManager;
//	public void setBMapManager(BMapManager bMapManager)
//	{
//		this.bMapManager = bMapManager;
//		planGenerator = new PlanGenerator(bMapManager);
//	}
	
	PlanGenerator planGenerator;
//	public boolean addSpot(JSONObject spot)
//	{
//		//TODO make sure all spots are in one city,
//		//and divide them into proper days
//		
//		try
//		{
//			toCity = spot.getString("city");
//		}
//		catch (JSONException e)
//		{
//			if (e.getMessage() == null)
//        	{
//        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller addSpot : JSONException");
//        	}
//        	else
//        	{
//        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller addSpot : " + e.getMessage());
//			}
//		}
//		return planGenerator.addSpot(spot);
//	}
	
	int planDays = 3;
//	public void setPlanDays(int days)
//	{
//		planDays = days;
//		planGenerator.setPlanDays(days);
//	}
	
	JSONObject hotel = new JSONObject();
//	public boolean setHotel(JSONObject hotel)
//	{
//		boolean ret = false;
//		try
//		{
//			hotel.put("name", 
//					hotel.getJSONObject("attrs").getString("hotelName"));
//			String bpoint = hotel.getJSONObject("attrs").getString("bpoint");
//			String [] values = bpoint.split(",");
//			if (values != null && values.length == 2)
//			{
//				int latitude = (int) (Float.parseFloat(values[0]) * 1000000);
//				int longitude = (int) (Float.parseFloat(values[1]) * 1000000);
//				hotel.put("latitude", latitude);
//				hotel.put("longitude", longitude);
//				hotel.put("city", hotel.getString("cityName"));
//				this.hotel = hotel;
//				ret = planGenerator.setHotel(hotel);
//			}
//		}
//		catch (JSONException e)
//		{
//			if (e.getMessage() == null)
//        	{
//        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller setHotel : JSONException");
//        	}
//        	else
//        	{
//        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller setHotel : " + e.getMessage());
//			}
//			
//			e.printStackTrace();
//		}
//		catch (NumberFormatException e)
//		{
//			if (e.getMessage() == null)
//        	{
//        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller setHotel : JSONException");
//        	}
//        	else
//        	{
//        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller setHotel : " + e.getMessage());
//			}
//			
//			e.printStackTrace();
//		}
//		
//		return ret;
//	}
	
//	public boolean genPlan()
//	{
//		return planGenerator.genPlan();
//	}
//	
//	public float finishPercentage()
//	{
//		return planGenerator.finishPercentage();
//	}
//	
//	public ArrayList<ArrayList<JSONObject>> getPlan()
//	{
//		return planGenerator.getPlan();
//	}
//	
//	public ArrayList<ArrayList<JSONObject>> getSugPlan()
//	{
//		return planGenerator.getSugPlan();
//	}
//	
//	public ArrayList<HashMap<String, RouteEntry>> getRoutes()
//	{
//		return planGenerator.getRoutes();
//	}
	
	public String getDayTitle()
	{
		Calendar endDate = Calendar.getInstance();
		endDate.setTime(new Date());
		endDate.set(Calendar.YEAR, mYear);
		endDate.set(Calendar.MONTH, mMonth);
		endDate.set(Calendar.DAY_OF_MONTH, mDay);
		endDate.add(Calendar.DAY_OF_MONTH, viewDayIndex);
		return endDate.get(Calendar.MONTH) + "." + endDate.get(Calendar.DAY_OF_MONTH);
	}
	
	int viewDayIndex = 0;
	public void gotoDay(int day)
	{
		viewDayIndex = day;
	}
	
//	public ArrayList<JSONObject> getOneDayPlan()
//	{
//		ArrayList<ArrayList<JSONObject>> plans = planGenerator.getPlan();
//		if ((0 <= viewDayIndex) && (viewDayIndex < plans.size()))
//		{
//			return plans.get(viewDayIndex);
//		}
//		else
//		{
//			return null;
//		}
//	}
//	
//	public ArrayList<JSONObject> getOneDaySugPlan()
//	{
//		ArrayList<ArrayList<JSONObject>> plans = planGenerator.getSugPlan();
//		if ((0 <= viewDayIndex) && (viewDayIndex < plans.size()))
//		{
//			return plans.get(viewDayIndex);
//		}
//		else
//		{
//			return null;
//		}
//	}	
//	
//	public HashMap<String, RouteEntry> getOneDayRoutes()
//	{
//		ArrayList<HashMap<String, RouteEntry>> routes = planGenerator.getRoutes();
//		if ((0 <= viewDayIndex) && (viewDayIndex < routes.size()))
//		{
//			return routes.get(viewDayIndex);
//		}
//		else
//		{
//			return null;
//		}
//	}
//	
//	public void showPlan()
//	{
//		ArrayList<ArrayList<JSONObject>> plan = planGenerator.getSugPlan();
//		
//		for (int i = 0; i < plan.size(); i ++)
//		{
//			ArrayList<JSONObject> onedayPlan = plan.get(i);
//			for (int j = 0; j < onedayPlan.size(); j ++)
//			{
//				System.out.println(onedayPlan.get(j).toString());
//			}
//		}
//		
//		ArrayList<HashMap<String, RouteEntry>> routes = getRoutes();
//		for (int i = 0; i < routes.size(); i ++)
//		{
//			HashMap<String, RouteEntry> onedayRoute = routes.get(i);
//			for (String id : onedayRoute.keySet())
//			{
//				System.out.println(onedayRoute.get(id).toString());
//			}
//		}
//	}
		
	DBManager dbManager;
	public void setDBManager(DBManager dbManager)
	{
		this.dbManager = dbManager;
	}
	
	public void closeDB()
	{
		if (dbManager != null)
		{
			dbManager.closeDB();
		}
	}
	
	public void openDB()
	{
		if (dbManager != null && !dbManager.dbIsOpen())
		{
			dbManager.openDB();
		}
	}
	
	SimpleTravelApplication app;
	public void setAPP(SimpleTravelApplication app)
	{
		this.app = app;
	}
	
	public SimpleTravelApplication getApp()
	{
		return app;
	}
	
	Bundle spotDetail = new Bundle();
	public void seeSpotDetail(Bundle spot)
	{
		spotDetail = spot;
	}
	
	public Bundle getSpotDetail()
	{
		return spotDetail;
	}
	
	LocationClient locationClient = null;
	LocationModule locationModule = null;
	public void setLocationClient(LocationClient client)
	{
		locationClient = client;
		locationModule = new LocationModule(locationClient);
	}
	
	public LocationClient getLocationClient()
	{
		return locationClient;
	}
	
	public BDLocation getLocation()
	{
		return locationModule.getLocation();
	}
	
	public void reqLocation()
	{
		locationModule.reqLocation();
	}
		
	@SuppressLint("HandlerLeak")
	Handler controllerHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Log.d(Constant.LOG_LEVEL_DEBUG, "get info at controllerHandler : " + (String)msg.obj);
		}
	};
//	private static int instanceCount = 1;
	private static Controller instance = new Controller();
	private Controller()
	{
		
	}
}
