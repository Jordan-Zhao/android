package baidulocationsdk.demo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * 基础定位功能
 */
public class BaseLocationActivity extends Activity implements OnClickListener{
	private Button mSettingBtn;
	private RadioGroup mLocationModeGroup;
	private RadioButton mLocationModeHeightAccuracy;
	private RadioButton mLocationModeSavingBattery;
	private RadioButton mLocationModeDeviceSensor;
	
	private RadioGroup mLocationFrequencyGroup;
	private RadioButton mContinuousLocation;
	private RadioButton mSignalLocation;
	
	private EditText mScanSpanTime;
	
	private SharedPreferences mPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_location_activity);
		
		mPreferences = this.getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
		
		mSettingBtn = (Button) findViewById(R.id.test_demo);
		mSettingBtn.setOnClickListener(this);
		mLocationModeGroup = (RadioGroup) findViewById(R.id.location_mode);
		mLocationModeHeightAccuracy = (RadioButton) findViewById(R.id.location_mode_height_accuracy);
		mLocationModeSavingBattery = (RadioButton) findViewById(R.id.location_mode_saving_battery);
		mLocationModeDeviceSensor = (RadioButton) findViewById(R.id.location_mode_device_sensor);
		int locationModeId = mPreferences.getInt(Constants.LOCATION_MODE, 0);
		mLocationModeGroup.check(locationModeId);
		
		mLocationFrequencyGroup = (RadioGroup) findViewById(R.id.location_frequency_mode);
		mContinuousLocation = (RadioButton) findViewById(R.id.continuous_location);
		mSignalLocation = (RadioButton) findViewById(R.id.single_location);
		int frequencyId = mPreferences.getInt(Constants.LOCATION_SEQUENCE, 0);
		mLocationFrequencyGroup.check(frequencyId);
		
		mScanSpanTime = (EditText) findViewById(R.id.scan_span);
		int scanTime = mPreferences.getInt(Constants.LOCATION_SCAN_TIME, 0);
		if (scanTime != 0) {
			mScanSpanTime.setText(String.valueOf(scanTime));
		}
	}
	

	@Override
	public void onClick(View v) {
		Editor editor = mPreferences.edit();
		editor.putInt(Constants.LOCATION_MODE, 
				mLocationModeGroup.getCheckedRadioButtonId() == -1 ? 0 : mLocationModeGroup.getCheckedRadioButtonId())
				.commit();
		editor.putInt(Constants.LOCATION_SEQUENCE, 
				mLocationFrequencyGroup.getCheckedRadioButtonId() == -1 ? 0 : mLocationFrequencyGroup.getCheckedRadioButtonId())
				.commit();
		
		String timeStr = mScanSpanTime.getText().toString();
		int scanTime = TextUtils.isEmpty(timeStr) ? 0 : Integer.valueOf(timeStr);
		editor.putInt(Constants.LOCATION_SCAN_TIME, scanTime).commit();
		
		editor.putInt(Constants.BASE_LOCATION_FUNCTION, 1).commit();
		
		this.finish();
	}
}
