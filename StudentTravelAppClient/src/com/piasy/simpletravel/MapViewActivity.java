package com.piasy.simpletravel;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfigeration;
import com.baidu.mapapi.map.MyLocationConfigeration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.piasy.simpletravel.controller.Controller;
import com.piasy.simpletravel.model.Constant;

public class MapViewActivity extends Activity
{
	MapView mMapView;
	Controller myController;
	int viewMapType;
	BaiduMap mBaiduMap;
	BitmapDescriptor mCurrentMarker;
	boolean isFirstLoc = true;
	PopMenu popMenu = null;
	Marker curSpot = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mapview);
		myController = Controller.getController();
		myController.setActivityHandler(handler);
		viewMapType = myController.getViewMapType();
		
		mMapView = (MapView) findViewById(R.id.mapView);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMyLocationEnabled(true);
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfigeration(
				LocationMode.NORMAL, true, null));
		
		popMenu = new PopMenu(this, Constant.POPUP_VIEW_INMAP);
		popMenu.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				popMenu.dismiss();
				if (curSpot != null)
				{
					switch (position)
					{
					case 0:
						break;
					case 1:
						myController.seeSpotDetail(curSpot.getExtraInfo());
						Intent spotdetailIntent = new Intent(MapViewActivity.this, SpotDetailActivity.class);
						startActivity(spotdetailIntent);
						break;
					default:
						break;
					}
				}
			}
		});
		
		mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback()
		{
			
			@Override
			public void onMapLoaded()
			{
				updateMap();
			}
		});
		
		mBaiduMap.setOnMyLocationClickListener(new BaiduMap.OnMyLocationClickListener()
		{
			
			@Override
			public boolean onMyLocationClick()
			{
				Controller.makeToast("当前位置：" + location.getAddrStr());
				return true;
			}
		});
		
		mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener()
		{
			
			@Override
			public boolean onMarkerClick(Marker marker)
			{
//				Controller.makeToast(marker.getTitle());
				Bundle desc = marker.getExtraInfo();
				LatLng pos = new LatLng(desc.getDouble("latitude"), desc.getDouble("longitude"));
				MapStatusUpdate uc = MapStatusUpdateFactory.newLatLng(pos);
				mBaiduMap.animateMapStatus(uc);
				
				popMenu.clearItems();
				String [] items = {marker.getTitle(), "详情"};
				popMenu.addItems(items);
				curSpot = marker;
				popMenu.showAsDropDown(findViewById(R.id.spotDetailOptions));
				return false;
			}
		});
	}
	

	int zoomLevel = -1;
	LatLng centerPT = new LatLng(39.914271, 116.403119);
	protected void updateMap()
	{
		switch (viewMapType)
		{
		case Constant.VIEW_POS_MAP:
			planedSpots = myController.getAroundSpots();
			updateLocationOverlay();
			updateSpotsOverlay();
			mBaiduMap.setMyLocationData(locData);
			
			if (isFirstLoc) 
			{
				isFirstLoc = false;
				MapStatusUpdate ucz = MapStatusUpdateFactory
						.newLatLngZoom(getCenterPoint(), getZoomLevel());
				mBaiduMap.animateMapStatus(ucz);
				System.out.println("MapViewActivity.updateMap()");
			}
			break;
//		case Constant.VIEW_SPOTS_MAP:
//			planedSpots = myController.getOneDayPlan();
//			updateSpotsOverlay();
//			break;
		default:
			break;
		}
	}
	
	BDLocation location = null;
	MyLocationData locData = null;
	protected void updateLocationOverlay()
	{
		location = myController.getLocation();
		if (location != null)
		{
			System.out.println("MapViewActivity.updateLocationOverlay(): loc: ");
			System.out.println(location.getCity() + ", addr: " + location.getAddrStr()
					+ ", radirs: " + location.getRadius() + ", dir: " + location.getDirection()
					+ ", long: " + location.getLongitude() + ", lat: " + location.getLatitude()
					+ ", prov: " + location.getProvince() + ", street: " + location.getStreet()
					+ ", streetNO: " + location.getStreetNumber());
			locData = new MyLocationData.Builder()
				.accuracy(location.getRadius())
				.direction(location.getDirection())
				//.direction(100)
				.latitude(location.getLatitude())
				.longitude(location.getLongitude()).build();
			centerPT = new LatLng(location.getLatitude(), location.getLongitude());
		}
	}
	
	ArrayList<JSONObject> planedSpots = new ArrayList<JSONObject>();
	protected void updateSpotsOverlay()
	{
		System.out.println(planedSpots.size() + " spots");
		
		mBaiduMap.clear();
		
		synchronized (planedSpots)
		{
			for (int i = 0; i < planedSpots.size(); i ++)
			{
				JSONObject spot = planedSpots.get(i);
				try
				{
					LatLng pos = new LatLng(((double) spot.getInt("latitude")) / 1e6, 
											((double) spot.getInt("longitude")) / 1e6);
					BitmapDescriptor icon = BitmapDescriptorFactory
							.fromBitmap(writeOnDrawable(R.drawable.icon_gcoding, i + 1));
					Bundle desc = new Bundle();
					desc.putString("name", spot.getString("name"));
					desc.putString("city", spot.getString("city"));
					desc.putString("ticket", spot.getJSONObject("ticket").toString());
					desc.putString("opentime", spot.getString("opentime"));
					desc.putString("suggesttime", spot.getString("suggesttime"));
					desc.putString("intro", spot.getString("intro"));
					desc.putString("tip", spot.getJSONObject("tip").toString());
					desc.putFloat("score", (float) spot.getDouble("score"));
					desc.putString("photo", spot.getString("photo"));
					desc.putDouble("latitude", (double) spot.getInt("latitude") / 1e6);
					desc.putDouble("longitude", (double) spot.getInt("longitude") / 1e6);
					desc.putString("address", spot.getString("address"));
					desc.putString("phone", spot.getString("phone"));
					desc.putString("eval", spot.getJSONArray("eval").toString());
					
					OverlayOptions one = new MarkerOptions().position(pos).icon(icon)
							.title(spot.getString("name")).extraInfo(desc);
					
					mBaiduMap.addOverlay(one);
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	protected Bitmap writeOnDrawable(int drawableId, int num)
	{

        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);
        
        Paint paint = new Paint(); 
        paint.setStyle(Style.FILL);  
        paint.setColor(Color.WHITE); 
        paint.setTextSize(20); 

        Canvas canvas = new Canvas(bm);
        int width;
        if (num < 10)
        {
        	width = bm.getWidth() / 3;
        }
        else if (num < 100)
        {
        	width = bm.getWidth() / 4;
		}
        else
        {
        	width = bm.getWidth() / 6;
		}
        canvas.drawText("" + num, width, bm.getHeight() / 2, paint);

        return bm;
    }
	
	public int getZoomLevel()
	{
		if (zoomLevel == -1)
		{
			parseSpots();
		}
		return zoomLevel;
	}
	
	public LatLng getCenterPoint()
	{
		return centerPT;
	}
	
	int longitude_min = 181000000, longitude_max = -181000000, latitude_min = 91000000, latitude_max = -91000000;
	protected void parseSpots()
	{
		synchronized (planedSpots)
		{
			for (int i = 0; i < planedSpots.size(); i ++)
			{
				JSONObject spot = planedSpots.get(i);
				int longtude, latitude;
				try
				{
					longtude = spot.getInt("longitude");
					latitude = spot.getInt("latitude");
					if (longtude < longitude_min)
					{
						longitude_min = longtude;
					}
					if (longitude_max < longtude)
					{
						longitude_max = longtude;
					}
					if (latitude < latitude_min)
					{
						latitude_min = latitude;
					}
					if (latitude_max < latitude)
					{
						latitude_max = latitude;
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		if (viewMapType == Constant.VIEW_POS_MAP)
		{
			int curLongitude = -1, curLatitude = -1;
			if (locData != null)
			{
				curLongitude = (int) (locData.longitude * 1e6);
				curLatitude = (int) (locData.latitude * 1e6);
				if (curLongitude < longitude_min)
				{
					longitude_min = curLongitude;
				}
				if (longitude_max < curLongitude)
				{
					longitude_max = curLongitude;
				}
				if (curLatitude < latitude_min)
				{
					latitude_min = curLatitude;
				}
				if (latitude_max < curLatitude)
				{
					latitude_max = curLatitude;
				}
			}
		}

		int width;
		if (longitude_max - longitude_min < latitude_max - latitude_min)
		{
			width = latitude_max - latitude_min;
		}
		else
		{
			width = longitude_max - longitude_min;
		}
		
		if (width <= 3000)
		{
			zoomLevel = 13;
		}
		else
		{
			zoomLevel = (int) (3 + Math.log((double) Constant.ZOOM_LEVEL3_WIDTH / (double) width) / Math.log(2));
		}
		centerPT = new LatLng(((double) (latitude_max + latitude_min)) / (2 * 1e6), 
							  ((double) (longitude_max + longitude_min)) / (2 * 1e6));
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		myController.setActivityHandler(handler);

		System.out.println("MapViewActivity.onResume()");
		
		if (myController.exiting())
		{
			finish();
		}
		else
		{
			mMapView.onResume();
			if (!isFirstLoc)
			{
				updateMap();
			}
		}
	}
	
	@Override
    protected void onPause() 
	{
        super.onPause();
        mMapView.onPause();
        System.out.println("MapViewActivity.onPause()");
    }
        
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		System.out.println("MapViewActivity.onDestroy()");
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
