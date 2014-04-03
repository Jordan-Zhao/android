package io.yunos.bbs;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

/**
 * Define object for personal center
 * 
 * @author zhengyi.wzy
 * 
 */
public class PersonCenterData {
	private String username;
	private String group;
	private String imageurl;
	private Bitmap headPicBitmap;


	private String gold;
	private String coin;
	private String weiwang;
	private String threads;
	private String favorites;
	private List<ThreadsData> threadsList;
	private List<FavoritesData> favoritesList;
	private List<GroupPersonPosts> groupPosts;

	/**
	 * Initialize groupPosts
	 */
	public PersonCenterData() {
		super();
		groupPosts = new ArrayList<GroupPersonPosts>();
	}

	public void setGroupPersonPosts(GroupPersonPosts gpp) {
		groupPosts.add(gpp);
	}
	
	public void clearGroupPersonPosts() {
		groupPosts.clear();
	}

	public List<GroupPersonPosts> getGroupPosts() {
		return groupPosts;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getImageurl() {
		return imageurl;
	}

	public void setImageurl(String imageurl) {
		this.imageurl = imageurl;
	}
	
	public Bitmap getHeadPicBitmap() {
		return headPicBitmap;
	}

	public void setHeadPicBitmap(Bitmap headPicBitmap) {
		this.headPicBitmap = headPicBitmap;
	}

	public String getGold() {
		return gold;
	}

	public void setGold(String gold) {
		this.gold = gold;
	}

	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

	public String getWeiwang() {
		return weiwang;
	}

	public void setWeiwang(String weiwang) {
		this.weiwang = weiwang;
	}

	public String getThreads() {
		return threads;
	}

	public void setThreads(String threads) {
		this.threads = threads;
	}

	public String getFavorites() {
		return favorites;
	}

	public void setFavorites(String favorites) {
		this.favorites = favorites;
	}

	public List<ThreadsData> getThreadsList() {
		return threadsList;
	}

	public void setThreadsList(List<ThreadsData> threadsList) {
		this.threadsList = threadsList;
	}

	public List<FavoritesData> getFavoritesList() {
		return favoritesList;
	}

	public void setFavoritesList(List<FavoritesData> favoritesList) {
		this.favoritesList = favoritesList;
	}

}
