package com.bolming.weather.dao;

public interface WeatherDao {
	public CityWeather getCityWeather();
	public void parse(MyLocation location);
}
