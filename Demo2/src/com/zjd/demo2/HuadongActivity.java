package com.zjd.demo2;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class HuadongActivity extends Activity {

	private ImageView image1 = null;
	private ImageView image2 = null;
	private ImageView image3 = null;
	
    private List<ImageView> list;

    private int count=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_huadong);

		image1 = (ImageView) findViewById(R.id.image1);
		image2 = (ImageView) findViewById(R.id.image2);
		image3 = (ImageView) findViewById(R.id.image3);

		list = new ArrayList<ImageView>();
		list.add(image1);
		list.add(image2);
		list.add(image3);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.huadong, menu);
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			Log.i("HuadongActivity", "move---");
			showImage();
		}

		return super.onTouchEvent(event);
	}

	private void showImage()
	{
		image1.setVisibility(View.VISIBLE);
		count = count % 3;
		for (ImageView i : list)
		{
			i.setVisibility(View.INVISIBLE);
		}
		list.get(count).setVisibility(View.VISIBLE);
		count++;
	}

}
