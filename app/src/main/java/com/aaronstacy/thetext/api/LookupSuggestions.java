package com.aaronstacy.thetext.api;

import android.support.annotation.Nullable;

import com.aaronstacy.thetext.db.BookReference;
import com.aaronstacy.thetext.db.ChapterReference;
import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.List;

public final class LookupSuggestions {
  public static List<Suggestion> forQuery(String query, List<ChapterReference> recentChapters) {
    if ("".equals(query)) {
      List<Suggestion> suggestions = new ArrayList<>(recentChapters.size());
      for (ChapterReference chapter : recentChapters) {
        suggestions.add(Suggestion.builder().chapter(chapter).build());
      }
      return suggestions;
    }

    List<Suggestion> suggestsions = new ArrayList<>();

    for (ChapterReference chapter : recentChapters) {
      if (matches(chapter, query)) {
        suggestsions.add(Suggestion.builder().chapter(chapter).build());
      }
    }

    for (String book : BookReference.BOOKS) {
      if (matches(book, query)) {
        suggestsions.add(Suggestion.builder().book(book).build());
      }
    }

    return suggestsions;
  }

  private static boolean matches(String bookString, String prefix) {
    for (String token : bookString.toLowerCase().split("\\s")) {
      if (token.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  private static boolean matches(ChapterReference chapterReference, String prefix) {
    return matches(chapterReference.book().toString(), prefix);
  }

  @AutoValue public static abstract class Suggestion {
    @Nullable public abstract ChapterReference chapter();
    @Nullable public abstract BookReference book();

    @AutoValue.Builder abstract static class Builder {
      public abstract Builder chapter(ChapterReference value);
      public abstract Builder book(BookReference value);

      public Builder book(String value) {
        return book(BookReference.builder().book(value).build());
      }

      abstract Suggestion autoBuild();

      public Suggestion build() {
        Suggestion suggestion = autoBuild();
        if (suggestion.chapter() == null && suggestion.book() == null) {
          throw new IllegalStateException("Either ChapterReference or BookReference must be set.");
        }
        return suggestion;
      }
    }

    public static Builder builder() {
      return new AutoValue_LookupSuggestions_Suggestion.Builder();
    }
  }
}
