package com.zjd.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setFlags(flag, flag);
        
        setContentView(R.layout.activity_main);
        
        TextView tv = (TextView)findViewById(R.id.txtMetrics);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        tv.setText("屏幕分辨率："+metrics.widthPixels + "*"+metrics.heightPixels);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /** 
     * Called when the user clicks the 
     * 确定 
     * */
    public void onOkHandler(View view) {
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.inputName);
        String message = editText.getText().toString();
        intent.putExtra(Constants.KEY_NAME, message);
        editText = (EditText)findViewById(R.id.inputPass);
        intent.putExtra(Constants.KEY_PASS, editText.getText().toString());
        startActivityForResult(intent, Constants.REQ_CODE1);
    }
    
    /** 
     * Called when the user clicks the 
     * 取消
 	 */
    public void onCancelHandler(View view) {
    	return ;
    }
    
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	if(requestCode == Constants.REQ_CODE1){
    		Log.v("get return data", "获取返回的数据");
    		TextView view = (TextView)findViewById(R.id.tv_title);
	        view.setText(data.getStringExtra(Constants.KEY_RETURN)+" return!");
    	}
    }
    
    public void onShowHandler(View view1){
    	//显示toast
        Toast toast = new Toast(this);
        ImageView view = new ImageView(this);
        view.setImageResource(R.drawable.btna);
        toast.setView(view);
        toast.setDuration(3);
        toast.show();
        
        //显示dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setIcon(R.drawable.tip);
        dialogBuilder.setTitle("这是提示标题");
        dialogBuilder.setMessage("这是提示信息");
        dialogBuilder.setPositiveButton(R.string.btn_ok, null);
        dialogBuilder.show();
    }
    
    public void onOpenDemo2Handler(View view){
    	//把目标app中的activity作为组件在当前app中打开，不会打开一个目标app进程
    	Intent intent = new Intent(Intent.ACTION_MAIN);  
    	intent.setComponent(new ComponentName("com.zjd.demo2","com.zjd.demo2.MainActivity")); 
    	Bundle bundle = new Bundle();
    	bundle.putString("key1", "这是value1");
    	intent.putExtras(bundle);
    	startActivity(intent); 
    	
    	//只输入包名，会打开一个目标app的进程
//    	Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.zjd.demo2");  
//    	Bundle bundle = new Bundle();
//    	bundle.putString("key1", "这是value1");
//    	launchIntent.putExtras(bundle);
//    	startActivity(launchIntent); 
    }
    
    //进入demo3页面
    public void onOpenDemo3Handler(View view){
    	Intent intent = new Intent(this,ShowActivity.class);
    	startActivity(intent); 
    }
    
}
