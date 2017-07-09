package com.messi.languagehelper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.iflytek.voiceads.AdError;
import com.iflytek.voiceads.AdKeys;
import com.iflytek.voiceads.IFLYNativeAd;
import com.iflytek.voiceads.IFLYNativeListener;
import com.iflytek.voiceads.NativeADDataRef;
import com.messi.languagehelper.adapter.RcReadingListAdapter;
import com.messi.languagehelper.dao.Reading;
import com.messi.languagehelper.db.DataBaseUtil;
import com.messi.languagehelper.service.PlayerService;
import com.messi.languagehelper.util.ADUtil;
import com.messi.languagehelper.util.AVOUtil;
import com.messi.languagehelper.util.LogUtil;
import com.messi.languagehelper.util.NumberUtil;
import com.messi.languagehelper.util.Settings;
import com.messi.languagehelper.util.ToastUtil;
import com.messi.languagehelper.wxapi.WXEntryActivity;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;

public class ListenActivity extends BaseActivity implements OnClickListener {

    @BindView(R.id.action_previous)
    ImageView actionPrevious;
    @BindView(R.id.action_previous_layout)
    FrameLayout actionPreviousLayout;
    @BindView(R.id.action_play)
    ImageView actionPlay;
    @BindView(R.id.action_play_layout)
    FrameLayout actionPlayLayout;
    @BindView(R.id.action_next)
    ImageView actionNext;
    @BindView(R.id.action_next_layout)
    FrameLayout actionNextLayout;
    @BindView(R.id.listview)
    RecyclerView listview;
    private RcReadingListAdapter mAdapter;
    private List<Reading> avObjects;
    private String category = "listening";
    private int skip = 0;
    private int maxRandom;
    private IFLYNativeAd nativeAd;
    private boolean loading;
    private boolean hasMore = true;
    private Reading mADObject;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listen_activity);
        ButterKnife.bind(this);
        registerBroadcast();
        initViews();
        loadAD();
        new QueryTask().execute();
        getMaxPageNumberBackground();
    }

    private void initViews() {
        getSupportActionBar().setTitle(getResources().getString(R.string.title_listening));
        avObjects = new ArrayList<Reading>();
        avObjects.addAll(DataBaseUtil.getInstance().getReadingList(Settings.page_size, category, "", ""));
        initSwipeRefresh();
        mAdapter = new RcReadingListAdapter(avObjects);
        mAdapter.setItems(avObjects);
        mAdapter.setFooter(new Object());
        hideFooterview();
        mLinearLayoutManager = new LinearLayoutManager(this);
        listview.setLayoutManager(mLinearLayoutManager);
        listview.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .colorResId(R.color.text_tint)
                        .sizeResId(R.dimen.list_divider_size)
                        .marginResId(R.dimen.padding_margin, R.dimen.padding_margin)
                        .build());
        listview.setAdapter(mAdapter);
        setListOnScrollListener();
        if(WXEntryActivity.musicSrv.PlayerStatus == 1){
            actionPlay.setImageResource(R.drawable.ic_pause_white);
        }
    }

    private void random() {
        skip = (int) Math.round(Math.random() * maxRandom);
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
                        new QueryTask().execute();
                    }
                }
            }
        });
    }

    private void isADInList(RecyclerView view, int first, int vCount) {
        if (avObjects.size() > 3) {
            for (int i = first; i < (first + vCount); i++) {
                if (i < avObjects.size() && i > 0) {
                    Reading mAVObject = avObjects.get(i);
                    if (mAVObject != null && mAVObject.isAd()) {
                        if (!mAVObject.isAdShow()) {
                            NativeADDataRef mNativeADDataRef = mAVObject.getmNativeADDataRef();
                            boolean isExposure = mNativeADDataRef.onExposured(view.getChildAt(i % vCount));
                            LogUtil.DefalutLog("isExposure:" + isExposure);
                            mAVObject.setAdShow(isExposure);
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

//        else if(music_action.equals(PlayerService.action_start)){
//            actionPlay.setImageResource(R.drawable.ic_play_white);
//            mAdapter.notifyDataSetChanged();
//        }else if (music_action.equals(PlayerService.action_pause)) {
//            actionPlay.setImageResource(R.drawable.ic_pause_white);
//        }
    }

    private void hideFooterview() {
        mAdapter.hideFooter();
    }

    private void showFooterview() {
        mAdapter.showFooter();
    }

    @Override
    public void onSwipeRefreshLayoutRefresh() {
        loadAD();
        hideFooterview();
        random();
        avObjects.clear();
        mAdapter.notifyDataSetChanged();
        new QueryTask().execute();
    }

    private class QueryTask extends AsyncTask<Void, Void, List<AVObject>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressbar();
            loading = true;
        }

        @Override
        protected List<AVObject> doInBackground(Void... params) {
            AVQuery<AVObject> query = new AVQuery<AVObject>(AVOUtil.Reading.Reading);
            query.whereEqualTo(AVOUtil.Reading.category, category);
            query.addDescendingOrder(AVOUtil.Reading.publish_time);
            query.skip(skip);
            query.limit(Settings.page_size);
            try {
                return query.find();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<AVObject> avObject) {
            loading = false;
            hideProgressbar();
            onSwipeRefreshLayoutFinish();
            if (avObject != null) {
                if (avObject.size() == 0) {
                    ToastUtil.diaplayMesShort(ListenActivity.this, "没有了！");
                    hideFooterview();
                } else {
                    if (skip == 0) {
                        avObjects.clear();
                    }
                    StudyFragment.changeData(avObject, avObjects);
                    if (addAD()) {
                        mAdapter.notifyDataSetChanged();
                    }
                    skip += Settings.page_size;
                    showFooterview();
                }
            } else {
                ToastUtil.diaplayMesShort(ListenActivity.this, "加载失败，下拉可刷新");
            }
            if (skip == maxRandom) {
                hasMore = false;
            }

        }
    }

    private void loadAD() {
        nativeAd = new IFLYNativeAd(this, ADUtil.randomAd(), new IFLYNativeListener() {
            @Override
            public void onConfirm() {
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onAdFailed(AdError arg0) {
                LogUtil.DefalutLog("onAdFailed---" + arg0.getErrorCode() + "---" + arg0.getErrorDescription());
            }

            @Override
            public void onADLoaded(List<NativeADDataRef> adList) {
                if (adList != null && adList.size() > 0) {
                    NativeADDataRef nad = adList.get(0);
                    mADObject = new Reading();
                    mADObject.setmNativeADDataRef(nad);
                    mADObject.setAd(true);
                    if (!loading) {
                        addAD();
                    }
                }
            }
        });
        nativeAd.setParameter(AdKeys.DOWNLOAD_ALERT, "true");
        nativeAd.loadAd(1);
    }

    private boolean addAD() {
        if (mADObject != null && avObjects != null && avObjects.size() > 0) {
            int index = avObjects.size() - Settings.page_size + NumberUtil.randomNumberRange(2, 4);
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
    public void onBackPressed() {
        if (JCVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
    }

    @Override
    public void onClick(View v) {
    }

    private void getMaxPageNumberBackground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AVQuery<AVObject> query = new AVQuery<AVObject>(AVOUtil.Reading.Reading);
                    query.whereEqualTo(AVOUtil.Reading.category, category);
                    maxRandom = query.count();
                    maxRandom /= Settings.page_size;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
    }

}