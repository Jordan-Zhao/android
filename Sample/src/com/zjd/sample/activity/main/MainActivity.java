package com.zjd.sample.activity.main;

import java.util.ArrayList;
import java.util.List;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.zjd.sample.R;
import com.zjd.sample.activity.main.vo.TabItem;

public class MainActivity extends TabActivity {
	private TabHost mTabHost;
	private TabWidget mTabWidget;

	List<TabItem> mItems;
	private LayoutInflater mLayoutInflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_Tabhost);
		setContentView(R.layout.main);

		mTabHost = getTabHost();
		mTabWidget = getTabWidget();
		mLayoutInflater = getLayoutInflater();

		prepare();
		initTop();
		initTabSpec();

		mTabHost.setCurrentTab(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * 在初始化TabWidget前调用 和TabWidget有关的必须在这里初始化
	 */
	protected void prepare() {
		TabItem news = new TabItem("新闻", // title
				R.drawable.icon_home, // icon
				R.drawable.example_tab_item_bg, // background
				new Intent(this, MainNewsActivity.class)); // intent

		TabItem vedio = new TabItem("视频",
				R.drawable.icon_selfinfo,
				R.drawable.example_tab_item_bg,
				new Intent(this, MainVedioActivity.class));

		TabItem photo = new TabItem("图片", R.drawable.icon_meassage,
				R.drawable.example_tab_item_bg,
				new Intent(this, MainPhotoActivity.class));

		mItems = new ArrayList<TabItem>();
		mItems.add(news);
		mItems.add(vedio);
		mItems.add(photo);

		// 设置分割线
		mTabWidget.setDividerDrawable(R.drawable.tab_divider);
	}

	private void initTop() {
		View child = mLayoutInflater.inflate(R.layout.main_top, null);
		LinearLayout layout = (LinearLayout) findViewById(R.id.tab_top);
		layout.addView(child);
	}

	private void initTabSpec() {
		for (int i = 0; i < mItems.size(); i++) {
			//设置tab页面
			TabSpec tabSpec = mTabHost.newTabSpec("tab"+i);
			//tabItem菜单区域
			View tabItem = mLayoutInflater.inflate(R.layout.main_tab_item, null);
			TextView tabText = (TextView) tabItem.findViewById(R.id.tab_text);
			tabText.setPadding(3, 3, 3, 3);
			tabText.setText(mItems.get(i).getTitle());
			tabText.setBackgroundResource(mItems.get(i).getBg());
			tabText.setCompoundDrawablesWithIntrinsicBounds(0, mItems.get(i).getIcon(), 0, 0);
			tabSpec.setIndicator(tabItem);
			//内容
			tabSpec.setContent(mItems.get(i).getIntent());// 点击tab时触发的事件
			mTabHost.addTab(tabSpec);
		}

	}
}
