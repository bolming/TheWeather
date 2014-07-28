/**
 * 
 */
package com.bolming.weather;

import com.bolming.common.BitmapCache;
import com.bolming.weather.setting.Settings;

import android.app.Application;

/**
 * @author Wang Baoming
 *
 */
public class MyApplication extends Application {
	
	public MyApplication() {
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		BitmapCache.newInstance(this);
		Settings.newInstance(this);
	}
	
	@Override
	public void onLowMemory() {		
		super.onLowMemory();
		System.gc();
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}
}
