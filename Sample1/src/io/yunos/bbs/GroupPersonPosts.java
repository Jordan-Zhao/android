package io.yunos.bbs;

import java.util.List;

/**
 * Define object for Expandable List display
 * 
 * @author zhengyi.wzy
 * 
 */
public class GroupPersonPosts {
	private String groupName; // 大组名称
	private String number; // 帖子数量
	private List<ForumPost> groupChild; // 对应大组的小组成员对象list

	public GroupPersonPosts() {
		super();
	}

	public GroupPersonPosts(String groupName, List<ForumPost> groupChild) {
		super();
		this.groupName = groupName;
		this.groupChild = groupChild;
	}

	// Get... Set...

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public List<ForumPost> getGroupChild() {
		return groupChild;
	}

	public void setGroupChild(List<ForumPost> groupChild) {
		this.groupChild = groupChild;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * 小组添加帖子
	 * 
	 * @param post
	 */
	public void add(ForumPost post) {
		groupChild.add(post);
	}

	/**
	 * 小组删除帖子
	 * 
	 * @param post
	 */
	public void remove(ForumPost post) {
		groupChild.remove(post);
	}

	/**
	 * 小组大小
	 */
	public int getChildSize() {
		return groupChild.size();
	}

	/**
	 * 根据下标得到帖子
	 * 
	 * @param index
	 * @return
	 */
	public ForumPost getChild(int index) {
		return groupChild.get(index);
	}

}
