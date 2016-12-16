package com.aaronstacy.thetext.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import rx.functions.Func1;

@AutoValue
public abstract class Chapter implements Parcelable {
  public static final String TABLE = "chapter";

  static final String TRANSLATION = "translation";
  static final String BOOK = "book";
  static final String CHAPTER = "chapter";
  static final String TEXT = "text";

  public static final String CHAPTER_QUERY = "SELECT * FROM " + TABLE + " WHERE " + TRANSLATION
      + "= 0 AND " + BOOK + " = ? AND " + CHAPTER + "= ?;";

  public abstract long translation();
  public abstract ChapterReference chapterReference();
  public abstract String text();

  public static final Func1<Cursor, Chapter> MAPPER = new Func1<Cursor, Chapter>() {
    @Override
    public Chapter call(Cursor cursor) {
      return builder()
          .translation(cursor.getLong(cursor.getColumnIndexOrThrow(TRANSLATION)))
          .book(cursor.getInt(cursor.getColumnIndexOrThrow(BOOK)))
          .chapter(cursor.getInt(cursor.getColumnIndexOrThrow(CHAPTER)))
          .text(cursor.getString(cursor.getColumnIndexOrThrow(TEXT)))
          .build();
    }
  };

  public ContentValues toContentValues() {
    ContentValues contentValues = new ContentValues();
    contentValues.put(TRANSLATION, translation());
    contentValues.put(BOOK, chapterReference().book().toString());
    contentValues.put(CHAPTER, chapterReference().chapter());
    contentValues.put(TEXT, text());
    return contentValues;
  }

  public static Builder builder() {
    return new AutoValue_Chapter.Builder().translation(0);
  }

  public static class Builder {
    private final ChapterReference.Builder chapterReference;
    private long _translation = 0;
    private String _text;

    public Builder() {
      chapterReference = ChapterReference.builder();
    }

    public Builder translation(long value) {
      _translation = value;
      return this;
    }

    public Builder book(BookReference value) {
      chapterReference.book(value);
      return this;
    }

    public Builder book(String value) {
      chapterReference.book(value);
      return this;
    }

    public Builder book(int value) {
      chapterReference.book(value);
      return this;
    }

    public Builder chapter(ChapterReference value) {
      chapterReference.book(value.book()).chapter(value.chapter());
      return this;
    }

    public Builder chapter(int value) {
      chapterReference.chapter(value);
      return this;
    }

    public Builder text(String value) {
      _text = value;
      return this;
    }

    public Chapter build() {
      String missing = "";
      if (_text == null) {
        missing += " text";
      }
      if (!missing.isEmpty()) {
        throw new IllegalStateException("missing required properties " + missing);
      }
      return new AutoValue_Chapter(_translation, chapterReference.build(), _text);
    }
  }
}
