package com.aaronstacy.thetext.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.aaronstacy.thetext.R;
import com.aaronstacy.thetext.TheTextApp;
import com.aaronstacy.thetext.db.Chapter;
import com.aaronstacy.thetext.db.ChapterReference;
import com.aaronstacy.thetext.db.Lookup;
import com.google.auto.value.AutoValue;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class ChapterFragment extends Fragment {
  private WebView chapterTextView;
//  private TextView loadingView;
  private BehaviorSubject<ChapterReference> chapterReferenceInput =
      BehaviorSubject.create(ChapterReference.builder().book("Genesis").chapter(1).build());
  @Inject PublishSubject<Lookup> lookups;
  @Inject BriteDatabase db;


  private Chapter chapter = null;
  private Subscription subscription;

  private void maybeSetChapterReference(@Nullable Bundle bundle) {
    if (bundle != null) {
      ChapterReference chapterReference = bundle.getParcelable(Chapter.TABLE);
      if (chapterReference != null) {
        chapterReferenceInput.onNext(chapterReference);
      }
    }
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    TheTextApp.component(activity).inject(this);
    maybeSetChapterReference(getArguments());
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    maybeSetChapterReference(savedInstanceState);
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater,
                                               @Nullable ViewGroup container,
                                               @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.chapter, container, false);
    chapterTextView = (WebView) rootView.findViewById(R.id.chapter_text);
//    loadingView = (TextView) rootView.findViewById(R.id.loading_text);
    return rootView;
  }

  @Override public void onResume() {
    super.onResume();

    Observable<Model> references = chapterReferenceInput
        .map(new Func1<ChapterReference, Model>() {
          @Override public Model call(ChapterReference chapterReference) {
            return Model.builder().chapterReference(chapterReference).build();
          }
        });

    Observable<Model> chapters = chapterReferenceInput
        .flatMap(new Func1<ChapterReference, Observable<Model>>() {
          @Override public Observable<Model> call(final ChapterReference chapterReference) {
            // Fetch the chapter from the network in parallel with the database lookup. If the value
            // is not present or out of date, it will be inserted and we'll get the chapter either
            // way.
            lookups.onNext(Lookup.builder().chapterReference(chapterReference).build());

            String book = chapterReference.book().toString();
            String chapter = String.valueOf(chapterReference.chapter());

            return db.createQuery(Chapter.TABLE, Chapter.CHAPTER_QUERY, book, chapter)
                .mapToOne(Chapter.MAPPER)
                .filter(new Func1<Chapter, Boolean>() {
                  @Override public Boolean call(Chapter chapter) {
                    return chapter != null;
                  }
                })
                .map(new Func1<Chapter, Model>() {
                  @Override public Model call(Chapter chapter) {
                    return Model.builder()
                        .chapterReference(chapter.chapterReference())
                        .chapter(chapter)
                        .build();
                  }
                });
          }
        })

        // If chapterReferenceInput receives a new value before the database call returns a chapter,
        // the previous chapter could be emitted, creating a momentarily invalid state. This ensures
        // that Chapter's are only emitted if they match the latest inputChapterReference.
        .withLatestFrom(chapterReferenceInput, new Func2<Model, ChapterReference, Model>() {
          @Override public Model call(Model state, ChapterReference latestInputChapterReference) {
            return latestInputChapterReference.equals(state.chapterReference())? state : null;
          }
        })
        .filter(new Func1<Model, Boolean>() {
          @Override public Boolean call(Model model) {
            return model != null;
          }
        });

    subscription = Observable.merge(references, chapters)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(new Action1<Model>() {
          @Override public void call(Model model) {
            ChapterFragment.this.bind(model);
          }
        })
        .subscribe();
  }

  @Override public void onPause() {
    super.onPause();
    subscription.unsubscribe();
    subscription = null;
  }

  private void bind(Model model) {
    if (model.chapter() == null) {
      chapterTextView.setVisibility(View.INVISIBLE);
//      loadingView.setVisibility(View.VISIBLE);
    } else {
      chapterTextView.loadData(model.chapter().text(), "text/html", null);
      chapterTextView.setVisibility(View.VISIBLE);
//      loadingView.setVisibility(View.INVISIBLE);
    }
  }

  @AutoValue
  static abstract class Model implements Parcelable {
    public abstract ChapterReference chapterReference();
    @Nullable public abstract Chapter chapter();

    @AutoValue.Builder
    abstract static class Builder {
      abstract Builder chapterReference(ChapterReference value);
      abstract Builder chapter(Chapter value);
      abstract Model build();
    }

    public static Builder builder() {
      return new AutoValue_ChapterFragment_Model.Builder();
    }
  }
}