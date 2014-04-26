package com.findher.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
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

public class Main extends Activity {
	private BMapManager mBMapManager = null;
	private MapView mMapView = null;
	private MapController mMapController = null;

	private ArrayList<OverlayItem> mItems = null;

	// 定位相关
	private LocationClient mLocClient;
	private LocationData locData = null;
	public MyLocationListenner myListener = new MyLocationListenner();
	// 定位图层
	private MyLocationOverlay myLocationOverlay = null;

	boolean isRequest = false;// 是否手动触发请求定位
	boolean isFirstLoc = true;// 是否首次定位

	// 自定义图层
	private ItemizedOverlay itemizedOverlay = null;
	private OverlayItem item;
	// 位置
	private double lat_she = 30.292492;
	private double lon_she = 120.068873;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBMapManager = new BMapManager(getApplicationContext());
		mBMapManager.init(new MyGeneralListener());
		setContentView(R.layout.activity_main);
		setTitle("我的位置");

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.baseMapView);
		mMapController = mMapView.getController();
		mMapView.getController().setZoom(14);
		mMapView.getController().enableClick(true);
		mMapView.setBuiltInZoomControls(true);

		// 定位初始化
		mLocClient = new LocationClient(this);
		locData = new LocationData();
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
		myLocationOverlay.setData(locData);
		// 添加定位图层
		myLocationOverlay.enableCompass();
		
		//对方层
		itemizedOverlay = new ItemizedOverlay(getResources().getDrawable(R.drawable.icon_marka), mMapView);
		GeoPoint point = new GeoPoint((int) (lat_she * 1E6), (int) (lon_she * 1E6));
		item = new OverlayItem(point, "lay1", "");
		item.setMarker(getResources().getDrawable(R.drawable.icon_marka));
		itemizedOverlay.addItem(item);
		
		mMapView.getOverlays().add(myLocationOverlay);
		mMapView.getOverlays().add(itemizedOverlay);
		mMapView.refresh();

		// 定位我的位置
		mLocClient.requestLocation();

		sheStartMoving();
		

//		GeoPoint p = new GeoPoint((int)(39.933859 * 1E6), (int)(116.400191* 1E6));
//        mMapController.setCenter(p);
	}


	private void sheStartMoving() {
		AsyncTask<Integer, Integer, Integer> task = new AsyncTask<Integer, Integer, Integer>() {
			@Override
			protected Integer doInBackground(Integer... vals) {
				while (true) {
					int lat = item.getPoint().getLatitudeE6();
					int lon = item.getPoint().getLongitudeE6();
					if (lat < lat_she * 1E6 + 1000 * 10) {
						GeoPoint p = new GeoPoint(lat + 1000, lon + 1000);
						item.setGeoPoint(p);
					} else {
						GeoPoint p = new GeoPoint((int) (lat_she * 1E6), (int) (lon_she * 1E6));
						item.setGeoPoint(p);
					}
					itemizedOverlay.updateItem(item);
					mMapView.refresh();
					try {
						Thread.sleep(2 * 1000);
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

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;

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
		}

		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}

	// 选择联系人
	public void selectContactHandler(View view) {

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
			// ...
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

}
