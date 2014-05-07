package baidulocationsdk.demo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;

/**
 * 反地理编码功能 
 *
 */
public class GeocodeingActivity extends Activity implements OnClickListener{
	private SharedPreferences mPreferences;
	
	private Button mSettingBtn;
	private RadioGroup mGeocodingGroup;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geocoding_activity);
		mPreferences = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
		
		mSettingBtn = (Button) findViewById(R.id.test_demo);
		mSettingBtn.setOnClickListener(this);
		mGeocodingGroup = (RadioGroup) findViewById(R.id.is_need_geocode_group);
		int geocodingType = mPreferences.getInt(Constants.GEOCODING_TYPE, 0);
		mGeocodingGroup.check(geocodingType);
	}
	
	@Override
	public void onClick(View v) {
		Editor editor = mPreferences.edit();
		editor.putInt(Constants.GEOCODING_TYPE, 
				mGeocodingGroup.getCheckedRadioButtonId() == -1 ? 0 : mGeocodingGroup.getCheckedRadioButtonId())
				.commit();
		
		editor.putInt(Constants.GEOCODING_LOCATION_FUNCTION, 1).commit();
		this.finish();
	}
}
