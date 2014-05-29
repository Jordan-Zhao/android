package com.zjd.demo2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;


public class SendMsgActivity extends Activity {
	private final int REQUEST_CODE_SELECT_CONTACT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_msg);
		
		TextView tv = (TextView)findViewById(R.id.myNumberTxt);
		TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceid = tm.getDeviceId();
        String tel = tm.getLine1Number();//手机号码
        String imei = tm.getSimSerialNumber();
        String imsi = tm.getSubscriberId(); 
        tv.setText("deviceid:"+deviceid+";tel:"+tel+";imei:"+imei+"imsi:"+imsi);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.send_msg, menu);
		return true;
	}

	public void selecteContactHandler(View view) {
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, REQUEST_CODE_SELECT_CONTACT);
	}

	@Override
	public void onActivityResult(int reqCode, int resCode, Intent data) {
		super.onActivityResult(reqCode, resCode, data);
		switch (reqCode) {
			case (REQUEST_CODE_SELECT_CONTACT): {
				if (resCode == Activity.RESULT_OK) {
					Uri contactData = data.getData();
//					Cursor c = managedQuery(contactData, null, null, null, null);
					Cursor c = getContentResolver().query(contactData, null, null, null, null);
					c.moveToFirst();
					//姓名
					String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					//电话号码
					String nums = "";
					int num = c.getInt(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
					if (num > 0) 
				    { 
						// 获得联系人的ID号 
				        int idColumn = c.getColumnIndex(ContactsContract.Contacts._ID); 
				        String contactId = c.getString(idColumn); 
			            // 获得联系人的电话号码的cursor; 
			            Cursor phones = getContentResolver().query( 
			            		ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
			            		null, 
			            		ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ " = " + contactId,  
			            		null, null); 
			            if (phones.moveToFirst()) 
			            { 
		                    // 遍历所有的电话号码 
		                    for (;!phones.isAfterLast();phones.moveToNext()) 
		                    {                                             
		                        int index = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER); 
		                        int typeindex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE); 
		                        int phone_type = phones.getInt(typeindex); 
		                        String phoneNumber = phones.getString(index); 
		                        nums += ("type=>"+ phone_type + ",号码=>"+phoneNumber + ";");
		                    } 
		                    if (!phones.isClosed()) 
		                    { 
		                           phones.close(); 
		                    } 
		            } 
				    } 
					if(!c.isClosed()){
						c.close();
					}
					
					TextView tv = (TextView) findViewById(R.id.sendContactTxt);
					tv.setText(name + "****"+nums);
				}
				break;
			}
		}
	}

}
