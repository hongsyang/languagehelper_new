package com.messi.languagehelper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.messi.languagehelper.adapter.StudyDialogCategoryAdapter;
import com.messi.languagehelper.util.AVOUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class StudyDialogCategoryActivity extends BaseActivity implements OnClickListener{

	
	private ListView category_lv;
	private List<AVObject> avObjects;
	private StudyDialogCategoryAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invest_list_activity);
		initSwipeRefresh();
		initViews();
		new QueryTask(this).execute();
	}
	
	private void initViews(){
		getSupportActionBar().setTitle(getResources().getString(R.string.simulation_dialogue));
		avObjects = new ArrayList<AVObject>();
		category_lv = (ListView) findViewById(R.id.studycategory_lv);
		mAdapter = new StudyDialogCategoryAdapter(this, avObjects);
		category_lv.setAdapter(mAdapter);
	}

	@Override
	public void onSwipeRefreshLayoutRefresh() {
		super.onSwipeRefreshLayoutRefresh();
		new QueryTask(this).execute();
	}
	
	private class QueryTask extends AsyncTask<Void, Void, Void> {

		private WeakReference<StudyDialogCategoryActivity> mainActivity;

		public QueryTask(StudyDialogCategoryActivity mActivity){
			mainActivity = new WeakReference<>(mActivity);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgressbar();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			AVQuery<AVObject> query = new AVQuery<AVObject>(AVOUtil.StudyDialogCategory.StudyDialogCategory);
			query.whereEqualTo(AVOUtil.StudyDialogCategory.SDIsValid, "1");
			query.orderByDescending(AVOUtil.StudyDialogCategory.SDOrder);
			try {
				List<AVObject> avObject  = query.find();
				if(avObject != null){
					avObjects.clear();
					avObjects.addAll(avObject);
				}
			} catch (AVException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if(mainActivity.get() != null){
				hideProgressbar();
				onSwipeRefreshLayoutFinish();
				mAdapter.notifyDataSetChanged();
			}
		}
		
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		default:
			break;
		}
	}
	
}
