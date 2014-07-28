package com.piasy.plantraveltrace.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper
{
	private static final String DATABASE_NAME = "PLANS.db";
	private static final int DATABASE_VERSION = 1;
	
	public DBHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public DBHelper(Context context, String name, CursorFactory factory,
			int version)
	{
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
//		db.execSQL("CREATE TABLE IF NOT EXISTS " + PTTConfig.DATABASE_TABLE_NAME +  
//                "(id INTEGER PRIMARY KEY AUTOINCREMENT, date INTEGER, hotel TEXT, spot TEXT, trafic TEXT)"); 
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
//		db.execSQL("ALTER TABLE records ADD COLUMN other TEXT");  
	}

}
