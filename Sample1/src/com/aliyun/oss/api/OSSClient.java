package com.aliyun.oss.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.aliyun.oss.module.BucketAclResponse;
import com.aliyun.oss.module.InputStreamResponse;
import com.aliyun.oss.module.ListAllMyBucketsResponse;
import com.aliyun.oss.module.ListBucketResponse;
import com.aliyun.oss.module.ListGroupIndexResponse;
import com.aliyun.oss.module.Response;
import com.aliyun.oss.module.SuffixToMime;

public class OSSClient {
	private final int DEFAULT_CONNECT_TIMEOUT = 10000;
	private final int DEFAULT_SO_TIMEOUT = 10000;
	private int port;
	private String server;
	private String accessId;
	private String secretAccessKey;
	private int connectTimeout;
	private int soTimeout;

	/**
	 * Create a new interface to interact with OSS with the given credential and
	 * connection parameters
	 * 
	 * @param server
	 *            Which host to connect to. Usually, this will be
	 *            storage.aliyun.com
	 * @param port
	 *            Which port to use.
	 * @param accessId
	 *            Your user key into OSS
	 * @param secretAccessKey
	 *            The secret string used to generate signatures for
	 *            authentication.
	 */
	public OSSClient(String server, int port, String accessId,
			String secretAccessKey) {
		this.server = server;
		this.port = port;
		this.accessId = accessId;
		this.secretAccessKey = secretAccessKey;
		this.connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		this.soTimeout = DEFAULT_SO_TIMEOUT;
	}

	public OSSClient(String accessId, String secretAccessKey) {
		this(Utils.DEFAULT_HOST, Utils.DEFAULT_PORT, accessId, secretAccessKey);
	}

	/**
	 * Create a new interface to interact with OSS with anonymous user
	 * parameters
	 * 
	 * @param server
	 *            Which host to connect to. Usually, this will be
	 *            storage.aliyun.com
	 * @param port
	 *            Which port to use.
	 */
	public OSSClient(String server, int port) {
		this(server, port, "", "");
	}

	public OSSClient() {
		this(Utils.DEFAULT_HOST, Utils.DEFAULT_PORT, "", "");
	}

	private String createSignForNormalAuth(String method,
			Map<String, String> headers, String resource) {
		String authValue = "OSS "
				+ this.accessId
				+ ":"
				+ Authorization.getAssign(this.secretAccessKey, method,
						headers, resource);
		return authValue;
	}

	/**
	 * Make a new HttpClient.
	 * 
	 * @param verb
	 *            The HTTP method to use (GET, PUT, DELETE, HEAD).
	 * @param url
	 *            Request URL.
	 * @param headers
	 *            A Map of String to List of Strings representing the HTTP.
	 *            headers to pass
	 * @param entity
	 *            The object content that is to be written (can be null).
	 */
	private HttpMethod execute(String verb, String url,
			Map<String, String> headers, RequestEntity entity)
			throws HttpException, IOException {
		HttpMethod method;
		HttpClient client = new HttpClient();
		client.getParams().setHttpElementCharset(Utils.CHARSET);
		client.getParams().setContentCharset(Utils.CHARSET);
		client.getHttpConnectionManager().getParams()
				.setConnectionTimeout(connectTimeout);
		client.getHttpConnectionManager().getParams().setSoTimeout(soTimeout);
		client.getHostConfiguration().setHost(server, port);
		if ("GET".equals(verb)) {
			method = new GetMethod(url);
		} else if ("PUT".equals(verb)) {
			if (entity == null) {
				method = new PutMethod(url);
			} else {
				PutMethod putMethod = new PutMethod(url);
				putMethod.setRequestEntity(entity);
				Utils.setHeaders(putMethod, headers);
				client.executeMethod(putMethod);
				return putMethod;
			}
		} else if ("DELETE".equals(verb)) {
			method = new DeleteMethod(url);
		} else if ("HEAD".equals(verb)) {
			method = new HeadMethod(url);
		} else if ("POST".equals(verb)) {
			PostMethod postMethod = new PostMethod(url);
			postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, Utils.CHARSET);
			postMethod.setRequestEntity(entity);
			Utils.setHeaders(postMethod, headers);
			client.executeMethod(postMethod);
			return postMethod;
		} else {
			method = new GetMethod(url);
		}
		Utils.setHeaders(method, headers);
		client.executeMethod(method);
		return method;
	}

	private HttpMethod request(String verb, String url, String resource,
			Map<String, String> headers) throws HttpException, IOException {
		return request(verb, url, resource, headers, null);
	}

	private HttpMethod request(String verb, String url, String resource,
			Map<String, String> headers, RequestEntity entity)
			throws HttpException, IOException {
		String date = Utils.getGMTTime();
		headers.put(Utils.DATE, date);
		headers.put(Utils.HOST, server + ":" + port);
		if (!"".equals(secretAccessKey) && !"".equals(accessId)) {
			headers.put(Utils.AUTHORIZATION,
					createSignForNormalAuth(verb, headers, resource));
		} else if (!"".equals(accessId)) {
			headers.put(Utils.AUTHORIZATION, this.accessId);
		}
		return execute(verb, url, headers, entity);
	}

	private HttpMethod bucketOperation(String verb, String bucket,
			Map<String, String> headers, Map<String, String> params)
			throws HttpException, IOException, IllegalArgumentException {
		if (!Utils.validateBucketName(bucket)) {
			throw new IllegalArgumentException("Unsupported bucket name:"
					+ bucket);
		}
		String url = "/" + bucket + "/" + Utils.paramsToString(params);
		String resource = "/" + bucket + "/";
		if (params != null && params.containsKey("acl")){
			resource += "?acl";
		}
		return request(verb, url, resource, headers);
	}

	private HttpMethod objectOperation(String verb, String bucket,
			String object, Map<String, String> headers) throws HttpException,
			IOException, IllegalArgumentException {
		return objectOperation(verb, bucket, object, headers, null);
	}

	private HttpMethod objectOperation(String verb, String bucket,
			String object, Map<String, String> headers,
			RequestEntity entity) throws HttpException, IOException,
			IllegalArgumentException {
		if (!Utils.validateBucketName(bucket)) {
			throw new IllegalArgumentException("Unsupported bucket name:"
					+ bucket);
		}
		if (!Utils.validateObjectName(object)) {
			throw new IllegalArgumentException("Unsupported object name:"
					+ object);
		}
		String resource = "/" + bucket + "/" + object;
		String url = "/" + bucket + "/" + Utils.objectUri(object);
		return request(verb, url, resource, headers, entity);
	}

	private HttpMethod groupOperation(String verb, String bucket, String group,
			Map<String, String> headers, String groupRequestXml)
			throws HttpException, IOException, IllegalArgumentException {
		if (!Utils.validateBucketName(bucket)) {
			throw new IllegalArgumentException("Unsupported bucket name:"
					+ bucket);
		}
		if (!Utils.validateObjectName(group)) {
			throw new IllegalArgumentException("Unsupported group name:"
					+ group);
		}
		String url = "/" + bucket + "/" + Utils.objectUri(group) + "?group";
		String resource = "/" + bucket + "/" + group + "?group";
		headers.put(Utils.CONTENT_TYPE, "text/html; charset=utf-8");
		StringRequestEntity entity = new StringRequestEntity(groupRequestXml,
				"text/html", "utf-8");
		HttpMethod method = request(verb, url, resource, headers, entity);
		return method;
	}
	
	/**
	 * Access OSS based on the input method, url, body and headers
	 * 
	 * @param method
	 *            One of PUT, GET, DELETE, HEAD.
	 * @param url
	 *            HTTP address of bucket or object, e.g:
	 *            http://HOST/bucket/object
	 * @param headers
	 *            A Map of String to List of Strings representing the HTTP
	 *            headers to pass.
	 * @param entity
	 *            The body of http request.
	 * @return Constructed according to the type of request, e.g: Response
	 *         response = new Response(httpMethod); ListAllMyBucketResponse
	 *         response = new ListAllMyBucketResponse(httpMethod);
	 *         ListBucketResponse response = new ListBucketResponse(htthMethod);
	 * @throws HttpException
	 * @throws IOException
	 */
	public HttpMethod accessByUrl(String method, String url,
			Map<String, String> headers, RequestEntity entity)
			throws HttpException, IOException {
		if (headers == null) {
			headers = new HashMap<String, String>();
		}
		return execute(method, url, headers, entity);
	}

	/**
	 * Access OSS based on the input method, url, body and headers
	 * 
	 * @param method
	 *            One of PUT, GET, DELETE, HEAD.
	 * @param url
	 *            HTTP address of bucket or object, eg:
	 *            http://HOST/bucket/object
	 * @return Constructed according to the type of request, eg:
	 *         Response response = new Response(httpMethod);
	 *         ListAllMyBucketResponse response = new ListAllMyBucketResponse(httpMethod);
	 *         ListBucketResponse response = new ListBucketResponse(htthMethod);
	 * @throws HttpException
	 * @throws IOException
	 */
	public HttpMethod accessByUrl(String method, String url)
			throws HttpException, IOException {
		return accessByUrl(method, url, null, null);
	}
	
	/**
	 * List all the buckets created by this account.
	 * @throws HttpException
	 * @throws IOException
	 */
	public ListAllMyBucketsResponse listAllMyBuckets() throws HttpException,
			IOException {
		Map<String, String> headers = new HashMap<String, String>();
		HttpMethod method = request("GET", "/", "/", headers);
		return new ListAllMyBucketsResponse(method);
	}

	/**
	 * Lists the contents of a bucket.
	 * 
	 * @param bucket
	 *            The name of the bucket to list.
	 * @param prefix
	 *            All returned keys will start with this string (can be null).
	 * @param marker
	 *            All returned keys will be lexicographically greater than this
	 *            string (can be null).
	 * @param maxKeys
	 *            The maximum number of keys to return (1-100 and can be null).
	 * @param delimiter
	 *            Keys that contain a string between the prefix and the first
	 *            occurrence of the delimiter will be rolled up into a single
	 *            element(can be null).
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public ListBucketResponse listBucket(String bucket, String prefix,
			String marker, String maxKeys, String delimiter)
			throws HttpException, IOException, IllegalArgumentException {
		Map<String, String> headers = new HashMap<String, String>();
		Map<String, String> params = new HashMap<String, String>();
		params.put("prefix", prefix);
		params.put("marker", marker);
		params.put("delimiter", delimiter);
		params.put("max-keys", maxKeys);
		HttpMethod method = bucketOperation("GET", bucket, headers, params);
		return new ListBucketResponse(method);
	}

	/**
	 * Creates a new bucket.
	 * 
	 * @param bucket
	 *            The name of the bucket to create.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response createBucket(String bucket) throws HttpException,
			IOException, IllegalArgumentException {
		Map<String, String> headers = new HashMap<String, String>();
		HttpMethod method = bucketOperation("PUT", bucket, headers, null);
		return new Response(method);
	}

	/**
	 * Write a new ACL for a given bucket
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param acl
	 *            The ACL representation of the ACL as a
	 *            String(private,public-read,public-read-write).
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response putBucketACL(String bucket, String acl)
			throws HttpException, IOException, IllegalArgumentException {
		Map<String, String> headers = new HashMap<String, String>();
		if (acl == null || "".equals(acl)) {
			acl = "private";
		}
		headers.put(Utils.ACL, acl);
		HttpMethod method = bucketOperation("PUT", bucket, headers, null);
		return new Response(method);
	}
	
	/**
	 * Get the ACL for a given bucket
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @return Utils.PRIVATE, Utils.PUBLIC_READ, Utils.PUBLIC_READ_WRITE,
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public BucketAclResponse getBucketACL(String bucket) throws HttpException,
			IOException, IllegalArgumentException {
		Map<String, String> headers = new HashMap<String, String>();
		Map<String, String> params = new HashMap<String, String>();
		params.put("acl", null);
		HttpMethod method = bucketOperation("GET", bucket, headers, params);
		return new BucketAclResponse(method);
	}
	
	/**
	 * Deletes a bucket.
	 * 
	 * @param bucket
	 *            The name of the bucket to delete.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response deleteBucket(String bucket) throws HttpException,
			IOException, IllegalArgumentException {
		Map<String, String> headers = new HashMap<String, String>();
		HttpMethod method = bucketOperation("DELETE", bucket, headers, null);
		return new Response(method);
	}

	/**
	 * Creates a new group.
	 * 
	 * @param bucket
	 *            The name of the bucket.
	 * @param group
	 *            The name of the group to create
	 * @param groupRequestXml
	 *            The bucket list of group
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response createGroup(String bucket, String group,
			String groupRequestXml) throws HttpException,
			IllegalArgumentException, IOException {
		Map<String, String> headers = new HashMap<String, String>();
		HttpMethod method = groupOperation("POST", bucket, group, headers,
				groupRequestXml);
		return new Response(method);
	}

	/**
	 * Reads an group from OSS.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param group
	 *            The name of the group to use.
	 * @param headers
	 *            A Map of String to List of Strings representing the HTTP
	 *            headers to pass.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public InputStreamResponse getGroupInputStream(String bucket, String group,
			Map<String, String> headers) throws HttpException,
			IllegalArgumentException, IOException {
		return getObjectInputStream(bucket, group, headers);
	}

	/**
	 * Reads an group from OSS.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param group
	 *            The name of the group to use.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public InputStreamResponse getGroupInputStream(String bucket, String group)
			throws HttpException, IllegalArgumentException, IOException {
		Map<String, String> headers = new HashMap<String, String>();
		return getGroupInputStream(bucket, group, headers);
	}

	/**
	 * Deletes an group from OSS.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param group
	 *            The name of the group to use.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response deleteGroup(String bucket, String group)
			throws HttpException, IllegalArgumentException, IOException {
		return deleteObject(bucket, group);
	}

	/**
	 * Head an group from OSS.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param group
	 *            The name of the group to use.
	 * @param headers
	 *            A Map of String to List of Strings representing the HTTP
	 *            headers to pass.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response headGroup(String bucket, String group,
			Map<String, String> headers) throws HttpException,
			IllegalArgumentException, IOException {
		return headObject(bucket, group, headers);
	}

	/**
	 * Head an group from OSS.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param group
	 *            The name of the group to use.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response headGroup(String bucket, String group)
			throws HttpException, IllegalArgumentException, IOException {
		Map<String, String> headers = new HashMap<String, String>();
		return headGroup(bucket, group, headers);
	}

	/**
	 * Lists the contents of a group.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param group
	 *            The name of the group to use.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public ListGroupIndexResponse listGroupIndex(String bucket, String group)
			throws HttpException, IllegalArgumentException, IOException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("x-oss-file-group", "");
		HttpMethod method = objectOperation("GET", bucket, group, headers);
		return new ListGroupIndexResponse(method);
	}

	/**
	 * Deletes an object from OSS.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param object
	 *            The name of the object to use.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response deleteObject(String bucket, String object)
			throws HttpException, IOException, IllegalArgumentException {
		Map<String, String> headers = new HashMap<String, String>();
		HttpMethod method = objectOperation("DELETE", bucket, object, headers);
		return new Response(method);
	}

	/**
	 * Head an object from OSS.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param object
	 *            The name of the object to use.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response headObject(String bucket, String object)
			throws HttpException, IOException, IllegalArgumentException {
		Map<String, String> headers = new HashMap<String, String>();
		return headObject(bucket, object, headers);
	}

	/**
	 * Head an object from OSS.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param object
	 *            The name of the object to use.
	 * @param headers
	 *            A Map of String to List of Strings representing the HTTP
	 *            headers to pass.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response headObject(String bucket, String object,
			Map<String, String> headers) throws HttpException, IOException,
			IllegalArgumentException {
		HttpMethod method = objectOperation("HEAD", bucket, object, headers);
		return new Response(method);
	}

	/**
	 * Reads an object from OSS.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param object
	 *            The name of the object to use.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public InputStreamResponse getObjectInputStream(String bucket, String object)
			throws HttpException, IOException, IllegalArgumentException {
		Map<String, String> headers = new HashMap<String, String>();
		return getObjectInputStream(bucket, object, headers);
	}

	/**
	 * Reads an object from OSS.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param object
	 *            The name of the object to use.
	 * @param headers
	 *            A Map of String to List of Strings representing the HTTP
	 *            headers to pass.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public InputStreamResponse getObjectInputStream(String bucket,
			String object, Map<String, String> headers) throws HttpException,
			IOException, IllegalArgumentException {
		HttpMethod method = objectOperation("GET", bucket, object, headers);
		return new InputStreamResponse(method);
	}

	/**
	 * Download an object from OSS to disk.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param object
	 *            The name of the object to use.
	 * @param fileName
	 *            The path of the file to download.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response getObjectToFile(String bucket, String object,
			String fileName) throws HttpException, IOException,
			IllegalArgumentException {
		Map<String, String> headers = new HashMap<String, String>();
		return getObjectToFile(bucket, object, fileName, headers);
	}

	/**
	 * Download an object from OSS to disk.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param object
	 *            The name of the object to use.
	 * @param fileName
	 *            The path of the file to download.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response getObjectToFile(String bucket, String object,
			String fileName, Map<String, String> headers) throws HttpException,
			IOException, IllegalArgumentException {
		InputStreamResponse response = getObjectInputStream(bucket, object,
				headers);
		if (response.getStatusCode() == 200) {
			InputStream is = response.getInputStream();
			Utils.saveInputStreamToFile(is, fileName);
		}
		return response;

	}

	/**
	 * Writes an object to OSS from String.
	 * 
	 * @param bucket
	 *            The name of the bucket to which the object will be added.
	 * @param object
	 *            The name of the object to use.
	 * @param inputContent
	 *            Contents of the object
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response putObjectFromString(String bucket, String object,
			String inputContent) throws HttpException, IOException,
			IllegalArgumentException {
		Map<String, String> headers = new HashMap<String, String>();
		return putObjectFromString(bucket, object, inputContent, headers);
	}

	/**
	 * Writes an object to OSS from String.
	 * 
	 * @param bucket
	 *            The name of the bucket to which the object will be added.
	 * @param object
	 *            The name of the object to use.
	 * @param inputContent
	 *            Contents of the object
	 * @param headers
	 *            A Map of String to List of Strings representing the HTTP
	 *            headers to pass.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response putObjectFromString(String bucket, String object,
			String inputContent, Map<String, String> headers)
			throws HttpException, IOException, IllegalArgumentException {
		int fileSize = inputContent.getBytes().length;
		InputStream in = null;
		Response response;
		try {
			in = new ByteArrayInputStream(inputContent.getBytes());
			response = putObjectFromInputStream(bucket, object, in, fileSize,
					headers);
			return response;
		} finally {
			if (in != null) try { in.close(); } catch (Exception e) {}
		}
	}

	/**
	 * Writes an object to OSS from file.
	 * 
	 * @param bucket
	 *            The name of the bucket to which the object will be added.
	 * @param object
	 *            The name of the object to use.
	 * @param fileName
	 *            The path of the file to upload.
	 * @throws FileNotFoundException
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response putObjectFromFile(String bucket, String object,
			String fileName) throws FileNotFoundException, HttpException,
			IOException, IllegalArgumentException {
		Map<String, String> headers = new HashMap<String, String>();
		return putObjectFromFile(bucket, object, fileName, headers);
	}

	/**
	 * Writes an object to OSS from file.
	 * 
	 * @param bucket
	 *            The name of the bucket to which the object will be added.
	 * @param object
	 *            The name of the object to use.
	 * @param fileName
	 *            The path of the file to upload.
	 * @param headers
	 *            A Map of String to List of Strings representing the HTTP
	 *            headers to pass.
	 * @throws FileNotFoundException
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response putObjectFromFile(String bucket, String object,
			String fileName, Map<String, String> headers)
			throws FileNotFoundException, HttpException, IOException,
			IllegalArgumentException {
		File file = new File(fileName);
		long fileSize = file.length();
		FileInputStream in = null;
		Response response;
		try {
			in = new FileInputStream(fileName);
			response = putObjectFromInputStream(bucket, object, in, fileSize,
					headers);
			return response;
		} finally {
			if (in != null) try { in.close(); } catch (Exception e) {}
		}
	}

	/**
	 * Writes an object to OSS from InputStream.
	 * 
	 * @param bucket
	 *            The name of the bucket to which the object will be added.
	 * @param object
	 *            The name of the object to use.
	 * @param in
	 *            The InputStream will be upload
	 * @param fileSize
	 *            File size
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response putObjectFromInputStream(String bucket, String object,
			InputStream in, long fileSize) throws IOException, HttpException,
			IllegalArgumentException {
		Map<String, String> headers = new HashMap<String, String>();
		return putObjectFromInputStream(bucket, object, in, fileSize, headers);
	}

	/**
	 * Writes an object to OSS from InputStream.
	 * 
	 * @param bucket
	 *            The name of the bucket to which the object will be added.
	 * @param object
	 *            The name of the object to use.
	 * @param in
	 *            The InputStream will be upload
	 * @param fileSize
	 *            File size
	 * @param headers
	 *            A Map of String to List of Strings representing the HTTP
	 *            headers to pass.
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public Response putObjectFromInputStream(String bucket, String object,
			InputStream in, long fileSize, Map<String, String> headers)
			throws IOException, HttpException, IllegalArgumentException {
		headers.put(Utils.CONTENT_LENGTH, String.valueOf(fileSize));
		if (!headers.containsKey(Utils.CONTENT_TYPE)){
			headers.put(Utils.CONTENT_TYPE, SuffixToMime.getContentTypeByFileName(object));
		}
		InputStreamRequestEntity entity = new InputStreamRequestEntity(in,
				fileSize);
		HttpMethod method = objectOperation("PUT", bucket, object, headers,
				entity);
		return new Response(method);
	}

	public Response copyObject(String sourceBucket, String sourceObject,
			String targetBucket, String targetObject,
			Map<String, String> headers) throws IOException, HttpException,
			IllegalArgumentException {
		String sourceUri = "/" + sourceBucket + "/" + Utils.objectUri(sourceObject);
		headers.put(Utils.COPY_SOURCE, sourceUri);
		HttpMethod method = objectOperation("PUT", targetBucket, targetObject, headers); 
		return new Response(method);
	}

	public Response copyObject(String sourceBucket, String sourceObject,
			String targetBucket, String targetObject) throws IOException,
			HttpException, IllegalArgumentException {
		Map<String, String> headers = new HashMap<String, String>();
		return copyObject(sourceBucket, sourceObject, targetBucket,
				targetObject, headers);
	}
	
	/**
	 * Check if the specified object exists
	 * 
	 * @param bucket
	 *            The name of the bucket to check
	 * @return true if the bucket exists
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public boolean isObjectExist(String bucket, String object)
			throws HttpException, IOException, IllegalArgumentException {
		Response response = headObject(bucket, object);
		if (response.getStatusCode() / 100 == 2)
			return true;
		return false;
	}

	/**
	 * Check if the specified bucket exists
	 * 
	 * @param bucket
	 *            The name of the bucket to check
	 * @return true if the bucket exists
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public boolean isBucketExist(String bucket) throws HttpException,
			IOException, IllegalArgumentException {
		ListBucketResponse listBucketResponse = listBucket(bucket, null, null,
				null, null);
		if (listBucketResponse.getStatusCode() / 100 == 2) {
			return true;
		}
		return false;
	}

	/**
	 * Return object size
	 * 
	 * @param bucket
	 *            The name of the bucket to check
	 * @param object
	 *            The name of the object to use.
	 * @return Object size
	 * @throws IllegalArgumentException
	 * @throws HttpException
	 * @throws IOException
	 */
	public long getObjectFileSize(String bucket, String object)
			throws HttpException, IOException, IllegalArgumentException {
		Response response = headObject(bucket, object);
		if (response.getStatusCode() / 100 == 2) {
			Map<String, String> headers = response.getHeaders();
			if (headers != null && headers.containsKey(Utils.CONTENT_LENGTH))
				return Long.valueOf(headers.get(Utils.CONTENT_LENGTH));
		}
		return -1;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setReadTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public String getAccessId() {
		return accessId;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	public void setSecretAccessKey(String secretAccessKey) {
		this.secretAccessKey = secretAccessKey;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

}
