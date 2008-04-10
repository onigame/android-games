package com.google.android.games.tubes;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

import com.google.android.games.tubes.ScoreList;

public class ScoresActivity extends ListActivity {
  private static final String TAG = "ScoresActivity";

  private ScoresDbAdapter mDbAdapter;
  private Uri mURI;
  private Cursor mCursor;
  
  private static final String[] PROJECTION = new String[] {
          ScoreList.ScoreColumns._ID, // 0
          ScoreList.ScoreColumns.PUZZLE_ID, // 1
          ScoreList.ScoreColumns.SCORE, // 2
          ScoreList.ScoreColumns.WIDTH, // 3
          ScoreList.ScoreColumns.HEIGHT, // 4
          ScoreList.ScoreColumns.MODIFIED_DATE // 5
  };
  
  @Override
  protected void onCreate(Bundle icicle) {
      super.onCreate(icicle);
      
      mDbAdapter = new ScoresDbAdapter(this);
      mDbAdapter.open();
      
      final Intent intent = getIntent();
      final String type = intent.resolveType(this);

      // Do some setup based on the action being performed.

      final String action = intent.getAction();
      if (action.equals(Intent.INSERT_ACTION)) {
          // Requested to insert: set that state, and create a new entry
          // in the container.
          Uri u = intent.getData();
          mURI = mDbAdapter.insert(u, null);

          // If we were unable to write a new score, then just finish
          // this activity.  A RESULT_CANCELED will be sent back to the
          // original activity if they requested a result.
          if (mURI == null) {
              Log.e(TAG, "Failed to insert new score");
              finish();
              return;
          }

          // The new entry was created, so assume all will end well and
          // set the result to be returned.
          setResult(RESULT_OK, mURI.toString());
          
      } else if (action.equals(Intent.VIEW_ACTION)) {
          
      } else {
          // Whoops, unknown action!  Bail.
          finish();
          return;
      }
      
      setContentView(R.layout.scores);  
      // Get all of the rows from the database and create the item list
      mCursor = mDbAdapter.fetchAllNotes();
      startManagingCursor(mCursor);
      
      String[] from = new String[]{ScoreList.ScoreColumns.SCORE};
      
      // and an array of the fields we want to bind those fields to
      int[] to = new int[]{R.id.score_row};
      
      // Now create a simple cursor adapter and set it to display
      SimpleCursorAdapter notes = 
              new SimpleCursorAdapter(this, R.layout.scores, mCursor, from, to);
      setListAdapter(notes);      
        
  }

  
}
