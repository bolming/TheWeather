/**
 * 
 */
package com.bolming.weather.dao;

/**
 * @author Wang Baoming
 *
 */
public class CityWeather {
	private String mCity;
	private	int mTemperature;
	private String mWeather; // the weather description
	private String mIconUrl;
	private Wind mWind;	
	
	public void setCity(String city){
		mCity = city;
	}
	public void setTempr(int tempr){
		mTemperature = tempr;
	}
	public void setWeather(String weather){
		mWeather = weather;
	}	
	public void setIconUrl(String iconUrl){
		mIconUrl = iconUrl;
	}
	public void setWind(Wind wind){
		mWind = wind;		
	}
	
	public String getCity(){
		return mCity;
	}
	public int getTempr(){
		return mTemperature;
	}
	public String getWeather(){
		return mWeather;
	}
	public String getIconUrl(){
		return mIconUrl;
	}
	public Wind getWind(){
		return mWind;
	}
	
	public void show(){
		System.out.printf("city: %s, tempr:%d, weather:%s, iconUrl: %s\n", mCity, mTemperature, mWeather, mIconUrl);
		if(null != mWind) mWind.show();
	}
}


