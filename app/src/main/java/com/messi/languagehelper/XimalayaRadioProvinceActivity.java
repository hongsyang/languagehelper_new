package com.messi.languagehelper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.iflytek.voiceads.IFLYNativeAd;
import com.iflytek.voiceads.config.AdError;
import com.iflytek.voiceads.config.AdKeys;
import com.iflytek.voiceads.conn.NativeDataRef;
import com.iflytek.voiceads.listener.IFLYNativeListener;
import com.messi.languagehelper.adapter.RcXmlyRadioProvinceAdapter;
import com.messi.languagehelper.bean.RadioForAd;
import com.messi.languagehelper.impl.AdapterStringListener;
import com.messi.languagehelper.service.PlayerService;
import com.messi.languagehelper.util.ADUtil;
import com.messi.languagehelper.util.KeyUtil;
import com.messi.languagehelper.util.LogUtil;
import com.messi.languagehelper.util.NumberUtil;
import com.messi.languagehelper.util.SaveData;
import com.messi.languagehelper.util.Setings;
import com.messi.languagehelper.util.TXADUtil;
import com.messi.languagehelper.util.ToastUtil;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.live.provinces.Province;
import com.ximalaya.ting.android.opensdk.model.live.provinces.ProvinceList;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.live.radio.RadioList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class XimalayaRadioProvinceActivity extends BaseActivity implements AdapterStringListener,
        IXmPlayerStatusListener {

    @BindView(R.id.listview)
    RecyclerView listview;
    private List<Province> provinceList;
    private List<Radio> radios;
    private RcXmlyRadioProvinceAdapter adapter;
    private int skip = 1;
    private int max_page = 1;
    private String province;
    private IFLYNativeAd nativeAd;
    private RadioForAd mADObject;
    private boolean loading;
    private boolean hasMore = true;
    private LinearLayoutManager mLinearLayoutManager;
    private List<NativeExpressADView> mTXADList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ximalaya_tags_activity);
        ButterKnife.bind(this);
        registerBroadcast();
        initData();
        getProvinceTask();
    }

    private void initData() {
        provinceList = new ArrayList<Province>();
        radios = new ArrayList<Radio>();
        mTXADList = new ArrayList<NativeExpressADView>();
        initSwipeRefresh();
        adapter = new RcXmlyRadioProvinceAdapter(provinceList,radios,this);
        adapter.setItems(radios);
        adapter.setHeader(new Object());
        adapter.setFooter(new Object());
        hideFooterview();
        XmPlayerManager.getInstance(this).addPlayerStatusListener(this);
        initSwipeRefresh();
        listview.setAdapter(adapter);
        mLinearLayoutManager = new LinearLayoutManager(this);
        listview.setLayoutManager(mLinearLayoutManager);
        listview.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .colorResId(R.color.text_tint)
                        .sizeResId(R.dimen.list_divider_size)
                        .marginResId(R.dimen.padding_margin, R.dimen.padding_margin)
                        .build());
        setListOnScrollListener();
    }

    private void getProvinceTask() {
        showProgressbar();
        Type type = new TypeToken<List<Province>>() {}.getType();
        List<Province> tagList = SaveData.getDataListFonJson(this, KeyUtil.XmlyRadioProvince, type);
        if (tagList != null) {
            provinceList.clear();
            provinceList.addAll(tagList);
            setProvinceData();
        } else {
            getProvince();
        }
    }

    private void setProvinceData(){
        if(provinceList.size() > 0){
            provinceList.get(0).setCreatedAt(1);
            province = String.valueOf(provinceList.get(0).getProvinceCode());
            adapter.notifyDataSetChanged();
            getRankRadios();
            loadAD();
        }
    }

    private void getProvince() {
        Map<String, String> map = new HashMap<String, String>();
        CommonRequest.getProvinces(map, new IDataCallBack<ProvinceList>() {
            @Override
            public void onSuccess(@Nullable ProvinceList pls) {
                provinceList.clear();
                provinceList.addAll(pls.getProvinceList());
                setProvinceData();
                saveData();
            }

            @Override
            public void onError(int i, String s) {
            }
        });
    }

    private void saveData() {
        SaveData.saveDataListAsJson(this, KeyUtil.XmlyRadioProvince, provinceList);
    }


    @Override
    public void OnItemClick(String item) {
        province = item;
        skip = 1;
        radios.clear();
        adapter.notifyDataSetChanged();
        getRankRadios();
        loadAD();
    }

    public void setListOnScrollListener() {
        listview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visible = mLinearLayoutManager.getChildCount();
                int total = mLinearLayoutManager.getItemCount();
                int firstVisibleItem = mLinearLayoutManager.findFirstCompletelyVisibleItemPosition();
                isADInList(recyclerView, firstVisibleItem, visible);
                if (!loading && hasMore) {
                    if ((visible + firstVisibleItem) >= total) {
                        loadAD();
                        getRankRadios();
                    }
                }
            }
        });
    }

    private void isADInList(RecyclerView view, int first, int vCount) {
        if (radios.size() > 3) {
            for (int i = first; i < (first + vCount); i++) {
                if (i < radios.size() && i > 0) {
                    Radio mAVObject = radios.get(i);
                    if (mAVObject instanceof RadioForAd) {
                        if(((RadioForAd) mAVObject).getmNativeADDataRef() != null){
                            if (!((RadioForAd) mAVObject).isAdShow()) {
                                NativeDataRef mNativeADDataRef = ((RadioForAd) mAVObject).getmNativeADDataRef();
                                boolean isExposure = mNativeADDataRef.onExposure(view.getChildAt(i % vCount));
                                LogUtil.DefalutLog("isExposure:" + isExposure);
                                if(isExposure){
                                    ((RadioForAd) mAVObject).setAdShow(isExposure);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void getRankRadios() {
        if(TextUtils.isEmpty(province)){
            finishLoading();
            return;
        }
        loading = true;
        showProgressbar();
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.RADIOTYPE , "2");
        map.put(DTransferConstants.PROVINCECODE , province);
        map.put(DTransferConstants.PAGE_SIZE, String.valueOf(Setings.page_size));
        map.put(DTransferConstants.PAGE, String.valueOf(skip));
        CommonRequest.getRadios(map, new IDataCallBack<RadioList>() {
            @Override
            public void onSuccess(@Nullable RadioList radioList) {
                finishLoading();
                if (radioList != null && radioList.getRadios() != null) {
                    initStatus(radioList.getRadios());
                    radios.addAll(radioList.getRadios());
                    skip += 1;
                    if (addAD()) {
                        adapter.notifyDataSetChanged();
                    }
                    if (skip > radioList.getTotalPage()) {
                        ToastUtil.diaplayMesShort(XimalayaRadioProvinceActivity.this, "没有了！");
                        hideFooterview();
                        hasMore = false;
                    } else {
                        hasMore = true;
                        showFooterview();
                    }
                    max_page = radioList.getTotalPage();
                }
            }

            @Override
            public void onError(int i, String s) {
                finishLoading();
            }
        });
    }

    private void initStatus(List<Radio> radios) {
        if (XmPlayerManager.getInstance(this).isPlaying()) {
            if (XmPlayerManager.getInstance(this).getCurrSound() instanceof Radio) {
                Radio mRadio = (Radio) XmPlayerManager.getInstance(this).getCurrSound();
                for (Radio radio : radios) {
                    if (radio.getDataId() == mRadio.getDataId()) {
                        radio.setActivityLive(true);
                    } else {
                        radio.setActivityLive(false);
                    }
                }
            }
        }
    }

    private void upStatus(Radio mRadio, boolean isTrue) {
        for (Radio radio : radios) {
            if (radio.getDataId() == mRadio.getDataId()) {
                radio.setActivityLive(isTrue);
            } else {
                radio.setActivityLive(false);
            }
        }
    }

    private void finishLoading() {
        loading = false;
        hideProgressbar();
        onSwipeRefreshLayoutFinish();
    }

    @Override
    public void onSwipeRefreshLayoutRefresh() {
        radios.clear();
        random();
        loadAD();
        getRankRadios();
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

    private void random() {
        if (max_page > 1) {
            skip = new Random().nextInt(max_page) + 1;
        } else {
            skip = 1;
        }
        LogUtil.DefalutLog("random:" + skip);
    }

    private void loadXFAD() {
        nativeAd = new IFLYNativeAd(this, ADUtil.XXLAD, new IFLYNativeListener() {
            @Override
            public void onConfirm() {
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onAdFailed(AdError arg0) {
                LogUtil.DefalutLog("onAdFailed---" + arg0.getErrorCode() + "---" + arg0.getErrorDescription());
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
        mADObject = new RadioForAd();
        mADObject.setmNativeADDataRef(nad);
        mADObject.setAd(true);
        if (!loading) {
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
        TXADUtil.showXXL_ZWYT(this, new NativeExpressAD.NativeExpressADListener() {
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
                if(list != null && list.size() > 0){
                    mTXADList.add(list.get(0));
                    mADObject = new RadioForAd();
                    mADObject.setmTXADView(list.get(0));
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

    private boolean addAD() {
        if (mADObject != null && radios != null && radios.size() > 0) {
            int index = radios.size() - Setings.page_size + NumberUtil.randomNumberRange(1, 2);
            if (index < 0) {
                index = 1;
            }
            radios.add(index, mADObject);
            adapter.notifyDataSetChanged();
            mADObject = null;
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        dataChange();
    }

    private void hideFooterview() {
        adapter.hideFooter();
    }

    private void showFooterview() {
        adapter.showFooter();
    }

    private void dataChange() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void reset() {
        for (Radio mRadio : radios) {
            mRadio.setActivityLive(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
        XmPlayerManager.getInstance(this).removePlayerStatusListener(this);
        if(mTXADList != null){
            for(NativeExpressADView adView : mTXADList){
                adView.destroy();
            }
        }
    }

    @Override
    public void updateUI(String music_action) {
        if (music_action.equals(PlayerService.action_loading)) {
            showProgressbar();
        } else if (music_action.equals(PlayerService.action_finish_loading)) {
            hideProgressbar();
        } else {
            dataChange();
        }
    }

    @Override
    public void onPlayStart() {
        if (XmPlayerManager.getInstance(this).getCurrSound() instanceof Radio) {
            Radio mRadio = (Radio) XmPlayerManager.getInstance(this).getCurrSound();
            upStatus(mRadio, true);
            dataChange();
        }
    }

    @Override
    public void onPlayPause() {
        setPauseStatus();
    }

    private void setPauseStatus() {
        if (XmPlayerManager.getInstance(this).getCurrSound() instanceof Radio) {
            Radio mRadio = (Radio) XmPlayerManager.getInstance(this).getCurrSound();
            upStatus(mRadio, false);
            dataChange();
        }
    }

    @Override
    public void onPlayStop() {
        setPauseStatus();
    }

    @Override
    public void onSoundPlayComplete() {
        setPauseStatus();
    }

    @Override
    public void onSoundPrepared() {

    }

    @Override
    public void onSoundSwitch(PlayableModel playableModel, PlayableModel playableModel1) {
        if (XmPlayerManager.getInstance(this).getCurrSound() instanceof Track) {
            reset();
            dataChange();
        }
    }

    @Override
    public void onBufferingStart() {

    }

    @Override
    public void onBufferingStop() {

    }

    @Override
    public void onBufferProgress(int i) {

    }

    @Override
    public void onPlayProgress(int i, int i1) {

    }

    @Override
    public boolean onError(XmPlayerException e) {
        return false;
    }
}
