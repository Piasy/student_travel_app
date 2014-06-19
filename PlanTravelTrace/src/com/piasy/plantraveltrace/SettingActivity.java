package com.piasy.plantraveltrace;

import java.io.File;
import java.math.BigDecimal;

import org.json.JSONException;
import org.json.JSONObject;

import com.piasy.plantraveltrace.controller.Controller;
import com.piasy.plantraveltrace.model.Constant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingActivity extends Activity
{
	Controller myController;
	TextView name, cacheSize, curVersion;
	LinearLayout modifyInfo, clearCache, reportTo, checkUpdate, aboutUs;
	Button signout;
	Button travelInfo, planTable, setting;
	JSONObject info;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		myController = Controller.getController();
		myController.setActivityHandler(handler);
		
		name = (TextView) findViewById(R.id.nameInSetting);
		cacheSize = (TextView) findViewById(R.id.cacheSize);
		curVersion = (TextView) findViewById(R.id.currentVersion);
		modifyInfo = (LinearLayout) findViewById(R.id.modifyInfo);
		clearCache = (LinearLayout) findViewById(R.id.clearCache);
		reportTo = (LinearLayout) findViewById(R.id.reportTo);
		checkUpdate = (LinearLayout) findViewById(R.id.checkUpdate);
		aboutUs = (LinearLayout) findViewById(R.id.aboutUs);
		signout = (Button) findViewById(R.id.signoutButton);
		travelInfo = (Button) findViewById(R.id.travelInfoInSetting);
		planTable = (Button) findViewById(R.id.planTableInSetting);
		setting = (Button) findViewById(R.id.SettingInSetting);
		
		info = myController.getUserInfo();
		try
		{
			name.setText(info.getString("name"));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			name.setText("暂无");
		}
		
		long size = getCacheSize();
		if (size < 1024)
		{
			cacheSize.setText("1 KB");
		}
		else if (size < 1024 * 1024)
		{
			cacheSize.setText((size / 1024) + " KB");
		}
		else if (size < 1024 * 1024 * 1024)
		{
			float f1 = new BigDecimal((double) size / (double) (1024 * 1024))
				.setScale(2, BigDecimal.ROUND_HALF_UP)
				.floatValue();
			cacheSize.setText(f1 + " MB");
		}
		else
		{
			float f1 = new BigDecimal((double) size / (double) (1024 * 1024 * 1024))
				.setScale(2, BigDecimal.ROUND_HALF_UP)
				.floatValue();
			cacheSize.setText(f1 + " GB");
		}
		
		curVersion.setText("当前版本：" + myController.getCurVer());
		
		modifyInfo.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				
			}
		});
		
		clearCache.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				
			}
		});
		
		reportTo.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				
			}
		});
		
		checkUpdate.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				
			}
		});
		
		aboutUs.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				
			}
		});
		
		signout.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				SharedPreferences pref = getSharedPreferences(Constant.APP_PREF_NAME, 0);
				SharedPreferences.Editor editor = pref.edit();
				editor.putString("password", "NONE");
				editor.putBoolean("remember", false);
				editor.commit();
				
				Intent intent = new Intent(SettingActivity.this, LaunchActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		travelInfo.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(SettingActivity.this, SearchActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		planTable.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(SettingActivity.this, AllPlanActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		setting.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				
			}
		});
	}
	
	protected long getCacheSize()
	{
		long size = 0;
		File [] files = new File(Constant.APP_CACHE_DIR).listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				size += file.length();
			}
		}
		
		return size;
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
