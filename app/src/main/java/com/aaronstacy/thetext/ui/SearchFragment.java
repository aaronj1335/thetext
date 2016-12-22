package com.aaronstacy.thetext.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aaronstacy.thetext.R;

public final class SearchFragment extends Fragment {
  @Nullable @Override public View onCreateView(LayoutInflater inflater,
                                               @Nullable ViewGroup container,
                                               @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.search, container, false);
    return view;
  }

  public static Fragment newInstance() {
    return new SearchFragment();
  }
}
