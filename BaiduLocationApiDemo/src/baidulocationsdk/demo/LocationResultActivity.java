package baidulocationsdk.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * 定位结果页面
 * @author jpren
 *
 */
public class LocationResultActivity extends Activity implements OnClickListener{
	private TextView mLocationResult;
	private Button mEndTestBtn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_result_activity);
		mLocationResult = (TextView) findViewById(R.id.location_result);
		((MyApplication)getApplication()).mLocationResult = mLocationResult;
		
		mEndTestBtn = (Button) findViewById(R.id.end_test_demo);
		mEndTestBtn.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		((MyApplication)getApplication()).mLocationClient.stop();
		this.finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			((MyApplication)getApplication()).mLocationClient.stop();
			this.finish();
		}
		
		return true;
	}
}
