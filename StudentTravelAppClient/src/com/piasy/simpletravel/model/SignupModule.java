package com.piasy.simpletravel.model;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.piasy.simpletravel.controller.Controller;
import com.piasy.simpletravel.util.Util;

public class SignupModule
{
	String username, password;
	JSONObject info;
	public SignupModule(String username, String password, JSONObject info)
	{
		this.username = username;
		this.password = password;
		this.info = info;
	}
	
	boolean requested = false;
	public void signup()
	{
		if (!requested)
		{
			Thread signupThread = new Thread(signupRunnable);
			signupThread.start();
		}
	}
	
	Runnable signupRunnable = new Runnable()
	{
		
		@Override
		public void run()
		{
			requested = true;
			try
			{
				BasicHttpParams httpParams = new BasicHttpParams();  
		        HttpConnectionParams.setConnectionTimeout(httpParams, Constant.HTTPCLIENT_CON_TIMEOUT);  
		        HttpConnectionParams.setSoTimeout(httpParams, Constant.HTTPCLIENT_READ_TIMEOUT);
		        HttpProtocolParams.setUseExpectContinue(httpParams, false);
				HttpClient client = new DefaultHttpClient(httpParams);
				HttpHost host = new HttpHost(Setting.SERVER_IP, Setting.SERVER_PORT, "http");
				HttpPost post = new HttpPost("/signup");
				List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
				params.add(new BasicNameValuePair("username", username));  
				params.add(new BasicNameValuePair("password", 
						Util.RSAEncrypt(password))); 
				params.add(new BasicNameValuePair("info", info.toString()));
				post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
				HttpResponse response = client.execute(host, post);
				JSONObject responseInfo = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
				String result = responseInfo.getString("result");
				if (result.equals("success"))
				{
					Controller.getController().setSuccessResult(Constant.SIGNUP, responseInfo.getString("token"));
				}
				else if (result.equals("fail"))
				{
					Controller.getController().setFailResult(Constant.SIGNUP, responseInfo.getString("reason"));
				}
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
				Controller.getController().setFailResult(Constant.SIGNUP, "网络出错，请稍后重试");
			}
			catch (ClientProtocolException e)
			{
				e.printStackTrace();
				Controller.getController().setFailResult(Constant.SIGNUP, "网络出错，请稍后重试");
			}
			catch (IOException e)
			{
				e.printStackTrace();
				Controller.getController().setFailResult(Constant.SIGNUP, "网络出错，请稍后重试");
			}
			catch (JSONException e)
			{
				e.printStackTrace();
				Controller.getController().setFailResult(Constant.SIGNUP, "网络出错，请稍后重试");
			}
			requested = false;
		}
	};
}
