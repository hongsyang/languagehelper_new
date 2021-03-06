// Generated code from Butter Knife. Do not modify!
package com.messi.languagehelper;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.facebook.drawee.view.SimpleDraweeView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class AiTuringActivity_ViewBinding implements Unbinder {
  private AiTuringActivity target;

  private View view7f0802c2;

  private View view7f0803be;

  private View view7f08014a;

  private View view7f0803b7;

  private View view7f0800c2;

  @UiThread
  public AiTuringActivity_ViewBinding(AiTuringActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public AiTuringActivity_ViewBinding(final AiTuringActivity target, View source) {
    this.target = target;

    View view;
    target.inputEt = Utils.findRequiredViewAsType(source, R.id.input_et, "field 'inputEt'", AppCompatEditText.class);
    view = Utils.findRequiredView(source, R.id.submit_btn_cover, "field 'submitBtn' and method 'onViewClicked'");
    target.submitBtn = Utils.castView(view, R.id.submit_btn_cover, "field 'submitBtn'", CardView.class);
    view7f0802c2 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.contentLv = Utils.findRequiredViewAsType(source, R.id.content_lv, "field 'contentLv'", RecyclerView.class);
    target.recordAnimImg = Utils.findRequiredViewAsType(source, R.id.record_anim_img, "field 'recordAnimImg'", ImageView.class);
    target.recordLayout = Utils.findRequiredViewAsType(source, R.id.record_layout, "field 'recordLayout'", LinearLayout.class);
    target.volumeImg = Utils.findRequiredViewAsType(source, R.id.volume_img, "field 'volumeImg'", ImageView.class);
    view = Utils.findRequiredView(source, R.id.volume_btn, "field 'volumeBtn' and method 'onViewClicked'");
    target.volumeBtn = Utils.castView(view, R.id.volume_btn, "field 'volumeBtn'", FrameLayout.class);
    view7f0803be = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.inputTypeBtn = Utils.findRequiredViewAsType(source, R.id.input_type_btn, "field 'inputTypeBtn'", ImageView.class);
    view = Utils.findRequiredView(source, R.id.input_type_layout, "field 'inputTypeLayout' and method 'onViewClicked'");
    target.inputTypeLayout = Utils.castView(view, R.id.input_type_layout, "field 'inputTypeLayout'", LinearLayout.class);
    view7f08014a = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.voiceBtn = Utils.findRequiredViewAsType(source, R.id.voice_btn, "field 'voiceBtn'", TextView.class);
    view = Utils.findRequiredView(source, R.id.voice_btn_cover, "field 'voiceBtnCover' and method 'onViewClicked'");
    target.voiceBtnCover = Utils.castView(view, R.id.voice_btn_cover, "field 'voiceBtnCover'", CardView.class);
    view7f0803b7 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.micLayout = Utils.findRequiredViewAsType(source, R.id.mic_layout, "field 'micLayout'", LinearLayout.class);
    target.keybordLayout = Utils.findRequiredViewAsType(source, R.id.keybord_layout, "field 'keybordLayout'", LinearLayout.class);
    target.progressTv = Utils.findRequiredViewAsType(source, R.id.progress_tv, "field 'progressTv'", TextView.class);
    target.numberProgressBar = Utils.findRequiredViewAsType(source, R.id.number_progress_bar, "field 'numberProgressBar'", NumberProgressBar.class);
    target.adImg = Utils.findRequiredViewAsType(source, R.id.ad_img, "field 'adImg'", SimpleDraweeView.class);
    target.adSource = Utils.findRequiredViewAsType(source, R.id.ad_source, "field 'adSource'", TextView.class);
    target.adLayout = Utils.findRequiredViewAsType(source, R.id.ad_layout, "field 'adLayout'", RelativeLayout.class);
    view = Utils.findRequiredView(source, R.id.delete_btn, "field 'deleteBtn' and method 'onViewClicked'");
    target.deleteBtn = Utils.castView(view, R.id.delete_btn, "field 'deleteBtn'", FrameLayout.class);
    view7f0800c2 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.splash_container = Utils.findRequiredViewAsType(source, R.id.splash_container, "field 'splash_container'", FrameLayout.class);
    target.skip_view = Utils.findRequiredViewAsType(source, R.id.skip_view, "field 'skip_view'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    AiTuringActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.inputEt = null;
    target.submitBtn = null;
    target.contentLv = null;
    target.recordAnimImg = null;
    target.recordLayout = null;
    target.volumeImg = null;
    target.volumeBtn = null;
    target.inputTypeBtn = null;
    target.inputTypeLayout = null;
    target.voiceBtn = null;
    target.voiceBtnCover = null;
    target.micLayout = null;
    target.keybordLayout = null;
    target.progressTv = null;
    target.numberProgressBar = null;
    target.adImg = null;
    target.adSource = null;
    target.adLayout = null;
    target.deleteBtn = null;
    target.splash_container = null;
    target.skip_view = null;

    view7f0802c2.setOnClickListener(null);
    view7f0802c2 = null;
    view7f0803be.setOnClickListener(null);
    view7f0803be = null;
    view7f08014a.setOnClickListener(null);
    view7f08014a = null;
    view7f0803b7.setOnClickListener(null);
    view7f0803b7 = null;
    view7f0800c2.setOnClickListener(null);
    view7f0800c2 = null;
  }
}
