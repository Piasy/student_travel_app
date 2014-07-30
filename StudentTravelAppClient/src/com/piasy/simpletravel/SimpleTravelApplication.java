package com.piasy.simpletravel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.piasy.simpletravel.controller.Controller;
import com.piasy.simpletravel.model.Constant;

public class SimpleTravelApplication extends Application
{
    private static SimpleTravelApplication mInstance = null;
	private SDKReceiver mReceiver;
	private LocationClient locationClient = null;
    
	@SuppressLint("HandlerLeak")
	@Override
    public void onCreate() 
	{
		Log.i(Constant.LOG_LEVEL_INFO, "app oncreate");
	    super.onCreate();
	    mInstance = this;
	    
	    SDKInitializer.initialize(this);
	    Controller myController = Controller.getController();
		myController.setAPP(mInstance);
	    Handler toastHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				Toast.makeText(mInstance, (String) msg.obj, Toast.LENGTH_LONG).show();
			}
		};
		Controller.setToastHandler(toastHandler);
		myController.setActivityHandler(handler);
		
		//register SDK broadcast receiver
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
		mReceiver = new SDKReceiver();
		registerReceiver(mReceiver, iFilter);
		
		locationClient = new LocationClient(this);
		myController.setLocationClient(locationClient);
	}
	
	public static SimpleTravelApplication getInstance() 
	{
		return mInstance;
	}
		
	//TODO will be called?
	@Override
	public void onTerminate()
	{
		Log.i(Constant.LOG_LEVEL_DEBUG, "app onDestroy");
		super.onTerminate();
		unregisterReceiver(mReceiver);
	}
    
    @SuppressLint("HandlerLeak")
	Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Log.d(Constant.LOG_LEVEL_DEBUG, "get info at search activity : " + (String)msg.obj);
		}
	};
	
	/**
	 * Broadcast receiver, listening for SDK key error, and Internet error
	 */
	public class SDKReceiver extends BroadcastReceiver 
	{
		public void onReceive(Context context, Intent intent) 
		{
			String s = intent.getAction();
			Log.d(Constant.LOG_LEVEL_DEBUG, "action: " + s);
			if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) 
			{
				Toast.makeText(mInstance, "key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置",
						Toast.LENGTH_LONG).show();
			} 
			else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) 
			{
				Toast.makeText(mInstance, "网络出错", Toast.LENGTH_LONG).show();
			}
		}
	}
}
