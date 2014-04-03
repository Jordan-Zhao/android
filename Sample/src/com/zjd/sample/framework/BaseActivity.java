package com.zjd.sample.framework;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class BaseActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		Log.i("onCreate", "activity class:"+this.getClass().getName());
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onStart(){
		Log.i("onStart","activity class:"+this.getClass().getName());
		super.onStart();
	}
	
	@Override
	protected void onResume(){
		Log.i("onResume","activity class:"+this.getClass().getName());
		super.onResume();
	}
	
	@Override
	protected void onPause(){
		Log.i("onPause","activity class:"+this.getClass().getName());
		super.onPause();
	}
	
	@Override
	protected void onStop(){
		Log.i("onStop","activity class:"+this.getClass().getName());
		super.onStop();
	}
	
	@Override
	protected void onDestroy(){
		Log.i("onRestart","activity class:"+this.getClass().getName());
		super.onDestroy();
	}
	
	@Override
	protected void onRestart(){
		Log.i("onRestart","activity class:"+this.getClass().getName());
		super.onRestart();
	}
}
