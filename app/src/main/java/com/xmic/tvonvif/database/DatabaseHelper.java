package com.xmic.tvonvif.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String CREATE_SQL = "CREATE TABLE t_onvif_cameras (id integer PRIMARY KEY AUTOINCREMENT, UUID varchar(32), name varchar(32), username varchar(16), password varchar(16), width integer, height integer, rate integer);"; 
	
	public DatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	
	public DatabaseHelper(Context context, String name)
	{
		this(context, name, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		try {
			db.execSQL(CREATE_SQL);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		super.onOpen(db);
	}

}
