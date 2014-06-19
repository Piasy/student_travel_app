package model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import util.Util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import dao.DBManager;

public class CommitHandler implements HttpHandler
{

	/**
	 * username&token&name&city&score&eval
	 * all url encoded
	 * eval is json-format, contains user info
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
			
			Util.Log(Constant.LOG_LEVEL_INFO, "Receive commit request : "
			+ URLDecoder.decode(URLDecoder.decode(data, "UTF-8"), "UTF-8"), "CommitHandler");
			
			HashMap<String, String> params = Util.getParams(data);
			
			String token = DBManager.getDBManager().getToken(params.get("username"));
			
			JSONObject response = new JSONObject();
			if (token.equals(params.get("token")))
			{
				if (DBManager.getDBManager().addCommit(params.get("name"), 
						params.get("city"), params.get("score"), params.get("eval")))
				{
					response.put("result", "success");
				}
				else
				{
					response.put("result", "fail");
					response.put("reason", "error");
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
	        
	        Util.Log(Constant.LOG_LEVEL_INFO, "Send commit response : " + new String(responseBody, "UTF-8"), "CommitHandler");
		}
		catch (IOException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "CommitHandler");
		}
		catch (JSONException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "CommitHandler");
		}
	}

}
