package com.zjd.sample.activity.news;

import android.os.Bundle;
import android.view.Menu;

import com.zjd.sample.R;
import com.zjd.sample.framework.BaseActivity;

public class NewsPublishActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_publish);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.publish, menu);
		return true;
	}

}
