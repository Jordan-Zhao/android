package com.zjd.demo2;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ContactPickActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_pick);
		Intent orgIntent = getIntent();
		Uri queryUri = orgIntent.getData();
		final Cursor c = managedQuery(queryUri, null, null, null, null);
		String[] fromColumns = new String[] { ContactsContract.Contacts.DISPLAY_NAME };
		int[] toLayoutIDs = new int[] { R.id.contactItemTxt };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.contact_pick_item, c, fromColumns, toLayoutIDs);
		ListView lv = (ListView) findViewById(R.id.contactPickList);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				c.moveToPosition(pos);
				int rowId = c.getInt(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
				Uri outURI = Uri.parse(ContactsContract.Contacts.CONTENT_URI.toString() + rowId);
				Intent outData = new Intent();
				outData.setData(outURI);
				setResult(Activity.RESULT_OK, outData);
				finish();
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_pick, menu);
		return true;
	}

}
