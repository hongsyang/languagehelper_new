package com.messi.languagehelper;

import java.util.ArrayList;
import java.util.List;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.karumi.headerrecyclerview.HeaderSpanSizeLookup;
import com.messi.languagehelper.adapter.RcSymbolListAdapter;
import com.messi.languagehelper.adapter.RcWordListAdapter;
import com.messi.languagehelper.adapter.WordStudyListAdapter;
import com.messi.languagehelper.impl.FragmentProgressbarListener;
import com.messi.languagehelper.util.ADUtil;
import com.messi.languagehelper.util.AVOUtil;
import com.messi.languagehelper.util.KeyUtil;
import com.messi.languagehelper.util.LogUtil;
import com.messi.languagehelper.util.SaveData;
import com.messi.languagehelper.util.Settings;
import com.messi.languagehelper.util.TimeUtil;
import com.messi.languagehelper.util.XFYSAD;
import com.messi.languagehelper.views.DividerGridItemDecoration;
import com.messi.languagehelper.views.GridViewWithHeaderAndFooter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WordStudyListFragment extends BaseFragment{

	private static final int NUMBER_OF_COLUMNS = 2;
	private RecyclerView category_lv;
	private RcWordListAdapter mAdapter;
	private List<AVObject> avObjects;
	private XFYSAD mXFYSAD;
	private SharedPreferences spf;
	private boolean isNeedSaveData;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mProgressbarListener = (FragmentProgressbarListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement FragmentProgressbarListener");
        }
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.word_study_list_fragment, container, false);
		initSwipeRefresh(view);
		initViews(view);
		initData();
		return view;
	}
	
	private void initViews(View view){
		spf = Settings.getSharedPreferences(getContext());
		avObjects = new ArrayList<AVObject>();
		category_lv = (RecyclerView) view.findViewById(R.id.listview);
		mXFYSAD = new XFYSAD(getContext(), ADUtil.SecondaryPage);
		mAdapter = new RcWordListAdapter(mXFYSAD);
		category_lv.setHasFixedSize(true);
		GridLayoutManager layoutManager = new GridLayoutManager(getContext(), NUMBER_OF_COLUMNS);
		HeaderSpanSizeLookup headerSpanSizeLookup = new HeaderSpanSizeLookup(mAdapter, layoutManager);
		layoutManager.setSpanSizeLookup(headerSpanSizeLookup);
		category_lv.setLayoutManager(layoutManager);
		category_lv.addItemDecoration(new DividerGridItemDecoration(1));
		mAdapter.setHeader(new Object());
		category_lv.setAdapter(mAdapter);
	}
	
	private void initData(){
		try {
			long lastTimeSave = spf.getLong(KeyUtil.SaveLastTime_WordStudyCategoryList, 0);
			if(System.currentTimeMillis() - lastTimeSave > 1000*60*60*24*10){
				SaveData.deleteObject(getActivity(), "WordStudyCategoryList");
				LogUtil.DefalutLog("deleteObject   WordStudyCategoryList");
				new QueryTask().execute();
			}else{
				List<String> listStr =  (ArrayList<String>) SaveData.getObject(getActivity(), "WordStudyCategoryList");
				if(listStr == null || listStr.size() == 0){
					LogUtil.DefalutLog("avObjects is null");
					new QueryTask().execute();
				}else{
					LogUtil.DefalutLog("avObjects is not null");
					for(String str : listStr){
						avObjects.add(AVObject.parseAVObject(str));
					}
					mAdapter.setItems(avObjects);
					mAdapter.notifyDataSetChanged();
				}
			}
		} catch (Exception e) {
			new QueryTask().execute();
			e.printStackTrace();
		}
	}
	
	@Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
        	if(mXFYSAD != null){
        		mXFYSAD.startPlayImg();
        	}
        }else{
        	if(mXFYSAD != null){
        		mXFYSAD.canclePlayImg();
        	}
        }
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(mXFYSAD != null){
    		mXFYSAD.startPlayImg();
    	}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if(mXFYSAD != null){
    		mXFYSAD.canclePlayImg();
    	}
	}
	
	@Override
	public void onSwipeRefreshLayoutRefresh() {
		new QueryTask().execute();
	}
	
	private class QueryTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgressbar();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				AVQuery<AVObject> query = new AVQuery<AVObject>(AVOUtil.WordCategory.WordCategory);
				query.whereEqualTo(AVOUtil.WordCategory.isvalid, "1");
				query.orderByAscending(AVOUtil.WordCategory.order);
				List<AVObject> mAVObjects  = query.find();
				if(avObjects != null){
					avObjects.clear();
					avObjects.addAll(mAVObjects);
					isNeedSaveData = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			hideProgressbar();
			onSwipeRefreshLayoutFinish();
			mAdapter.setItems(avObjects);
			mAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(avObjects != null && isNeedSaveData){
			List<String> listStr = new ArrayList<String>();
			for(AVObject item : avObjects){
				listStr.add(item.toString());
			}
			SaveData.saveObject(getActivity(), "WordStudyCategoryList", listStr);
			Settings.saveSharedPreferences(spf, KeyUtil.SaveLastTime_WordStudyCategoryList, 
					System.currentTimeMillis());
			LogUtil.DefalutLog("saveObject   WordStudyCategoryList");
		}
		if(mXFYSAD != null){
    		mXFYSAD.canclePlayImg();
    		mXFYSAD = null;
    	}
	}
	
}
