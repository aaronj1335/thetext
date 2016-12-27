package com.aaronstacy.thetext.api;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.aaronstacy.thetext.db.ChapterReference;
import com.aaronstacy.thetext.rx.Join;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public final class RecentChapters implements SharedPreferences.OnSharedPreferenceChangeListener {
  private final String PREFS = "RecentChapters";
  private static final int MAX_RECENT_CHAPTERS = 10;
  private static final String DELIMITER = ",";
  private final BehaviorSubject<List<ChapterReference>> recentLookups =
      BehaviorSubject.create(Collections.<ChapterReference>emptyList());
  private final Observable<List<ChapterReference>> distinctRecentLookups = recentLookups.distinct();
  private final Application app;

  private RecentChapters(final Application app) {
    this.app = app;

    Schedulers.io().createWorker().schedule(new Action0() {
      @Override public void call() {
        SharedPreferences sharedPreferences = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        recentLookups.onNext(deserialize(sharedPreferences.getString(PREFS, "")));
        sharedPreferences.registerOnSharedPreferenceChangeListener(RecentChapters.this);
      }
    });
  }

  private static List<ChapterReference> deserialize(String recentChapters) {
    List<ChapterReference> references = new ArrayList<>();
    for (String recentChapter : recentChapters.split(DELIMITER)) {
      try {
        references.add(ChapterReference.builder().of(recentChapter).build());
      } catch (ParseException ignored) {}
    }
    return references;
  }

  public Observable<List<ChapterReference>> asObservable() {
    return distinctRecentLookups;
  }

  public void add(final ChapterReference chapterReference) {
    Observable.just(chapterReference)
        .observeOn(Schedulers.io())
        .concatWith(Observable.from(recentLookups.getValue()))
        .take(MAX_RECENT_CHAPTERS)
        .flatMap(new Func1<ChapterReference, Observable<String>>() {
          @Override public Observable<String> call(ChapterReference ref) {
            return Observable.just(ref.book().toString(), " ", String.valueOf(ref.chapter()));
          }
        })
        .lift(Join.with(DELIMITER))
        .reduce(new StringBuilder(), new Func2<StringBuilder, String, StringBuilder>() {
          @Override public StringBuilder call(StringBuilder stringBuilder, String s) {
            return stringBuilder.append(s);
          }
        })
        .subscribe(new Action1<StringBuilder>() {
          @Override public void call(StringBuilder stringBuilder) {
            app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(PREFS, stringBuilder.toString())
                .apply();
          }
        });

    // Gonna keep the non-Rx implementation here just to show how dumb I am.
//    Schedulers.io().createWorker().schedule(new Action0() {
//      @Override public void call() {
//        List<ChapterReference> current = recentLookups.getValue();
//        if (chapterReference.equals(current.get(0))) {
//          return;
//        }
//        StringBuilder newValue = new StringBuilder(current.size());
//        newValue.append(chapterReference.book()).append(" ").append(chapterReference.chapter());
//        for (int i = 0; i < current.size() - 1; i++) {
//          ChapterReference chapterReference = current.get(i);
//          newValue.append(chapterReference.book()).append(" ").append(chapterReference.chapter());
//        }
//        app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
//            .edit()
//            .putString(PREFS, newValue.toString())
//            .apply();
//      }
//    });
  }

  public static RecentChapters create(Application app) {
    return new RecentChapters(app);
  }

  @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
    if (PREFS.equals(s)) {
      recentLookups.onNext(deserialize(sharedPreferences.getString(PREFS, "")));
    }
  }
}
