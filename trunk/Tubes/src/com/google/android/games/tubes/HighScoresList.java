/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.games.tubes;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class HighScoresList extends ListActivity {
    private static final int ACTIVITY_EDIT=0;
    
    private static final int SORT_BY_PUZZLE_ID = Menu.FIRST;
    private static final int SORT_BY_MOVES = Menu.FIRST + 1;

    private NotesDbAdapter mDbHelper;
    private Cursor mNotesCursor;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.high_score_list);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (action.equals(Intent.INSERT_ACTION)) {
            Bundle b = intent.getExtras();
            mDbHelper.addData(b);
        } else {
            // Whoops, unknown action!  Bail.
            finish();
            return;
        }

        Button confirmButton = (Button) findViewById(R.id.view_scores_confirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {

          public void onClick(View view) {
              setResult(RESULT_OK, null, null);
              finish();
          }
          
      });
        
        fillData();
    }
    
    private void fillData() {
        // Get all of the rows from the database and create the item list
        mNotesCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(mNotesCursor);
        
        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{NotesDbAdapter.KEY_TITLE};
        
        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};
        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = 
        	    new SimpleCursorAdapter(this, R.layout.high_score, mNotesCursor, from, to);
        setListAdapter(notes);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, SORT_BY_PUZZLE_ID, "Menu insert String");
        menu.add(0, SORT_BY_MOVES, "Menu delete string");
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, Item item) {
        switch(item.getId()) {
        case SORT_BY_PUZZLE_ID:
            Log.e("High Score List", "User requested sort by ID, not implemented yet.");
            return true;
        case SORT_BY_MOVES:
            Log.e("High Score List", "User requested sort by moves, not implemented yet.");
	        return true;
	    }        
        return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
        Cursor c = mNotesCursor;
        c.moveTo(position);
        Intent i = new Intent(this, HighScoreView.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        i.putExtra(NotesDbAdapter.KEY_TITLE, c.getString(
                c.getColumnIndex(NotesDbAdapter.KEY_TITLE)));
        i.putExtra(NotesDbAdapter.KEY_BODY, c.getString(
                c.getColumnIndex(NotesDbAdapter.KEY_BODY)));
        startSubActivity(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, String data, Bundle extras) {
        super.onActivityResult(requestCode, resultCode, data, extras);
        
        switch(requestCode) {
        case ACTIVITY_EDIT:
            Log.e("High Score List", "Successfully viewed high score list.");
            break;
        }
    }
}
