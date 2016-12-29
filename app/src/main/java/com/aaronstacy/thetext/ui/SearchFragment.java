package com.aaronstacy.thetext.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.aaronstacy.thetext.R;
import com.aaronstacy.thetext.TheTextApp;
import com.aaronstacy.thetext.api.LookupSuggestions;
import com.aaronstacy.thetext.api.LookupSuggestions.Suggestion;
import com.aaronstacy.thetext.api.RecentChapters;
import com.aaronstacy.thetext.db.BookReference;
import com.aaronstacy.thetext.db.ChapterReference;
import com.google.auto.value.AutoValue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public final class SearchFragment
    extends Fragment
    implements TextView.OnEditorActionListener, TextWatcher {
  private static final String SEARCH_TEXT = "SEARCH_TEXT";
  private EditText lookup;
  private Adapter adapter;
  private final BehaviorSubject<String> searchText = BehaviorSubject.create("");
  private final PublishSubject<Integer> emptyResultsOnSubmit = PublishSubject.create();
  @SuppressWarnings("WeakerAccess") @Inject RecentChapters recentChapters;
  private Subscription subscription;
  private OnChapterListener listener;
  private TextView noRecentMessage;
  private TextView noSuggestionsMessage;
  private TextView noResultsForSubmittedMessage;

  public static Fragment newInstance() {
    return new SearchFragment();
  }

  @Override public void onAttach(Context context) {
    Activity activity = getActivity();
    if (!(activity instanceof OnChapterListener)) {
      throw new IllegalStateException("SearchFragment can only be attached to Activity that " +
          "implements OnChapterListener");
    } else {
      listener = (OnChapterListener) activity;
    }
    super.onAttach(context);
    TheTextApp.component(activity).inject(this);
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
      searchText.onNext(savedInstanceState.getString(SEARCH_TEXT, ""));
    }
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater,
                                               @Nullable ViewGroup container,
                                               @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.search, container, false);

    lookup = (EditText) view.findViewById(R.id.lookup);
    lookup.addTextChangedListener(this);
    lookup.setOnEditorActionListener(this);

    RecyclerView results = (RecyclerView) view.findViewById(R.id.results);
    results.setHasFixedSize(true);
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    results.setLayoutManager(layoutManager);
    adapter = new Adapter();
    results.setAdapter(adapter);

    noRecentMessage = (TextView) view.findViewById(R.id.no_recent);
    noSuggestionsMessage = (TextView) view.findViewById(R.id.no_suggestions);
    noResultsForSubmittedMessage = (TextView) view.findViewById(R.id.no_results_for_submitted);

    return view;
  }

  @Override public void onResume() {
    super.onResume();

    // Is this really the best way to show the keyboard onResume?
    lookup.postDelayed(new Runnable() {
      @Override public void run() {
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
            .toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
      }
    }, 0);

    Observable<Model> suggestions = Observable.combineLatest(searchText, recentChapters.asObservable(),
        new Func2<String, List<ChapterReference>, List<Suggestion>>() {
          @Override public List<Suggestion> call(String query, List<ChapterReference> refs) {
            return LookupSuggestions.forQuery(query, refs);
          }
        })
        .withLatestFrom(searchText.asObservable(), new Func2<List<Suggestion>, String, Model>() {
          @Override public Model call(List<Suggestion> chapterReferences, String query) {
            return Model.builder().query(query).results(chapterReferences).build();
          }
        });

    Observable<Model> emptyResults = emptyResultsOnSubmit
        .withLatestFrom(searchText, new Func2<Integer, String, Model>() {
          @Override public Model call(Integer integer, String query) {
            return Model.builder().query(query).results(null).build();
          }
        });

    subscription = Observable.merge(suggestions, emptyResults)
        .subscribe(new Action1<Model>() {
          @Override public void call(Model model) {
            SearchFragment.this.bind(model);
          }
        });
  }

  @Override public void onPause() {
    super.onPause();
    subscription.unsubscribe();
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    outState.putString(SEARCH_TEXT, searchText.getValue());
    super.onSaveInstanceState(outState);
  }

  @Override public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
    try {
      selectChapter(ChapterReference.builder().chapter(1).of(searchText.getValue()).build());
    } catch (ParseException ignored) {
      emptyResultsOnSubmit.onNext(actionId);
    }
    return true;
  }

  @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
  @Override public void afterTextChanged(Editable editable) {}
  @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    searchText.onNext(String.valueOf(charSequence));
  }

  private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    class ViewHolder extends RecyclerView.ViewHolder {
      private final View view;
      private final ImageButton selectChapter;
      private Suggestion suggestion;
      private View.OnClickListener goToChapter = new View.OnClickListener() {
        @Override public void onClick(View view) {
          if (suggestion.chapter() != null) {
            selectChapter(suggestion.chapter());
          } else {
            selectChapter(ChapterReference.builder().book(suggestion.book()).chapter(1).build());
          }
        }
      };

      ViewHolder(View view) {
        super(view);
        this.view = view;
        view.setOnClickListener(goToChapter);
        ((ImageButton) view.findViewById(R.id.go)).setOnClickListener(goToChapter);
        selectChapter = (ImageButton) view.findViewById(R.id.select_chapter);
        selectChapter.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View view) {
            final BookReference book = suggestion.book();
            int chapterCount = BookReference.CHAPTER_COUNTS[book.index()];
            String[] chapters = new String[chapterCount];
            for (int i = 0; i < chapterCount; i++) {
              chapters[i] = String.valueOf(i + 1);
            }
            new AlertDialog.Builder(SearchFragment.this.getContext())
                .setItems(chapters, new DialogInterface.OnClickListener() {
                  @Override public void onClick(DialogInterface dialogInterface, int i) {
                    selectChapter(ChapterReference.builder().book(book).chapter(i + 1).build());
                  }
                })
                .create()
                .show();
          }
        });
      }

      void bind(Suggestion suggestion) {
        this.suggestion = suggestion;
        TextView textView = (TextView) view.findViewById(R.id.text);
        if (suggestion.book() != null) {
          textView.setText(suggestion.book().toString());
          selectChapter.setVisibility(View.VISIBLE);
        } else {
          ChapterReference reference = suggestion.chapter();
          textView.setText(reference.book() + " " + reference.chapter());
          selectChapter.setVisibility(View.INVISIBLE);
        }
      }
    }

    private List<Suggestion> results = new ArrayList<>();

    Adapter() {
      results = new ArrayList<>();
    }

    void results(List<Suggestion> results) {
      this.results = results;
      notifyDataSetChanged();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new ViewHolder(LayoutInflater
          .from(parent.getContext())
          .inflate(R.layout.recent_chapter, parent, false));
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
      holder.bind(results.get(position));
    }

    @Override public int getItemCount() {
      return results.size();
    }
  }

  private void selectChapter(ChapterReference chapterReference) {
    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
        .hideSoftInputFromWindow(lookup.getWindowToken(), 0);
    recentChapters.add(chapterReference);
    listener.onChapterSelected(chapterReference);
  }

  @SuppressWarnings("WrongConstant") private void bind(Model model) {
    adapter.results(model.resultsOrEmptyList());
    noRecentMessage.setVisibility(model.noRecentResults());
    noSuggestionsMessage.setVisibility(model.noSuggestions());
    noResultsForSubmittedMessage.setVisibility(model.noResultsFoundForSubmittedSearch());
  }

  @AutoValue static abstract class Model implements Parcelable {
    public abstract String query();
    @Nullable public abstract List<Suggestion> results();

    public List<Suggestion> resultsOrEmptyList() {
      return results() != null? results() : new ArrayList<Suggestion>();
    }

    public int noResultsFoundForSubmittedSearch() {
      return results() == null? VISIBLE : GONE;
    }

    public int noRecentResults() {
      return "".equals(query()) && results() != null && results().size() == 0? VISIBLE : GONE;
    }

    public int noSuggestions() {
      return !"".equals(query()) && results() != null && results().size() == 0? VISIBLE : GONE;
    }

    @AutoValue.Builder abstract static class Builder {
      abstract Builder query(String value);
      abstract Builder results(List<Suggestion> value);
      abstract Model build();
    }

    public static Builder builder() {
      return new AutoValue_SearchFragment_Model.Builder();
    }
  }
}
