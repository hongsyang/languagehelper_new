package com.messi.languagehelper;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.messi.languagehelper.adapter.SymbolAdapter;
import com.messi.languagehelper.impl.FragmentProgressbarListener;

public class SymbolActivity extends BaseActivity implements FragmentProgressbarListener {

    private TabLayout tablayout;
    private ViewPager viewpager;
    private SymbolAdapter pageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joke_activity);
        initViews();
    }

    private void initViews() {
        getSupportActionBar().setTitle(getResources().getString(R.string.title_symbol));
        tablayout = (TabLayout) findViewById(R.id.tablayout);
        viewpager = (ViewPager) findViewById(R.id.viewpager);

        pageAdapter = new SymbolAdapter(getSupportFragmentManager(), this);
        viewpager.setAdapter(pageAdapter);
        viewpager.setOffscreenPageLimit(4);
        tablayout.setupWithViewPager(viewpager);
    }
}
