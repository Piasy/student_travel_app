package com.piasy.plantraveltrace.model;

import org.json.JSONArray;

import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKTransitRoutePlan;
import com.piasy.plantraveltrace.util.Util;

public class RouteEntry
{
	public MKPlanNode srcNode, destNode;
	public MKTransitRoutePlan route;
	public JSONArray others;
	public String city;
	
	public RouteEntry(MKPlanNode source, MKPlanNode target, String city)
	{
		this.srcNode = source;
		this.destNode = target;
		this.city = city;
	}
	
	@Override
	public String toString()
	{
		if (route != null)
		{
			return srcNode.name + " -> " + destNode.name + ":\n" 
					+ route.getTime() + " sec\n"
					+ Util.routePlan2Json(route).toString() + "\n"
					+ others.toString();
		}
		else
		{
			return "No result...";
		}
	}
}
