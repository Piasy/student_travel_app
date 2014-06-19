package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import model.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.Util;

public class DBManager
{
	static int count = 0;
	static DBManager instance;
	
	public static DBManager getDBManager()
	{
		if (count == 0)
		{
			instance = new DBManager();
			count ++;
		}
		
		return instance;
	}
	
	public void test()
	{
		try
		{
    		Statement stmt;
    		if ((stmt = getStatement()) != null)
    		{
    			String sql = "SELECT count(*) FROM users";
    			Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager hasUser");
    			
    			ResultSet rst = stmt.executeQuery(sql);
    			if (rst.next())
    			{
    				System.out.println(rst.getInt(1));
    			}
    		}
		}
    	catch (SQLException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager hasUser");
    	}
	}
	
    public boolean hasUser(String username)
    {
    	boolean ret = false;
    	
    	try
		{
    		Statement stmt;
    		if ((stmt = getStatement()) != null)
    		{
    			String sql = "SELECT * FROM users WHERE username = '" 
						+ prepare(username, Constant.STRING) + "'";
    			Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager hasUser");
    			
    			ResultSet rst = stmt.executeQuery(sql);
				if (rst.next())
				{
					ret = true;
				}
    		}
		}
    	catch (SQLException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager hasUser");
    	}
    	
    	return ret;
    }
    
    public boolean addUser(String username, String password, JSONObject info)
    {
    	boolean ret = false;
    	
    	try
		{
    		Statement stmt;
    		if (!hasUser(username) && (stmt = getStatement()) != null)
    		{
    			String sql = "INSERT INTO users (username, password, info, plan, visited, viewed, token, committed) VALUES "
    					+ "('" + prepare(username, Constant.STRING) + "', "
    					+ "'" + prepare(password, Constant.STRING) + "', "
    					+ "'" + prepare(info.toString(), Constant.STRING) + "', "
    					+ "'[]', "
    					+ "'[]', "
    					+ "'[]', "
    					+ "'" + Constant.INVALID_TOKEN + "', "
    					+ "'[]')";
    			Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager addUser");
    			
    			stmt.execute(sql);
    			ret = true;
    		}
		}
    	catch (SQLException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager addUser");
    	}
    	
    	return ret;
    }
    
    public JSONObject getUserInfo(String username)
    {
    	JSONObject info = new JSONObject();
    	
    	try
		{
    		Statement stmt;
    		if ((stmt = getStatement()) != null)
    		{
    			String sql = "SELECT * FROM users WHERE username = '" 
						+ prepare(username, Constant.STRING) + "'";
				Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager getUserInfo");
				
				ResultSet rst = stmt.executeQuery(sql);
				if (rst.next())
				{
					info = new JSONObject(rst.getString("info"));
				}
    		}
		}
    	catch (SQLException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager getUserInfo");
    	}
    	catch (JSONException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager getUserInfo");
    	}
    	
    	return info;
    }
    
    public boolean updateUserInfo(String username, JSONObject info)
    {
    	boolean ret = false;
    	
    	try
		{
    		Statement stmt;
    		if ((stmt = getStatement()) != null)
    		{
    			String sql = "UPDATE users SET info = '" + prepare(info.toString(), Constant.STRING) 
    					+ "' WHERE username = '" + prepare(username, Constant.STRING) + "'";
				Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager updateUserInfo");
				
				stmt.execute(sql);
				ret = true;
    		}
		}
    	catch (SQLException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager updateUserInfo");
    	}
    	
    	return ret;
    }
    
    public boolean virifyUser(String username, String password, String timestamp)
    {
    	boolean ret = false;
    	
    	try
		{
    		Statement stmt;
    		if ((stmt = getStatement()) != null)
    		{
    			if ((username != null) && (password != null) && (timestamp != null))
    			{
    				String sql = "SELECT * FROM users WHERE username = '" 
    						+ prepare(username, Constant.STRING) + "'";
    				Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager virifyUser");
    				
    				ResultSet rst = stmt.executeQuery(sql);
    				if (rst.next())
    				{
    					String passwd = rst.getString("password");
    					String encrypt = Util.getSHA1Value(passwd + Long.toHexString(Long.parseLong(timestamp)));
    					if (password.equals(encrypt))
    					{
    						ret = true;
    					}
    				}
    			}
    		}
		}
    	catch (NumberFormatException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager virifyUser");
    	}
    	catch (SQLException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager virifyUser");
    	}
    	
    	return ret;
    }
    
    public boolean updateToken(String username, String token)
    {
    	boolean ret = false;
    	
    	try
		{
    		Statement stmt;
    		if ((stmt = getStatement()) != null)
    		{
    			String sql = "UPDATE users SET token = '" + token + "' WHERE username = '" + prepare(username, Constant.STRING) + "'";
				Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager updateToken");
				
				stmt.execute(sql);
				ret = true;
    		}
		}
    	catch (SQLException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager updateToken");
    	}
    	
    	return ret;
    }
    
    public String getToken(String username)
    {
    	String token = "";
    	
    	try
		{
    		Statement stmt;
    		if ((stmt = getStatement()) != null)
    		{
    			String sql = "SELECT * FROM users WHERE username = '" 
						+ prepare(username, Constant.STRING) + "'";
    			Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager getToken");
    			
    			ResultSet rst = stmt.executeQuery(sql);
				if (rst.next())
				{
					token = rst.getString("token");
				}
    		}
		}
    	catch (SQLException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager getToken");
    	}
    	
    	return token;
    }
    
    public JSONArray getSpotsByCity(String city, String pagenum)
    {
    	JSONArray spots = new JSONArray();
    	
    	try
		{
    		int pageNum = Integer.parseInt(pagenum);
    		Statement stmt;
    		if ((stmt = getStatement()) != null)
    		{
    			String sql = "SELECT * FROM spots WHERE city = '" 
						+ prepare(city, Constant.STRING) + "' ORDER BY score DESC";
    			Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager getSpotsByCity");
    			
    			int count = 0, added = 0;
    			ResultSet rst = stmt.executeQuery(sql);
				while (rst.next())
				{
					if (count < pageNum * Constant.SPOTS_PAGE_SIZE)
					{
						count ++;
						continue;
					}
					
					spots.put(cursor2Json(rst));
					added ++;
					
					if (Constant.SPOTS_PAGE_SIZE < added)
					{
						break;
					}
				}
    		}
		}
    	catch (SQLException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager getSpotsByCity");
    	}
    	catch (JSONException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager getSpotsByCity");
    	}
    	
    	return spots;
    }
    
    public JSONArray getSpots(String keyword, String pagenum)
    {
    	JSONArray spots = new JSONArray();
    	
    	try
		{
    		int pageNum = Integer.parseInt(pagenum);
    		Statement stmt;
    		if ((stmt = getStatement()) != null)
    		{
    			String sql = "SELECT * FROM spots WHERE name LIKE '%" 
						+ prepare(keyword, Constant.STRING) + "%' ORDER BY score DESC";
    			Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager getSpots");
    			
    			int count = 0, added = 0;
    			ResultSet rst = stmt.executeQuery(sql);
				while (rst.next())
				{
					if (count < pageNum * Constant.SPOTS_PAGE_SIZE)
					{
						count ++;
						continue;
					}
					
					spots.put(cursor2Json(rst));
					added ++;
					
					if (Constant.SPOTS_PAGE_SIZE < added)
					{
						break;
					}
				}
				
				if (added < Constant.SPOTS_PAGE_SIZE)
				{
					sql = "SELECT * FROM spots WHERE city LIKE '%" 
							+ prepare(keyword, Constant.STRING) + "%' ORDER BY score DESC";
					rst = stmt.executeQuery(sql);
					while (rst.next())
					{
						spots.put(cursor2Json(rst));
						added ++;
						
						if (Constant.SPOTS_PAGE_SIZE < added)
						{
							break;
						}
					}
				}
    		}
		}
    	catch (SQLException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager getSpots");
    	}
    	catch (JSONException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager getSpots");
    	}
    	
    	return spots;
    }
    
    public JSONArray getCommitted(String username)
    {
    	JSONArray ret = new JSONArray();
    	
    	try
		{
    		Statement stmt;
    		if ((stmt = getStatement()) != null)
    		{
    			String sql = "SELECT * FROM users WHERE username = '" 
						+ prepare(username, Constant.STRING) + "'";
    			Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager getCommitted");
    			
    			ResultSet rst = stmt.executeQuery(sql);
				if (rst.next())
				{
					Util.Log(Constant.LOG_LEVEL_DEBUG, "committed : " + rst.getString("committed"), "DBManager getCommitted");
					ret = new JSONArray(rst.getString("committed"));
				}
    		}
		}
    	catch (SQLException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager getCommitted");
    	}
		catch (JSONException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager getCommitted");
		}
    	
    	Util.Log(Constant.LOG_LEVEL_DEBUG, "return : " + ret.toString(), "DBManager getCommitted");
    	return ret;
    }
	
    /**
     * eval: jsonobject-format string
     * */
    public boolean addCommit(String name, String city, String score, String eval)
    {
    	boolean ret = false;
    	
    	try
		{
    		Statement stmt;
    		if ((stmt = getStatement()) != null)
    		{
    			JSONArray oldEvalArray = getEval(name, city);
    			if (oldEvalArray.length() == 0)
    			{
    				String sql = "UPDATE spots SET score = " + score
    							+ " WHERE name = '" + prepare(name, Constant.STRING) + "' AND "
	    						+ "city = '" + prepare(city, Constant.STRING) + "'";
    				Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager addCommit");
    				
    				stmt.execute(sql);
    			}
    			else
    			{
    				String sql = "SELECT * FROM spots "
							+ "WHERE name = '" + prepare(name, Constant.STRING) + "' AND "
    						+ "city = '" + prepare(city, Constant.STRING) + "'";
					Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager addCommit");
					
	    			ResultSet rst = stmt.executeQuery(sql);
					if (rst.next())
					{
						float oldScore = rst.getFloat("score");
						float newScore = (oldScore * oldEvalArray.length() + Float.parseFloat(score)) 
								/ (oldEvalArray.length() + 1);
						
						sql = "UPDATE spots SET score = " + newScore 
								+ " WHERE name = '" + prepare(name, Constant.STRING) + "' AND "
	    						+ "city = '" + prepare(city, Constant.STRING) + "'";
						Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager addCommit");
						
						stmt.execute(sql);
					}
    			}
    			oldEvalArray.put(new JSONObject(eval));
				
				String sql = "UPDATE comments SET "
						+ "eval = '" + prepare(oldEvalArray.toString(), Constant.STRING) + "' "
						+ "WHERE name = '" + prepare(name, Constant.STRING) + "' AND "
						+ "city = '" + prepare(city, Constant.STRING) + "'";
				Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager addCommit");
				
				stmt.execute(sql);
				
				ret = true;
    		}
		}
    	catch (SQLException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager addCommit");
    	}
    	catch (JSONException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager addCommit");
    	}
    	catch (NumberFormatException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager addCommit");
    	}
    	
    	return ret;
    }
    
    public JSONArray getRecSpots(String username)
    {
    	JSONArray ret = new JSONArray();
    	
    	try
		{
    		Statement stmt;
    		if ((stmt = getStatement()) != null)
    		{
    			String sql = "SELECT * FROM spots WHERE name = '" 
						+ prepare("北京动物园", Constant.STRING) + "'";
    			Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager getRecSpots");
    			
    			ResultSet rst = stmt.executeQuery(sql);
				if (rst.next())
				{
					ret.put(cursor2Json(rst));
				}
				
				sql = "SELECT * FROM spots WHERE name = '" 
						+ prepare("北京植物园", Constant.STRING) + "'";
    			Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager getRecSpots");
    			
    			rst = stmt.executeQuery(sql);
				if (rst.next())
				{
					ret.put(cursor2Json(rst));
				}
				
				sql = "SELECT * FROM spots WHERE name = '" 
						+ prepare("天安门广场", Constant.STRING) + "'";
    			Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager getRecSpots");
    			
    			rst = stmt.executeQuery(sql);
				if (rst.next())
				{
					ret.put(cursor2Json(rst));
				}
				
				sql = "SELECT * FROM spots WHERE name = '" 
						+ prepare("欢乐谷", Constant.STRING) + "' and city = '"
						+ prepare("北京", Constant.STRING) + "'";
    			Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager getRecSpots");
    			
    			rst = stmt.executeQuery(sql);
				if (rst.next())
				{
					ret.put(cursor2Json(rst));
				}
    		}
		}
    	catch (SQLException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager getRecSpots");
    	}
		catch (JSONException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager getRecSpots");
		}
    	
//    	Util.Log(Constant.LOG_LEVEL_DEBUG, ret.toString(), "DBManager getRecSpots");
    	return ret;
    }
    
    protected JSONObject cursor2Json(ResultSet rst) throws JSONException, SQLException
    {
    	JSONObject spot = new JSONObject();
		spot.put("name", rst.getString("name"));
		spot.put("city", rst.getString("city"));
		spot.put("ticket", new JSONObject(rst.getString("ticket")));
		spot.put("opentime", rst.getString("opentime"));
		spot.put("suggesttime", rst.getString("suggesttime"));
		spot.put("intro", rst.getString("intro"));
		spot.put("tip", new JSONObject(rst.getString("tip")));
		spot.put("score", rst.getFloat("score"));
		spot.put("photo", rst.getString("photo"));
		spot.put("latitude", rst.getInt("latitude"));
		spot.put("longitude", rst.getInt("longitude"));
		spot.put("address", rst.getString("address"));
		spot.put("phone", rst.getString("phone"));
		spot.put("eval", getEval(rst.getString("name"), rst.getString("city")));
		
		return spot;
    }
    
    /**
     * one commit format:
     * {
     * "content" : content,		//string
     * "username" : username,	//string
     * "score" : score,			//float
     * "time" : time			//string, yyyy/MM/dd HH:mm
     * }
     * */
    protected JSONArray getEval(String name, String city)
    {
    	JSONArray eval = new JSONArray();
    	
    	try
		{
    		Statement stmt;
    		if ((stmt = getStatement()) != null)
    		{
    			String sql = "SELECT * FROM comments WHERE name = '" 
						+ prepare(name, Constant.STRING) + "' AND "
						+ "city = '" + prepare(city, Constant.STRING) + "'";
    			Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager getEval");
    			
    			ResultSet rst = stmt.executeQuery(sql);
				if (rst.next())
				{
					eval = new JSONArray(rst.getString("eval"));
				}
    		}
		}
    	catch (SQLException e)
    	{
    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager getEval");
    	}
		catch (JSONException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager getEval");
		}
    	
    	return eval;
    }
    
	String url = "jdbc:mysql://localhost/studenttravelappdata";
    String user = "root";
    String pwd = "1qaz2wsx!@";
    Connection conn;
    
    Timer keepAliveTimer = new Timer();
    TimerTask keepAliveTask = new TimerTask()
	{
		
		@Override
		public void run()
		{
			try
			{
	    		Statement stmt;
	    		if ((stmt = getStatement()) != null)
	    		{
	    			String sql = "SELECT count(*) FROM users";
	    			Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager hasUser");
	    			
	    			ResultSet rst = stmt.executeQuery(sql);
	    			if (rst.next())
	    			{
	    				int rowCount = rst.getInt(1);
						Util.Log(Constant.LOG_LEVEL_INFO, 
								"Keep connection alive, there are " + rowCount + " users", 
								"DBManager keepAliveTask");
	    			}
	    		}
			}
	    	catch (SQLException e)
	    	{
	    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager hasUser");
	    	}
		}
	};
    
    
    protected boolean init()
    {
    	boolean ret = false;
    	try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(url, user, pwd);
			if (!conn.isClosed())
			{
				Statement stmt = conn.createStatement();
				String  sql = "CREATE TABLE IF NOT EXISTS users (" 
							+ "id int NOT NULL AUTO_INCREMENT, " 
							+ "PRIMARY KEY(id), " 
							+ "username TEXT, " 
							+ "password TEXT, " 
							+ "info TEXT, " 
							+ "plan TEXT, " 
							+ "visited TEXT, " 
							+ "viewed TEXT, " 
							+ "token TEXT, " 
							+ "committed TEXT)";
				Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager init");
				
				stmt.execute(sql);
				
				Calendar nextZero = Calendar.getInstance();
				nextZero.setTime(new Date());
				nextZero.set(Calendar.HOUR_OF_DAY, 0);
				nextZero.set(Calendar.MINUTE, 0);
				nextZero.set(Calendar.SECOND, 0);
				nextZero.set(Calendar.MILLISECOND, 0);
				nextZero.add(Calendar.DAY_OF_MONTH, 1);
				refreshTimer.scheduleAtFixedRate(refreshTask, nextZero.getTime(), Constant.REFRESH_TIME_INTERVAL);
							
				keepAliveTimer.schedule(keepAliveTask, Constant.DB_CON_KEEPALIVE_INTERVAL, 
						Constant.DB_CON_KEEPALIVE_INTERVAL);
				
				ret = true;
			}
		}
		catch (InstantiationException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager init");
		}
		catch (IllegalAccessException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager init");
		}
		catch (ClassNotFoundException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager init");
		}
		catch (SQLException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager init");
		}
    	return ret;
    }
    
    Timer refreshTimer = new Timer();
    TimerTask refreshTask = new TimerTask()
	{
		@Override
		public void run()
		{
			try
			{
	    		Statement stmt;
	    		if ((stmt = getStatement()) != null)
	    		{
	    			String sql = "SELECT * FROM users";
	    			Util.Log(Constant.LOG_LEVEL_DEBUG, "Database excute : " + sql, "DBManager refreshTask");
	    			
					ResultSet rst = stmt.executeQuery(sql);
					while (rst.next())
					{
						String username = rst.getString("username");
						updateToken(username, Constant.INVALID_TOKEN);
					}
	    		}
			}
	    	catch (SQLException e)
	    	{
	    		Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager refreshTask");
	    	}
		}
	};
    
    protected Statement getStatement()
    {
    	Statement stmt = null;
    	synchronized (conn)
		{
			try
			{
				if (!conn.isClosed())
				{
					stmt = conn.createStatement();
				}
			}
			catch (SQLException e)
			{
				Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "DBManager getStatement");
			}
		}
    	return stmt;
    }
    
    protected String prepare(String param, int type)
    {
    	StringBuffer sb = new StringBuffer();
    	for (int i = 0; i < param.length(); i ++)
    	{
    		switch (type)
			{
			case Constant.STRING:
				if (param.charAt(i) == '\'')
	    		{
	    			sb.append("''");
	    		}
	    		else
	    		{
					sb.append(param.charAt(i));
				}
				break;
			case Constant.TABLENAME:
				if (param.charAt(i) == '`')
	    		{
	    			sb.append("``");
	    		}
	    		else
	    		{
					sb.append(param.charAt(i));
				}
				break;
			default:
				break;
			}
    	}
    	return sb.toString();
    }
    
    private DBManager()
    {
    	init();
    }
}
