package com.xiang.tuanshopping.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.xiang.tuanshopping.R;
import com.xiang.tuanshopping.adapter.ImageListAdapter;
import com.xiang.tuanshopping.adapter.TypeAdapter;
import com.xiang.tuanshopping.application.DataApplication;
import com.xiang.tuanshopping.bean.Merchs;
import com.xiang.tuanshopping.bean.Page;
import com.xiang.tuanshopping.util.PullToRefreshView;
import com.xiang.tuanshopping.util.PullToRefreshView.OnFooterRefreshListener;
import com.xiang.tuanshopping.util.PullToRefreshView.OnHeaderRefreshListener;
import com.xiang.tuanshopping.util.TuanJsonParser;
import com.xiang.tuanshopping.util.Utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

public class TuangouActivity extends Activity {
	private long exitTime = 0;

	private GridView gv_type;
	private ArrayList<Integer> types;
	private TypeAdapter typeAdapter;
	private static final int MESSAGE_INIT_DATA_SUCCESS = 1;

	// ���ſ���API�ӿ�
	private final String URI = "";
	private ImageView iv_map;
	private ImageView iv_search;

	private DataApplication dataApp = null;
	// ��Ʒ��Ϣ
	private List<Merchs> merchsList = null;
	// ��Ʒ��Ϣ������
	private ImageListAdapter merchAdapter = null;
	// ������ݵ�ַ
	public static final String path = "http://192.168.2.99:8080/TuanShoppingServer/mypack/merchsAction_getAllMerchsList";
	private TextView tv_merchs_info;
	private ListView lv_like_shop;

	private LayoutInflater inflater;
	private int headerHeight; // ͷ�߶�
	private int lastHeaderPadding; // ���һ�ε���Move Header��Padding
	private boolean isBack; // ��Release ת�� pull
	private int headerState = DONE; // ͷ��״̬
	static final private int RELEASE_To_REFRESH = 0; // �ͷ�ˢ��:һֱ������Ļʱ��ʾ
	static final private int PULL_To_REFRESH = 1; // ����ˢ�£��ſ���Ļ����ʾ
	static final private int REFRESHING = 2; // ����ˢ��
	static final private int DONE = 3;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.first);
		initView();// ��ʼ�����ؼ�
		initCategory();// ��ʼ������
		setListener();//��ؼ����ü���
		// �����̲߳�ѯ���
		new Thread(new InitDataTask()).start();
	}

	private Handler handler = new Handler() {
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.what) {
			// ��ʼ����ݳɹ�
			case MESSAGE_INIT_DATA_SUCCESS:
				// ����ݵ�������
				merchAdapter = new ImageListAdapter(TuangouActivity.this,
						merchsList);
				lv_like_shop.setAdapter(merchAdapter);
				// ������ɣ����ؼ��ض���
				tv_merchs_info.setVisibility(View.GONE);
				Utility.setListViewHeightBasedOnChildren(lv_like_shop);
				break;
			}
		};
	};
	private ScrollView sc;
	private LinearLayout globleLayout;
	private LinearLayout header;
	private Animation anim;
	private ImageView iv_anim_first;
	private ImageView iv_header_fresh_anim;
	private TextView tv_text;
	private AnimationDrawable ad;

	/**
	 * ��ʼ�����ؼ�
	 */
	public void initView() {
		// ��ͷ��������ImageView���õ���¼�����
		iv_map = (ImageView) findViewById(R.id.iv_map);
		iv_search = (ImageView)findViewById(R.id.iv_search);
		// ��ʼ���м���Ʒ��Ϣ�ؼ�
		tv_merchs_info = (TextView) findViewById(R.id.tv_load_info);
		lv_like_shop = (ListView) findViewById(R.id.lv_like_shop);

		// ScrollView
		sc = (ScrollView) findViewById(R.id.sv_first_sc);
		// ���岼��
		globleLayout = (LinearLayout) findViewById(R.id.globleLayout);
		// ���ּ�����
		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// ͷ������
		header = (LinearLayout) inflater.inflate(R.layout.first_header, null);
		tv_text = (TextView) header.findViewById(R.id.tv_first_refresh_text);
		iv_header_fresh_anim = (ImageView) header
				.findViewById(R.id.iv_header_anim);
		iv_header_fresh_anim.setBackgroundResource(R.drawable.frame);
		ad = (AnimationDrawable) iv_header_fresh_anim.getBackground();
		// ͷ������
		anim = AnimationUtils.loadAnimation(this, R.anim.rotate);
		// ����Ӧ�õ��Ŀؼ�
		iv_anim_first = (ImageView) header.findViewById(R.id.iv_first_refresh);
		// ����ͷ���߶�
		measureView(header);
		headerHeight = header.getMeasuredHeight();
		lastHeaderPadding = (-1 * headerHeight);
		header.setPadding(10, lastHeaderPadding, 0, 20);
		header.invalidate();
		// ���ͷ������
		globleLayout.addView(header, 0);
		anim.setFillAfter(true);// ��������󱣳ֶ���
		// ΪScrollView�󶨼���
		sc.setOnTouchListener(new OnTouchListener() {
			private int beginY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_MOVE:
					/**
					 * sc.getScrollY == 0 scrollview ������ͷ�� lastHeaderPadding >
					 * (-1*headerHeight) ��ʾheader��û��ȫ��������ʱ headerState !=
					 * REFRESHING����ˢ��ʱ
					 */
					if ((sc.getScrollY() == 0 || lastHeaderPadding > (-1 * headerHeight))
							&& headerState != REFRESHING) {
						// �õ�������Y�����
						int interval = (int) (event.getY() - beginY);
						// �����»����������ϻ���
						if (interval > 0) {
							interval = interval / 2;// �»�����
							lastHeaderPadding = interval + (-1 * headerHeight);
							header.setPadding(10, lastHeaderPadding, 0, 20);
							if (lastHeaderPadding > 0) {
								// txView.setText("��Ҫˢ�¿�");
								headerState = RELEASE_To_REFRESH;
								// �Ƿ��Ѿ�������UI
								if (!isBack) {
									isBack = true; // ����Release״̬�������ػ�������pull����������
									changeHeaderViewByState();
								}
							} else {
								headerState = PULL_To_REFRESH;
								changeHeaderViewByState();
								// txView.setText("��������Ŷ");
								// sc.scrollTo(0, headerPadding);
							}
						}
					}
					break;
				case MotionEvent.ACTION_DOWN:
					// �����»�������ʵ�ʻ�������Ĳ���ֵ��
					beginY = (int) ((int) event.getY() + sc.getScrollY() * 1.5);
					break;
				case MotionEvent.ACTION_UP:
					if (headerState != REFRESHING) {
						switch (headerState) {
						case DONE:
							// ʲôҲ����
							break;
						case PULL_To_REFRESH:
							headerState = DONE;
							lastHeaderPadding = -1 * headerHeight;
							header.setPadding(10, lastHeaderPadding, 0, 0);
							changeHeaderViewByState();
							break;
						case RELEASE_To_REFRESH:
							isBack = false; // ׼����ʼˢ�£���ʱ��������ػ���
							headerState = REFRESHING;
							changeHeaderViewByState();
							onRefresh();
							break;
						default:
							break;
						}
					}
					break;
				}
				// ���Header����ȫ�����ص�����ScrollView��������¼��������Ļ�������¼�
				if (lastHeaderPadding > (-1 * headerHeight)
						&& headerState != REFRESHING) {
					return true;
				} else {
					return false;
				}

			}

		});
	}

	private void onRefresh() {
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				try {
					Thread.sleep(2000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				onRefreshComplete();
			}

		}.execute();
	}

	public void onRefreshComplete() {
		headerState = DONE;
		changeHeaderViewByState();
	}

	/**
	 * ��ʼ������
	 */
	public void initCategory() {
		gv_type = (GridView) findViewById(R.id.gv_type);

		types = new ArrayList<Integer>();
		types.add(R.drawable.ic_category_0);
		types.add(R.drawable.ic_category_1);
		types.add(R.drawable.ic_category_2);
		types.add(R.drawable.ic_category_3);
		types.add(R.drawable.ic_category_4);
		types.add(R.drawable.ic_category_5);
		types.add(R.drawable.ic_category_6);
		types.add(R.drawable.ic_category_7);

		typeAdapter = new TypeAdapter(types, this);

		gv_type.setAdapter(typeAdapter);
		gv_type.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					Intent food = new Intent(TuangouActivity.this,
							FoodActivity.class);
					startActivity(food);
					break;
				case 1:
					Intent movie = new Intent(TuangouActivity.this,
							MovieActivity.class);
					startActivity(movie);
					break;
				case 2:
					Intent hotel = new Intent(TuangouActivity.this,
							HotelActivity.class);
					startActivity(hotel);
					break;
				case 3:
					Intent ktv = new Intent(TuangouActivity.this,
							KtvActivity.class);
					startActivity(ktv);
					break;
				case 4:
					Intent health = new Intent(TuangouActivity.this,
							HealthActivity.class);
					startActivity(health);
					break;
				case 5:
					Intent amusement = new Intent(TuangouActivity.this,
							AmusementActivity.class);
					startActivity(amusement);
					break;
				case 6:
					Intent today = new Intent(TuangouActivity.this,
							TodayActivity.class);
					startActivity(today);
					break;
				case 7:
					Intent all = new Intent(TuangouActivity.this,
							AllActivity.class);
					startActivity(all);
					break;

				default:
					break;
				}

			}
		});
	}

	/**
	 * ���̲߳�ѯ��Ʒ���
	 */
	class InitDataTask implements Runnable {
		@Override
		public void run() {
//			merchsList = new ArrayList<Merchs>();
			Log.d("geek", "�����߳�");
			// �������:���ܷ���˵����
			merchsList = TuanJsonParser.parse(TuangouActivity.path);
			Log.d("geek", "��С��" + merchsList.size());
			// ��ѯ���˽���ݸ�������
			handler.sendEmptyMessage(MESSAGE_INIT_DATA_SUCCESS);
		}

	}

	/**
	 * ���ؼ��ļ���
	 */
	public void setListener() {
		iv_map.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				iv_map.setBackgroundColor(getResources().getColor(
						R.color.abs__background_holo_light));
				Intent intent = new Intent(TuangouActivity.this,
						MapActivity.class);
				startActivity(intent);
			}
		});
		iv_search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});
	}

	/**
	 * �õ�ͷ���߶�,onCreate����ò���
	 */
	private void measureView(View childView) {
		LayoutParams p = childView.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int height = p.height;
		int childHeightSpec;
		if (height > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(height,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		childView.measure(childWidthSpec, childHeightSpec);
	}

	/**
	 * ͨ��״̬���ı�ͷ����ͼ
	 */
	private void changeHeaderViewByState() {
		switch (headerState) {
		case PULL_To_REFRESH:
			// ����RELEASE_To_REFRESH״̬ת������
			if (isBack) { // ������
				isBack = false;
				// ��������
				iv_anim_first.startAnimation(anim);
				ad.start();
				tv_text.setText("����ˢ��");
			}
			tv_text.setText("����ˢ��");
			break;
		case RELEASE_To_REFRESH: // �����ϣ�����ֻ���ұߵĽ�ȶ���
			iv_anim_first.setVisibility(View.VISIBLE);
			iv_header_fresh_anim.setVisibility(View.VISIBLE);
			tv_text.setVisibility(View.VISIBLE);
			iv_anim_first.startAnimation(anim); // �ұߵĽ�ȶ���
			tv_text.setText("����ˢ��");
			break;
		case REFRESHING:
			lastHeaderPadding = 0;
			header.setPadding(10, lastHeaderPadding, 0, 20);
			header.invalidate();
			iv_header_fresh_anim.setVisibility(View.VISIBLE);
			iv_anim_first.setVisibility(View.VISIBLE);
			tv_text.setText("������...");
			ad.start();
			break;
		case DONE: // ������
			lastHeaderPadding = -1 * headerHeight;
			header.setPadding(10, lastHeaderPadding, 0, 20);
			header.invalidate();
			iv_header_fresh_anim.setVisibility(View.GONE);
			tv_text.setText("����ˢ��");
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void exit() {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����",
					Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			finish();
			System.exit(0);
		}

	}

}
