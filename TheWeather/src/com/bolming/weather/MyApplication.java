/**
 * 
 */
package com.bolming.weather;

import com.bolming.common.BitmapCache;

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
