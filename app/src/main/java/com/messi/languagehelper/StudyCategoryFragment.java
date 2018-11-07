package com.messi.languagehelper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVAnalytics;
import com.facebook.drawee.view.SimpleDraweeView;
import com.messi.languagehelper.adapter.XmlyRecommendPageAdapter;
import com.messi.languagehelper.dao.EveryDaySentence;
import com.messi.languagehelper.db.DataBaseUtil;
import com.messi.languagehelper.http.LanguagehelperHttpClient;
import com.messi.languagehelper.http.UICallback;
import com.messi.languagehelper.impl.FragmentProgressbarListener;
import com.messi.languagehelper.service.PlayerService;
import com.messi.languagehelper.util.JsonParser;
import com.messi.languagehelper.util.KeyUtil;
import com.messi.languagehelper.util.LogUtil;
import com.messi.languagehelper.util.NumberUtil;
import com.messi.languagehelper.util.Setings;
import com.messi.languagehelper.util.TimeUtil;
import com.messi.languagehelper.util.XimalayaUtil;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.CategoryRecommendAlbums;
import com.ximalaya.ting.android.opensdk.model.album.CategoryRecommendAlbumsList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StudyCategoryFragment extends BaseFragment {

    @BindView(R.id.symbol_study_cover)
    FrameLayout symbolStudyCover;
    @BindView(R.id.study_listening_layout)
    FrameLayout studyListeningLayout;
    @BindView(R.id.en_examination_layout)
    FrameLayout enExaminationLayout;
    @BindView(R.id.study_composition)
    FrameLayout studyComposition;
    @BindView(R.id.study_word_layout)
    FrameLayout studyWordLayout;

    @BindView(R.id.collected_layout)
    FrameLayout collectedLayout;
    @BindView(R.id.study_spoken_english)
    FrameLayout studySpokenEnglish;
    @BindView(R.id.en_grammar)
    FrameLayout enGrammar;
    @BindView(R.id.en_story_layout)
    FrameLayout enStoryLayout;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    @BindView(R.id.xmly_layout)
    FrameLayout xmlyLayout;
    @BindView(R.id.tablayout)
    TabLayout tablayout;
    @BindView(R.id.dailysentence_txt)
    TextView dailysentence_txt;

    @BindView(R.id.study_daily_sentence)
    FrameLayout study_daily_sentence;
    @BindView(R.id.daily_sentence_item_img)
    SimpleDraweeView daily_sentence_item_img;

    private List<CategoryRecommendAlbums> mTabList;
    private EveryDaySentence mEveryDaySentence;
    private XmlyRecommendPageAdapter mAdapter;

    public static StudyCategoryFragment getInstance() {
        return new StudyCategoryFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            registerBroadcast();
            mProgressbarListener = (FragmentProgressbarListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement FragmentProgressbarListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.study_category_fragment, null);
        ButterKnife.bind(this, view);
        mAdapter = new XmlyRecommendPageAdapter(getChildFragmentManager());
        viewpager.setAdapter(mAdapter);
        getDailySentence();
        isLoadDailySentence();
        QueryTask();
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && isHasLoadData && isFragmentInit){
            if(mTabList == null){
                QueryTask();
            }
        }
    }

    @Override
    public void updateUI(String music_action) {
        if (music_action.equals(PlayerService.action_loading)) {
            showProgressbar();
        } else if (music_action.equals(PlayerService.action_finish_loading)) {
            hideProgressbar();
        }
    }

    @OnClick({R.id.symbol_study_cover, R.id.study_listening_layout,
            R.id.en_examination_layout, R.id.study_composition, R.id.collected_layout,
            R.id.study_spoken_english, R.id.en_grammar, R.id.en_story_layout,
            R.id.xmly_layout, R.id.study_word_layout, R.id.study_daily_sentence})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.symbol_study_cover:
                toActivity(SymbolActivity.class, null);
                AVAnalytics.onEvent(getContext(), "tab3_to_symbol");
                break;
            case R.id.study_listening_layout:
                toActivity(ListenActivity.class, null);
                AVAnalytics.onEvent(getContext(), "tab3_to_listening");
                break;
            case R.id.en_examination_layout:
                toExaminationActivity(getContext().getResources().getString(R.string.examination));
                AVAnalytics.onEvent(getContext(), "tab3_to_examination");
                break;
            case R.id.study_composition:
                toActivity(CompositionActivity.class, null);
                AVAnalytics.onEvent(getContext(), "tab3_to_composition");
                break;
            case R.id.collected_layout:
                toActivity(CollectedActivity.class, null);
                AVAnalytics.onEvent(getContext(), "tab3_to_collected");
                break;
            case R.id.study_spoken_english:
                toActivity(SpokenActivity.class, null);
                AVAnalytics.onEvent(getContext(), "tab3_to_spoken_english");
                break;
            case R.id.en_grammar:
                toActivity(GrammarActivity.class, null);
                AVAnalytics.onEvent(getContext(), "tab3_to_grammar");
                break;
            case R.id.en_story_layout:
                toActivity(StoryActivity.class, null);
                AVAnalytics.onEvent(getContext(), "tab3_to_story");
                break;
            case R.id.xmly_layout:
                toActivity(XmlyActivity.class,null);
                AVAnalytics.onEvent(getContext(), "tab3_to_ximalaya_home");
                break;
            case R.id.study_word_layout:
                toActivity(WordsActivity.class,null);
                AVAnalytics.onEvent(getContext(), "tab3_to_ximalaya_home");
                break;
            case R.id.study_daily_sentence:
                toActivity(DailySentenceAndEssayActivity.class,null);
                AVAnalytics.onEvent(getContext(), "tab3_to_dailysentence");
                break;
        }
    }

    private void toExaminationActivity(String title) {
        Intent intent = new Intent(getContext(), ExaminationActivity.class);
        intent.putExtra(KeyUtil.ActionbarTitle, title);
        getContext().startActivity(intent);
    }

    private void QueryTask() {
        showProgressbar();
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.CATEGORY_ID, XimalayaUtil.Category_english);
        map.put(DTransferConstants.DISPLAY_COUNT, "1");
        CommonRequest.getCategoryRecommendAlbums(map,
                new IDataCallBack<CategoryRecommendAlbumsList>() {
                    @Override
                    public void onSuccess(@Nullable CategoryRecommendAlbumsList categoryRecommendAlbumsList) {
                        onFinishLoadData();
                        initTab(categoryRecommendAlbumsList);
                    }

                    @Override
                    public void onError(int i, String s) {
                        onFinishLoadData();
                    }
                });
    }

    private void initTab(CategoryRecommendAlbumsList categoryRecommendAlbumsList) {
        mTabList = categoryRecommendAlbumsList.getCategoryRecommendAlbumses();
        Collections.reverse(mTabList);
        if(mTabList != null && mTabList.size() > 0){
            mAdapter.refreshByTags(XimalayaUtil.Category_english,mTabList.get(0).getTagName());
        }
        for (CategoryRecommendAlbums dra : mTabList) {
            if(!TextUtils.isEmpty(dra.getTagName())){
                tablayout.addTab(tablayout.newTab().setText(dra.getTagName()));
            }else {
                tablayout.addTab(tablayout.newTab().setText("精选"));
            }
        }
        tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onTabChange(mTabList.get(tab.getPosition()));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabChange(mTabList.get(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
        });
    }

    private void onTabChange(CategoryRecommendAlbums dra){
        mAdapter.refreshByTags(XimalayaUtil.Category_english,dra.getTagName());
    }

    private void onFinishLoadData() {
        hideProgressbar();
    }

    private void getDailySentence(){
        List<EveryDaySentence> mList = DataBaseUtil.getInstance().getDailySentenceList(1);
        if(mList != null){
            if(mList.size() > 0){
                mEveryDaySentence = mList.get(0);
            }
        }
        setSentence();
        LogUtil.DefalutLog("StudyFragment-getDailySentence()");
    }

    private void isLoadDailySentence(){
        String todayStr = TimeUtil.getTimeDateLong(System.currentTimeMillis());
        long cid = NumberUtil.StringToLong(todayStr);
        boolean isExist = DataBaseUtil.getInstance().isExist(cid);
        if(!isExist){
            requestDailysentence();
        }
        LogUtil.DefalutLog("StudyFragment-isLoadDailySentence()");
    }

    private void requestDailysentence(){
        LogUtil.DefalutLog("StudyFragment-requestDailysentence()");
        LanguagehelperHttpClient.get(Setings.DailySentenceUrl, new UICallback(getActivity()){
            public void onResponsed(String responseString) {
                if(JsonParser.isJson(responseString)){
                    mEveryDaySentence = JsonParser.parseEveryDaySentence(responseString);
                    setSentence();
                }
            }
        });
    }

    private void setSentence(){
        LogUtil.DefalutLog("StudyFragment-setSentence()");
        if(mEveryDaySentence != null){
            dailysentence_txt.setText(mEveryDaySentence.getContent());
            daily_sentence_item_img.setImageURI(Uri.parse(mEveryDaySentence.getPicture2()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterBroadcast();
    }

}
