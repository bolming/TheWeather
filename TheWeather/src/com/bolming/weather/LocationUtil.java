package com.bolming.weather;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.bolming.weather.conf.Constants;
import com.bolming.weather.dao.MyLocation;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;


public class LocationUtil {
	 public static boolean saveLocation(Context c, Location location){
			System.out.printf("latitude: %f, longigude: %f\n", location.getLatitude(), location.getLongitude());
			SharedPreferences sps = c.getSharedPreferences(Constants.SHARED_PREFRENCES_LOCATION, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sps.edit();
			editor.putFloat(Constants.SHARED_PREFRENCES_LOCATION_KEY_LATITUDE, 
					(float) location.getLatitude());
			editor.putFloat(Constants.SHARED_PREFRENCES_LOCATION_KEY_LONGITUDE, 
					(float) location.getLongitude());
			editor.commit();
			
	    	return true;
	    }
	 
	 public static MyLocation readLocation(Context c){
			SharedPreferences sps = c.getSharedPreferences(Constants.SHARED_PREFRENCES_LOCATION, Context.MODE_PRIVATE);
			MyLocation location = new MyLocation();
			final int defv = -360;
			location.latitude = sps.getFloat(Constants.SHARED_PREFRENCES_LOCATION_KEY_LATITUDE, defv);
			location.longitude = sps.getFloat(Constants.SHARED_PREFRENCES_LOCATION_KEY_LONGITUDE, defv);
			if(defv == location.latitude || defv == location.longitude){
				location = null;
			}
			
	    	return location;
	    }
}
