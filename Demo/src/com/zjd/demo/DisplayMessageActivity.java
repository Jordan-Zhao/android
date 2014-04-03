package com.zjd.demo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class DisplayMessageActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_display_message);
		// // Show the Up button in the action bar.
		// setupActionBar();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_message);

		// Get the message from the intent
		Intent intent = getIntent();
		String name = intent.getStringExtra(Constants.KEY_NAME);
		String pass = intent.getStringExtra(Constants.KEY_PASS);
		TextView textView = (TextView)findViewById(R.id.txtName);
		textView.setText(name);
		textView = (TextView)findViewById(R.id.txtPass);
		textView.setText(pass);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_message, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 点击返回按钮事件
	 * 
	 * @param view
	 */
	public void onReturnHandler(View view) {
//		Intent intent = new Intent(this, MainActivity.class);
//		intent.putExtra(Constants.KEY_RETURN, "我来自第二个页面");
//		startActivity(intent);
		
		Intent backIntent = new Intent();
		Bundle stringBundle = new Bundle();
		stringBundle.putString(Constants.KEY_RETURN, "我来自第二个页面");
		backIntent.putExtras(stringBundle);
		setResult(Constants.RSP_CODE1,backIntent);
		finish();
	}

}
