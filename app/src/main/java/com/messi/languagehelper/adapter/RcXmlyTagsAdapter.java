package com.messi.languagehelper.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.karumi.headerrecyclerview.HeaderRecyclerViewAdapter;
import com.messi.languagehelper.R;
import com.messi.languagehelper.impl.AdapterStringListener;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.tag.Tag;

import java.util.List;

/**
 * Created by luli on 10/23/16.
 */

public class RcXmlyTagsAdapter extends HeaderRecyclerViewAdapter<RecyclerView.ViewHolder, Object, Album, Object> {

    private List<Tag> list;
    private AdapterStringListener listener;

    public RcXmlyTagsAdapter(List<Tag> list,AdapterStringListener listener){
        this.list = list;
        this.listener = listener;
    }

    public RcXmlyTagsAdapter(){
    }

    @Override
    protected RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = getLayoutInflater(parent);
        View headerView = inflater.inflate(R.layout.xmly_tags_list_header, parent, false);
        return new RcXmlyTagsHeaderViewHolder(headerView,listener);
    }

    @Override
    protected void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindHeaderViewHolder(holder, position);
        RcXmlyTagsHeaderViewHolder headerViewHolder = (RcXmlyTagsHeaderViewHolder)holder;
        headerViewHolder.setData(list);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = getLayoutInflater(parent);
        View characterView = inflater.inflate(R.layout.ximalaya_list_item, parent, false);
        return new RcXmlyTagsItemViewHolder(characterView);
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        Album mAVObject = getItem(position);
        RcXmlyTagsItemViewHolder itemViewHolder = (RcXmlyTagsItemViewHolder)holder;
        itemViewHolder.render(mAVObject);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = getLayoutInflater(parent);
        View footerView = inflater.inflate(R.layout.footerview, parent, false);
        return new RcLmFooterViewHolder(footerView);
    }

    @Override
    protected void onBindFooterViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindFooterViewHolder(holder, position);
    }

    private LayoutInflater getLayoutInflater(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext());
    }
}