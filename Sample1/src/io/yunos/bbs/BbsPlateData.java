package io.yunos.bbs;

/**
 * Define bbs plate data object
 * 
 * @author zhengyi.wzy
 */
public class BbsPlateData implements Comparable<BbsPlateData> {
	/**
	 * Bbs title
	 */
	private String title;

	/**
	 * 版块列表图片
	 */
	private String icon;

	/**
	 * Bbs fid
	 */
	private String fid;

	/**
	 * Number of topic
	 */
	private String topics;

	/**
	 * Number of thread
	 */
	private String threads;

	/**
	 * The authority for ranking
	 */
	private int authority;

	/**
	 * Header image network url
	 */
	private String imageurl;

	/**
	 * Initial topics and threads
	 */
	public BbsPlateData() {
		this.topics = "0";
		this.threads = "0";
		this.authority = Integer.MAX_VALUE;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getFid() {
		return fid;
	}

	public void setFid(String fid) {
		this.fid = fid;
	}

	public String getTopics() {
		return topics;
	}

	public void setTopics(String topics) {
		this.topics = topics;
	}

	public String getThreads() {
		return threads;
	}

	public void setThreads(String threads) {
		this.threads = threads;
	}

	public int getAuthority() {
		return authority;
	}

	public void setAuthority(int authority) {
		this.authority = authority;
	}

	public String getImageurl() {
		return imageurl;
	}

	public void setImageurl(String imageurl) {
		this.imageurl = imageurl;
	}

	@Override
	public int compareTo(BbsPlateData another) {
		return this.getAuthority() - another.getAuthority();
	}

}
