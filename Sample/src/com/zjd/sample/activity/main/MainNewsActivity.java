package com.zjd.sample.activity.main;

import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.zjd.sample.R;
import com.zjd.sample.framework.BaseActivity;

public class MainNewsActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_list);
		TextView titleView = (TextView)getParent().findViewById(R.id.topTitleText);
		if(titleView != null){
			titleView.setText("今日新闻");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
