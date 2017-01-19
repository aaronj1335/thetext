package com.aaronstacy.thetext.ui;

import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import rx.subjects.BehaviorSubject;

final class NavVisibilityController
    extends GestureDetector.SimpleOnGestureListener
    implements View.OnTouchListener {
  private static final float FLING_UP_THRESHOLD = 1500;
  private static final float SCROLL_DOWN_THRESHOLD = 20;
  private final BehaviorSubject<Boolean> isNavVisible;
  private GestureDetectorCompat gestureDetector;
  private ScrollView view;

  public NavVisibilityController(BehaviorSubject<Boolean> isNavVisible) {
    this.isNavVisible = isNavVisible;
  }

  public void view(ScrollView view) {
    this.view = view;
    gestureDetector = new GestureDetectorCompat(view.getContext(), this);
    view.setOnTouchListener(this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      view.setOnScrollChangeListener(new View.OnScrollChangeListener() {
        @Override public void onScrollChange(View view, int i, int i1, int i2, int i3) {
          NavVisibilityController.this.onScrollChange();
        }
      });
    }
  }

  @Override public boolean onTouch(View view, MotionEvent motionEvent) {
    gestureDetector.onTouchEvent(motionEvent);
    return false;
  }

  @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    if (isAtTop() || isAtBottom()) {
      isNavVisible.onNext(true);
    } else if (distanceY > SCROLL_DOWN_THRESHOLD) {
      isNavVisible.onNext(false);
    }

    return super.onScroll(e1, e2, distanceX, distanceY);
  }

  @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    if (velocityY > FLING_UP_THRESHOLD) {
      isNavVisible.onNext(true);
    }
    return super.onFling(e1, e2, velocityX, velocityY);
  }

  private boolean isAtTop() {
    return view.getScrollY() < 10;
  }

  private boolean isAtBottom() {
    int childBottom = view.getChildAt(view.getChildCount() - 1).getBottom();
    return Math.abs(view.getScrollY() + view.getHeight() - childBottom) < 130;
  }

  private void onScrollChange() {
    if (isAtBottom() || isAtTop()) {
      isNavVisible.onNext(true);
    }
  }
}
