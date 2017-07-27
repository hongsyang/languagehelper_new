package com.messi.languagehelper;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.iflytek.cloud.thirdparty.S;
import com.karumi.headerrecyclerview.HeaderSpanSizeLookup;
import com.messi.languagehelper.adapter.RcSearchHistoryAdapter;
import com.messi.languagehelper.impl.AdapterStringListener;
import com.messi.languagehelper.util.AVOUtil;
import com.messi.languagehelper.util.KeyUtil;
import com.messi.languagehelper.util.LogUtil;
import com.messi.languagehelper.util.Settings;
import com.messi.languagehelper.util.ToastUtil;
import com.messi.languagehelper.views.DividerGridItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends BaseActivity implements AdapterStringListener {

    private static final int NUMBER_OF_COLUMNS = 2;
    @BindView(R.id.search_et)
    EditText searchEt;
    @BindView(R.id.search_btn)
    FrameLayout searchBtn;
    @BindView(R.id.studycategory_lv)
    RecyclerView category_lv;
    @BindView(R.id.clear_history)
    FrameLayout clearHistory;
    private RcSearchHistoryAdapter mAdapter;
    private long lastTime;
    private ArrayList<AVObject> historyList;
    private ArrayList<AVObject> avObjects;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        init();
        new QueryTask().execute();
    }

    private void init() {
        hideTitle();
        searchEt.requestFocus();
        historyList = new ArrayList<AVObject>();
        avObjects = new ArrayList<AVObject>();
        addHistory();
        mAdapter = new RcSearchHistoryAdapter(this);
        mAdapter.setItems(avObjects);
        GridLayoutManager layoutManager = new GridLayoutManager(this, NUMBER_OF_COLUMNS);
        HeaderSpanSizeLookup headerSpanSizeLookup = new HeaderSpanSizeLookup(mAdapter, layoutManager);
        layoutManager.setSpanSizeLookup(headerSpanSizeLookup);
        category_lv.setLayoutManager(layoutManager);
        category_lv.addItemDecoration(new DividerGridItemDecoration(2));
        category_lv.setAdapter(mAdapter);

        searchEt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_ENTER) {
                    if (System.currentTimeMillis() - lastTime > 1000) {
                        search(searchEt.getText().toString());
                    }
                    lastTime = System.currentTimeMillis();
                }
                return false;
            }
        });
    }

    private void addHistory(){
        historyList.clear();
        String history_str = Settings.getSharedPreferences(this).getString(KeyUtil.SearchHistory,"");
        if (!TextUtils.isEmpty(history_str)) {
            String[] hiss = history_str.split(",");
            if(hiss.length > 0){
                for (int i=0; i<6; i++){
                    if(i < hiss.length) {
                        if(!TextUtils.isEmpty(hiss[i])){
                            AVObject avobj = new AVObject();
                            avobj.put(AVOUtil.SearchHot.name, hiss[i]);
                            historyList.add(avobj);
                        }
                    }
                }
                avObjects.addAll(0,historyList);
            }
        }
    }

    @Override
    public void OnItemClick(String item) {
        search(item);
    }

    private class QueryTask extends AsyncTask<Void, Void,  List<AVObject>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<AVObject> doInBackground(Void... params) {
            AVQuery<AVObject> query = new AVQuery<AVObject>(AVOUtil.SearchHot.SearchHot);
            query.orderByAscending(AVOUtil.SearchHot.createdAt);
            query.orderByDescending(AVOUtil.SearchHot.click_time);
            query.limit(20);
            try {
                return query.find();
            } catch (AVException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<AVObject> avObject) {
            super.onPostExecute(avObject);
            if(avObject != null){
                if(avObject.size() != 0){
                    if(avObjects != null && mAdapter != null){
                        for(AVObject obj : avObject){
                            boolean isAdd = true;
                            for (AVObject history : historyList){
                                if(history.getString(AVOUtil.SearchHot.name).equals(
                                        obj.getString(AVOUtil.SearchHot.name))){
                                    isAdd = false;
                                    break;
                                }
                            }
                            if (isAdd) {
                                avObjects.add(obj);
                            }
                        }

                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @OnClick({R.id.search_btn, R.id.clear_history})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.search_btn:
                search(searchEt.getText().toString());
                break;
            case R.id.clear_history:
                clearHistory();
                break;
        }
    }

    private void clearHistory(){
        Settings.saveSharedPreferences(Settings.getSharedPreferences(this),
                KeyUtil.SearchHistory,
                "");
        historyList.clear();
        avObjects.clear();
        mAdapter.notifyDataSetChanged();
        new QueryTask().execute();
    }

    private void search(String quest) {
        if (!TextUtils.isEmpty(quest)) {
            Intent intent = new Intent(this, SearchResultActivity.class);
            intent.putExtra(KeyUtil.ActionbarTitle, quest);
            intent.putExtra(KeyUtil.SearchKey, quest);
            startActivity(intent);
            saveHistory(quest);
        }
    }

    private void saveHistory(String quest){
        StringBuilder sb = new StringBuilder();
        sb.append(quest);
        String history_str = Settings.getSharedPreferences(this).getString(KeyUtil.SearchHistory,"");
        if (!TextUtils.isEmpty(history_str)) {
            String[] hiss = history_str.split(",");
            if(hiss.length > 0){
                for (int i=0; i<6; i++){
                    if(i < hiss.length){
                        if(!quest.equals(hiss[i]) && !TextUtils.isEmpty(hiss[i])){
                            sb.append(",");
                            sb.append(hiss[i]);
                        }
                    }
                }
            }
        }
        Settings.saveSharedPreferences(Settings.getSharedPreferences(this),
                KeyUtil.SearchHistory,
                sb.toString());
        saveHistoryToServer(quest);
    }

    private void saveHistoryToServer(final String quest){
        new Thread(new Runnable() {
            @Override
            public void run() {
                checkAndSaveData(quest);
            }
        }).start();
    }

    private void checkAndSaveData(final String quest){
        AVQuery<AVObject> query = new AVQuery<>(AVOUtil.SearchHot.SearchHot);
        query.whereEqualTo(AVOUtil.SearchHot.name, quest);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (list.size() > 0) {
                    AVObject mAVObject = list.get(0);
                    int times = mAVObject.getInt(AVOUtil.SearchHot.click_time);
                    mAVObject.put(AVOUtil.SearchHot.click_time,times+1);
                    mAVObject.saveInBackground();
                } else {
                    AVObject object = new AVObject(AVOUtil.SearchHot.SearchHot);
                    object.put(AVOUtil.SearchHot.name, quest);
                    object.saveInBackground();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideIME();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideIME();
    }

    /**
     * 点击翻译之后隐藏输入法
     */
    private void hideIME() {
        final InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEt.getWindowToken(), 0);
    }

    /**
     * 点击编辑之后显示输入法
     */
    private void showIME() {
        final InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
    }



}
