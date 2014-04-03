package io.yunos.bbs;

import io.yunos.bbs.layout.CustomViewPager;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

	private static final String TAG = "MainActivity";

	private TabHost tabHost; // TabHost
	private List<View> views; // ViewPager内的View对象集合
	private FragmentManager manager; // Activity管理器
	private static CustomViewPager pager; // ViewPager
	private static Context mContext;
	private View view;
	private TextView tv;

	final static int RIGHT = 0;
	final static int LEFT = 1;

	public static final String[] PAGES = new String[] { "page1", "page2",
			"page3", "page4" };
	public static final int[] TAB_NAME_ID = new int[] { R.string.picked,
			R.string.hot, R.string.plates, R.string.person_center };
	public static final int TAB_NUM = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = (Context) this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		pager = (CustomViewPager) findViewById(R.id.viewpager);
		tabHost = (TabHost) findViewById(R.id.tab_host);
		manager = getSupportFragmentManager();
		views = new ArrayList<View>();

		tabHost.setup();

		views.add(manager.findFragmentById(R.id.picked_fragment).getView());
		views.add(manager.findFragmentById(R.id.hot_fragment).getView());
		views.add(manager.findFragmentById(R.id.plates_fragment).getView());
		views.add(manager.findFragmentById(R.id.person_center_fragment)
				.getView());

		TabContentFactory factory = new TabContentFactory() {
			@Override
			public View createTabContent(String tag) {
				return new View(MainActivity.this);
			}
		};

		for (int i = 0; i < TAB_NUM; i++) {
			TabSpec tabSpec = tabHost.newTabSpec(PAGES[i]);
			tabSpec.setIndicator(createTabView(TAB_NAME_ID[i]));
			tabSpec.setContent(factory);
			tabHost.addTab(tabSpec);
		}

		//pager.setScanScroll(false);// 屏蔽ViewPager滑动
		pager.setOffscreenPageLimit(3);
		pager.setAdapter(new PageAdapter());
		pager.setOnPageChangeListener(new PageChangeListener());
		tabHost.setOnTabChangedListener(new TabChangeListener());

		for (int i = 0; i < TAB_NUM; i++) {

			view = tabHost.getTabWidget().getChildAt(i);
			tv = (TextView) tabHost.getTabWidget().getChildAt(i)
					.findViewById(R.id.tab_text);

			int color = getResources().getColor(R.color.header_text);

			if (i == 0) {
				color = getResources().getColor(R.color.green);
				pager.setCurrentItem(i);
			}
			tv.setTextColor(color);
			// 修改TextView的属性

		}
		tabHost.setCurrentTab(0);
	}

	private class PageAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object arg2) {
			container.removeView(views.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(views.get(position));
			return views.get(position);
		}
	}

	/**
	 * 标签页点击切换监听器
	 * 
	 * @author Administrator
	 * 
	 */
	private class TabChangeListener implements OnTabChangeListener {
		@Override
		public void onTabChanged(String tabId) {

			for (int i = 0; i < TAB_NUM; i++) {

				view = tabHost.getTabWidget().getChildAt(i);
				tv = (TextView) tabHost.getTabWidget().getChildAt(i)
						.findViewById(R.id.tab_text);

				int color = getResources().getColor(R.color.header_text);

				if (PAGES[i].equals(tabId)) { // 如果是选定tab，则修改之
					color = getResources().getColor(R.color.green);
					pager.setCurrentItem(i);
				}
				tv.setTextColor(color);
				// 修改TextView的属性

			}
		}
	}

	/**
	 * ViewPager滑动切换监听器
	 * 
	 * @author Administrator
	 * 
	 */
	private class PageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int arg0) {
			tabHost.setCurrentTab(arg0);
		}
	}

	/**
	 * 创建tab View
	 * 
	 * @param string
	 * @return
	 */
	private View createTabView(int stringId) {
		View tabView = getLayoutInflater().inflate(R.layout.tab, null);
		TextView textView = (TextView) tabView.findViewById(R.id.tab_text);
		textView.setText(stringId);
		return tabView;
	}

	public static Context getContext() {
		return mContext;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 弹出确定退出对话框
			new AlertDialog.Builder(this)
					.setMessage(R.string.sure_to_exit)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									finish();
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									dialog.cancel();
								}
							}).show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public static void doResult(int action) {

		int mCurrentViewID = pager.getCurrentItem();
		switch (action) {
		case RIGHT:

			if (mCurrentViewID != 0) {
				mCurrentViewID--;
				pager.setCurrentItem(mCurrentViewID, true);
			}

			break;

		case LEFT:
			if (mCurrentViewID != 3) {
				mCurrentViewID++;
				pager.setCurrentItem(mCurrentViewID, true);
			}
			break;

		}
	}
}
