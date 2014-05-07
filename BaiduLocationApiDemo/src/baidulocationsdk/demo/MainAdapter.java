package baidulocationsdk.demo;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 主页功能列表Adapter
 *
 */
public class MainAdapter extends BaseAdapter {
	public static final String PARAM_MAIN_TITLE = "main_title";
	public static final String PARAM_SECOND_TITLE = "second_title";
	public static final String PARAM_SETTING_TITLE = "setting_title";
	
	private Context mContext;
	private List<Map<String, Object>> mList;
	private LayoutInflater mInflater;
	
	public MainAdapter(Context context, List<Map<String, Object>> list) {
		mList = list;
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		if (mList != null) {
			return mList.size();
		}
		
		return 0;
	}

	@Override
	public Object getItem(int arg0) {
		if(mList != null) {
			return mList.get(arg0);
		}
		
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item, null);
			ViewHolder holder = new ViewHolder();
			holder.titleTxt = (TextView) convertView.findViewById(R.id.main_title);
			holder.secondTxt = (TextView) convertView.findViewById(R.id.second_title);
			holder.settingTxt = (TextView) convertView.findViewById(R.id.setting_title);
			
			convertView.setTag(holder);
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		Map<String, Object> item = (Map<String, Object>) getItem(position);
		holder.titleTxt.setText((String)item.get(PARAM_MAIN_TITLE));
		holder.secondTxt.setText((String)item.get(PARAM_SECOND_TITLE));
		holder.settingTxt.setText((String)item.get(PARAM_SETTING_TITLE));
		
		return convertView;
	}

	class ViewHolder {
		public TextView titleTxt;
		public TextView secondTxt;
		public TextView settingTxt;
	}
}
