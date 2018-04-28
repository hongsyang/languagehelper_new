package com.messi.languagehelper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.iflytek.voiceads.AdError;
import com.iflytek.voiceads.AdKeys;
import com.iflytek.voiceads.IFLYNativeAd;
import com.iflytek.voiceads.IFLYNativeListener;
import com.iflytek.voiceads.NativeADDataRef;
import com.messi.languagehelper.adapter.RcStudyListAdapter;
import com.messi.languagehelper.bean.ReadingCategory;
import com.messi.languagehelper.dao.Reading;
import com.messi.languagehelper.db.DataBaseUtil;
import com.messi.languagehelper.impl.FragmentProgressbarListener;
import com.messi.languagehelper.impl.TablayoutOnSelectedListener;
import com.messi.languagehelper.service.PlayerService;
import com.messi.languagehelper.util.ADUtil;
import com.messi.languagehelper.util.AVOUtil;
import com.messi.languagehelper.util.LogUtil;
import com.messi.languagehelper.util.NumberUtil;
import com.messi.languagehelper.util.Settings;
import com.messi.languagehelper.util.TXADUtil;
import com.messi.languagehelper.util.ToastUtil;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jzvd.JZVideoPlayer;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class StudyFragment extends BaseFragment implements TablayoutOnSelectedListener {

    @BindView(R.id.listview)
    RecyclerView listview;
    @BindView(R.id.tablayout)
    TabLayout tablayout;
    @BindView(R.id.search_btn)
    FrameLayout searchBtn;
    private RcStudyListAdapter mAdapter;
    private List<Reading> avObjects;
    private List<AVObject> tempList;
    private int skip = 0;
    private int maxRandom;
    private IFLYNativeAd nativeAd;
    private boolean loading;
    private boolean hasMore;
    private Reading mADObject;
    private String category;
    private LinearLayoutManager mLinearLayoutManager;
    private List<ReadingCategory> categories;
    private boolean isNeedClear;
    private List<NativeExpressADView> mTXADList;

    public static StudyFragment getInstance() {
        return new StudyFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            registerBroadcast();
            mProgressbarListener = (FragmentProgressbarListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement FragmentProgressbarListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.study_fragment, null);
        ButterKnife.bind(this, view);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        avObjects = new ArrayList<Reading>();
        mTXADList = new ArrayList<NativeExpressADView>();
        avObjects.addAll(DataBaseUtil.getInstance().getReadingList(Settings.page_size, "", "", ""));
        skip = 0;
        initSwipeRefresh(view);
        mAdapter = new RcStudyListAdapter(avObjects, mProgressbarListener, getActivity());
        mAdapter.setItems(avObjects);
        mAdapter.setFooter(new Object());
        mAdapter.setHeader(new Object());
        hideFooterview();
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        listview.setLayoutManager(mLinearLayoutManager);
        listview.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .colorResId(R.color.text_tint)
                        .sizeResId(R.dimen.list_divider_size)
                        .marginResId(R.dimen.padding_margin, R.dimen.padding_margin)
                        .build());
        listview.setAdapter(mAdapter);
        setListOnScrollListener();
        categories = getTabItem(getContext());
        initTablayout();
        getMaxPageNumberBackground();
    }

    private void initTablayout() {
        for (ReadingCategory item : categories) {
            tablayout.addTab(tablayout.newTab().setText(item.getName()));
        }
        tablayout.getTabAt(0).select();
        tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onTabSelectedListener(categories.get(tab.getPosition()));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabReselectedListener(categories.get(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
        });
    }

    private void random() {
        skip = (int) Math.round(Math.random() * maxRandom);
        LogUtil.DefalutLog("skip:" + skip);
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
                        QueryTask();
                        LogUtil.DefalutLog("StudyFragment-setListOnScrollListener-QueryTask");
                    }
                }
            }
        });
    }

    private void isADInList(RecyclerView view, int first, int vCount) {
        if (avObjects.size() > 2) {
            for (int i = first; i < (first + vCount); i++) {
                if (i < avObjects.size() && i > 0) {
                    Reading mAVObject = avObjects.get(i);
                    if (mAVObject != null && mAVObject.isAd()) {
                        if (!mAVObject.isAdShow()) {
                            NativeADDataRef mNativeADDataRef = mAVObject.getmNativeADDataRef();
                            boolean isExposure = mNativeADDataRef.onExposured(view.getChildAt(i % vCount));
                            LogUtil.DefalutLog("isExposure:" + isExposure);
                            if(isExposure){
                                mAVObject.setAdShow(isExposure);
                            }
                        }
                    }
                }
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
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            JZVideoPlayer.releaseAllVideos();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void loadDataOnStart() {
        super.loadDataOnStart();
        skip = 0;
        loadAD();
        QueryTask();
    }

    @Override
    public void onSwipeRefreshLayoutRefresh() {
        isNeedClear = true;
        random();
        refresh();
    }

    private void refresh() {
        loadAD();
        hideFooterview();
        QueryTask();
    }

    private void loadAD(){
        if(ADUtil.Advertiser.equals(ADUtil.Advertiser_XF)){
            loadXFAD();
        }else {
            loadTXAD();
        }
    }

    private void QueryTask() {
        loading = true;
        showProgressbar();
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                getNetworkData();
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(String s) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        onFinishLoadData();
                    }
                });

    }

    private void getNetworkData() {
        AVQuery<AVObject> query = new AVQuery<AVObject>(AVOUtil.Reading.Reading);
        if (!TextUtils.isEmpty(category)) {
            query.whereEqualTo(AVOUtil.Reading.category, category);
        }
        query.addDescendingOrder(AVOUtil.Reading.publish_time);
        query.skip(skip);
        query.limit(Settings.page_size);
        try {
            tempList = query.find();
            LogUtil.DefalutLog("skip:" + skip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onFinishLoadData() {
        loading = false;
        hasMore = true;
        hideProgressbar();
        onSwipeRefreshLayoutFinish();
        if (tempList != null) {
            if (tempList.size() == 0) {
                ToastUtil.diaplayMesShort(getContext(), "没有了！");
                hideFooterview();
                hasMore = false;
            } else {
                if (avObjects != null && mAdapter != null) {
                    if (skip == 0) {
                        avObjects.clear();
                    }
                    if (isNeedClear) {
                        avObjects.clear();
                        isNeedClear = false;
                    }
                    changeData(tempList, avObjects);
                    if (addAD()) {
                        mAdapter.notifyDataSetChanged();
                    }
                    skip += Settings.page_size;
                    showFooterview();
                }
            }
        }
    }

    private void loadXFAD() {
        nativeAd = new IFLYNativeAd(getContext(), ADUtil.XXLAD, new IFLYNativeListener() {
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
            public void onADLoaded(List<NativeADDataRef> adList) {
                LogUtil.DefalutLog("onADLoaded---");
                if (adList != null && adList.size() > 0) {
                    NativeADDataRef nad = adList.get(0);
                    addXFAD(nad);
                }
            }
        });
        nativeAd.setParameter(AdKeys.DOWNLOAD_ALERT, "true");
        nativeAd.loadAd(1);
    }

    private void addXFAD(NativeADDataRef nad){
        mADObject = new Reading();
        mADObject.setmNativeADDataRef(nad);
        mADObject.setAd(true);
        if (!loading) {
            addAD();
        }
    }

    private void onADFaile(){
        if(ADUtil.isHasLocalAd()){
            NativeADDataRef nad = ADUtil.getRandomAd();
            addXFAD(nad);
        }
    }

    private void loadTXAD(){
        TXADUtil.showXXL(getActivity(), new NativeExpressAD.NativeExpressADListener() {
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
                    mADObject = new Reading();
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
        if (mADObject != null && avObjects != null && avObjects.size() > 0) {
            int index = avObjects.size() - Settings.page_size + NumberUtil.randomNumberRange(1, 2);
            if (index < 0) {
                index = 0;
            }
            avObjects.add(index, mADObject);
            mAdapter.notifyDataSetChanged();
            mADObject = null;
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onTabSelectedListener(ReadingCategory mReadingCategory) {
        listview.scrollToPosition(1);
        category = mReadingCategory.getCategory();
        skip = 0;
        refresh();
    }

    @Override
    public void onTabReselectedListener(ReadingCategory mReadingCategory) {
        listview.scrollToPosition(1);
        onSwipeRefreshLayoutRefresh();
    }

    private void getMaxPageNumberBackground() {
        maxRandom = (int) ((Math.random() * 1200));
    }

    private void hideFooterview() {
        mAdapter.hideFooter();
    }

    private void showFooterview() {
        mAdapter.showFooter();
    }

    public void onTabReselected(int index) {
        listview.scrollToPosition(0);
        onSwipeRefreshLayoutRefresh();
    }

    public static void changeData(List<AVObject> avObjectlist, List<Reading> avObjects) {
        for (AVObject item : avObjectlist) {
            Reading mReading = new Reading();
            mReading.setObject_id(item.getObjectId());
            if(item.has(AVOUtil.Reading.category)){
                mReading.setCategory(item.getString(AVOUtil.Reading.category));
            }
            if(item.has(AVOUtil.Reading.content)){
                mReading.setContent(item.getString(AVOUtil.Reading.content));
            }
            if(item.has(AVOUtil.Reading.type_id)){
                mReading.setType_id(item.getString(AVOUtil.Reading.type_id));
            }
            if(item.has(AVOUtil.Reading.type_name)){
                mReading.setType_name(item.getString(AVOUtil.Reading.type_name));
            }
            if(item.has(AVOUtil.Reading.title)){
                mReading.setTitle(item.getString(AVOUtil.Reading.title));
            }
            if(item.has(AVOUtil.Reading.item_id)){
                mReading.setItem_id(String.valueOf(item.getNumber(AVOUtil.Reading.item_id)));
            }
            if(item.has(AVOUtil.Reading.img_url)){
                mReading.setImg_url(item.getString(AVOUtil.Reading.img_url));
            }
            if(item.has(AVOUtil.Reading.publish_time)){
                mReading.setPublish_time(String.valueOf(item.getDate(AVOUtil.Reading.publish_time).getTime()));
            }
            if(item.has(AVOUtil.Reading.img_type)){
                mReading.setImg_type(item.getString(AVOUtil.Reading.img_type));
            }
            if(item.has(AVOUtil.Reading.source_name)){
                mReading.setSource_name(item.getString(AVOUtil.Reading.source_name));
            }
            if(item.has(AVOUtil.Reading.source_url)){
                mReading.setSource_url(item.getString(AVOUtil.Reading.source_url));
            }
            if(item.has(AVOUtil.Reading.type)){
                mReading.setType(item.getString(AVOUtil.Reading.type));
            }
            if(item.has(AVOUtil.Reading.media_url)){
                mReading.setMedia_url(item.getString(AVOUtil.Reading.media_url));
            }
            if(item.has(AVOUtil.Reading.content_type)){
                mReading.setContent_type(item.getString(AVOUtil.Reading.content_type));
            }
            DataBaseUtil.getInstance().saveOrGetStatus(mReading);
            avObjects.add(mReading);
        }
    }

    public static void changeData(List<AVObject> avObjectlist, List<Reading> avObjects, boolean isAddToHead) {

        for (AVObject item : avObjectlist) {
            Reading mReading = new Reading();
            mReading.setObject_id(item.getObjectId());
            mReading.setCategory(item.getString(AVOUtil.Reading.category));
            mReading.setContent(item.getString(AVOUtil.Reading.content));
            mReading.setType_id(item.getString(AVOUtil.Reading.type_id));
            mReading.setType_name(item.getString(AVOUtil.Reading.type_name));
            mReading.setTitle(item.getString(AVOUtil.Reading.title));
            mReading.setItem_id(String.valueOf(item.getNumber(AVOUtil.Reading.item_id)));
            mReading.setImg_url(item.getString(AVOUtil.Reading.img_url));
            if(item.has(AVOUtil.Reading.publish_time)){
                mReading.setPublish_time(String.valueOf(item.getDate(AVOUtil.Reading.publish_time).getTime()));
            }
            mReading.setImg_type(item.getString(AVOUtil.Reading.img_type));
            mReading.setSource_name(item.getString(AVOUtil.Reading.source_name));
            mReading.setSource_url(item.getString(AVOUtil.Reading.source_url));
            mReading.setType(item.getString(AVOUtil.Reading.type));
            mReading.setMedia_url(item.getString(AVOUtil.Reading.media_url));
            mReading.setContent_type(item.getString(AVOUtil.Reading.content_type));
            DataBaseUtil.getInstance().saveOrGetStatus(mReading);
            if (isAddToHead) {
                avObjects.add(0, mReading);
            } else {
                avObjects.add(mReading);
            }
        }
    }

    public static List<ReadingCategory> getTabItem(Context context) {
        List<ReadingCategory> readingCategories = new ArrayList<ReadingCategory>();
        readingCategories.add(new ReadingCategory(context.getString(R.string.recommend), ""));
        readingCategories.add(new ReadingCategory(context.getString(R.string.reading), "shuangyu_reading"));
        readingCategories.add(new ReadingCategory(context.getString(R.string.title_listening), "listening"));
        readingCategories.add(new ReadingCategory(context.getString(R.string.title_word_study_vocabulary), "word"));
        readingCategories.add(new ReadingCategory(context.getString(R.string.spoken_english_practice), "spoken_english"));
        readingCategories.add(new ReadingCategory(context.getString(R.string.title_composition), "composition"));
        readingCategories.add(new ReadingCategory(context.getString(R.string.examination), "examination"));
        readingCategories.add(new ReadingCategory(context.getString(R.string.title_english_story), "story"));
        readingCategories.add(new ReadingCategory(context.getString(R.string.title_english_jokes), "jokes"));
        return readingCategories;
    }

    @OnClick(R.id.search_btn)
    public void onViewClicked() {
        toActivity(SearchActivity.class,null);
        AVAnalytics.onEvent(getContext(), "tab4_to_search");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        JZVideoPlayer.releaseAllVideos();
        unregisterBroadcast();
        if(mTXADList != null){
            for(NativeExpressADView adView : mTXADList){
                adView.destroy();
            }
        }
    }
}
