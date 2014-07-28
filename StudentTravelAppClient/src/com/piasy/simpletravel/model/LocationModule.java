package com.piasy.simpletravel.model;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.piasy.simpletravel.controller.Controller;

public class LocationModule
{
	LocationClient locationClient;
	
	public LocationModule(LocationClient locationClient)
	{
		this.locationClient = locationClient;
		System.out.println("ok 3");
		init();
		System.out.println("ok 8");
		locationClient.requestLocation(); 
		System.out.println("ok 9");
	}
	
	BDLocation mLocation = null;
	public BDLocation getLocation()
	{
		if (mLocation == null)
		{
			locationClient.requestLocation();
		}
		
		return mLocation;
	}
	
	protected void init()
	{
		System.out.println("ok 4");
		locationClient.registerLocationListener(new MyLocationListener());
		
		System.out.println("ok 5");
		LocationClientOption option = new LocationClientOption();
		
		//v 3.x
		option.setOpenGps(true);
		option.setAddrType("all");		//返回的定位结果包含地址信息
		option.setCoorType("bd09ll");	//返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(Constant.POS_REQ_TIMESTAMP);		//设置发起定位请求的间隔时间为300000 ms
		option.disableCache(true);		//禁止启用缓存定位
		option.setPoiNumber(5);    		//最多返回POI个数   
		option.setPoiDistance(1000);	//poi查询距离        
		option.setPoiExtraInfo(true); 	//是否需要POI的电话和地址等详细信息 
		
		
		//v 4.1
//		option.setOpenGps(true);
//		option.setLocationMode(LocationMode.Hight_Accuracy);
//		option.setIsNeedAddress(true);
//		option.setCoorType("bd09ll");	//返回的定位结果是百度经纬度,默认值gcj02
//		option.setScanSpan(Constant.POS_REQ_TIMESTAMP);		//设置发起定位请求的间隔时间为300000 ms  
		locationClient.setLocOption(option);
		System.out.println("ok 6");
		locationClient.start();
		System.out.println("ok 7");
	}
	
	class MyLocationListener implements BDLocationListener
	{

		@Override
		public void onReceivePoi(BDLocation arg0)
		{
			
		}
		
		@Override
		public void onReceiveLocation(BDLocation location)
		{
			if (location == null)
			{
				return;
			}
			
			int locType = location.getLocType();
			if (!(locType == BDLocation.TypeGpsLocation || locType == BDLocation.TypeNetWorkLocation))
			{
				return;
			}
			
//			locInfo.put("time", location.getTime());
//			locInfo.put("latitude", "" + location.getLatitude());
//			locInfo.put("longitude", "" + location.getLongitude());
//			String address = "我的位置";
//			if (locType == BDLocation.TypeNetWorkLocation)
//				address = location.getAddrStr();
//			locInfo.put("addr", address);
//			locInfo.put("accuracy", "" + location.getRadius());
//			locInfo.put("direction", "" + location.getDerect());
			
//			!fuck... null
//			synchronized (mLocation)
			{
				mLocation = location;
				Controller.getController().searchAroundSpots();
			}
		}
		
	}
}
