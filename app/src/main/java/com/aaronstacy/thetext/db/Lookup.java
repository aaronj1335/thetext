package com.aaronstacy.thetext.db;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

@AutoValue public abstract class Lookup implements Parcelable {
  public abstract BookReference book();
  public abstract int chapter();

  public static Builder builder() {
    return new AutoValue_Lookup.Builder().chapter(1);
  }

  public String toString() {
    return book() + " " + chapter();
  }

  @AutoValue.Builder public abstract static class Builder {
    public abstract Builder book(BookReference value);

    public Builder book(String name) {
      return this.book(BookReference.builder().book(name).build());
    }

    public Builder chapterReference(ChapterReference value) {
      return this.book(value.book()).chapter(value.chapter());
    }

    public abstract Builder chapter(int value);
    public abstract Lookup build();
  }
}
