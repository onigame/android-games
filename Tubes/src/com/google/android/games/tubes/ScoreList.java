package com.google.android.games.tubes;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ScoreList {
  public static final class ScoreColumns implements BaseColumns {
    public static final Uri CONTENT_URI
            = Uri.parse("content://com.google.android.games.tubes.scorelist/scores");

    public static final String DEFAULT_SORT_ORDER = "modified DESC";

    public static final String PUZZLE_ID = "puzzle_id";
    public static final String SCORE = "score";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String CREATED_DATE = "created";
    public static final String MODIFIED_DATE = "modified";
  }
}
