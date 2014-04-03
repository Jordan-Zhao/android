package io.yunos.bbs.layout;

import io.yunos.bbs.MainActivity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class CustomViewPager extends ViewPager {

	private GestureDetector mGestureDetector;
	View.OnTouchListener mGestureListener;

	private int WIDTH;
	private int HEIGHT;
	final int RIGHT = 0;
	final int LEFT = 1;
	private boolean isCanScroll = true;
	private Context mContext;

	public CustomViewPager(Context context) {
		super(context);
		mContext = context;
		mGestureDetector = new GestureDetector(new XYScrollDetector());
	}

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mGestureDetector = new GestureDetector(new XYScrollDetector());
	}

	/**
	 * 获取屏幕长宽
	 * */
	private void getWidthAndHeight() {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(dm);
		WIDTH = dm.widthPixels;
		HEIGHT = dm.heightPixels;
	}

	public void setScanScroll(boolean isCanScroll) {
		this.isCanScroll = isCanScroll;
	}


	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (isCanScroll == false) {
			return mGestureDetector.onTouchEvent(ev);
		} else {
			return super.onInterceptTouchEvent(ev);
		}

	}

	class XYScrollDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			float x = e2.getX() - e1.getX();
            float y = e2.getY() - e1.getY();
            Log.i("1", ""+e2.getX()+" "+e1.getX());
            //限制必须得划过屏幕的1/2才能算划过
            float x_limit = 150;
            Log.i("1", ""+"x_limit"+x_limit);
            float x_abs = Math.abs(x);
            float y_abs = Math.abs(y);
            if(x_abs >= y_abs){
                //gesture left or right
                if(x_abs > x_limit || x_abs < -x_limit){
                    if(x>0){
                        //right
                        MainActivity.doResult(RIGHT); 
                    }else if(x<=0){
                        //left
                        MainActivity.doResult(LEFT); 
                    }
                }
            }
			return super.onFling(e1, e2, velocityX, velocityY);
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.i("1", ""+e.getX());
			return false;
		}
	}
	
	
}