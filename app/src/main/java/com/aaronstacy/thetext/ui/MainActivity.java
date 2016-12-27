package com.aaronstacy.thetext.ui;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.aaronstacy.thetext.R;
import com.aaronstacy.thetext.db.Chapter;
import com.aaronstacy.thetext.db.ChapterReference;

public class MainActivity
    extends AppCompatActivity
    implements BottomNavigationView.OnNavigationItemSelectedListener, OnChapterListener {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.main, ReadFragment.newInstance())
          .commit();

    }

    BottomNavigationView nav = (BottomNavigationView) findViewById(R.id.nav);
    nav.setOnNavigationItemSelectedListener(this);
  }

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    // TODO: don't navigate if we're already there
    switch (item.getItemId()) {
      case (R.id.nav_read):
        getSupportFragmentManager().beginTransaction()
            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
            .replace(R.id.main, ReadFragment.newInstance())
            .addToBackStack(null)
            .commit();
        break;
      case (R.id.nav_search):
        getSupportFragmentManager().beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
            .replace(R.id.main, SearchFragment.newInstance())
            .addToBackStack(null)
            .commit();
        break;
    }
    return true;
  }

  @Override public void onChapterSelected(ChapterReference chapterReference) {
    Fragment fragment = ReadFragment.newInstance();
    Bundle args = new Bundle();
    args.putParcelable(Chapter.TABLE, chapterReference);
    fragment.setArguments(args);
    getSupportFragmentManager().beginTransaction()
        .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
        .replace(R.id.main, fragment)
        .addToBackStack(null)
        .commit();
  }

  // TODO: update current selection and animate navigation on back button press
}
