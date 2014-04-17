package com.zjd.demo2;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;

public class TabBarActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_tab_bar);
		ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		bar.setDisplayOptions(0,ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_SHOW_TITLE );	//Òþ²Ø±êÌâÀ¸
		
		Tab taba = bar.newTab().setText("a tab");
		FragementA fa = new FragementA();
		taba.setTabListener(new MyTabsListener(fa));
		bar.addTab(taba);
		
		
		Tab tabb = bar.newTab().setText("b tab");
		FragementB fb = new FragementB();
		tabb.setTabListener(new MyTabsListener(fb));
		bar.addTab(tabb);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tab_bar, menu);
		return true;
	}
	
	protected class MyTabsListener implements ActionBar.TabListener  
    {  
        private Fragment fragment;  
        public MyTabsListener(Fragment fragment)  
        {  
            this.fragment = fragment;  
        }  
        @Override  
        public void onTabSelected(Tab tab, FragmentTransaction ft)  
        {  
            ft.replace(R.id.fragActivity, fragment, null);  
        }  
        @Override  
        public void onTabReselected(Tab arg0, FragmentTransaction arg1) {  
            // TODO Auto-generated method stub  
              
        }  
        @Override  
        public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {  
            // TODO Auto-generated method stub  
              
        }  
    }  
}
