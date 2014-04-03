package io.yunos.bbs;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ExAdapter extends BaseExpandableListAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<GroupPersonPosts> group;

	public ExAdapter(Context context, List<GroupPersonPosts> group) {
		super();
		this.context = context;
		this.group = group;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getGroupCount() {
		return group.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return group.get(groupPosition).getChildSize();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return group.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return group.get(groupPosition).getChild(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View vi = convertView;
		if (vi == null) {
			vi = inflater.inflate(R.layout.expandable_list_group_layout, null);
		}

		TextView title = (TextView) vi
				.findViewById(R.id.expandable_list_group_title);

		TextView number = (TextView) vi
				.findViewById(R.id.expandable_list_group_num);

		title.setText(group.get(groupPosition).getGroupName());
		number.setText(group.get(groupPosition).getNumber());

		// Change background color
		int count = Integer.parseInt(group.get(groupPosition).getNumber());
		if (isExpanded && count > 0) {
			vi.setBackgroundColor(context.getResources().getColor(
					R.color.plates_head_bg));
		} else {
			vi.setBackgroundColor(context.getResources()
					.getColor(R.color.white));
		}

		return vi;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View cvi = convertView;

		if (cvi == null) {
			cvi = inflater.inflate(R.layout.expandable_list_child_layout, null);
		}

		TextView childTitle = (TextView) cvi
				.findViewById(R.id.expandable_list_child_title);
		TextView childNum = (TextView) cvi
				.findViewById(R.id.expandable_list_child_num);

		childTitle.setText(group.get(groupPosition).getChild(childPosition)
				.getTitle());
		childNum.setText(group.get(groupPosition).getChild(childPosition)
				.getReplies());

		return cvi;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public void refresh(List<GroupPersonPosts> group) {
		this.group = group;
		notifyDataSetChanged();
	}
}
