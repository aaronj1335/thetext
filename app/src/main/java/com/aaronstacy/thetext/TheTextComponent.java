package com.aaronstacy.thetext;

import com.aaronstacy.thetext.ui.ChapterFragment;
import com.aaronstacy.thetext.ui.MainActivity;
import com.aaronstacy.thetext.ui.SearchFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton @Component(modules = TheTextModule.class) public interface TheTextComponent {
  void inject(TheTextApp app);
  void inject(ChapterFragment fragment);
  void inject(SearchFragment fragment);
  void inject(MainActivity activity);
}
