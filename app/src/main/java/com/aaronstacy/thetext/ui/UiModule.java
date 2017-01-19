package com.aaronstacy.thetext.ui;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.subjects.BehaviorSubject;

@Module
public final class UiModule {
  @Provides @Singleton BehaviorSubject<Boolean> isNavVisible() {
    return BehaviorSubject.create(true);
  }

  @Provides NavVisibilityController navVisibilityController(BehaviorSubject<Boolean> isNavVisible) {
    return new NavVisibilityController(isNavVisible);
  }
}
