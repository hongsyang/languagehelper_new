package com.messi.languagehelper.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.messi.languagehelper.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.sdkdownloader.XmDownloadManager;

import java.util.List;

public class AppUpdateUtil {

    public static void runCheckUpdateTask(final Activity mActivity) {
        checkUpdate(mActivity);
        initXMLY(mActivity);
    }

    public static void initXMLY(Activity mActivity){
        XmPlayerManager.getInstance(mActivity).init();
        XmPlayerManager.getInstance(mActivity).setCommonBusinessHandle(XmDownloadManager.getInstance());
//		XmPlayerManager.getInstance(this).clearAllLocalHistory();

        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        SystemUtil.SCREEN_WIDTH = dm.widthPixels;
        SystemUtil.SCREEN_HEIGHT = dm.heightPixels;
        SystemUtil.screen = SystemUtil.SCREEN_WIDTH + "x" + SystemUtil.SCREEN_HEIGHT;
    }

    public static void checkUpdate(final Activity mActivity) {
        AVQuery<AVObject> query = new AVQuery<AVObject>(AVOUtil.UpdateInfo.UpdateInfo);
        if(mActivity.getPackageName().equals(Setings.application_id_zyhy)){
            query.whereEqualTo(AVOUtil.UpdateInfo.AppCode, "zyhy");
        }else if(mActivity.getPackageName().equals(Setings.application_id_zyhy_google)){
            query.whereEqualTo(AVOUtil.UpdateInfo.AppCode, "zyhy_google");
        }else if(mActivity.getPackageName().equals(Setings.application_id_yys)){
            query.whereEqualTo(AVOUtil.UpdateInfo.AppCode, "yys");
        }else if(mActivity.getPackageName().equals(Setings.application_id_yys_google)){
            query.whereEqualTo(AVOUtil.UpdateInfo.AppCode, "yys_google");
        }else if(mActivity.getPackageName().equals(Setings.application_id_yyj)){
            query.whereEqualTo(AVOUtil.UpdateInfo.AppCode, "yyj");
        }else if(mActivity.getPackageName().equals(Setings.application_id_yyj_google)){
            query.whereEqualTo(AVOUtil.UpdateInfo.AppCode, "yyj_google");
        }else if(mActivity.getPackageName().equals(Setings.application_id_yycd)){
            query.whereEqualTo(AVOUtil.UpdateInfo.AppCode, "yycd");
        }else if(mActivity.getPackageName().equals(Setings.application_id_xbky)){
            query.whereEqualTo(AVOUtil.UpdateInfo.AppCode, "xbky");
        }else{
            query.whereEqualTo(AVOUtil.UpdateInfo.AppCode, "noupdate");
        }
        query.findInBackground(new FindCallback<AVObject>() {
            public void done(List<AVObject> avObjects, AVException e) {
                if (avObjects != null && avObjects.size() > 0) {
                    final AVObject mAVObject = avObjects.get(0);
                    saveSetting(mActivity,mAVObject);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showUpdateDialog(mActivity,mAVObject);
                        }
                    }, 3700);
                }
            }
        });
    }

    public static void saveSetting(Activity mActivity,AVObject mAVObject){
        SharedPreferences mSharedPreferences = Setings.getSharedPreferences(mActivity);
        LogUtil.DefalutLog(mAVObject.getString(AVOUtil.UpdateInfo.AppName));
        String app_advertiser = mAVObject.getString(AVOUtil.UpdateInfo.ad_type);
        String uctt_url = mAVObject.getString(AVOUtil.UpdateInfo.uctt_url);
        String ucsearch_url = mAVObject.getString(AVOUtil.UpdateInfo.ucsearch_url);
        String ad_ids = mAVObject.getString(AVOUtil.UpdateInfo.ad_ids);
        String no_ad_channel = mAVObject.getString(AVOUtil.UpdateInfo.no_ad_channel);
        Setings.saveSharedPreferences(mSharedPreferences,KeyUtil.APP_Advertiser,app_advertiser);
        Setings.saveSharedPreferences(mSharedPreferences,KeyUtil.Lei_UCTT,uctt_url);
        Setings.saveSharedPreferences(mSharedPreferences,KeyUtil.Lei_UCSearch,ucsearch_url);
        Setings.saveSharedPreferences(mSharedPreferences,KeyUtil.Ad_Ids,ad_ids);
        Setings.saveSharedPreferences(mSharedPreferences,KeyUtil.No_Ad_Channel,no_ad_channel);
        Setings.saveSharedPreferences(mSharedPreferences,KeyUtil.VersionCode,
                mAVObject.getInt(AVOUtil.UpdateInfo.VersionCode));

    }

    public static void showUpdateDialog(final Activity mActivity,final AVObject mAVObject) {
        String isValid = mAVObject.getString(AVOUtil.UpdateInfo.IsValid);
        if(!TextUtils.isEmpty(isValid) && isValid.equals("3")){
            int newVersionCode = mAVObject.getInt(AVOUtil.UpdateInfo.VersionCode);
            int oldVersionCode = Setings.getVersion(mActivity);
            if (newVersionCode > oldVersionCode) {
                String updateInfo = mAVObject.getString(AVOUtil.UpdateInfo.AppUpdateInfo);
                String downloadType = mAVObject.getString(AVOUtil.UpdateInfo.DownloadType);
                String apkUrl = "";
                if (downloadType.equals("apk")) {
                    AVFile avFile = mAVObject.getAVFile(AVOUtil.UpdateInfo.Apk);
                    apkUrl = avFile.getUrl();
                } else {
                    apkUrl = mAVObject.getString(AVOUtil.UpdateInfo.APPUrl);
                }
                final String downloadUrl = apkUrl;
                LogUtil.DefalutLog("apkUrl:" + apkUrl);

                final DialogPlus dialog = DialogPlus.newDialog(mActivity)
                        .setContentHolder(new ViewHolder(R.layout.dialog_update_info))
                        .setCancelable(false)
                        .setGravity(Gravity.BOTTOM)
                        .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                        .setOverlayBackgroundResource(R.color.none_alpha)
                        .create();
                View view = dialog.getHolderView();
                TextView updage_info = (TextView) view.findViewById(R.id.updage_info);
                TextView cancel_btn = (TextView) view.findViewById(R.id.cancel_btn);
                TextView update_btn = (TextView) view.findViewById(R.id.update_btn);

                updage_info.setText(updateInfo);
                cancel_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                update_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        new AppDownloadUtil(mActivity,
                                downloadUrl,
                                mAVObject.getString(AVOUtil.UpdateInfo.AppName),
                                mAVObject.getObjectId(),
                                SDCardUtil.apkUpdatePath
                        ).DownloadFile();
                    }
                });
                dialog.show();
            }
        }
    }
}
