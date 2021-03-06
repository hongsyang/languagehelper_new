// Generated code from Butter Knife. Do not modify!
package com.messi.languagehelper;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class LoadingPreActivity_ViewBinding implements Unbinder {
  private LoadingPreActivity target;

  private View view7f08003c;

  private View view7f0801cb;

  @UiThread
  public LoadingPreActivity_ViewBinding(LoadingPreActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public LoadingPreActivity_ViewBinding(final LoadingPreActivity target, View source) {
    this.target = target;

    View view;
    target.privacyPolicy = Utils.findRequiredViewAsType(source, R.id.privacy_policy, "field 'privacyPolicy'", TextView.class);
    target.privacyTv1 = Utils.findRequiredViewAsType(source, R.id.privacy_tv1, "field 'privacyTv1'", TextView.class);
    target.privacyTv2 = Utils.findRequiredViewAsType(source, R.id.privacy_tv2, "field 'privacyTv2'", TextView.class);
    view = Utils.findRequiredView(source, R.id.agree, "method 'onClick'");
    view7f08003c = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick(p0);
      }
    });
    view = Utils.findRequiredView(source, R.id.no_agree, "method 'onClick'");
    view7f0801cb = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClick(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    LoadingPreActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.privacyPolicy = null;
    target.privacyTv1 = null;
    target.privacyTv2 = null;

    view7f08003c.setOnClickListener(null);
    view7f08003c = null;
    view7f0801cb.setOnClickListener(null);
    view7f0801cb = null;
  }
}
