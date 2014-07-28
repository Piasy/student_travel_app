package com.piasy.plantraveltrace.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBManager
{
	private DBHelper helper;
	private SQLiteDatabase database;
	
	public DBManager(Context context)
	{
		helper = new DBHelper(context);
		database = helper.getWritableDatabase();
	}
	
	public boolean dbIsOpen()
	{
		return database.isOpen();
	}
	
	public void openDB()
	{
		database = helper.getWritableDatabase();
	}
	
	public void closeDB()
	{
		database.close();
	}
}
