// Generated code from Butter Knife. Do not modify!
package com.messi.languagehelper;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SettingActivity_ViewBinding implements Unbinder {
  private SettingActivity target;

  private View view7f080274;

  private View view7f080272;

  private View view7f080277;

  private View view7f080276;

  @UiThread
  public SettingActivity_ViewBinding(SettingActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public SettingActivity_ViewBinding(final SettingActivity target, View source) {
    this.target = target;

    View view;
    target.seekbarText = Utils.findRequiredViewAsType(source, R.id.seekbar_text, "field 'seekbarText'", TextView.class);
    target.seekbar = Utils.findRequiredViewAsType(source, R.id.seekbar, "field 'seekbar'", SeekBar.class);
    target.settingAutoPlayCb = Utils.findRequiredViewAsType(source, R.id.setting_auto_play_cb, "field 'settingAutoPlayCb'", CheckBox.class);
    view = Utils.findRequiredView(source, R.id.setting_auto_play, "field 'settingAutoPlay' and method 'onViewClicked'");
    target.settingAutoPlay = Utils.castView(view, R.id.setting_auto_play, "field 'settingAutoPlay'", FrameLayout.class);
    view7f080274 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.settingAutoClearCb = Utils.findRequiredViewAsType(source, R.id.setting_auto_clear_cb, "field 'settingAutoClearCb'", CheckBox.class);
    view = Utils.findRequiredView(source, R.id.setting_auto_clear, "field 'settingAutoClear' and method 'onViewClicked'");
    target.settingAutoClear = Utils.castView(view, R.id.setting_auto_clear, "field 'settingAutoClear'", FrameLayout.class);
    view7f080272 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.setting_clear_all_except_favorite, "field 'settingClearAllExceptFavorite' and method 'onViewClicked'");
    target.settingClearAllExceptFavorite = Utils.castView(view, R.id.setting_clear_all_except_favorite, "field 'settingClearAllExceptFavorite'", FrameLayout.class);
    view7f080277 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.setting_clear_all, "field 'settingClearAll' and method 'onViewClicked'");
    target.settingClearAll = Utils.castView(view, R.id.setting_clear_all, "field 'settingClearAll'", FrameLayout.class);
    view7f080276 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    SettingActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.seekbarText = null;
    target.seekbar = null;
    target.settingAutoPlayCb = null;
    target.settingAutoPlay = null;
    target.settingAutoClearCb = null;
    target.settingAutoClear = null;
    target.settingClearAllExceptFavorite = null;
    target.settingClearAll = null;

    view7f080274.setOnClickListener(null);
    view7f080274 = null;
    view7f080272.setOnClickListener(null);
    view7f080272 = null;
    view7f080277.setOnClickListener(null);
    view7f080277 = null;
    view7f080276.setOnClickListener(null);
    view7f080276 = null;
  }
}
