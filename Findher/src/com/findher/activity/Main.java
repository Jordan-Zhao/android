package com.findher.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.MyLocationOverlay.LocationMode;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.findher.common.Constants;
import com.findher.common.OsUtil;
import com.findher.contact.Contacter;
import com.findher.contact.GetContacterResultHandler;
import com.findher.contact.PickContactUtil;
import com.findher.framework.ActivityLifeCallback;
import com.findher.framework.ActivityLifeManager;
import com.findher.web.SimpleWebUtil;

public class Main extends ActivityLifeManager {
	private final String LOG_TAG = "main activity";

	private PickContactUtil pickContactUtil = null;
	private ContacterOverlayManager contacterManager = new ContacterOverlayManager(this);
	private OsUtil osUtil = new OsUtil(this);
	private String myNumber = "111";

	private BMapManager mBMapManager = null;
	private MapView mMapView = null;
	private MapController mMapController = null;

	// 定位相关
	private LocationClient mLocClient;
	private LocationData locData = null; // 我的位置坐标
	public MyLocationListenner myListener = new MyLocationListenner();
	private MyLocationOverlay myLocationOverlay = null;

	// 联系人图层
	private MyLocationOverlay contacterOverlay = null;

	boolean isRequest = false;// 是否手动触发请求定位
	boolean isFirstLoc = true;// 是否首次定位

	// 自定义图层
	private ItemizedOverlay itemizedOverlay = null;

	@Override
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBMapManager = new BMapManager(getApplicationContext());
		mBMapManager.init(new MyGeneralListener());

		setContentView(R.layout.activity_main);
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}
		
		setTitle("我的位置");

//		myNumber = osUtil.getMyPhoneNumber();

		// 注册选择联系人功能
		pickContactUtil = new PickContactUtil(this);
		GetContacterResultHandler handler = new GetContacterResultHandler(this);
		super.registerResultHandler(handler, new GetContacterCallback());

		// 初始化百度地图
		initBdMap();
		initBdLocation();
		initBdContacterOverlay();

		mMapView.refresh();

		// 定位我的位置
		mLocClient.requestLocation();

		// 开启线程，更新联系人位置
		 startUpdateContacterLocTask();
	}

	// 初始化百度地图
	private void initBdMap() {
		// 地图初始化
		mMapView = (MapView) findViewById(R.id.baseMapView);
		mMapController = mMapView.getController();
		mMapView.getController().setZoom(14);
		mMapView.getController().enableClick(true);
		mMapView.setBuiltInZoomControls(true);

	}

	// 初始化定位图层
	private void initBdLocation() {
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();
		// 定位图层初始化
		myLocationOverlay = new MyLocationOverlay(mMapView);
		// 设置定位数据
		locData = new LocationData();
		myLocationOverlay.setData(locData);
		myLocationOverlay.enableCompass();
		// 添加定位图层
		mMapView.getOverlays().add(myLocationOverlay);
	}

	// 初始化联系人移动层
	private void initBdContacterOverlay() {
		itemizedOverlay = new ItemizedOverlay(getResources().getDrawable(R.drawable.icon_marka), mMapView);
		mMapView.getOverlays().add(itemizedOverlay);
	}

	// 创建线程，更新联系人位置信息
	private void startUpdateContacterLocTask() {
		AsyncTask<Integer, Integer, Integer> task = new AsyncTask<Integer, Integer, Integer>() {
			@Override
			protected Integer doInBackground(Integer... vals) {
				while (true) {
					String res = SimpleWebUtil.request(Constants.SERVER_HOST
							+ "/updateContacterLoc.do?myNumber=" + myNumber,
							Constants.REQUEST_METHOD_GET);
					List<ContacterLocation> list = new ArrayList<ContacterLocation>();
					try {
						JSONArray jsonArray = new JSONArray(res);
						for (int i = 0; i < jsonArray.length(); i++) {
							ContacterLocation location = new ContacterLocation();
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							location.setFirstNumber(jsonObject.getString("phoneNumber"));
							int lat = (int) (jsonObject.getDouble("lat") * 1E6);
							int lon = (int) (jsonObject.getDouble("lon") * 1E6);
							GeoPoint point = new GeoPoint(lat, lon);
							location.setPoint(point);
							list.add(location);
						}
					} catch (Exception e) {
						Log.e(LOG_TAG, "获取联系人位置信息，解析json异常", e);
						throw new RuntimeException(e);
					}
					contacterManager.updateLocation(list, itemizedOverlay);

					mMapView.refresh();

					try {
						Thread.sleep(5 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				super.onProgressUpdate(values);
			}

			@Override
			protected void onPostExecute(Integer val) {
				super.onPostExecute(val);
			}
		};
		task.execute(0);
	}

	// 处理选中的联系人
	private class GetContacterCallback implements ActivityLifeCallback {
		@Override
		public void callback(Object obj) {
			Contacter contacter = (Contacter) obj;
			OverlayItem item = contacterManager.addContacter(contacter);
			itemizedOverlay.addItem(item);
			// 上传联系人
			String url = Constants.SERVER_HOST
					+ "/addContacter.do?myNumber=" + myNumber
					+ "&contacterNumber=" + contacter.getPhones().get(0);
			String res = SimpleWebUtil.request(url, Constants.REQUEST_METHOD_GET);
		}
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			// 我的位置
			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();

			// 如果不显示定位精度圈，将accuracy赋值为0即可
			locData.accuracy = location.getRadius();
			// 此处可以设置 locData的方向信息, 如果定位 SDK 未返回方向信息，用户可以自己实现罗盘功能添加方向信息。
			locData.direction = location.getDerect();
			// 更新定位数据
			myLocationOverlay.setData(locData);
			// 更新图层数据执行刷新后生效
			mMapView.refresh();
			// 是手动触发请求或首次定位时，移动到定位点
			if (isRequest || isFirstLoc) {
				// 移动地图到定位点
				Log.d("LocationOverlay", "receive location, animate to it");
				mMapController.animateTo(new GeoPoint((int) (locData.latitude * 1e6), (int) (locData.longitude * 1e6)));
				isRequest = false;
				myLocationOverlay.setLocationMode(LocationMode.FOLLOWING);
			}
			// 首次定位完成
			isFirstLoc = false;

			// 上传我的位置
			String res = SimpleWebUtil.request(Constants.SERVER_HOST
					+ "/updateMyLoc.do?myNumber=" + myNumber
					+ "&lat=" + location.getLatitude()
					+ "&lon=" + location.getLongitude()
					, Constants.REQUEST_METHOD_GET);
		}

		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}

	// 选择联系人
	public void selectContactHandler(View view) {
		pickContactUtil.startGetContacter();
	}

	/**
	 * 手动触发一次定位请求
	 */
	public void requestLocClick() {
		isRequest = true;
		mLocClient.requestLocation();
		Toast.makeText(this, "正在定位……", Toast.LENGTH_SHORT).show();
	}

	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
	class MyGeneralListener implements MKGeneralListener {

		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				Toast.makeText(getApplicationContext(), "您的网络出错啦！",
						Toast.LENGTH_LONG).show();
			}
			else if (iError == MKEvent.ERROR_NETWORK_DATA) {
				Toast.makeText(getApplicationContext(), "输入正确的检索条件！",
						Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onGetPermissionState(int iError) {
			// 非零值表示key验证未通过
			if (iError != 0) {
				// 授权Key错误：
				Toast.makeText(getApplicationContext(),
						"请在 DemoApplication.java文件输入正确的授权Key,并检查您的网络连接是否正常！error: " + iError, Toast.LENGTH_LONG).show();
			}
			else {
				Toast.makeText(getApplicationContext(),
						"key认证成功", Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	public void testWebHandler(View view){
		String s = SimpleWebUtil.request("http://10.68.35.6/updateContacterLoc.do?myNumber=111", "GET");
		TextView tv = (TextView)findViewById(R.id.testWebTxt);
		tv.setText(s);
	}

}
