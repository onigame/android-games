package com.google.android.games.tubes;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.games.tubes.ScoreList;

public class ScoresActivity extends Activity {
  private static final String TAG = "ScoresActivity";
  
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

      final Intent intent = getIntent();
      final String type = intent.resolveType(this);

      // Do some setup based on the action being performed.

      final String action = intent.getAction();
      if (action.equals(Intent.INSERT_ACTION)) {
          // Requested to insert: set that state, and create a new entry
          // in the container.
          mURI = getContentResolver().insert(intent.getData(), null);

          // If we were unable to write a new score, then just finish
          // this activity.  A RESULT_CANCELED will be sent back to the
          // original activity if they requested a result.
          if (mURI == null) {
              Log.e(TAG, "Failed to insert new score into "
                      + getIntent().getData());
              finish();
              return;
          }

          // The new entry was created, so assume all will end well and
          // set the result to be returned.
          setResult(RESULT_OK, mURI.toString());
          
          Log.e(TAG, "insert insert");

      } else {
          // Whoops, unknown action!  Bail.
          Log.e(TAG, "Unknown action, exiting");
          finish();
          return;
      }
      
      setContentView(R.layout.scores);
     
      // Get the note!
      mCursor = managedQuery(mURI, PROJECTION, null, null);
  }

  
}
