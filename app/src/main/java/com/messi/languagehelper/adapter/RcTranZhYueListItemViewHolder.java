package com.messi.languagehelper.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;
import com.messi.languagehelper.PracticeYYSActivity;
import com.messi.languagehelper.R;
import com.messi.languagehelper.box.BoxHelper;
import com.messi.languagehelper.box.TranResultZhYue;
import com.messi.languagehelper.util.AVAnalytics;
import com.messi.languagehelper.util.AudioTrackUtil;
import com.messi.languagehelper.util.KeyUtil;
import com.messi.languagehelper.util.PlayUtil;
import com.messi.languagehelper.util.SDCardUtil;
import com.messi.languagehelper.util.Setings;
import com.messi.languagehelper.util.ToastUtil;
import com.messi.languagehelper.util.XFUtil;

import java.util.List;

/**
 * Created by luli on 10/23/16.
 */

public class RcTranZhYueListItemViewHolder extends RecyclerView.ViewHolder {

    private Context context;
    public TextView record_question;
    public TextView record_answer;
    public FrameLayout record_answer_cover;
    public FrameLayout record_to_practice;
    public FrameLayout record_question_cover;
    public FrameLayout delete_btn;
    public FrameLayout collected_btn;
    public FrameLayout weixi_btn;
    public ImageButton voice_play;
    public ImageView unread_dot_answer;
    public ImageView unread_dot_question;
    public CheckBox collected_cb;
    public FrameLayout voice_play_layout;
    public ProgressBar play_content_btn_progressbar;

    private List<TranResultZhYue> beans;
    private RcTranZhYueListAdapter mAdapter;

    public RcTranZhYueListItemViewHolder(View convertView,
                                         List<TranResultZhYue> mBeans,
                                         RcTranZhYueListAdapter mAdapter) {
        super(convertView);
        this.context = convertView.getContext();
        this.beans = mBeans;
        this.mAdapter = mAdapter;
        record_question_cover = (FrameLayout) convertView.findViewById(R.id.record_question_cover);
        record_answer_cover = (FrameLayout) convertView.findViewById(R.id.record_answer_cover);
        record_to_practice = (FrameLayout) convertView.findViewById(R.id.record_to_practice);
        record_question = (TextView) convertView.findViewById(R.id.record_question);
        record_answer = (TextView) convertView.findViewById(R.id.record_answer);
        voice_play = (ImageButton) convertView.findViewById(R.id.voice_play);
        unread_dot_answer = (ImageView) convertView.findViewById(R.id.unread_dot_answer);
        unread_dot_question = (ImageView) convertView.findViewById(R.id.unread_dot_question);
        collected_cb = (CheckBox) convertView.findViewById(R.id.collected_cb);
        voice_play_layout = (FrameLayout) convertView.findViewById(R.id.voice_play_layout);
        delete_btn = (FrameLayout) convertView.findViewById(R.id.delete_btn);
        collected_btn = (FrameLayout) convertView.findViewById(R.id.collected_btn);
        weixi_btn = (FrameLayout) convertView.findViewById(R.id.weixi_btn);
        play_content_btn_progressbar = (ProgressBar) convertView.findViewById(R.id.play_content_btn_progressbar);
    }

    public void render(final TranResultZhYue mBean) {
        AnimationDrawable animationDrawable = (AnimationDrawable) voice_play.getBackground();
        MyOnClickListener mMyOnClickListener = new MyOnClickListener(mBean,animationDrawable,voice_play,play_content_btn_progressbar,true);
        MyOnClickListener mQuestionOnClickListener = new MyOnClickListener(mBean,animationDrawable,voice_play,play_content_btn_progressbar,false);
        if(mBean.getIscollected().equals("0")){
            collected_cb.setChecked(false);
        }else{
            collected_cb.setChecked(true);
        }
        if (getLayoutPosition() == 0) {
            if (!PlayUtil.getSP().getBoolean(KeyUtil.IsShowAnswerUnread, false)) {
                unread_dot_answer.setVisibility(View.VISIBLE);
            } else {
                unread_dot_answer.setVisibility(View.GONE);
            }
        } else {
            unread_dot_answer.setVisibility(View.GONE);
            unread_dot_question.setVisibility(View.GONE);
        }
        record_question.setText(mBean.getChinese());
        record_answer.setText(mBean.getEnglish());

        record_question_cover.setOnClickListener(mQuestionOnClickListener);
        record_answer_cover.setOnClickListener(mMyOnClickListener);
        voice_play_layout.setOnClickListener(mMyOnClickListener);

        record_answer_cover.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Setings.copy(context,mBean.getEnglish());
                return true;
            }
        });
        record_question_cover.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Setings.copy(context,mBean.getChinese());
                return true;
            }
        });

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteEntity(getLayoutPosition());
            }
        });
        weixi_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = "";
                if(mBean.getEnglish().contains("英[") || mBean.getEnglish().contains("美[")){
                    text = mBean.getChinese()+"\n"+mBean.getEnglish();
                }else{
                    text = mBean.getEnglish();
                }
                Setings.share(context,text);
                AVAnalytics.onEvent(context, "tab1_share_btn");
            }
        });
        collected_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCollectedStatus(mBean);
                AVAnalytics.onEvent(context, "tab1_tran_collected");
            }
        });
        record_to_practice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,PracticeYYSActivity.class);
                Setings.dataMap.put(KeyUtil.DialogBeanKey, mBean);
                context.startActivity(intent);
                AVAnalytics.onEvent(context, "tab1_tran_to_practicepg");
            }
        });
    }

    public void deleteEntity(int position) {
        try{
            TranResultZhYue mBean = beans.remove(position);
            mAdapter.notifyItemRemoved(position);
            BoxHelper.remove(mBean);
            ToastUtil.diaplayMesShort(context,context.getResources().getString(R.string.dele_success));
            AVAnalytics.onEvent(context, "tab1_tran_delete");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateCollectedStatus(TranResultZhYue mBean){
        if (mBean.getIscollected().equals("0")) {
            mBean.setIscollected("1");
            ToastUtil.diaplayMesShort(context,context.getResources().getString(R.string.favorite_success));
        } else {
            mBean.setIscollected("0");
            ToastUtil.diaplayMesShort(context,context.getResources().getString(R.string.favorite_cancle));
        }
        mAdapter.notifyDataSetChanged();
        BoxHelper.update(mBean);
    }

    public class MyOnClickListener implements View.OnClickListener {

        private TranResultZhYue mBean;
        private ImageButton voice_play;
        private AnimationDrawable animationDrawable;
        private ProgressBar play_content_btn_progressbar;
        private boolean isPlayResult;
        boolean isNotify = false;

        private MyOnClickListener(TranResultZhYue bean,AnimationDrawable mAnimationDrawable,ImageButton voice_play,
                                  ProgressBar progressbar, boolean isPlayResult){
            this.mBean = bean;
            this.voice_play = voice_play;
            this.animationDrawable = mAnimationDrawable;
            this.play_content_btn_progressbar = progressbar;
            this.isPlayResult = isPlayResult;
        }
        @Override
        public void onClick(final View v) {
            String filepath = "";
            String speakContent = "";
            String speaker = "";
            String path = SDCardUtil.getDownloadPath(SDCardUtil.sdPath);
            if (TextUtils.isEmpty(mBean.getResultVoiceId()) || TextUtils.isEmpty(mBean.getQuestionVoiceId())) {
                mBean.setQuestionVoiceId(System.currentTimeMillis() + "");
                mBean.setResultVoiceId(System.currentTimeMillis() - 5 + "");
            }
            if (!PlayUtil.getSP().getBoolean(KeyUtil.IsShowAnswerUnread, false)){
                isNotify = true;
            }
            if (isPlayResult) {
                Setings.saveSharedPreferences(PlayUtil.getSP(), KeyUtil.IsShowAnswerUnread, true);
                filepath = path + mBean.getResultVoiceId() + ".pcm";
                mBean.setResultAudioPath(filepath);
                if (!TextUtils.isEmpty(mBean.getBackup1())) {
                    speakContent = mBean.getBackup1();
                } else {
                    speakContent = mBean.getEnglish();
                }
            } else {
                Setings.saveSharedPreferences(PlayUtil.getSP(), KeyUtil.IsShowQuestionUnread, true);
                filepath = path + mBean.getQuestionVoiceId() + ".pcm";
                mBean.setQuestionAudioPath(filepath);
                speakContent = mBean.getChinese();
            }
            if (mBean.getSpeak_speed() != PlayUtil.getSP().getInt(context.getString(R.string.preference_key_tts_speed), 50)) {
                String filep1 = path + mBean.getResultVoiceId() + ".pcm";
                String filep2 = path + mBean.getQuestionVoiceId() + ".pcm";
                AudioTrackUtil.deleteFile(filep1);
                AudioTrackUtil.deleteFile(filep2);
                mBean.setSpeak_speed(PlayUtil.getSP().getInt(context.getString(R.string.preference_key_tts_speed), 50));
            }
            if (isNotify) {
                mAdapter.notifyDataSetChanged();
            }
            if (isPlayResult) {
                if(mBean.getBackup3().equals(Setings.yue)){
                    speaker = XFUtil.SpeakerHk;
                }else{
                    speaker = XFUtil.SpeakerZh;
                }
            } else {
                if(mBean.getBackup3().equals(Setings.yue)){
                    speaker = XFUtil.SpeakerZh;
                }else{
                    speaker = XFUtil.SpeakerHk;
                }
            }
            PlayUtil.play(filepath, speakContent, animationDrawable, speaker,null,
                    new SynthesizerListener() {
                        @Override
                        public void onSpeakResumed() {
                        }
                        @Override
                        public void onSpeakProgress(int arg0, int arg1, int arg2) {
                        }
                        @Override
                        public void onSpeakPaused() {
                        }
                        @Override
                        public void onSpeakBegin() {
                            play_content_btn_progressbar.setVisibility(View.GONE);
                            voice_play.setVisibility(View.VISIBLE);
                            PlayUtil.onStartPlay();
                        }
                        @Override
                        public void onCompleted(SpeechError arg0) {
                            if (arg0 != null) {
                                ToastUtil.diaplayMesShort(context, arg0.getErrorDescription());
                            }
                            BoxHelper.update(mBean);
                            PlayUtil.onFinishPlay();
                        }
                        @Override
                        public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
                        }
                        @Override
                        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
                        }
                    });

            if (v.getId() == R.id.record_question_cover) {
                AVAnalytics.onEvent(context, "tab1_tran_play_question");
            } else if (v.getId() == R.id.record_answer_cover) {
                AVAnalytics.onEvent(context, "tab1_tran_play_result");
            } else if (v.getId() == R.id.voice_play_layout) {
                AVAnalytics.onEvent(context, "tab1_tran_play_voice");
            }
        }
    }
}
