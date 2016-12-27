package com.aaronstacy.thetext.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aaronstacy.thetext.R;
import com.aaronstacy.thetext.db.Chapter;
import com.aaronstacy.thetext.db.ChapterReference;

public final class ReadFragment extends Fragment {

  @Nullable @Override public View onCreateView(LayoutInflater inflater,
                                               @Nullable ViewGroup container,
                                               @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.read, container, false);
    ViewPager pager = (ViewPager) view.findViewById(R.id.chapter_pager);

    ChapterPagerAdapter pagerAdapter = new ChapterPagerAdapter(getChildFragmentManager());
    pager.setAdapter(pagerAdapter);

    Bundle arguments = getArguments();
    if (arguments != null) {
      ChapterReference chapterReference = arguments.getParcelable(Chapter.TABLE);
      if (chapterReference != null) {
        pager.setCurrentItem(chapterReference.toIndex(), false);
      }
    }

    return view;
  }

  public static Fragment newInstance() {
    return new ReadFragment();
  }
}
