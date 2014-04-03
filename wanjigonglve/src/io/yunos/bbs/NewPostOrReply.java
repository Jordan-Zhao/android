package io.yunos.bbs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.yunos.sdk.account.AccessToken;
import com.yunos.sdk.account.AccessTokenManager;

import android.R.bool;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class NewPostOrReply extends Activity {

	private final String TAG = "NewPost";
	private static final String REPLY_URL = "http://m.bbs.yunos.com/reply/reply.do";
	private static final String PUBLISH_URL = "http://m.bbs.yunos.com/thread/publish.do";

	private Context mContext;
	private GridView emoGridView;
	private EditText post_content;
	private EditText post_title;
	private ImageButton add_emo;
	private Button publish;
	private TextView word_now;
	private RelativeLayout relativeLayout;
	private ImageButton back;
	private TextView head_title;
	private AccessToken mToken;
	private String token = null;

	// 发帖还是回帖，发帖为true，回帖为false;
	private boolean IS_NEW_POST = true;
	private String tid;
	private String fid;
	private String replyTo = null;

	private final int EMO_COUNT = 37;
	private final int WORD_AVAILABLE = 1000;
	private boolean IS_EMO_VISIABLE = false;
	private int currentImageIndex = 0;
	private static final int Request_Code_Carema[] = { 3000, 3001, 3002, 3003,
			3004 };
	private static final int Request_Code_Album[] = { 5000, 5001, 5002, 5003,
			5004 };

	private InputMethodManager imm;

	private final int[] EMO_DRAWABLE_ID = { R.drawable.emo_116,
			R.drawable.emo_117, R.drawable.emo_118, R.drawable.emo_119,
			R.drawable.emo_120, R.drawable.emo_121, R.drawable.emo_122,
			R.drawable.emo_123, R.drawable.emo_124, R.drawable.emo_125,
			R.drawable.emo_126, R.drawable.emo_127, R.drawable.emo_128,
			R.drawable.emo_129, R.drawable.emo_130, R.drawable.emo_131,
			R.drawable.emo_132, R.drawable.emo_133, R.drawable.emo_134,
			R.drawable.emo_135, R.drawable.emo_136, R.drawable.emo_137,
			R.drawable.emo_138, R.drawable.emo_139, R.drawable.emo_140,
			R.drawable.emo_141, R.drawable.emo_142, R.drawable.emo_143,
			R.drawable.emo_144, R.drawable.emo_145, R.drawable.emo_146,
			R.drawable.emo_147, R.drawable.emo_148, R.drawable.emo_149,
			R.drawable.emo_150, R.drawable.emo_151, R.drawable.emo_152, };

	private static final File PHOTO_DIR = new File(Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera");
	private File mPhotoFile;
	private ImageButton[] images = new ImageButton[5];
	private ArrayList<Uri> originalUris = new ArrayList<Uri>();
	private ArrayList<String> imagePaths = new ArrayList<String>();
	private ArrayList<Bitmap> smallPic = new ArrayList<Bitmap>();
	private OSSUtils ossUtils = new OSSUtils();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_post_or_reply_layout);

		Bundle bundle = this.getIntent().getExtras();
		IS_NEW_POST = bundle.getBoolean("IS_NEW_POST");
		if (IS_NEW_POST)
			fid = bundle.getString("fid");
		else
			tid = bundle.getString("tid");

		mContext = (Context) NewPostOrReply.this;
		post_title = (EditText) findViewById(R.id.new_post_title);
		post_content = (EditText) findViewById(R.id.new_post_content);
		add_emo = (ImageButton) findViewById(R.id.new_post_add_emo);
		publish = (Button) findViewById(R.id.new_post_submit);
		word_now = (TextView) findViewById(R.id.new_post_word_now);
		relativeLayout = (RelativeLayout) findViewById(R.id.new_post_relativelayout);
		back = (ImageButton) findViewById(R.id.new_post_back);
		head_title = (TextView) findViewById(R.id.new_post_head_title);

		images[0] = (ImageButton) findViewById(R.id.new_post_image_0);
		images[1] = (ImageButton) findViewById(R.id.new_post_image_1);
		images[2] = (ImageButton) findViewById(R.id.new_post_image_2);
		images[3] = (ImageButton) findViewById(R.id.new_post_image_3);
		images[4] = (ImageButton) findViewById(R.id.new_post_image_4);

		// 如果是回帖则隐藏标题栏
		if (!IS_NEW_POST) {
			post_title.setVisibility(View.GONE);
			post_content.setHint(R.string.reply_hint);
			replyTo = bundle.getString("replyTo");
			head_title.setText(getString(R.string.reply) + replyTo);
			if (replyTo.length() > 0) {
				post_content.setHint(getString(R.string.reply) + " " + replyTo
						+ ":");
				// word_now.setText(replyTo.length()+"");
			}
		}

		emoGridView = (GridView) findViewById(R.id.new_post_emo);
		setAdapter();
		// 添加消息处理
		emoGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				post_content.setText(post_content.getText().toString() + "[s:"
						+ (116 + arg2) + "] ");
			}
		});

		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		// 禁止软键盘自动弹出
		// imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

		add_emo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (IS_EMO_VISIABLE == false) {
					// 收起软键盘
					imm.hideSoftInputFromWindow(post_title.getWindowToken(), 0);
					imm.hideSoftInputFromWindow(post_content.getWindowToken(),
							0);
					emoGridView.setVisibility(View.VISIBLE);
					add_emo.setImageDrawable(getResources().getDrawable(
							R.drawable.add_smile_solid));
					IS_EMO_VISIABLE = true;
				} else {
					emoGridView.setVisibility(View.GONE);
					add_emo.setImageDrawable(getResources().getDrawable(
							R.drawable.add_smile));
					IS_EMO_VISIABLE = false;
				}
			}
		});

		publish.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!NetworkUtils.isNetworkAvailable(mContext)) {
					Toast.makeText(mContext, R.string.no_network,
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (IS_NEW_POST){
					if(post_title.getText().toString().length()!=0&&post_content.getText().toString().length()!=0){
						publish.setClickable(false);
						post_new_thread.start();
					}else{
						contentEmptyDialog(getString(R.string.title_and_content_cannot_be_empty));
					}
				}else{
					if(post_content.getText().toString().length()!=0){
						publish.setClickable(false);
						post_reply_thread.start();
					}else{
						contentEmptyDialog(getString(R.string.reply_cannot_be_empty));
					}
				}
			}

		});

		setTextChangeListener();

		post_title.setOnClickListener(tvOnClickListener);
		post_content.setOnClickListener(tvOnClickListener);
		relativeLayout.setOnClickListener(tvOnClickListener);
		post_title.setOnFocusChangeListener(tvFocusChangeListener);
		post_content.setOnFocusChangeListener(tvFocusChangeListener);

		for (int i = 0; i < 5; i++) {
			images[i].setOnClickListener(imageOnClickListener);
		}

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!"".equals(post_title.getText().toString().trim())
						|| !"".equals(post_content.getText().toString().trim())) {
					sureToExitDialog();
				} else {
					finish();
				}
			}
		});
	}

	Thread post_reply_thread = new Thread(new Runnable() {

		@Override
		public void run() {
			StringBuffer sb = new StringBuffer();
			String reply_content = post_content.getText().toString();
			sb.append(reply_content);
			Log.d(TAG, "start to post reply thread to tid " + tid);

			for (int i = 0; i < imagePaths.size(); i++) {
				// the filename used on OSS storage
				String key = "tid" + tid + "-" + "attachment" + i + ".jpg";
				Log.i(TAG,
						"post_reply_thread upload image file "
								+ imagePaths.get(i));

				// upload image file to aliyun OSS
				StringBuffer ossUrl = new StringBuffer();
				if (ossUtils.pubObjectFromInputStream(key, imagePaths.get(i),
						ossUrl)) {
					// append image oss url to content
					String imgUrl = "<img src=\"" + ossUrl.toString()
							+ "\" /img>";
					sb.append(imgUrl);
					Log.d(TAG, "Upload Reply Image File Success: " + imgUrl);
				} else
					Log.d(TAG, "oss upload file error");
			}

			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			Intent intent = new Intent();
			Bundle bundle = new Bundle();

			mToken = AccessTokenManager.readAccessToken(mContext);
			if (mToken != null && !TextUtils.isEmpty(mToken.getAccessToken())) {
				Log.d(TAG,
						"Token in Post reply, token is :"
								+ mToken.getAccessToken());
				token = mToken.getAccessToken();
				params.add(new BasicNameValuePair("token", token));
			}

			params.add(new BasicNameValuePair("tid", tid));
			params.add(new BasicNameValuePair("content", sb.toString()));
			if (replyTo != null)
				params.add(new BasicNameValuePair("title", "reply to "
						+ replyTo));
			Log.d(TAG, "post_content: " + sb.toString());
			sb.setLength(0);

			if (HttpUtils.doHttpPost(REPLY_URL, params, sb)
					&& HttpUtils.getResponseCode(sb.toString()) == HttpStatus.SC_OK) {
				bundle.putString("response_content", sb.toString());
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
			} else {
				Log.d(TAG, "failed to send reply content to server");
				setResult(RESULT_CANCELED, intent);
			}
			finish();
		}
	});

	Thread post_new_thread = new Thread(new Runnable() {

		@Override
		public void run() {
			StringBuffer sb = new StringBuffer();
			String title = post_title.getText().toString();
			String content = post_content.getText().toString();
			sb.append(content);
			Log.i(TAG, "start to post new thread " + title);

			for (int i = 0; i < imagePaths.size(); i++) {
				// the filename used on OSS storage
				String key = "fid" + fid + "-" + "attachment" + i + ".jpg";

				Log.i(TAG,
						"post_new_thread upload image file "
								+ imagePaths.get(i));
				// upload image file to aliyun OSS
				StringBuffer ossUrl = new StringBuffer();
				if (ossUtils.pubObjectFromInputStream(key, imagePaths.get(i),
						ossUrl)) {
					// append image oss url to content
					String imgUrl = "<img src=\"" + ossUrl.toString()
							+ "\" /img>";
					sb.append(imgUrl);
					Log.d(TAG, "Upload NewPost image file success: " + imgUrl);
				} else
					Log.d(TAG, "oss upload file error");
			}

			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			Intent intent = new Intent();
			Bundle bundle = new Bundle();

			mToken = AccessTokenManager.readAccessToken(mContext);
			if (mToken != null && !TextUtils.isEmpty(mToken.getAccessToken())) {
				Log.d(TAG,
						"Token in Post new, token is :"
								+ mToken.getAccessToken());
				token = mToken.getAccessToken();
				params.add(new BasicNameValuePair("token", token));
			}
			params.add(new BasicNameValuePair("fid", fid));
			params.add(new BasicNameValuePair("title", title));
			params.add(new BasicNameValuePair("content", sb.toString()));
			sb.setLength(0);

			if (HttpUtils.doHttpPost(PUBLISH_URL, params, sb)
					&& HttpUtils.getResponseCode(sb.toString()) == HttpStatus.SC_OK) {
				bundle.putString("response_content", sb.toString());
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
			} else {
				Log.d(TAG, "failed to send new content to server");
				setResult(RESULT_CANCELED, intent);
			}
			finish();
		}
	});

	private OnFocusChangeListener tvFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			Log.i(TAG, "onFocusChange");
			if (hasFocus == true) {
				emoGridView.setVisibility(View.GONE);
				add_emo.setImageDrawable(getResources().getDrawable(
						R.drawable.add_smile));
				IS_EMO_VISIABLE = false;
			}
		}
	};
	private OnClickListener tvOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			emoGridView.setVisibility(View.GONE);
			add_emo.setImageDrawable(getResources().getDrawable(
					R.drawable.add_smile));
			IS_EMO_VISIABLE = false;
		}
	};

	private OnClickListener imageOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			for (int i = 0; i < 5; i++) {
				if (v.equals(images[i])) {
					howToGetPic(i);
				}
			}
		}
	};

	private void setAdapter() {
		ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < EMO_COUNT; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ItemImage", EMO_DRAWABLE_ID[i]);// 添加图像资源的ID
			lstImageItem.add(map);
		}

		// 生成适配器的ImageItem <====> 动态数组的元素，两者一一对应
		SimpleAdapter saImageItems = new SimpleAdapter(this, // 没什么解释
				lstImageItem,// 数据来源
				R.layout.emo_item,// night_item的XML实现

				// 动态数组与ImageItem对应的子项
				new String[] { "ItemImage" },

				// ImageItem的XML文件里面的ImageViewID
				new int[] { R.id.emo_item_image });
		// 添加并且显示
		emoGridView.setAdapter(saImageItems);
	}

	private void setTextChangeListener() {
		post_content.addTextChangedListener(new TextWatcher() {
			private CharSequence temp;
			private int selectionStart;
			private int selectionEnd;

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				temp = s;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				int number = s.length();
				word_now.setText("" + number);
				selectionStart = post_content.getSelectionStart();
				selectionEnd = post_content.getSelectionEnd();
				// System.out.println("start="+selectionStart+",end="+selectionEnd);
				if (temp.length() > WORD_AVAILABLE) {
					word_now.setTextColor(getResources().getColor(R.color.red));
				} else {
					word_now.setTextColor(getResources()
							.getColor(R.color.green));
				}
			}
		});
	}

	// 如何获取图片
	public void howToGetPic(final int index) {
		if (index == currentImageIndex) {
			new AlertDialog.Builder(mContext).setItems(
					new CharSequence[] { getString(R.string.from_camera),
							getString(R.string.from_album),
							getString(R.string.cancel) },
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							switch (item) {
							case 0:
								takePhotoFromCamera(index);
								break;
							case 1:
								takePhotoFromAlbum(index);
								break;
							default:
								break;
							}
						}
					}).show();
		} else {
			new AlertDialog.Builder(mContext).setItems(
					new CharSequence[] { getString(R.string.from_camera),
							getString(R.string.from_album),
							getString(R.string.delete),
							getString(R.string.cancel) },
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							switch (item) {
							case 0:
								takePhotoFromCamera(index);
								break;
							case 1:
								takePhotoFromAlbum(index);
								break;
							case 2:
								deleteImage(index);
							default:
								break;
							}
						}
					}).show();
		}
	}

	public void takePhotoFromCamera(int index) {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
			try {
				// Launch camera to take photo for selected contact
				PHOTO_DIR.mkdirs();// 创建照片的存储目录
				mPhotoFile = new File(PHOTO_DIR, getPhotoFileName());// 给新照的照片文件命名
				final Intent intent = new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
				intent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(mPhotoFile));
				startActivityForResult(intent, Request_Code_Carema[index]);
			} catch (ActivityNotFoundException e) {
			}
		} else {
			Toast.makeText(mContext, R.string.no_sd_card, Toast.LENGTH_SHORT)
					.show();
		}

	}

	/**
	 * 用当前时间给取得的图片命名
	 * 
	 */
	private String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyyMMdd_HHmmss");
		return dateFormat.format(date) + ".jpg";
	}

	public void takePhotoFromAlbum(int index) {
		try {
			// Launch picker to choose photo for selected contact
			final Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
			intent.setType("image/*");
			startActivityForResult(intent, Request_Code_Album[index]);
		} catch (ActivityNotFoundException e) {
		}
	}

	private void deleteImage(int index) {
		if (index >= currentImageIndex) {
			return;
		} else {
			originalUris.remove(index);
			smallPic.remove(index);
			for (int i = 0; i < currentImageIndex - 1; i++) {
				images[i].setImageBitmap(smallPic.get(i));
			}
			images[currentImageIndex].setImageDrawable(getResources()
					.getDrawable(R.drawable.add_picture_solid));
			images[currentImageIndex].setVisibility(View.GONE);
			currentImageIndex--;
			if (currentImageIndex == 0) {
				images[currentImageIndex].setImageDrawable(getResources()
						.getDrawable(R.drawable.add_picture));
			} else {
				images[currentImageIndex].setImageDrawable(getResources()
						.getDrawable(R.drawable.add_picture_solid));
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		ContentResolver resolver = getContentResolver();
		int index = 0;
		if (resultCode == Activity.RESULT_OK) {

			for (int i = 0; i < 5; i++) {
				if (requestCode == Request_Code_Carema[i]) {
					if (i == currentImageIndex) {
						originalUris.add(Uri.fromFile(mPhotoFile));
						imagePaths.add(mPhotoFile.getPath());
					} else if (i < currentImageIndex) {
						originalUris.set(i, Uri.fromFile(mPhotoFile));
						imagePaths.set(i, mPhotoFile.getPath());
					}
					index = i;
					break;
				}
				if (requestCode == Request_Code_Album[i]) {
					if (i == currentImageIndex) {
						originalUris.add(data.getData());
					} else if (i < currentImageIndex) {
						originalUris.set(i, data.getData());
					}
					Cursor c = getContentResolver().query(data.getData(), null,
							null, null, null);
					String path = null;
					if (c.moveToFirst()) {
						path = c.getString(c
								.getColumnIndex(MediaStore.Images.Media.DATA));
						Log.d(TAG, "getContentResolver get filepath: " + path);
					}
					if (i == currentImageIndex) {
						imagePaths.add(path);
					} else if (i < currentImageIndex) {
						imagePaths.set(i, path);
					}
					c.close();
					index = i;
					break;
				}
			}
			Log.i(TAG, originalUris.get(index).getPath());
			Bitmap bm = null;
			try {
				bm = MediaStore.Images.Media.getBitmap(resolver,
						originalUris.get(index));

				Log.i(TAG, bm.toString());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (index == currentImageIndex) {
				smallPic.add(createFramedPhoto(100, 100, small(bm), 10));
				images[currentImageIndex].setImageBitmap(smallPic
						.get(currentImageIndex));
				if (currentImageIndex < 4) {
					currentImageIndex++;
					images[currentImageIndex].setVisibility(View.VISIBLE);
				}
			} else {
				smallPic.set(index, createFramedPhoto(100, 100, small(bm), 10));
				images[index].setImageBitmap(smallPic.get(index));
			}

		}
	}

	// Uri转化为Path
	private String uriToPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(this, uri, projection, null,
				null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		Log.i(TAG, cursor.getString(column_index));
		return cursor.getString(column_index);
	}

	// 压缩图片
	private static Bitmap small(Bitmap bitmap) {
		Matrix matrix = new Matrix();
		matrix.postScale(0.08f, 0.08f); // 长和宽放大缩小的比例
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return resizeBmp;
	}

	// 画圆角矩形
	private Bitmap createFramedPhoto(int x, int y, Bitmap image,
			float outerRadiusRat) {
		// 根据源文件新建一个darwable对象
		Drawable imageDrawable = new BitmapDrawable(image);

		// 新建一个新的输出图片
		Bitmap output = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		// 新建一个矩形
		RectF outerRect = new RectF(0, 0, x, y);

		// 产生一个红色的圆角矩形
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.RED);
		canvas.drawRoundRect(outerRect, outerRadiusRat, outerRadiusRat, paint);

		// 将源图片绘制到这个圆角矩形上
		// 详解见http://lipeng88213.iteye.com/blog/1189452
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		imageDrawable.setBounds(0, 0, x, y);
		canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
		imageDrawable.draw(canvas);
		canvas.restore();

		return output;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (IS_EMO_VISIABLE == true) {
				emoGridView.setVisibility(View.GONE);
				add_emo.setImageDrawable(getResources().getDrawable(
						R.drawable.add_smile));
				IS_EMO_VISIABLE = false;
				return true;
			} else if (!"".equals(post_title.getText().toString().trim())
					|| !"".equals(post_content.getText().toString().trim())) {
				sureToExitDialog();
			} else {
				return super.onKeyDown(keyCode, event);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void sureToExitDialog() {
		new AlertDialog.Builder(this)
				.setMessage(R.string.sure_to_exit_edit)
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
	}
	
	private void contentEmptyDialog(String message) {
		new AlertDialog.Builder(this)
				.setMessage(message)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.cancel();
							}
						}).show();
	}

}
