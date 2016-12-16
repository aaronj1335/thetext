package com.aaronstacy.thetext.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

final class DbOpenHelper extends SQLiteOpenHelper {
  private static final int VERSION = 1;
  private static final String NAME = "thetext.db";

  private static final String CREATE_CHAPTER = ""
      + "CREATE TABLE " + Chapter.TABLE + " ("
      // Only ESV for now
      + Chapter.TRANSLATION + " INTEGER NOT NULL DEFAULT (0), "
      + Chapter.BOOK + " INTEGER NOT NULL, "
      + Chapter.CHAPTER + " INTEGER NOT NULL, "
      + Chapter.TEXT + " TEXT NOT NULL, "
      + "PRIMARY KEY ("
        + Chapter.TRANSLATION + ", "
        + Chapter.BOOK + ", "
        + Chapter.CHAPTER + "))";

  private static final String CHAPTER_REFERENCE_INDEX = ""
      + "CREATE INDEX chapter_book_chapter ON " + Chapter.TABLE
      + " (" + Chapter.TRANSLATION + ", " + Chapter.BOOK + ", " + Chapter.CHAPTER + ")";

  public DbOpenHelper(Context context) {
    super(context, NAME, null, VERSION);
  }

  @Override public void onCreate(SQLiteDatabase db) {
    db.execSQL(CREATE_CHAPTER);
    db.execSQL(CHAPTER_REFERENCE_INDEX);
  }

  @Override public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    throw new RuntimeException("onUpgrade not implemented for " + NAME);
  }
}
