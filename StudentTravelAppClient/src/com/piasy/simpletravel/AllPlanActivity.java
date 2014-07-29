package com.piasy.simpletravel;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;

import com.baidu.location.BDLocation;
import com.piasy.simpletravel.controller.Controller;
import com.piasy.simpletravel.model.Constant;

public class AllPlanActivity extends Activity
{
	ListView allPlanListView;
	MyListViewAdapter adapter;
	Button travelInfoButton, planTableButton, settingButton;
	Button dateSelectButton, viewMapButton;
	
	int longClickPos = 0, clickPos = 0;
	int mYear, mMonth, mDay;
	boolean inited = false;
	
	Controller myController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		myController = Controller.getController();
		myController.setActivityHandler(handler);
//		myController.setPlanDays(2);
				
		travelInfoButton = (Button) findViewById(R.id.travelInfoInMain);
		planTableButton = (Button) findViewById(R.id.planTableInMain);
		settingButton = (Button) findViewById(R.id.SettingInMain);
		dateSelectButton = (Button) findViewById(R.id.dataSelectButton);
		viewMapButton = (Button) findViewById(R.id.viewMapInMain);
		allPlanListView = (ListView) findViewById(R.id.allPlanListview);
		adapter = new MyListViewAdapter(this, Constant.ACTIVITY_ALLPLAN);
		allPlanListView.setAdapter(adapter);

		settingButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				if (!inited)
				{
					initUI();
				}
				
				Intent settingIntent = new Intent(AllPlanActivity.this, SettingActivity.class);
				startActivity(settingIntent);
			}
		});
		
		if (myController.verified())
		{
			initUI();
			updateUI();
		}
		else
		{
			Controller.makeToast("身份认证失败，请重新登录");
		}
	}
	
	protected void initUI()
	{
		travelInfoButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent travelInfoIntent = new Intent(AllPlanActivity.this, SearchActivity.class);
				startActivity(travelInfoIntent);
			}
		});
		
		planTableButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				
			}
		});
		
		
		dateSelectButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Calendar cur = Calendar.getInstance();
				new DatePickerDialog(AllPlanActivity.this, new DatePickerDialog.OnDateSetListener()
				{
					
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear,
							int dayOfMonth)
					{
						mYear = year;
						mMonth = monthOfYear;
						mDay = dayOfMonth;
					}
				}, cur.get(Calendar.YEAR), cur.get(Calendar.MONTH), cur.get(Calendar.DAY_OF_MONTH)).show();
			}
		});
		
		viewMapButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
//				myController.searchAroundSpots();
//				BDLocation location = myController.getLocation();
//				if (location == null || myController.getAroundSpots().size() == 0)
//				{
//					Controller.makeToast("正在努力定位中，先看看别的吧~");
//				}
//				else
//				{
//					myController.viewMap(Constant.VIEW_POS_MAP);
//					Intent intent = new Intent(AllPlanActivity.this, MapViewActivity.class);
//					startActivity(intent);
//				}
			}
		});
		
		allPlanListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				if (position == adapter.getCount() - 1)
				{
					Calendar cur = Calendar.getInstance();
					DatePickerDialog dpDialog = new DatePickerDialog
					(AllPlanActivity.this, new DatePickerDialog.OnDateSetListener()
					{
						
						@Override
						public void onDateSet(DatePicker view, int year, int monthOfYear,
								int dayOfMonth)
						{
							mYear = year;
							mMonth = monthOfYear;
							mDay = dayOfMonth;
							myController.setPlanStartDate(year, monthOfYear, dayOfMonth);
							
							Intent travelInfoIntent = new Intent(AllPlanActivity.this, SearchActivity.class);
							startActivity(travelInfoIntent);
						}
					}, cur.get(Calendar.YEAR), cur.get(Calendar.MONTH), cur.get(Calendar.DAY_OF_MONTH));
					dpDialog.setTitle("请选择行程开始日期");
					dpDialog.show();
				}
				else
				{
					myController.gotoDay(position);
					Intent singledayPlanIntent = new Intent(AllPlanActivity.this, SingledayPlanActivity.class);
					startActivity(singledayPlanIntent);
				}
			}
		});
		allPlanListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				longClickPos = position;
				return false;
			}
		});
		allPlanListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener()
		{
			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo)
			{
				menu.add(0, 0, 0, "删除计划");
			}
		});
		
		inited = true;
	}
	
	protected void updateUI()
	{
//		ArrayList<ArrayList<JSONObject>> allPlans = myController.getPlan();
////		System.out.println("ok2");
////		System.out.println("" + (allPlans == null));
//		if (allPlans.size() != 0)
//		{
////			JSONObject date = Util.date2Json(myController.getPlanDate());
//			adapter.clear();
//			for (int i = 0; i < allPlans.size(); i ++)
//			{
//				JSONObject plan = new JSONObject();
//				try
//				{
//					plan.put("type", Constant.LISTVIEW_ITEM_PLAN);
//					plan.put("status", Constant.PLAN_STATUS_PLAN);
//					String spots = "";
//					for (int j = 0; j < allPlans.get(i).size(); j ++)
//					{
//						spots += allPlans.get(i).get(j).getString("name");
//						
//						if (j < allPlans.get(i).size() - 1)
//						{
//							spots += "，";
//						}
//					}
//					
//					Calendar date = myController.getPlanDate();
//					plan.put("spots", spots);
//					plan.put("year", date.get(Calendar.YEAR));
//					plan.put("month", date.get(Calendar.MONTH));
//					plan.put("day", date.get(Calendar.DAY_OF_MONTH) + i);
//					plan.put("weekday", (date.get(Calendar.DAY_OF_WEEK) + i) % 7);
//					
//					adapter.addItem(plan);
//				}
//				catch (JSONException e)
//				{
//					if (e.getMessage() == null)
//		        	{
//						Log.e(Constant.LOG_LEVEL_ERROR, "AllPlanActivity updateUI : JSONException");
//		        	}
//		        	else
//		        	{
//		        		Log.e(Constant.LOG_LEVEL_ERROR, "AllPlanActivity updateUI : " + e.getMessage());
//					}
//				}
//			}
//			
//			adapter.notifyDataSetChanged();
//		}
//		else
//		{
//			Controller.makeToast("行程表还是空的，赶快创建一个吧~");
//		}
	}
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Log.d(Constant.LOG_LEVEL_DEBUG, "get info at search activity : " + (String)msg.obj);
		}
	};
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case 0:
			break;
		case 1:
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		myController.setActivityHandler(handler);

		System.out.println("AllPlanActivity.onResume() " + myController.exiting());
		
		if (myController.exiting())
		{
			System.out.println("AllPlanActivity.onResume()");
			finish();
		}
		else
		{
			if (myController.verified())
			{
				if (!inited)
				{
					initUI();
				}
				updateUI();
				System.out.println("ok!");
			}
			else
			{
				Controller.makeToast("身份认证失败，请重新登录");
			}
		}
	}
}
