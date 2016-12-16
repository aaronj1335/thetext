package com.aaronstacy.thetext;

import android.app.Application;

import com.aaronstacy.thetext.api.ApiModule;
import com.aaronstacy.thetext.db.DbModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
  includes = {ApiModule.class, DbModule.class}
)
public final class TheTextModule {
  private final Application application;

  TheTextModule(Application application) {
    this.application = application;
  }

  @Provides @Singleton Application provideApplication() {
    return application;
  }
}