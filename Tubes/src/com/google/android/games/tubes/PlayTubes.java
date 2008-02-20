package com.google.android.games.tubes;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.Window;

public class PlayTubes extends Activity {
	
	private GridView mGridView;
	
	private static String ICICLE_KEY = "tubes-grid-view";
	
    /** Called when the activity is first created.
     * Turns off the title bar, sets up the content views,
     * and fires up the GridView.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // No Title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.grid_layout);

        mGridView = (GridView) findViewById(R.id.grid);
        mGridView.setTextView((TextView) findViewById(R.id.text));

        if (icicle == null) {
            // We were just launched -- set up a new game
        	mGridView.setMode(GridView.READY);
        } else {
            // We are being restored
            Bundle map = icicle.getBundle(ICICLE_KEY);
            if (map != null) {
            	mGridView.restoreState(map);
            } else {
            	mGridView.setMode(GridView.PAUSE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the game along with the activity
        mGridView.setMode(GridView.PAUSE);
    }

    @Override
    public void onFreeze(Bundle outState) {
        //Store the game state
        outState.putBundle(ICICLE_KEY, mGridView.saveState());
    }
}