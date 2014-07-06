/**
 * 
 */
package com.bolming.common;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author Wang Baoming
 *
 */
public class BitmapUtil {
	
	 public static Bitmap loadImage(String urlStr){
		 Bitmap bm = null;
		 /*
	    	try {
		        URL url = new URL(urlStr);
		        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		        conn.setConnectTimeout(5 * 1000);        
		        conn.setRequestMethod("GET");
		        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
		            InputStream is = conn.getInputStream();		        
			        bm = BitmapFactory.decodeStream(is);
		        }
			} catch (IOException e) {
				e.printStackTrace();
			}
	*/				 
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpParams params = httpclient.getParams();
	        HttpConnectionParams.setConnectionTimeout(params, 1000 * 5);
	        HttpConnectionParams.setSoTimeout(params, 1000 * 5);
	        HttpGet httpGet = new HttpGet(urlStr);
	        try {
				HttpResponse httpResponse= httpclient.execute(httpGet);
				HttpEntity entity = httpResponse.getEntity();			
	            InputStream is = entity.getContent();	        
		        bm = BitmapFactory.decodeStream(is);
		        is.close();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	        return bm;
	 }
}
