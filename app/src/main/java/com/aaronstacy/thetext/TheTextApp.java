package com.aaronstacy.thetext;

import android.app.Application;
import android.content.Context;

import com.aaronstacy.thetext.api.LookupCacher;
import com.squareup.leakcanary.LeakCanary;

import javax.inject.Inject;

public final class TheTextApp extends Application {
  private TheTextComponent component;

  @Inject LookupCacher lookupCacher;

  @Override public void onCreate() {
    super.onCreate();

    if (LeakCanary.isInAnalyzerProcess(this)) {
      return;
    }
    LeakCanary.install(this);

    component = DaggerTheTextComponent.builder().theTextModule(new TheTextModule(this)).build();

    component.inject(this);

    lookupCacher.cacheLookupsToDb();
  }

  public static TheTextComponent component(Context context) {
    return ((TheTextApp) context.getApplicationContext()).component;
  }
}
