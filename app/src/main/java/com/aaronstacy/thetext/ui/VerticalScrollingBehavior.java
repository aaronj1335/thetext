package com.aaronstacy.thetext.ui;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class VerticalScrollingBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {
  @ScrollDirection private int overScrollDirection = ScrollDirection.NONE;
  @ScrollDirection int scrollDirection = ScrollDirection.NONE;
  private int totalDyUnconsumed = 0;
  private int totalDy = 0;

  public VerticalScrollingBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

//  public VerticalScrollingBehavior() {
//    super();
//  }

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({ScrollDirection.UP, ScrollDirection.DOWN, ScrollDirection.NONE})
  public @interface ScrollDirection {
    int UP = 1;
    int DOWN = -1;
    int NONE = 0;
  }

  @ScrollDirection public int getOverScrollDirection() {
    return overScrollDirection;
  }

  @ScrollDirection public int getScrollDirection() {
    return scrollDirection;
  }

  public abstract void onNestedVerticalOverScroll(CoordinatorLayout coordinatorLayout, V child,
                                                  @ScrollDirection int direction,
                                                  int currentOverScroll, int totalOverScroll);

  public abstract void onDirectionNestedPreScroll(CoordinatorLayout coordinatorLayout, V child,
                                                  View target, int dx, int dy, int[] consumed,
                                                  @ScrollDirection int scrollDirection);

  @Override
  public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child,
                                   View directTargetChild, View target, int nestedScrollAxes) {
    return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
  }

//  @Override
//  public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, V child,
//                                     View directTargetChild, View target, int nestedScrollAxes) {
//    super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
//  }
//
//  @Override
//  public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target) {
//    super.onStopNestedScroll(coordinatorLayout, child, target);
//  }

  @Override
  public void onNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target,
                             int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
        dyUnconsumed);
    if (dyUnconsumed > 0 && totalDyUnconsumed < 0) {
      totalDyUnconsumed = 0;
      overScrollDirection = ScrollDirection.UP;
    } else if (dyUnconsumed < 0 && totalDyUnconsumed > 0) {
      totalDyUnconsumed = 0;
      overScrollDirection = ScrollDirection.DOWN;
    }
    totalDyUnconsumed += dyUnconsumed;
    onNestedVerticalOverScroll(coordinatorLayout, child, overScrollDirection, dyConsumed,
        totalDyUnconsumed);
  }

  @Override
  public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx,
                                int dy, int[] consumed) {
    super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
    if (dy > 0 && totalDy < 0) {
      totalDy = 0;
      scrollDirection = ScrollDirection.UP;
    } else if (dy < 0 && totalDy > 0) {
      totalDy = 0;
      scrollDirection = ScrollDirection.DOWN;
    }
    totalDy += dy;
    onDirectionNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, scrollDirection);
  }

  @Override
  public boolean onNestedFling(CoordinatorLayout coordinatorLayout, V child, View target,
                               float velocityX, float velocityY, boolean consumed) {
    super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    scrollDirection = velocityY > 0 ? ScrollDirection.UP : ScrollDirection.DOWN;
    return onNestedDirectionFling(coordinatorLayout, child, target, velocityX, velocityY,
        scrollDirection);
  }

  protected abstract boolean onNestedDirectionFling(CoordinatorLayout coordinatorLayout, V child,
                                                    View target, float velocityX, float velocityY,
                                                    @ScrollDirection int scrollDirection);

//  @Override
//  public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, V child, View target,
//                                  float velocityX, float velocityY) {
//    return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
//  }

//  @Override
//  public WindowInsetsCompat onApplyWindowInsets(CoordinatorLayout coordinatorLayout, V child,
//                                                WindowInsetsCompat insets) {
//    return super.onApplyWindowInsets(coordinatorLayout, child, insets);
//  }

//  @Override
//  public Parcelable onSaveInstanceState(CoordinatorLayout parent, V child) {
//    return super.onSaveInstanceState(parent, child);
//  }
}
