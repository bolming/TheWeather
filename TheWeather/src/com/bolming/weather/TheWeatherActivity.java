package com.bolming.weather;

import java.lang.ref.WeakReference;
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
import android.os.Message;
import android.os.SystemClock;
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

import com.bolming.common.BitmapCache;
import com.bolming.weather.dao.CityWeather;
import com.bolming.weather.dao.CityWeatherXmlParser;
import com.bolming.weather.dao.Forecast;
import com.bolming.weather.dao.ForecastParser;
import com.bolming.weather.dao.IForecastDao;
import com.bolming.weather.dao.MyLocation;
import com.bolming.weather.dao.WeatherDao;
import com.bolming.weather.dao.Wind;

public class TheWeatherActivity extends Activity {
	private final static String Tag = "TheWeatherActivity";
	
	private TheWeatherActivity mContext;
	
	private WeatherDao mWeatherDao;
	private CityWeather mCityWeather;
	
	private List<Forecast> mForecastList;
	private GetForecastInfoThread mGetForecastInfoThread;
	
	private CityWeatherParseAsyncTask mCityWeatherParseAsyncTask;
	
	private ListView mLstvForecast;
	private ForecastListViewAdapter mForecastListViewAdapter;
	
	private LinearLayout mWeatherWidget;
	private Bitmap mBmWeatherImg;

	private ProgressBar mPrgsBar;
	
	private final static int PRGS_CURR_WEATHER_FINISH = 1 << 0;
	private final static int PRGS_FORECAST_FINISH = 1 << 1;
	/**
	 * bit flags:<br>
	 * PRGS_CURR_WEATHER_FINISH: current weather info has finished<br>
	 * PRGS_FORECAST_FINISH: forecast info has finished 
	 */
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
			case MSG_PULL_FORECAST_FINISH:{

				showProgress(false, PRGS_FORECAST_FINISH);
				if(null != msg.obj){
					mForecastList = (List<Forecast>) msg.obj;					
				}

				mForecastListViewAdapter.notifyDataSetChanged();
				break;
				}
			}
		};
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mContext = this;
        
        mWeatherDao = new CityWeatherXmlParser();
//        mWeatherDao = new CityWeatherJsonParser(); 
        
        mGetForecastInfoThread = new GetForecastInfoThread(mContext);
        mForecastList = new ArrayList<Forecast>(0);
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
				mCityWeatherParseAsyncTask = new CityWeatherParseAsyncTask(mContext);
				mCityWeatherParseAsyncTask.execute(mWeatherDao);
				
				// get the forecasts
				if(mGetForecastInfoThread.hasDied()){
					mGetForecastInfoThread = new GetForecastInfoThread(mContext);
					mGetForecastInfoThread.start();
				}
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
		LocationUtil.saveLocation(mContext, location);
		
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
        
        mCityWeatherParseAsyncTask = new CityWeatherParseAsyncTask(mContext);  
		mCityWeatherParseAsyncTask.execute(mWeatherDao);
		
		// get the forecasts
		mGetForecastInfoThread.start();
    }
    
    private void updateWidget(CityWeather weather, Bitmap weatherImg){
    	// get the views
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
    	
        Bitmap bm = BitmapCache.getInstance().getBitmap(urlStr);        
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
    
    private static final class CityWeatherParseAsyncTask extends AsyncTask<WeatherDao, Integer, Boolean>{
    	
    	private final WeakReference<TheWeatherActivity> mWeakRefActivity;
    	
    	public CityWeatherParseAsyncTask(TheWeatherActivity activity) {
    		mWeakRefActivity = new WeakReference<TheWeatherActivity>(activity);
		}
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();  
    		TheWeatherActivity activity = mWeakRefActivity.get();
    		if(null != activity){
    			activity.showProgress(true, -1);
    		}
    	}

		@Override
		protected Boolean doInBackground(WeatherDao... params) {
			WeatherDao weatherDao = params[0];
			
			TheWeatherActivity activity = mWeakRefActivity.get();
			if(null == activity) return false;
			MyLocation location = LocationUtil.readLocation(activity);
			activity = null;
			
			// a long term process
			weatherDao.parse(location);
			
			activity = mWeakRefActivity.get();
			if(null == activity) return false;
			activity.mCityWeather = weatherDao.getCityWeather();
	        
	        if(null == activity.mCityWeather) return false;
	        activity.mCityWeather.show();
	        activity.mBmWeatherImg = activity.loadWeatherImg(activity.mCityWeather.getIconUrl());
	        activity = null;
	        
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			TheWeatherActivity activity = mWeakRefActivity.get();
			if(null == activity) return;
			
	        if(result) activity.updateWidget(activity.mCityWeather, activity.mBmWeatherImg);	        
	        activity.showProgress(false, PRGS_CURR_WEATHER_FINISH);
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
    
    private static final class GetForecastInfoThread extends Thread{

    	private IForecastDao mForecastDao;
    	
    	private final WeakReference<TheWeatherActivity> mWeakRefActity;
    	
    	private boolean mIsDead = false;
    	
    	public GetForecastInfoThread(TheWeatherActivity activity) {
    		mWeakRefActity = new WeakReference<TheWeatherActivity>(activity);  
    		
            mForecastDao = new ForecastParser();
		}
    	
    	@Override
    	public void run() {
    		sendEmptyMessage(MSG_PULL_FORECAST_BEGIN);
			List<Forecast> data = getForecastInfo();
			sendMessage(MSG_PULL_FORECAST_FINISH, -1, -1, data);
			mIsDead = true;
    	}    	
    	
    	private void sendEmptyMessage(int what){
    		sendMessage(what, -1, -1, null);
    	}
    	
    	private void sendMessage(int what, int arg1, int arg2, Object obj){
    		TheWeatherActivity activity = mWeakRefActity.get();
    		if(null != activity){
    			Message msg = activity.mUiHandler.obtainMessage();
    			msg.what = what;
    			msg.obj = obj;
    			msg.arg1 = arg1;
    			msg.arg2 = arg2;
    			activity.mUiHandler.sendMessage(msg);
    		}
    	}
    	
    	private List<Forecast> getForecastInfo(){
    		TheWeatherActivity activity = mWeakRefActity.get();
    		if(null != activity){
    			MyLocation location = LocationUtil.readLocation(activity);
    			activity = null;
            	mForecastDao.requestThenParseData(location);
    		}
    		
        	return mForecastDao.getForecasts();
        }
    	
    	public boolean hasDied(){
    		return mIsDead;
    	}
    }
}