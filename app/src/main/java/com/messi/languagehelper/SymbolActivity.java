package com.messi.languagehelper;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;

import com.messi.languagehelper.adapter.SpokenEnglishTabAdapter;
import com.messi.languagehelper.adapter.SymbolAdapter;
import com.messi.languagehelper.impl.FragmentProgressbarListener;
import com.messi.languagehelper.util.DownLoadUtil;

public class SymbolActivity extends BaseActivity implements FragmentProgressbarListener{

	private TabLayout tablayout;
	private ViewPager viewpager;
	private SymbolAdapter pageAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.joke_activity);
		initViews();
	}
	
	private void initViews(){
		getSupportActionBar().setTitle(getResources().getString(R.string.title_symbol));
		tablayout = (TabLayout) findViewById(R.id.tablayout);
		viewpager = (ViewPager) findViewById(R.id.viewpager);
		
		pageAdapter = new SymbolAdapter(getSupportFragmentManager(),this);
		viewpager.setAdapter(pageAdapter);
		viewpager.setOffscreenPageLimit(2);
		tablayout.setupWithViewPager(viewpager);
	}
}
