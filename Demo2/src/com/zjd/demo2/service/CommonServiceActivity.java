package com.zjd.demo2.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.zjd.demo2.R;

public class CommonServiceActivity extends Activity {
	private final String LOG_TAG = "CommonServiceActivity";
	private CommonService commonService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_common_service);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.common, menu);
		return true;
	}

	//晩崗
	public void openLogHandloer(View view) {
		startService(new Intent(this, CommonService.class));
	}

	public void clouseLogHandloer(View view) {
		stopService(new Intent(this, CommonService.class));
	}

	public void bindLogHandloer(View view) {
		bindService(new Intent("com.zjd.demo2.service.CommonService"), sc, Context.BIND_AUTO_CREATE);
		Log.i(LOG_TAG,"bindLogHandloer end。。。");
	}

	public void unbindLogHandloer(View view) {
		unbindService(sc);
	}

	private ServiceConnection sc = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			commonService = ((CommonService.CommonServiceBinder) service).getService();
			Log.i(LOG_TAG,"onServiceConnected。。。");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			commonService = null;
			Log.i(LOG_TAG,"onServiceDisconnected。。。");
		}
	};
	
	
	//咄赤
	private ServiceConnection musicSc = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(LOG_TAG,"music onServiceConnected。。。");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(LOG_TAG,"music onServiceDisconnected。。。");
		}
	};
	public void openMusicHandloer(View view){
		bindService(new Intent("com.zjd.demo2.service.MusicService"), musicSc, Context.BIND_AUTO_CREATE);
	}
	public void closeMusicHandloer(View view){
		unbindService(musicSc);
	}

}
