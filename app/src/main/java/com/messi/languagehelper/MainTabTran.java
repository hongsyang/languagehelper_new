package com.messi.languagehelper;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.messi.languagehelper.adapter.RcTranslateListAdapter;
import com.messi.languagehelper.dao.record;
import com.messi.languagehelper.db.DataBaseUtil;
import com.messi.languagehelper.event.FinishEvent;
import com.messi.languagehelper.impl.FragmentProgressbarListener;
import com.messi.languagehelper.impl.OnTranslateFinishListener;
import com.messi.languagehelper.util.KeyUtil;
import com.messi.languagehelper.util.LogUtil;
import com.messi.languagehelper.util.NetworkUtil;
import com.messi.languagehelper.util.PlayUtil;
import com.messi.languagehelper.util.Setings;
import com.messi.languagehelper.util.ToastUtil;
import com.messi.languagehelper.util.TranslateUtil;
import com.messi.languagehelper.views.DividerItemDecoration;
import com.youdao.sdk.ydtranslate.Translate;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainTabTran extends BaseFragment {


    private RecyclerView recent_used_lv;
    private record currentDialogBean;
    private RcTranslateListAdapter mAdapter;
    private List<record> beans;
    private String lastSearch;

    public static MainTabTran getInstance(FragmentProgressbarListener listener) {
        MainTabTran mMainFragment = new MainTabTran();
        mMainFragment.setmProgressbarListener(listener);
        return mMainFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_tab_tran, null);
        LogUtil.DefalutLog("MainTabTran-onCreateView");
        init(view);
        return view;
    }

    private void init(View view) {
        recent_used_lv = (RecyclerView) view.findViewById(R.id.recent_used_lv);
        beans = new ArrayList<record>();
        beans.addAll(DataBaseUtil.getInstance().getDataListRecord(0, Setings.offset));
        boolean IsHasShowBaiduMessage = PlayUtil.getSP().getBoolean(KeyUtil.IsHasShowBaiduMessage, false);
        if (!IsHasShowBaiduMessage) {
            initSample();
            Setings.saveSharedPreferences(PlayUtil.getSP(), KeyUtil.IsHasShowBaiduMessage, true);
        }
        mAdapter = new RcTranslateListAdapter(beans);
        recent_used_lv.setHasFixedSize(true);
        recent_used_lv.setLayoutManager(new LinearLayoutManager(getContext()));
        recent_used_lv.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));
        mAdapter.setItems(beans);
        mAdapter.setFooter(new Object());
        recent_used_lv.setAdapter(mAdapter);
    }

    private void initSample() {
        record sampleBean = new record("Click the mic to speak", "点击话筒说话");
        DataBaseUtil.getInstance().insert(sampleBean);
        beans.add(0, sampleBean);
    }

    public void refresh() {
        if (Setings.isMainFragmentNeedRefresh) {
            Setings.isMainFragmentNeedRefresh = false;
            reloadData();
        }

    }

    private void translateController(){
        lastSearch = Setings.q;
        if(NetworkUtil.isNetworkConnected(getContext())){
            LogUtil.DefalutLog("online");
            try {
                RequestJinShanNewAsyncTask();
            } catch (Exception e) {
                LogUtil.DefalutLog("online exception");
                e.printStackTrace();
            }
        }else {
            LogUtil.DefalutLog("offline");
            translateOffline();
        }
    }

    private void translateOffline(){
        showProgressbar();
        Observable.create(new ObservableOnSubscribe<Translate>() {
            @Override
            public void subscribe(ObservableEmitter<Translate> e) throws Exception {
                TranslateUtil.offlineTranslate(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Translate>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }
                    @Override
                    public void onNext(Translate translate) {
                        parseOfflineData(translate);
                    }
                    @Override
                    public void onError(Throwable e) {
                        onComplete();
                    }
                    @Override
                    public void onComplete() {
                        hideProgressbar();
                    }
                });
    }

    private void parseOfflineData(Translate translate){
        if(translate != null){
            if(translate.getErrorCode() == 0){
                StringBuilder sb = new StringBuilder();
                TranslateUtil.addSymbol(translate,sb);
                for(String tran : translate.getTranslations()){
                    sb.append(tran);
                    sb.append("\n");
                }
                currentDialogBean = new record(sb.substring(0, sb.lastIndexOf("\n")), Setings.q);
                insertData();
                autoClearAndautoPlay();
            }
        }else{
            showToast("没找到离线词典，请到更多页面下载！");
        }
    }
    /**
     * send translate request
     */
    private void RequestJinShanNewAsyncTask() throws Exception{
        showProgressbar();
        TranslateUtil.Translate(new OnTranslateFinishListener() {
            @Override
            public void OnFinishTranslate(record mRecord) {
                try {
                    hideProgressbar();
                    if(mRecord == null){
                        showToast(getContext().getResources().getString(R.string.network_error));
                    }else {
                        currentDialogBean = mRecord;
                        insertData();
                        autoClearAndautoPlay();
                    }
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void insertData() {
        mAdapter.addEntity(0, currentDialogBean);
        recent_used_lv.scrollToPosition(0);
        long newRowId = DataBaseUtil.getInstance().insert(currentDialogBean);
    }

    public void autoClearAndautoPlay() {
        EventBus.getDefault().post(new FinishEvent());
        if (PlayUtil.getSP().getBoolean(KeyUtil.AutoPlayResult, false)) {
            new AutoPlayWaitTask().execute();
        }
    }

    private void autoPlay() {
        try {
            View mView = recent_used_lv.getChildAt(0);
            final FrameLayout record_answer_cover = (FrameLayout) mView.findViewById(R.id.record_answer_cover);
            if(record_answer_cover != null){
                record_answer_cover.post(new Runnable() {
                    @Override
                    public void run() {
                        record_answer_cover.performClick();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void reloadData() {
        showProgressbar();
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                beans.clear();
                beans.addAll(DataBaseUtil.getInstance().getDataListRecord(0, Setings.offset));
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
                        hideProgressbar();
                        mAdapter.notifyDataSetChanged();
                    }
                });

    }

    class AutoPlayWaitTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            autoPlay();
        }
    }


}

