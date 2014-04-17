package com.zjd.demo2.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

public class CounterService extends Service {

	private final String BROADCAST_COUNTER_ACTION = "broadcast.action.counter";

	private CounterServiceBinder binder = new CounterServiceBinder();

	private boolean stop = false;

	@Override
	public void onCreate() {

		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public class CounterServiceBinder extends Binder {
		CounterService getService() {
			return CounterService.this;
		}
	}

	public void startCount() {
		AsyncTask<Integer, Integer, Integer> task = new AsyncTask<Integer, Integer, Integer>() {
			@Override
			protected Integer doInBackground(Integer... vals) {
				Integer initCounter = vals[0];
				stop = false;
				while (!stop) {
					publishProgress(initCounter);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					initCounter++;
				}
				return initCounter;
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				super.onProgressUpdate(values);
				int counter = values[0];
				Intent intent = new Intent(BROADCAST_COUNTER_ACTION);
				intent.putExtra("count", counter);
				sendBroadcast(intent);
			}

			@Override
			protected void onPostExecute(Integer val) {
				int counter = val;
				Intent intent = new Intent(BROADCAST_COUNTER_ACTION);
				intent.putExtra("count", counter);
				sendBroadcast(intent);
			}
		};
		task.execute(0);
	}

	public void stopCount() {
		stop = true;
	}

}
