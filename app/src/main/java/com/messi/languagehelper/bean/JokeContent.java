package com.messi.languagehelper.bean;


import com.iflytek.voiceads.conn.NativeDataRef;

public class JokeContent {
	private String ct;

	private String img;

	private String title;
	
	private String text;

	private int type;

	private NativeDataRef mNativeADDataRef;

	private boolean isHasShowAD;

	public boolean isHasShowAD() {
		return isHasShowAD;
	}

	public void setHasShowAD(boolean hasShowAD) {
		isHasShowAD = hasShowAD;
	}

	public NativeDataRef getmNativeADDataRef() {
		return mNativeADDataRef;
	}

	public void setmNativeADDataRef(NativeDataRef mNativeADDataRef) {
		this.mNativeADDataRef = mNativeADDataRef;
	}


	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setCt(String ct) {
		this.ct = ct;
	}

	public String getCt() {
		return this.ct;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getImg() {
		return this.img;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return this.title;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return this.type;
	}

}
