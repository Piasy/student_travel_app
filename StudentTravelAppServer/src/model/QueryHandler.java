package model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.Util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import dao.DBManager;

public class QueryHandler implements HttpHandler
{

	/**
	 * username&token&keyword&pagenum
	 * all url encoded
	 * */
	@Override
	public void handle(HttpExchange exchange) throws IOException
	{
		try
		{
			InputStream is = exchange.getRequestBody();
			byte [] buf = new byte[Constant.BUFFER_SIZE];
			int read = is.read(buf, 0, Constant.BUFFER_SIZE);
			String data = new String(buf, 0, read);
			
			Util.Log(Constant.LOG_LEVEL_INFO, "Receive query request : "
			+ URLDecoder.decode(URLDecoder.decode(data, "UTF-8"), "UTF-8"), "QueryHandler");
			
			HashMap<String, String> params = Util.getParams(data);
			
			String token = DBManager.getDBManager().getToken(params.get("username"));
			
			JSONObject response = new JSONObject();
			if (token.equals(params.get("token")))
			{
				response.put("result", "success");
				int searchType = Constant.SEARCH_BY_KEYWORD;
				try
				{
					searchType = Integer.parseInt(params.get("searchtype"));
				}
				catch (NumberFormatException e)
				{
					Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "QueryHandler");
				}
				
				switch (searchType)
				{
				case Constant.SEARCH_BY_KEYWORD:
					response.put("spots", DBManager.getDBManager().getSpots(params.get("keyword"), 
							params.get("pagenum")));
					break;
				case Constant.SEARCH_BY_CITY:
					response.put("spots", DBManager.getDBManager().getSpotsByCity(params.get("keyword"), 
							params.get("pagenum")));
					break;
				default:
					response.put("spots", new JSONArray());
					break;
				}
			}
			else
			{
				response.put("result", "fail");
				response.put("reason", "invalid token");
			}
			
			byte [] responseBody = response.toString().getBytes("UTF-8");
			exchange.sendResponseHeaders(200, responseBody.length);
	        OutputStream os = exchange.getResponseBody();
	        os.write(responseBody);
	        os.flush();
	        os.close();
	        
	        Util.Log(Constant.LOG_LEVEL_INFO, "Send query response : " + new String(responseBody, "UTF-8"), "QueryHandler");
		}
		catch (IOException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "QueryHandler");
		}
		catch (JSONException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "QueryHandler");
		}
	}

}
