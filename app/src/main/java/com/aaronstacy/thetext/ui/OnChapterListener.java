package com.aaronstacy.thetext.ui;

import com.aaronstacy.thetext.db.ChapterReference;

interface OnChapterListener {
  void onChapterSelected(ChapterReference chapterReference);
}
