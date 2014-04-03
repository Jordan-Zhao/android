package io.yunos.bbs;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;

public class NetworkUtils {	
	private static String APN_MOBILE_WAP = "wap";
	private static String APN_MOBILE_NET = "net";
	
	public static boolean isWifi(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}
	
	public static boolean isMobileNetwork(Context context){
		if(getNetworkType(context) == ConnectivityManager.TYPE_MOBILE){
			return true;
		}
		return false;
	}
	
	public static boolean is2GNetwork(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null 
				&& 
				(activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_EDGE
					|| activeNetInfo.getType() == TelephonyManager.NETWORK_TYPE_GPRS 
					|| activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_CDMA)
				){
			return true;
		}
		return false;
    }
	
	public static boolean is3GNetwork(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null 
				&& (activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_UMTS
						|| activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_HSDPA
						|| activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_HSPA
						|| activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_HSUPA
						|| activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_EVDO_B
						|| activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_EVDO_A
						|| activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_HSPAP
						|| activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_LTE)) {
			return true;
		}
		return false;
    }
	
	public static boolean isWapNetwork(Context context){
		String apnNet = getCurrentApnName(context);
		if(APN_MOBILE_WAP.equals(apnNet)){
			return true;
		}
		return false;
	}
	
	public static boolean isNetNetwork(Context context){
		String apnNet = getCurrentApnName(context);
		if(APN_MOBILE_NET.equals(apnNet)){
			return true;
		}
		return false;
	}
	
	public static NetworkInfo getActiveNetworkInfo(Context context) {
		ConnectivityManager connectManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectManager == null) {
			return null;
		}
		return connectManager.getActiveNetworkInfo();
	}
	
	public static boolean isNetworkAvailable(Context context) {
		NetworkInfo info = getActiveNetworkInfo(context);
		if (info == null) {
			return false;
		}
		int networkType = info.getType();
		switch (networkType) {
		case ConnectivityManager.TYPE_WIFI:
		case ConnectivityManager.TYPE_WIMAX:
			return info.isAvailable();
		default:
			return info.isConnected();
		}
	}
	
	public static void startNetworkDialog(Context context){
		Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	
	private static String getCurrentApnName(Context context){
		NetworkInfo networkInfo = getActiveNetworkInfo(context);
		if(networkInfo != null){
			if(networkInfo.getState() == NetworkInfo.State.CONNECTED
					&& networkInfo.getExtraInfo() != null
					&& networkInfo.getExtraInfo().toLowerCase().endsWith("wap")){
				return APN_MOBILE_WAP;
			}
			else{
				return APN_MOBILE_NET;
			}
		}
		return "";
	}
	
	private static int getNetworkType(Context context){
		NetworkInfo networkInfo = getActiveNetworkInfo(context);
		if(networkInfo != null){
			return networkInfo.getType();
		}
		return -1;
		
	}
}

