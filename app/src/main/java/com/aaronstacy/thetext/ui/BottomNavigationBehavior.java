package com.aaronstacy.thetext.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

public final class BottomNavigationBehavior<V extends View> extends VerticalScrollingBehavior<V> {
  private static final Interpolator INTERPOLATOR = new LinearOutSlowInInterpolator();
  private ViewPropertyAnimatorCompat offsetValueAnimator;
  private final int tabLayoutId;
  private int elevation = 8;
  int[] attributesArray = new int[]{android.R.attr.id, android.R.attr.elevation};
  private boolean scrollingEnabled = true;
  private boolean hideAlongSnackbar = false;
  private boolean hidden = false;
  private ViewGroup tabLayout;
  private int snackbarHeight = -1;

//  public BottomNavigationBehavior() {
//    super();
//  }

  public BottomNavigationBehavior(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    TypedArray attrs = context.obtainStyledAttributes(attributeSet, attributesArray);
    tabLayoutId = attrs.getResourceId(0, View.NO_ID);
    elevation = attrs.getResourceId(1, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        elevation, context.getResources().getDisplayMetrics()));
    attrs.recycle();
  }

  public static <V extends View> BottomNavigationBehavior<V> from(@NonNull V view) {
    ViewGroup.LayoutParams params = view.getLayoutParams();

    if (!(params instanceof CoordinatorLayout.LayoutParams)) {
      throw new IllegalArgumentException("View must be a child of CoordinatorLayout");
    }

    CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params).getBehavior();

    if (!(behavior instanceof BottomNavigationBehavior)) {
      throw new IllegalArgumentException("View must be associated with BottomNavigationBehavior");
    }

    return (BottomNavigationBehavior<V>) behavior;
  }

  @Override public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
    updateScrollingForSnackbar(dependency, child, true);
    return super.layoutDependsOn(parent, child, dependency);
  }

  private void updateScrollingForSnackbar(View dependency, V child, boolean enabled) {
    if (dependency instanceof Snackbar.SnackbarLayout) {
      scrollingEnabled = enabled;
      if (!hideAlongSnackbar && ViewCompat.getTranslationY(child) != 0) {
        ViewCompat.setTranslationY(child, 0);
        hidden = false;
        hideAlongSnackbar = true;
      } else if (hideAlongSnackbar) {
        hidden = true;
        animateOffset(child, -child.getHeight());
      }
    }
  }

  @Override public boolean onDependentViewChanged(CoordinatorLayout parent, V child,
                                                  View dependency) {
    updateScrollingForSnackbar(dependency, child, false);
    return super.onDependentViewChanged(parent, child, dependency);
  }

  @Override public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
    boolean layoutChild = super.onLayoutChild(parent, child, layoutDirection);
    if (tabLayout == null && tabLayoutId != View.NO_ID) {
      tabLayout = findTabLayout(child);
      elevateNavigationView();
    }

    return layoutChild;
  }

  @Override public void onNestedVerticalOverScroll(CoordinatorLayout coordinatorLayout, V child,
                                                   @ScrollDirection int direction,
                                                   int currentOverScroll, int totalOverScroll) {
  }

  @Override public void onDirectionNestedPreScroll(CoordinatorLayout coordinatorLayout, V child,
                                                   View target, int dx, int dy, int[] consumed,
                                                   @ScrollDirection int scrollDirection) {
    handleDirection(child, scrollDirection);
  }

  @Override protected boolean onNestedDirectionFling(CoordinatorLayout coordinatorLayout, V child,
                                                     View target, float velocityX, float velocityY,
                                                     @ScrollDirection int scrollDirection) {
    handleDirection(child, scrollDirection);
    return true;
  }

  private void handleDirection(V child, @ScrollDirection int scrollDirection) {
    if (!scrollingEnabled) return;
    if (scrollDirection == ScrollDirection.DOWN && hidden) {
      hidden = false;
      animateOffset(child, 0);
    } else if (scrollDirection == ScrollDirection.UP && !hidden) {
      hidden = true;
      animateOffset(child, child.getHeight());
    }
  }

  @Nullable private ViewGroup findTabLayout(@NonNull V child) {
    if (tabLayoutId == 0) {
      return null;
    }
    return (ViewGroup) child.findViewById(tabLayoutId);
  }

  private void elevateNavigationView() {
    if (tabLayout != null) {
      ViewCompat.setElevation(tabLayout, elevation);
    }
  }

  private void animateOffset(final V child, final int offset) {
    ensureOrCancelAnimator(child);
    offsetValueAnimator.translationY(offset).start();
  }

  private void ensureOrCancelAnimator(@NonNull  V child) {
    if (offsetValueAnimator == null) {
      offsetValueAnimator = ViewCompat.animate(child);
      offsetValueAnimator.setDuration(100);
      offsetValueAnimator.setInterpolator(INTERPOLATOR);
    } else {
      offsetValueAnimator.cancel();
    }
  }

  private interface BottomNavigationWithSnackbar {
    void updateSnackbar(CoordinatorLayout parent, View dependency, View child);
  }

  private class PreLollipopBottomNavWithSnackBarImpl implements BottomNavigationWithSnackbar {
    @Override public void updateSnackbar(CoordinatorLayout parent, View dependency, View child) {
      if (dependency instanceof Snackbar.SnackbarLayout) {
        if (snackbarHeight == -1) {
          snackbarHeight = dependency.getHeight();
        }

        int targetPadding = child.getMeasuredHeight();

        int shadow = (int) ViewCompat.getElevation(child);
        ViewGroup.MarginLayoutParams layoutParams =
            (ViewGroup.MarginLayoutParams) dependency.getLayoutParams();
        layoutParams.bottomMargin = targetPadding - shadow;
        child.bringToFront();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
          child.getParent().requestLayout();
          ((View) child.getParent()).invalidate();
        }

      }
    }
  }

  private class LollipopBottomNavWithSnackBarImpl implements BottomNavigationWithSnackbar {
    @Override public void updateSnackbar(CoordinatorLayout parent, View dependency, View child) {
      if (dependency instanceof Snackbar.SnackbarLayout) {
        if (snackbarHeight == -1) {
          snackbarHeight = dependency.getHeight();
        }
        int targetPadding = (snackbarHeight + child.getMeasuredHeight());
        dependency.setPadding(dependency.getPaddingLeft(), dependency.getPaddingTop(),
            dependency.getPaddingRight(), targetPadding);
      }
    }
  }

}
