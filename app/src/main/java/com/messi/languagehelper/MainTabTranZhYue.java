package com.messi.languagehelper;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.messi.languagehelper.adapter.RcTranZhYueListAdapter;
import com.messi.languagehelper.box.BoxHelper;
import com.messi.languagehelper.box.TranResultZhYue;
import com.messi.languagehelper.event.FinishEvent;
import com.messi.languagehelper.event.TranAndDicRefreshEvent;
import com.messi.languagehelper.impl.FragmentProgressbarListener;
import com.messi.languagehelper.impl.OnTranZhYueFinishListener;
import com.messi.languagehelper.util.KeyUtil;
import com.messi.languagehelper.util.LogUtil;
import com.messi.languagehelper.util.NetworkUtil;
import com.messi.languagehelper.util.NullUtil;
import com.messi.languagehelper.util.PlayUtil;
import com.messi.languagehelper.util.Setings;
import com.messi.languagehelper.util.ToastUtil;
import com.messi.languagehelper.util.TranslateUtil;
import com.messi.languagehelper.views.DividerItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MainTabTranZhYue extends BaseFragment {

    private RecyclerView recent_used_lv;
    private TranResultZhYue currentDialogBean;
    private RcTranZhYueListAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private List<TranResultZhYue> beans;
    private String lastSearch;
    private int skip;
    private boolean noMoreData;
    private boolean isNeedRefresh;

    public static MainTabTranZhYue getInstance(FragmentProgressbarListener listener) {
        MainTabTranZhYue mMainFragment = new MainTabTranZhYue();
        mMainFragment.setmProgressbarListener(listener);
        return mMainFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.main_tab_tran, null);
        LogUtil.DefalutLog("MainTabTranZhYue-onCreateView");
        init(view);
        return view;
    }

    private void init(View view) {
        recent_used_lv = (RecyclerView) view.findViewById(R.id.recent_used_lv);
        beans = new ArrayList<TranResultZhYue>();
        loadData();
        boolean IsHasShowBaiduMessage = PlayUtil.getSP().getBoolean(KeyUtil.IsHasShowBaiduMessage, false);
        if (!IsHasShowBaiduMessage) {
            initSample();
            Setings.saveSharedPreferences(PlayUtil.getSP(), KeyUtil.IsHasShowBaiduMessage, true);
        }
        mAdapter = new RcTranZhYueListAdapter(beans);
        recent_used_lv.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        recent_used_lv.setLayoutManager(mLinearLayoutManager);
        recent_used_lv.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));
        mAdapter.setItems(beans);
        mAdapter.setFooter(new Object());
        recent_used_lv.setAdapter(mAdapter);
        setListOnScrollListener();
    }

    public void setListOnScrollListener(){
        recent_used_lv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(!noMoreData){
                    int visible  = mLinearLayoutManager.getChildCount();
                    int total = mLinearLayoutManager.getItemCount();
                    int firstVisibleItem = mLinearLayoutManager.findFirstCompletelyVisibleItemPosition();
                    if ((visible + firstVisibleItem) >= total){
                        loadData();
                    }
                }
            }
        });
    }

    public void loadData(){
        List<TranResultZhYue> list = BoxHelper.getTranResultZhYueList(skip, Setings.RecordOffset);
        if (NullUtil.isNotEmpty(list)) {
            beans.addAll(list);
            skip += Setings.RecordOffset;
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }else {
            noMoreData = true;
        }
    }

    private void initSample() {
        TranResultZhYue sampleBean = new TranResultZhYue("点击咪讲嘢", "点击话筒说话","yue");
        BoxHelper.insert(sampleBean);
        beans.add(0, sampleBean);
    }


    private void translateController(){
        lastSearch = Setings.q;
        if(NetworkUtil.isNetworkConnected(getContext())){
            LogUtil.DefalutLog("online");
            try {
                RequestTask();
            } catch (Exception e) {
                LogUtil.DefalutLog("online exception");
                e.printStackTrace();
            }
        }else {
            showToast(getContext().getResources().getString(R.string.network_offline));
        }
    }

    /**
     * send translate request
     */
    private void RequestTask() throws Exception{
        showProgressbar();
        TranslateUtil.TranslateZhYue(new OnTranZhYueFinishListener() {
            @Override
            public void OnFinishTranslate(TranResultZhYue mTranResultZhYue) {
                hideProgressbar();
                if(mTranResultZhYue == null){
                    showToast(getContext().getResources().getString(R.string.network_error));
                }else {
                    currentDialogBean = mTranResultZhYue;
                    insertData();
                    autoClearAndautoPlay();
                }
            }
        });
    }

    public void insertData() {
        mAdapter.addEntity(0, currentDialogBean);
        recent_used_lv.scrollToPosition(0);
        long newRowId = BoxHelper.insert(currentDialogBean);
    }

    public void autoClearAndautoPlay() {
        EventBus.getDefault().post(new FinishEvent());
        if (PlayUtil.getSP().getBoolean(KeyUtil.AutoPlayResult, false)) {
            delayAutoPlay();
        }
    }

    private void autoPlay() {
        View mView = recent_used_lv.getChildAt(0);
        final FrameLayout record_answer_cover = (FrameLayout) mView.findViewById(R.id.record_answer_cover);
        record_answer_cover.post(new Runnable() {
            @Override
            public void run() {
                record_answer_cover.performClick();
            }
        });
    }

    /**
     * toast message
     * @param toastString
     */
    private void showToast(String toastString) {
        ToastUtil.diaplayMesShort(getContext(), toastString);
    }

    /**
     * submit request task
     */
    public void submit() {
        translateController();
    }

    public void refresh() {
        if (isNeedRefresh && mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            isNeedRefresh = false;
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(TranAndDicRefreshEvent event){
        reloadData();
        LogUtil.DefalutLog("---TranAndDicRefreshEvent---onEvent");
    }

    private void reloadData() {
        isNeedRefresh = true;
        beans.clear();
        beans.addAll(BoxHelper.getTranResultZhYueList(0, Setings.RecordOffset));
    }

    private void delayAutoPlay(){
        new Handler().postDelayed(() -> autoPlay(),100);
    }

}

