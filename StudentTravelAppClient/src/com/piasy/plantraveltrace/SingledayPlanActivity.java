package com.piasy.plantraveltrace;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.ListView;
import android.widget.TextView;

import com.piasy.plantraveltrace.controller.Controller;
import com.piasy.plantraveltrace.model.Constant;
import com.piasy.plantraveltrace.model.RouteEntry;
import com.piasy.plantraveltrace.util.Util;

public class SingledayPlanActivity extends Activity
{
	TextView titleTextView;
	Button backToAllButton, viewMapButton;
	Button settingButton, travelInfoButton, planTableButton;
	ListView planShowListView;
	MyListViewAdapter adapter;
	ProgressDialog progressDialog;
	
	Controller myController;
	
	int longClickPos = 0, clickPos = 0;
	boolean inited = false;
			
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_singledayplan);
		myController = Controller.getController();
		myController.setActivityHandler(handler);

		planShowListView = (ListView) findViewById(R.id.planShowListView);
		titleTextView = (TextView) findViewById(R.id.titleInSingleDay);
		backToAllButton = (Button) findViewById(R.id.backToAllButton);
		viewMapButton = (Button) findViewById(R.id.viewMapInSingleDay);
		settingButton = (Button) findViewById(R.id.SettingInSingleDay);
		travelInfoButton = (Button) findViewById(R.id.travelInfoInSingleDay);
		planTableButton = (Button) findViewById(R.id.planTableInSingleDay);
		
		adapter = new MyListViewAdapter(this, Constant.ACTIVITY_SINGLEDAY);
		planShowListView.setAdapter(adapter);
		
		settingButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				if (!inited)
				{
					initUI();
				}				

				Intent settingIntent = new Intent(SingledayPlanActivity.this, SettingActivity.class);
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
		titleTextView.setText(myController.getDayTitle());
		
		backToAllButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		
		viewMapButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				myController.viewMap(Constant.VIEW_SPOTS_MAP);
				Intent intent = new Intent(SingledayPlanActivity.this, MapViewActivity.class);
				startActivity(intent);
			}
		});
		
		travelInfoButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent travelInfoIntent = new Intent(SingledayPlanActivity.this, SearchActivity.class);
				startActivity(travelInfoIntent);
			}
		});
		
		planTableButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				HashMap<String, RouteEntry> onedayRoute = myController.getOneDayRoutes();
				if (onedayRoute == null)
				{
					if (myController.genPlan())
					{
						progressDialog = new ProgressDialog(SingledayPlanActivity.this);
						progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						progressDialog.setTitle("提示");
						progressDialog.setMessage("正在努力地生成路线安排...");
						progressDialog.setIcon(R.drawable.gen_plan_down);
						progressDialog.setProgress(100);
						progressDialog.setIndeterminate(false);
						progressDialog.setCancelable(true);
						progressDialog.show();
										
						Thread checkThread = new Thread(checkProgressRunnable);
						checkThread.start();
					}
					else
					{
						Controller.makeToast("您还没有预定酒店呢，赶紧预定一个去吧~");
					}
				}
			}
		});

		planShowListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				System.out
						.println("click : " + position);
				if (adapter.getItemType(position) == Constant.LISTVIEW_ITEM_TRAFFIC)
				{
					adapter.switchTrafficDetail(position);
					adapter.notifyDataSetChanged();
				}
				else if (adapter.getItemType(position) == Constant.LISTVIEW_ITEM_SPOT)
				{
					JSONObject spot = (JSONObject) adapter.getItem(position);
					myController.seeSpotDetail(spot);
					
					Intent spotDetailInfoIntent = new Intent(SingledayPlanActivity.this, SpotDetailActivity.class);
					startActivity(spotDetailInfoIntent);
				}
			}
		});
		
		planShowListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				longClickPos = position;
				boolean ret = true;
				switch (adapter.getItemType(position))
				{
				case Constant.LISTVIEW_ITEM_SPOT:
					ret = false;
					break;
				case Constant.LISTVIEW_ITEM_TRAFFIC:
					ret = true;
					break;
				default:
					ret = true;
					break;
				}
				
				return ret;
			}
		});
		
		planShowListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener()
		{
			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo)
			{
				menu.add(0, 0, 0, "上移");
				menu.add(0, 1, 0, "下移");
				menu.add(0, 2, 0, "调整时间");
				menu.add(0, 3, 0, "调整日期");
			}
		});
		
	}
	
	protected void updateUI()
	{
		ArrayList<JSONObject> planedSpots = myController.getOneDayPlan();
		HashMap<String, RouteEntry> onedayRoute = myController.getOneDayRoutes();
		
		adapter.clear();
		
		for (int i = 0; i < planedSpots.size(); i ++)
		{
			JSONObject spot = planedSpots.get(i);
			try
			{
				if (spot.getInt("type") == Constant.LISTVIEW_ITEM_HOTEL_SEARCH)
				{
					spot.put("type", Constant.LISTVIEW_ITEM_HOTEL);
				}
				else if (spot.getInt("type") == Constant.LISTVIEW_ITEM_SPOT_SEARCH)
				{
					spot.put("type", Constant.LISTVIEW_ITEM_SPOT);
				}
				
				adapter.addItem(spot);
			}
			catch (JSONException e)
			{
				if (e.getMessage() == null)
	        	{
					Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity updateUI : JSONException");
	        	}
	        	else
	        	{
	        		Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity updateUI : " + e.getMessage());
				}
			}
		}
		
		if (onedayRoute != null)
		{
			ArrayList<JSONObject> planItems = adapter.getItems();
			synchronized (planItems)
			{
				for (int i = 0; i < planItems.size() - 1; i +=2)
				{
					try
					{
						String id = planItems.get(i).getString("name") + "||" 
								+ planItems.get(i + 1).getString("name");
						
						planItems.add(i + 1, Util.routePlan2Json(onedayRoute.get(id).route));
					}
					catch (JSONException e)
					{
						if (e.getMessage() == null)
			        	{
							Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity checkProgressRunnable : JSONException");
			        	}
			        	else
			        	{
			        		Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity checkProgressRunnable : " + e.getMessage());
						}
					}
				}
				
				try
				{
					String id = planItems.get(planItems.size() - 1).getString("name") + "||" 
							+ planItems.get(0).getString("name");
					planItems.add(planItems.size(), Util.routePlan2Json(onedayRoute.get(id).route));
				}
				catch (JSONException e)
				{
					if (e.getMessage() == null)
		        	{
						Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity checkProgressRunnable : JSONException");
		        	}
		        	else
		        	{
		        		Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity checkProgressRunnable : " + e.getMessage());
					}
				}
			}
			adapter.setItems(planItems);
		}
		
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		myController.setActivityHandler(handler);
		
		System.out.println("SingledayPlanActivity.onResume()");
		
		if (myController.exiting())
		{
			System.out.println("SingledayPlanActivity.onResume()");
			finish();
		}
		else
		{
			adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case 0:
			//TODO add adjust interface
//			synchronized (planItems)
//			{
//				planItems.add((longClickPos - 1 < 0) ? 0 : (longClickPos - 1), 
//						planItems.remove(longClickPos));
//			}
			break;
		case 1:
//			synchronized (planItems)
//			{
//				planItems.add((planItems.size() < longClickPos + 1) 
//						? planItems.size() : (longClickPos + 1), 
//						planItems.remove(longClickPos));
//			}
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Log.d(Constant.LOG_LEVEL_DEBUG, "get info at search activity : " + (String)msg.obj);
			try
			{
				JSONObject info = new JSONObject((String)msg.obj);
				
				String type = info.getString("type");
				if (type.equals("genplan"))
				{
					if (info.getString("result").equals("success"))
					{
						adapter.notifyDataSetChanged();
						progressDialog.cancel();
					}
				}
			}
			catch (JSONException e)
			{
				if (e.getMessage() == null)
	        	{
					Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity handler : JSONException");
	        	}
	        	else
	        	{
	        		Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity handler : " + e.getMessage());
				}
			}
		}
	};
	
	Runnable checkProgressRunnable = new Runnable()
	{
		
		@Override
		public void run()
		{
			while (myController.finishPercentage() != 1.0)
			{
				try
				{
					progressDialog.setProgress((int) (myController.finishPercentage() * 100));
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{
					progressDialog.cancel();
					if (e.getMessage() == null)
		        	{
						Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity checkProgressRunnable : InterruptedException");
		        	}
		        	else
		        	{
		        		Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity checkProgressRunnable : " + e.getMessage());
					}
				}
			}
			
			HashMap<String, RouteEntry> onedayRoute = myController.getOneDayRoutes();
			ArrayList<JSONObject> planItems = adapter.getItems();
			synchronized (planItems)
			{
				for (int i = 0; i < planItems.size() - 1; i +=2)
				{
					try
					{
						String id = planItems.get(i).getString("name") + "||" 
								+ planItems.get(i + 1).getString("name");
						
//						System.out
//								.println("planItems : " + (planItems == null) 
//										+ ", id : " + (id == null));
//						System.out
//						.println( "onedayRoute : " + (onedayRoute == null));
//						System.out
//						.println( "route : " + (onedayRoute.get(id) == null));
						planItems.add(i + 1, Util.routePlan2Json(onedayRoute.get(id).route));
					}
					catch (JSONException e)
					{
						if (e.getMessage() == null)
			        	{
							Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity checkProgressRunnable : JSONException");
			        	}
			        	else
			        	{
			        		Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity checkProgressRunnable : " + e.getMessage());
						}
					}
				}
				
				try
				{
					String id = planItems.get(planItems.size() - 1).getString("name") + "||" 
							+ planItems.get(0).getString("name");
					planItems.add(planItems.size(), Util.routePlan2Json(onedayRoute.get(id).route));
				}
				catch (JSONException e)
				{
					if (e.getMessage() == null)
		        	{
						Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity checkProgressRunnable : JSONException");
		        	}
		        	else
		        	{
		        		Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity checkProgressRunnable : " + e.getMessage());
					}
				}
			}
			adapter.setItems(planItems);
			
			try
			{
				Message msg = Message.obtain();
				JSONObject info = new JSONObject();
				info.put("type", "genplan");
				info.put("result", "success");
				msg.obj = info.toString();
				synchronized (handler)
				{
					handler.sendMessage(msg);
				}
			}
			catch (JSONException e)
			{
				if (e.getMessage() == null)
	        	{
					Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity checkProgressRunnable : JSONException");
	        	}
	        	else
	        	{
	        		Log.e(Constant.LOG_LEVEL_ERROR, "SingledayPlanActivity checkProgressRunnable : " + e.getMessage());
				}
			}
			
		}
	};
}
