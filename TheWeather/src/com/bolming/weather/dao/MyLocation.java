package com.bolming.weather.dao;

import android.os.Parcel;
import android.os.Parcelable;

public class MyLocation implements Parcelable{
	public final static MyLocation DEF_LOCATION = new MyLocation(31.39f,120.95f);

	private MyLocation(float latitude, float longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	public MyLocation() {
	}
	public float latitude;
	public float longitude;

	public static final Parcelable.Creator<MyLocation> CREATOR = new Creator<MyLocation>() {
		
		@Override
		public MyLocation[] newArray(int size) {
			return new MyLocation[size];
		}
		
		@Override
		public MyLocation createFromParcel(Parcel source) {
			return new MyLocation(source);
		}
	};
	
	private MyLocation(Parcel source) {
		longitude = source.readFloat();
		latitude = source.readFloat();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(longitude);
		dest.writeFloat(latitude);
	}
}