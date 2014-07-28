/*
 * @(#)PrintDb.java 2014-7-17
 *
 */

package com.bolming.weather.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Wang Baoming
 *
 */
public class FootprintDbHelper extends SQLiteOpenHelper {
	private static final int DB_VERSION = 2;
	private static final String DB_NAME = "footprint_db";	
	
	private static final String FOOTPRINT_TABLE_NAME = "footprint";	 
	private static final String KEY_ROW_ID = "row_id"; 
	private static final String KEY_DATE = "date"; 
	private static final String KEY_LONGITUDE = "longitude"; 
	private static final String KEY_LATITUDE = "latitude";  
	private static final String KEY_CITY = "city"; 
	private static final String KEY_ICON_URL = "icon_url";
	private static final String SQL_CREATE_TABLE = "CREATE TABLE " + 
	    		FOOTPRINT_TABLE_NAME + " (" +
	    		KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
	    		KEY_DATE + " TEXT, " +
	    		KEY_LONGITUDE + " REAL, " +
	    		KEY_LATITUDE + " REAL, " +
	    		KEY_CITY + " TEXT, " +
	    		KEY_ICON_URL + " TEXT);";

	public FootprintDbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	public List<Footprint> query(){
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(FOOTPRINT_TABLE_NAME, 
				new String[]{KEY_ROW_ID, KEY_DATE, KEY_LONGITUDE, KEY_LATITUDE, KEY_CITY, KEY_ICON_URL}, 
				null, null, null, null, 
				String.format(" %s DESC ", KEY_DATE));
		
		if(!cursor.moveToFirst()) {
			cursor.close();
			db.close();
			return new ArrayList<Footprint>(0);
		}
		
		final int count = cursor.getCount();
		ArrayList<Footprint> footprints = new ArrayList<Footprint>(count); 
		for(int i = 0; i < count; i++){			
			Footprint footprint = new Footprint();
			footprint.setId(cursor.getInt(0));
			footprint.setDate(cursor.getString(1));
			footprint.setLongitude(cursor.getFloat(2));
			footprint.setLatitude(cursor.getFloat(3));
			footprint.setCity(cursor.getString(4));
			footprint.setIcon(cursor.getString(5));
			
			footprints.add(footprint);
			
			cursor.moveToNext();
		}
		
		return footprints;
	}
	
	public void insert(Footprint footprint){
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();	
//		values.put(KEY_ROW_ID, footprint.getId());	
		values.put(KEY_DATE, footprint.getDate());
		values.put(KEY_LONGITUDE, footprint.getLongitude());
		values.put(KEY_LATITUDE, footprint.getLatitude());
		values.put(KEY_CITY, footprint.getCity());
		values.put(KEY_ICON_URL, footprint.getIcon());
		
		db.insert(FOOTPRINT_TABLE_NAME, null, values);
	}
	
	public void delete(long id){
		SQLiteDatabase db = getWritableDatabase();
		db.delete(FOOTPRINT_TABLE_NAME, String.format("%s=%d", KEY_ROW_ID, id), null);
		db.close();
	}
}
