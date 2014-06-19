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

public class SignupHandler implements HttpHandler
{

	/**
	 * username&password
	 * all url encoded
	 * password is rsa encrypted
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
			
			Util.Log(Constant.LOG_LEVEL_INFO, "Receive signup request : "
			+ URLDecoder.decode(URLDecoder.decode(data, "UTF-8"), "UTF-8"), "SignupHandler");
			
			HashMap<String, String> params = Util.getParams(data);
			String cipherPasswd = params.get("password");
			JSONObject userInfo = new JSONObject(params.get("info"));
			
			JSONObject response = new JSONObject();
			
			
			if (DBManager.getDBManager().hasUser(params.get("username")))
			{
				response.put("result", "fail");
				response.put("reason", "邮箱已注册");
			}
			else
			{
				if (DBManager.getDBManager().addUser(params.get("username"), 
						Util.RSADecrypt(cipherPasswd), userInfo))
				{
					response.put("result", "success");
					String token = Util.genToken();
					DBManager.getDBManager().updateToken(params.get("username"), token);
					response.put("token", token);
				}
				else
				{
					response.put("result", "fail");
					response.put("reason", "注册失败，请稍后重试");
				}
			}
			
			byte [] responseBody = response.toString().getBytes("UTF-8");
			exchange.sendResponseHeaders(200, responseBody.length);
	        OutputStream os = exchange.getResponseBody();
	        os.write(responseBody);
	        os.flush();
	        os.close();
	        
	        Util.Log(Constant.LOG_LEVEL_INFO, "Send signup response : " + new String(responseBody, "UTF-8"), "SignupHandler");
		}
		catch (IOException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "SignupHandler");
		}
		catch (JSONException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "SignupHandler");
		}
	}

}
