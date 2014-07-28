/*
 * @(#)Footprint.java 2014-7-17
 *
 */

package com.bolming.weather.db;


/**
 * represent one footprint
 * @author Wang Baoming
 *
 */
public class Footprint {
	
	private int mId;
	
	private String mCity;
	private String mDate;
	private float mLongitude, mLatitude;
	private String mIcon;
	
	public String getCity() {
		return mCity;
	}
	public void setCity(String city) {
		this.mCity = city;
	}
	public String getDate() {
		return mDate;
	}
	public void setDate(String date) {
		this.mDate = date;
	}
	public float getLongitude() {
		return mLongitude;
	}
	public void setLongitude(float longitude) {
		this.mLongitude = longitude;
	}
	public float getLatitude() {
		return mLatitude;
	}
	public void setLatitude(float latitude) {
		this.mLatitude = latitude;
	}
	public String getIcon() {
		return mIcon;
	}
	public void setIcon(String icon) {
		this.mIcon = icon;
	}
	public int getId() {
		return mId;
	}
	public void setId(int id) {
		this.mId = id;
	}
}
