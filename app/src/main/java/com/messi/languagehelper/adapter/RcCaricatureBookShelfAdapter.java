package com.messi.languagehelper.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.messi.languagehelper.R;
import com.messi.languagehelper.box.CNWBean;
import com.messi.languagehelper.util.HeaderFooterRecyclerViewAdapter;


/**
 * Created by luli on 10/23/16.
 */

public class RcCaricatureBookShelfAdapter extends HeaderFooterRecyclerViewAdapter<RecyclerView.ViewHolder, Object, CNWBean, Object> {

    @Override
    protected RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = getLayoutInflater(parent);
        View characterView = inflater.inflate(R.layout.caricature_bookshelf_list_item, parent, false);
        return new RcCaricatureBookshelfItemViewHolder(characterView);
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        CNWBean mAVObject = getItem(position);
        RcCaricatureBookshelfItemViewHolder itemViewHolder = (RcCaricatureBookshelfItemViewHolder)holder;
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
