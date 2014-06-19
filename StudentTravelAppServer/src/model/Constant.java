package model;

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
	public static final String LOG_LEVEL_INFO = "INFO";
	public static final String LOG_LEVEL_DEBUG = "DEBUG";
	public static final String LOG_LEVEL_WARNING = "WARNING";
	public static final String LOG_LEVEL_ERROR = "ERROR";
	public static final String LOG_LEVEL_FATAL = "FATAL";
	public static final int LEVEL_DEBUG = 1;
	public static final int LEVEL_INFO = 2;
	public static final int LEVEL_WARNING = 3;
	public static final int LEVEL_ERROR = 4;
	public static final int LEVEL_FATAL = 5;
	
	
	public static final String INVALID_TOKEN = "INVALID";
	
	public static final int STRING = 1;
	public static final int TABLENAME = 2;
	public static final int SEARCH_BY_KEYWORD = 1;
	public static final int SEARCH_BY_CITY = 2;
	
	public static final int BUFFER_SIZE = 8 * 1024;
	public static final int THREAD_POOL_SIZE = 5000;
	public static final int TOKEN_LENGTH = 64;
	public static final int REFRESH_TIME_INTERVAL = 24 * 3600 * 1000;
	public static final int SPOTS_PAGE_SIZE = 16;
	
	
	public static final long DB_CON_KEEPALIVE_INTERVAL = 60 * 60 * 1000;
}
