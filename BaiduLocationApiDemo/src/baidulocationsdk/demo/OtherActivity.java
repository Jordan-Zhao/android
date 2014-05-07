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
 * 其他功能 
 *
 */
public class OtherActivity extends Activity implements OnClickListener{
	private Button mSetingBtn;
	private SharedPreferences mPreferences;
	
	private RadioGroup mLocationCoordGroup;
	private RadioGroup mLocationDirectionGroup;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.other_activity);
		mPreferences = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
		
		mSetingBtn = (Button) findViewById(R.id.test_demo);
		mSetingBtn.setOnClickListener(this);
		mLocationCoordGroup = (RadioGroup) findViewById(R.id.location_coord);
		mLocationDirectionGroup = (RadioGroup) findViewById(R.id.is_need_direction_group);
		
		int coordType = mPreferences.getInt(Constants.LOCATION_COORD_TYPE_FOR_OTHER, 0);
		int direction = mPreferences.getInt(Constants.LOCATION_DIRECTION, 0);
		mLocationCoordGroup.check(coordType);
		mLocationDirectionGroup.check(direction);
	}

	@Override
	public void onClick(View v) {
		Editor editor = mPreferences.edit();
		editor.putInt(Constants.LOCATION_COORD_TYPE_FOR_OTHER, 
				mLocationCoordGroup.getCheckedRadioButtonId() == -1 ? 0 : mLocationCoordGroup.getCheckedRadioButtonId())
				.commit();
		editor.putInt(Constants.LOCATION_DIRECTION, 
				mLocationDirectionGroup.getCheckedRadioButtonId() == -1 ? 0 : mLocationDirectionGroup.getCheckedRadioButtonId())
				.commit();
		
		editor.putInt(Constants.OTHRER_LOCATION_FUNCTION, 1).commit();
		
		this.finish();
	}
}
