package com.piasy.simpletravel;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.piasy.simpletravel.controller.Controller;
import com.piasy.simpletravel.model.Constant;

public class HotelDetailActivity extends Activity
{
	Button travelInfoButton, planTableButton, settingButton;
	Button addHotelButton;
	TextView titleTextView;
	WebView hotelWebView;
	
	Controller myController;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hotel_detail);
		
		myController = Controller.getController();
		travelInfoButton = (Button) findViewById(R.id.travelInfoInHotelDetail);
		planTableButton = (Button) findViewById(R.id.planTableInHotelDetail);
		settingButton = (Button) findViewById(R.id.SettingInHotelDetail);
		addHotelButton = (Button) findViewById(R.id.addHotelButton);
		titleTextView = (TextView) findViewById(R.id.titleInHotelDetail);
		hotelWebView = (WebView) findViewById(R.id.hotelDetailWeb);
		
		hotelWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		WebSettings webSettings = hotelWebView.getSettings();       
		webSettings.setJavaScriptEnabled(true);  
		hotelWebView.setWebViewClient(new WebViewClient()
		{       
			public boolean shouldOverrideUrlLoading(WebView view, String url) 
			{       
				view.loadUrl(url); 
				System.out
						.println("loading " + url);
				return true;       
			}       
		}); 
		
		final JSONObject hotel = myController.getHotelDetail();
		
		try
		{
			String hotelName = hotel.getJSONObject("attrs").getString("hotelName");
			if (Constant.MAX_TITLE_LEN < hotelName.length())
			{
				hotelName = hotelName.substring(0, Constant.MAX_TITLE_LEN) + "...";
			}
			titleTextView.setText(hotelName);
			
			addHotelButton.setOnClickListener(new View.OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					if (myController.setHotel(hotel))
					{
						Controller.makeToast("添加成功");
					}
					else
					{
						Controller.makeToast("添加失败");
					}
				}
			});
			
			travelInfoButton.setOnClickListener(new View.OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					Intent travelInfoIntent = new Intent(HotelDetailActivity.this, SearchActivity.class);
					startActivity(travelInfoIntent);
				}
			});
			
			planTableButton.setOnClickListener(new View.OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					Intent planTableIntent = new Intent(HotelDetailActivity.this, AllPlanActivity.class);
					startActivity(planTableIntent);
				}
			});
			
			settingButton.setOnClickListener(new View.OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					Intent settingIntent = new Intent(HotelDetailActivity.this, SettingActivity.class);
					startActivity(settingIntent);
				}
			});
			
			String detailUrl = "http://touch.qunar.com/h5/hotel/hoteldetail"
					+ "?seq=" + hotel.getString("id")
					+ "&checkInDate=" + myController.getFromDate()
					+ "&checkOutDate=" + myController.getToDate()
					+ "&bd_source=3w_hotel";
//					"http://hotel.qunar.com/city/"
//					+ hotel.getString("citystr") + "/dt-" + matcher.group(2)
//					+ "/?tag=" + hotel.getString("citystr") 
//					+ "#fromDate=" + myController.getFromDate()
//					+ "&toDate=" + myController.getToDate()
//					+ "&q=" + URLEncoder.encode(myController.getLimit(), "UTF-8")
//					+ "&from=qunarHotel%7Cdiv&filterid=26f5e36e-7d14-421f-97cb-847367f174a3_C"
//					+ "&showMap=0&qptype=poi&haspoi=1&QHFP=ZSS_A2AE3BF6&QHPR=1_2_1_0";

			hotelWebView.loadUrl(detailUrl);
			System.out.println("loading : " + detailUrl);
		}
		catch (JSONException e)
		{
			if (e.getMessage() == null)
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "HotelDetailActivity onCreate : JSONException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "HotelDetailActivity onCreate : " + e.getMessage());
			}
			
			e.printStackTrace();
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{       
        if ((keyCode == KeyEvent.KEYCODE_BACK) && hotelWebView.canGoBack()) 
        {       
        	hotelWebView.goBack();       
            return true;       
        }       
        return super.onKeyDown(keyCode, event);       
    }

	@Override
	protected void onResume()
	{
		super.onResume();
		myController.setActivityHandler(handler);
		
		System.out.println("HotelDetailActivity.onResume()");
		
		if (myController.exiting())
		{
			System.out.println("HotelDetailActivity.onResume()");
			finish();
		}
	}
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Log.d(Constant.LOG_LEVEL_DEBUG, "get info at launch activity : " + (String)msg.obj);
		}
	};
}
