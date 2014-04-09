package com.zjd.demo2.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class MusicService extends Service {
	// 定义音乐播放器变量
	private MediaPlayer mPlayer;
	
	private MusicServiceBinder  binder = new MusicServiceBinder();

	@Override
	public void onCreate(){
		mPlayer = new MediaPlayer();
		try {
			File file = new File("/sdcard/myfile/m1.mp3"); 
			FileInputStream fis = new FileInputStream(file); 
			mPlayer.setDataSource(fis.getFD()); 
			mPlayer.prepare(); 
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		mPlayer.start();
		return binder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		mPlayer.stop();
		return super.onUnbind(intent);
	}

	public class MusicServiceBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}
	}
}
