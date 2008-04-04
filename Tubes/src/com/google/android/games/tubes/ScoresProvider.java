package com.google.android.games.tubes;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Resources;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


public class ScoresProvider extends ContentProvider {

  private SQLiteDatabase mDB;

  private static final String TAG = "ScoresProvider";
  private static final String DATABASE_NAME = "tubes_scores.db";
  private static final int DATABASE_VERSION = 1;

  private static HashMap<String, String> NOTES_LIST_PROJECTION_MAP;

  private static final int SCORES = 1;
  private static final int SCORE_ID = 2;

  private static final UriMatcher URL_MATCHER;

  private static class DatabaseHelper extends SQLiteOpenHelper {

      @Override
      public void onCreate(SQLiteDatabase db) {
          db.execSQL("CREATE TABLE scores (_id INTEGER PRIMARY KEY,"
                  + "puzzle_id INTEGER, score INTEGER, width INTEGER, height INTEGER,"
                  + "created INTEGER, modified INTEGER" + ");");
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
          Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                  + newVersion + ", which will destroy all old data");
          db.execSQL("DROP TABLE IF EXISTS scores");
          onCreate(db);
      }
  }

  @Override
  public boolean onCreate() {
      DatabaseHelper dbHelper = new DatabaseHelper();
      mDB = dbHelper.openDatabase(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
      return (mDB == null) ? false : true;
  }

  @Override
  public Cursor query(Uri url, String[] projection, String selection,
          String[] selectionArgs, String sort) {
      SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

      switch (URL_MATCHER.match(url)) {
      case SCORES:
          qb.setTables("scores");
          qb.setProjectionMap(NOTES_LIST_PROJECTION_MAP);
          break;

      case SCORE_ID:
          qb.setTables("scores");
          qb.appendWhere("_id=" + url.getPathSegments().get(1));
          break;

      default:
          throw new IllegalArgumentException("Unknown URL " + url);
      }

      // If no sort order is specified use the default
      String orderBy;
      if (TextUtils.isEmpty(sort)) {
          orderBy = ScoreList.ScoreColumns.DEFAULT_SORT_ORDER;
      } else {
          orderBy = sort;
      }

      Cursor c = qb.query(mDB, projection, selection, selectionArgs, null, null, orderBy);
      c.setNotificationUri(getContext().getContentResolver(), url);
      return c;
  }

  @Override
  public String getType(Uri url) {
      switch (URL_MATCHER.match(url)) {
      case SCORES:
          return "vnd.android.cursor.dir/vnd.google.note";

      case SCORE_ID:
          return "vnd.android.cursor.item/vnd.google.note";

      default:
          throw new IllegalArgumentException("Unknown URL " + url);
      }
  }

  @Override
  public Uri insert(Uri url, ContentValues initialValues) {
      long rowID;
      ContentValues values;
      if (initialValues != null) {
          values = new ContentValues(initialValues);
      } else {
          values = new ContentValues();
      }

      if (URL_MATCHER.match(url) != SCORES) {
          throw new IllegalArgumentException("Unknown URL " + url);
      }

      Long now = Long.valueOf(System.currentTimeMillis());
      Resources r = Resources.getSystem();

      // Make sure that the fields are all set
      if (values.containsKey(ScoreList.ScoreColumns.CREATED_DATE) == false) {
          values.put(ScoreList.ScoreColumns.CREATED_DATE, now);
      }

      if (values.containsKey(ScoreList.ScoreColumns.MODIFIED_DATE) == false) {
          values.put(ScoreList.ScoreColumns.MODIFIED_DATE, now);
      }

      if (values.containsKey(ScoreList.ScoreColumns.PUZZLE_ID) == false) {
        values.put(ScoreList.ScoreColumns.PUZZLE_ID, 123);
    }

      if (values.containsKey(ScoreList.ScoreColumns.SCORE) == false) {
        values.put(ScoreList.ScoreColumns.SCORE, 232);
    }

      if (values.containsKey(ScoreList.ScoreColumns.WIDTH) == false) {
        values.put(ScoreList.ScoreColumns.WIDTH, 7);
    }

      if (values.containsKey(ScoreList.ScoreColumns.HEIGHT) == false) {
        values.put(ScoreList.ScoreColumns.HEIGHT, 7);
    }


      rowID = mDB.insert("notes", "note", values);
      if (rowID > 0) {
          Uri uri = ContentUris.withAppendedId(ScoreList.ScoreColumns.CONTENT_URI, rowID);
          getContext().getContentResolver().notifyChange(uri, null);
          return uri;
      }

      throw new SQLException("Failed to insert row into " + url);
  }

  @Override
  public int delete(Uri url, String where, String[] whereArgs) {
      int count;
      long rowId = 0;
      switch (URL_MATCHER.match(url)) {
      case SCORES:
          count = mDB.delete("note_pad", where, whereArgs);
          break;

      case SCORE_ID:
          String segment = url.getPathSegments().get(1);
          rowId = Long.parseLong(segment);
          count = mDB
                  .delete("notes", "_id="
                          + segment
                          + (!TextUtils.isEmpty(where) ? " AND (" + where
                                  + ')' : ""), whereArgs);
          break;

      default:
          throw new IllegalArgumentException("Unknown URL " + url);
      }

      getContext().getContentResolver().notifyChange(url, null);
      return count;
  }

  @Override
  public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
      int count;
      switch (URL_MATCHER.match(url)) {
      case SCORES:
          count = mDB.update("notes", values, where, whereArgs);
          break;

      case SCORE_ID:
          String segment = url.getPathSegments().get(1);
          count = mDB
                  .update("notes", values, "_id="
                          + segment
                          + (!TextUtils.isEmpty(where) ? " AND (" + where
                                  + ')' : ""), whereArgs);
          break;

      default:
          throw new IllegalArgumentException("Unknown URL " + url);
      }

      getContext().getContentResolver().notifyChange(url, null);
      return count;
  }

  static {
      URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
      URL_MATCHER.addURI("com.google.provider.NotePad", "notes", SCORES);
      URL_MATCHER.addURI("com.google.provider.NotePad", "notes/#", SCORE_ID);

      NOTES_LIST_PROJECTION_MAP = new HashMap<String, String>();
      String _ID = "_id";
      NOTES_LIST_PROJECTION_MAP.put(ScoreList.ScoreColumns._ID, "_id");
      NOTES_LIST_PROJECTION_MAP.put(ScoreList.ScoreColumns.PUZZLE_ID, "puzzle_id");
      NOTES_LIST_PROJECTION_MAP.put(ScoreList.ScoreColumns.SCORE, "score");
      NOTES_LIST_PROJECTION_MAP.put(ScoreList.ScoreColumns.WIDTH, "width");
      NOTES_LIST_PROJECTION_MAP.put(ScoreList.ScoreColumns.HEIGHT, "height");
      NOTES_LIST_PROJECTION_MAP.put(ScoreList.ScoreColumns.CREATED_DATE, "created");
      NOTES_LIST_PROJECTION_MAP.put(ScoreList.ScoreColumns.MODIFIED_DATE, "modified");
  }
}
