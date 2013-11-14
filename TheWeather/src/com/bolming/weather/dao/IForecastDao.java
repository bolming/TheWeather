package com.bolming.weather.dao;

import java.util.List;

public interface IForecastDao {
	public List<Forecast> getForecasts();
	public void requestThenParseData(MyLocation location);
}
