package com.messi.languagehelper.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.messi.languagehelper.R;
import com.messi.languagehelper.SubjectByTypeFragment;
import com.messi.languagehelper.SubjectFragment;
import com.messi.languagehelper.util.AVOUtil;
import com.messi.languagehelper.util.KeyUtil;

import java.util.ArrayList;

public class ListenSubjectAdapter extends FragmentPagerAdapter {

    private ArrayList<String> titleList = new ArrayList<String>();

    public ListenSubjectAdapter(FragmentManager fm, Context mContext) {
        super(fm);
        titleList.add(mContext.getResources().getString(R.string.beginner));
        titleList.add(mContext.getResources().getString(R.string.intermediate));
        titleList.add(mContext.getResources().getString(R.string.advanced));
        titleList.add(mContext.getResources().getString(R.string.title_broadcast));
    }

    @Override
    public Fragment getItem(int position) {
        if( position == 0 ){
            return SubjectFragment.getInstance(AVOUtil.Category.listening,"1");
        }else if( position == 1 ){
            return SubjectFragment.getInstance(AVOUtil.Category.listening,"2");
        }else if( position == 2 ){
            return SubjectFragment.getInstance(AVOUtil.Category.listening,"3");
        }else if( position == 3 ){
            return SubjectByTypeFragment.getInstance(AVOUtil.Category.broadcast,KeyUtil.RecentBroadcast);
        }
        return null;
    }

    @Override
    public int getCount() {
        return titleList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position).toUpperCase();
    }
}