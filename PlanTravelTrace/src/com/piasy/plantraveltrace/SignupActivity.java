package com.piasy.plantraveltrace;

import org.json.JSONException;
import org.json.JSONObject;

import com.piasy.plantraveltrace.controller.Controller;
import com.piasy.plantraveltrace.model.Constant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SignupActivity extends Activity
{
	EditText mailEdit, passEdit, nameEdit;
	Button signupButton;
	String mail, password, name;
	Controller myController;
	ProgressDialog dialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup_one);
		
		myController = Controller.getController();
		myController.setActivityHandler(handler);
		
		mailEdit = (EditText) findViewById(R.id.mailEdit);
		passEdit = (EditText) findViewById(R.id.passwordEdit);
		nameEdit = (EditText) findViewById(R.id.nameEdit);
		signupButton = (Button) findViewById(R.id.signupButton);
		
		signupButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				mail = mailEdit.getText().toString();
				password = passEdit.getText().toString();
				name = nameEdit.getText().toString();
				
				if (mail.equals(""))
				{
					Controller.makeToast("邮箱不能为空");
					return;
				}
				if (password.equals("") || password.length() < 6)
				{
					Controller.makeToast("密码长度不能小于六位");
					return;
				}
				if (name.equals(""))
				{
					Controller.makeToast("昵称不能为空");
					return;
				}
				
				JSONObject info = new JSONObject();
				try
				{
					info.put("name", name);
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				
				myController.signup(mail, password, info);
				
				dialog = new ProgressDialog(SignupActivity.this);
				dialog.setMessage("正在注册，请稍候...");
				dialog.setIndeterminate(false);
				dialog.setCancelable(true);
				dialog.show();
			}
		});
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
				
				if (type.equals("signup"))
				{
					if (dialog != null)
					{
						dialog.cancel();
					}
					
					if (info.getString("result").equals("success"))
					{
						Intent data = new Intent();
						data.putExtra("username", mail);
						data.putExtra("password", password);
						setResult(Constant.SIGNUP_SUCCESS, data);
						finish();
					}
					else
					{
						Controller.makeToast(info.getString("reason"));
					}
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
		
		System.out.println("SignupActivity.onResume()");
		
		if (myController.exiting())
		{
			System.out.println("SignupActivity.onResume()");
			finish();
		}
	}
}
