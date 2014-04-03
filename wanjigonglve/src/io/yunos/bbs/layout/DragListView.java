package io.yunos.bbs.layout;

import io.yunos.bbs.R;
import io.yunos.bbs.R.id;
import io.yunos.bbs.R.layout;
import io.yunos.bbs.R.string;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/***
 * 自定义拖拉ListView
 * 
 * @author zhangjia
 * 
 */
public class DragListView extends ListView implements OnScrollListener,
		OnClickListener {

	// 点击加载更多枚举所有状态
	private enum DListViewLoadingMore {
		LV_NORMAL, // 普通状态
		LV_LOADING, // 加载状态
		LV_OVER; // 结束状态
	}

	private View mFootView;// 尾部mFootView
	private View mLoadMoreView;// mFootView 的view(mFootView)
	private TextView mLoadMoreTextView;// 加载更多.(mFootView)
	private View mLoadingView;// 加载中...View(mFootView)

	private DListViewLoadingMore loadingMoreState = DListViewLoadingMore.LV_NORMAL;// 加载更多默认状态.

	private OnRefreshLoadingMoreListener onRefreshLoadingMoreListener;// 下拉刷新接口（自定义）
	

	public DragListView(Context context) {
		super(context, null);
		initDragListView(context);
	}

	public DragListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDragListView(context);
	}
	
	// 注入下拉刷新接口
		public void setOnRefreshListener(
				OnRefreshLoadingMoreListener onRefreshLoadingMoreListener) {
			this.onRefreshLoadingMoreListener = onRefreshLoadingMoreListener;
		}

	/***
	 * 初始化ListView
	 */
	public void initDragListView(Context context) {

		initLoadMoreView(context);// 初始化footer

		setOnScrollListener(this);// ListView滚动监听
	}

	/***
	 * 初始化底部加载更多控件
	 */
	private void initLoadMoreView(Context context) {
		mFootView = LayoutInflater.from(context).inflate(R.layout.load_more_footer, null);

		mLoadMoreView = mFootView.findViewById(R.id.load_more_view);

		mLoadMoreTextView = (TextView) mFootView
				.findViewById(R.id.load_more_tv);

		mLoadingView = (LinearLayout) mFootView
				.findViewById(R.id.loading_layout);

		mLoadMoreView.setOnClickListener(this);

		addFooterView(mFootView);
	}


	
	/***
	 * 点击加载更多
	 * 
	 * @param flag
	 *            数据是否已全部加载完毕
	 */
	public void onLoadMoreComplete(boolean flag) {
		if (flag) {
			updateLoadMoreViewState(DListViewLoadingMore.LV_OVER);
		} else {
			updateLoadMoreViewState(DListViewLoadingMore.LV_NORMAL);
		}

	}

	// 更新Footview视图
	private void updateLoadMoreViewState(DListViewLoadingMore state) {
		switch (state) {
		// 普通状态
		case LV_NORMAL:
			mLoadingView.setVisibility(View.GONE);
			mLoadMoreTextView.setVisibility(View.VISIBLE);
			mLoadMoreTextView.setText(getResources().getString(R.string.load_more));
			break;
		// 加载中状态
		case LV_LOADING:
			mLoadingView.setVisibility(View.VISIBLE);
			mLoadMoreTextView.setVisibility(View.GONE);
			break;
		// 加载完毕状态
		case LV_OVER:
			mLoadingView.setVisibility(View.GONE);
			mLoadMoreTextView.setVisibility(View.VISIBLE);
			mLoadMoreTextView.setText(getResources().getString(R.string.no_more));
			break;
		default:
			break;
		}
		loadingMoreState = state;
	}

	/***
	 * ListView 滑动监听
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}

	/***
	 * 底部点击事件
	 */
	@Override
	public void onClick(View v) {
		// 防止重复点击
		if (onRefreshLoadingMoreListener != null
				&& loadingMoreState == DListViewLoadingMore.LV_NORMAL) {
			updateLoadMoreViewState(DListViewLoadingMore.LV_LOADING);
			onRefreshLoadingMoreListener.onLoadMore();// 对外提供方法加载更多.
		}

	}

	/***
	 * 自定义接口
	 */
	public interface OnRefreshLoadingMoreListener {

		/***
		 * 点击加载更多
		 */
		void onLoadMore();
	}

}
