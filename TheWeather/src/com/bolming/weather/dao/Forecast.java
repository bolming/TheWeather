package com.bolming.weather.dao;

public class Forecast {
	
	private String mPeriod;
	private String mIconUrl;
	private String mForecast;
	
	public Forecast() {
	}
	
	public String getmPeriod() {
		return mPeriod;
	}
	public void setmPeriod(String mPeriod) {
		this.mPeriod = mPeriod;
	}
	public String getmIconUrl() {
		return mIconUrl;
	}
	public void setmIconUrl(String mIconUrl) {
		this.mIconUrl = mIconUrl;
	}
	public String getmForecast() {
		return mForecast;
	}
	public void setmForecast(String mForecast) {
		this.mForecast = mForecast;
	}
}
