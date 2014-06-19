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

public class VerifyHandler implements HttpHandler
{

	/**
	 * username&password&timestamp
	 * all url encoded
	 * password is SHA1(SHA1(plain text)||hex(timestamp))
	 * */
	@Override
	public void handle(HttpExchange exchange)
	{
		try
		{
			InputStream is = exchange.getRequestBody();
			byte [] buf = new byte[Constant.BUFFER_SIZE];
			int read = is.read(buf, 0, Constant.BUFFER_SIZE);
			String data = new String(buf, 0, read);
			
			Util.Log(Constant.LOG_LEVEL_INFO, "Receive verify request : "
			+ URLDecoder.decode(URLDecoder.decode(data, "UTF-8"), "UTF-8"), "VerifyHandler");
			
			HashMap<String, String> params = Util.getParams(data);
			boolean verified = DBManager.getDBManager().virifyUser(params.get("username"), params.get("password"), params.get("timestamp"));
			
			JSONObject response = new JSONObject();
			if (verified)
			{
				response.put("result", "success");
				String token = Util.genToken();
				DBManager.getDBManager().updateToken(params.get("username"), token);
				response.put("token", token);
				response.put("committed", DBManager.getDBManager().getCommitted(params.get("username")));
				response.put("recspots", DBManager.getDBManager().getRecSpots(params.get("username")));
				response.put("info", DBManager.getDBManager().getUserInfo(params.get("username")));
			}
			else
			{
				response.put("result", "fail");
				response.put("reason", "账户名或密码错误");
			}
			
			byte [] responseBody = response.toString().getBytes("UTF-8");
			exchange.sendResponseHeaders(200, responseBody.length);
	        OutputStream os = exchange.getResponseBody();
	        os.write(responseBody);
	        os.flush();
	        os.close();
	        
	        Util.Log(Constant.LOG_LEVEL_INFO, "Send verify response : " + new String(responseBody, "UTF-8"), "VerifyHandler");
		}
		catch (IOException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "VerifyHandler");
		}
		catch (JSONException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "VerifyHandler");
		}
	}

}
