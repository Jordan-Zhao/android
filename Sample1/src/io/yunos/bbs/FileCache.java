package io.yunos.bbs;

import java.io.File;

import android.content.Context;

public class FileCache {
	private static final String DIR_NAME = "wanjigonglue";
	private File cacheDir;

	public FileCache(Context context) {
		// Find the directory to save cached images
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					DIR_NAME);
		} else {
			cacheDir = context.getCacheDir();
		}

		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
	}

	public File getFile(String url) {
		// Identify images by url's hash code
		String filename = String.valueOf(url.hashCode());

		File f = new File(cacheDir, filename);

		return f;
	}

	public void clear() {
		File[] files = cacheDir.listFiles();
		if (files == null) {
			return;
		} else {
			for (File f : files) {
				f.delete();
			}
		}
	}
}
