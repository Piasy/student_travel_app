package com.piasy.simpletravel;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

//import com.baidu.mapapi.BMapManager;
//import com.baidu.mapapi.MKGeneralListener;
//import com.baidu.mapapi.map.MKEvent;
import com.piasy.simpletravel.controller.Controller;
import com.piasy.simpletravel.dao.DBManager;
import com.piasy.simpletravel.model.Constant;
import com.piasy.simpletravel.util.Util;

public class LaunchActivity extends Activity
{
//    BMapManager mBMapManager = null;
    DBManager dbManager;
    Controller myController;
	EditText usernameEdit, passwordEdit;
	CheckBox rememberMe;
	TextView forgetPass, signupAccount;
	Button signinButton;
	String username = "INVALID";
	String password = "INVALID";
	boolean remember = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i(Constant.LOG_LEVEL_INFO, "Launch activity oncreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

    	myController = Controller.getController();
    	Thread initThread = new Thread(initRunnable);
		initThread.start();
//		if (initEngineManager(getApplicationContext()))
//		{
//			Thread initThread = new Thread(initRunnable);
//			initThread.start();
//		}
	}


	Runnable initRunnable = new Runnable()
	{
		
		@Override
		public void run()
		{
			Looper.prepare();

		    AssetManager assetManager = getAssets();
			try
			{
				InputStream is = assetManager.open("id_rsa.pub");

			    if (Util.init(is))
			    {
					DBManager dbManager = new DBManager(getApplicationContext());
					myController.setDBManager(dbManager);
					myController.setActivityHandler(handler);
					
					SharedPreferences pref = getSharedPreferences(Constant.APP_PREF_NAME, 0);
					username = pref.getString("username", "INVALID");
					password = pref.getString("password", "NONE");
					remember = pref.getBoolean("remember", false);
					
					if (username.equals("INVALID") || password.length() < 6)
					{
						Message msg = Message.obtain();
						JSONObject info = new JSONObject();
						try
						{
							info.put("type", "init");
							msg.obj = info.toString();
							synchronized (handler)
							{
								handler.sendMessage(msg);
							}
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
						
					}
					else
					{
						myController.verify(username, password);
					}
			    }
			    else
			    {
			    	Controller.makeToast("应用程序初始化错误");
					System.exit(0);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			Looper.loop();
		}
	};
	
//	public boolean initEngineManager(Context context) 
//	{
//		boolean ret = false;
//		
//        if (locationClient == null)
//        {
////        	locationClient = new LocationClient(this);
//        }
//        ret = true;
//        
//        return ret;
//	}
	
	// normal exception listener, such as auth error, network error
//    public static class MyGeneralListener implements MKGeneralListener 
//    {
//        
//        @Override
//        public void onGetNetworkState(int iError) 
//        {
//            if (iError == MKEvent.ERROR_NETWORK_CONNECT) 
//            {
//            	Toast.makeText(SimpleTravelApplication.getInstance().getApplicationContext(), "你的网络出错啦！",
//                        Toast.LENGTH_LONG).show();
//            }
//            else if (iError == MKEvent.ERROR_NETWORK_DATA) 
//            {
//            	Toast.makeText(SimpleTravelApplication.getInstance().getApplicationContext(), "输入正确的检索条件！",
//                        Toast.LENGTH_LONG).show();
//            }
//            // ...
//        }
//
//        @Override
//        public void onGetPermissionState(int iError)
//        {
//            if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) 
//            {
//                //auth Key error
//                Toast.makeText(SimpleTravelApplication.getInstance().getApplicationContext(), 
//                        "授权错误，请从合法途径获取本软件！", Toast.LENGTH_LONG).show();
//                SimpleTravelApplication.getInstance().m_bKeyRight = false;
//            }
//        }
//    }
	
	
	
	ProgressDialog dialog = null;
	protected void initUI()
	{
		setContentView(R.layout.activity_launch);
		
		usernameEdit = (EditText) findViewById(R.id.usernameEdit);
		passwordEdit = (EditText) findViewById(R.id.passwordEdit);
		rememberMe = (CheckBox) findViewById(R.id.remenberMe);
		signinButton = (Button) findViewById(R.id.signinButton);
		
		forgetPass = (TextView) findViewById(R.id.forgetPassword);
		signupAccount = (TextView) findViewById(R.id.signupAccount);

		rememberMe.setChecked(remember);
		if (!username.equals("INVALID"))
		{
			usernameEdit.setText(username);
			
			if (remember)
			{
				passwordEdit.setText(password);
			}
		}
		
		rememberMe.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener()
		{
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				remember = isChecked;
			}
		});
		
		signinButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				if (usernameEdit.getText().toString().equals(""))
				{
					Controller.makeToast("邮箱不能为空");
					return;
				}
				
				if (passwordEdit.getText().toString().equals(""))
				{
					Controller.makeToast("密码不能为空");
					return;
				}
				username = usernameEdit.getText().toString();
				password = passwordEdit.getText().toString();
				
				//salt hash
				password = Util.getSHA1Value(Util.getSHA1Value(username) + Util.getSHA1Value(password));
				
				verify();
			}
		});
		
		forgetPass.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				
			}
		});
		
		signupAccount.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent signupIntent = new Intent(LaunchActivity.this, SignupActivity.class);
				startActivityForResult(signupIntent, 100);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == Constant.SIGNUP_SUCCESS)
		{
			username = data.getExtras().getString("username");
			password = data.getExtras().getString("password");
			remember = true;
			verify();
		}
	}
	
	protected void verify()
	{
		myController.verify(username, password);
						
		dialog = new ProgressDialog(LaunchActivity.this);
		dialog.setMessage("正在登录，请稍候...");
		dialog.setIndeterminate(false);
		dialog.setCancelable(true);
		dialog.show();
	}
	
    @SuppressLint("HandlerLeak")
	Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Log.d(Constant.LOG_LEVEL_DEBUG, "get info at launch activity : " + (String)msg.obj);
			
			try
			{
				JSONObject info = new JSONObject((String)msg.obj);
				
				String type = info.getString("type");
				
				if (type.equals("verify"))
				{
					if (dialog != null)
					{
						dialog.cancel();
					}
					
					if (info.getString("result").equals("success"))
					{
						if (remember)
						{
							SharedPreferences pref = getSharedPreferences(Constant.APP_PREF_NAME, 0);
							SharedPreferences.Editor editor = pref.edit();
							editor.putString("username", username);
							editor.putString("password", password);
							editor.putBoolean("remember", remember);
							editor.commit();
						}
						Intent allPlanIntent = new Intent(LaunchActivity.this, AllPlanActivity.class);
						finish();
						startActivity(allPlanIntent);
					}
					else
					{
						if (!password.equals("NONE"))
						{
							Controller.makeToast(info.getString("reason"));
						}
						initUI();
					}
				}
				else if (type.equals("init"))
				{
					initUI();
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
	};

	@Override
	protected void onResume()
	{
		super.onResume();
		myController.setActivityHandler(handler);
		
		System.out.println("LaunchActivity.onResume()");
		
		if (myController.exiting())
		{
			System.out.println("LaunchActivity.onResume()");
			finish();
		}
	}
	
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {  
        if (keyCode == KeyEvent.KEYCODE_BACK )  
        {  
        	System.out.println("exit ...");
//            finish();
//        	android.os.Process.killProcess(android.os.Process.myPid());
        	System.out.println("before " + myController.exiting());
        	myController.exit();
        	System.out.println("after " + myController.exiting());
        	finish();
        }  
          
        return false;  
          
    }  
}
