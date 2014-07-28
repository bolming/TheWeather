package com.bolming.weather.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.bolming.weather.conf.Debug;
import com.bolming.weather.webservice.WebServices;
import com.bolming.weather.webservice.WebServices.Type;

public class CityWeatherJsonParser implements WeatherDao{
	private final static String TARGET_JSON = "/mnt/sdcard/theweather/debug/31.23,120.57.json";
	
	private final static String NAME_OBSERVATION = "current_observation";
	private final static String NAME_LOCATION = "display_location";
	private final static String NAME_CITY = "full";
	private final static String NAME_WEATHER = "weather";
	private final static String NAME_TEMPR = "temp_c";
	private final static String NAME_WIND_DIR = "wind_dir";
	private final static String NAME_WIND_SPEED = "wind_kph";
	private final static String NAME_ICON_URL = "icon_url";
	
	private CityWeather mCityWeather;
	private Wind mWind;
	
	public CityWeatherJsonParser() {
	}
	
	@Override
	public CityWeather getCityWeather() {
		return mCityWeather;
	}
	
	public void parse(MyLocation mylocation){
		try {
			char[] buffer = new char[512];
			StringBuilder sb = new StringBuilder();
			
			if(Debug.DATA_LOCAL){
				File jsonFile = new File(TARGET_JSON);
				BufferedReader br = new BufferedReader(new FileReader(jsonFile));
				while(-1 != br.read(buffer)){
					sb.append(buffer);
				}
			}else{				
				URL url = new URL(WebServices.getWeatherWebserviceUrl(Type.json, mylocation));
				HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
				httpConn.setConnectTimeout(1000 * 10);
				httpConn.setReadTimeout(1000 * 5);
				if(HttpURLConnection.HTTP_OK != httpConn.getResponseCode()) return;

				mCityWeather = new CityWeather();
				mWind = new Wind();
				
				InputStreamReader insr = new InputStreamReader(httpConn.getInputStream());
				while(-1 != insr.read(buffer)){
					sb.append(buffer);
				}
			}
			
			String json = sb.toString();
			JSONObject jsonObj = new JSONObject(json);
			JSONObject observation = jsonObj.getJSONObject(NAME_OBSERVATION);
			
			JSONObject location = observation.getJSONObject(NAME_LOCATION);
			String city = location.getString(NAME_CITY);
			String weather = observation.getString(NAME_WEATHER);
			int tempr = observation.getInt(NAME_TEMPR); // C
			String windDir = observation.getString(NAME_WIND_DIR);
			int windSpeed = observation.getInt(NAME_WIND_SPEED);
			String iconUrl = observation.getString(NAME_ICON_URL);
			System.out.printf("city:%s, weather:%s, tmpr:%d, windDir:%s, windSpeed:%d, iconUrl:%s\n", 
					city, weather, tempr, windDir, windSpeed, iconUrl);
		
			mCityWeather.setCity(city);
			mCityWeather.setWeather(weather);
			mCityWeather.setTempr(tempr);
			mWind.setDir(windDir);
			mWind.setSpeed(windSpeed);
			mCityWeather.setWind(mWind);
			mCityWeather.setIconUrl(iconUrl);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
