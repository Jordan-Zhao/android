package io.yunos.bbs;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Plates extends Fragment {

	private static final int LIST_WHAT = 0x0001;

	private static String TAG = "Plates";
	private Context mContext;
	private ListView listView;
	private BbsHandler handler;
	private BaseAdapter baseAdapter;
	private ArrayList<BbsPlateData> data;
	// 网络是否可用
	private boolean networkState = true;
	// 是否在加载数据
	private boolean isLoading = false;

	@Override
	public android.view.View onCreateView(android.view.LayoutInflater inflater,
			android.view.ViewGroup container,
			android.os.Bundle savedInstanceState) {
		return inflater.inflate(R.layout.plates_layout, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mContext = (Context) getActivity().getApplicationContext();
		listView = (ListView) getActivity().findViewById(R.id.plates_list);

		// 获取网络状态
		networkState = isNetworkAvailable(mContext);
		Log.d(TAG, "networkState = " + networkState);

		setBbsPlateData();

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(mContext, PlateList.class);
				Bundle bundle = new Bundle();
				bundle.putString("fid", data.get(position).getFid());
				bundle.putString("url", data.get(position).getImageurl());
				bundle.putString("title", data.get(position).getTitle());
				intent.putExtras(bundle);
				startActivity(intent);
			}

		});

		// 注册广播接收器
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.registerReceiver(mReceiver, mFilter);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//注销广播接收器
		mContext.unregisterReceiver(mReceiver);
	}
	

	/**
	 * Generate setters for data
	 * 
	 * @param data
	 */
	public void setData(ArrayList<BbsPlateData> data) {
		this.data = data;
	};

	/**
	 * Set adapter for plates listview
	 */
	public void setListViewAdapter() {
		baseAdapter = new PlatesListViewAdapter(mContext, data);
		listView.setAdapter(baseAdapter);
		isLoading = false;
	}

	/**
	 * New thread to get the data from network to display on the ListView
	 */
	private void setBbsPlateData() {
		// start handler to handle message
		handler = new BbsHandler(this);

		// start thread for network data
		Thread thread = new Thread(new BbsRunnable());
		isLoading = true;
		if (!NetworkUtils.isNetworkAvailable(mContext)) {
			Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		thread.start();
	}

	class BbsRunnable implements Runnable {

		@Override
		public void run() {
			// Get bbs plate data
			ArrayList<BbsPlateData> data = ParseBbsForumData.getBbsForumData();

			// Send msg to handler
			handler.sendMessage(handler.obtainMessage(LIST_WHAT, data));
		}

	}

	/**
	 * Static inner class handler to resolve leaks
	 * 
	 * @author zhengyi.wzy
	 */
	private static class BbsHandler extends Handler {
		private final WeakReference<Plates> plates;

		public BbsHandler(Plates plates) {
			super();
			this.plates = new WeakReference<Plates>(plates);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			Plates outclass = plates.get();
			if (msg.what == LIST_WHAT) {
				ArrayList<BbsPlateData> data = (ArrayList<BbsPlateData>) msg.obj;
				outclass.setData(data);
				outclass.setListViewAdapter();
			}
		}

	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			Log.d(TAG, "Unavailabel");
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						Log.d(TAG, "Availabel");
						return true;
					}
				}
			}
		}
		return false;
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				Log.d(TAG, "网络状态已经改变");
				// 若网络重新联通，且数据加载未完成则重新加载
				if (networkState == false && (isNetworkAvailable(context))
						&& isLoading) {
					Log.d(TAG, "setBbsPlateData()");
					setBbsPlateData();
					networkState = true;
				} else if (isNetworkAvailable(mContext) == false) {
					networkState = false;
				}
			}
		}
	};
}
