package com.zjd.demo2;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

public class MenuBarActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_bar);
		
		ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		SpinnerAdapter sAdapter = ArrayAdapter.createFromResource(this, R.array.barMenuList, R.layout.menu_item);
		bar.setListNavigationCallbacks(sAdapter, new DropDownListenser());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_bar, menu);
		return true;
	}
	
	class DropDownListenser implements OnNavigationListener      
	{          
	  // �õ���SpinnerAdapter��һ�µ��ַ�����         
//	  String[] listNames = getResources().getStringArray(R.array.student);          
	  /* ��ѡ�������˵����ʱ�򣬽�Activity�е������û�Ϊ��Ӧ��Fragment */          
	  public boolean onNavigationItemSelected(int itemPosition, long itemId)          
	  {              
	   Log.i("MenuBarActivity","position:"+itemPosition+";id:"+itemId);      
	   return true;          
	  } 
	}

}
