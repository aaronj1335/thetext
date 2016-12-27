package com.aaronstacy.thetext.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.aaronstacy.thetext.R;
import com.aaronstacy.thetext.TheTextApp;
import com.aaronstacy.thetext.api.RecentChapters;
import com.aaronstacy.thetext.db.ChapterReference;
import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

public final class SearchFragment
    extends Fragment
    implements TextView.OnEditorActionListener, TextWatcher {
  private static final String SEARCH_TEXT = "SEARCH_TEXT";
  private EditText lookup;
  private Adapter adapter;
  private final BehaviorSubject<String> searchText = BehaviorSubject.create("");
  @SuppressWarnings("WeakerAccess") @Inject RecentChapters recentChapters;
  private Subscription subscription;
  private OnChapterListener listener;

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

    RecyclerView results = (RecyclerView) view.findViewById(R.id.results);
    results.setHasFixedSize(true);
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    results.setLayoutManager(layoutManager);
    adapter = new Adapter();
    results.setAdapter(adapter);

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

    subscription = searchText
        .flatMap(new Func1<String, Observable<List<ChapterReference>>>() {
          @Override public Observable<List<ChapterReference>> call(String query) {
            if ("".equals(query)) {
              return recentChapters.asObservable();
            } else {
              List<ChapterReference> searchResults = new ArrayList<>();
              searchResults.add(ChapterReference.builder().book("Psalms").chapter(37).build());
              searchResults.add(ChapterReference.builder().book("Proverbs").chapter(3).build());
              return Observable.just(searchResults);
            }
          }
        })
        .map(new Func1<List<ChapterReference>, Model>() {
          @Override public Model call(List<ChapterReference> chapterReferences) {
            return Model.builder().results(chapterReferences).build();
          }
        })
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
      private ChapterReference chapterReference;

      ViewHolder(View view) {
        super(view);
        this.view = view;
        view.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View view) {
            listener.onChapterSelected(chapterReference);
          }
        });
      }

      void bind(ChapterReference ref) {
        chapterReference = ref;
        ((TextView) view.findViewById(R.id.text)).setText(ref.book() + " " + ref.chapter());
      }
    }

    private List<ChapterReference> results = new ArrayList<>();

    Adapter() {
      results = new ArrayList<>();
    }

    void results(List<ChapterReference> results) {
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

  private void bind(Model model) {
    adapter.results(model.results());
  }

  @AutoValue static abstract class Model implements Parcelable {
    public abstract List<ChapterReference> results();

    @AutoValue.Builder abstract static class Builder {
      abstract Builder results(List<ChapterReference> value);
      abstract Model build();
    }

    public static Builder builder() {
      return new AutoValue_SearchFragment_Model.Builder();
    }
  }
}
