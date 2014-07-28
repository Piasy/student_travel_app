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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.piasy.simpletravel.controller.Controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class HotelSeacher
{
	String hostUrl = "http://hotel.qunar.com";
	String toCity;// = "北京";
	String limit;// = "清华大学";
	String fromDate;// = "2014-02-21";
	String toDate;// = "2014-02-23";
	int pageNum = 0;
	String imageBaseUrl = "http://userimg.qunar.com/imgs/";
	String imageEndUrl = "240.jpg";
	
	boolean requested = false;
	public boolean search(String toCity, String limit, String fromDate, String toDate, int pageNum)
	{
		boolean ret = false;
		if (!requested)
		{
			this.toCity = toCity;
			this.limit = limit;
			this.fromDate = fromDate;
			this.toDate = toDate;
			this.pageNum = pageNum;
			Thread searchThread = new Thread(searchRunnable);
			searchThread.start();
			ret = true;
		}
		
		return ret;
	}
	
	public void run()
	{
		Thread searchThread = new Thread(searchRunnable);
		searchThread.start();
	}
	
	Runnable searchRunnable = new Runnable()
	{
		
		@Override
		public void run()
		{
			requested = true;
			String responseStr = null;
			try
			{
				String searchUrl = "/search.jsp?toCity=" + URLEncoder.encode(toCity, "UTF-8")
						+ "&q=" + URLEncoder.encode(limit, "UTF-8")
						+ "&fromDate=" + fromDate
						+ "&toDate=" + toDate
						+ "&from=qunarHotel%7Cdiv&QHFP=ZSI0A3DE22A3";
				
				BasicHttpParams httpParams = new BasicHttpParams();  
		        HttpConnectionParams.setConnectionTimeout(httpParams, Constant.HTTPCLIENT_CON_TIMEOUT);  
		        HttpConnectionParams.setSoTimeout(httpParams, Constant.HTTPCLIENT_READ_TIMEOUT);
		        HttpProtocolParams.setUseExpectContinue(httpParams, false);
				HttpClient client = new DefaultHttpClient(httpParams);
				HttpClientParams.setCookiePolicy(client.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);
				client.getParams().setParameter("http.useragent", 
						"Mozilla/5.0 (Windows NT 6.3; WOW64) "
						+ "AppleWebKit/537.36 (KHTML, like Gecko) "
						+ "Chrome/30.0.1599.101 Safari/537.36");
				
				HttpGet get = new HttpGet(hostUrl + searchUrl);
				get.getParams().setParameter("http.protocol.handle-redirects", false);
				
				HttpResponse response = client.execute(get);
				
				
				if (response.getStatusLine().getStatusCode() == 301)
				{
					String location = response.getFirstHeader("Location").getValue();
					String city = location.substring(location.indexOf("/") + 1, 
							location.indexOf("/", location.indexOf("/") + 1));
					System.out.println(city);
					
					get.abort();
					get = new HttpGet("http://hotel.qunar.com/city/beijing_city");
					response = client.execute(get);
					
					responseStr = EntityUtils.toString(response.getEntity());					
					Document doc = Jsoup.parse(responseStr);
					Element mixKey = doc.getElementById("eyKxim");
					System.out.println(mixKey.text());
					
					String requestUrl = "/render/renderAPIList.jsp?attrs="
							+ "0FA456A3,L0F4L3C1,ZO1FcGJH,J6TkcChI,HCEm2cI6,"
							+ "08F7hM4i,8dksuR_,YRHLp-jc,pl6clDL0,HFn32cI6,"
							+ "vf_x4Gjt,2XkzJryU,vNfnYBK6,TDoolO-H,pk4QaDyF,"
							+ "x0oSHP6u,z4VVfNJo,5_VrVbqO,VAuXapLv,U1ur4rJN,"
							+ "px3FxFdF,xaSZV4wU,ZZY89LZZ,ZYCXZYHIRU,HGYGeXFY,"
							+ "ownT_WG6,0Ie44fNU,yYdMIL83,MMObDrW4,dDjWmcqr,"
							+ "Y0LTFGFh,6X7_yoo3,8F2RFLSO,U3rHP23d"
//							+ "&showAllCondition=1&showBrandInfo=1&showNonPrice=1"
//							+ "&showFullRoom=1&showPromotion=1&showTopHotel=1"
//							+ "&showGroupShop=1&needFP=1"
							+ "&output=json1.1&v=0.24676465662196279"
							+ "&requestTime=" + System.currentTimeMillis() 
							+ "&mixKey=" + mixKey.text()
							+ "&requestor=RT_HSLIST&cityurl=" + city
							+ "&q=" + URLEncoder.encode(limit, "UTF-8") 
							+ "&fromDate=" + fromDate
							+ "&toDate=" + toDate
							+ "&limit=" + (pageNum * 15) + "%2C15"
							+ "&filterid=e758fc1d-61b3-4933-ae72-60b0ea27100b_C";
					
//					System.out.println(hostUrl + requestUrl);
					
					get.abort();
					get = new HttpGet(hostUrl + requestUrl);
					response = client.execute(get);
					
					responseStr = EntityUtils.toString(response.getEntity());		
					get.abort();
					
					JSONObject result = new JSONObject(responseStr);
					JSONArray hotels = result.getJSONArray("hotels");
					
					for (int i = 0; i < hotels.length(); i ++)
					{
						System.out.println(hotels.getJSONObject(i).toString());
						String imageUri = hotels.getJSONObject(i)
								.getJSONObject("attrs").getString("imageUri");
						String photoUrlString;
						if (imageUri == null || imageUri.length() < 5)
						{
							hotels.getJSONObject(i).getJSONObject("attrs").put("imageID", Constant.NO_PHOTO);
						}
						else
						{
							photoUrlString = imageBaseUrl + imageUri + imageEndUrl;
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
							hotels.getJSONObject(i).getJSONObject("attrs").put("imageID", photo.getAbsolutePath());
						}
						hotels.getJSONObject(i).put("citystr", city);
					}
					System.out.println(hotels.length() + " hotels");
					Controller.getController().setHotelResult(hotels);
//					
//					
//					JSONObject hotel = hotels.getJSONObject(0);
//					String open = hotel.getString("os");
//					int price = hotel.getInt("price");
//					String hotelId = hotel.getString("id");
//					JSONObject attr = hotel.getJSONObject("attrs");
//					String bpoint = attr.getString("bpoint");
//					String address = attr.getString("hotelAddress");
//					String name = attr.getString("hotelName");
//					String score = attr.getString("CommentScore");
//					String imageUrl = attr.getString("imageID");
//					String oneSentence = attr.getString("oneSentence");
//					
//					System.out.println(open + ", " + name + ", " + address + ", " + oneSentence + ", " 
//							+ hotelId + ", " + score + ", " + price + ", " + bpoint + ", " + imageUrl);
//								
				}
				
			}
			catch (UnsupportedEncodingException e)
			{
				if (e.getMessage() == null)
	        	{
	        		Log.e(Constant.LOG_LEVEL_ERROR, "searchRunnable : UnsupportedEncodingException");
	        	}
	        	else
	        	{
	        		Log.e(Constant.LOG_LEVEL_ERROR, "searchRunnable : " + e.getMessage());
				}
				
				e.printStackTrace();
			}
			catch (ClientProtocolException e)
			{
				if (e.getMessage() == null)
	        	{
	        		Log.e(Constant.LOG_LEVEL_ERROR, "searchRunnable : ClientProtocolException");
	        	}
	        	else
	        	{
	        		Log.e(Constant.LOG_LEVEL_ERROR, "searchRunnable : " + e.getMessage());
				}
				
				e.printStackTrace();
			}
			catch (IOException e)
			{
				if (e.getMessage() == null)
	        	{
	        		Log.e(Constant.LOG_LEVEL_ERROR, "searchRunnable : IOException");
	        	}
	        	else
	        	{
	        		Log.e(Constant.LOG_LEVEL_ERROR, "searchRunnable : " + e.getMessage());
				}
				
				e.printStackTrace();
			}
			catch (JSONException e)
			{
				System.out
						.println(responseStr);
				if (e.getMessage() == null)
	        	{
	        		Log.e(Constant.LOG_LEVEL_ERROR, "searchRunnable : JSONException");
	        	}
	        	else
	        	{
	        		Log.e(Constant.LOG_LEVEL_ERROR, "searchRunnable : " + e.getMessage());
				}
				
				e.printStackTrace();
			}
			
			requested = false;
		}
	};
}


//Pattern pattern = Pattern.compile("([a-z_]*)([0-9]*)");
//Matcher matcher = pattern.matcher(hotelId);
//
////System.out.println(matcher.find());
//if (matcher.find())
//{
//	System.out.println(matcher.group(0) + ", " + matcher.group(1) + ", " + matcher.group(2));
//	requestUrl = "http://hotel.qunar.com/city/" + city + "/dt-" + matcher.group(2)
//			+ "/?tag=" + city;
//	
//	System.out.println(requestUrl);
//	get = new HttpGet(requestUrl);
//	response = client.execute(get);
//	responseStr = EntityUtils.toString(response.getEntity());					
//	doc = Jsoup.parse(responseStr);
//	mixKey = doc.getElementById("eyKxim");
//	System.out.println(mixKey.text());
//	
//	requestUrl = "/nprice/basicData.jsp?cityurl=" + city
//			+ "&HotelSEQ=" + hotelId
//			+ "&mixKey=" + mixKey.text();
//	get.abort();
//	get = new HttpGet(hostUrl + requestUrl);
//	
//	String detailUrl = "/render/detailV2.jsp?fromDate=" + fromDate
//			+ "&toDate=" + toDate
//			+ "&cityurl=" + city
//			+ "&HotelSEQ=" + hotelId
//			+ "&mixKey=" + mixKey.text()
//			+ "&v=" + System.currentTimeMillis() 
//			+ "&cn=1"
//			+ "&roomId=&filterid=86418ee7-94e6-4888-a346-62ab737c1d18_A"
//			+ "&QUFP=ZSS_A594D175&QUCP=ZSD_A5E31B53&lastupdate=-1"
//			+ "&basicData=1&requestID=c0a8e978-m3c2i-1gnmv&output=json1.1";
//	System.out.println(hostUrl + detailUrl);
//	
//	get.abort();
//	get = new HttpGet(hostUrl + detailUrl);
////	get.setHeader("Cookie", cookie);
//	response = client.execute(get);
//	
////	cookies = ((AbstractHttpClient) client).getCookieStore().getCookies();   
////	for (int i = 0; i < cookies.size(); i ++)
////	{
////		System.out.println(cookies.get(i));
////	}
//	
//	responseStr = EntityUtils.toString(response.getEntity());
//	
//	JSONObject detail = new JSONObject(responseStr.substring(1, responseStr.length() - 1)).getJSONObject("result");
//	
//	Iterator<?> it = detail.keys();
//	int choiceCount = 0;
//	if (it.hasNext())
//	{
//		String key = (String) it.next();
//		JSONArray value = detail.getJSONArray(key);
//		int roomPrice = value.getInt(0);
//		String roomType = value.getString(3);
//		String bookUrl = hostUrl + value.getString(4);
//		String company = value.getString(5);
//		int availa = value.getInt(9);
//		
//		System.out.println(company + ", " + roomType + ", " + roomPrice
//				+ ", " + availa + ", " + bookUrl);
//		choiceCount ++;
//	}
//	
//	while (it.hasNext())
//	{
//		it.next();
//		choiceCount ++;
//	}
//	System.out.println(choiceCount + " choices");
//}