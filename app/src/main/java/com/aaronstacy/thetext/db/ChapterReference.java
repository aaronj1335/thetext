package com.aaronstacy.thetext.db;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ChapterReference implements Parcelable {
  public static final int CHAPTER_COUNT = BookReference.CHAPTER_SUMS[BookReference.BOOKS.length - 1]
      + BookReference.CHAPTER_COUNTS[BookReference.BOOKS.length - 1];

  public abstract BookReference book();
  public abstract int chapter();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder book(BookReference value);

    public Builder book(String value) {
      return this.book(BookReference.builder().book(value).build());
    }

    public Builder book(int value) {
      return this.book(BookReference.builder().index(value).build());
    }

    public abstract Builder chapter(int value);
    public abstract ChapterReference build();
  }

  public static Builder builder() {
    return new AutoValue_ChapterReference.Builder();
  }

  public static ChapterReference fromIndex(int index) {
    int lo = 0;
    int hi = BookReference.BOOKS.length;
    int position = getPosition(halfway(lo, hi), index);

    while (position != 0) {
      if (position == 1) {
        lo = halfway(lo, hi);
      } else {
        hi = halfway(lo, hi);
      }

      position = getPosition(halfway(lo, hi), index);
    }

    return ChapterReference.builder()
        .book(halfway(lo, hi))
        .chapter(index - BookReference.CHAPTER_SUMS[halfway(lo, hi)] + 1)
        .build();
  }

  private static int getPosition(int bookIndex, int chapterIndex) {
    int chaptersBeforeBook = BookReference.CHAPTER_SUMS[bookIndex];
    int chaptersInBook = BookReference.CHAPTER_COUNTS[bookIndex];
    if (chapterIndex >= chaptersBeforeBook) {
      if (chapterIndex < chaptersBeforeBook + chaptersInBook) {
        return 0;
      } else {
        return 1;
      }
    } else {
      return -1;
    }
  }

  private static int halfway(int lo, int hi) {
    return lo + (hi - lo) / 2;
  }
}
