/*
 * @(#)WebServices.java 2014-7-8
 *
 */

package com.bolming.weather.webservice;

import com.bolming.weather.dao.MyLocation;
import com.bolming.weather.setting.Settings;

/**
 * @author Wang Baoming
 *
 */
public class WebServices {
	private final static String WEATHER = "http://api.wunderground.com/api/2755dad4ff1d72a3/conditions/lang:%s/q/%f,%f.%s";
	private final static String FORECAST = "http://api.wunderground.com/api/2755dad4ff1d72a3/forecast/lang:%s/q/%f,%f.%s";
	
	public enum Type {
		json, xml,
	}
	
	public static String getWeatherWebserviceUrl(Type type, MyLocation location){
		Settings settings = Settings.getInstance();
		String url = String.format(WEATHER, 
				settings.getLang(), location.latitude, location.longitude, type.toString());
		
		return  url;
	}
	
	public static String getForecastWebserviceUrl(Type type, MyLocation location){
		Settings settings = Settings.getInstance();
		String url = String.format(FORECAST, 
				settings.getLang(), location.latitude, location.longitude, type.toString());
		
		return  url;
	}
}
