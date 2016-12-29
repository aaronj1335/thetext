package com.aaronstacy.thetext;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.webkit.WebView;

import com.aaronstacy.thetext.api.LookupCacher;
import com.squareup.leakcanary.LeakCanary;

import javax.inject.Inject;

public final class TheTextApp extends Application {
  private TheTextComponent component;

  @SuppressWarnings("WeakerAccess") @Inject LookupCacher lookupCacher;

  @Override public void onCreate() {
    super.onCreate();

    initializeDevStuff();
    initializeDependencyInjection();

    lookupCacher.cacheLookupsToDb();
  }

  private void initializeDevStuff() {
    if (LeakCanary.isInAnalyzerProcess(this)) {
      return;
    }
    LeakCanary.install(this);

    if (BuildConfig.DEBUG) {
      // The first WebView instantiation causes StrictMode VM policy violations, something about
      // some disk IO not closing a file. Add this call before flipping on StrictMode to duck the
      // issue.
      new WebView(this);

      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
          .detectAll()
          .penaltyDeath()
          .build());
      StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
          .detectAll()
          .penaltyDeath()
          .build());

    }
  }

  private void initializeDependencyInjection() {
    component = DaggerTheTextComponent.builder().theTextModule(new TheTextModule(this)).build();
    component.inject(this);
  }

  public static TheTextComponent component(Context context) {
    return ((TheTextApp) context.getApplicationContext()).component;
  }
}
