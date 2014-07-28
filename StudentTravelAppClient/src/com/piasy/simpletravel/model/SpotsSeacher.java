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

/**
 * Get spots info using some conditions
 * */
public class SpotsSeacher
{
	boolean requested = false;
	
	String keyword, token, username;
	int searchType = Constant.SEARCH_BY_KEYWORD;
	int pagenum;
	public boolean search(String username, String token, String keyword, int type, int pagenum)
	{
		boolean ret = false;
		if (!requested)
		{
			requested = true;
			this.username = username;
			this.token = token;
			this.keyword = keyword;
			this.searchType = type;
			this.pagenum = pagenum;
			Thread searchThread = new Thread(searchRunnable);
			searchThread.start();
			
			ret = true;
		}
		return ret;
	}
	
	Runnable searchRunnable = new Runnable()
	{
		
		@Override
		public void run()
		{
			try
			{
				BasicHttpParams httpParams = new BasicHttpParams();  
		        HttpConnectionParams.setConnectionTimeout(httpParams, Constant.HTTPCLIENT_CON_TIMEOUT);  
		        HttpConnectionParams.setSoTimeout(httpParams, Constant.HTTPCLIENT_READ_TIMEOUT);
				HttpProtocolParams.setUseExpectContinue(httpParams, false);
				HttpClient client = new DefaultHttpClient(httpParams);

				HttpHost host = new HttpHost(Setting.SERVER_IP, Setting.SERVER_PORT, "http");
				HttpPost post = new HttpPost("/query");
				List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
				params.add(new BasicNameValuePair("username", username));
				params.add(new BasicNameValuePair("token", token));
				params.add(new BasicNameValuePair("keyword", keyword));
				params.add(new BasicNameValuePair("searchtype", "" + searchType));
				params.add(new BasicNameValuePair("pagenum", "" + pagenum));
				post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
				HttpResponse response = client.execute(host, post);
				JSONObject responseInfo = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
				String result = responseInfo.getString("result");
				if (result.equals("success"))
				{
					JSONArray spots = responseInfo.getJSONArray("spots");
					for (int i = 0; i < spots.length(); i ++)
					{
						String photoUrlString = spots.getJSONObject(i).getString("photo");
						File photo = new File(Constant.APP_CACHE_DIR + "/"
								+ URLEncoder.encode(photoUrlString, "UTF-8"));
						if (!photo.exists())
						{
							URL photoUrl = new URL(photoUrlString);
							HttpURLConnection conn = (HttpURLConnection) photoUrl.openConnection();
							conn.setConnectTimeout(6000);
							conn.setDoInput(true);
							conn.setUseCaches(true);
							InputStream is = conn.getInputStream();
							Bitmap bitmap = BitmapFactory.decodeStream(is);
							is.close();
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(photo));
							bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
							bos.flush();
							bos.close();
						}
						spots.getJSONObject(i).put("photo", photo.getAbsolutePath());
						spots.getJSONObject(i).put("type", Constant.LISTVIEW_ITEM_SPOT_SEARCH);
					}
					
					switch (searchType)
					{
					case Constant.SEARCH_BY_KEYWORD:
						Controller.getController().setSuccessResult(Constant.QUERY, spots.toString());
						break;
					case Constant.SEARCH_BY_CITY:
						Controller.getController().setSuccessResult(Constant.AROUND_SPOTS, spots.toString());
						break;
					default:
						break;
					}
				}
				else if (result.equals("fail"))
				{
					Controller.getController().setFailResult(Constant.QUERY, responseInfo.getString("reason"));
				}
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
				Controller.getController().setFailResult(Constant.QUERY, "网络出错，请稍后重试");
			}
			catch (ClientProtocolException e)
			{
				e.printStackTrace();
				Controller.getController().setFailResult(Constant.QUERY, "网络出错，请稍后重试");
			}
			catch (IOException e)
			{
				e.printStackTrace();
				Controller.getController().setFailResult(Constant.QUERY, "网络出错，请稍后重试");
			}
			catch (JSONException e)
			{
				e.printStackTrace();
				Controller.getController().setFailResult(Constant.QUERY, "网络出错，请稍后重试");
			}
			requested = false;
		}
	};
}
