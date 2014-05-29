package com.findher.contact;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.findher.common.Constants;
import com.findher.framework.ActivityLifeCallback;
import com.findher.framework.ActivityOnActivityResultHandler;

public class GetContacterResultHandler implements ActivityOnActivityResultHandler {
	private Activity activity;

	public GetContacterResultHandler(Activity activity) {
		this.activity = activity;
	}

	/**
	 * 接收选中的联系人数据
	 */
	public void onActivityResult(int reqCode, int resCode, Intent data, ActivityLifeCallback callbackHandler) {
		switch (reqCode) {
			case (Constants.CODE_GET_CONTACT): {
				if (resCode == Activity.RESULT_OK) {
					Contacter contacter = new Contacter();
					Uri contactData = data.getData();
					Cursor c = activity.getContentResolver().query(contactData, null, null, null, null);
					c.moveToFirst();
					// 姓名
					String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					contacter.setName(name);
					// 电话号码
					int count = c.getInt(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
					if (count > 0)
					{
						String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID)); // 获得联系人的ID号
						// 获得联系人的电话号码的cursor
						Cursor phoneCur = activity.getContentResolver().query(
								ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
								null,
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
								null, null);
						if (phoneCur.moveToFirst())
						{
							List<String> phoneNumberList = new ArrayList<String>();
							// 遍历所有的电话号码
							for (; !phoneCur.isAfterLast(); phoneCur.moveToNext())
							{
								String phoneNumber = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
								phoneNumberList.add(phoneNumber);
								// int typeindex =
								// phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
								// int phone_type = phoneCur.getInt(typeindex);
								// nums += ("type=>"+ phone_type +
								// ",号码=>"+phoneNumber + ";");
							}
							contacter.setPhones(phoneNumberList);
							if (!phoneCur.isClosed())
							{
								phoneCur.close();
							}
						}
					}
					if (!c.isClosed()) {
						c.close();
					}
					callbackHandler.callback(contacter);
				}
				break;
			}
		}
	}
}
