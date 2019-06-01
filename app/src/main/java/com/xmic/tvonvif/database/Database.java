package com.xmic.tvonvif.database;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xmic.tvonvif.finder.CameraDevice;

public class Database {

	public static final String DATABASE_NAME = "xmic.tvonvif.db";
	public static final String T_TABLE = "t_onvif_cameras";
	public static final String C_USERNAME = "username";
	public static final String C_PASSWORD = "password";
	public static final String C_NAME = "name";
	public static final String C_WIDTH = "width";
	public static final String C_HEIGHT = "height";
	public static final String C_UUID = "UUID";
	public static final String C_RATE = "rate";

	private SQLiteDatabase mDbRead, mDbWrite;
	private DatabaseHelper mDh;

	public Database(Context context) {
		mDh = new DatabaseHelper(context, DATABASE_NAME);
		mDbRead = mDh.getReadableDatabase();
		mDbWrite = mDh.getWritableDatabase();
	}

	public boolean addCamera(CameraDevice cd) {
		if (this.getCameraByUUID(cd.uuid) != null) {
			return false;
		} else {
			ContentValues cv = new ContentValues();
			cv.put(C_HEIGHT, cd.height);
			cv.put(C_WIDTH, cd.width);
			cv.put(C_RATE, cd.rate);
			cv.put(C_UUID, cd.uuid.toString());
			cv.put(C_USERNAME, cd.username);
			cv.put(C_PASSWORD, cd.password);
			cd.setId((int)mDbWrite.insert(T_TABLE, null, cv));
			return true;
		}
	}
	
	public boolean deleteCamera(CameraDevice cd) {
		if (this.getCameraByUUID(cd.uuid) != null) {
			int result = mDbWrite.delete(T_TABLE, C_UUID+"=?", new String[]{cd.uuid.toString()});
			if (result > 0) {
				return true;
			} else {
				return false;
			}
			
		} else {
			return false;
		}
	}
	
	public boolean updateCamera(CameraDevice cd) {
		if (this.getCameraByUUID(cd.uuid) != null) {
			ContentValues cv = new ContentValues();
			cv.put(C_HEIGHT, cd.height);
			cv.put(C_WIDTH, cd.width);
			cv.put(C_RATE, cd.rate);
			cv.put(C_UUID, cd.uuid.toString());
			cv.put(C_USERNAME, cd.username);
			cv.put(C_PASSWORD, cd.password);
			mDbWrite.update(T_TABLE, cv, C_UUID+"=?", new String[]{cd.uuid.toString()});
			return true;
		} else {
			return false;
		}
	}
	
	public CameraDevice getCameraByid(int id) {
		String sql = "SELECT * FROM " + T_TABLE + " WHERE id = %d;";
		sql = String.format(sql, id);
		Cursor c = mDbRead.rawQuery(sql, null);
		if (c.getCount() == 0) {
			return null;
		} else {
			c.moveToFirst();
			return this.getCameraDevice(c);
		}
	}
	
	public CameraDevice getCameraByUUID(UUID uuid) {
		String sql = "SELECT * FROM " + T_TABLE + " WHERE " + C_UUID + " = '%s';";
		sql = String.format(sql, uuid.toString());
		Cursor c = mDbRead.rawQuery(sql, null);
		if (c.getCount() == 0) {
			return null;
		} else {
			c.moveToFirst();
			return this.getCameraDevice(c);
		}
	}
	
	public List<CameraDevice> getCameraDevices() {
		List<CameraDevice> list = new ArrayList<CameraDevice>();
		String sql = "SELECT * FROM " + T_TABLE;
		Cursor c = mDbRead.rawQuery(sql, null);
		while (c.moveToNext()) {
			UUID uuid = UUID.fromString(c.getString(c.getColumnIndex(C_UUID)));
			CameraDevice cd = new CameraDevice(uuid, "");
			cd.setId(c.getInt(0));
			cd.setProperties(c.getInt(c.getColumnIndex(C_WIDTH)), 
					c.getInt(c.getColumnIndex(C_HEIGHT)), 
					c.getInt(c.getColumnIndex(C_RATE)));
			cd.setSecurity(c.getString(c.getColumnIndex(C_USERNAME)), 
					c.getString(c.getColumnIndex(C_PASSWORD)));
			list.add(cd);
		}
		return list;
	}
	
	public CameraDevice getCameraDevice(Cursor c) {
		UUID uuid = UUID.fromString(c.getString(c.getColumnIndex(C_UUID)));
		CameraDevice cd = new CameraDevice(uuid, "");
		cd.setId(c.getInt(0));
		cd.setProperties(c.getInt(c.getColumnIndex(C_WIDTH)), 
				c.getInt(c.getColumnIndex(C_HEIGHT)), 
				c.getInt(c.getColumnIndex(C_RATE)));
		cd.setSecurity(c.getString(c.getColumnIndex(C_USERNAME)), 
				c.getString(c.getColumnIndex(C_PASSWORD)));
		return cd;
	}
}
