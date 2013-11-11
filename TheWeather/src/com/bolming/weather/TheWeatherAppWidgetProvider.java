package com.bolming.weather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.bolming.weather.dao.CityWeather;
import com.bolming.weather.dao.CityWeatherXmlParser;
import com.bolming.weather.dao.WeatherDao;
import com.bolming.weather.dao.Wind;

public class TheWeatherAppWidgetProvider extends AppWidgetProvider {
	public final static String MY_ACTION_UPDATE_WIDGETS = "com.bolming.weather.intent.action.APPWIDGET_UPDATE";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		UpdateWidgetThread updateWidgetThread = new UpdateWidgetThread(context, appWidgetManager, appWidgetIds);
		updateWidgetThread.start();
		
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final String act = intent.getAction();
		if(MY_ACTION_UPDATE_WIDGETS.equals(act)){
			AppWidgetManager awm = AppWidgetManager.getInstance(context);

			int[] ids = awm.getAppWidgetIds(new ComponentName(context, TheWeatherAppWidgetProvider.class));
			onUpdate(context, awm, ids);
			
			return;
		}
		super.onReceive(context, intent);
	}
	
	private Bitmap loadWeatherImg(String urlStr) {

		Bitmap bm = Util.loadImage(urlStr);
		
		return bm;
	}
	
	private void updateWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, CityWeather weather, Bitmap weatherImg){
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
		
		if(null != weather){
	    	// set the weather info
	    	views.setTextViewText(R.id.weather_widget_tv_city, weather.getCity());
	    	views.setTextViewText(R.id.weather_widget_tv_weather, weather.getWeather());
	    	views.setTextViewText(R.id.weather_widget_tv_tmpt, String.valueOf(weather.getTempr()) + "¡æ");
	    	Wind wind = weather.getWind();
	    	views.setTextViewText(R.id.weather_widget_tv_windDir, wind.getDir());
	    	views.setTextViewText(R.id.weather_widget_tv_windSpeed, String.valueOf(wind.getSpeed()) + "km/h");    	
		
		}
		
    	// set weather img
        if(null != weatherImg) 
        	views.setBitmap(R.id.weather_widget_imgv_weatherIcon, "setImageBitmap", weatherImg);
        
        Intent intent = new Intent(context, TheWeatherActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.weather_widget_imgbtn_switch, pendingIntent);
        
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }
	
	private final class UpdateWidgetThread extends Thread{
		private int[] mWidgetIds;
		private WeatherDao mWeatherDao;
		
		private Context mContext;
		private AppWidgetManager mAppWidgetMgr;
		
		public UpdateWidgetThread(Context c, AppWidgetManager appWidgetManager, int[] widgetIds) {
			mWidgetIds = widgetIds;
			mWeatherDao = new CityWeatherXmlParser();
			
			mContext = c;
			mAppWidgetMgr = appWidgetManager;
		}
		
		@Override
		public void run() {
			super.run();
			mWeatherDao.parse(Util.readLocation(mContext));
			CityWeather cityWeather = mWeatherDao.getCityWeather();
			Bitmap bm = null;
			if(null != cityWeather)  {
				cityWeather.show();
				bm = loadWeatherImg(cityWeather.getIconUrl());
			}
			
			updateWidget(mContext, mAppWidgetMgr, mWidgetIds, cityWeather, bm);
		}
	}
}
