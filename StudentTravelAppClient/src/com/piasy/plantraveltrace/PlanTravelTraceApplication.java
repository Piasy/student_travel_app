package com.piasy.plantraveltrace;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.piasy.plantraveltrace.controller.Controller;
import com.piasy.plantraveltrace.model.Constant;

public class PlanTravelTraceApplication extends Application
{
    private static PlanTravelTraceApplication mInstance = null;
    public boolean m_bKeyRight = true;
    public BMapManager mBMapManager = null;
    
	@SuppressLint("HandlerLeak")
	@Override
    public void onCreate() 
	{
		Log.i(Constant.LOG_LEVEL_INFO, "app oncreate");
	    super.onCreate();
	    mInstance = this;
	    
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
	}
	
	public void setBMapManager(BMapManager mBMapManager)
	{
		this.mBMapManager = mBMapManager;		
	}

	public static PlanTravelTraceApplication getInstance() 
	{
		return mInstance;
	}
	
	@Override
	public void onTerminate()
	{
		Log.d(Constant.LOG_LEVEL_DEBUG, "app onDestroy");
		super.onTerminate();
	}
    
    @SuppressLint("HandlerLeak")
	Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Log.d(Constant.LOG_LEVEL_DEBUG, "get info at search activity : " + (String)msg.obj);
		}
	};
}
