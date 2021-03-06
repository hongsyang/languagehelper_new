// Generated code from Butter Knife. Do not modify!
package com.messi.languagehelper;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class CaricatureDetailActivity_ViewBinding implements Unbinder {
  private CaricatureDetailActivity target;

  private View view7f08003b;

  private View view7f0802f2;

  private View view7f080060;

  private View view7f08027e;

  @UiThread
  public CaricatureDetailActivity_ViewBinding(CaricatureDetailActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public CaricatureDetailActivity_ViewBinding(final CaricatureDetailActivity target, View source) {
    this.target = target;

    View view;
    target.itemImg = Utils.findRequiredViewAsType(source, R.id.item_img, "field 'itemImg'", SimpleDraweeView.class);
    target.item_img_bg = Utils.findRequiredViewAsType(source, R.id.item_img_bg, "field 'item_img_bg'", SimpleDraweeView.class);
    target.name = Utils.findRequiredViewAsType(source, R.id.name, "field 'name'", TextView.class);
    target.tags = Utils.findRequiredViewAsType(source, R.id.tags, "field 'tags'", TextView.class);
    target.author = Utils.findRequiredViewAsType(source, R.id.author, "field 'author'", TextView.class);
    target.source = Utils.findRequiredViewAsType(source, R.id.source, "field 'source'", TextView.class);
    target.views = Utils.findRequiredViewAsType(source, R.id.views, "field 'views'", TextView.class);
    target.des = Utils.findRequiredViewAsType(source, R.id.des, "field 'des'", TextView.class);
    view = Utils.findRequiredView(source, R.id.add_bookshelf, "field 'addBookshelf' and method 'onAddBookshelfClicked'");
    target.addBookshelf = Utils.castView(view, R.id.add_bookshelf, "field 'addBookshelf'", TextView.class);
    view7f08003b = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onAddBookshelfClicked();
      }
    });
    view = Utils.findRequiredView(source, R.id.to_read, "field 'toRead' and method 'onToReadClicked'");
    target.toRead = Utils.castView(view, R.id.to_read, "field 'toRead'", TextView.class);
    view7f0802f2 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onToReadClicked();
      }
    });
    target.xx_ad_layout = Utils.findRequiredViewAsType(source, R.id.xx_ad_layout, "field 'xx_ad_layout'", FrameLayout.class);
    view = Utils.findRequiredView(source, R.id.back_btn, "field 'backBtn' and method 'onBackBtnClicked'");
    target.backBtn = Utils.castView(view, R.id.back_btn, "field 'backBtn'", ImageView.class);
    view7f080060 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onBackBtnClicked();
      }
    });
    view = Utils.findRequiredView(source, R.id.share_img, "field 'share_img' and method 'onViewClicked'");
    target.share_img = Utils.castView(view, R.id.share_img, "field 'share_img'", ImageView.class);
    view7f08027e = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked();
      }
    });
    target.itemLayout = Utils.findRequiredViewAsType(source, R.id.item_layout, "field 'itemLayout'", LinearLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    CaricatureDetailActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.itemImg = null;
    target.item_img_bg = null;
    target.name = null;
    target.tags = null;
    target.author = null;
    target.source = null;
    target.views = null;
    target.des = null;
    target.addBookshelf = null;
    target.toRead = null;
    target.xx_ad_layout = null;
    target.backBtn = null;
    target.share_img = null;
    target.itemLayout = null;

    view7f08003b.setOnClickListener(null);
    view7f08003b = null;
    view7f0802f2.setOnClickListener(null);
    view7f0802f2 = null;
    view7f080060.setOnClickListener(null);
    view7f080060 = null;
    view7f08027e.setOnClickListener(null);
    view7f08027e = null;
  }
}
