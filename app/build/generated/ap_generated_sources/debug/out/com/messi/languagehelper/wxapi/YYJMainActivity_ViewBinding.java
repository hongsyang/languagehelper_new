// Generated code from Butter Knife. Do not modify!
package com.messi.languagehelper.wxapi;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.design.widget.BottomNavigationView;
import android.view.View;
import android.widget.FrameLayout;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.messi.languagehelper.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class YYJMainActivity_ViewBinding implements Unbinder {
  private YYJMainActivity target;

  @UiThread
  public YYJMainActivity_ViewBinding(YYJMainActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public YYJMainActivity_ViewBinding(YYJMainActivity target, View source) {
    this.target = target;

    target.content = Utils.findRequiredViewAsType(source, R.id.content, "field 'content'", FrameLayout.class);
    target.navigation = Utils.findRequiredViewAsType(source, R.id.navigation, "field 'navigation'", BottomNavigationView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    YYJMainActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.content = null;
    target.navigation = null;
  }
}
