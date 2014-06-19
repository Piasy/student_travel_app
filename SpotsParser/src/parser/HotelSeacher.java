package parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class HotelSeacher
{
	public static void main(String[] args)
	{
		new HotelSeacher().run();
	}

	String hostUrl = "http://hotel.qunar.com";
	String toCity = "北京";
	String limit = "清华大学";
	String fromDate = "2014-02-21";
	String toDate = "2014-02-23";
	int pageNum = 1;
	protected void run()
	{
		String responseStr = null;
		try
		{
			String mixKey = getMixKey();
			String city = getCity();
			
			CloseableHttpClient httpClient = HttpClients.createDefault();
			
			String requestUrl = "/render/renderAPIList.jsp?attrs="
					+ "0FA456A3,L0F4L3C1,ZO1FcGJH,J6TkcChI,HCEm2cI6,"
					+ "08F7hM4i,8dksuR_,YRHLp-jc,pl6clDL0,HFn32cI6,"
					+ "vf_x4Gjt,2XkzJryU,vNfnYBK6,TDoolO-H,pk4QaDyF,"
					+ "x0oSHP6u,z4VVfNJo,5_VrVbqO,VAuXapLv,U1ur4rJN,"
					+ "px3FxFdF,xaSZV4wU,ZZY89LZZ,ZYCXZYHIRU,HGYGeXFY,"
					+ "ownT_WG6,0Ie44fNU,yYdMIL83,MMObDrW4,dDjWmcqr,"
					+ "Y0LTFGFh,6X7_yoo3,8F2RFLSO,U3rHP23d"
//					+ "&showAllCondition=1&showBrandInfo=1&showNonPrice=1"
//					+ "&showFullRoom=1&showPromotion=1&showTopHotel=1"
//					+ "&showGroupShop=1&needFP=1"
					+ "&output=json1.1&v=0.24676465662196279"
					+ "&requestTime=" + System.currentTimeMillis() 
					+ "&mixKey=" + mixKey
					+ "&requestor=RT_HSLIST&cityurl=" + city
					+ "&q=" + URLEncoder.encode(limit, "UTF-8") 
					+ "&fromDate=" + fromDate
					+ "&toDate=" + toDate
					+ "&limit=" + (pageNum * 15 - 15) + "%2C15"
					+ "&filterid=e758fc1d-61b3-4933-ae72-60b0ea27100b_C";
			
			HttpGet httpGet = new HttpGet(hostUrl + requestUrl);
			
			HttpResponse response = httpClient.execute(httpGet);
			
			responseStr = EntityUtils.toString(response.getEntity());
			JSONObject result = new JSONObject(responseStr);
			JSONArray hotels = result.getJSONArray("hotels");
			System.out.println(hotels.length() + " hotels");
			
			for (int i = 0; i < hotels.length(); i ++)
			{

				JSONObject hotel = hotels.getJSONObject(i);
				String open = hotel.getString("os");
				int price = hotel.getInt("price");
				String hotelId = hotel.getString("id");
				JSONObject attr = hotel.getJSONObject("attrs");
				String bpoint = attr.getString("bpoint");
				String address = attr.getString("hotelAddress");
				String name = attr.getString("hotelName");
				String score = attr.getString("CommentScore");
				String imageUrl = attr.getString("imageID");
				String oneSentence = attr.getString("oneSentence");
				
				System.out.println(open + ", " + name + ", " + address + ", " + oneSentence + ", " 
						+ hotelId + ", " + score + ", " + price + ", " + bpoint + ", " + imageUrl);
				
				httpGet.releaseConnection();
				
				Pattern pattern = Pattern.compile("([a-z_]*)([0-9]*)");
				Matcher matcher = pattern.matcher(hotelId);
				
				if (matcher.find())
				{
					requestUrl = "http://hotel.qunar.com/city/" + city + "/dt-" + matcher.group(2)
							+ "/?tag=" + city 
							+ "#fromDate=" + fromDate
							+ "&toDate=" + toDate
							+ "&q=" + URLEncoder.encode(limit, "UTF-8") 
							+ "&from=qunarHotel%7Cdiv"
							+ "&filterid=26f5e36e-7d14-421f-97cb-847367f174a3_C"
							+ "&showMap=0&qptype=poi&haspoi=1&QHFP=ZSS_A2AE3BF6&QHPR=1_2_1_0";
					
					System.out.println(requestUrl);
					httpGet = new HttpGet(requestUrl);
					response = httpClient.execute(httpGet);
					responseStr = EntityUtils.toString(response.getEntity());					
					Document doc = Jsoup.parse(responseStr);
					mixKey = doc.getElementById("eyKxim").text();
					System.out.println(mixKey);
					
					requestUrl = "/nprice/basicData.jsp?cityurl=" + city
							+ "&HotelSEQ=" + hotelId
							+ "&mixKey=" + mixKey;
					
					httpGet = new HttpGet(hostUrl + requestUrl);
					response = httpClient.execute(httpGet);
					
					System.out.println(EntityUtils.toString(response.getEntity()));
					
					requestUrl = "/render/detailV2.jsp?fromDate=" + fromDate
							+ "&toDate=" + toDate
							+ "&cityurl=" + city
							+ "&HotelSEQ=" + hotelId
							+ "&mixKey=" + mixKey
							+ "&v=" + System.currentTimeMillis() 
							+ "&cn=1"
							+ "&roomId=&filterid=86418ee7-94e6-4888-a346-62ab737c1d18_A"
							+ "&QUFP=ZSS_A594D175&QUCP=ZSD_A5E31B53&lastupdate=-1"
							+ "&basicData=1&requestID=c0a8e978-m3c2i-1gnmv&output=json1.1";
					
					httpGet = new HttpGet(hostUrl + requestUrl);
					response = httpClient.execute(httpGet);
					responseStr = EntityUtils.toString(response.getEntity());
					
					JSONObject detail = new JSONObject(responseStr.substring(1, responseStr.length() - 1)).getJSONObject("result");
					
					Iterator<?> it = detail.keys();
					int choiceCount = 0;
					if (it.hasNext())
					{
						String key = (String) it.next();
						JSONArray value = detail.getJSONArray(key);
						int roomPrice = value.getInt(0);
						String roomType = value.getString(3);
						String bookUrl = hostUrl + value.getString(4);
						String company = value.getString(5);
						int availa = value.getInt(9);
						
						System.out.println(company + ", " + roomType + ", " + roomPrice
								+ ", " + availa + ", " + bookUrl);
						choiceCount ++;
					}
					
					while (it.hasNext())
					{
						it.next();
						choiceCount ++;
					}
					System.out.println(choiceCount + " choices");
				}
			}
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (JSONException e)
		{
			System.out.println(responseStr);
			e.printStackTrace();
		}
	}
	
	protected String getMixKey()
	{
		String ret = "";
		try
		{
			Document doc = Jsoup.connect("http://hotel.qunar.com/city/beijing_city").get();
			Element mixKey = doc.getElementById("eyKxim");
			ret = mixKey.text();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
				
		return ret;
	}
	
	protected String getCity()
	{
		String city = "";
		try 
		{
			RedirectStrategy redirectStrategy = new RedirectStrategy()
			{
				
				@Override
				public boolean isRedirected(HttpRequest arg0, HttpResponse arg1,
						HttpContext arg2) throws ProtocolException
				{
					return false;
				}
				
				@Override
				public HttpUriRequest getRedirect(HttpRequest arg0, HttpResponse arg1,
						HttpContext arg2) throws ProtocolException
				{
					return null;
				}
			};
			
			CloseableHttpClient httpClient = HttpClients.custom()
			        .setRedirectStrategy(redirectStrategy)
			        .build();
			
			String searchUrl = "/search.jsp?toCity=" + URLEncoder.encode(toCity, "UTF-8")
					+ "&q=" + URLEncoder.encode(limit, "UTF-8")
					+ "&fromDate=" + fromDate
					+ "&toDate=" + toDate
					+ "&from=qunarHotel%7Cdiv&QHFP=ZSI0A3DE22A3";
			
			HttpGet httpget = new HttpGet(hostUrl + searchUrl);

            System.out.println("Executing request " + httpget.getRequestLine());
            
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() 
            {

                public String handleResponse(HttpResponse response) 
                {
                    int status = response.getStatusLine().getStatusCode();
                    
                    if (status == 301) 
                    {
                    	String location = response.getFirstHeader("Location").getValue();
    					String city = location.substring(location.indexOf("/") + 1, 
    							location.indexOf("/", location.indexOf("/") + 1));
    					return city;
                    } 
                    
                    return "";
                }

            };
            city = httpClient.execute(httpget, responseHandler);
            httpClient.close();
        }
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return city;
	}
}
