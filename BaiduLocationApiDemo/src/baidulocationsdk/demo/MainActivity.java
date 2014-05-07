package baidulocationsdk.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.location.BDGeofence;
import com.baidu.location.BDLocationStatusCodes;
import com.baidu.location.GeofenceClient;
import com.baidu.location.GeofenceClient.OnAddBDGeofencesResultListener;
import com.baidu.location.GeofenceClient.OnGeofenceTriggerListener;
import com.baidu.location.GeofenceClient.OnRemoveBDGeofencesResultListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

/**
 * 百度定位SDK产品Demo功能主页
 */
public class MainActivity extends Activity implements OnItemClickListener, OnClickListener{
	private ListView mListView;
	private Button mTestLocBtn;
	private MainAdapter mAdapter;
	private SharedPreferences mSharedPreferences;
	private LocationClient mLocClient;
	
	private boolean mLocationInit;
	private boolean mGeofenceInit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mLocClient = ((MyApplication)getApplication()).mLocationClient;
		
		mSharedPreferences = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
		
		mListView = (ListView) findViewById(R.id.function_list);
		mTestLocBtn = (Button) findViewById(R.id.test_demo);
		mListView.setOnItemClickListener(this);
		mTestLocBtn.setOnClickListener(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		initAdapter();
	}
	
	//初始化主界面List
	private void initAdapter() {
		String[] mainTitle = this.getResources().getStringArray(R.array.main_title);
		String[] secondTitle = this.getResources().getStringArray(R.array.second_title);
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>(5);
		for (int i = 0; i < 5; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MainAdapter.PARAM_MAIN_TITLE, mainTitle[i]);
			map.put(MainAdapter.PARAM_SECOND_TITLE, secondTitle[i]);
			map.put(MainAdapter.PARAM_SETTING_TITLE, getCurrentSetting(i));
			list.add(map);
		}
		mAdapter = new MainAdapter(this, list);
		mListView.setAdapter(mAdapter);
	}
	
	//各设置项是否进行设置
	private String getCurrentSetting(int index) {
		int setting = 0;
		switch (index) {
		case 0:
			setting = mSharedPreferences.getInt(Constants.BASE_LOCATION_FUNCTION, 0);
			break;
		case 1:
			setting = mSharedPreferences.getInt(Constants.GEOCODING_LOCATION_FUNCTION, 0);
			break;
		case 2:
			setting = mSharedPreferences.getInt(Constants.GEOFENCE_LOCATION_FUNCTION, 0);
			break;
		case 3:
			setting = mSharedPreferences.getInt(Constants.OTHRER_LOCATION_FUNCTION, 0);
			break;
		case 4:
			setting = Integer.MAX_VALUE;
			break;

		default:
			break;
		}
		
		return setting == 0 ? "未设置" : (setting == 1 ? "设置完毕" : "");
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Intent i ;
		switch (arg2) {
		case 0:
			//基础定位功能
			i = new Intent(MainActivity.this, BaseLocationActivity.class);
			startActivity(i);
			break;
		case 1:
			//反地理编码功能
			i = new Intent(MainActivity.this, GeocodeingActivity.class);
			startActivity(i);
			break;
		case 2:
			//地理围栏功能
			i = new Intent(MainActivity.this, GeofenceActivity.class);
			startActivity(i);
			break;
		case 3:
			//其他功能
			i = new Intent(MainActivity.this, OtherActivity.class);
			startActivity(i);
			break;
		case 4:
			//其他功能
			i = new Intent(MainActivity.this, QuestActivity.class);
			startActivity(i);
			break;

		default:
			break;
		}
	}

	
	private LocationMode mLocationMode;
	private boolean mLocationSequency;
	private int mScanSpan;
	private boolean mIsNeedAddress;
	private String mCoordType;
	private boolean mIsNeedDirection;
	
	private int mGeofenceType;
	
	private GeofenceClient mGeofenceClient;
	private String mSavingFenceName;
	private String mSavingFenceLongitude;
	private String mSavingFenceLaitude;
	private String mSavingFenceRadius;
	private String mSavingFenceCoordType;
	private Long mSavingFenceExpirationTime;
	
	private String mHightAccuracyLongitude;
	private String mHightAccuracyLaitude;
	private String mHightAccuracyRadius;
	private String mHightAccuracyCoordType;
	
	@Override
	public void onClick(View v) {
		getLocationParams();
		setLocationOption();
		//开始定位
		if (mLocationInit) {
			mLocClient.start();
		} else {
			Toast.makeText(this, "请设置定位相关的参数", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (!mLocationSequency && mLocClient.isStarted()) {
			//单次请求定位
			mLocClient.requestLocation();
		} 
		
		if (mGeofenceInit) {
			//地理围栏实现
			if (mGeofenceType == 0) {
				//高精度
				//4个参数代表要位置提醒的点的坐标，具体含义依次为：纬度，经度，距离范围，坐标系类型(gcj02,gps,bd09,bd09ll)
				((MyApplication)getApplication()).mNotifyLister.SetNotifyLocation(Double.valueOf(mHightAccuracyLaitude),
						Double.valueOf(mHightAccuracyLongitude), Float.valueOf(mHightAccuracyRadius), 
						mHightAccuracyCoordType);
				mLocClient.registerNotify(((MyApplication)getApplication()).mNotifyLister);
			} else if (mGeofenceType == 1) {
				//低功耗
				mGeofenceClient = ((MyApplication)getApplication()).mGeofenceClient;
				mGeofenceClient.registerGeofenceTriggerListener(new GeofenceTriggerListener());
				addGeofenceBySaving();
			}
		}
		
		Intent i = new Intent(MainActivity.this, LocationResultActivity.class);
		startActivity(i);
	}
	
	//添加一个低功耗围栏
	private void addGeofenceBySaving() {
		BDGeofence fence = new BDGeofence.Builder().setGeofenceId(mSavingFenceName).
				setCircularRegion(Double.valueOf(mSavingFenceLongitude), Double.valueOf(mSavingFenceLaitude), Integer.valueOf(mSavingFenceRadius)).
				setExpirationDruation(mSavingFenceExpirationTime).
				setCoordType(mSavingFenceCoordType).
				build();
		
		mGeofenceClient.addBDGeofence(fence, new AddGeofenceListener());
	}
	
	//获取定位参数数据
	private void getLocationParams() {
		//定位精度
		int locationMode = mSharedPreferences.getInt(Constants.LOCATION_MODE, 0);
		switch (locationMode) {
		case R.id.location_mode_height_accuracy:
			mLocationMode = LocationMode.Hight_Accuracy;
			break;
		case R.id.location_mode_saving_battery:
			mLocationMode = LocationMode.Battery_Saving;
			break;
		case R.id.location_mode_device_sensor:
			mLocationMode = LocationMode.Device_Sensors;
			break;
		default:
			break;
		}
		
		//定位模式及间隔时间
		int locationSequence = mSharedPreferences.getInt(Constants.LOCATION_SEQUENCE, 0);
		switch (locationSequence) {
		case R.id.continuous_location:
			mLocationSequency = true;
			mScanSpan = mSharedPreferences.getInt(Constants.LOCATION_SCAN_TIME, 1000);
			break;
		case R.id.single_location:
			mLocationSequency = false;
			break;

		default:
			break;
		}
		
		//地址信息
		int geocoding = mSharedPreferences.getInt(Constants.GEOCODING_TYPE, 0);
		switch (geocoding) {
		case R.id.use_geocode:
			mIsNeedAddress = false;
			break;
		case R.id.no_use_geocode:
			mIsNeedAddress = true;
			break;

		default:
			break;
		}
		
		//定位坐标类型
		int coord = mSharedPreferences.getInt(Constants.LOCATION_COORD_TYPE_FOR_OTHER, 1);
		switch (coord) {
		case R.id.location_coord_type_gcj:
			mCoordType = "gcj02";
			break;
		case R.id.location_coord_type_bd09ll:
			mCoordType = "bd09ll";
			break;
		case R.id.location_coord_type_bd09:
			mCoordType = "bd09";
			break;

		default:
			break;
		}
		
		//是否需要方向
		int direction = mSharedPreferences.getInt(Constants.LOCATION_DIRECTION, 1);
		switch (direction) {
		case R.id.is_need_direction_yes:
			mIsNeedDirection = true;
			break;
		case R.id.is_need_direction_no:
			mIsNeedDirection = false;
			break;

		default:
			break;
		}
		
		
		try {
			//地理围栏选项
			mGeofenceType = mSharedPreferences.getInt(Constants.GEOFENCE_TYPE, R.id.saving_battery);
			switch (mGeofenceType) {
			case R.id.hight_accuracy:
				//高精度
				mHightAccuracyLongitude = mSharedPreferences.getString(Constants.GEOFENCE_LONGITUDEE, "116.30677");
				mHightAccuracyLaitude = mSharedPreferences.getString(Constants.GEOFENCE_LATITUDE, "40.04173");
				mHightAccuracyRadius = mSharedPreferences.getString(Constants.GEOFENCE_RADIUS, "3000");
				mHightAccuracyCoordType = mSharedPreferences.getString(Constants.LOCATION_COORD_TYPE, "gps");
				mGeofenceInit = true;
				break;
			case R.id.saving_battery:
				//低功耗
				mSavingFenceName = mSharedPreferences.getString(Constants.BATTERYSAVE_GEOFENCE_NAME, "fence_name");
				mSavingFenceLongitude = mSharedPreferences.getString(Constants.BATTERYSAVE_GEOFENCE_LONGITUDEE, "116.30677");
				mSavingFenceLaitude = mSharedPreferences.getString(Constants.BATTERYSAVE_GEOFENCE_LATITUDE, "40.04173");
				mSavingFenceRadius = mSharedPreferences.getString(Constants.BATTERYSAVE_GEOFENCE_NAME, "1");
				mSavingFenceCoordType = mSharedPreferences.getString(Constants.BATTERYSAVE_GEOFENCE_COORD_TYPE, BDGeofence.COORD_TYPE_BD09LL);
				mSavingFenceExpirationTime = mSharedPreferences.getLong(Constants.BATTERYSAVE_GEOFENCE_VALITATE_TIME, 10L * (3600 * 1000));
				mGeofenceInit = true;
				break;
			default:
				break;
			}
		} catch (Exception e) {
			mGeofenceInit = false;
		}
	}
	
	//设置Option
	private void setLocationOption() {
		try {
			LocationClientOption option = new LocationClientOption();
			option.setLocationMode(mLocationMode);
			option.setCoorType(mCoordType);
			option.setScanSpan(mScanSpan);
			option.setNeedDeviceDirect(mIsNeedDirection);
			option.setIsNeedAddress(mIsNeedAddress);
			mLocClient.setLocOption(option);
			mLocationInit = true;
		} catch (Exception e) {
			e.printStackTrace();
			mLocationInit = false;
		}
	}
	
	
	/**
	 * 实现添加围栏监听器
	 *
	 */
	public class AddGeofenceListener implements OnAddBDGeofencesResultListener {

		@Override
		public void onAddBDGeofencesResult(int statusCode, String geofenceId) {
			try {
				if (statusCode == BDLocationStatusCodes.SUCCESS) {
					//开发者实现创建围栏成功的功能逻辑
					
					Toast.makeText(MainActivity.this, "围栏" + geofenceId + "添加成功", Toast.LENGTH_SHORT).show();
					
					if (mGeofenceClient != null) {
						//在添加地理围栏成功后，开启地理围栏服务，对本次创建成功且已进入的地理围栏，可以实时的提醒
						mGeofenceClient.start();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 实现删除围栏监听器
	 *
	 */
	public class RemoveGeofenceListener implements OnRemoveBDGeofencesResultListener {

		@Override
		public void onRemoveBDGeofencesByRequestIdsResult(int statusCode, String[] geofenceIds) {
			if (statusCode == BDLocationStatusCodes.SUCCESS) {
				//开发者实现删除围栏成功的功能逻辑

				Toast.makeText(MainActivity.this, "围栏删除成功", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/**
	 * 实现进行围栏监听器
	 *
	 */
	public class GeofenceTriggerListener implements OnGeofenceTriggerListener {

		@Override
		public void onGeofenceTrigger(String geofenceId) {
			//开发者实现进入围栏的功能逻辑
			try {
				((Vibrator)MainActivity.this.getApplication().getSystemService(Service.VIBRATOR_SERVICE)).vibrate(3000);
				Toast.makeText(MainActivity.this, "已进入围栏" + geofenceId, Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				
			}
		}
	}
	
}
