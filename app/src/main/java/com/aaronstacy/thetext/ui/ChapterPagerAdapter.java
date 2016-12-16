package com.aaronstacy.thetext.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.aaronstacy.thetext.db.Chapter;
import com.aaronstacy.thetext.db.ChapterReference;

final class ChapterPagerAdapter extends FragmentStatePagerAdapter {
  ChapterPagerAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override public Fragment getItem(int position) {
    Fragment fragment = new ChapterFragment();
    Bundle args = new Bundle();
    args.putParcelable(Chapter.TABLE, ChapterReference.fromIndex(position));
    fragment.setArguments(args);
    return fragment;
  }

  @Override public int getCount() {
    return ChapterReference.CHAPTER_COUNT;
  }
}
