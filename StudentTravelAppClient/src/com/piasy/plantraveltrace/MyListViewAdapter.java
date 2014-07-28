package com.piasy.plantraveltrace;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.piasy.plantraveltrace.controller.Controller;
import com.piasy.plantraveltrace.model.Constant;
import com.piasy.plantraveltrace.model.Flag;


@SuppressLint("UseValueOf")
public class MyListViewAdapter extends BaseAdapter
{
	Context context;
	JSONObject addPlan;
	Controller myController;
	int activityID = Constant.ACTIVITY_ALLPLAN;
	
	public MyListViewAdapter(Context context, int activityID)
	{
		this.context = context;
		this.activityID = activityID;
		myController = Controller.getController();
		
		if (activityID == Constant.ACTIVITY_ALLPLAN)
		{
			addPlan = new JSONObject();
			try
			{
				addPlan.put("type", Constant.LISTVIEW_ITEM_PLAN);
				addPlan.put("status", Constant.PLAN_STATUS_ADD);
				planItems.add(addPlan);
			}
			catch (JSONException e)
			{
				if (e.getMessage() == null)
	        	{
					Log.e(Constant.LOG_LEVEL_ERROR, "MyListViewAdapter constr : JSONException");
	        	}
	        	else
	        	{
	        		Log.e(Constant.LOG_LEVEL_ERROR, "MyListViewAdapter constr : " + e.getMessage());
				}
			}
		}
	}
	
	/**
	 * {
	 * "type" : type,	//int, spot/traffic/hotel/plan
	 * 
	 * -spot-
	 * "name" : name,	//string
	 * "intro" : intro,	//string
	 * "photo" " photo	//string, absolute local path 
	 * 
	 * -traffic-
	 * "overall" : overall,	//string
	 * "desc" : [
	 * 			{
	 * 				"type" : type,	//string, walk/bus
	 * 				"desc" : desc	//string
	 * 			}
	 * 			]
	 * 
	 * -plan-
	 * "status" : status,	//int, add/plan/hist
	 * "spots" : spots,		//string, all spots name
	 * "year" : year,		//int
	 * "month" : month,		//int
	 * "day" : day,		//int
	 * "weekday" : weekday		//int
	 * 
	 * -hotel-
	 * "name" : name,		//string
	 * "address" : address,	//string
	 * "photo" : photo		//string, absolute local path
	 * }
	 * */
	ArrayList<JSONObject> planItems = new ArrayList<JSONObject>();
	
	final String[] weekdays = {"无", "日", "一", "二", "三", "四", "五", "六"};
	
	public void addItem(JSONObject item)
	{
		synchronized (planItems)
		{
			if (activityID == Constant.ACTIVITY_ALLPLAN)
			{
				int index = (planItems.size() - 1 < 0) ? 0 : (planItems.size() - 1);
				planItems.add(index, item);
			}
			else
			{
				planItems.add(item);
			}
		}
	}
	
	public void clear()
	{
		synchronized (planItems)
		{
			planItems.clear();
			if (activityID == Constant.ACTIVITY_ALLPLAN)
			{
				planItems.add(addPlan);
			}
		}
	}
	
	public ArrayList<JSONObject> getItems()
	{
		return planItems;
	}
	
	public void setItems(ArrayList<JSONObject> items)
	{
		synchronized (planItems)
		{
			planItems = items;
		}
	}
	
	public void sortByPrice()
	{
		try
		{
			synchronized (planItems)
			{
				for (int i = 0; i < planItems.size(); i ++)
				{
					int minPrice = planItems.get(i).getInt("price");
					int minIndex = i;
					for (int j = i + 1; j < planItems.size(); j ++)
					{
						int price = planItems.get(j).getInt("price");
						if (price < minPrice)
						{
							minIndex = j;
							minPrice = price;
						}
					}
					
					if (minIndex != i)
					{
						planItems.add(i, planItems.remove(minIndex));
					}
				}
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	public void sortByEval()
	{
		try
		{
			synchronized (planItems)
			{
				for (int i = 0; i < planItems.size(); i ++)
				{
					float maxScore = Float.parseFloat(planItems.get(i)
							.getJSONObject("attrs").getString("CommentScore"));
					int maxIndex = i;
					for (int j = i + 1; j < planItems.size(); j ++)
					{
						float score = Float.parseFloat(planItems.get(j)
								.getJSONObject("attrs").getString("CommentScore"));
						if (maxScore < score)
						{
							maxIndex = j;
							maxScore = score;
						}
					}
					
					if (maxIndex != i)
					{
						planItems.add(i, planItems.remove(maxIndex));
					}
				}
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
	}
	
	public int getItemType(int pos)
	{
		int ret = -1;
		synchronized (planItems)
		{
			if ((0 <= pos) && (pos < planItems.size()))
			{
				try
				{
					ret = planItems.get(pos).getInt("type");
				}
				catch (JSONException e)
				{
					if (e.getMessage() == null)
		        	{
						Log.e(Constant.LOG_LEVEL_ERROR, "MyListViewAdapter getItemType : JSONException");
		        	}
		        	else
		        	{
		        		Log.e(Constant.LOG_LEVEL_ERROR, "MyListViewAdapter getItemType : " + e.getMessage());
					}
				}
			}
		}
		return ret;
	}
	
	ArrayList<Flag> trafficDetailsStatus = new ArrayList<Flag>();
	public void switchTrafficDetail(int pos)
	{
//		System.out.println("switch : " + pos);
		
		if ((0 <= pos) && (pos < trafficDetailsStatus.size()) 
				&& (activityID == Constant.ACTIVITY_SINGLEDAY))
		{
			if (!trafficDetailsStatus.get(pos).value)
			{
//				System.out.println("switch : visible");
				trafficDetailsStatus.set(pos, new Flag(true));
//				System.out.println("visible ? " + trafficDetailsStatus.get(pos).value);
			}
			else
			{
//				System.out.println("switch : gone");
				trafficDetailsStatus.set(pos, new Flag(false));
//				System.out.println("visible ? " + trafficDetailsStatus.get(pos).value);
			}
		}
	}
	
	@Override
	public int getCount()
	{
		int count;
		synchronized (planItems)
		{
			count = planItems.size();
		}
		
		if ((activityID == Constant.ACTIVITY_SINGLEDAY)
				&& (trafficDetailsStatus.size() < planItems.size()))
		{
			synchronized (trafficDetailsStatus)
			{
				trafficDetailsStatus = new ArrayList<Flag>();
				for (int i = 0; i < count; i ++)
				{
					trafficDetailsStatus.add(new Flag(false));
				}
			}
		}
//		System.out.println("MyListViewAdapter.getCount() " + count);
		return count;
	}

	@Override
	public Object getItem(int position)
	{
		JSONObject ret;
		synchronized (planItems)
		{
			ret = planItems.get(position);
		}
		return ret;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final JSONObject item;
		synchronized (planItems)
		{
			item = planItems.get(position);
		}
		try
		{
//			System.out.println("get position : " + position);
			int type = item.getInt("type");
			switch (type)
			{
			case Constant.LISTVIEW_ITEM_SPOT:
			{
//				System.out.println("get : " + item.getString("name"));
				SpotViewHolder holder = new SpotViewHolder();
				convertView = LayoutInflater.from(context).inflate(
						R.layout.listview_item_spot, null);
				holder.image = (ImageView) convertView
						.findViewById(R.id.spotImage);
				holder.name = (TextView) convertView
						.findViewById(R.id.spotName);
				holder.intro = (TextView) convertView
						.findViewById(R.id.spotIntro);
				convertView.setTag(holder);
				
				File photo = new File(item.getString("photo"));
				if (photo.exists())
				{
					Bitmap bitmap = BitmapFactory.decodeFile(item.getString("photo"));
					holder.image.setImageBitmap(bitmap);
				}
				else
				{
					holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.spot_defalut));
				}
				holder.name.setText(item.getString("name"));
				String introString = item.getString("intro");
				if (introString.length() <= Constant.SPOT_INTRO_LEN)
				{
					introString = "      " + introString;
				}
				else
				{
					introString = "      " + introString.substring(0, Constant.SPOT_INTRO_LEN) + "...";
				}
				holder.intro.setText(introString);
				break;
			}
			case Constant.LISTVIEW_ITEM_SPOT_SEARCH:
			{
//				System.out.println("get : " + item.getString("name"));
				SpotSearchViewHolder holder = new SpotSearchViewHolder();
				convertView = LayoutInflater.from(context).inflate(
						R.layout.listview_item_spot_search_result, null);
				holder.image = (ImageView) convertView
						.findViewById(R.id.spotImageInSearch);
				holder.name = (TextView) convertView
						.findViewById(R.id.spotNameInSearch);
				holder.city = (TextView) convertView
						.findViewById(R.id.spotCityInSearch);
				holder.intro = (TextView) convertView
						.findViewById(R.id.spotIntroInSearch);
				holder.hotelAround = (Button) convertView
						.findViewById(R.id.hotelAroundButton);				
				convertView.setTag(holder);
				
				File photo = new File(item.getString("photo"));
				if (photo.exists())
				{
					Bitmap bitmap = BitmapFactory.decodeFile(item.getString("photo"));
//					int width = context.getResources().getDisplayMetrics().widthPixels
//							- Util.dip2px(context, 24);
//					int height = Util.dip2px(context, 106);
//					
//					System.out.println(bitmap.getWidth() + ", " + bitmap.getHeight());
//					Matrix matrix = new Matrix();
//					matrix.postScale((float) width / (float) bitmap.getWidth(), 
//							(float) width / (float) bitmap.getWidth());
//					Bitmap resize = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//					System.out.println(resize.getWidth() + ", " + resize.getHeight());
					holder.image.setImageBitmap(bitmap);
				}
				else
				{
					holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.spot_defalut));
				}
				holder.name.setText(item.getString("name"));
				holder.city.setText("城市：" + item.getString("city"));
				String introString = item.getString("intro");
				if (introString.length() <= Constant.SPOT_INTRO_LEN)
				{
					introString = "      " + introString;
				}
				else
				{
					introString = "      " + introString.substring(0, Constant.SPOT_INTRO_LEN) + "...";
				}
				holder.intro.setText(introString);
				
				holder.hotelAround.setOnClickListener(new View.OnClickListener()
				{
					
					@Override
					public void onClick(View v)
					{
						try
						{
							myController.setToCity(item.getString("city"));
							
							if (myController.getFromDate() == null)
							{
								Calendar cur = Calendar.getInstance();
								DatePickerDialog dpDialog = new DatePickerDialog
								(context, new DatePickerDialog.OnDateSetListener()
								{
									
									@Override
									public void onDateSet(DatePicker view, int year, int monthOfYear,
											int dayOfMonth)
									{
										myController.setPlanStartDate(year, monthOfYear, dayOfMonth);
										
										try
										{
											if (!myController.searchHotel(item.getString("name"), 1))
											{
												Controller.makeToast("正在搜索中，请耐心等待");
											}
										}
										catch (JSONException e)
										{
											e.printStackTrace();
										}
									}
								}, cur.get(Calendar.YEAR), cur.get(Calendar.MONTH), cur.get(Calendar.DAY_OF_MONTH));
								dpDialog.setTitle("请选择行程开始日期");
								dpDialog.show();
							}
							else
							{
								try
								{
									if (!myController.searchHotel(item.getString("name"), 1))
									{
										Controller.makeToast("正在搜索中，请耐心等待");
									}
								}
								catch (JSONException e)
								{
									e.printStackTrace();
								}
							}
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
						
					}
				});
				break;
			}
			case Constant.LISTVIEW_ITEM_TRAFFIC:
			{
//				System.out.println("get : " + item.getString("overall"));
				convertView = LayoutInflater.from(context).inflate(
						R.layout.listview_item_traffic, null);
				TrafficViewHolder holder = new TrafficViewHolder();
				
				holder.trafficDetailsLayout = (LinearLayout) convertView
						.findViewById(R.id.trafficDetails);
								
				JSONArray details = item.getJSONArray("desc");
				for (int i = 0; i < details.length(); i ++)
				{
					LinearLayout oneDetail = (LinearLayout) LayoutInflater.from(context).inflate(
							R.layout.traffic_onedetail, null);
					ImageView detailIcon = (ImageView) oneDetail.findViewById(R.id.trafficDetailTypeIcon);
					TextView detailDesc = (TextView) oneDetail.findViewById(R.id.trafficDetailDesc);
					JSONObject obj = details.getJSONObject(i);
					
					detailDesc.setText(obj.getString("desc"));
					if (obj.getString("type").equals("bus"))
					{
						detailIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.traffic_bus));
					}
					else if (obj.getString("type").equals("walk"))
					{
						detailIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.traffic_walk));
					}
					holder.trafficDetailsLayout.addView(oneDetail);
				}
				
				holder.trafficOverall = (TextView) convertView
						.findViewById(R.id.trafficOverall);
				holder.trafficOverall.setText(item.getString("overall"));
				
				synchronized (trafficDetailsStatus)
				{
//					System.out.println("visible ? " + trafficDetailsStatus.get(position).value);
					if (trafficDetailsStatus.get(position).value)
					{
//						System.out.println("set : VISIBLE");
						holder.trafficDetailsLayout.setVisibility(View.VISIBLE);
					}
					else
					{
//						System.out.println("set : gone");
						holder.trafficDetailsLayout.setVisibility(View.GONE);
					}
				}
				break;
			}
			case Constant.LISTVIEW_ITEM_HOTEL:
			{
				SpotViewHolder holder = new SpotViewHolder();
				convertView = LayoutInflater.from(context).inflate(
						R.layout.listview_item_spot, null);
				holder.image = (ImageView) convertView
						.findViewById(R.id.spotImage);
				holder.name = (TextView) convertView
						.findViewById(R.id.spotName);
				holder.intro = (TextView) convertView
						.findViewById(R.id.spotIntro);
				convertView.setTag(holder);
				
				File photo = new File(item.getJSONObject("attrs").getString("imageID"));
				if (photo.exists())
				{
					Bitmap bitmap = BitmapFactory.decodeFile(item
							.getJSONObject("attrs").getString("imageID"));
					holder.image.setImageBitmap(bitmap);
				}
				else
				{
					holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.spot_defalut));
				}
				holder.name.setText(item.getJSONObject("attrs")
						.getString("hotelName"));
//				System.out.println("get : " + item.getJSONObject("attrs")
//						.getString("hotelName"));
				holder.intro.setText(item.getJSONObject("attrs")
						.getString("hotelAddress"));
				break;
			}
			case Constant.LISTVIEW_ITEM_HOTEL_SEARCH:
			{
				int price = item.getInt("price");
				String city = item.getString("cityName");
				JSONObject attr = item.getJSONObject("attrs");
				String address = attr.getString("hotelAddress");
				String name = attr.getString("hotelName");
				String score = attr.getString("CommentScore");
				String imageUrl = attr.getString("imageID");
				String oneSentence = attr.getString("oneSentence");
//				System.out.println("get : " + name);
//				System.out.println(item.toString());
				
				HotelSearchViewHolder holder = new HotelSearchViewHolder();
				convertView = LayoutInflater.from(context).inflate(
						R.layout.listview_item_hotel_search_result, null);
				holder.image = (ImageView) convertView
						.findViewById(R.id.hotelImageInSearch);
				holder.name = (TextView) convertView
						.findViewById(R.id.hotelNameInSearch);
				holder.city = (TextView) convertView
						.findViewById(R.id.hotelCityInSearch);
				holder.intro = (TextView) convertView
						.findViewById(R.id.hotelIntroInSearch);
				holder.address = (TextView) convertView
						.findViewById(R.id.hotelAddressInSearch);	
				holder.score = (TextView) convertView
						.findViewById(R.id.hotelScore);
				holder.price = (TextView) convertView
						.findViewById(R.id.hotelMoney);
				holder.scoreLayout = (LinearLayout) convertView
						.findViewById(R.id.hotelScoreLayout);
				holder.priceLayout = (LinearLayout) convertView
						.findViewById(R.id.hotelPriceLayout);
				convertView.setTag(holder);
				
				File photo = new File(imageUrl);
				if (photo.exists())
				{
//					System.out.println("imageUrl = " + imageUrl);
					Bitmap bitmap = BitmapFactory.decodeFile(imageUrl);
					holder.image.setImageBitmap(bitmap);
				}
				else
				{
					holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.spot_defalut));
				}
				holder.name.setText(name);
				holder.city.setText("城市：" + city);
				if (oneSentence != null && 3 < oneSentence.length())
				{
					holder.intro.setText("      " + oneSentence);
				}
				else
				{
					holder.intro.setVisibility(View.GONE);
				}
				holder.address.setText("地址：" + address);
				holder.score.setText(score + "分");
				holder.price.setText("" + price + "元起");
				break;
			}
			case Constant.LISTVIEW_ITEM_PLAN:
			{
				int status = item.getInt("status");
				PlanViewHolder holder = new PlanViewHolder();
				switch (status)
				{
				case Constant.PLAN_STATUS_ADD:
					convertView = LayoutInflater.from(context).inflate(
							R.layout.listview_item_plan_add, null);
					convertView.setTag(holder);
					break;
				case Constant.PLAN_STATUS_PLAN:
				case Constant.PLAN_STATUS_HIST:
					convertView = LayoutInflater.from(context).inflate(
							R.layout.listview_item_plan, null);
					holder.spots = (TextView) convertView.findViewById(R.id.spotsNameList);
					holder.spots.setText(item.getString("spots"));
					holder.weekday = (TextView) convertView.findViewById(R.id.planWeekday);
					holder.weekday.setText("周" + weekdays[item.getInt("weekday")]);
					holder.day = (TextView) convertView.findViewById(R.id.planDay);
					holder.day.setText("" + item.getInt("day"));
					holder.month = (TextView) convertView.findViewById(R.id.planMonth);
					holder.month.setText((item.getInt("month") + 1) + "月");
					convertView.setTag(holder);
					break;
				default:
					break;
				}
			}
				break;
			default:
				break;
			}
		}
		catch (JSONException e)
		{
			if (e.getMessage() == null)
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "MyListViewAdapter getView : JSONException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "MyListViewAdapter getView : " + e.getMessage());
			}
		}
		
		return convertView;
	}
	
	public final class SpotViewHolder 
	{
		public ImageView image;
		public TextView name;
		public TextView intro;
	}
	
	public final class SpotSearchViewHolder 
	{
		public ImageView image;
		public TextView name;
		public TextView city;
		public TextView intro;
		public Button hotelAround;
	}
	
	public final class HotelSearchViewHolder 
	{
		public ImageView image;
		public TextView name;
		public TextView city;
		public TextView intro;
		public TextView address;
		public TextView score;
		public TextView price;
		LinearLayout scoreLayout, priceLayout;
	}
	
	public final class TrafficViewHolder 
	{
		public LinearLayout trafficDetailsLayout;
		public TextView trafficOverall;
	}
	
	public final class PlanViewHolder 
	{
		public TextView spots;
		public TextView weekday;
		public TextView day;
		public TextView month;
	}
}
