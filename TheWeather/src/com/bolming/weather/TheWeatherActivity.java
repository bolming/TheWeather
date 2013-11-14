package com.bolming.weather;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bolming.weather.dao.CityWeather;
import com.bolming.weather.dao.CityWeatherXmlParser;
import com.bolming.weather.dao.Forecast;
import com.bolming.weather.dao.ForecastParser;
import com.bolming.weather.dao.IForecastDao;
import com.bolming.weather.dao.WeatherDao;
import com.bolming.weather.dao.Wind;

public class TheWeatherActivity extends Activity {
	private final static String Tag = "TheWeatherActivity";
	
	private TheWeatherActivity mContext;
	
	private WeatherDao mWeatherDao;
	private CityWeather mCityWeather;
	
	private IForecastDao mForecastDao;
	private List<Forecast> mForecastList;
	
	private CityWeatherParseAsyncTask mCityWeatherParseAsyncTask;
	
	private ListView mLstvForecast;
	private ForecastListViewAdapter mForecastListViewAdapter;
	
	private LinearLayout mWeatherWidget;
	private Bitmap mBmWeatherImg;

	private ProgressBar mPrgsBar;
	/**
	 * bit flags:
	 * PRGS_CURR_WEATHER_FINISH: current weather info has finished
	 * PRGS_FORECAST_FINISH: forecast info has finished 
	 */
	private int PRGS_CURR_WEATHER_FINISH = 1 << 0;
	private int PRGS_FORECAST_FINISH = 1 << 1;
	private int mProgress = 0;	
	
	private LocationManager mLocationManager;
	
	private final static int 	MSG_PULL_FORECAST_FINISH = 1;
	private final static int 	MSG_PULL_FORECAST_BEGIN = 2;
	private final Handler mUiHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case MSG_PULL_FORECAST_BEGIN:
				showProgress(true, -1);
				break;
			case MSG_PULL_FORECAST_FINISH:
				showProgress(false, PRGS_FORECAST_FINISH);
				mForecastListViewAdapter.notifyDataSetChanged();
				break;
			}
		};
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mContext = this;

		mPrgsBar = new ProgressBar(mContext);
        
        mWeatherDao = new CityWeatherXmlParser();
//        mWeatherDao = new CityWeatherJsonParser();  
        mForecastDao = new ForecastParser();
        
        mForecastList = new ArrayList<Forecast>();
        mForecastListViewAdapter = new ForecastListViewAdapter();
        
        getView();		

        init();  
    }    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.weather_main_menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case R.id.wheather_main_cancel:
			
			break;
		case R.id.wheather_main_updateLocation:
			if(updateLocation()) {
				sendBroadcast(new Intent(TheWeatherAppWidgetProvider.MY_ACTION_UPDATE_WIDGETS));
				if(null != mCityWeatherParseAsyncTask){
					mCityWeatherParseAsyncTask.cancel(true);
				}
				mCityWeatherParseAsyncTask = new CityWeatherParseAsyncTask();
				mCityWeatherParseAsyncTask.execute(mWeatherDao);
				
				// TODO get the forecasts
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						mUiHandler.sendEmptyMessage(MSG_PULL_FORECAST_BEGIN);
						getForecastInfo();
						mUiHandler.sendEmptyMessage(MSG_PULL_FORECAST_FINISH);
					}
				}).start();
			}
			break;
		default:
	    	return super.onOptionsItemSelected(item);
		}
    	
    	return true;
    }
    
    @Override
    protected void onDestroy() {
    	if(null != mCityWeatherParseAsyncTask){
			mCityWeatherParseAsyncTask.cancel(true);
		}
    	
    	super.onDestroy();
    }
    
    private boolean updateLocation(){
    	mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	if(null == mLocationManager){
    		Toast.makeText(mContext, "error: do not support lacation service", Toast.LENGTH_SHORT).show();
    		Log.e(Tag, "error: do not support lacation service");
//    		finish();
    		return false;
    	}
    	
    	Location location = getLocationByGsp();
    	if(null != location && saveLocation(location)) {    		
    		return true;
    	}
    	location = getLocationByNetwork();
    	if(null != location && saveLocation(location)) {
    		return true;
    	}

		Toast.makeText(mContext, "error: update failed", Toast.LENGTH_SHORT).show();
		Log.e(Tag, "error: update failed");
    	return false;
    }
    
    private boolean saveLocation(Location location){
		Log.d(Tag, "latiude: " + location.getLatitude() + ", longitude: " + location.getLongitude());
		Util.saveLocation(mContext, location);
		
    	return true;
    }
    
    private Location getLocationByGsp(){
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
		criteria.setAltitudeRequired(false); // 不要求海拔
		criteria.setBearingRequired(false); // 不要求方位
		criteria.setCostAllowed(true); // 允许有花费
		criteria.setPowerRequirement(Criteria.POWER_LOW);// 低功耗

		// 从可用的位置提供器中，匹配以上标准的最佳提供器
		String provider = mLocationManager.getBestProvider(criteria, true);

		// 获得最后一次变化的位置
		Location location = mLocationManager.getLastKnownLocation(provider); 
    	return location;
    }
    
    private Location getLocationByNetwork(){
    	return mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }
    
    private void getView(){
    	mWeatherWidget = (LinearLayout) findViewById(R.id.main_weather_widget);
    	mPrgsBar = (ProgressBar) findViewById(R.id.main_prgsBar);
    	
    	mLstvForecast = (ListView) findViewById(R.id.main_list_forcast);
    }
    
    private void init(){
		mPrgsBar.setIndeterminate(true);
		mLstvForecast.setAdapter(mForecastListViewAdapter);
        
        mCityWeatherParseAsyncTask = new CityWeatherParseAsyncTask();  
		mCityWeatherParseAsyncTask.execute(mWeatherDao);
		
		// TODO get the forecasts
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				mUiHandler.sendEmptyMessage(MSG_PULL_FORECAST_BEGIN);
				getForecastInfo();
				mUiHandler.sendEmptyMessage(MSG_PULL_FORECAST_FINISH);
			}
		}).start();
    }
    
    private void getForecastInfo(){
    	mForecastDao.requestThenParseData(Util.readLocation(mContext));
    	mForecastList = mForecastDao.getForecasts();
    }
    
    private void updateWidget(CityWeather weather, Bitmap weatherImg){
    	TextView tvCity = (TextView) mWeatherWidget.findViewById(R.id.weather_widget_tv_city);
    	TextView tvWeather = (TextView) mWeatherWidget.findViewById(R.id.weather_widget_tv_weather);
    	TextView tvTmpr = (TextView) mWeatherWidget.findViewById(R.id.weather_widget_tv_tmpt);
    	TextView tvWindDir = (TextView) mWeatherWidget.findViewById(R.id.weather_widget_tv_windDir);
    	TextView tvWindSpeed = (TextView) mWeatherWidget.findViewById(R.id.weather_widget_tv_windSpeed);
    	
    	// set the weather info
    	tvCity.setText(weather.getCity());
    	tvWeather.setText(weather.getWeather());
    	tvTmpr.setText(String.valueOf(weather.getTempr()) + "℃");
    	Wind wind = weather.getWind();
    	tvWindDir.setText(wind.getDir());
    	tvWindSpeed.setText(String.valueOf(wind.getSpeed()) + "km/h");    	

    	// set weather img
    	ImageView rlWeatherIcon = (ImageView) mWeatherWidget.findViewById(R.id.weather_widget_imgv_weatherIcon);
        if(null != weatherImg) rlWeatherIcon.setImageBitmap(weatherImg);
    }
    
    private Bitmap loadWeatherImg(String urlStr){
    	
        Bitmap bm = Util.loadImage(urlStr);        
		return bm;
    }
    
    private void showProgress(boolean show, int bit){
    	if(show) {
    		mPrgsBar.setVisibility(View.VISIBLE);
    	}else{
        	mProgress |= bit; 
        	if(PRGS_CURR_WEATHER_FINISH == (mProgress & PRGS_CURR_WEATHER_FINISH) && 
        			PRGS_FORECAST_FINISH == (mProgress & PRGS_FORECAST_FINISH)){
            	mPrgsBar.setVisibility(View.GONE);
            	mProgress = 0;
        	}
    	}
    }
    
    private class CityWeatherParseAsyncTask extends AsyncTask<WeatherDao, Integer, Boolean>{
    	
    	public CityWeatherParseAsyncTask() {
		}
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();    	
    		showProgress(true, -1);
    	}

		@Override
		protected Boolean doInBackground(WeatherDao... params) {
			WeatherDao weatherDao = params[0];
			weatherDao.parse(Util.readLocation(mContext));
	        mCityWeather = weatherDao.getCityWeather();
	        if(null == mCityWeather) return false;
	        mCityWeather.show();
	        mBmWeatherImg = loadWeatherImg(mCityWeather.getIconUrl());
	        
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
	        if(result) updateWidget(mCityWeather, mBmWeatherImg);		        
	        
    		showProgress(false, PRGS_CURR_WEATHER_FINISH);
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
//    		showProgress(false);
			Log.d(Tag, "parsing is cancelled");
		}
    	
    }
    
    private class ForecastListViewAdapter extends BaseAdapter{
    	private class ForecastListViewItemHolder{
    		public ImageView imgvIcon;
    		public TextView tvPeriod;
    		public TextView tvForecast;
    	}

		@Override
		public int getCount() {
			return mForecastList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mForecastList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			View v = arg1;
			ForecastListViewItemHolder holder = null;
			
			if(null == v){
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.view_forcast_list_item, null, false);
				
				holder = new ForecastListViewItemHolder();
				holder.imgvIcon = (ImageView) v.findViewById(R.id.forcast_list_item_img_icon);
				holder.tvPeriod = (TextView) v.findViewById(R.id.forcast_list_item_tv_period);
				holder.tvForecast = (TextView) v.findViewById(R.id.forcast_list_item_tv_forcast);
				
				v.setTag(holder);
			}else{
				holder = (ForecastListViewItemHolder) v.getTag();
			}
			
			Forecast forecast = mForecastList.get(arg0);
			Bitmap bm = forecast.getIcon();
			if(null != bm) holder.imgvIcon.setImageBitmap(bm);
			holder.tvPeriod.setText(forecast.getPeriod());
			holder.tvForecast.setText(forecast.getForecast());
			
			return v;
		}
    	
    }
}