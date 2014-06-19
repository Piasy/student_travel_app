package com.piasy.plantraveltrace.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.piasy.plantraveltrace.util.Util;

/**
 * Make a plan from a list of spots
 * */
public class PlanGenerator
{
	BMapManager bMapManager;
	MKSearch mSearch = new MKSearch();	//only last one is useful --> changed, it could have many instance TODO
	String searchRouteId = "";			//TODO it will work with one mSearch
	public PlanGenerator(BMapManager bMapManager)
	{
		this.bMapManager = bMapManager;
		mSearch.init(bMapManager, new MKSearchListener()
        {
            @Override
            public void onGetPoiDetailSearchResult(int type, int error) { }
            @Override
            public void onGetPoiResult(MKPoiResult res, int type, int error) { }
            
            @Override
			public void onGetTransitRouteResult(MKTransitRouteResult res, int error)
			{
            	boolean noresult = false;
            	
            	if (res == null || error != 0 || res.getNumPlan() == 0)
            	{
            		Log.d(Constant.LOG_LEVEL_DEBUG, "no result");
            		setNoRouteResult(searchRouteId);
            		noresult = true;
            	}
            	
				if (!noresult)
				{
	            	int routeNum = res.getNumPlan();
					int minTime = 24 * 3600, minIndex = 0;
					ArrayList<String> others = new ArrayList<String>();
				    for (int i = 0; i < routeNum; i ++)
				    {
				    	others.add(Util.routePlan2Json(res.getPlan(i)).toString());
				    	if (res.getPlan(i).getTime() < minTime)
				    	{
				    		minTime = res.getPlan(i).getTime();
				    		minIndex = i;
				    	}
				    }
				    
				    others.remove(minIndex);
				    
				    String id = res.getStart().name + "||" + res.getEnd().name;
				    synchronized (routes)
					{
						for (int i = 0; i < routes.size(); i ++)
						{
							if (routes.get(i).containsKey(id))
							{
								RouteEntry route = routes.get(i).get(id);
								route.route = res.getPlan(minIndex);
								route.others = new JSONArray(others);
//								routes.get(i).put(id, route);
								
								synchronized (status)
								{
									status.get(i).put(id, Boolean.valueOf(true));
								}
								break;
							}
						}
					}
				    
				    Log.d(Constant.LOG_LEVEL_DEBUG, "get route " + minIndex + " : " 
				    + res.getStart().name + " -> " + res.getEnd().name + ", " + res.getPlan(minIndex).getTime() + " sec");
				}
				
			    if (genThread != null)
			    {
			    	synchronized (genThread)
					{
			    		requestTask.cancel();
			    		genThread.notify();
					}
			    }
			}
			
			@Override
			public void onGetAddrResult(MKAddrInfo arg0, int arg1) {}
			@Override
			public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {}
			@Override
			public void onGetDrivingRouteResult(MKDrivingRouteResult arg0, int arg1) {}
			@Override
			public void onGetShareUrlResult(MKShareUrlResult arg0, int arg1, int arg2) {}
			@Override
			public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {}
			@Override
			public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {}
        });
		mSearch.setPoiPageCapacity(Constant.POI_SEARCH_PAGE_CAPACITY);
	}
	
	protected void setNoRouteResult(String id)
	{
		synchronized (routes)
		{
			for (int i = 0; i < routes.size(); i ++)
			{
				if (routes.get(i).containsKey(id))
				{
					RouteEntry route = routes.get(i).get(id);
					route.route = null;
					route.others = new JSONArray();
					
					synchronized (status)
					{
						status.get(i).put(id, Boolean.valueOf(true));
					}
					break;
				}
			}
		}
	}
	
	/**
	 * spot/hotel:
	 * {
	 * "name" : name,	//string
	 * "city" : city,	//string
	 * "latitude" : latitude,	//int
	 * "longitude" : longitude	//int
	 * }
	 * 
	 * */
	ArrayList<JSONObject> chosedSpots = new ArrayList<JSONObject>();
	ArrayList<ArrayList<JSONObject>> planedSpots = new ArrayList<ArrayList<JSONObject>>();
	public boolean addSpot(JSONObject spot)
	{
		boolean ret = true;
		synchronized (chosedSpots)
		{
			for (int i = 0; i < chosedSpots.size(); i ++)
			{
				try
				{
					if (chosedSpots.get(i).getString("name").equals(spot.getString("name"))
					 && chosedSpots.get(i).getString("city").equals(spot.getString("city")))
					{
						ret = false;
						break;
					}
				}
				catch (JSONException e)
				{
					if (e.getMessage() == null)
		        	{
						Log.e(Constant.LOG_LEVEL_ERROR, "PlanGenerator addSpot : JSONException");
		        	}
		        	else
		        	{
		        		Log.e(Constant.LOG_LEVEL_ERROR, "PlanGenerator addSpot : " + e.getMessage());
					}
				}
			}
			
			if (ret)
			{
				chosedSpots.add(spot);
			}
		}
		
		return ret;
	}
	
	int planDays = 2;
	public void setPlanDays(int planDays)
	{
		this.planDays = planDays;
	}
	
	JSONObject hotel = null;
	boolean hasAddHotel = false;
	public boolean setHotel(JSONObject hotel)
	{
		boolean ret = false;
		
		this.hotel = hotel;
		
		synchronized (planedSpots)
		{
			if (!hasAddHotel)
			{
				if (planedSpots.size() != 0)
				{
					hasAddHotel = true;
					for (int i = 0; i < planedSpots.size(); i ++)
					{
						planedSpots.get(i).add(0, hotel);
					}
				}
			}
			else
			{
				for (int i = 0; i < planedSpots.size(); i ++)
				{
					planedSpots.get(i).set(0, hotel);
				}
			}
			
			ret = true;
		}
		
		return ret;
	}
	
	public ArrayList<ArrayList<JSONObject>> adjust(int prevDay, int index, int day)
	{
		if ((0 <= prevDay) && (prevDay < planDays)
		 && (0 <= day) && (prevDay < day))
		{
			synchronized (planedSpots)
			{
				ArrayList<JSONObject> oneday = planedSpots.get(prevDay);
				if ((0 <= index) && (index < oneday.size()))
				{
					planedSpots.get(day).add(oneday.remove(index));
				}
			}
		}
		
		return getPlan();
	}
	
	public ArrayList<ArrayList<JSONObject>> getPlan()
	{
		synchronized (planedSpots)
		{
			if (planedSpots.size() == 0)
			{
				synchronized (chosedSpots)
				{
					int size = (int) Math.ceil((double) chosedSpots.size() / (double) planDays), pos = 0;
					Log.d(Constant.LOG_LEVEL_DEBUG, "chosedSpots size = " + chosedSpots.size());
					Log.d(Constant.LOG_LEVEL_DEBUG, "size = " + size);
					
					for (int i = 0; i < planDays; i ++)
					{						
						if (chosedSpots.size() <= pos)
						{
							break;
						}
					
						ArrayList<JSONObject> oneday = new ArrayList<JSONObject>();
						if (hotel != null)
						{
							oneday.add(hotel);
							hasAddHotel = true;
						}
						for (int j = 0; j < size; j ++)
						{
							if (chosedSpots.size() <= pos)
							{
								break;
							}
							
							oneday.add(chosedSpots.get(pos));
							pos ++;
						}
						
						
						planedSpots.add(oneday);
					}
				}
			}
			return planedSpots;
		}
	}
	
	boolean suggested = false;
	public ArrayList<ArrayList<JSONObject>> getSugPlan()
	{
		if (!suggested)
		{
			synchronized (planedSpots)
			{
				genThread.genSugPlan();
			}
			
			suggested = true;
		}
		
		return planedSpots;
	}
	
	ArrayList<HashMap<String, RouteEntry>> routes = new ArrayList<HashMap<String,RouteEntry>>();
	public ArrayList<HashMap<String, RouteEntry>> getRoutes()
	{
		return routes;
	}
	
	ArrayList<HashMap<String, Boolean>> status = new ArrayList<HashMap<String,Boolean>>();
	GenThread genThread;
	public boolean genPlan()
	{
		boolean ret = false;
		if (hotel != null)
		{
			genThread = new GenThread();
			genThread.start();
			
			ret = true;
		}
		
		return ret;
		
//		MKPlanNode srcNode = new MKPlanNode();
//		srcNode.name = "北京动物园";
//		srcNode.pt = new GeoPoint(39944504, 116346224);
//		
//		MKPlanNode destNode = new MKPlanNode();
//		destNode.name = "北京植物园";
//		destNode.pt = new GeoPoint(40007792, 116214848);
//		mSearch.transitSearch("北京", srcNode, destNode);
	}

	int finishedCount = 0;
	public float finishPercentage()
	{
		finishedCount = 0;
		synchronized (status)
		{
			for (int i = 0; i < status.size(); i ++)
			{
				for (String key : status.get(i).keySet())
				{
					if (status.get(i).get(key).booleanValue())
					{
						finishedCount ++;
					}
				}					
			}
		}
				
		return (float) finishedCount / (float) routeCount;
	}
		
	Timer requestTimer = new Timer();
	
	class ReRequestTask extends TimerTask
	{
		RouteEntry route;
		public ReRequestTask(RouteEntry route)
		{
			this.route = route;
		}
		
		@Override
		public void run()
		{
			mSearch.transitSearch(route.city, route.srcNode, route.destNode);
			
			Log.d(Constant.LOG_LEVEL_DEBUG, "resend transit search request : "
					+ route.srcNode.name + " -> " + route.destNode.name);
		}
		
	}
	
	ReRequestTask requestTask;
	
	int routeCount = 0;
	/**
	 * */
	class GenThread extends Thread
	{
		
		boolean finished = false;
		@Override
		public void run()
		{
			synchronized (this)
			{
				synchronized (planedSpots)
				{
					Log.d(Constant.LOG_LEVEL_DEBUG, "planedSpots size = " + planedSpots.size());
					for (int i = 0; i < planedSpots.size(); i ++)
					{
						HashMap<String, RouteEntry> onedayRoute = new HashMap<String, RouteEntry>();
						ArrayList<JSONObject> oneday = planedSpots.get(i);
						HashMap<String, Boolean> onedayStatus = new HashMap<String, Boolean>();
						Log.d(Constant.LOG_LEVEL_DEBUG, "oneday size = " + oneday.size());
						for (int j = 0; j < oneday.size(); j ++)
						{
							for (int k = 0; k < oneday.size(); k ++)
							{
								if (k == j)
								{
									continue;
								}
								
								try
								{
									JSONObject src = oneday.get(j);
									JSONObject dest = oneday.get(k);
									
									MKPlanNode srcNode = new MKPlanNode();
									srcNode.name = src.getString("name");
									srcNode.pt = new GeoPoint(src.getInt("latitude"), src.getInt("longitude"));
									
									MKPlanNode destNode = new MKPlanNode();
									destNode.name = dest.getString("name");
									destNode.pt = new GeoPoint(dest.getInt("latitude"), dest.getInt("longitude"));
									
									onedayStatus.put(srcNode.name + "||" + destNode.name, 
											Boolean.valueOf(false));
									
									RouteEntry route = new RouteEntry(srcNode, destNode, src.getString("city"));
									onedayRoute.put(srcNode.name + "||" + destNode.name, route);
									
									Log.d(Constant.LOG_LEVEL_DEBUG, "route : " + srcNode.name + "||" + destNode.name);
									routeCount ++;
								}
								catch (JSONException e)
								{
									if (e.getMessage() == null)
						        	{
						        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller GenThread : JSONException");
						        	}
						        	else
						        	{
						        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller GenThread : " + e.getMessage());
									}
								}
							}
						}
						synchronized (status)
						{
							status.add(onedayStatus);
						}
						synchronized (routes)
						{
							routes.add(onedayRoute);
						}
					}
					
					Log.d(Constant.LOG_LEVEL_DEBUG, "routes size = " + routes.size());
					Log.d(Constant.LOG_LEVEL_DEBUG, "routeCount = " + routeCount);
					
					for (int i = 0; i < routes.size(); i ++)
					{
						HashMap<String, RouteEntry> onedayRoute = routes.get(i);
						for (String id : onedayRoute.keySet())
						{
							RouteEntry route = onedayRoute.get(id);
							searchRouteId = id;
							mSearch.transitSearch(route.city, route.srcNode, route.destNode);
							Log.d(Constant.LOG_LEVEL_DEBUG, "send transit search request : " + id);
							
							requestTask = new ReRequestTask(route);
							requestTimer.schedule(requestTask, Constant.ROUTE_REQUESR_TIMEOUT, Constant.ROUTE_REQUESR_INTERVAL);
							try
							{
								this.wait();
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						}
					}
					
					genSugPlan();
				}
			}
		}
		
		public void genSugPlan()
		{
			for (int i = 0; i < planedSpots.size(); i ++)
			{
				planDFS(planedSpots.get(i), 1, planedSpots.get(i).size() - 1, i);
				ArrayList<JSONObject> tmp = new ArrayList<JSONObject>(shortest);
				planedSpots.set(i, tmp);
				shortest.clear();
				minTime = 999999999;
			}
		}
		
		ArrayList<JSONObject> shortest = new ArrayList<JSONObject>();
		int minTime = 999999999;
		protected void planDFS(ArrayList<JSONObject> oneday, int begin, int end, int day)
		{
			if (begin == end)
			{
				int time = 0;
				HashMap<String, RouteEntry> onedayRoute = routes.get(day);
				try
				{
					for (int i = 1; i < oneday.size(); i ++)
					{
						String id = oneday.get(i - 1).getString("name") + "||" 
								  + oneday.get(i).getString("name");
						if (onedayRoute.get(id).route != null)
						{
							time += onedayRoute.get(id).route.getTime();
						}
					}
					
					String id = oneday.get(oneday.size() - 1).getString("name") + "||" 
							  + oneday.get(0).getString("name");
					if (onedayRoute.get(id).route != null)
					{
						time += onedayRoute.get(id).route.getTime();
					}
					
					if (time < minTime)
					{
						shortest.clear();
						shortest.addAll(oneday);
						minTime = time;
					}
				}
				catch (JSONException e)
				{
					if (e.getMessage() == null)
		        	{
		        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller planDFS : JSONException");
		        	}
		        	else
		        	{
		        		Log.e(Constant.LOG_LEVEL_ERROR, "Controller planDFS : " + e.getMessage());
					}
				}
			}
			else
			{
				for (int i = begin; i <= end; i ++)
				{
					swap(oneday, begin, i);
					
					planDFS(oneday, begin + 1, end, day);
					
					swap(oneday, i, begin);
				}
			}
		}
		
		protected void swap(ArrayList<JSONObject> oneday, int i, int j)
		{
			JSONObject tmp = oneday.get(i);
			oneday.set(i, oneday.get(j));
			oneday.set(j, tmp);
		}
	}
}
