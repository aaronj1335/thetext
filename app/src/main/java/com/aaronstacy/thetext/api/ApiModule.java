package com.aaronstacy.thetext.api;

import com.aaronstacy.thetext.db.DbModule;
import com.aaronstacy.thetext.db.Lookup;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.subjects.PublishSubject;

@Module(
    includes = {DbModule.class}
)
public final class ApiModule {
  @Provides @Singleton PublishSubject<Lookup> lookups() {
    return PublishSubject.create();
  }

  @Provides @Singleton LookupCacher lookupCacher(PublishSubject<Lookup> lookups, BriteDatabase db) {
    return LookupCacher.create(lookups, db);
  }
}
