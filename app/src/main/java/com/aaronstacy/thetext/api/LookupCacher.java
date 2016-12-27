package com.aaronstacy.thetext.api;

import android.content.ContentValues;
import android.util.Log;

import com.aaronstacy.thetext.db.Chapter;
import com.aaronstacy.thetext.db.Lookup;
import com.squareup.sqlbrite.BriteDatabase;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;

public final class LookupCacher {
  private final PublishSubject<Lookup> lookups;
  private final BriteDatabase db;

  private static class LookupAndResponse {
    final Lookup lookup;
    final ResponseBody response;
    LookupAndResponse(Lookup lookup, ResponseBody response) {
      this.lookup = lookup;
      this.response = response;
    }
  }

  private static class LookupAndResponseWithBody {
    final Lookup lookup;
    final ResponseBody response;
    final String body;
    LookupAndResponseWithBody(Lookup lookup, ResponseBody response, String body) {
      this.lookup = lookup;
      this.response = response;
      this.body = body;
    }
  }

  private LookupCacher(PublishSubject<Lookup> lookups, BriteDatabase db) {
    this.lookups = lookups;
    this.db = db;
  }

  public static LookupCacher create(PublishSubject<Lookup> lookups, BriteDatabase db) {
    return new LookupCacher(lookups, db);
  }

  public void cacheLookupsToDb() {
    lookups
        // If we get a bunch of lookups, we only want to take the most recent, but the chapter pager
        // may render up to 3 chapters at a time, so save the most recent couple ViewPagers's worth
        // of lookups and just make those.
        // TODO: dedupe the chapters in this window
        .window(6)
        .sample(200, TimeUnit.MILLISECONDS)
        .flatMap(new Func1<Observable<Lookup>, Observable<Lookup>>() {
          @Override public Observable<Lookup> call(Observable<Lookup> lookupObservable) {
            return lookupObservable;
          }
        })

        .observeOn(Schedulers.io())
        .flatMap(new Func1<Lookup, Observable<LookupAndResponse>>() {
          @Override
          public Observable<LookupAndResponse> call(final Lookup passage) {
            return Esv.service()
                .lookup(passage.toString())
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends ResponseBody>>() {
                  @Override public Observable<? extends ResponseBody> call(Throwable throwable) {
                    Log.e("LookupCacher", "Recieved error: " + throwable, throwable);
                    return Observable.empty();
                  }
                })
                .map(new Func1<ResponseBody, LookupAndResponse>() {
                  @Override public LookupAndResponse call(ResponseBody responseBody) {
                    return new LookupAndResponse(passage, responseBody);
                  }
                });
          }
        })
        .map(new Func1<LookupAndResponse, LookupAndResponseWithBody>() {
          @Override
          public LookupAndResponseWithBody call(LookupAndResponse input) {
            try {
              return new LookupAndResponseWithBody(
                  input.lookup,
                  input.response,
                  input.response.string());
            } catch (IOException e) {
              throw Exceptions.propagate(e);
            }
          }
        })
        .subscribe(new Action1<LookupAndResponseWithBody>() {
          @Override public void call(LookupAndResponseWithBody input) {
            Chapter chapter = Chapter.builder()
                .book(input.lookup.book())
                .chapter(input.lookup.chapter())
                .text(input.body)
                .build();
            ContentValues values = chapter.toContentValues();
            db.insert(Chapter.TABLE, values, CONFLICT_REPLACE);
          }
        });
  }
}
