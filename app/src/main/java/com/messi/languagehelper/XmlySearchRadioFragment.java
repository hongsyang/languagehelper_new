package com.messi.languagehelper;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iflytek.voiceads.conn.NativeDataRef;
import com.messi.languagehelper.ViewModel.XXLForXMLYRadioModel;
import com.messi.languagehelper.adapter.RcXmlySearchRaidoAdapter;
import com.messi.languagehelper.bean.RadioForAd;
import com.messi.languagehelper.impl.FragmentProgressbarListener;
import com.messi.languagehelper.service.PlayerService;
import com.messi.languagehelper.util.KeyUtil;
import com.messi.languagehelper.util.LogUtil;
import com.messi.languagehelper.util.Setings;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.live.radio.RadioList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class XmlySearchRadioFragment extends BaseFragment implements
        IXmPlayerStatusListener {

    RecyclerView listview;
    private List<Radio> radios;
    private RcXmlySearchRaidoAdapter adapter;
    private int skip = 1;
    private int max_page = 1;
    private String search_text;
    private LinearLayoutManager mLinearLayoutManager;
    private XXLForXMLYRadioModel mXXLModel;

    public static Fragment newInstance(String search_text) {
        XmlySearchRadioFragment fragment = new XmlySearchRadioFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KeyUtil.SearchKey, search_text);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mProgressbarListener = (FragmentProgressbarListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement FragmentProgressbarListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle mBundle = getArguments();
        this.search_text = mBundle.getString(KeyUtil.SearchKey);
        registerBroadcast();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.xmly_search_reasult_fragment, container, false);
        initSwipeRefresh(view);
        listview = (RecyclerView)view.findViewById(R.id.listview);
        initData();
        return view;
    }

    private void initData() {
        radios = new ArrayList<Radio>();
        mXXLModel = new XXLForXMLYRadioModel(getActivity());
        adapter = new RcXmlySearchRaidoAdapter(radios);
        adapter.setItems(radios);
        adapter.setFooter(new Object());
        mXXLModel.setAdapter(radios,adapter);
        hideFooterview();
        XmPlayerManager.getInstance(getContext()).addPlayerStatusListener(this);
        listview.setAdapter(adapter);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        listview.setLayoutManager(mLinearLayoutManager);
        listview.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .colorResId(R.color.text_tint)
                        .sizeResId(R.dimen.list_divider_size)
                        .marginResId(R.dimen.padding_2, R.dimen.padding_2)
                        .build());
        setListOnScrollListener();
    }

    @Override
    public void loadDataOnStart() {
        getRankRadios();
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
                if (!mXXLModel.loading && mXXLModel.hasMore) {
                    if ((visible + firstVisibleItem) >= total) {
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
        if(TextUtils.isEmpty(search_text)){
            finishLoading();
            return;
        }
        if(mXXLModel != null){
            mXXLModel.loading = true;
        }
        showProgressbar();
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.SEARCH_KEY , search_text);
        map.put(DTransferConstants.PAGE_SIZE, String.valueOf(Setings.page_size));
        map.put(DTransferConstants.PAGE, String.valueOf(skip));
        CommonRequest.getSearchedRadios(map, new IDataCallBack<RadioList>() {
            @Override
            public void onSuccess(@Nullable RadioList radioList) {
                finishLoading();
                if (radioList != null && radioList.getRadios() != null) {
                    initStatus(radioList.getRadios());
                    radios.addAll(radioList.getRadios());
                    skip += 1;
                    adapter.notifyDataSetChanged();
                    loadAD();
                    if (skip > radioList.getTotalPage()) {
//                        ToastUtil.diaplayMesShort(getContext(), "没有了！");
                        hideFooterview();
                        mXXLModel.hasMore = false;
                    } else {
                        mXXLModel.hasMore = true;
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
        if (XmPlayerManager.getInstance(getContext()).isPlaying()) {
            if (XmPlayerManager.getInstance(getContext()).getCurrSound() instanceof Radio) {
                Radio mRadio = (Radio) XmPlayerManager.getInstance(getContext()).getCurrSound();
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
        mXXLModel.loading = false;
        hideProgressbar();
        onSwipeRefreshLayoutFinish();
    }

    @Override
    public void onSwipeRefreshLayoutRefresh() {
        radios.clear();
        random();
        getRankRadios();
    }

    private void loadAD(){
        if (mXXLModel != null) {
            mXXLModel.showAd();
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
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
        XmPlayerManager.getInstance(getContext()).removePlayerStatusListener(this);
        if(mXXLModel != null){
            mXXLModel.onDestroy();
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
        if (XmPlayerManager.getInstance(getContext()).getCurrSound() instanceof Radio) {
            Radio mRadio = (Radio) XmPlayerManager.getInstance(getContext()).getCurrSound();
            upStatus(mRadio, true);
            dataChange();
        }
    }

    @Override
    public void onPlayPause() {
        setPauseStatus();
    }

    private void setPauseStatus() {
        if (XmPlayerManager.getInstance(getContext()).getCurrSound() instanceof Radio) {
            Radio mRadio = (Radio) XmPlayerManager.getInstance(getContext()).getCurrSound();
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
        if (XmPlayerManager.getInstance(getContext()).getCurrSound() instanceof Track) {
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
