package com.findher.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.findher.contact.Contacter;

public class ContacterOverlayManager {
	private List<ContacterOverlay> list = new ArrayList<ContacterOverlay>();
	private Activity activity;
	
	public ContacterOverlayManager(Activity activity){
		this.activity = activity;
	}
	
	public OverlayItem addContacter(Contacter contacter) {
		for (ContacterOverlay item : list) {
			if (contacter.getPhones().get(0).equals(item.getFirstNumber())) {
				return null;
			}
		}
		ContacterOverlay lay = new ContacterOverlay();
		GeoPoint point = new GeoPoint((int)(getRandomLat()*1E6), (int)(getRandomLon()*1E6));
		OverlayItem item = new OverlayItem(point, contacter.getName(), contacter.getName());
		item.setMarker(activity.getResources().getDrawable(R.drawable.icon_marka));
		lay.setFirstNumber(contacter.getPhones().get(0));
		lay.setContacter(contacter);
		lay.setOverlayItem(item);
		list.add(lay);
		
		return item;
	}
	

	public void updateLocation(List<ContacterLocation> locationList,ItemizedOverlay itemizedOverlay) {
		for (ContacterLocation location : locationList) {
			for (ContacterOverlay item : list) {
				if (location.getFirstNumber().equals(item.getFirstNumber())
						&& (item.getOverlayItem().getPoint() == null
								|| location.getPoint().getLatitudeE6() != item.getOverlayItem().getPoint().getLatitudeE6()
								|| location.getPoint().getLongitudeE6() != item.getOverlayItem().getPoint().getLongitudeE6())) {
					item.getOverlayItem().setGeoPoint(location.getPoint());
					itemizedOverlay.updateItem(item.getOverlayItem());
					break;
				}
			}
		}
	}
	
	private double getRandomLat() {
		return 30.232554 + new Random().nextDouble() / 100;
	}

	private double getRandomLon() {
		return 120.000871 + new Random().nextDouble() / 100;
	}
}
