package io.yunos.bbs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.httpclient.HttpException;
import org.apache.http.HttpStatus;

import android.util.Log;

import com.aliyun.oss.api.OSSClient;
import com.aliyun.oss.module.ListBucketResponse;
import com.aliyun.oss.module.Response;


public class OSSUtils {
	private static final String TAG = "OSS";
	private static final String bucketName = "yunosshequ";
	private static final String accessKeyId = "BMBRiCF5HHgyvlS2";
	private static final String accessKeySecret = "qb1SwCVfNGlmO9QFHjbEbR46EeS6tl";
	//private static final String endpoint = "http://oss.aliyuncs.com";
	//private static final String urlPrefix = "http://yunos-bbs.oss-cn-hangzhou.aliyuncs.com/";
	private static final String urlPrefix = "http://yunosshequ.oss.aliyuncs.com/";

/*
 * new SDK, cannot used on Android 
	public void putObject(String key, String filePath) {		
		OSSClient client = new OSSClient(accessKeyId, accessKeySecret);
		File file = new File(filePath);
		InputStream content = null;
		try {
			content = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentLength(file.length());
		meta.setContentType("image/jpeg");
		
		PutObjectResult result = client.putObject(bucketName, key, content, meta);
		
		Log.d(TAG, "PutObject returns: " + result.getETag());
	}
*/

	/**
	 * Writes an object to OSS from InputStream.
	 * 
	 * @param key
	 *            The name of the object to be added.
	 * @param filePath
	 *            The local path to the image file which will be uploaded
	 * @param ossUrl
	 *            The url of the object which can be used to access the uploaded file
	 * @return true if the object uploaded successfully
	 */
	public boolean pubObjectFromInputStream(String key, String filePath, StringBuffer ossUrl) {
		File file = new File(filePath);
		if (!file.canRead())
			return false;
		
		OSSClient client = new OSSClient(accessKeyId, accessKeySecret);
		ListBucketResponse listBucketResponse = null;

		try {
			listBucketResponse = client.listBucket(bucketName, null,
					null, null, null);
			if (listBucketResponse.getStatusCode() != HttpStatus.SC_OK) {
				Log.d(TAG, "ListBucketResponse StatusCode: " + listBucketResponse.getStatusCode());
				Log.d(TAG, "ListBucketResponse error Message: " + listBucketResponse.getErrorMessage());
				return false;
			}
		} catch (HttpException e1) {
			Log.e(TAG, "pubObjectFromInputStream failed " + e1);
		} catch (IllegalArgumentException e1) {
			Log.e(TAG, "pubObjectFromInputStream failed " + e1);
		} catch (IOException e1) {
			Log.e(TAG, "pubObjectFromInputStream failed " + e1);
		}

		Response response;
		try {
			response = client.putObjectFromFile(bucketName, key, filePath);
			if (response.getStatusCode() != HttpStatus.SC_OK) {
				Log.d(TAG, "PutObject StatusCode: " + response.getStatusCode());
				Log.d(TAG, "PutObject error Message: " + response.getErrorMessage());
				return false;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ossUrl.append(urlPrefix).append(key);
		return true;
	}
	
}