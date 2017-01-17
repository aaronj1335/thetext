package com.aaronstacy.thetext.prefs;

import android.app.Application;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

@Module
public final class PrefsModule {
  @Provides @Singleton RecentChapters recentLookups(Application application) {
    return RecentChapters.create(application);
  }

  @Provides @Singleton Preference<Boolean> nightMode(Application application) {
    return RxSharedPreferences.create(getDefaultSharedPreferences(application))
        .getBoolean("NIGHT_MODE", false);
  }
}