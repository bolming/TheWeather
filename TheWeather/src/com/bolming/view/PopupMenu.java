/*
 * @(#)PopupMenu.java 2014-7-12
 *
 */

package com.bolming.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

import com.bolming.weather.R;

/**
 * @author Wang Baoming
 *
 */
public class PopupMenu {
	private Context mContext;
	private PopupWindow mPopupWindow;
	private View mContentView;
	
	private Button mBtnChangLang, mBtnSaveLocation, mBtnShowFootprint;
	
	public PopupMenu(Context context) {
		mContext = context;
		init();
	}
	
	private void init(){
		mContentView = View.inflate(mContext, R.layout.view_popupmenu_more, null);
		getView();
		setOnClickListener();
		
		mPopupWindow = new PopupWindow(mContentView, 
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 
				true);
		
		mPopupWindow.setAnimationStyle(android.R.style.Animation_InputMethod);
		mPopupWindow.setBackgroundDrawable(new ColorDrawable(R.color.popupmenu_bg));
		mPopupWindow.setOutsideTouchable(true);
	}
	
	private void getView(){
		mBtnChangLang = (Button) mContentView.findViewById(R.id.view_popupmenu_item_btn_changelang);
		mBtnSaveLocation = (Button) mContentView.findViewById(R.id.view_popupmenu_item_btn_savelocation);
		mBtnShowFootprint = (Button) mContentView.findViewById(R.id.view_popupmenu_item_btn_footprint);
	}	
	
	private void setOnClickListener(){	
		mBtnChangLang.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPopupWindow.dismiss();
			}
		});
		
		mBtnSaveLocation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPopupWindow.dismiss();
			}
		});
		
		mBtnShowFootprint.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPopupWindow.dismiss();
			}
		});
		
	}
	
	public void setChangeLangBtnClickListener(View.OnClickListener listener){
		mBtnChangLang.setOnClickListener(listener);
	}
	
	public void setSaveLocationBtnClickListener(View.OnClickListener listener){
		mBtnSaveLocation.setOnClickListener(listener);
	}
	
	public void setShowFootprintBtnClickListener(View.OnClickListener listener){
		mBtnShowFootprint.setOnClickListener(listener);
	}
	
	public void show(View parent){
		mPopupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
	}
	
	public void dismiss(){
		mPopupWindow.dismiss();
	}
}
