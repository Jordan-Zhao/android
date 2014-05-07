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
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * 地理围栏功能
 *
 */
public class GeofenceActivity extends Activity implements OnClickListener{
	private SharedPreferences mPreferences;
	
	private Button mSettingBtn;
	private RadioGroup mGeofenceTypeGroup;
	
	private EditText mHightLongtitude;
	private EditText mHightLatitude;
	private EditText mHightCoordType;
	private EditText mHightRadius;
	
	private EditText mFenceName;
	private EditText mSavingLongtitude;
	private EditText mSavingLatitude;
	private EditText mSavingCoordType;
	private EditText mSavingRadius;
	private EditText mSavingEffectiveTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geofence_activity);
		mPreferences = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
		
		mSettingBtn = (Button) findViewById(R.id.test_demo);
		mSettingBtn.setOnClickListener(this);
		mGeofenceTypeGroup = (RadioGroup) findViewById(R.id.is_need_geofence_group);
		int geofenctType = mPreferences.getInt(Constants.GEOFENCE_TYPE, 0);
		mGeofenceTypeGroup.check(geofenctType);
		
		mHightLongtitude = (EditText) findViewById(R.id.hight_longtitude);
		mHightLatitude = (EditText) findViewById(R.id.hight_latitude);
		mHightCoordType = (EditText) findViewById(R.id.hight_coord_type);
		mHightRadius = (EditText) findViewById(R.id.hight_radius);
		
		mFenceName = (EditText) findViewById(R.id.saving_fence_name);
		mSavingLongtitude = (EditText) findViewById(R.id.saving_longtitude);
		mSavingLatitude = (EditText) findViewById(R.id.saving_latitude);
		mSavingCoordType = (EditText) findViewById(R.id.saving_coord_type);
		mSavingRadius = (EditText) findViewById(R.id.saving_radius);
		mSavingEffectiveTime = (EditText) findViewById(R.id.saving_effective_time);
		
		String hightLongitude = mPreferences.getString(Constants.GEOFENCE_LONGITUDEE, "");
		String hightLaitude = mPreferences.getString(Constants.GEOFENCE_LATITUDE, "");
		String hightRadius = mPreferences.getString(Constants.GEOFENCE_RADIUS, "");
		String hightCoordType = mPreferences.getString(Constants.LOCATION_COORD_TYPE, "");
		
		mHightLongtitude.setText(hightLongitude);
		mHightLatitude.setText(hightLaitude);
		mHightRadius.setText(hightRadius);
		mHightCoordType.setText(hightCoordType);
		
		String savingFenceName = mPreferences.getString(Constants.BATTERYSAVE_GEOFENCE_NAME, "");
		String savingLongitude = mPreferences.getString(Constants.BATTERYSAVE_GEOFENCE_LONGITUDEE, "");
		String savingLaitude = mPreferences.getString(Constants.BATTERYSAVE_GEOFENCE_LATITUDE, "");
		String savingRadius = mPreferences.getString(Constants.BATTERYSAVE_GEOFENCE_RADIUS, "");
		String savingVaitateTime = mPreferences.getString(Constants.BATTERYSAVE_GEOFENCE_VALITATE_TIME, "");
		String savingCoordType = mPreferences.getString(Constants.LOCATION_COORD_TYPE, "");
		
		mFenceName.setText(savingFenceName);
		mSavingLongtitude.setText(savingLongitude);
		mSavingLatitude.setText(savingLaitude);
		mSavingRadius.setText(savingRadius);
		mSavingEffectiveTime.setText(savingVaitateTime);
		mSavingCoordType.setText(savingCoordType);
	}
	
	@Override
	public void onClick(View v) {
		Editor editor = mPreferences.edit();
		int fenceType = mGeofenceTypeGroup.getCheckedRadioButtonId() == -1 ? -1 : mGeofenceTypeGroup.getCheckedRadioButtonId();
		editor.putInt(Constants.GEOFENCE_TYPE, fenceType).commit();
		if (fenceType == 0) {
			//高精度
			String longitude = mHightLongtitude.getText().toString();
			String laitude = mHightLatitude.getText().toString();
			String radius = mHightRadius.getText().toString();
			String coordType = mHightCoordType.getText().toString();
			if (TextUtils.isEmpty(longitude) || TextUtils.isEmpty(laitude)
					|| TextUtils.isEmpty(radius) || TextUtils.isEmpty(coordType)) {
				Toast.makeText(GeofenceActivity.this, "请填写高精度的各个参数", Toast.LENGTH_SHORT).show();
				return;
			}
			editor.putString(Constants.GEOFENCE_LONGITUDEE, longitude).commit();
			editor.putString(Constants.GEOFENCE_LATITUDE, laitude).commit();
			editor.putString(Constants.GEOFENCE_RADIUS, radius).commit();
			editor.putString(Constants.LOCATION_COORD_TYPE, coordType).commit();
		} else if(fenceType == 1) {
			//低功耗
			String fenceName = mFenceName.getText().toString();
			String longitude = mSavingLongtitude.getText().toString();
			String laitude = mSavingLatitude.getText().toString();
			String radius = mSavingRadius.getText().toString();
			String time = mSavingEffectiveTime.getText().toString();
			String coordType = mSavingCoordType.getText().toString();
			
			if (TextUtils.isEmpty(fenceName) || TextUtils.isEmpty(longitude) 
					|| TextUtils.isEmpty(laitude) ||  TextUtils.isEmpty(time)
					|| TextUtils.isEmpty(radius) || TextUtils.isEmpty(coordType)) {
				Toast.makeText(GeofenceActivity.this, "请填写低功耗的各个参数", Toast.LENGTH_SHORT).show();
				return;
			}
			editor.putString(Constants.BATTERYSAVE_GEOFENCE_NAME, fenceName).commit();
			editor.putString(Constants.BATTERYSAVE_GEOFENCE_LONGITUDEE, longitude).commit();
			editor.putString(Constants.BATTERYSAVE_GEOFENCE_LATITUDE, laitude).commit();
			editor.putString(Constants.BATTERYSAVE_GEOFENCE_RADIUS, radius).commit();
			editor.putString(Constants.BATTERYSAVE_GEOFENCE_VALITATE_TIME, time).commit();
			editor.putString(Constants.LOCATION_COORD_TYPE, time).commit();
		}
		
		editor.putInt(Constants.GEOFENCE_LOCATION_FUNCTION, 1).commit();
		this.finish();
	}
}
