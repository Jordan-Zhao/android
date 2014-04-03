package io.yunos.bbs;

/**
 * Define object for favorite topics
 * 
 * @author zhengyi.wzy
 * 
 */
public class FavoritesData {
	private String title;
	private String replies;
	private String tid;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getReplies() {
		return replies;
	}

	public void setReplies(String replies) {
		this.replies = replies;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}
}
