package com.bolming.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.WeakHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

/**
 * BitmapCache interface.
 * @author Wang Baoming
 *
 */
interface IBitmapCache{
	/**
	 * 
	 * @param key
	 * @return true if the bitmap the key indicates has bean cache, false otherwise
	 */
	boolean isCached(Object key);
	/**
	 * 
	 * @param key
	 * @return 
	 * the bitmap the key indicates, which if cached return it directly, 
	 * otherwise load the bitmap then return that.  
	 */
	Bitmap getBitmap(Object key);
	/**
	 * cache the bitmap, you can get the bitmap via the unique key. 
	 * @param bm
	 * @param key
	 */
	void cacheBitmap(Bitmap bm, Object key);
	/**
	 * clean all cached bitmap.
	 */
	void cleanCache();
}

/**
 * cache the bitmap. two levels cache caches the bitmaps.
 * the first is the mem cache, and the second is the local cache in package data dir.
 * @author Wang Baoming
 *
 */
public class BitmapCache implements IBitmapCache{
	
	private MemBitmapCache mMemCache;
	private LocalBitmapCashe mLocalCache;
	
	private static BitmapCache mInstance;
	private BitmapCache(Context c) {
		mMemCache = MemBitmapCache.getInstance();
		mLocalCache = LocalBitmapCashe.getInstance(c);
	}
	public synchronized static void newInstance(Context c){
		if(null == mInstance){
			mInstance = new BitmapCache(c);
		}
	}
	/**
	 * make sure the instance has been initialized by {@link BitmapCache#newInstance(Context) BitmapCache.newInstance(Context)}
	 * before calling {@link BitmapCache#getInstance() BitmapCache.getInstance()}
	 * @return
	 * {@link BitmapCache} instance
	 */
	public synchronized static BitmapCache getInstance(){		
		return mInstance;
	}

	/**
	 * true if either mem cache or local cache has cached the bitmap, false otherwise
	 */
	@Override
	public boolean isCached(Object key) {
		return mMemCache.isCached(key) || mLocalCache.isCached(key);
	}

	@Override
	public Bitmap getBitmap(Object key) {
		Bitmap bm = mMemCache.getBitmap(key);
		if(null == bm){
			bm = mLocalCache.getBitmap(key);
		}
		
		cacheBitmap(bm, key);
		return bm;
	}
	
	@Override
	public void cacheBitmap(Bitmap bm, Object key) {
		if(!mMemCache.isCached(key)) mMemCache.cacheBitmap(bm, key);
		if(!mLocalCache.isCached(key)) mLocalCache.cacheBitmap(bm, key);
	}

	@Override
	public void cleanCache() {
		mMemCache.cleanCache();		
		mLocalCache.cleanCache();
	}
	
}

/**
 * cache the bitmap in mem.
 * @author Wang Baoming
 *
 */
class MemBitmapCache implements IBitmapCache{
	private final static String TAG = "BitmapCache";
	private final static boolean DEBUG = true;
	
	private WeakHashMap<Object, Bitmap> mCache;
	
	private static MemBitmapCache mInstance;
	public synchronized static MemBitmapCache getInstance(){
		if(null == mInstance){
			mInstance = new MemBitmapCache();
		}
		
		return mInstance;
	}
	private MemBitmapCache() {
		mCache = new WeakHashMap<Object, Bitmap>();
	}

	/**
	 * return the cached bitmap if hit in the mem cache, otherwise load it from url.
	 */
	@Override
	public Bitmap getBitmap(Object key) {
		Bitmap bm = mCache.get(key);
		if(null == bm){			
			bm = BitmapUtil.loadImage((String) key);
		}
		
		return bm;
	}

	@Override
	public void cacheBitmap(Bitmap bm, Object key) {
		mCache.put(key, bm);
	}

	@Override
	public void cleanCache() {
		mCache.clear();
	}
	
	@Override
	public boolean isCached(Object key) {
		return mCache.containsKey(key);
	}
	
}

class LocalBitmapCashe implements IBitmapCache{
	/**
	 * the imgs cache dir name in application cache dir.
	 */
	private final static String CACHE_SUBDIR = "imgs"; 
	/**
	 * the absolute path of imgs cache dir
	 */
	private File mCacheDir;
	/**
	 * the cached imgs name list. the name is the md5 from url. 
	 */
	private ArrayList<String> mCache;
	
	private static LocalBitmapCashe mInstance;	
	private LocalBitmapCashe(Context c) {
		mCacheDir = new File(c.getCacheDir(), CACHE_SUBDIR);
		if(!mCacheDir.exists()) mCacheDir.mkdirs();
		
		String[] names = mCacheDir.list();
		mCache = new ArrayList<String>((int) (names.length * 1.5));
		for(int i = 0; i< names.length; i++){
			mCache.add(names[i]);
		}
	}
	
	public synchronized static LocalBitmapCashe getInstance(Context c){
		if(null == mInstance){
			mInstance = new LocalBitmapCashe(c);
		}
		return mInstance;
	}

	@Override
	public Bitmap getBitmap(Object key) {
		Bitmap bm = null;
		String urlStr = (String) key;
		final String cachedName = getMd5(urlStr); 
		 if(mCache.contains(cachedName)){
			 bm = BitmapFactory.decodeFile(mCacheDir.getPath() + File.separatorChar + (String) key);
		 }else{
			 bm = BitmapUtil.loadImage(urlStr);
			 cacheBitmap(bm, key);
		 }
		return bm;
	}

	/**
	 * the key is the img url string.
	 */
	@Override
	public void cacheBitmap(Bitmap bm, Object key) {
		final String cachedName = getMd5((String) key); 
		File file = new File(mCacheDir, cachedName);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			bm.compress(CompressFormat.PNG, 90, fos);
			mCache.add(cachedName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void cleanCache() {
		for(String name : mCache){
			File file = new File(mCacheDir, name);
			file.delete();
		}
	}

	@Override
	public boolean isCached(Object key) {
		return mCache.contains(key);
	}
	
	private String getMd5(String str){
		String md5 = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			byte[] hashValue = md.digest();
			md5 = convertToHexString(hashValue);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return md5;
	}
	
	private String convertToHexString(byte data[]) {
		StringBuffer strBuffer = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			strBuffer.append(Integer.toHexString(0xff & data[i]));
		}
		return strBuffer.toString();
	}
}
