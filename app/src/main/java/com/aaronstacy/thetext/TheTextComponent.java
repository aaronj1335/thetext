package com.aaronstacy.thetext;

import com.aaronstacy.thetext.ui.ChapterFragment;
import com.aaronstacy.thetext.ui.ReadFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = TheTextModule.class)
public interface TheTextComponent {
  void inject(TheTextApp app);
//  void inject(ReadFragment fragment);
  void inject(ChapterFragment fragment);
}
