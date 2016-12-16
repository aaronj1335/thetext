package com.aaronstacy.thetext.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.aaronstacy.thetext.R;

public final class ReadFragment
    extends Fragment
    implements BottomNavigationView.OnNavigationItemSelectedListener {
  private ViewPager pager;
  private ChapterPagerAdapter pagerAdapter;

  @Nullable @Override public View onCreateView(LayoutInflater inflater,
                                               @Nullable ViewGroup container,
                                               @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.read, container, false);
    pager = (ViewPager) view.findViewById(R.id.chapter_pager);
    pagerAdapter = new ChapterPagerAdapter(getFragmentManager());
    pager.setAdapter(pagerAdapter);

    BottomNavigationView nav = (BottomNavigationView) view.findViewById(R.id.nav);
    nav.setOnNavigationItemSelectedListener(this);
    return view;
  }

  public static Fragment newInstance() {
    return new ReadFragment();
  }

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case (R.id.nav_read):
        Log.d("BLERG", "BLAG");
        break;
      case (R.id.nav_settings):
        Log.d("BLARG", "BLIGGITY");
        break;
    }
    return false;
  }
}
