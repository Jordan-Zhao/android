package com.findher.framework;

import android.content.Intent;

public interface ActivityOnActivityResultHandler {
	void onActivityResult(int reqCode, int resCode, Intent data, ActivityLifeCallback callback);
}
