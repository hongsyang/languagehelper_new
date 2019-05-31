package com.messi.languagehelper.http;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.messi.languagehelper.bean.BaiduAccessToken;
import com.messi.languagehelper.impl.ProgressListener;
import com.messi.languagehelper.util.CameraUtil;
import com.messi.languagehelper.util.JsonParser;
import com.messi.languagehelper.util.KeyUtil;
import com.messi.languagehelper.util.LogUtil;
import com.messi.languagehelper.util.MD5;
import com.messi.languagehelper.util.PlayUtil;
import com.messi.languagehelper.util.Setings;
import com.messi.languagehelper.util.StringUtils;
import com.messi.languagehelper.util.TranslateHelper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LanguagehelperHttpClient {
	
	public static final int HTTP_RESPONSE_DISK_CACHE_MAX_SIZE = 10 * 1024 * 1024;
	private static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");
	public static final String Header = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36";
	public static OkHttpClient client = new OkHttpClient.Builder()
			.connectTimeout(15, TimeUnit.SECONDS)
			.readTimeout(30, TimeUnit.SECONDS)
			.writeTimeout(15, TimeUnit.SECONDS)
			.build();
	
	public static OkHttpClient initClient(Context mContext){
		File baseDir = mContext.getCacheDir();
		File cacheDir = new File(baseDir,"HttpResponseCache");
		if(cacheDir != null){
			client = new OkHttpClient.Builder()
					.connectTimeout(15, TimeUnit.SECONDS)
					.readTimeout(30, TimeUnit.SECONDS)
					.writeTimeout(15, TimeUnit.SECONDS)
					.cache(new Cache(cacheDir, HTTP_RESPONSE_DISK_CACHE_MAX_SIZE))
					.build();
		}
		return client;
	}

	public static Response get(String url) {
		Response mResponse = null;
		try {
			Request request = new Request.Builder()
					.url(url)
					.build();
			mResponse = client.newCall(request).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mResponse;
	}
	
	public static void get(String url, Callback mCallback) {
		Request request = new Request.Builder()
			.url(url)
			.header("User-Agent", Header)
			.build();
		client.newCall(request).enqueue(mCallback);
	}

	public static void get(Request request, Callback mCallback) {
		client.newCall(request).enqueue(mCallback);
	}

	public static Response get(String url,ProgressListener progressListener) {
		Response mResponse = null;
		try {
			Request request = new Request.Builder()
					.url(url)
					.build();
			OkHttpClient clone = LanguagehelperHttpClient.addProgressResponseListener(progressListener);
			mResponse = clone.newCall(request).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mResponse;
	}

	public static void get(HttpUrl mHttpUrl, String apikey, Callback mCallback) {
		if (TextUtils.isEmpty(apikey)) {
			Request request = new Request.Builder()
					.url(mHttpUrl)
					.header("User-Agent", Header)
					.build();
			client.newCall(request).enqueue(mCallback);
		}else {
			Request request = new Request.Builder()
					.url(mHttpUrl)
					.addHeader("apikey",apikey)
					.header("User-Agent", Header)
					.build();
			client.newCall(request).enqueue(mCallback);
		}
	}

	public static Response post(String url, RequestBody params, Callback mCallback) {
		Request request = new Request.Builder()
				.url(url)
				.post(params)
				.build();
		return executePost(request,mCallback);
	}

	public static Response postWithHeader(String url, RequestBody params, Callback mCallback) {
		Request request = new Request.Builder()
				.header("User-Agent", Header)
				.url(url)
				.post(params)
				.build();
		return executePost(request,mCallback);
	}

	public static Response executePost(Request request,Callback mCallback){
		Response mResponse = null;
		if(mCallback != null){
			client.newCall(request).enqueue(mCallback);
		}else {
			try {
				mResponse = client.newCall(request).execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mResponse;
	}

	public static Response postBaidu(Callback mCallback) {
		long salt = System.currentTimeMillis();
		FormBody formBody = new FormBody.Builder()
			.add("appid", Setings.baidu_appid)
			.add("salt", String.valueOf(salt))
			.add("q", Setings.q)
			.add("from", Setings.from)
			.add("to", Setings.to)
			.add("sign", getBaiduTranslateSign(salt))
			.build();
		return post(Setings.baiduTranslateUrl,  formBody , mCallback);
	}

	public static Response postHjApi(Callback mCallback) {
		//cn en jp(Japanese) kr(Korean) fr(French) de(German) th(Thai)
		//es(Spaish) ru(Russian) pt(Portuguese) it(Italian)
		String from = "";
		String to = "";
		if (StringUtils.isEnglish(Setings.q)) {
			from = "/en";
			to = "/cn";
		} else {
			from = "/cn";
			to = "/en";
		}
		String url = Setings.HjTranslateUrl + from + to;
		LogUtil.DefalutLog("HjTranslateUrl:"+url);
		FormBody formBody = new FormBody.Builder()
			.add("content", Setings.q)
			.build();
		Request request = new Request.Builder()
				.url(url)
				.header("User-Agent", Header)
				.header("Cookie", TranslateHelper.HJCookie)
				.post(formBody)
				.build();
		return executePost(request,mCallback);
	}

	public static Response postIcibaNew(Callback mCallback) {
		//zh  en  ja  ko  fr  de  es
		String from = "";
		String to = "";
		if (StringUtils.isEnglish(Setings.q)) {
			from = "en";
			to = "zh";
		} else {
			from = "zh";
			to = "en";
		}
		LogUtil.DefalutLog("from:"+from+"---to:"+to);
		FormBody formBody = new FormBody.Builder()
			.add("w", Setings.q)
			.add("", "")
			.add("f", from)
			.add("t", to)
			.build();
		Request request = new Request.Builder()
			.url(Setings.IcibaTranslateNewUrl)
			.header("User-Agent", Header)
			.post(formBody)
			.build();
		return executePost(request,mCallback);
	}

	public static Response getAiyueyu(Callback mCallback) {
		LogUtil.DefalutLog("getAiyueyu");
		String type = "";
		if (Setings.to.equals(Setings.yue)) {
			type = "0";
		} else {
			type = "1";
		}
		LogUtil.DefalutLog("type:"+type);
		FormBody formBody = new FormBody.Builder()
				.add("type", type)
				.add("text", Setings.q)
				.build();
		Request request = new Request.Builder()
				.url(Setings.TranAiyueyuUrl)
				.header("User-Agent", Header)
				.post(formBody)
				.build();
		return executePost(request,mCallback);
	}

	public static void postBaiduOCR(Activity context, String path, Callback mCallback) {
		try {
			String BaiduAccessToken = PlayUtil.getSP().getString(KeyUtil.BaiduAccessToken,"");
			long BaiduAccessTokenExpires = PlayUtil.getSP().getLong(KeyUtil.BaiduAccessTokenExpires,(long)0);
			long BaiduAccessTokenCreateAt = PlayUtil.getSP().getLong(KeyUtil.BaiduAccessTokenCreateAt,(long)0);
			boolean isExpired = System.currentTimeMillis() - BaiduAccessTokenCreateAt > BaiduAccessTokenExpires;
			if(!TextUtils.isEmpty(BaiduAccessToken) && !isExpired){
				String base64 = CameraUtil.getImageBase64(path,1280,1280,4000);
				FormBody formBody = new FormBody.Builder()
						.add("image", base64)
						.build();
				Request request = new Request.Builder()
						.url(Setings.BaiduOCRUrl+"?access_token="+PlayUtil.getSP().getString(KeyUtil.BaiduAccessToken,""))
						.header("Content-Type", "application/x-www-form-urlencoded")
						.post(formBody)
						.build();
				client.newCall(request).enqueue(mCallback);
			}else {
				getBaiduAccessToken(context,path,mCallback);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getBaiduAccessToken(final Activity context,final String path,final Callback mCallback){
		FormBody formBody = new FormBody.Builder()
				.add("grant_type", "client_credentials")
				.add("client_id", Setings.BaiduORCAK)
				.add("client_secret", Setings.BaiduORCSK)
				.build();
		post(Setings.BaiduAccessToken,  formBody ,new UICallback(context){
			@Override
			public void onFailured() {
			}
			@Override
			public void onFinished() {
			}
			@Override
			public void onResponsed(String responseString) {
				if(JsonParser.isJson(responseString)){
					BaiduAccessToken mBaiduAccessToken = JSON.parseObject(responseString, BaiduAccessToken.class);
					if(mBaiduAccessToken != null){
						Setings.saveSharedPreferences(PlayUtil.getSP(), KeyUtil.BaiduAccessToken, mBaiduAccessToken.getAccess_token());
						Setings.saveSharedPreferences(PlayUtil.getSP(), KeyUtil.BaiduAccessTokenExpires, mBaiduAccessToken.getExpires_in());
						Setings.saveSharedPreferences(PlayUtil.getSP(), KeyUtil.BaiduAccessTokenCreateAt, System.currentTimeMillis());
					}
					postBaiduOCR(context,path,mCallback);
				}
			}
		});
	}
	
	public static OkHttpClient addProgressResponseListener(final ProgressListener progressListener){
		OkHttpClient clone = new OkHttpClient.Builder().addNetworkInterceptor(new Interceptor() {
			@Override
			public Response intercept(Chain chain) throws IOException {
				// 拦截
				Response originalResponse = chain.proceed(chain.request());
				// 包装响应体并返回
				return originalResponse
						.newBuilder()
						.body(new ProgressResponseBody(originalResponse.body(), progressListener))
						.build();
			}
		}).build();
		return clone;
    }

	public static String getBaiduTranslateSign(long salt) {
		String str = Setings.baidu_appid + Setings.q + salt + Setings.baidu_secretkey;
		return MD5.encode(str);
	}

	public static void setTranslateLan(boolean isToYue){
		if(isToYue){
			Setings.from = Setings.zh;
			Setings.to = Setings.yue;
		}else{
			Setings.from = Setings.yue;
			Setings.to = Setings.zh;
		}
	}
}
