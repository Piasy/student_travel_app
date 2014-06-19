package com.piasy.plantraveltrace.model;


public class ListViewItems
{
	int size = 1; //button组不用设置
	String recordTime = null, recordLong = null, recordLat = null, recordPlace = null;
	String recordImage = null;
	String recordState = null;
	public ListViewItems() {}
	
	
	public void setItemTypeOne(String recordTime, String recordLong, String recordLat, String recordPlace)
	{
		this.recordTime = recordTime;
		this.recordPlace = recordPlace;
		this.recordLong = recordLong;
		this.recordLat = recordLat;
		size ++;
	}
	
	public void setItemTypeTwo(String recordImage)
	{
		this.recordImage = recordImage;
		size ++;
	}
	
	public void setItemTypeThree(String recordState)
	{
		this.recordState = recordState;
		size ++;
	}
	
	public int size()
	{
		return size;
	}
	
	public String getLong()
	{
		return recordLong;
	}
	
	public String getLat()
	{
		return recordLat;
	}
	
	public String getPlace()
	{
		return recordPlace;
	}
	
	public String getImage()
	{
		return recordImage;
	}
	
	public String getState()
	{
		return recordState;
	}
	
	public String getTime()
	{
		return recordTime;
	}
}
