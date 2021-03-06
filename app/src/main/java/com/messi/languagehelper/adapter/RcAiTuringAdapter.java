package com.messi.languagehelper.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.karumi.headerrecyclerview.HeaderRecyclerViewAdapter;
import com.messi.languagehelper.AiTuringActivity;
import com.messi.languagehelper.R;
import com.messi.languagehelper.dao.AiEntity;

import java.util.List;

public class RcAiTuringAdapter extends HeaderRecyclerViewAdapter<RecyclerView.ViewHolder, Object, AiEntity, Object> {

	private List<AiEntity> beans;
	private ProgressBar mProgressbar;
	private AiTuringActivity mAiChatActivity;

	public RcAiTuringAdapter(AiTuringActivity mAiChatActivity, List<AiEntity> beans, ProgressBar mProgressbar){
		this.mAiChatActivity = mAiChatActivity;
		this.beans = beans;
		this.mProgressbar = mProgressbar;
	}

	@Override
	protected RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = getLayoutInflater(parent);
		View characterView = inflater.inflate(R.layout.ai_chat_list_item, parent, false);
		return new RcAiTuringItemViewHolder(characterView,beans,this,mProgressbar,mAiChatActivity);
	}

	@Override
	protected void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
		AiEntity mAVObject = getItem(position);
		RcAiTuringItemViewHolder mItemViewHolder = (RcAiTuringItemViewHolder)holder;
		mItemViewHolder.render(mAVObject);
	}

	private LayoutInflater getLayoutInflater(ViewGroup parent) {
		return LayoutInflater.from(parent.getContext());
	}

	public void addEntity(int position, AiEntity entity) {
		beans.add(position, entity);
		notifyItemInserted(position);
	}
}
