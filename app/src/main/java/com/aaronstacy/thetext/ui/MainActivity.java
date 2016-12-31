package com.aaronstacy.thetext.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
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
    implements BottomNavigationView.OnNavigationItemSelectedListener, OnChapterListener {

  private BottomNavigationView nav;
  private static final Map<String, Integer> tagToId = new ArrayMap<>();

  static {
    tagToId.put(ReadFragment.TAG, R.id.nav_read);
    tagToId.put(SearchFragment.TAG, R.id.nav_search);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.main, ReadFragment.newInstance())
          .commit();

    }

    nav = (BottomNavigationView) findViewById(R.id.nav);
    nav.setOnNavigationItemSelectedListener(this);
  }

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    String currentTag = getCurrentTag();
    switch (item.getItemId()) {
      case (R.id.nav_read):
        navigateTo(ReadFragment.newInstance(), ReadFragment.TAG);
        break;
      case (R.id.nav_search):
        navigateTo(SearchFragment.newInstance(), SearchFragment.TAG);
        break;
    }
    return true;
  }

  private void navigateTo(Fragment fragment, @NonNull String tag) {
    if (!tag.equals(getCurrentTag())) {
      nav.getMenu().findItem(tagToId.get(tag)).setChecked(true);
      getSupportFragmentManager().beginTransaction()
          .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
          .replace(R.id.main, fragment)
          .addToBackStack(tag)
          .commit();
    }
  }

  @Nullable private String getCurrentTag() {
    FragmentManager manager = getSupportFragmentManager();
    if (manager.getBackStackEntryCount() <= 0) {
      return null;
    }
    BackStackEntry entry = manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1);
    return entry != null? entry.getName() : null;
  }

  @Nullable private String getPreviousTag() {
    FragmentManager manager = getSupportFragmentManager();
    if (manager.getBackStackEntryCount() <= 1) {
      return null;
    }
    BackStackEntry entry = manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 2);
    return entry != null? entry.getName() : null;
  }

  @Override public void onChapterSelected(ChapterReference chapterReference) {
    Fragment fragment = ReadFragment.newInstance();
    Bundle args = new Bundle();
    args.putParcelable(Chapter.TABLE, chapterReference);
    fragment.setArguments(args);
    navigateTo(fragment, ReadFragment.TAG);
  }

  @Override public void onBackPressed() {
    int id = getPreviousTag() == null? R.id.nav_read : tagToId.get(getPreviousTag());
    MenuItem item = nav.getMenu().findItem(id);
    if (item != null) {
      item.setChecked(true);
    }
    super.onBackPressed();
  }
}
