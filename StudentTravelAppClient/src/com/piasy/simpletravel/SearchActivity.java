package com.piasy.simpletravel;


import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.piasy.simpletravel.ScrollRefreshListView.OnRefreshListener;
import com.piasy.simpletravel.controller.Controller;
import com.piasy.simpletravel.model.Constant;

public class SearchActivity extends Activity
{
	PopMenu popMenu;
	Button searchType;
	Button travalInfoButton, planTableButton, settingButton;
	Button searchButton, sortTypeButton;
	EditText keywordEditText;
	ScrollRefreshListView resultListView;
	MyListViewAdapter adapter;
	LinearLayout sortTypeLayout;
	Button byPrice, byEval;
	
	final String[] types = {"求景点", "求住店"};
	final int [] searchTypesBg = {R.drawable.search_spot_style, R.drawable.search_hotel_style};
	final String[] searchHints = {"景点关键字", "酒店关键字"};
	int searchTypeId = 0;
	Controller myController;
	int longClickPos = 0;
	boolean inited = false;
	boolean sortTypeVisible = false;
	
	JSONArray spots = new JSONArray();
	JSONArray hotels = new JSONArray();
	
	AsyncTask<Void, Void, Void> refreshTask = null;
	int lastSearchPage = 0;
	String keyword = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		myController = Controller.getController();
		myController.setActivityHandler(handler);
		
		travalInfoButton = (Button) findViewById(R.id.travelInfoInSearch);
		planTableButton = (Button) findViewById(R.id.planTableInSearch);
		settingButton = (Button) findViewById(R.id.SettingInSearch);
		keywordEditText = (EditText) findViewById(R.id.keyword);
		searchButton = (Button) findViewById(R.id.searchButton);
		sortTypeButton = (Button) findViewById(R.id.sortTypeButton);
		resultListView = (ScrollRefreshListView) findViewById(R.id.searchResultListView);
		sortTypeLayout = (LinearLayout) findViewById(R.id.sortTypeLayout);
		byPrice = (Button) findViewById(R.id.sortByPrice);
		byEval = (Button) findViewById(R.id.sortByEval);
		
		popMenu = new PopMenu(this);
		popMenu.addItems(types);
		popMenu.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				searchType.setBackgroundResource(searchTypesBg[position]);
				keywordEditText.setHint(searchHints[position]);
				sortTypeButton.setVisibility(View.GONE);
				searchTypeId = position;
				updateUI();
				popMenu.dismiss();
			}
		});
		
		settingButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				if (!inited)
				{
					initUI();
				}
				Intent settingIntent = new Intent(SearchActivity.this, SettingActivity.class);
				startActivity(settingIntent);
			}
		});
		
		searchType = (Button) findViewById(R.id.searchType);
		searchType.setBackgroundResource(searchTypesBg[0]);
		keywordEditText.setHint(searchHints[0]);
		sortTypeLayout.setVisibility(View.GONE);
		adapter = new MyListViewAdapter(this, Constant.ACTIVITY_SEARCH);
		resultListView.setAdapter(adapter);
		
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
		spots = myController.getRecSpots();
		
		searchType.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				popMenu.showAsDropDown(v);
				keywordEditText.setVisibility(View.VISIBLE);
				searchButton.setVisibility(View.VISIBLE);
			}
		});
		
		byPrice.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				sortTypeLayout.setVisibility(View.GONE);
				sortTypeButton.setBackgroundResource(R.drawable.sort_type_hide);
				sortTypeVisible = false;
				
				adapter.sortByPrice();
				adapter.notifyDataSetChanged();
				Controller.makeToast("排序成功");
			}
		});
		
		byEval.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				sortTypeLayout.setVisibility(View.GONE);
				sortTypeButton.setBackgroundResource(R.drawable.sort_type_hide);
				sortTypeVisible = false;
				
				adapter.sortByEval();
				adapter.notifyDataSetChanged();
				Controller.makeToast("排序成功");
			}
		});
		
		searchButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				keyword = keywordEditText.getText().toString();
				if (searchTypeId == 0)
				{
					if (!myController.query(keyword, Constant.SEARCH_BY_KEYWORD, lastSearchPage))
					{
						Controller.makeToast("正在搜索中，请耐心等待");
					}
				}
				else if (searchTypeId == 1)
				{
					if (myController.getToCity() == null)
					{
						Builder dialog = new AlertDialog.Builder(SearchActivity.this);
						dialog.setTitle("还没有设置目的城市哦~");
						dialog.setIcon(android.R.drawable.ic_dialog_info);
						
						final EditText toCity = new EditText(SearchActivity.this);
						toCity.setHint("目的城市");
						
						dialog.setView(toCity);
						dialog.setPositiveButton("确定", new DialogInterface.OnClickListener()
						{
							
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								myController.setToCity(toCity.getText().toString());
								
								if (!myController.searchHotel(keyword, lastSearchPage))
								{
									Controller.makeToast("正在搜索中，请耐心等待");
								}
								
								sortTypeButton.setVisibility(View.VISIBLE);
							}
						});
						dialog.setNegativeButton("取消", null);
						dialog.show();
					}
					else 
					{
						if (!myController.searchHotel(keyword, lastSearchPage))
						{
							Controller.makeToast("正在搜索中，请耐心等待");
						}
						
						sortTypeButton.setVisibility(View.VISIBLE);
					}
				}
				

				keywordEditText.setVisibility(View.INVISIBLE);
				searchButton.setVisibility(View.INVISIBLE);
			}
		});
		
		sortTypeButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				if (sortTypeVisible)
				{
					sortTypeLayout.setVisibility(View.GONE);
					sortTypeButton.setBackgroundResource(R.drawable.sort_type_hide);
				}
				else
				{
					sortTypeLayout.setVisibility(View.VISIBLE);
					sortTypeButton.setBackgroundResource(R.drawable.sort_type_show);
				}
				
				sortTypeVisible = !sortTypeVisible;
			}
		});
		
		planTableButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});

		resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				//because the first is a refresh header
				if (0 < position)
				{
					position --;
				}
				if (adapter.getItemType(position) == Constant.LISTVIEW_ITEM_HOTEL_SEARCH)
				{
					myController.seeHotelDetail((JSONObject) adapter.getItem(position));
					
					Intent hotelDetailInfoIntent = new Intent(SearchActivity.this, HotelDetailActivity.class);
					startActivity(hotelDetailInfoIntent);
				}
				else if (adapter.getItemType(position) == Constant.LISTVIEW_ITEM_SPOT_SEARCH)
				{
					myController.seeSpotDetail((JSONObject) adapter.getItem(position));
					
					Intent spotDetailInfoIntent = new Intent(SearchActivity.this, SpotDetailActivity.class);
					startActivity(spotDetailInfoIntent);
				}
			}
		});
		resultListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				//because the first is a refresh header
				if (0 < position)
				{
					position --;
				}
				longClickPos = position;
				return false;
			}
		});
		resultListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener()
		{
			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo)
			{
				menu.add(0, 0, 0, "添加到行程");
			}
		});
		resultListView.setonRefreshListener(new OnRefreshListener() 
		{

			@Override
			public void onRefresh() 
			{
				if (keyword != null)
				{
					lastSearchPage ++;
					switch (searchTypeId)
					{
					case 0:
						if (!myController.query(keyword, Constant.SEARCH_BY_KEYWORD, lastSearchPage))
						{
							Controller.makeToast("正在搜索中，请耐心等待");
						}
						break;
					case 1:
						if (!myController.searchHotel(keyword, lastSearchPage))
						{
							Controller.makeToast("正在搜索中，请耐心等待");
						}
						break;
					default:
						break;
					}
				}
			}
		});
		
		inited = true;
	}

	protected void updateUI()
	{
		adapter.clear();
		
		if (searchTypeId == 0)
		{
			synchronized (spots)
			{
				for (int i = 0; i < spots.length(); i ++)
				{
					try
					{
						JSONObject spot = spots.getJSONObject(i);
						spot.put("type", Constant.LISTVIEW_ITEM_SPOT_SEARCH);
						adapter.addItem(spot);
					}
					catch (JSONException e)
					{
						if (e.getMessage() == null)
			        	{
							Log.e(Constant.LOG_LEVEL_ERROR, "SearchActivity updateUI : JSONException");
			        	}
			        	else
			        	{
			        		Log.e(Constant.LOG_LEVEL_ERROR, "SearchActivity updateUI : " + e.getMessage());
						}
					}
				}
			}
		}
		else if (searchTypeId == 1)
		{
			synchronized (hotels)
			{
				for (int i = 0; i < hotels.length(); i ++)
				{
					try
					{
						JSONObject hotel = hotels.getJSONObject(i);
						hotel.put("type", Constant.LISTVIEW_ITEM_HOTEL_SEARCH);
//						hotel.put("city", myController.getToCity());
						adapter.addItem(hotel);
					}
					catch (JSONException e)
					{
						if (e.getMessage() == null)
			        	{
							Log.e(Constant.LOG_LEVEL_ERROR, "SearchActivity updateUI : JSONException");
			        	}
			        	else
			        	{
			        		Log.e(Constant.LOG_LEVEL_ERROR, "SearchActivity updateUI : " + e.getMessage());
						}
					}
				}
			}
			
			adapter.sortByPrice();
		}
		
		adapter.notifyDataSetChanged();
		resultListView.onRefreshComplete();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		myController.setActivityHandler(handler);
		
		System.out.println("SearchActivity.onResume()");
		
		if (myController.exiting())
		{
			System.out.println("SearchActivity.onResume()");
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
			}
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		try
		{
			switch (item.getItemId())
			{
			case 0:
				if (myController.getFromDate() == null)
				{
					Calendar cur = Calendar.getInstance();
					DatePickerDialog dpDialog = new DatePickerDialog
					(SearchActivity.this, new DatePickerDialog.OnDateSetListener()
					{
						
						@Override
						public void onDateSet(DatePicker view, int year, int monthOfYear,
								int dayOfMonth)
						{
							myController.setPlanStartDate(year, monthOfYear, dayOfMonth);
							
							synchronized (spots)
							{
								try
								{
									JSONObject spot = spots.getJSONObject(longClickPos);
									if (myController.addSpot(spot))
									{
										Controller.makeToast("添加成功");
									}
									else
									{
										Controller.makeToast("已经在行程中啦");
									}
								}
								catch (JSONException e)
								{
									e.printStackTrace();
								}
							}
						}
					}, cur.get(Calendar.YEAR), cur.get(Calendar.MONTH), cur.get(Calendar.DAY_OF_MONTH));
					dpDialog.setTitle("请选择行程开始日期");
					dpDialog.show();
				}
				else
				{
					JSONObject spot;
					synchronized (spots)
					{
						spot = spots.getJSONObject(longClickPos);
					}
					if (myController.addSpot(spot))
					{
						Controller.makeToast("添加成功");
					}
					else
					{
						Controller.makeToast("已经在行程中啦");
					}
				}
				break;
			default:
				return super.onContextItemSelected(item);
			}
		}
		catch (JSONException e)
		{
			if (e.getMessage() == null)
        	{
				Log.e(Constant.LOG_LEVEL_ERROR, "SearchActivity onContextItemSelected : JSONException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "SearchActivity onContextItemSelected : " + e.getMessage());
			}
		}
		return true;
	}
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			try
			{
				Log.d(Constant.LOG_LEVEL_DEBUG, "get info at search activity : " + (String)msg.obj);
				JSONObject data = new JSONObject((String)msg.obj);
				String type = data.getString("type");
				
				if (type.equals("query"))
				{
					if (data.getString("result").equals("success"))
					{
						Controller.makeToast("搜索成功");
						
						synchronized (spots)
						{
							JSONArray oldSpots = spots;
							spots = data.getJSONArray("spots");
							for (int i = 0; i < oldSpots.length(); i ++)
							{
								spots.put(oldSpots.getJSONObject(i));
							}
						}
						
						updateUI();
					}
					else
					{
						Controller.makeToast("搜索失败");
					}
				}
				else if (type.equals("hotelsearch"))
				{
					if (data.getString("result").equals("success"))
					{
						Controller.makeToast("搜索成功");
						
						synchronized (hotels)
						{
							JSONArray oldHotels = hotels;
							hotels = data.getJSONArray("hotels");
							for (int i = 0; i < oldHotels.length(); i ++)
							{
								hotels.put(oldHotels.getJSONObject(i));
							}
						}
						
						sortTypeButton.setVisibility(View.VISIBLE);
						searchTypeId = 1;
						searchType.setBackgroundResource(searchTypesBg[searchTypeId]);
						keywordEditText.setHint(searchHints[searchTypeId]);
						
						updateUI();
					}
					else
					{
						Controller.makeToast("搜索失败");
					}
				}
			} 
			catch (JSONException e)
			{
				if (e.getMessage() == null)
	        	{
					Log.e(Constant.LOG_LEVEL_ERROR, "SearchActivity handler : JSONException");
	        	}
	        	else
	        	{
	        		Log.e(Constant.LOG_LEVEL_ERROR, "SearchActivity handler : " + e.getMessage());
				}
			}
		}
	};
}
