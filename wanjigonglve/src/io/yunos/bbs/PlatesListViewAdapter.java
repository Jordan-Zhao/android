package io.yunos.bbs;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Plate ListView adapter
 * 
 * @author zhengyi.wzy
 */
public class PlatesListViewAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private ArrayList<BbsPlateData> data;
	private ImageLoader imageLoader;

	public PlatesListViewAdapter(Context context, ArrayList<BbsPlateData> data) {
		super();
		this.context = context;
		this.data = data;
		this.inflater = LayoutInflater.from(this.context);
		imageLoader = new ImageLoader(this.context);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (vi == null) {
			vi = inflater.inflate(R.layout.plates_item, null);
		}

		ImageView plageImageView = (ImageView) vi
				.findViewById(R.id.plate_image);
		TextView plateName = (TextView) vi.findViewById(R.id.plate_name);
		TextView plateTitleNum = (TextView) vi
				.findViewById(R.id.plate_title_num);
		TextView platePostNum = (TextView) vi.findViewById(R.id.plate_post_num);

		plateName.setText(data.get(position).getTitle());
		plateTitleNum.setText(data.get(position).getTopics());
		platePostNum.setText(data.get(position).getThreads());
		imageLoader.disPlayImage(data.get(position).getIcon(), plageImageView);

		return vi;
	}

}
