package io.yunos.bbs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.widget.ImageView;

public class ImageLoader {
	/**
	 * Network time out
	 */
	private static final int TIME_OUT = 30000;
	/**
	 * Default picture resource
	 */
	private static final int DETAILS_DEFAULT_BG = R.drawable.plate_list_head_bg;
	private static final int PLATES_DEFAULT_BG = R.drawable.ic_launcher;

	/**
	 * Thread pool number
	 */
	private static final int THREAD_NUM = 5;

	private static final int PLATES_PIC_WIDTH = 44;

	private static final int DETAILS_PIC_WIDTH = 520;

	/**
	 * Memory image cache
	 */
	MemoryCache memoryCache = new MemoryCache();

	/**
	 * File image cache
	 */
	FileCache fileCache;

	/**
	 * Judge image view if it is reuse
	 */
	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());

	/**
	 * Thread pool
	 */
	ExecutorService executorService;

	/**
	 * Handler to display images in UI thread
	 */
	Handler handler = new Handler();

	public ImageLoader(Context context) {
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(THREAD_NUM);
	}

	public void disPlayImage(String url, ImageView imageView) {
		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {
			// Display image from Memory cache
			imageView.setImageBitmap(bitmap);
		} else {
			// Display image from File cache or Network
			queuePhoto(url, imageView);
		}
	}

	private void queuePhoto(String url, ImageView imageView) {
		PhotoToLoad photoToLoad = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(photoToLoad));
	}

	private Bitmap getBitmap(PhotoToLoad photo) {
		File f = fileCache.getFile(photo.url);

		// From File cache
		Bitmap bmp = decodeFile(f, photo.url, photo.imageView);
		if (bmp != null) {
			return bmp;
		}

		// From Network
		try {
			Bitmap bitmap = null;
			URL imageUrl = new URL(photo.url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl
					.openConnection();
			conn.setConnectTimeout(TIME_OUT);
			conn.setReadTimeout(TIME_OUT);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			copyStream(is, os);
			os.close();
			conn.disconnect();
			bitmap = decodeFile(f, photo.url, photo.imageView);
			return bitmap;
		} catch (Throwable ex) {
			if (ex instanceof OutOfMemoryError) {
				clearCache();
			}
			return null;
		}

	}

	private void copyStream(InputStream is, OutputStream os) {
		int buffer_size = 1024;

		try {
			byte[] bytes = new byte[buffer_size];
			while (true) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1) {
					break;
				}
				os.write(bytes, 0, count);
			}

		} catch (Exception e) {

		}
	}

	private Bitmap decodeFile(File f, String url, ImageView imageView) {
		try {
			// TODO:Compress image size
			FileInputStream inputStreamOne = new FileInputStream(f);

			// Decode image size
			Bitmap oriBitmap = BitmapFactory.decodeStream(inputStreamOne);
			int oriWidth = oriBitmap.getWidth();
			int oriHeight = oriBitmap.getHeight();

			// The width and height of image view

			int reqWidth = getImageMaxWidth(imageView) - 4;
			int reqHeight = getImageMaxHeight(imageView) - 4;

			// zoom Bitmap to the right size
			float scaleWidth = ((float) reqWidth) / oriWidth;
			float scaleHeight = ((float) reqHeight / oriHeight);
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			Bitmap displayBitmap = Bitmap.createBitmap(oriBitmap, 0, 0,
					oriWidth, oriHeight, matrix, true);

			return displayBitmap;

		} catch (FileNotFoundException e) {
			return null;
		}
	}

	@TargetApi(16)
	private int getImageMaxWidth(ImageView imageView) {
		return imageView.getMaxWidth();
	}

	@TargetApi(16)
	private int getImageMaxHeight(ImageView imageView) {
		return imageView.getMaxHeight();
	}

	private void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

	/**
	 * Task for the queue
	 * 
	 * @author zhengyi.wzy
	 */
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String url, ImageView imageView) {
			this.url = url;
			this.imageView = imageView;
		}
	}

	/**
	 * Asynchronous to load picture
	 * 
	 * @author zhengyi.wzy
	 */
	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		public PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		private boolean imageViewReused(PhotoToLoad photoToLoad) {
			String tag = imageViews.get(photoToLoad.imageView);
			if (tag == null || !tag.equals(photoToLoad.url)) {
				return true;
			}

			return false;
		}

		@Override
		public void run() {
			// Abort current thread if Image View reused
			if (imageViewReused(photoToLoad)) {
				return;
			}

			Bitmap bitmap = getBitmap(photoToLoad);

			// Update Memory
			memoryCache.put(photoToLoad.url, bitmap);

			if (imageViewReused(photoToLoad)) {
				return;
			}

			// Don't change UI in children thread
			BitmapDisplayer bd = new BitmapDisplayer(bitmap, photoToLoad);
			handler.post(bd);
		}

		class BitmapDisplayer implements Runnable {
			Bitmap bitmap;
			PhotoToLoad photoToLoad;

			public BitmapDisplayer(Bitmap bitmap, PhotoToLoad photoToLoad) {
				this.bitmap = bitmap;
				this.photoToLoad = photoToLoad;
			}

			@Override
			public void run() {
				if (imageViewReused(photoToLoad)) {
					return;
				}

				if (bitmap != null) {
					photoToLoad.imageView.setImageBitmap(bitmap);
				} else {
					int resID;
					switch (getImageMaxWidth(photoToLoad.imageView)) {
					case DETAILS_PIC_WIDTH:
						resID = DETAILS_DEFAULT_BG;
						break;
					case PLATES_PIC_WIDTH:
					default:
						resID = PLATES_DEFAULT_BG;
						break;
					}
					photoToLoad.imageView.setImageResource(resID);
				}
			}

		}
	}
}
