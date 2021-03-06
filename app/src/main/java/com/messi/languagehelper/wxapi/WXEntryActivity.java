package com.messi.languagehelper.wxapi;


import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.messi.languagehelper.BaseActivity;
import com.messi.languagehelper.LoadingActivity;
import com.messi.languagehelper.MoreActivity;
import com.messi.languagehelper.R;
import com.messi.languagehelper.adapter.MainPageAdapter;
import com.messi.languagehelper.databinding.ContentFrameBinding;
import com.messi.languagehelper.impl.FragmentProgressbarListener;
import com.messi.languagehelper.service.PlayerService;
import com.messi.languagehelper.util.AVAnalytics;
import com.messi.languagehelper.util.AppUpdateUtil;
import com.messi.languagehelper.util.KeyUtil;
import com.messi.languagehelper.util.LogUtil;
import com.messi.languagehelper.util.PlayUtil;
import com.messi.languagehelper.util.Setings;
import com.messi.languagehelper.util.SystemUtil;
import com.messi.languagehelper.util.ToastUtil;
import com.messi.languagehelper.util.TranslateHelper;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;

import java.util.Locale;

import cn.jzvd.Jzvd;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class WXEntryActivity extends BaseActivity implements FragmentProgressbarListener {

	private long leaveTime = 0;
	private long pressTime = 0;
	private SharedPreferences sp;
	private Intent playIntent;
	private ContentFrameBinding binding;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			binding = ContentFrameBinding.inflate(LayoutInflater.from(this));
			setContentView(binding.getRoot());
			initData();
			initViews();
			initSDKAndPermission();
			AppUpdateUtil.isNeedUpdate(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initData(){
		SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + getString(R.string.app_id));
		sp = getSharedPreferences(this.getPackageName(), Activity.MODE_PRIVATE);
		PlayUtil.initData(this, sp);
		SystemUtil.lan = Locale.getDefault().getLanguage();
		if (toolbar != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
			getSupportActionBar().setTitle("");
		}
		TranslateHelper.init(sp);
	}

	private void initViews() {
		final MainPageAdapter mAdapter = new MainPageAdapter(this.getSupportFragmentManager(),this,
				this);
		if(SystemUtil.lan.equals("en")){
			binding.tablayout.setTabMode(TabLayout.MODE_SCROLLABLE);
		}
		binding.pager.setAdapter(mAdapter);
		binding.pager.setOffscreenPageLimit(5);
		binding.tablayout.setupWithViewPager(binding.pager);
		binding.tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {
				if (mAdapter != null) {
					mAdapter.onTabReselected(tab.getPosition());
				}
			}
		});
		setLastTimeSelectTab();
	}

	private void initSDKAndPermission(){
		new Handler().postDelayed(
				() -> WXEntryActivityPermissionsDispatcher.showPermissionWithPermissionCheck(WXEntryActivity.this)
		, 1 * 1000);
	}

	//connect to the service
	private ServiceConnection musicConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			PlayerService.MusicBinder binder = (PlayerService.MusicBinder) service;
			PlayerService.musicSrv = binder.getService();
			LogUtil.DefalutLog("WXEntryActivity---musicConnection---:"+PlayerService.musicSrv);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};

	@Override
	public void onStart() {
		super.onStart();
		LogUtil.DefalutLog("WXEntryActivity---onStart");
		startMusicPlayerService();
		isNeedShowAD();
	}

	public void isNeedShowAD(){
		if (leaveTime > 10) {
			if ((System.currentTimeMillis() - leaveTime) > 1000*60*3) {
				leaveTime = 0;
				Intent intent = new Intent(this, LoadingActivity.class);
				startActivity(intent);
			}
		}
		leaveTime = 0;
	}

	private void startMusicPlayerService() {
		if (playIntent == null) {
			playIntent = new Intent(this, PlayerService.class);
			startService(playIntent);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
		}
	}

	private void setLastTimeSelectTab() {
		int index = sp.getInt(KeyUtil.LastTimeSelectTab, 0);
		binding.pager.setCurrentItem(index);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_more:
				toMoreActivity();
				break;
		}
		return true;
	}

	private void toMoreActivity() {
		Intent intent = new Intent(this, MoreActivity.class);
		startActivity(intent);
		AVAnalytics.onEvent(this, "index_pg_to_morepg");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_MENU:
				toMoreActivity();
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		if ((System.currentTimeMillis() - pressTime) > 2000) {
			Toast.makeText(getApplicationContext(), this.getResources().getString(R.string.exit_program), Toast.LENGTH_SHORT).show();
			pressTime = System.currentTimeMillis();
		} else {
			exitApp();
		}
	}

	public void exitApp(){
		if (Setings.MPlayerIsPlaying()) {
			exitLikeHome();
		} else {
			finish();
		}
	}

	public void exitLikeHome(){
		leaveTime = System.currentTimeMillis();
		Intent home = new Intent(Intent.ACTION_MAIN);
		home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		home.addCategory(Intent.CATEGORY_HOME);
		startActivity(home);
	}

	private void saveSelectTab() {
		int index = binding.pager.getCurrentItem();
		LogUtil.DefalutLog("WXEntryActivity---onDestroy---saveSelectTab---index:" + index);
		Setings.saveSharedPreferences(sp, KeyUtil.LastTimeSelectTab, index);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
		LogUtil.DefalutLog("WXEntryActivity---onSaveInstanceState");
	}

	@Override
	protected void onStop() {
		super.onStop();
		LogUtil.DefalutLog("WXEntryActivity---onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			saveSelectTab();
			Jzvd.releaseAllVideos();
			PlayUtil.onDestroy();
			unbindService(musicConnection);
			isBackgroundPlay();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void isBackgroundPlay(){
		if(XmPlayerManager.getInstance(this).isPlaying() || Setings.MPlayerIsPlaying()){
			LogUtil.DefalutLog("xmly or myplayer is playing.");
		}else {
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(Setings.NOTIFY_ID);
			if (playIntent != null) {
				stopService(playIntent);
			}
			XmPlayerManager.getInstance(this).release();
			PlayerService.musicSrv = null;
		}
	}

	@NeedsPermission({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION})
	void showPermission() {
		LogUtil.DefalutLog("showPermission");
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		WXEntryActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
	}

	@OnShowRationale({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION})
	void onShowRationale(final PermissionRequest request) {
		new AlertDialog.Builder(this,R.style.Theme_AppCompat_Light_Dialog_Alert)
				.setTitle("温馨提示")
				.setMessage("需要授权才能使用。")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						request.proceed();
					}
				}).show();
	}

	@OnPermissionDenied({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION})
	void onPerDenied() {
		ToastUtil.diaplayMesShort(this,"没有授权，部分功能将无法使用！");
	}
}
