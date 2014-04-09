package com.zjd.demo2.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class CommonService extends Service{
	private final String LOG_TAG = "CommonService";
	
	private CommonServiceBinder binder = new CommonServiceBinder();
	
	private String status;
	public String getStatus(){
		return this.status;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(LOG_TAG,"oncreate...");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(LOG_TAG,"onStartCommand...startId:"+startId);
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(LOG_TAG,"onBind...");
		status = "common service is binded";
		return binder;
	}
	
	@Override
	public void onRebind(Intent intent) {
		Log.i(LOG_TAG,"onRebind...");
		super.onRebind(intent);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(LOG_TAG,"onUnbind...");
		return super.onUnbind(intent);
	}
	
	@Override
	public void onDestroy() {
		Log.i(LOG_TAG,"onDestroy...");
		super.onDestroy();
	}
	
	public class CommonServiceBinder extends Binder {
		CommonService getService() {
                return CommonService.this;
        }
}
}
