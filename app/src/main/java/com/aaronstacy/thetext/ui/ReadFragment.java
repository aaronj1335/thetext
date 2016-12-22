package com.aaronstacy.thetext.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aaronstacy.thetext.R;

public final class ReadFragment extends Fragment {
  private ViewPager pager;
  private ChapterPagerAdapter pagerAdapter;

  @Nullable @Override public View onCreateView(LayoutInflater inflater,
                                               @Nullable ViewGroup container,
                                               @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.read, container, false);
    pager = (ViewPager) view.findViewById(R.id.chapter_pager);
    pagerAdapter = new ChapterPagerAdapter(getChildFragmentManager());
    pager.setAdapter(pagerAdapter);
    return view;
  }

  public static Fragment newInstance() {
    return new ReadFragment();
  }
}
