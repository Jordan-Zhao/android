package com.findher.common;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;

public class OsUtil {
	private Activity activity;

	public OsUtil(Activity activity) {
		this.activity = activity;
	}
	
	//获取本机号码
	public String getMyPhoneNumber() {
		TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceid = tm.getDeviceId();
		String tel = tm.getLine1Number();// 手机号码
		return tel == null?"13888888888":tel;
//		String imei = tm.getSimSerialNumber();
//		String imsi = tm.getSubscriberId();
//		tv.setText("deviceid:" + deviceid + ";tel:" + tel + ";imei:" + imei + "imsi:" + imsi);
	}
}
