package com.bolming.weather.dao;

import android.graphics.Bitmap;

public class Forecast {
	
	private String mPeriod;
	private String mIconUrl;
	private Bitmap mIcon;
	public Bitmap getIcon() {
		return mIcon;
	}

	public void setIcon(Bitmap mIcon) {
		this.mIcon = mIcon;
	}
	private String mForecast;
	
	public Forecast() {
	}
	
	public String getPeriod() {
		return mPeriod;
	}
	public void setPeriod(String mPeriod) {
		this.mPeriod = mPeriod;
	}
	public String getIconUrl() {
		return mIconUrl;
	}
	public void setIconUrl(String mIconUrl) {
		this.mIconUrl = mIconUrl;
	}
	public String getForecast() {
		return mForecast;
	}
	public void setForecast(String mForecast) {
		this.mForecast = mForecast;
	}
}
