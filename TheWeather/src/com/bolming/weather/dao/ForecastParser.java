package com.bolming.weather.dao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bolming.weather.conf.Debug;

import android.util.Log;

public class ForecastParser implements IForecastDao{

	private final static String XML_FORECAST_TARGET = "/mnt/sdcard/theweather/debug/31.23,120.57.xml";
	private final static String XML_FORECAST_TARGET_URL = "http://api.wunderground.com/api/2755dad4ff1d72a3/forecast/lang:CN/q/%f,%f.xml";
	
	private ForecastParserHandler mForecastParserHandler;
	private SAXParser mXmlParser;
	
	public ForecastParser() {

		SAXParserFactory saxParseFacttory = SAXParserFactory.newInstance();
		try {
			mXmlParser = saxParseFacttory.newSAXParser();
			
			mForecastParserHandler = new ForecastParserHandler();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public List<Forecast> getForecasts() {
		return mForecastParserHandler.getForecastList();
	}

	@Override
	public void requestThenParseData(MyLocation location) {
		try {
			if (Debug.DATE_LOCAL) {
				mXmlParser.parse(new File(XML_FORECAST_TARGET),	mForecastParserHandler);
				return;
			}

			if (null == location) location = MyLocation.DEF_LOCATION;
			URL url = new URL(String.format(XML_FORECAST_TARGET_URL, location.latitude, location.longitude));
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setConnectTimeout(1000 * 5);
			httpConn.setReadTimeout(1000 * 5);
			if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStream ins = httpConn.getInputStream();
				mXmlParser.parse(ins, mForecastParserHandler);
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class ForecastParserHandler extends DefaultHandler{	
	private static final String TAG = "CDataParserHandler";
	
	private final static String TAG_PERIOD = "title";
	private final static String TAG_ICON_URL = "icon_url";
	private final static String TAG_FORECAST = "fcttext_metric";

	private final static String TAG_FORECAST_ITEM = "forecastday";
	
	private final static int FORECAST_COUNT = 6 + 1;

	private final static String[] DOM_TREE_FORECASTLIST_TRACE = {
		"response", "forecast", "txt_forecast", "forecastdays"
	};
	
	private Stack<String> mTagStack;
	
	private StringBuilder mStrBuilder;
	
	private List<Forecast> mForecastList;
	private Forecast mForecast;
	
	public ForecastParserHandler() {
		mTagStack = new Stack<String>();
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);		
		mStrBuilder.append(ch, start, length);
	}
	
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		mForecastList = new ArrayList<Forecast>(FORECAST_COUNT);
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		mStrBuilder = new StringBuilder();
		mTagStack.push(localName);
		System.out.printf("push: %s\n", localName);
		

		if(TAG_FORECAST_ITEM.equals(localName)){
			mForecast = new Forecast();
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		String poppedTag = mTagStack.pop();
		System.out.printf("pop: %s\n", poppedTag);	
		Log.e(TAG, "size: " + mTagStack.size() + 
				", last:" + (mTagStack.size() > 0 ? mTagStack.peek() : "null") + 
				", reverse second:" + (mTagStack.size() >= 2 ? mTagStack.elementAt(mTagStack.size() - 2) : "null"));
		
		if(mTagStack.size() < DOM_TREE_FORECASTLIST_TRACE.length) {
			return ;
		}
		for(int i = DOM_TREE_FORECASTLIST_TRACE.length - 1; i > 0; i--){ // check begins from the reverse first.
			if(!mTagStack.elementAt(i).equals(DOM_TREE_FORECASTLIST_TRACE[i])){
				return ;
			}
		}
		
		if(TAG_FORECAST_ITEM.equals(localName)){
			mForecastList.add(mForecast);
			System.out.printf("period: %s, icon: %s, forcast: %s\n", 
					mForecast.getmPeriod(), mForecast.getmIconUrl(), mForecast.getmForecast());
			mForecast = null;
			return ;
		}
		
		final String value = mStrBuilder.toString();
		if(TAG_PERIOD.equals(localName)){
			mForecast.setmPeriod(value);
		}else if(TAG_ICON_URL.equals(localName)){
			mForecast.setmIconUrl(value);
		}else if(TAG_FORECAST.equals(localName)){
			mForecast.setmForecast(value);
		}
	}
	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}
	
	public List<Forecast> getForecastList(){
		return mForecastList;
	}
}
