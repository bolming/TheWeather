/*
 * @(#)Setting.java 2014-7-8
 *
 */
package com.bolming.weather.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;

import com.bolming.weather.dao.MyLocation;

/**
 * @author Wang Baoming
 *
 */
public class Settings {
	private final static String PREF_SETTINGS = "app_settings";	
	/* location */
	public final static String PREF_SETTINGS_LATITUDE = "latitude";
	public final static String PREF_SETTINGS_LONGITUDE = "longitude";
	/* lang */
	public final static String PREF_SETTINGS_LANG = "lang";	
	public final static String PREF_SETTINGS_LANG_EN = "EN";	
	public final static String PREF_SETTINGS_LANG_CN = "CN";	
	
	private SharedPreferences mSettingsPre;
	private Editor mSettingEditor;
		
	public enum Lang {
		EN, CN,
	}
	private Lang mLang = Lang.EN;
	
	private MyLocation mCurrLocation;
	
	private static Settings mInstance;
	private Settings(Context c) {
		mSettingsPre = c.getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
		mSettingEditor = mSettingsPre.edit();
		
		readLang();
		readLocation();
	}
	public synchronized static void newInstance(Context c){
		if(null == mInstance){
			mInstance = new Settings(c);
		}
	}	
	public static Settings getInstance(){
		return mInstance;
	}
	
	public Lang getLang(){
		return mLang;
	}
	
	public MyLocation getCurrLocation(){
		return mCurrLocation;
	}
	
	public void updateLang(Lang lang){
		mLang = lang;
		
		mSettingEditor.putString(PREF_SETTINGS_LANG, lang.toString());
		mSettingEditor.commit();
	}
	
	public void upateLocation(Location location){	
		mCurrLocation.latitude = (float) location.getLatitude();
		mCurrLocation.longitude = (float) location.getLongitude();
		
		mSettingEditor.putFloat(PREF_SETTINGS_LATITUDE, mCurrLocation.latitude);
		mSettingEditor.putFloat(PREF_SETTINGS_LONGITUDE, mCurrLocation.longitude);
		mSettingEditor.commit();
		
	}
	
	private void readLang(){
		final String lang = mSettingsPre.getString(PREF_SETTINGS_LANG, PREF_SETTINGS_LANG_EN);
		mLang = Lang.valueOf(lang);
	}
	
	private void readLocation(){
		mCurrLocation = new MyLocation();
		final int defv = -360;
		mCurrLocation.latitude = mSettingsPre.getFloat(PREF_SETTINGS_LATITUDE, defv);
		mCurrLocation.longitude = mSettingsPre.getFloat(PREF_SETTINGS_LONGITUDE, defv);
		if(defv == mCurrLocation.latitude || defv == mCurrLocation.longitude){
			mCurrLocation = MyLocation.DEF_LOCATION;
		}		
	}
	
}
