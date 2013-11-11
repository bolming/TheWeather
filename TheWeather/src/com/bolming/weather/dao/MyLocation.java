package com.bolming.weather.dao;

public class MyLocation {
	public final static MyLocation DEF_LOCATION = new MyLocation(31.39f,120.95f);

	private MyLocation(float latitude, float longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	public MyLocation() {
	}
	public float latitude;
	public float longitude;
}