package com.messi.languagehelper;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.iflytek.voiceads.IFLYNativeAd;
import com.iflytek.voiceads.config.AdError;
import com.iflytek.voiceads.config.AdKeys;
import com.iflytek.voiceads.conn.NativeDataRef;
import com.iflytek.voiceads.listener.IFLYNativeListener;
import com.karumi.headerrecyclerview.HeaderSpanSizeLookup;
import com.messi.languagehelper.adapter.RcXVideoAdapter;
import com.messi.languagehelper.util.ADUtil;
import com.messi.languagehelper.util.AVOUtil;
import com.messi.languagehelper.util.KeyUtil;
import com.messi.languagehelper.util.LogUtil;
import com.messi.languagehelper.util.NumberUtil;
import com.messi.languagehelper.util.Setings;
import com.messi.languagehelper.util.TXADUtil;
import com.messi.languagehelper.util.ToastUtil;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;

import java.util.ArrayList;
import java.util.List;

public class XVideoSearchResultActivity extends BaseActivity{

    private static final int NUMBER_OF_COLUMNS = 2;
    private RecyclerView category_lv;
    private RcXVideoAdapter mAdapter;
    private GridLayoutManager layoutManager;
    private List<AVObject> mList;
    private int skip = 0;
    private int max_count = 1000;
    private boolean loading;
    private boolean hasMore = true;
    private boolean isNeedClear = true;
    private IFLYNativeAd nativeAd;
    private AVObject mADObject;
    private List<NativeExpressADView> mTXADList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xvideo_fragment);
        initViews();
        initSwipeRefresh();
        randomPage();
        loadAD();
        RequestAsyncTask();
    }

    private void initViews() {
        mList = new ArrayList<AVObject>();
        mTXADList = new ArrayList<NativeExpressADView>();
        category_lv = (RecyclerView) findViewById(R.id.listview);
        category_lv.setHasFixedSize(true);
        mAdapter = new RcXVideoAdapter(mList);
        layoutManager = new GridLayoutManager(this, NUMBER_OF_COLUMNS);
        HeaderSpanSizeLookup headerSpanSizeLookup = new HeaderSpanSizeLookup(mAdapter, layoutManager);
        layoutManager.setSpanSizeLookup(headerSpanSizeLookup);
        category_lv.setLayoutManager(layoutManager);
        mAdapter.setFooter(new Object());
        mAdapter.setItems(mList);
        category_lv.setAdapter(mAdapter);
        setListOnScrollListener();
    }

    public void setListOnScrollListener() {
        category_lv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visible = layoutManager.getChildCount();
                int total = layoutManager.getItemCount();
                int firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition();
                isADInList(recyclerView, firstVisibleItem, visible);
                if (!loading && hasMore) {
                    if ((visible + firstVisibleItem) >= total) {
                        loadAD();
                        RequestAsyncTask();
                    }
                }
            }
        });
    }

    private void isADInList(RecyclerView view,int first, int vCount){
        if(mList.size() > 3){
            for(int i=first;i< (first+vCount);i++){
                if(i < mList.size() && i > 0){
                    AVObject mAVObject = mList.get(i);
                    if(mAVObject != null && mAVObject.get(KeyUtil.ADKey) != null){
                        if(!(Boolean) mAVObject.get(KeyUtil.ADIsShowKey)){
                            NativeDataRef mNativeADDataRef = (NativeDataRef) mAVObject.get(KeyUtil.ADKey);
                            boolean isExposure = mNativeADDataRef.onExposure(view.getChildAt(i%vCount));
                            LogUtil.DefalutLog("isExposure:"+isExposure);
                            if(isExposure){
                                mAVObject.put(KeyUtil.ADIsShowKey, isExposure);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onSwipeRefreshLayoutRefresh() {
        loadAD();
        randomPage();
        hasMore = true;
        RequestAsyncTask();
    }

    private void loadAD(){
        if(ADUtil.IsShowAD){
            if(ADUtil.Advertiser.equals(ADUtil.Advertiser_XF)){
                loadXFAD();
            }else {
                loadTXAD();
            }
        }
    }

    private void randomPage(){
        isNeedClear = true;
        if(max_count > 0){
            skip = NumberUtil.getRandomNumber(max_count);
            LogUtil.DefalutLog("spip:"+skip);
        }else {
            skip = 0;
        }
    }

    private void RequestAsyncTask() {
        showProgressbar();
        loading = true;
        AVQuery<AVObject> query = new AVQuery<AVObject>(AVOUtil.XVideo.XVideo);
//        query.whereEqualTo(AVOUtil.XVideo.category,"english");
        query.orderByDescending(AVOUtil.XVideo.play_count);
        query.skip(skip);
        query.limit(Setings.ca_psize);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                hideProgressbar();
                loading = false;
                onSwipeRefreshLayoutFinish();
                if(list != null){
                    if(list.size() == 0){
                        hasMore = false;
                        hideFooterview();
                    }else {
                        if(isNeedClear){
                            isNeedClear = false;
                            mList.clear();
                        }
                        mList.addAll(list);
                        if(addAD()){
                            mAdapter.notifyDataSetChanged();
                        }
                        if(list.size() < Setings.ca_psize){
                            hasMore = false;
                            hideFooterview();
                        }else {
                            skip += Setings.ca_psize;
                            hasMore = true;
                            showFooterview();
                        }
                    }
                }else{
                    ToastUtil.diaplayMesShort(XVideoSearchResultActivity.this, "加载失败，下拉可刷新");
                }
            }
        });
    }

    private void loadXFAD(){
        nativeAd = new IFLYNativeAd(this, ADUtil.XXLAD, new IFLYNativeListener() {
            @Override
            public void onConfirm() {
            }
            @Override
            public void onCancel() {
            }
            @Override
            public void onAdFailed(AdError arg0) {
                LogUtil.DefalutLog("onAdFailed---"+arg0.getErrorCode()+"---"+arg0.getErrorDescription());
                if(ADUtil.Advertiser.equals(ADUtil.Advertiser_XF)){
                    loadTXAD();
                }else {
                    onADFaile();
                }
            }
            @Override
            public void onAdLoaded(NativeDataRef nativeDataRef) {
                if(nativeDataRef != null){
                    addXFAD(nativeDataRef);
                }
            }
        });
        nativeAd.setParameter(AdKeys.DOWNLOAD_ALERT, "true");
        nativeAd.loadAd();
    }

    private void addXFAD(NativeDataRef nad){
        mADObject = new AVObject();
        mADObject.put(KeyUtil.ADKey, nad);
        mADObject.put(KeyUtil.ADIsShowKey, false);
        if(!loading){
            addAD();
        }
    }

    private void onADFaile(){
        if(ADUtil.isHasLocalAd()){
            NativeDataRef nad = ADUtil.getRandomAd(this);
            addXFAD(nad);
        }
    }

    private void loadTXAD(){
        TXADUtil.showCDTZX(this, new NativeExpressAD.NativeExpressADListener() {
            @Override
            public void onNoAD(com.qq.e.comm.util.AdError adError) {
                LogUtil.DefalutLog(adError.getErrorMsg());
                if(ADUtil.Advertiser.equals(ADUtil.Advertiser_TX)){
                    loadXFAD();
                }else {
                    onADFaile();
                }
            }
            @Override
            public void onADLoaded(List<NativeExpressADView> list) {
                LogUtil.DefalutLog("onADLoaded");
                if(list != null && list.size() > 0 && mTXADList != null){
                    mTXADList.add(list.get(0));
                    mADObject = new AVObject();
                    mADObject.put(KeyUtil.TXADView, list.get(0));
                    if (!loading) {
                        addAD();
                    }
                }
            }
            @Override
            public void onRenderFail(NativeExpressADView nativeExpressADView) {
                LogUtil.DefalutLog("onRenderFail");
            }
            @Override
            public void onRenderSuccess(NativeExpressADView nativeExpressADView) {
                LogUtil.DefalutLog("onRenderSuccess");
            }
            @Override
            public void onADExposure(NativeExpressADView nativeExpressADView) {
                LogUtil.DefalutLog("onADExposure");
            }
            @Override
            public void onADClicked(NativeExpressADView nativeExpressADView) {
                LogUtil.DefalutLog("onADClicked");
            }
            @Override
            public void onADClosed(NativeExpressADView nativeExpressADView) {
                LogUtil.DefalutLog("onADClosed");
            }
            @Override
            public void onADLeftApplication(NativeExpressADView nativeExpressADView) {
                LogUtil.DefalutLog("onADLeftApplication");
            }
            @Override
            public void onADOpenOverlay(NativeExpressADView nativeExpressADView) {
                LogUtil.DefalutLog("onADOpenOverlay");
            }
            @Override
            public void onADCloseOverlay(NativeExpressADView nativeExpressADView) {
                LogUtil.DefalutLog("onADCloseOverlay");
            }
        });
    }

    private boolean addAD(){
        if(mADObject != null && mList != null && mList.size() > 0){
            int index = mList.size() - Setings.page_size + NumberUtil.randomNumberRange(1, 2);
            if(index < 1){
                index = 1;
            }
            mList.add(index,mADObject);
            mAdapter.notifyDataSetChanged();
            mADObject = null;
            return false;
        }else{
            return true;
        }
    }

    private void hideFooterview(){
        mAdapter.hideFooter();
    }

    private void showFooterview(){
        mAdapter.showFooter();
    }

}
