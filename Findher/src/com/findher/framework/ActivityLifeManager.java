package com.findher.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;

public class ActivityLifeManager extends Activity {
	private List<Map<ActivityOnActivityResultHandler, ActivityLifeCallback>> resultHandlerList = new ArrayList<Map<ActivityOnActivityResultHandler, ActivityLifeCallback>>();

	@Override
	public void onActivityResult(int reqCode, int resCode, Intent data) {
		super.onActivityResult(reqCode, resCode, data);
		for (Map<ActivityOnActivityResultHandler, ActivityLifeCallback> map : resultHandlerList) {
			ActivityOnActivityResultHandler handler = map.keySet().iterator().next();
			handler.onActivityResult(reqCode, resCode, data, map.get(handler));
		}
	}

	protected void registerResultHandler(ActivityOnActivityResultHandler handler, ActivityLifeCallback callback) {
		Map<ActivityOnActivityResultHandler, ActivityLifeCallback> map = new HashMap<ActivityOnActivityResultHandler, ActivityLifeCallback>();
		map.put(handler, callback);
		resultHandlerList.add(map);
	}
}
