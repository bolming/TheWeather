/*
 * @(#)FootprintListActivity.java 2014-7-21
 *
 */

package com.bolming.weather;

import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bolming.common.BitmapCache;
import com.bolming.weather.dao.MyLocation;
import com.bolming.weather.db.Footprint;
import com.bolming.weather.db.FootprintDbHelper;

/**
 * @author Wang Baoming
 *
 */
public class FootprintListActivity extends ListActivity {
		
	private FootprintLstvAdapter mFootprintLstvAdapter;
	private List<Footprint> mFootprints;
	
	private FootprintDbHelper mFootprintDbHelper;
	
	private FootprintListActivity mContext;
	
	private AlertDialog mAlertDelDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.footprint);
		mContext = this;
		
		mFootprintDbHelper = new FootprintDbHelper(mContext);
		mFootprintLstvAdapter = new FootprintLstvAdapter();		
		
		createDialog();
		
		setOnClickListener();
		init();
	}
	
	private void init(){
		mFootprints = mFootprintDbHelper.query();
		
		setListAdapter(mFootprintLstvAdapter);
	}
	
	private void setOnClickListener(){
		getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View v,
					int pos, long id) {

				showAlertDialog(id);
				
				return false;
			}
		});
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Footprint footprint = mFootprints.get(position);
		MyLocation location = new MyLocation();
		location.longitude = footprint.getLongitude();
		location.latitude = footprint.getLatitude();
				
		Intent intent = new Intent(mContext, TheWeatherActivity.class);
		intent.putExtra(Constants.LOCATION, location);
		startActivity(intent);
	}
	
	@Override
	protected void onDestroy() {
		if(null != mFootprintDbHelper){
			mFootprintDbHelper.close();
		}
		super.onDestroy();
	}
		
	private void createDialog(){
		if(null == mAlertDelDialog){
			Builder dlgBuilder = new Builder(mContext);
			dlgBuilder.setTitle(R.string.footprint_del_item_dlg_title)
				.setMessage(R.string.footprint_del_item_dlg_msg)
				.setPositiveButton(R.string.footprint_del_item_dlg_cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				})
				.setNegativeButton(R.string.footprint_del_item_dlg_confirm, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
			mAlertDelDialog = dlgBuilder.create();
		}
	}
	
	private void showAlertDialog(final long id){
		mAlertDelDialog.setButton(DialogInterface.BUTTON_NEGATIVE, 
				getString(R.string.footprint_del_item_dlg_confirm), 
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						FootprintDbHelper dbHelper = new FootprintDbHelper(mContext);
						dbHelper.delete(id);

						update();
					}
				});
		
		mAlertDelDialog.show();		
	}
	
	private void update(){
		mFootprints = mFootprintDbHelper.query();
		
		mFootprintLstvAdapter.notifyDataSetChanged();
	}
	
	private class FootprintLstvAdapter extends BaseAdapter{


		private class ItemHolder{
			ImageView imvIcon;
			TextView tvCity;
			TextView tvDate;
		}
		
		@Override
		public int getCount() {
			return mFootprints.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mFootprints.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return ((Footprint) getItem(position)).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ItemHolder holder = null; 
			if(null == view){
				view = View.inflate(mContext, R.layout.footprint_listview_item, null);
				
				holder = new ItemHolder();
				holder.imvIcon = (ImageView) view.findViewById(R.id.footprint_listview_item_imgv_icon);
				holder.tvCity = (TextView) view.findViewById(R.id.footprint_listview_item_tv_city);
				holder.tvDate = (TextView) view.findViewById(R.id.footprint_listview_item_tv_date);
				
				view.setTag(holder);
			}else{
				holder = (ItemHolder) view.getTag();
			}
			
			Footprint footprint = mFootprints.get(position);
			
			// assert the bitmap has been cached
			holder.imvIcon.setImageBitmap(BitmapCache.getInstance().getBitmap(footprint.getIcon()) );
			holder.tvCity.setText(footprint.getCity());
			holder.tvDate.setText(footprint.getDate());
			
			return view;
		}
		
	}
}
