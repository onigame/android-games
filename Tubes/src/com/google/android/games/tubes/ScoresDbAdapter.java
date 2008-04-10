package com.google.android.games.tubes;

import java.io.FileNotFoundException;
import java.util.HashMap;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Resources;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class ScoresDbAdapter {
  
  private static final String DATABASE_NAME = "tubes_scores.db";
  private static final String DATABASE_TABLE = "scores";
  private static final int DATABASE_VERSION = 2;
  private static final String DATABASE_CREATE =
    "CREATE TABLE " + DATABASE_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "puzzle_id INTEGER, score INTEGER, width INTEGER, height INTEGER,"
                + "created INTEGER, modified INTEGER" + ")";

  private SQLiteDatabase mDb;
  private final Context mCtx;

  private static HashMap<String, String> NOTES_LIST_PROJECTION_MAP;
  static {
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
  
  /**
   * Constructor - takes the context to allow the database to be opened/created
   * @param ctx the Context within which to work
   */
  public ScoresDbAdapter(Context ctx) {
      this.mCtx = ctx;
  }
  
  /**
   * Open the notes database. If it cannot be opened, try to create a new instance of
   * the database. If it cannot be created, throw an exception to signal the failure
   * @return this (self reference, allowing this to be chained in an initialization call)
   * @throws SQLException if the database could be neither opened or created
   */
  public ScoresDbAdapter open() throws SQLException {
      try {
          mDb = mCtx.openDatabase(DATABASE_NAME, null);
      } catch (FileNotFoundException e) {
          try {
              mDb =
                  mCtx.createDatabase(DATABASE_NAME, DATABASE_VERSION, 0,
                      null);
              mDb.execSQL(DATABASE_CREATE);
          } catch (FileNotFoundException e1) {
              throw new SQLException("Could not create database");
          }
      }
      return this;
  }

  public void close() {
      mDb.close();
  }

  public Uri insert(Uri url, ContentValues initialValues) {
    Log.e("foo", "I'm trying to insert!!!");
    long rowID;
    ContentValues values;
    if (initialValues != null) {
        values = new ContentValues(initialValues);
    } else {
        values = new ContentValues();
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
    values.put(ScoreList.ScoreColumns.SCORE, 232);
    
    if (values.containsKey(ScoreList.ScoreColumns.WIDTH) == false) {
      values.put(ScoreList.ScoreColumns.WIDTH, 7);
  }

    if (values.containsKey(ScoreList.ScoreColumns.HEIGHT) == false) {
      values.put(ScoreList.ScoreColumns.HEIGHT, 7);
  }


    rowID = mDb.insert(DATABASE_TABLE, null, values);
    if (rowID > 0) {
        Uri uri = ContentUris.withAppendedId(ScoreList.ScoreColumns.CONTENT_URI, rowID);
        return uri;
    }

    throw new SQLException("Failed to insert row into " + url);
  }

  /**
   * Return a Cursor over the list of all notes in the database
   * @return Cursor over all notes
   */
  public Cursor fetchAllNotes() {
    return mDb.query(DATABASE_TABLE, new String[] {
        ScoreList.ScoreColumns._ID, 
        ScoreList.ScoreColumns.PUZZLE_ID, 
        ScoreList.ScoreColumns.SCORE, 
        ScoreList.ScoreColumns.WIDTH, 
        ScoreList.ScoreColumns.HEIGHT, 
        ScoreList.ScoreColumns.CREATED_DATE, 
        ScoreList.ScoreColumns.MODIFIED_DATE
      }, null, null, null, null, null);
  }

}
