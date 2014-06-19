package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class Driver
{
	String url_prefix = "http://lvyou.baidu.com/";
	String url_surfix = "jingdian/";
	HashMap<String, String> spots = new HashMap<String, String>();
	HashMap<String, String> areas = new HashMap<String, String>();
	HashMap<String, String> failed = new HashMap<String, String>();
	String url = "jdbc:mysql://localhost/studenttravelappdata";
    String user = "root";
    String pwd = "1qaz2wsx!@"; 
    Connection conn;
    Statement stmt;
    
	protected void run() throws InterruptedException
	{
		
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(url, user, pwd);
			if (!conn.isClosed())
			{
				stmt = conn.createStatement();
//				getSpots();
				getDetails();
//				getPhoto();
//				recorrect();
//				try
//				{
//					getOneSpotDetail("http://lvyou.baidu.com/beijingdongwuyuan");
//				}
//				catch (SocketTimeoutException e)
//				{
//					e.printStackTrace();
//				}
//				catch (UnknownHostException e)
//				{
//					e.printStackTrace();
//				}
//				catch (IOException e)
//				{
//					e.printStackTrace();
//				}
//				catch (JSONException e)
//				{
//					e.printStackTrace();
//				}
				System.out.println("Job finished!");
			}
		}
		catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
	}
	
	protected void getPhoto()
	{
		
	}
	
	protected void getDetails() throws InterruptedException
	{
		try
		{
			String sql = "SELECT * FROM spots";
			Statement select = conn.createStatement();
			ResultSet res = select.executeQuery(sql);
			ArrayList<String> failedSpots = new ArrayList<String>();
			while (res.next())
			{
				String link = res.getString("link");
				String name = res.getString("name");
//				String ticket = res.getString("ticket");
				int id = res.getInt("id");
//				if (ticket != null)
//				{
//					continue;
//				}
				try
				{
					if (getOneSpotDetail(link))
					{
						System.out.println("Get details of " + id + " : " + name + " success!");
					}
					
					Thread.sleep(50);
				}
				catch (SocketTimeoutException e)
				{
					System.out.println("Connection timeout...");
					e.printStackTrace();
					failedSpots.add(link);
				}
				catch (UnknownHostException e)
				{
					System.out.println("UnknownHostException...");
					e.printStackTrace();
					failedSpots.add(link);
				}
				catch (JSONException e)
				{
					System.out.println("JSONException...");
					e.printStackTrace();
					failedSpots.add(link);
				}
			}
			
			ArrayList<String> secondFail = new ArrayList<String>();
			while (0 < failedSpots.size())
			{
				for (String link : failedSpots)
				{
					try
					{
						if (getOneSpotDetail(link))
						{
							System.out.println("Get details of " + link + " success!");
						}
						Thread.sleep(500);
					}
					catch (SocketTimeoutException e)
					{
						System.out.println("Connection timeout...");
						e.printStackTrace();
						secondFail.add(link);
					}
					catch (UnknownHostException e)
					{
						System.out.println("UnknownHostException...");
						e.printStackTrace();
						secondFail.add(link);
					}
					catch (JSONException e)
					{
						System.out.println("JSONException...");
						e.printStackTrace();
						secondFail.add(link);
					}
				}
				
				failedSpots.clear();
				failedSpots.addAll(secondFail);
				secondFail.clear();
			}
		}
		catch (IOException | SQLException e)
		{
			e.printStackTrace();
		}	
	}
	
	protected boolean getOneSpotDetail(String link) throws SocketTimeoutException, UnknownHostException, SQLException, IOException, JSONException
	{
		boolean ret = false;
		Document doc = Jsoup.connect(link).get();
		JSONObject ticket = new JSONObject();
//		JSONObject tip = new JSONObject();
//		StringBuilder introduction = new StringBuilder();
//		Elements intros = doc.select(".J-sketch-more-info").select(".subview-basicinfo-alert-more");
//		if (intros.size() == 0)
//		{
//			Element intro = doc.getElementById("J_desc-show");
//			if (intro == null)
//			{
//				introduction.append("暂无");
//			}
//			else
//			{
//				introduction.append(intro.text());
//			}
//		}
//		else
//		{
//			for (Element intro : intros)
//			{
//				introduction.append(intro.text());
//				introduction.append("\n");
//			}
//		}
		
		Elements ticketPrice = doc.select(".val").select(".price-value");
		if (ticketPrice.size() == 0)
		{
			ticket.put("price", "暂无");
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			for (Element price : ticketPrice)
			{
				sb.append(price.text());
				sb.append("  ");
			}
			ticket.put("price", sb.toString());
		}//subview-subitem  ticket_info-attention-list clearfix
		Elements ticketFavor = doc.select(".subview-subitem").select(".ticket_info-attention-list").select(".clearfix");
		if (ticketFavor.size() == 0)
		{
			ticket.put("favor", "none");
		}
		else
		{
//			StringBuilder sb = new StringBuilder();
//			System.out.println(ticketFavor.size());
//			for (Element favor : ticketFavor)
//			{
//				sb.append(favor.text());
//				System.out.println(favor.text());
//				sb.append("  ");
//			}
			ticket.put("favor", ticketFavor.get(0).text());
		}
		
//		Element open = doc.getElementById("J-aside-info-opening_hours");
//		StringBuilder openInfo = new StringBuilder();
//		if (open == null)
//		{
//			openInfo.append("暂无");
//		}
//		else
//		{
//			Elements openDetails = open.select(".text-p").select(".text-desc-p");
//			for (Element detail : openDetails)
//			{
//				openInfo.append(detail.text());
//				openInfo.append("\n");
//			}
//		}
//		
//		Element sug = doc.getElementById("J-aside-info-recommend_visit_time");
//		String suggestion = "暂无";
//		if ((sug != null) && (sug.text() != null) && !sug.text().equals(""))
//		{
//			suggestion = sug.text();
//		}
		
//		Element foodTip = doc.getElementById("mod-dining");
//		if (foodTip == null)
//		{
//			tip.put("food", "none");
//		}
//		else
//		{
//			tip.put("food", foodTip.text());
//		}
//		
//		Element shoppingTip = doc.getElementById("mod-shopping");
//		if (shoppingTip == null)
//		{
//			tip.put("shopping", "none");
//		}
//		else
//		{
//			tip.put("shopping", shoppingTip.text());
//		}
//		
//		Element cultureTip = doc.getElementById("mod-geography_history");
//		if (cultureTip == null)
//		{
//			tip.put("culture", "none");
//		}
//		else
//		{
//			tip.put("culture", cultureTip.text());
//		}
//		
//		Element trafficTip = doc.getElementById("mod-traffic");
//		if (trafficTip == null)
//		{
//			tip.put("traffic", "none");
//		}
//		else
//		{
//			tip.put("traffic", trafficTip.text());
//		}
//		
//		Element besttimeTip = doc.getElementById("mod-besttime");
//		if (besttimeTip == null)
//		{
//			tip.put("besttime", "none");
//		}
//		else
//		{
//			tip.put("besttime", besttimeTip.text());
//		}
//		
//		Element tipTip = doc.getElementById("mod-attention");
//		if (tipTip == null)
//		{
//			tip.put("tip", "none");
//		}
//		else
//		{
//			tip.put("tip", tipTip.text());
//		}
//		
//		Element activityTip = doc.getElementById("mod-entertainment");
//		if (activityTip == null)
//		{
//			tip.put("activity", "none");
//		}
//		else
//		{
//			tip.put("activity", activityTip.text());
//		}
		
//		Element tip = doc.getElementById("scene-middle-tab");
//		StringBuilder tipInfo = new StringBuilder();
//		if (tip == null)
//		{
//			tipInfo.append("暂无");
//		}
//		else
//		{
//			Elements tipDetails = tip.select(".text-p").select(".text-desc-p");
//			for (Element detail : tipDetails)
//			{
//				tipInfo.append(detail.text());
//				tipInfo.append("\n");
//			}
//		}
		
//		String sql = "UPDATE spots SET `ticket` = '" + prepare(ticket.toString()) + "', " +
//							   "`opentime` = '" + prepare(openInfo.toString()) + "', " +
//				               "`suggesttime` = '" + prepare(suggestion) + "', " +
//							   "`intro` = '" + prepare(introduction.toString()) + "', " +
//							   "`tip` = '" + prepare(tipInfo.toString()) + "' " +
//						 "WHERE `link` = '" + prepare(link) + "'";
//		String sql = "UPDATE spots SET `ticket` = '" + prepare(ticket.toString()) + "', " 
//				+ "`tip` = '" + prepare(tip.toString()) + "' " 
//				+ "WHERE `link` = '" + prepare(link) + "'";
		String sql = "UPDATE spots SET `ticket` = '" + prepare(ticket.toString()) + "' " 
				+ "WHERE `link` = '" + prepare(link) + "'";
//		System.out.println(ticket.toString());
//		System.out.println();
		stmt.execute(sql);
		ret = true;
		return ret;
	}
	
	protected void getSpots()
	{
		init();
		while (0 < areas.size())
		{
			parse();
		}
		show(failed);
		System.out.println("Finished, total " + spots.size() + " spots");
	}
	
	protected void init()
	{
		try
		{
			
//			Document doc = Jsoup.connect("http://lvyou.baidu.com/scene/").get();
//			//System.out.println(doc.html());
//			Elements links = doc.getElementsByTag("a");
//			System.out.println("Driver.main()");
//			for (Element link : links)
//			{
//				System.out.println(link.attr("href") + " : " + link.text());
//			}
		
			
//			HashMap<String, String> spots = new HashMap<String, String>();
//			BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream(new File("docs/spots.txt")), "UTF-8"));
//			String place = is.readLine();
//			while (place != null)
//			{
//				String [] details = place.split(" ");
//				System.out.println(details[1] + " : " + details[0]);
//				place = is.readLine();
//				spots.put(details[1], url_prefix + details[0] + url_surfix);
//			}
//			is.close();
//			show(spots);
			
			
			BufferedReader is;
			is = new BufferedReader(new InputStreamReader(new FileInputStream(new File("docs/spots.txt")), "Unicode"));
			String place = is.readLine();
			while (place != null)
			{
				String [] details = place.split(" : ");
				areas.put(details[0], details[1]);
				place = is.readLine();
			}
			is.close();
			
			Set<String> unget = new TreeSet<String>();
			ResultSet rse = stmt.executeQuery("select * from spots");
			while (rse.next())
			{
				String city = rse.getString("city");
				String photo = rse.getString("photo");
				if (photo == null)
				{
					unget.add(areas.get(city));
				}
			}
			
			System.out.println(unget.size() + " cities");
			ArrayList<String> parsed = new ArrayList<String>();
			for (String key : areas.keySet())
			{
				if (!unget.contains(areas.get(key)))
				{
					parsed.add(key);
				}
			}
			for (String key : parsed)
			{
				areas.remove(key);
			}
			System.out.println(areas.size() + " cities");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	int count = 0;
	protected void parse()
	{
		ArrayList<String> parsed = new ArrayList<String>();
		JSONObject scene = new JSONObject(), data = new JSONObject();
		JSONArray scene_list = new JSONArray();
		try
		{
			Set<String> keys = areas.keySet();
			for (String key : keys)
			{
				int pageNum = 0;
				String getURL = "http://lvyou.baidu.com/destination/ajax/allview?surl=" + areas.get(key) + "&format=json&cid=0&pn=" + pageNum + "&t=1390194506222";
				Document doc = null;
				try
				{
					doc = Jsoup.connect(getURL).get();
				}
				catch (SocketTimeoutException e)
				{
					failed.put(key, areas.get(key));
					System.out.println(e.getMessage());
					continue;
				}
				try
				{
					data = new JSONObject(doc.text());
					System.out.println("parse" + key);
					scene_list = data.getJSONObject("data").getJSONArray("scene_list");
				}
				catch (JSONException e)
				{
					System.out.println(e.getMessage());
					continue;
				}
				while (0 < scene_list.length())
				{
					System.out.println("get " + scene_list.length() + " spots");
					
					for (int i = 0; i < scene_list.length(); i ++)
					{
						try
						{
							scene = scene_list.getJSONObject(i);
							String link = url_prefix + scene.getString("surl");
							spots.put(scene.getString("sname"), link);
							
							String sql;
							String photo = scene.getJSONObject("cover").getString("full_url");
							String map_info = scene.getJSONObject("ext").getString("map_info");
							String address = scene.getJSONObject("ext").getString("address");
							int longitude = 720000000, latitude = 720000000;
							try
							{
								if (map_info != null && !map_info.equals(""))
								{
									
										String [] maps = map_info.split(",");
										longitude = (int) (Float.parseFloat(maps[0]) * 1000000);
										latitude = (int) (Float.parseFloat(maps[1]) * 1000000);
									
								}
								
								String phone = scene.getJSONObject("ext").getString("phone");
//									sql = "SELECT * FROM spots WHERE link='" + link + "'";
//									ResultSet rse = stmt.executeQuery(sql);
//									if (!rse.next())
//									{
//										sql = "INSERT INTO spots (name, city, link) VALUES ('" + scene.getString("sname") + "', '" + key + "', '" + link + "')";
									sql = "UPDATE spots SET "
											+ "`photo` = '" + prepare(photo) + "', "
											+ "`latitude` = '" + latitude + "', "
											+ "`longitude` = '" + longitude + "', "
											+ "`address` = '" + prepare(address) + "', "
											+ "`phone` = '" + prepare(phone) + "' "
											+ "WHERE `link` = '" + link + "'";
									stmt.execute(sql);
//									}
							}
							catch (NumberFormatException e)
							{
								System.out.println(e.getMessage());
								System.out.println("at " + scene.toString());
							}
						}
						catch (JSONException e)
						{
							System.out.println(e.getMessage());
							System.out.println(scene.toString());
						}
						
					}
					
					count += scene_list.length();
					Thread.sleep(500);
					pageNum ++;
					getURL = "http://lvyou.baidu.com/destination/ajax/allview?surl=" + areas.get(key) + "&format=json&cid=0&pn=" + pageNum + "&t=1390194506222";
					try
					{
						doc = Jsoup.connect(getURL).get();
					}
					catch (SocketTimeoutException e)
					{
						failed.put(key, areas.get(key));
						System.out.println(e.getMessage());
						continue;
					}
					try
					{
						data = new JSONObject(doc.text());
						scene_list = data.getJSONObject("data").getJSONArray("scene_list");
					}
					catch (JSONException e)
					{
						System.out.println(e.getMessage());
					}
				}
				parsed.add(key);
				
				System.out.println("finished " + count + " spots");
			}
			
			for (String key : parsed)
			{
				areas.remove(key);
			}
		}
		catch (IOException | InterruptedException  e)
		{
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	protected void recorrect()
	{
		try
		{
			Statement query = conn.createStatement();
//			ResultSet rse;
			ResultSet rse = query.executeQuery("select * from spots");
			while (rse.next())
			{
				String link = rse.getString("link");
				String sql1 = "update spots set `score` = 0.0 where `link` = '" + link + "'";
				stmt.execute(sql1);
				
				sql1 = "insert into comments (name, city, eval) values('" + rse.getString("name") + "', "
						+ "'" + rse.getString("city") + "', '[]')";
				stmt.execute(sql1);
				
				if (rse.getString("photo") == null || rse.getString("photo").equals(""))
				{
					String sql = "update spots set `photo` = '暂无' where `link` = '" + link + "'";
					stmt.execute(sql);
				}
				
				if (rse.getString("address") == null || rse.getString("address").equals("") || rse.getString("address").equals(" "))
				{
					String sql = "update spots set `address` = '暂无' where `link` = '" + link + "'";
					stmt.execute(sql);
				}
				
				if (rse.getString("phone") == null || rse.getString("phone").equals(""))
				{
					String sql = "update spots set `phone` = '暂无' where `link` = '" + link + "'";
					stmt.execute(sql);
				}
				
				if (rse.getInt("latitude") == 0)
				{
					String sql = "update spots set `latitude` = 720000000 where `link` = '" + link + "'";
					stmt.execute(sql);
				}
				
				if (rse.getInt("longitude") == 0)
				{
					String sql = "update spots set `longitude` = 720000000 where `link` = '" + link + "'";
					stmt.execute(sql);
				}
			}
//			while (rse.next())
//			{
//				String link = rse.getString("link");
//				String sql = "update spots set `ticket` = '暂无' where `link` = '" + link + "'";
//				stmt.execute(sql);
//			}
//			
//			rse = query.executeQuery("select * from spots where `opentime` = '鏆傛棤'");
//			while (rse.next())
//			{
//				String link = rse.getString("link");
//				String sql = "update spots set `opentime` = '暂无' where `link` = '" + link + "'";
//				stmt.execute(sql);
//			}
//			
//			rse = query.executeQuery("select * from spots where `suggesttime` = '鏆傛棤'");
//			while (rse.next())
//			{
//				String link = rse.getString("link");
//				String sql = "update spots set `suggesttime` = '暂无' where `link` = '" + link + "'";
//				stmt.execute(sql);
//			}
//			
//			rse = query.executeQuery("select * from spots where `intro` = '鏆傛棤'");
//			while (rse.next())
//			{
//				String link = rse.getString("link");
//				String sql = "update spots set `intro` = '暂无' where `link` = '" + link + "'";
//				stmt.execute(sql);
//			}
//			
//			rse = query.executeQuery("select * from spots where `tip` = '鏆傛棤'");
//			while (rse.next())
//			{
//				String link = rse.getString("link");
//				String sql = "update spots set `tip` = '暂无' where `link` = '" + link + "'";
//				stmt.execute(sql);
//			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws InterruptedException, SocketTimeoutException, SQLException, IOException
	{
		new Driver().run();
//		try
//		{
//			
//			Document doc = Jsoup.connect("http://lvyou.baidu.com/scene/").get();
//			//System.out.println(doc.html());
//			Elements links = doc.getElementsByTag("a");
//			System.out.println("Driver.main()");
//			for (Element link : links)
//			{
//				System.out.println(link.attr("href") + " : " + link.text());
//			}
//		}
//		catch (IOException e)
//		{
//			
//		}
	}
	
	protected void show(HashMap<String, String> spots)
	{
		Set<String> keys = spots.keySet();
		for (String key : keys)
		{
			System.out.println(key + " ==> " + spots.get(key));
		}
	}
	
	protected String prepare(String param)
    {
    	StringBuffer sb = new StringBuffer();
    	for (int i = 0; i < param.length(); i ++)
    	{
    		if (param.charAt(i) == 92)
    		{
    			continue;
    		}
    		if (param.charAt(i) == 39)
    		{
    			sb.append("''");
    		}
    		else
    		{
				sb.append(param.charAt(i));
			}
    	}
    	return sb.toString();
    }
}
