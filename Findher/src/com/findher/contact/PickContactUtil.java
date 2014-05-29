package com.findher.contact;

import com.findher.common.Constants;

import android.app.Activity;
import android.content.Intent;
import android.provider.ContactsContract;

public class PickContactUtil {
	private Activity activity;
	public PickContactUtil(Activity activity){
		this.activity = activity;
	}
	
	/**
	 * 从通讯录中选择联系人，通过GetContacterResultHandler来接收数据Contacter
	 */
	public void startGetContacter(){
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		activity.startActivityForResult(intent, Constants.CODE_GET_CONTACT);
	}
}
