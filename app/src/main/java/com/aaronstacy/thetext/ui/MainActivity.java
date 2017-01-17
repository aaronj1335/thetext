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
import android.view.MenuItem;

import com.aaronstacy.thetext.R;
import com.aaronstacy.thetext.db.Chapter;
import com.aaronstacy.thetext.db.ChapterReference;

import java.util.Map;

public class MainActivity
    extends AppCompatActivity
    implements OnNavigationItemSelectedListener, OnChapterListener, OnBackStackChangedListener {

  private BottomNavigationView nav;
  private static final Map<String, Integer> tagToId = new ArrayMap<>();

  static {
    tagToId.put(ReadFragment.TAG, R.id.nav_read);
    tagToId.put(SearchFragment.TAG, R.id.nav_search);
    tagToId.put(SettingsFragment.TAG, R.id.nav_settings);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    getSupportFragmentManager().addOnBackStackChangedListener(this);

    nav = (BottomNavigationView) findViewById(R.id.nav);

    if (savedInstanceState == null) {
      navigateTo(ReadFragment.newInstance(), ReadFragment.TAG);
    }

    nav.setOnNavigationItemSelectedListener(this);
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
}
