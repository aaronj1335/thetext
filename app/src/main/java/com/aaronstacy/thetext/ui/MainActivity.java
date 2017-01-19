package com.aaronstacy.thetext.ui;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.aaronstacy.thetext.R;
import com.aaronstacy.thetext.TheTextApp;
import com.aaronstacy.thetext.db.Chapter;
import com.aaronstacy.thetext.db.ChapterReference;

import java.util.Map;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

public class MainActivity
    extends AppCompatActivity
    implements OnNavigationItemSelectedListener, OnChapterListener, OnBackStackChangedListener, Animation.AnimationListener {

  private BottomNavigationView nav;
  private static final Map<String, Integer> tagToId = new ArrayMap<>();
  private Subscription subscription;
  private Animation slideUp;
  private Animation slideDown;
  @SuppressWarnings("WeakerAccess") @Inject BehaviorSubject<Boolean> isNavVisible;

  static {
    tagToId.put(ReadFragment.TAG, R.id.nav_read);
    tagToId.put(SearchFragment.TAG, R.id.nav_search);
    tagToId.put(SettingsFragment.TAG, R.id.nav_settings);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    TheTextApp.component(this).inject(this);
    setContentView(R.layout.main);

    getSupportFragmentManager().addOnBackStackChangedListener(this);
    slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
    slideUp.setAnimationListener(this);
    slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
    slideDown.setAnimationListener(this);

    nav = (BottomNavigationView) findViewById(R.id.nav);

    if (savedInstanceState == null) {
      navigateTo(ReadFragment.newInstance(), ReadFragment.TAG);
    }

    nav.setOnNavigationItemSelectedListener(this);
  }

  @Override protected void onResume() {
    super.onResume();
    subscription = isNavVisible.distinctUntilChanged()
        .subscribe(new Action1<Boolean>() {
          @Override public void call(Boolean isNavVisible) {
            Log.d("BLERG", "nav animation: " + isNavVisible);
            nav.startAnimation(isNavVisible? slideUp : slideDown);
          }
        });
  }

  @Override protected void onPause() {
    super.onPause();
    subscription.unsubscribe();
    subscription = null;
  }

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    if (!tagToId.get(getCurrentTag()).equals(item.getItemId())) {
      switch (item.getItemId()) {
        case (R.id.nav_read):
          navigateTo(ReadFragment.newInstance(), ReadFragment.TAG);
          break;
        case (R.id.nav_search):
          navigateTo(SearchFragment.newInstance(), SearchFragment.TAG);
          break;
        case (R.id.nav_settings):
          navigateTo(SettingsFragment.newInstance(), SettingsFragment.TAG);
          break;
      }
    }
    return true;
  }

  private void navigateTo(Fragment fragment, @NonNull String tag) {
    isNavVisible.onNext(true);
    getSupportFragmentManager().beginTransaction()
        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        .replace(R.id.main, fragment, tag)
        .addToBackStack(tag)
        .commit();
  }

  @NonNull private String getCurrentTag() {
    FragmentManager manager = getSupportFragmentManager();
    if (manager.getBackStackEntryCount() <= 0) {
      return ReadFragment.TAG;
    }
    BackStackEntry entry = manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1);
    return entry.getName();
  }

  @Override public void onChapterSelected(ChapterReference chapterReference) {
    Fragment fragment = ReadFragment.newInstance();
    Bundle args = new Bundle();
    args.putParcelable(Chapter.TABLE, chapterReference);
    fragment.setArguments(args);
    navigateTo(fragment, ReadFragment.TAG);
  }

  @Override public void onBackStackChanged() {
    nav.getMenu().findItem(tagToId.get(getCurrentTag())).setChecked(true);
  }

  @Override public void onBackPressed() {
    // u thought window.pushState was the worst navigation api
    FragmentManager fragmentManager = getSupportFragmentManager();
    if (fragmentManager.getBackStackEntryCount() > 1) {
      fragmentManager.popBackStack();
      fragmentManager.popBackStack(ReadFragment.TAG, 0);
    } else {
      fragmentManager.popBackStackImmediate();
      super.onBackPressed();
    }
  }

  @Override public void onAnimationStart(Animation animation) {
    nav.setVisibility(isNavVisible.getValue()? View.VISIBLE : View.INVISIBLE);
  }

  @Override public void onAnimationEnd(Animation animation) {
    nav.setVisibility(isNavVisible.getValue()? View.VISIBLE : View.INVISIBLE);
  }

  @Override public void onAnimationRepeat(Animation animation) {}
}
