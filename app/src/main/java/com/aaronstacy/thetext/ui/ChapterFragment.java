package com.aaronstacy.thetext.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ScrollView;

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
  private final BehaviorSubject<ChapterReference> chapterReferenceInput =
      BehaviorSubject.create(ChapterReference.builder().book("Genesis").chapter(1).build());
  private Subscription subscription;
  @SuppressWarnings("WeakerAccess") @Inject PublishSubject<Lookup> lookups;
  @SuppressWarnings("WeakerAccess") @Inject BriteDatabase db;
  @SuppressWarnings("WeakerAccess") @Inject Observable<String> style;
  @SuppressWarnings("WeakerAccess")
  @Inject NavVisibilityController navVisibilityController;

  private void maybeSetChapterReference(@Nullable Bundle bundle) {
    if (bundle != null) {
      ChapterReference chapterReference = bundle.getParcelable(Chapter.TABLE);
      if (chapterReference != null) {
        chapterReferenceInput.onNext(chapterReference);
      }
    }
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    TheTextApp.component(getActivity()).inject(this);
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
    navVisibilityController.view((ScrollView) rootView.findViewById(R.id.chapter_text_scroller));
    return rootView;
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    outState.putParcelable(Chapter.TABLE, chapterReferenceInput.getValue());
    super.onSaveInstanceState(outState);
  }

  @Override public void onResume() {
    super.onResume();
    Observable<ChapterReference> distinctChapterReferences =
        chapterReferenceInput.distinctUntilChanged();

    Observable<Model> references = distinctChapterReferences
        .map(new Func1<ChapterReference, Model>() {
          @Override public Model call(ChapterReference chapterReference) {
            return Model.builder().chapterReference(chapterReference).build();
          }
        });

    Observable<Model> chapters = distinctChapterReferences
        .flatMap(new Func1<ChapterReference, Observable<Model>>() {
          @Override public Observable<Model> call(final ChapterReference chapterReference) {
            // Fetch the chapter from the network in parallel with the database lookup. If the value
            // is not present or out of date, it will be inserted and we'll get the chapter either
            // way.
            lookups.onNext(Lookup.builder().chapterReference(chapterReference).build());

            String book = String.valueOf(chapterReference.book().index());
            String chapter = String.valueOf(chapterReference.chapter());

            return db.createQuery(Chapter.TABLE, Chapter.CHAPTER_QUERY, book, chapter)
                .mapToOne(Chapter.MAPPER)
                .filter(new Func1<Chapter, Boolean>() {
                  @Override public Boolean call(Chapter chapter) {
                    return chapter != null;
                  }
                })
                .withLatestFrom(style, new Func2<Chapter, String, Model>() {
                  @Override public Model call(Chapter chapter, String style) {
                    return Model.builder()
                        .chapterReference(chapter.chapterReference())
                        .chapter(chapter)
                        .style(style)
                        .build();
                  }
                });
          }
        })

        // If chapterReferenceInput receives a new value before the database call returns a chapter,
        // the previous chapter could be emitted, creating a momentarily invalid state. This ensures
        // that Chapter's are only emitted if they match the latest inputChapterReference.
        .withLatestFrom(distinctChapterReferences, new Func2<Model, ChapterReference, Model>() {
          @Override public Model call(Model model, ChapterReference latestInputChapterReference) {
            return latestInputChapterReference.equals(model.chapterReference())? model : null;
          }
        })
        .filter(new Func1<Model, Boolean>() {
          @Override public Boolean call(Model model) {
            return model != null;
          }
        })

        // Calling distinct here ensures that if something is already in the DB, we don't re-bind to
        // the same model after an API call returns.
        .distinctUntilChanged();

    subscription = Observable.merge(references, chapters)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<Model>() {
          @Override public void call(Model model) {
            ChapterFragment.this.bind(model);
          }
        });
  }

  @Override public void onPause() {
    super.onPause();
    subscription.unsubscribe();
    subscription = null;
  }

  private void bind(Model model) {
    String chapterHtml = model.chapterHtml();
    if (chapterHtml == null) {
      chapterTextView.setVisibility(View.INVISIBLE);
    } else {
      chapterTextView.loadData(chapterHtml, "text/html", null);
      chapterTextView.setVisibility(View.VISIBLE);
    }
  }

  @AutoValue static abstract class Model implements Parcelable {
    public abstract ChapterReference chapterReference();
    @Nullable public abstract Chapter chapter();
    @Nullable public abstract String style();

    @Nullable public String chapterHtml() {
      if (chapter() == null) {
        return null;
      }

      //noinspection ConstantConditions
      return "<!doctype html><html><head><style>" +
          style() +
          "</style></head><body>" +
          chapter().text() +
          "</body></html>";
    }

    @AutoValue.Builder abstract static class Builder {
      abstract Builder chapterReference(ChapterReference value);
      abstract Builder chapter(Chapter value);
      abstract Builder style(String value);
      abstract Model autoBuild();

      Model build() {
        Model model = autoBuild();
        if (model.chapter() == null && model.style() != null ||
            model.chapter() != null && model.style() == null) {
          throw new IllegalStateException("chapter and style must be set together");
        }
        return model;
      }
    }

    public static Builder builder() {
      return new AutoValue_ChapterFragment_Model.Builder();
    }
  }
}
