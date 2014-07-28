package com.piasy.plantraveltrace.model;

import android.os.Environment;

/**
 * <p>Constants will be used in the program.</p>
 *  
 *  
 * <p>author: 	Piasy Xu</p>
 * <p>date:		19:54 2013/12/22</p>
 * <p>email:	xz4215@gmail.com</p>
 * <p>github:	Piasy</p>
 * */
public class Constant
{
	public static final String APP_PREF_NAME = "StuTravelPref";
	
	public static final String LOG_LEVEL_INFO = "INFO";
	public static final String LOG_LEVEL_DEBUG = "DEBUG";
	public static final String LOG_LEVEL_WARNING = "WARNING";
	public static final String LOG_LEVEL_ERROR = "ERROR";
	public static final String LOG_LEVEL_FATAL = "FATAL";
	
	public static final String INVALID_TOKEN = "INVALID";
	public static final String NO_PHOTO = "NO_PHOTO";
	
	public static final String APP_CACHE_DIR = Environment.getExternalStorageDirectory().toString() + "/.travalAppCache";
	
	public static final int SIGNUP = 1;
	public static final int VERIFY = 2;
	public static final int QUERY = 3;
	public static final int COMMIT = 4;
	public static final int AROUND_SPOTS = 5;
	public static final int LISTVIEW_ITEM_SPOT = 1;
	public static final int LISTVIEW_ITEM_TRAFFIC = 2;
	public static final int LISTVIEW_ITEM_HOTEL = 3;
	public static final int LISTVIEW_ITEM_PLAN = 4;
	public static final int LISTVIEW_ITEM_SPOT_SEARCH = 5;
	public static final int LISTVIEW_ITEM_HOTEL_SEARCH = 6;
	public static final int PLAN_STATUS_ADD = 1;
	public static final int PLAN_STATUS_PLAN = 2;
	public static final int PLAN_STATUS_HIST = 3;
	public static final int ACTIVITY_ALLPLAN = 1;
	public static final int ACTIVITY_SINGLEDAY = 2;
	public static final int ACTIVITY_SEARCH = 3;
	public static final int SIGNUP_SUCCESS = 20;
	public static final int MAX_TITLE_LEN = 10;
	public static final int VIEW_SPOTS_MAP = 1;
	public static final int VIEW_POS_MAP = 2;
	public static final int SEARCH_BY_KEYWORD = 1;
	public static final int SEARCH_BY_CITY = 2;
	
	
	public static final int BUFFER_SIZE = 24 * 1024;
	public static final int THREAD_POOL_SIZE = 5000;
	public static final int TOKEN_LENGTH = 64;
	public static final int REFRESH_TIME_INTERVAL = 24 * 3600 * 1000;
	public static final int SPOTS_PAGE_SIZE = 16;
	public static final int POI_SEARCH_PAGE_CAPACITY = 20;
	public static final int ROUTE_REQUESR_TIMEOUT = 5 * 1000;
	public static final int ROUTE_REQUESR_INTERVAL = 4 * 1000;
	public static final int SPOT_INTRO_LEN = 41;
	public static final int POS_REQ_TIMESTAMP = 5 * 60 * 1000;
	public static final int HTTPCLIENT_CON_TIMEOUT = 30 * 1000;
	public static final int HTTPCLIENT_READ_TIMEOUT = 30 * 1000;
	
	public static final long VERIFY_TIME_OUT = 5 * 1000;
	
	public static final int ZOOM_LEVEL3_WIDTH = 128000000;
}
