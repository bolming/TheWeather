/**
 * 
 */
package com.bolming.weather.dao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.bolming.weather.conf.Debug;
import com.bolming.weather.webservice.WebServices;
import com.bolming.weather.webservice.WebServices.Type;

/**
 * @author Wang Baoming
 *
 */
public class CityWeatherXmlParser implements WeatherDao{
	private final static String TARGET_XML = "/mnt/sdcard/theweather/debug/31.23,120.57.xml";
			
	private SAXParser mXmlParser;
	private CityWeatherXmlHandler mXmlParseHandler;
	
	public CityWeatherXmlParser() {
		SAXParserFactory saxParseFacttory = SAXParserFactory.newInstance();
		try {
			mXmlParser = saxParseFacttory.newSAXParser();
			mXmlParseHandler = new CityWeatherXmlHandler();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
	
	public void parse(MyLocation location){
		try {
			if(Debug.DATA_LOCAL){
				mXmlParser.parse(new File(TARGET_XML), mXmlParseHandler);
				return ;
			}

			URL url = new URL(WebServices.getWeatherWebserviceUrl(Type.xml, location));
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setConnectTimeout(1000 * 5);
			httpConn.setReadTimeout(1000 * 5);
			if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK){
				InputStream ins = httpConn.getInputStream();
				mXmlParser.parse(ins, mXmlParseHandler);
			}

/*		
			HttpClient httpClient = new DefaultHttpClient();
			HttpParams params = httpClient.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 1000 * 5);
			HttpConnectionParams.setSoTimeout(params, 1000 * 5);
			HttpGet get = new HttpGet(TARGET_XML_URL);
			HttpResponse response = httpClient.execute(get);

			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				HttpEntity entity = response.getEntity();
				InputStream ins = entity.getContent();
				mXmlParser.parse(ins, mXmlParseHandler);
			}
*/		
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public CityWeather getCityWeather() {
		return mXmlParseHandler.getCityWeather();
	}

}

class CityWeatherXmlHandler extends DefaultHandler {
	private final static String Tag = "CityWeatherXmlHandler";
	
	private final static String TAG_CITY = "full";
	private final static String TAG_WEATHER = "weather";
	private final static String TAG_TEMPR = "temp_c";
	private final static String TAG_WIND_DIR = "wind_dir";
	private final static String TAG_WIND_SPEED = "wind_kph";
	
	private final static String TAG_DISPLAY_LOCATION = "display_location";	
	private final static String TAG_ICON_URL = "icon_url";
	
	private Stack<String> mTagStack;
	
	private StringBuilder mStrBuiler;
	private CityWeather mCityWeather;
	private Wind mWind;
	
	public CityWeatherXmlHandler() {
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		mStrBuiler.append(ch, start, length);
	}
	
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		mCityWeather = new CityWeather();	
		mWind = new Wind();
		mTagStack = new Stack<String>();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		mStrBuiler = new StringBuilder();
		mTagStack.push(localName);		
		System.out.printf("push: %s\n", localName);		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		
		String poppedTag = mTagStack.pop();
		System.out.printf("pop: %s\n", poppedTag);	
		
		final String value = mStrBuiler.toString();
		if(TAG_CITY.equals(localName)){
			if(TAG_DISPLAY_LOCATION.equals(mTagStack.peek())) mCityWeather.setCity(value);
		}else if(TAG_WEATHER.equals(localName)){
			mCityWeather.setWeather(value);
		}else if(TAG_TEMPR.equals(localName)){
			try {
				mCityWeather.setTempr(Integer.valueOf(value));
			} catch (NumberFormatException e) {
				Log.e(Tag, "error: the formate of the temperature string is invalid");
				e.printStackTrace();
			}
		}else if(TAG_ICON_URL.equals(localName)){
			mCityWeather.setIconUrl(value);
		}else if(TAG_WIND_DIR.equals(localName)){
			mWind.setDir(value);
		}else if(TAG_WIND_SPEED.equals(localName)){
			try {
				mWind.setSpeed(Integer.valueOf(value));
			} catch (NumberFormatException e) {
				Log.e(Tag, "error: the formate of the wind speed string is invalid");
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		mCityWeather.setWind(mWind);
	}

	public CityWeather getCityWeather() {
		return mCityWeather;
	}
}
