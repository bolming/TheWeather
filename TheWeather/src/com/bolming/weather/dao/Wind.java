/**
 * 
 */
package com.bolming.weather.dao;

/**
 * @author Wang Baoming
 *
 */
public class Wind {
	private String mDirection;
	private int mSpeed; // km/h
	
	public void setDir(String dir){
		mDirection = dir;
	}
	public void setSpeed(int speed){
		mSpeed = speed;
	}
	public String getDir(){
		return mDirection;
	}
	public int getSpeed(){
		return mSpeed;
	}
	
	public void show(){
		System.out.printf("dir:%s, speed:%d\n", mDirection, mSpeed);
	}
}
