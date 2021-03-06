package com.messi.languagehelper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.messi.languagehelper.databinding.FragmentEnDicBinding;
import com.messi.languagehelper.event.FinishEvent;
import com.messi.languagehelper.http.LanguagehelperHttpClient;
import com.messi.languagehelper.impl.FragmentProgressbarListener;
import com.messi.languagehelper.util.HtmlParseUtil;
import com.messi.languagehelper.util.LogUtil;
import com.messi.languagehelper.util.Setings;
import com.messi.languagehelper.util.ViewUtil;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;

public class EnDicFragment extends BaseFragment {

    private String lastSearch;
    private FragmentEnDicBinding binding;

    public static EnDicFragment getInstance(FragmentProgressbarListener listener) {
        EnDicFragment mMainFragment = new EnDicFragment();
        mMainFragment.setmProgressbarListener(listener);
        return mMainFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        if (binding != null && binding.getRoot() != null) {
            ViewUtil.removeFromParentView(binding.getRoot());
            return binding.getRoot();
        }
        binding = FragmentEnDicBinding.inflate(inflater);
        return binding.getRoot();
    }

    private void translateController() {
        lastSearch = Setings.q;
        showProgressbar();
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                String url = Setings.EndicApi + lastSearch;
                LogUtil.DefalutLog(url);
                Response mResponse = LanguagehelperHttpClient.get(url);
                if (mResponse != null && mResponse.isSuccessful()) {
                    e.onNext(HtmlParseUtil.parseEndicHtml(mResponse.body().string()));
                }
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(String content) {
                        binding.dicContent.setText(lastSearch+"\n"+content);
                        binding.dicScrollview.scrollTo(0,0);
                        EventBus.getDefault().post(new FinishEvent());
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgressbar();
                    }

                    @Override
                    public void onComplete() {
                        hideProgressbar();
                    }
                });

    }

    public void submit() {
        translateController();
    }

}
