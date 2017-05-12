package com.messi.languagehelper;

import java.util.List;

import com.messi.languagehelper.adapter.WordBookListAdapter;
import com.messi.languagehelper.bean.WordListItem;
import com.messi.languagehelper.util.ADUtil;
import com.messi.languagehelper.util.KeyUtil;
import com.messi.languagehelper.util.XFYSAD;
import com.messi.languagehelper.wxapi.WXEntryActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

public class WordStudyThirdActivity extends BaseActivity {

	private ListView category_lv;
	private WordBookListAdapter mAdapter;
	private List<WordListItem> child_list;
	private XFYSAD mXFYSAD;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invest_list_activity);
		initSwipeRefresh();
		initViews();
	}
	
	private void initViews(){
		child_list = (List<WordListItem>) WXEntryActivity.dataMap.get(KeyUtil.DataMapKey);
		WXEntryActivity.dataMap.clear();
		if(child_list != null){
			category_lv = (ListView) findViewById(R.id.studycategory_lv);
			View headerView = LayoutInflater.from(this).inflate(R.layout.xunfei_ysad_item, null);
			category_lv.addHeaderView(headerView);
			mAdapter = new WordBookListAdapter(this, child_list);
			category_lv.setAdapter(mAdapter);
			
			mXFYSAD = new XFYSAD(this, headerView, ADUtil.SecondaryPage);
			mXFYSAD.showAD();
			mAdapter.notifyDataSetChanged();
		}
	}
	
	public void onSwipeRefreshLayoutRefresh() {
		super.onSwipeRefreshLayoutRefresh();
		mAdapter.notifyDataSetChanged();
		onSwipeRefreshLayoutFinish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mXFYSAD != null){
    		mXFYSAD.canclePlayImg();
    		mXFYSAD = null;
    	}
	}

	
}