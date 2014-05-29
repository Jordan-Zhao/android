package com.findher.activity;

import com.baidu.mapapi.map.OverlayItem;
import com.findher.contact.Contacter;

public class ContacterOverlay {
	private String firstNumber;
	private Contacter contacter;
	private OverlayItem overlayItem;
	
	public Contacter getContacter() {
		return contacter;
	}
	public void setContacter(Contacter contacter) {
		this.contacter = contacter;
	}
	public OverlayItem getOverlayItem() {
		return overlayItem;
	}
	public void setOverlayItem(OverlayItem overlayItem) {
		this.overlayItem = overlayItem;
	}
	public String getFirstNumber() {
		return firstNumber;
	}
	public void setFirstNumber(String firstNumber) {
		this.firstNumber = firstNumber;
	}
	
	
}
