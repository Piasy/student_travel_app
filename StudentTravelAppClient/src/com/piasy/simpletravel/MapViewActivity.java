package com.piasy.simpletravel;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapTouchListener;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.piasy.simpletravel.controller.Controller;
import com.piasy.simpletravel.model.Constant;
import com.piasy.simpletravel.util.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MapViewActivity extends Activity
{
	MapView map;
	Controller myController;
	int viewMapType;
	MapController mapController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mapview);
		myController = Controller.getController();
		myController.setActivityHandler(handler);
		
		map = (MapView) findViewById(R.id.mapView);
		mapController = map.getController();
		mapController.enableClick(true);
		map.setBuiltInZoomControls(true);
		map.getOverlays().clear();
		map.refresh();
		map.regMapViewListener(myController.getApp().mBMapManager, new MKMapViewListener()
		{
			@Override
			public void onMapMoveFinish() 
			{
				/**
				 * 在此处理地图移动完成回调
				 * 缩放，平移等操作完成后，此回调被触发
				 */
			}
			
			@Override
			public void onClickMapPoi(MapPoi mapPoiInfo) 
			{
				/**
				 * 在此处理底图poi点击事件
				 * 显示底图poi名称并移动至该点
				 * 设置过： mMapController.enableClick(true); 时，此回调才能被触发
				 * 
				 */
				if (mapPoiInfo != null)
				{
					Controller.makeToast(mapPoiInfo.strText);
					mapController.animateTo(mapPoiInfo.geoPt);
				}
				pop.hidePop();
			}

			@Override
			public void onGetCurrentMap(Bitmap b) 
			{
				/**
				 *  当调用过 mMapView.getCurrentMap()后，此回调会被触发
				 *  可在此保存截图至存储设备
				 */
			}

			@Override
			public void onMapAnimationFinish() 
			{
				/**
				 *  地图完成带动画的操作（如: animationTo()）后，此回调被触发
				 */
			}

			@Override
			public void onMapLoadFinish() 
			{
				Controller.makeToast("地图加载完成");
			}
		});
		
		map.regMapTouchListner(new MKMapTouchListener()
		{
			@Override
			public void onMapClick(GeoPoint point)
			{
			  if ( pop != null )
			  {
				  pop.hidePop();
			  }
			}

			@Override
			public void onMapDoubleClick(GeoPoint point) 
			{
				
			}

			@Override
			public void onMapLongClick(GeoPoint point) 
			{
				
			}
		});
		
		viewMapType = myController.getViewMapType();
		createPaopao();
		
		updateMap();
	}
	
	protected void updateMap()
	{
		switch (viewMapType)
		{
		case Constant.VIEW_POS_MAP:
			planedSpots = myController.getAroundSpots();
			updateLocationOverlay();
			updateSpotsOverlay();
			break;
		case Constant.VIEW_SPOTS_MAP:
			planedSpots = myController.getOneDayPlan();
			updateSpotsOverlay();
			break;
		default:
			break;
		}

		mapController.setZoom(getZoomLevel());
		mapController.setCenter(getCenterPoint());
	}
	
	LocalLocationOverlay locationOverlay = null;
	BDLocation location = null;
	LocationData locData = null;
	protected void updateLocationOverlay()
	{
		if (locationOverlay == null)
		{
			locationOverlay = new LocalLocationOverlay(map);
		}
		
		location = myController.getLocation();
		
		if (location != null)
		{
			locData = new LocationData();
			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();
			locData.accuracy = location.getRadius();
			locData.direction = location.getDerect();
			locationOverlay.setData(locData);
			locationOverlay.setMarker(null);
			locationOverlay.enableCompass();
			
			map.getOverlays().clear();
			map.getOverlays().add(locationOverlay);
			if (spotsOverlay != null)
			{
				map.getOverlays().add(spotsOverlay);
			}
			map.refresh();
		}
	}
	
	SpotsOverlay spotsOverlay = null;
	ArrayList<JSONObject> planedSpots = new ArrayList<JSONObject>();
	protected void updateSpotsOverlay()
	{
		System.out.println(planedSpots.size() + " spots");
		
		if (spotsOverlay == null)
		{
			spotsOverlay = new SpotsOverlay(getResources().getDrawable(R.drawable.icon_gcoding), map);
		}
		
		spotsOverlay.removeAll();
		map.refresh();
		
		synchronized (planedSpots)
		{
			for (int i = 0; i < planedSpots.size(); i ++)
			{
				JSONObject spot = planedSpots.get(i);
				try
				{
					OverlayItem item = new OverlayItem(new GeoPoint(spot.getInt("latitude"), spot.getInt("longitude")), 
							   spot.getString("name"), spot.getString("city"));
					item.setMarker(writeOnDrawable(R.drawable.icon_gcoding, i + 1));
					spotsOverlay.addItem(item);
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		map.getOverlays().clear();
		if (locationOverlay != null)
		{
			map.getOverlays().add(locationOverlay);
		}
		map.getOverlays().add(spotsOverlay);
		map.refresh();
	}
	
	View viewCache = null;
	protected void createPaopao()
	{
		pop = new PopupOverlay(map, new PopupClickListener() 
		{                  
	        @Override  
	        public void onClickedPopup(int index) 
	        {  
	        	switch (index)
				{
				case 0:
					System.out.println("click 0");
					synchronized (planedSpots)
					{
						System.out.println(curDetailNum + " -- " + planedSpots.size());
						if (0 <= curDetailNum && curDetailNum < planedSpots.size())
						{
							try
							{
								int detailType = planedSpots.get(curDetailNum).getInt("type");
								Intent intent;
								switch (detailType)
								{
								case Constant.LISTVIEW_ITEM_HOTEL:
								case Constant.LISTVIEW_ITEM_HOTEL_SEARCH:
									myController.seeHotelDetail(planedSpots.get(curDetailNum));
									intent = new Intent(MapViewActivity.this, HotelDetailActivity.class);
									startActivity(intent);
									break;
								case Constant.LISTVIEW_ITEM_SPOT:
								case Constant.LISTVIEW_ITEM_SPOT_SEARCH:
									myController.seeSpotDetail(planedSpots.get(curDetailNum));
									intent = new Intent(MapViewActivity.this, SpotDetailActivity.class);
									startActivity(intent);
									break;
								default:
									break;
								}
							}
							catch (JSONException e)
							{
								e.printStackTrace();
							}
						}
					}
					break;
				case 1:
					System.out.println("click 1");
					break;
				case 2:
					System.out.println("click 2");
					break;
				default:
					break;
				}
	        }  
		});
		
		viewCache = getLayoutInflater().inflate(R.layout.custom_text_view, null);
		popupText = (TextView) viewCache.findViewById(R.id.textcache);
		popupText.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				System.out
						.println("click pop");
			}
		});
	}
	
	protected BitmapDrawable writeOnDrawable(int drawableId, int num)
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

        return new BitmapDrawable(getResources(), bm);
    }
	
	int zoomLevel = -1;
	public int getZoomLevel()
	{
		if (zoomLevel == -1)
		{
			parseSpots();
		}
		return zoomLevel;
	}
	
	GeoPoint centerPoint = null;
	public GeoPoint getCenterPoint()
	{
		if (centerPoint == null)
		{
			parseSpots();
		}
		return centerPoint;
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
//			centerPoint = new GeoPoint(39916042, 116402544);
		}
		else
		{
			zoomLevel = (int) (3 + Math.log((double) Constant.ZOOM_LEVEL3_WIDTH / (double) width) / Math.log(2));
		}
		centerPoint = new GeoPoint((latitude_max + latitude_min) / 2,
				(longitude_max + longitude_min) / 2);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		myController.setActivityHandler(handler);
		
		System.out.println("SettingActivity.onResume()");
		
		if (myController.exiting())
		{
			System.out.println("SettingActivity.onResume()");
			finish();
		}
		else
		{
			map.onResume();
			updateMap();
		}
	}
	
	@Override
    protected void onPause() 
	{
        super.onPause();
		map.onPause();
    }
        
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    	map.destroy();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) 
    {
    	super.onSaveInstanceState(outState);
    	map.onSaveInstanceState(outState);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
    	super.onRestoreInstanceState(savedInstanceState);
    	map.onRestoreInstanceState(savedInstanceState);
    }
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Log.d(Constant.LOG_LEVEL_DEBUG, "get info at launch activity : " + (String)msg.obj);
		}
	};
	
	private PopupOverlay   pop  = null;//弹出泡泡图层，浏览节点时使用
	private TextView  popupText = null;//泡泡view
	class LocalLocationOverlay extends MyLocationOverlay
	{

		public LocalLocationOverlay(MapView arg0)
		{
			super(arg0);
		}
		
		@Override
  		protected boolean dispatchTap() 
  		{
  			popupText.setBackgroundResource(R.drawable.popup);
			popupText.setText("当前位置");
			mapController.animateTo(new GeoPoint((int)(locData.latitude * 1E6), (int)(locData.longitude * 1E6)));
			pop.showPopup(Util.getBitmapFromView(popupText),
					new GeoPoint((int)(locData.latitude * 1e6), (int)(locData.longitude * 1e6)),
					8);
  			return true;
  		}
	}
	
	OverlayItem mCurItem;
	int curDetailNum = -1;
	@SuppressWarnings("rawtypes")
	class SpotsOverlay extends ItemizedOverlay
	{

		public SpotsOverlay(Drawable arg0, MapView arg1)
		{
			super(arg0, arg1);
		}
		
		@Override
		public boolean onTap(int index)
		{
			curDetailNum = index;
			mCurItem = getItem(index);
			popupText.setText(mCurItem.getTitle());
			mapController.animateTo(mCurItem.getPoint());
			pop.showPopup(Util.getBitmapFromView(popupText), mCurItem.getPoint(), 48);
			return true;
		}
	}
}
