package com.piasy.simpletravel.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.piasy.simpletravel.controller.Controller;
import com.piasy.simpletravel.util.Util;

public class VerifyModule
{
	String username, password;
	public VerifyModule(String username, String password)
	{
		this.username = username;
		this.password = password;
	}
	
	boolean requested = false;
	public void verify()
	{
		if (!requested)
		{
			Thread verifyThread = new Thread(verifyRunnable);
			verifyThread.start();
		}
	}
	
	Runnable verifyRunnable = new Runnable()
	{
		
		@Override
		public void run()
		{
			requested = true;
			try
			{
				System.out.println("ok 03");
				BasicHttpParams httpParams = new BasicHttpParams();  
		        HttpConnectionParams.setConnectionTimeout(httpParams, Constant.HTTPCLIENT_CON_TIMEOUT);  
		        HttpConnectionParams.setSoTimeout(httpParams, Constant.HTTPCLIENT_READ_TIMEOUT);
		        HttpProtocolParams.setUseExpectContinue(httpParams, false);
				HttpClient client = new DefaultHttpClient(httpParams);
				HttpHost host = new HttpHost(Setting.SERVER_IP, Setting.SERVER_PORT, "http");
				HttpPost post = new HttpPost("/verify");
				List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
				params.add(new BasicNameValuePair("username", username));
				long timestamp = System.currentTimeMillis();
				params.add(new BasicNameValuePair("timestamp", Long.toString(timestamp)));
				params.add(new BasicNameValuePair("password", 
						Util.getSHA1Value(Util.getSHA1Value(password) + Long.toHexString(timestamp)))); 
				post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
				System.out.println("ok 04");
				HttpResponse response = client.execute(host, post);
				System.out.println("ok 05");
				JSONObject responseInfo = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
				String result = responseInfo.getString("result");
				if (result.equals("success"))
				{
					Controller.getController().setSuccessResult(Constant.VERIFY, responseInfo.getString("token"));
					Controller.getController().setCommitted(responseInfo.getJSONArray("committed"));
					System.out.println("ok 06");
					Controller.getController().setUserInfo(responseInfo.getJSONObject("info"));
					System.out.println("ok 07");
					
					JSONArray recspots = responseInfo.getJSONArray("recspots");
					for (int i = 0; i < recspots.length(); i ++)
					{
						String photoUrlString = recspots.getJSONObject(i).getString("photo");
						URL photoUrl = new URL(photoUrlString);
						HttpURLConnection conn = (HttpURLConnection) photoUrl.openConnection();
						conn.setConnectTimeout(6000);
						conn.setDoInput(true);
						conn.setUseCaches(true);
						InputStream is = conn.getInputStream();
						Bitmap bitmap = BitmapFactory.decodeStream(is);
						is.close();
						File photo = new File(Constant.APP_CACHE_DIR + "/"
								+ URLEncoder.encode(photoUrl.getFile(), "UTF-8"));
						if (!photo.exists())
						{
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(photo));
							bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
							bos.flush();
							bos.close();
						}
						recspots.getJSONObject(i).put("photo", photo.getAbsolutePath());
					}
					Controller.getController().setRecSpots(recspots);
					System.out.println("ok 08");
				}
				else if (result.equals("fail"))
				{
					Controller.getController().setFailResult(Constant.VERIFY, responseInfo.getString("reason"));
				}
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
				Controller.getController().setFailResult(Constant.VERIFY, "网络出错，请稍后重试");
			}
			catch (ClientProtocolException e)
			{
				e.printStackTrace();
				Controller.getController().setFailResult(Constant.VERIFY, "网络出错，请稍后重试");
			}
			catch (IOException e)
			{
				e.printStackTrace();
				Controller.getController().setFailResult(Constant.VERIFY, "网络出错，请稍后重试");
			}
			catch (JSONException e)
			{
				e.printStackTrace();
				Controller.getController().setFailResult(Constant.VERIFY, "网络出错，请稍后重试");
			}

			System.out.println("ok !!");
			requested = false;
		}
	};
}
