package com.aaronstacy.thetext;

import com.aaronstacy.thetext.db.ChapterReference;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.text.ParseException;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class ChapterReferenceTest {
  private static final String[] BOOKS = {"Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy",
      "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel", "1 Kings", "2 Kings", "1 Chronicles",
      "2 Chronicles", "Ezra", "Nehemiah", "Esther", "Job", "Psalms", "Proverbs",
      "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah", "Lamentations",
      "Ezekiel", "Daniel", "Hosea", "Joel", "Amos", "Obadiah", "Jonah", "Micah",
      "Nahum", "Habakkuk", "Zephaniah", "Haggai", "Zechariah", "Malachi", "Matthew",
      "Mark", "Luke", "John", "Acts", "Romans", "1 Corinthians", "2 Corinthians",
      "Galatians", "Ephesians", "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians",
      "1 Timothy", "2 Timothy", "Titus", "Philemon", "Hebrews", "James", "1 Peter", "2 Peter",
      "1 John", "2 John", "3 John", "Jude", "Revelation"};
  private final int[] CHAPTER_COUNTS = {50, 40, 27, 36, 34, 24, 21, 4, 31, 24, 22, 25, 29,
      36, 10, 13, 10, 42, 150, 31, 12, 8, 66, 52, 5, 48, 12, 14, 3, 9, 1, 4, 7, 3, 3, 3, 2, 14, 4,
      28, 16, 24, 21, 28, 16, 16, 13, 6, 6, 4, 4, 5, 3, 6, 4, 3, 1, 13, 5, 5, 3, 5, 1, 1, 1, 22};

  @Test public void fromIndexIsCorrect() throws Exception {
    int total = 0;
    for (int i = 0; i < BOOKS.length; i++) {
      for (int j = 0; j < CHAPTER_COUNTS[i]; j++) {
        ChapterReference expected = ChapterReference.builder()
            .book(BOOKS[i])
            .chapter(j + 1)
            .build();
        ChapterReference actual = ChapterReference.fromIndex(total);
        assertEquals(expected, actual);
        total += 1;
      }
    }
  }

  @Test public void builderOfStringIsCorrect() throws Exception {
    assertEquals(
        ChapterReference.builder()
            .of("Romans 3")
            .build(),
        ChapterReference.builder()
            .book("Romans")
            .chapter(3)
            .build());
    assertEquals(
        ChapterReference.builder()
            .of("1 Corinthians 2")
            .build(),
        ChapterReference.builder()
            .book("1 Corinthians")
            .chapter(2)
            .build());
    assertEquals(
        ChapterReference.builder()
            .of("Romans")
            .chapter(3)
            .build(),
        ChapterReference.builder()
            .book("Romans")
            .chapter(3)
            .build());
    assertEquals(
        ChapterReference.builder()
            .book("Romans")
            .of("3")
            .build(),
        ChapterReference.builder()
            .book("Romans")
            .chapter(3)
            .build());
  }

  @Test(expected = ParseException.class) public void builderOfStringThrows() throws Exception {
    ChapterReference.builder().of("not a book").build();
  }

  @Test public void toIndexIsCorrect() {
    for (int i = 0; i < ChapterReference.CHAPTER_COUNT; i++) {
      assertEquals(ChapterReference.fromIndex(i).toIndex(), i);
    }
  }
}