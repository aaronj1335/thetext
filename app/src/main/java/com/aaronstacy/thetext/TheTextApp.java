package com.aaronstacy.thetext;

import android.app.Application;
import android.content.Context;

import com.aaronstacy.thetext.api.LookupCacher;

import javax.inject.Inject;

public final class TheTextApp extends Application {
  private TheTextComponent component;

  @Inject LookupCacher lookupCacher;

  @Override public void onCreate() {
    super.onCreate();

    component = DaggerTheTextComponent.builder().theTextModule(new TheTextModule(this)).build();

    component.inject(this);

    lookupCacher.cacheLookupsToDb();
  }

  public static TheTextComponent component(Context context) {
    return ((TheTextApp) context.getApplicationContext()).component;
  }
}
