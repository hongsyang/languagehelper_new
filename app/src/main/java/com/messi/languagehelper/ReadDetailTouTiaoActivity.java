package com.messi.languagehelper;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.messi.languagehelper.ViewModel.VideoADModel;
import com.messi.languagehelper.bean.TTParseBean;
import com.messi.languagehelper.bean.TTParseDataBean;
import com.messi.languagehelper.bean.ToutiaoVideoBean;
import com.messi.languagehelper.bean.ToutiaoWebRootBean;
import com.messi.languagehelper.box.BoxHelper;
import com.messi.languagehelper.box.Reading;
import com.messi.languagehelper.http.LanguagehelperHttpClient;
import com.messi.languagehelper.http.UICallback;
import com.messi.languagehelper.impl.FragmentProgressbarListener;
import com.messi.languagehelper.util.DialogUtil;
import com.messi.languagehelper.util.JsonParser;
import com.messi.languagehelper.util.KeyUtil;
import com.messi.languagehelper.util.LogUtil;
import com.messi.languagehelper.util.Setings;
import com.messi.languagehelper.util.StringUtils;
import com.messi.languagehelper.util.ViewUtil;
import com.ximalaya.ting.android.opensdk.util.DigestUtils;

import java.util.zip.CRC32;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.FormBody;


public class ReadDetailTouTiaoActivity extends BaseActivity implements FragmentProgressbarListener,
        Player.EventListener {

    private final String STATE_RESUME_WINDOW = "resumeWindow";
    private final String STATE_RESUME_POSITION = "resumePosition";
    private final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";
    private final String Hearder = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36";

    @BindView(R.id.refreshable_webview)
    WebView mWebView;
    @BindView(R.id.player_view)
    PlayerView simpleExoPlayerView;
    @BindView(R.id.title_tv)
    TextView titleTv;
    @BindView(R.id.video_ly)
    FrameLayout videoLayout;
    @BindView(R.id.back_btn)
    LinearLayout backBtn;
    @BindView(R.id.next_composition)
    LinearLayout nextComposition;
    @BindView(R.id.xx_ad_layout)
    FrameLayout xx_ad_layout;
    @BindView(R.id.webview_layout)
    FrameLayout webview_layout;
    @BindView(R.id.collect_btn)
    ImageView collect_btn;
    private String Url;
    private Reading mAVObject;
    private SimpleExoPlayer player;

    private FrameLayout mFullScreenButton;
    private ImageView mFullScreenIcon;
    private Dialog mFullScreenDialog;
    private boolean mExoPlayerFullscreen = false;
    private int mResumeWindow;
    private long mResumePosition;
    private VideoADModel mVideoADModel;
    private int status;
    private boolean isWVPSuccess;
    private String[] interceptUrls;
    private boolean isIntercept;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_detail_toutiao_activity);
        ButterKnife.bind(this);
        if (savedInstanceState != null) {
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
        }
        initViews();
        initFullscreenDialog();
        initFullscreenButton();
    }

    private void initViews() {
        SharedPreferences sp = Setings.getSharedPreferences(this);
        String intercepts = sp.getString(KeyUtil.InterceptUrls, "");
        LogUtil.DefalutLog("InterceptUrls:" + intercepts);
        if (!TextUtils.isEmpty(intercepts)) {
            if (intercepts.contains(",")) {
                interceptUrls = intercepts.split(",");
            }else {
                interceptUrls = new String[]{intercepts};
            }
        }
        setStatusbarColor(R.color.black);
        Object data = Setings.dataMap.get(KeyUtil.DataMapKey);
        Setings.dataMap.clear();
        if (data instanceof Reading) {
            mAVObject = (Reading) data;
        } else {
            finish();
            return;
        }
        titleTv.setText(mAVObject.getTitle());
        setCollected();
        Setings.MPlayerPause();
        Url = mAVObject.getSource_url();
        LogUtil.DefalutLog("Url:" + Url);
        mVideoADModel = new VideoADModel(this, xx_ad_layout);
        setWebView();
        setData();
        addVideoNewsList();
    }

    private void setCollected() {
        if (TextUtils.isEmpty(mAVObject.getIsCollected())) {
            collect_btn.setImageResource(R.drawable.uncollected);
        } else {
            collect_btn.setImageResource(R.drawable.collect_d);
        }
    }

    private void setData() {
        if (!TextUtils.isEmpty(mAVObject.getContent_type())) {
            if ("url_api".equals(mAVObject.getContent_type())) {
                parseVideoUrl();
            } else if ("url_media".equals(mAVObject.getContent_type())) {
                exoplaer(mAVObject.getMedia_url());
            } else if ("url".equals(mAVObject.getContent_type()) || "url_intercept".equals(mAVObject.getContent_type()) ) {
                if("今日头条".equals(mAVObject.getSource_name()) && !TextUtils.isEmpty(mAVObject.getVid())){
                    parseToutiaoHtml(mAVObject.getVid());
                }else {
                    interceptUrl();
                }
            } else {
                showWebView();
            }
        } else {
            exoplaer(mAVObject.getMedia_url());
        }
    }

    private void interceptUrl(){
        showProgressbar();
        isIntercept = true;
        mWebView.loadUrl(Url);
    }

    private void parseToutiaoHtml(String vid){
        LogUtil.DefalutLog("vid:"+vid);
        String videoUrl = "";
        try{
//            String pattern = "\"vid\":\"(\\w*)\"";
            String randint = StringUtils.getRandomString(16);
            String videoid = "/video/urls/v/1/toutiao/mp4/"+vid+"?r="+randint;
            CRC32 crc32 = new CRC32();
            crc32.update(videoid.getBytes());
            long checksum = crc32.getValue();
            videoUrl = "http://i.snssdk.com" + videoid + "&s=" + checksum;
            LogUtil.DefalutLog("videoid:"+videoid+"-videoUrl:"+videoUrl);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            parseToutiaoApi(videoUrl);
        }
    }

    private void parseToutiaoApi(String url){
        LogUtil.DefalutLog("parseToutiaoApi");
        LanguagehelperHttpClient.get(url,new UICallback(this){
            @Override
            public void onFailured() {
                interceptUrl();
            }
            @Override
            public void onFinished() {
            }
            @Override
            public void onResponsed(String responseStr) {
//                LogUtil.DefalutLog("parseToutiaoApi-responseStr:"+responseStr);
                String videoUrl = "";
                try{
                    ToutiaoVideoBean toutiaoVideo = JSON.parseObject(responseStr,ToutiaoVideoBean.class);
                    if(toutiaoVideo.getCode() == 0){
                        videoUrl = new String(
                                Base64.decode(toutiaoVideo.getData().getVideo_list().
                                        getVideo_1().getMain_url().getBytes(), Base64.DEFAULT));
                    }
                }catch (Exception e){
                    onFailured();
                    e.printStackTrace();
                }finally {
                    if(!TextUtils.isEmpty(videoUrl)){
                        exoplaer(videoUrl);
                    }else {
                        onFailured();
                    }
                }
            }
        });
    }

    private void showWebView(){
        hideProgressbar();
        isIntercept = false;
        isWVPSuccess = true;
        webview_layout.setVisibility(View.VISIBLE);
        videoLayout.setVisibility(View.GONE);
        mWebView.loadUrl(Url);
    }

    private void setWebView() {
        mWebView.requestFocus();
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mWebView.getSettings().setJavaScriptEnabled(true);
//        mWebView.getSettings().setUserAgentString(Hearder);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                LogUtil.DefalutLog("WebViewClient:onPageStarted");
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                LogUtil.DefalutLog("WebViewClient:onPageFinished");
                if (!isWVPSuccess) {
                    parseVideoUrl();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(final WebView view, String url) {
                LogUtil.DefalutLog("shouldInterceptRequest:" + url);
                interceptUrl(url);
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                DialogUtil.OnReceivedSslError(ReadDetailTouTiaoActivity.this,handler);
            }
        });
    }

    private void interceptUrl(String url) {
        if(isIntercept){
            boolean isPlay = false;
            if (interceptUrls != null) {
                for (String str : interceptUrls) {
                    if (url.contains(str)) {
                        isWVPSuccess = true;
                        isPlay = true;
                        LogUtil.DefalutLog("interceptUrl contains");
                        break;
                    }
                }
                if (isPlay) {
                    ReadDetailTouTiaoActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LogUtil.DefalutLog("runOnUiThread");
                            if("今日头条".equals(mAVObject.getSource_name())){
                                parseToutiaoWebApi(url);
                            }else {
                                exoplaer(url);
                            }
                        }
                    });
                }
            }
        }
    }

    private void parseToutiaoWebApi(String url){
        LogUtil.DefalutLog("parseToutiaoWebApi:"+url);
        LanguagehelperHttpClient.get(url,new UICallback(this){
            @Override
            public void onFailured() {
                LogUtil.DefalutLog("parseToutiaoWebApi-onFailured");
                parseVideoUrl();
            }
            @Override
            public void onFinished() {
            }
            @Override
            public void onResponsed(String responseString) {
                String videoUrl = "";
                try {
                    if(!TextUtils.isEmpty(responseString)){
                        int start = responseString.indexOf("{");
                        int end = responseString.lastIndexOf("}");
                        if(start > 0 && end > 0){
                            String jstr = responseString.substring(start,end+1);
                            LogUtil.DefalutLog("jstr:"+jstr);
                            if(JsonParser.isJson(jstr)){
                                ToutiaoWebRootBean result = JSON.parseObject(jstr, ToutiaoWebRootBean.class);
                                if(result != null
                                        && result.getData() != null
                                        && result.getData().getVideo_list() != null
                                        && result.getData().getVideo_list().getVideo_1() != null){
                                    videoUrl = result.getData().getVideo_list().getVideo_1().getMain_url();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    videoUrl = "";
                    e.printStackTrace();
                } finally {
                    LogUtil.DefalutLog("parseToutiaoWebApi-videoUrl:"+videoUrl);
                    if(!TextUtils.isEmpty(videoUrl)){
                        exoplaer(videoUrl);
                    }else {
                        onFailured();
                    }
                }
            }
        });
    }

    private void parseVideoUrl() {
        LogUtil.DefalutLog("parseVideoUrl");
        if(status == 1){
            return;
        }
        showProgressbar();
        Long timestamp = System.currentTimeMillis();
        String sign = DigestUtils.md5Hex(Url + timestamp + Setings.TTParseClientSecretKey);
        FormBody formBody = new FormBody.Builder()
                .add("link", Url)
                .add("timestamp", timestamp + "")
                .add("sign", sign)
                .add("client", Setings.TTParseClientId)
                .build();
        LanguagehelperHttpClient.post(Setings.TTParseApi, formBody, new UICallback(this) {
            @Override
            public void onResponsed(String responseString) {
                LogUtil.DefalutLog("parseVideoUrl-responseString:" + responseString);
                String videoUrl = "";
                try {
                    if (JsonParser.isJson(responseString)) {
                        TTParseBean result = JSON.parseObject(responseString, TTParseBean.class);
                        if (result != null && result.getSucc() && result.getData() != null) {
                            TTParseDataBean videoBean = result.getData();
                            if (videoBean != null && !TextUtils.isEmpty(videoBean.getVideo())) {
                                videoUrl = videoBean.getVideo();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (!TextUtils.isEmpty(videoUrl)) {
                        exoplaer(videoUrl);
                    } else {
                        onFailured();
                    }
                }
            }

            @Override
            public void onFailured() {
                showWebView();
            }

            @Override
            public void onFinished() {
                hideProgressbar();
            }
        });
    }

    private void exoplaer(String media_url) {
        LogUtil.DefalutLog("exoplaer---media_url:" + media_url);
        hideProgressbar();
        if (status == 0) {
            status = 1;
            webview_layout.setVisibility(View.GONE);
            videoLayout.setVisibility(View.VISIBLE);
            player = ExoPlayerFactory.newSimpleInstance(this);
            simpleExoPlayerView.setPlayer(player);

            boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;
            if (haveResumePosition) {
                simpleExoPlayerView.getPlayer().seekTo(mResumeWindow, mResumePosition);
            }

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                    Util.getUserAgent(this, Hearder));
            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(media_url));
            player.addListener(this);
            player.prepare(videoSource);
            player.setPlayWhenReady(true);
            LogUtil.DefalutLog("ACTION_setPlayWhenReady");
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        LogUtil.DefalutLog("onPlayerStateChanged:" + playbackState + "-:playWhenReady:" + playWhenReady);
        if (playWhenReady && playbackState == Player.STATE_ENDED) {
            mVideoADModel.showAd();
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
        outState.putLong(STATE_RESUME_POSITION, mResumePosition);
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void initFullscreenDialog() {
        mFullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if (mExoPlayerFullscreen) closeFullscreenDialog();
                super.onBackPressed();
            }
        };
    }

    private void openFullscreenDialog() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ((ViewGroup) simpleExoPlayerView.getParent()).removeView(simpleExoPlayerView);
        mFullScreenDialog.addContentView(simpleExoPlayerView,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_exit_grey600_24dp));
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();
    }

    private void closeFullscreenDialog() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((ViewGroup) simpleExoPlayerView.getParent()).removeView(simpleExoPlayerView);
        videoLayout.addView(simpleExoPlayerView);
        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_grey600_24dp));
    }

    private void initFullscreenButton() {
        mFullScreenIcon = simpleExoPlayerView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenButton = simpleExoPlayerView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mExoPlayerFullscreen)
                    openFullscreenDialog();
                else
                    closeFullscreenDialog();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void addVideoNewsList() {
        Fragment fragment = new ReadingFragment.Builder()
                .type("video")
                .maxRandom(2000)
                .boutique_code(mAVObject.getBoutique_code())
                .isNeedClear(true)
                .build();
        if (getApplication().getPackageName().equals(Setings.application_id_yys) ||
                getApplication().getPackageName().equals(Setings.application_id_yys_google)) {
            fragment = ReadingFragmentYYS.newInstance();
        } else if (getApplication().getPackageName().equals(Setings.application_id_ywcd)) {
            fragment = ReadingFragmentYWCD.newInstance();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.next_composition, fragment)
                .commit();
    }

    private void collected() {
        if (TextUtils.isEmpty(mAVObject.getIsCollected())) {
            mAVObject.setIsCollected("1");
            mAVObject.setCollected_time(System.currentTimeMillis());
        } else {
            mAVObject.setIsCollected("");
            mAVObject.setCollected_time(0);
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
        }
        BoxHelper.update(mAVObject);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewUtil.destroyWebView(mWebView);
        status = 1;
        releasePlayer();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @OnClick(R.id.back_btn)
    public void onViewClicked() {
        releasePlayer();
        onBackPressed();
    }

    public void releasePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.release();
            player = null;
        }
        if (mFullScreenDialog != null) {
            mFullScreenDialog.dismiss();
        }
    }

    @OnClick(R.id.collect_btn)
    public void onClick() {
        if(mAVObject != null){
            if(TextUtils.isEmpty(mAVObject.getIsCollected())){
                mAVObject.setIsCollected("1");
                mAVObject.setCollected_time(System.currentTimeMillis());
            }else {
                mAVObject.setIsCollected("");
                mAVObject.setCollected_time(0);
            }
            setCollected();
            BoxHelper.update(mAVObject);
        }
    }
}
