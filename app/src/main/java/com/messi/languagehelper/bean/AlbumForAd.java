package com.messi.languagehelper.bean;

import com.iflytek.voiceads.NativeADDataRef;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.ximalaya.ting.android.opensdk.model.album.Album;

/**
 * Created by luli on 10/11/2017.
 */

public class AlbumForAd extends Album {

    private boolean isAd;
    private boolean isAdShow;
    private NativeADDataRef mNativeADDataRef;
    private NativeExpressADView mTXADView;

    public NativeExpressADView getmTXADView() {
        return mTXADView;
    }
    public void setmTXADView(NativeExpressADView mTXADView) {
        this.mTXADView = mTXADView;
    }
    public boolean isAd() {
        return isAd;
    }

    public void setAd(boolean ad) {
        isAd = ad;
    }

    public boolean isAdShow() {
        return isAdShow;
    }

    public void setAdShow(boolean adShow) {
        isAdShow = adShow;
    }

    public NativeADDataRef getmNativeADDataRef() {
        return mNativeADDataRef;
    }

    public void setmNativeADDataRef(NativeADDataRef mNativeADDataRef) {
        this.mNativeADDataRef = mNativeADDataRef;
    }
}
