package com.piasy.simpletravel.model;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.piasy.simpletravel.controller.Controller;

public class LocationModule
{
	LocationClient locationClient;
	boolean aroudReqed = false;
	
	public LocationModule(LocationClient locationClient)
	{
		this.locationClient = locationClient;
		init();
		locationClient.requestLocation(); 
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
	
	public void reqLocation()
	{
		locationClient.requestLocation();
	}
	
	protected void init()
	{
		locationClient.registerLocationListener(new MyLocationListener());
		LocationClientOption option = new LocationClientOption();
		
//		//v 3.x
//		option.setOpenGps(true);
//		option.setAddrType("all");		//返回的定位结果包含地址信息
//		option.setCoorType("bd09ll");	//返回的定位结果是百度经纬度,默认值gcj02
//		option.setScanSpan(Constant.POS_REQ_TIMESTAMP);		//设置发起定位请求的间隔时间为300000 ms
//		option.disableCache(true);		//禁止启用缓存定位
//		option.setPoiNumber(5);    		//最多返回POI个数   
//		option.setPoiDistance(1000);	//poi查询距离        
//		option.setPoiExtraInfo(true); 	//是否需要POI的电话和地址等详细信息 
		
		
		//v 4.x
		option.setOpenGps(true);
		option.setLocationMode(LocationMode.Hight_Accuracy);
		option.setIsNeedAddress(true);
		option.setCoorType("bd09ll");	//返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(Constant.POS_REQ_TIMESTAMP);		//设置发起定位请求的间隔时间为300000 ms  
		locationClient.setLocOption(option);
		locationClient.start();
		System.out.println("LocationModule.init(): loc client start!");
	}
	
	class MyLocationListener implements BDLocationListener
	{
		@Override
		public void onReceiveLocation(BDLocation location)
		{
			System.out
					.println("LocationModule.MyLocationListener.onReceiveLocation(): receive loc");
			if (location == null)
			{
				System.out
						.println("LocationModule.MyLocationListener.onReceiveLocation(): null loc");
				return;
			}
			
			int locType = location.getLocType();
			if (!(locType == BDLocation.TypeGpsLocation || locType == BDLocation.TypeNetWorkLocation))
			{
				System.out
						.println("LocationModule.MyLocationListener.onReceiveLocation(): bad loc type");
				return;
			}
			mLocation = location;
			if (!aroudReqed)
			{
				Controller.getController().searchAroundSpots();
				System.out
						.println("LocationModule.MyLocationListener.onReceiveLocation(): search around");
				aroudReqed = true;
			}
			System.out
					.println("LocationModule.MyLocationListener.onReceiveLocation(): update loc");
		}
		
	}
}
